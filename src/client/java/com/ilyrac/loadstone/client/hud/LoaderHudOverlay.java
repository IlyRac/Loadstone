package com.ilyrac.loadstone.client.hud;

import com.ilyrac.loadstone.client.ClientLoaderCache;
import com.ilyrac.loadstone.client.config.LoadstoneConfig; // Your config class
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public final class LoaderHudOverlay {

    private LoaderHudOverlay() {}

    // --- HELPER METHODS FOR CONFIG ENUMS ---

    private static float getOpacityValue(LoadstoneConfig.HudOpacity opacityEnum) {
        return switch (opacityEnum) {
            case HIDE -> 0.0f;
            case PERCENT_50 -> 0.5f;
            case PERCENT_75 -> 0.75f;
            case PERCENT_100 -> 1.0f;
        };
    }

    private static float getSizeValue(LoadstoneConfig.HudSize sizeEnum) {
        return switch (sizeEnum) {
            case PERCENT_50 -> 0.5f;
            case PERCENT_75 -> 0.75f;
            case PERCENT_100 -> 1.0f;
        };
    }

    // Safely recalculates the alpha channel (0-255) of an ARGB integer
    private static int applyOpacity(int baseColor, float opacityFactor) {
        int originalAlpha = (baseColor >> 24) & 0xFF;
        int newAlpha = Math.round(originalAlpha * opacityFactor);
        return (newAlpha << 24) | (baseColor & 0x00FFFFFF);
    }

    // --- MAIN HUD LOGIC ---

    public static void Initializer() {
        HudElement loaderHud = (context, _) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Fetch live config settings
            LoadstoneConfig config = LoadstoneConfig.getInstance();
            float opacity = getOpacityValue(config.hudOpacity);

            // If the user chose "Hide", exit immediately to save frame rendering time
            if (opacity <= 0.0f) return;

            var hit = mc.hitResult;
            if (!(hit instanceof BlockHitResult blockHit)) return;

            BlockPos pos = blockHit.getBlockPos();
            if (mc.level == null) return;

            var state = mc.level.getBlockState(pos);
            if (!state.is(Blocks.LODESTONE)) return;

            Font font = mc.font;

            // Fetch tier info
            var maybeTier = ClientLoaderCache.get(pos);
            boolean isActive = maybeTier.isPresent();

            String statusText = isActive ? "Active" : "Inactive";
            int baseStatusColor = isActive ? 0xFF00FF00 : 0xFFFFA500;

            String tierName = maybeTier.map(t -> switch (t) {
                case IRON -> "Iron";
                case DIAMOND -> "Diamond";
                case NETHERITE -> "Netherite";
            }).orElse("—");

            String radiusText = maybeTier.map(t -> {
                int side = 2 * t.getRadius() + 1;
                return side + "x" + side;
            }).orElse("—");

            int baseTierColor = maybeTier.map(t -> switch (t) {
                case IRON -> 0xFFFFFFFF;
                case DIAMOND -> 0xFF00FFFF;
                case NETHERITE -> 0xFFAA55FF;
            }).orElse(0xFFBBBBBB);

            int baseBorderColor = isActive ? baseTierColor : 0xFF888888;

            String[] labels = {"Status:", "Tier:", "Radius:"};
            String[] values = {statusText, tierName, radiusText};
            int[] baseValueColors = {baseStatusColor, baseTierColor, baseTierColor};

            int paddingH = 6;
            int paddingV = 4;

            int maxLabelWidth = 0;
            for (String label : labels) {
                maxLabelWidth = Math.max(maxLabelWidth, font.width(label));
            }

            int maxValueWidth = 72;
            for (int i = 0; i < values.length; i++) {
                if (font.width(values[i]) > maxValueWidth) {
                    values[i] = font.plainSubstrByWidth(values[i], maxValueWidth - font.width("…")) + "…";
                }
            }

            int boxWidth = paddingH * 3 + maxLabelWidth + maxValueWidth;
            int rowHeight = 9;
            int boxHeight = paddingV * 2 + rowHeight * labels.length;

            // === 1. MATRIX SCALING SETUP ===
            float scale = getSizeValue(config.hudSize);

            // Push a new rendering state to memory
            context.pose().pushMatrix();
            // Scale the interface (using 2D scale modifier)
            context.pose().scale(scale, scale);

            // === 2. SCALED SCREEN COORDINATES ===
            // Since we scaled the matrix, the virtual screen size is altered
            int scaledScreenW = (int) (context.guiWidth() / scale);
            int scaledScreenH = (int) (context.guiHeight() / scale);
            int margin = 5;

            // === 3. HUD LOCATION MATH ===
            int xLeft = 0;
            int yTop = 0;

            switch (config.hudLocation) {
                case TOP_LEFT -> {
                    xLeft = margin;
                    yTop = margin;
                }
                case TOP_RIGHT -> {
                    xLeft = scaledScreenW - boxWidth - margin;
                    yTop = margin;
                }
                case BOTTOM_LEFT -> {
                    xLeft = margin;
                    yTop = scaledScreenH - boxHeight - margin;
                }
                case BOTTOM_RIGHT -> {
                    xLeft = scaledScreenW - boxWidth - margin;
                    yTop = scaledScreenH - boxHeight - margin;
                }
                case CENTER_LEFT -> {
                    xLeft = margin;
                    yTop = (scaledScreenH - boxHeight) / 2;
                }
                case CENTER_RIGHT -> {
                    xLeft = scaledScreenW - boxWidth - margin;
                    yTop = (scaledScreenH - boxHeight) / 2;
                }
                case CENTER_TOP -> {
                    xLeft = (scaledScreenW - boxWidth) / 2;
                    yTop = margin;
                }
            }

            int xRight = xLeft + boxWidth;

            // === 4. OPACITY COLOR BLENDING ===
            int finalBgColor = applyOpacity(0xC0000000, opacity);
            int finalLabelColor = applyOpacity(0xFFAAAAAA, opacity);
            int finalBorderColor = applyOpacity(baseBorderColor, opacity);

            // Draw Background
            context.fill(xLeft, yTop, xRight, yTop + boxHeight, finalBgColor);

            // Draw labels and values
            for (int i = 0; i < labels.length; i++) {
                int y = yTop + paddingV + i * rowHeight;
                int finalValColor = applyOpacity(baseValueColors[i], opacity);

                context.text(font, labels[i], xLeft + paddingH, y, finalLabelColor, false);
                context.text(font, values[i], xRight - paddingH - font.width(values[i]), y, finalValColor, false);
            }

            // Draw Outline
            context.fill(xLeft, yTop, xRight, yTop + 1, finalBorderColor); // top
            context.fill(xLeft, yTop + boxHeight - 1, xRight, yTop + boxHeight, finalBorderColor); // bottom
            context.fill(xLeft, yTop, xLeft + 1, yTop + boxHeight, finalBorderColor); // left
            context.fill(xRight - 1, yTop, xRight, yTop + boxHeight, finalBorderColor); // right

            // Pop matrix to avoid scaling the rest of the game's UI
            context.pose().popMatrix();
        };

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("loadstone", "loader_overlay"),
                loaderHud
        );
    }
}