package com.ilyrash.loadston;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LoadStone implements ModInitializer {
    public static final String MOD_ID = "loadstone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Track active loaders: position -> radius
    private static final Map<BlockPos, Integer> activeLoaders = new HashMap<>();
    // Track recently toggled loaders to prevent double-activation
    private static final Set<BlockPos> recentlyToggled = new HashSet<>();

    // Chunk radius constants
    private static final int IRON_RADIUS = 0;    // 1x1 chunks
    private static final int DIAMOND_RADIUS = 1; // 3x3 chunks
    private static final int NETHERITE_RADIUS = 3; // 9x9 chunks

    @Override
    public void onInitialize() {
        LOGGER.info("Load Stone chunk loader mod initialized!");
        registerEvents();
    }

    private void registerEvents() {
        // Check when player interacts with lodestone
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            // Only process main hand to avoid duplicate events
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.isOf(Blocks.LODESTONE)) {
                // Check if this lodestone should be a chunk loader
                return checkAndToggleChunkLoader(world, pos, player, hand);
            }
            return ActionResult.PASS;
        });

        // Keep track of loaded worlds
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                LOGGER.info("Overworld loaded, potentially reloading chunk loaders");
            }
        });

        // Clean up when server stops
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            activeLoaders.clear();
            recentlyToggled.clear();
            LOGGER.info("Cleared all chunk loaders on server shutdown");
        });

        // Clear recently toggled set every tick to allow new interactions
        ServerTickEvents.START_SERVER_TICK.register(server -> recentlyToggled.clear());

        // Periodic maintenance to ensure chunks stay loaded
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (!activeLoaders.isEmpty() && world.getRegistryKey() == World.OVERWORLD) {
                maintainChunkLoading(world);
            }
        });
    }

    private ActionResult checkAndToggleChunkLoader(World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, Hand hand) {
        // Prevent double-activation in the same tick
        if (recentlyToggled.contains(pos)) {
            return ActionResult.PASS;
        }

        ItemStack heldItem = player.getStackInHand(hand);
        int radius = getRadiusFromItem(heldItem);

        // If lodestone is already active
        if (activeLoaders.containsKey(pos)) {
            // Check if player is trying to change the radius with a different ore
            if (radius != -1 && radius != activeLoaders.get(pos)) {
                // Upgrade/downgrade the chunk loader
                return changeChunkLoaderRadius(world, pos, player, radius, heldItem);
            } else {
                // Deactivate the chunk loader
                return deactivateChunkLoader(world, pos, player);
            }
        } else {
            // Activate new chunk loader
            if (radius == -1) {
                player.sendMessage(Text.literal("§6Hold iron, diamond, or netherite to activate chunk loader"), false);
                return ActionResult.PASS;
            }
            return activateChunkLoader(world, pos, player, radius, heldItem);
        }
    }

    private ActionResult activateChunkLoader(World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, int radius, ItemStack heldItem) {
        // Check if player has the required item (if not in creative)
        if (!player.isCreative() && heldItem.getCount() < 1) {
            player.sendMessage(Text.literal("§cYou need the required item to activate the chunk loader!"), false);
            return ActionResult.FAIL;
        }

        recentlyToggled.add(pos);

        // Activate the chunk loader
        activateChunkLoader(world, pos, radius);

        // Consume the activation item if not in creative
        if (!player.isCreative()) {
            heldItem.decrement(1);
        }

        String radiusName = getRadiusName(radius);
        player.sendMessage(Text.literal("§a" + radiusName + " chunk loader activated! Cost: " + getItemName(radius)), false);
        return ActionResult.SUCCESS;
    }

    private ActionResult deactivateChunkLoader(World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player) {
        recentlyToggled.add(pos);

        // Get the radius before deactivating
        int oldRadius = activeLoaders.get(pos);

        // Deactivate the chunk loader
        deactivateChunkLoader(world, pos);

        // Return the activation item if not in creative
        if (!player.isCreative()) {
            ItemStack returnItem = getItemFromRadius(oldRadius);
            if (!returnItem.isEmpty()) {
                if (!player.giveItemStack(returnItem)) {
                    // If player inventory is full, drop the item
                    player.dropItem(returnItem, false);
                }
            }
        }

        player.sendMessage(Text.literal("§cChunk loader deactivated! Item returned."), false);
        return ActionResult.SUCCESS;
    }

    private ActionResult changeChunkLoaderRadius(World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, int newRadius, ItemStack heldItem) {
        recentlyToggled.add(pos);

        // Get the old radius and item
        int oldRadius = activeLoaders.get(pos);
        ItemStack oldItem = getItemFromRadius(oldRadius);

        // Check if player has the new item (if not in creative)
        if (!player.isCreative() && heldItem.getCount() < 1) {
            player.sendMessage(Text.literal("§cYou need the required item to change the chunk loader radius!"), false);
            return ActionResult.FAIL;
        }

        // Deactivate old chunks
        deactivateChunkLoader(world, pos);

        // Activate with new radius
        activateChunkLoader(world, pos, newRadius);

        // Handle items
        if (!player.isCreative()) {
            // Consume the new item
            heldItem.decrement(1);

            // Return the old item
            if (!oldItem.isEmpty()) {
                if (!player.giveItemStack(oldItem)) {
                    player.dropItem(oldItem, false);
                }
            }
        }

        String oldRadiusName = getRadiusName(oldRadius);
        String newRadiusName = getRadiusName(newRadius);
        player.sendMessage(Text.literal("§6Chunk loader radius changed from " + oldRadiusName + " to " + newRadiusName + "!"), false);

        return ActionResult.SUCCESS;
    }

    private int getRadiusFromItem(ItemStack stack) {
        if (stack.isEmpty()) return -1;

        if (stack.isOf(Items.IRON_INGOT)) return IRON_RADIUS;
        if (stack.isOf(Items.DIAMOND)) return DIAMOND_RADIUS;
        if (stack.isOf(Items.NETHERITE_INGOT)) return NETHERITE_RADIUS;

        return -1;
    }

    private ItemStack getItemFromRadius(int radius) {
        return switch (radius) {
            case IRON_RADIUS -> new ItemStack(Items.IRON_INGOT);
            case DIAMOND_RADIUS -> new ItemStack(Items.DIAMOND);
            case NETHERITE_RADIUS -> new ItemStack(Items.NETHERITE_INGOT);
            default -> ItemStack.EMPTY;
        };
    }

    private String getRadiusName(int radius) {
        int chunks = 2 * radius + 1;
        return chunks + "x" + chunks;
    }

    private String getItemName(int radius) {
        return switch (radius) {
            case IRON_RADIUS -> "Iron Ingot";
            case DIAMOND_RADIUS -> "Diamond";
            case NETHERITE_RADIUS -> "Netherite Ingot";
            default -> "Unknown";
        };
    }

    public static void activateChunkLoader(World world, BlockPos pos, int radius) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Calculate chunk area based on radius
        ChunkPos centerChunk = new ChunkPos(pos);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ChunkPos chunkPos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                addChunkTicket(serverWorld, chunkPos);
            }
        }

        activeLoaders.put(pos.toImmutable(), radius);

        // Visual feedback - different particles based on radius
        var particleType = switch (radius) {
            case IRON_RADIUS -> net.minecraft.particle.ParticleTypes.SMOKE;
            case NETHERITE_RADIUS -> net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME;
            default -> net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK;
        };

        int particleCount = (radius + 1) * 10; // More particles for larger radii

        serverWorld.spawnParticles(
                particleType,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                particleCount, 0.5, 0.5, 0.5, 0.1
        );

        String radiusName = getRadiusNameStatic(radius);
        LOGGER.info("Activated {} chunk loader at {}, forcing {} chunks to stay loaded",
                radiusName, pos, (2 * radius + 1) * (2 * radius + 1));
    }

    private static String getRadiusNameStatic(int radius) {
        int chunks = 2 * radius + 1;
        return chunks + "x" + chunks;
    }

    public static void deactivateChunkLoader(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Integer radius = activeLoaders.remove(pos);
        if (radius != null) {
            ChunkPos centerChunk = new ChunkPos(pos);

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    ChunkPos chunkPos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                    removeChunkTicket(serverWorld, chunkPos);
                }
            }

            // Visual feedback for deactivation
            serverWorld.spawnParticles(
                    net.minecraft.particle.ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    10, 0.5, 0.5, 0.5, 0.1
            );
        }

        LOGGER.info("Deactivated chunk loader at {}", pos);
    }

    private static void addChunkTicket(ServerWorld world, ChunkPos chunkPos) {
        // Force the chunk to load and stay loaded
        world.setChunkForced(chunkPos.x, chunkPos.z, true);

        // Access the chunk to ensure it's loaded and mark it for saving
        WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
        if (chunk != null) {
            chunk.markNeedsSaving();
        }
    }

    private static void removeChunkTicket(ServerWorld world, ChunkPos chunkPos) {
        world.setChunkForced(chunkPos.x, chunkPos.z, false);
    }

    private void maintainChunkLoading(World world) {
        // Ensure chunks stay loaded by periodically accessing them
        for (Map.Entry<BlockPos, Integer> entry : activeLoaders.entrySet()) {
            BlockPos loaderPos = entry.getKey();
            int radius = entry.getValue();

            ChunkPos centerChunk = new ChunkPos(loaderPos);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    ChunkPos chunkPos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                    // This access helps keep chunks loaded
                    world.getChunk(chunkPos.x, chunkPos.z);
                }
            }
        }
    }

    public static boolean isChunkLoaderActive(BlockPos pos) {
        return activeLoaders.containsKey(pos);
    }
}