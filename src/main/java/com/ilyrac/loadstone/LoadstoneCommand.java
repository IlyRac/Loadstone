package com.ilyrac.loadstone;

import com.ilyrac.loadstone.loader.ChunkLoaderManager;
import com.ilyrac.loadstone.loader.LoaderTier;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class LoadstoneCommand {

    private LoadstoneCommand() {}

    public static void Initializer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(
                Commands.literal("loadstone")
                        .requires(source -> {
                            if (source.getEntity() instanceof ServerPlayer player) {
                                return source.getServer().getPlayerList().isOp(player.nameAndId());
                            }
                            return true;
                        })

                        // /loadstone list
                        .then(Commands.literal("list")
                                .executes(context -> executeList(context.getSource()))
                        )

                        // /loadstone deactivate
                        .then(Commands.literal("deactivate")
                                .then(Commands.literal("all")
                                        .executes(context -> executeDeactivateAll(context.getSource()))
                                )
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> executeDeactivatePos(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos")
                                        ))
                                )
                        )

                        // NEW BRANCH: /loadstone activate <pos> <tier>
                        .then(Commands.literal("activate")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("tier", StringArgumentType.word())
                                                .suggests(LoadstoneCommand::suggestTiers) // Add autocomplete tab completion
                                                .executes(context -> executeActivate(
                                                        context.getSource(),
                                                        BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "tier")
                                                ))
                                        )
                                )
                        )
        ));
    }

    // Tab-completion helper
    private static CompletableFuture<Suggestions> suggestTiers(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(LoaderTier.values()).map(tier -> tier.name().toLowerCase(Locale.ROOT)),
                builder
        );
    }

    private static int executeActivate(CommandSourceStack source, BlockPos pos, String tierString) {
        ServerLevel world = source.getLevel();

        // 1. Sanity check for vanilla Lodestone block
        if (!world.getBlockState(pos).is(Blocks.LODESTONE)) {
            source.sendFailure(Component.literal(String.format("Cannot activate: There is no Lodestone block at [%d, %d, %d].",
                    pos.getX(), pos.getY(), pos.getZ())));
            return 0;
        }

        LoaderTier tier;
        try {
            tier = LoaderTier.valueOf(tierString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid tier name! Must be Iron, Diamond, or Netherite."));
            return 0;
        }

        boolean wasAlreadyActive = ChunkLoaderManager.isActive(world, pos);
        LoaderTier oldTier = wasAlreadyActive ? ChunkLoaderManager.snapshot(world).get(pos) : null;

        if (wasAlreadyActive && oldTier == tier) {
            source.sendFailure(Component.literal(String.format("Loader at [%d, %d, %d] is already activated with %s tier.",
                    pos.getX(), pos.getY(), pos.getZ(), tier.name())));
            return 0;
        }

        if (wasAlreadyActive) {
            ChunkLoaderManager.deactivate(world, pos);
        }

        // 2. Logic check: check for overlaps from OTHER nearby loaders
        if (ChunkLoaderManager.canActivate(world, pos, tier)) {
            // If it overlaps with an external loader, rollback and turn the old one back on
            if (wasAlreadyActive) {
                ChunkLoaderManager.activate(world, pos, oldTier);
            }
            source.sendFailure(Component.literal("Cannot activate loader here: would overlap an existing loader."));
            return 0;
        }

        // 3. Force load the new tier chunks on the tracking map
        ChunkLoaderManager.activate(world, pos, tier);

        source.sendSuccess(() -> Component.literal(String.format("Successfully forced activation of %s tier loader at [%d, %d, %d].",
                        tier.name(), pos.getX(), pos.getY(), pos.getZ()))
                .withStyle(ChatFormatting.GREEN), true);

        return 1;
    }

    private static int executeList(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        Map<BlockPos, LoaderTier> loaders = ChunkLoaderManager.snapshot(world);

        if (loaders.isEmpty()) {
            source.sendSuccess(() -> Component.literal("There are no active Loadstone loaders in this dimension.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("=== Active Loadstone Chunks ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        loaders.forEach((pos, tier) -> {
            ChatFormatting color = switch (tier) {
                case IRON -> ChatFormatting.GRAY;
                case DIAMOND -> ChatFormatting.AQUA;
                case NETHERITE -> ChatFormatting.LIGHT_PURPLE;
            };

            String tpCmd = String.format("/tp %d %d %d", pos.getX(), pos.getY() + 1, pos.getZ());

            MutableComponent posComponent = Component.literal(String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ()))
                    .withStyle(style -> style
                            .withColor(ChatFormatting.GREEN)
                            .withUnderlined(true)
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to teleport here")))
                            .withClickEvent(new ClickEvent.SuggestCommand(tpCmd))
                    );

            MutableComponent text = Component.literal("• Tier: ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(tier.name()).withStyle(color))
                    .append(" at ").withStyle(ChatFormatting.WHITE)
                    .append(posComponent);

            source.sendSuccess(() -> text, false);
        });

        return loaders.size();
    }

    private static int executeDeactivateAll(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        Map<BlockPos, LoaderTier> loaders = ChunkLoaderManager.snapshot(world);

        if (loaders.isEmpty()) {
            source.sendFailure(Component.literal("No active loaders found to deactivate."));
            return 0;
        }

        int count = loaders.size();
        Set<BlockPos> positions = Set.copyOf(loaders.keySet());

        positions.forEach(pos -> ChunkLoaderManager.deactivate(world, pos));

        source.sendSuccess(() -> Component.literal(String.format("Successfully deactivated and cleared all %d loaders.", count))
                .withStyle(ChatFormatting.GREEN), true);

        return count;
    }

    private static int executeDeactivatePos(CommandSourceStack source, BlockPos pos) {
        ServerLevel world = source.getLevel();

        if (!ChunkLoaderManager.isActive(world, pos)) {
            source.sendFailure(Component.literal(String.format("No active Loadstone found at [%d, %d, %d].", pos.getX(), pos.getY(), pos.getZ())));
            return 0;
        }

        ChunkLoaderManager.deactivate(world, pos);

        source.sendSuccess(() -> Component.literal(String.format("Deactivated loader at [%d, %d, %d].", pos.getX(), pos.getY(), pos.getZ()))
                .withStyle(ChatFormatting.GREEN), true);

        return 1;
    }
}