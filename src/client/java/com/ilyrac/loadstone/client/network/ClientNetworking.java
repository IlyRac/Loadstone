package com.ilyrac.loadstone.client.network;

import com.ilyrac.loadstone.client.ClientLoaderCache;
import com.ilyrac.loadstone.client.config.LoadstoneConfig;
import com.ilyrac.loadstone.loader.LoaderTier;
import com.ilyrac.loadstone.network.LoaderSnapshotS2CPacket;
import com.ilyrac.loadstone.network.LoaderUpdateS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;

public final class ClientNetworking {
    private ClientNetworking() {}

    public static void Initializer() {
        ClientPlayNetworking.registerGlobalReceiver(LoaderUpdateS2CPacket.TYPE, (payload, _) -> {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                BlockPos pos = payload.pos();
                LoaderTier tier = payload.hasLoader() ? LoaderTier.values()[payload.ordinal()] : null;

                // 1. UPDATE CACHE
                if (payload.hasLoader()) {
                    ClientLoaderCache.put(pos, tier);
                } else {
                    ClientLoaderCache.remove(pos);
                }

                // 2. REFRESH TINT IMMEDIATELY
                ClientLoaderCache.refreshBlockVisuals(pos);

                // 3. SPAWN INTERACTION PARTICLES (If enabled in config and loader was added)
                if (payload.hasLoader() && LoadstoneConfig.getInstance().interactionParticles && client.level != null) {
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 1.0;
                    double z = pos.getZ() + 0.5;

                    switch (tier) {
                        case IRON -> {
                            for (int i = 0; i < 15; i++) {
                                client.level.addParticle(ParticleTypes.LARGE_SMOKE,
                                        x, y, z, (Math.random() - 0.5) * 0.35, Math.random() * 0.35, (Math.random() - 0.5) * 0.35);
                            }
                        }
                        case DIAMOND -> {
                            for (int i = 0; i < 15; i++) {
                                client.level.addParticle(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS,
                                        x, y, z, (Math.random() - 0.5) * 0.35, Math.random() * 0.35, (Math.random() - 0.5) * 0.35);
                            }
                        }
                        case NETHERITE -> {
                            PowerParticleOption dragonBreath = PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F);
                            for (int i = 0; i < 15; i++) {
                                client.level.addParticle(dragonBreath,
                                        x, y, z, (Math.random() - 0.5) * 0.35, Math.random() * 0.35, (Math.random() - 0.5) * 0.35);
                            }
                        }
                    }
                }
            });
        });

        // Snapshot Receiver (For joining the game / crossing dimensions)
        ClientPlayNetworking.registerGlobalReceiver(LoaderSnapshotS2CPacket.TYPE, (payload, _) -> Minecraft.getInstance().execute(() -> {
            ClientLoaderCache.clear();
            for (LoaderUpdateS2CPacket e : payload.entries()) {
                if (e.hasLoader()) {
                    BlockPos pos = e.pos();
                    ClientLoaderCache.put(pos, LoaderTier.values()[e.ordinal()]);
                    // Refresh chunks when loading the initial snapshot
                    ClientLoaderCache.refreshBlockVisuals(pos);
                }
            }
        }));
    }
}