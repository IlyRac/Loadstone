package com.ilyrac.loadstone.client;

import com.ilyrac.loadstone.client.config.LoadstoneConfig;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class LoadstoneBlockTint {

    private LoadstoneBlockTint() {}

    public static void Initializer() {
        BlockTintSource lodestoneTintSource = new BlockTintSource() {
            @Override
            public int color(@NonNull BlockState state) {
                return -1;
            }

            @Override
            public int colorInWorld(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
                // Check if the user has enabled tinted loaders in their settings
                if (!LoadstoneConfig.getInstance().tintedLoaders) {
                    return -1; // Standard vanilla appearance
                }

                // If enabled, tint according to active cache tier
                return ClientLoaderCache.get(pos).map(tier -> switch (tier) {
                    case IRON -> 0xFF808080;      // bright silver
                    case DIAMOND -> 0xFF00FFFF;   // Glowing Aqua
                    case NETHERITE -> 0xFFAA55FF; // Deep Purple
                }).orElse(-1);
            }
        };

        BlockColorRegistry.register(
                List.of(lodestoneTintSource),
                Blocks.LODESTONE
        );
    }
}