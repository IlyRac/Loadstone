package com.ilyrac.loadstone.hud;

import com.ilyrac.loadstone.ClientLoaderCache;
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

    public static void Initializer() {
        // Define the HUD logic as a HudElement
        HudElement loaderHud = (context, _) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

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
            int statusColor = isActive ? 0xFF00FF00 : 0xFFFFA500;

            String tierName = maybeTier.map(t -> switch (t) {
                case IRON -> "Iron";
                case DIAMOND -> "Diamond";
                case NETHERITE -> "Netherite";
            }).orElse("—");

            String radiusText = maybeTier.map(t -> {
                int side = 2 * t.getRadius() + 1;
                return side + "x" + side;
            }).orElse("—");

            int tierColor = maybeTier.map(t -> switch (t) {
                case IRON -> 0xFFFFFFFF;
                case DIAMOND -> 0xFF00FFFF;
                case NETHERITE -> 0xFFAA55FF;
            }).orElse(0xFFBBBBBB);

            int borderColor = isActive ? tierColor : 0xFF888888;

            String[] labels = {"Status:", "Tier:", "Radius:"};
            String[] values = {statusText, tierName, radiusText};
            int[] valueColors = {statusColor, tierColor, tierColor};

            int paddingH = 6;
            int paddingV = 4;

            int maxLabelWidth = 0;
            for (String label : labels) {
                maxLabelWidth = Math.max(maxLabelWidth, font.width(label));
            }

            int maxValueWidth = 72;
            for (int i = 0; i < values.length; i++) {
                if (font.width(values[i]) > maxValueWidth) {
                    // font.plainSubstrByWidth is often font.split(..., width) or font.substrByWidth in newer mappings
                    values[i] = font.plainSubstrByWidth(values[i], maxValueWidth - font.width("…")) + "…";
                }
            }

            int boxWidth = paddingH * 3 + maxLabelWidth + maxValueWidth;
            int rowHeight = 9; // font.lineHeight is usually 9
            int boxHeight = paddingV * 2 + rowHeight * labels.length;

            int screenW = context.guiWidth();
            int screenH = context.guiHeight();
            int xRight = screenW - 5;
            int xLeft = xRight - boxWidth;
            int yTop = (screenH - boxHeight) / 2;

            // Background
            context.fill(xLeft, yTop, xRight, yTop + boxHeight, 0xC0000000);

            // Draw labels and values (drawString -> text)
            for (int i = 0; i < labels.length; i++) {
                int y = yTop + paddingV + i * rowHeight;
                // Labels
                context.text(font, labels[i], xLeft + paddingH, y, 0xFFAAAAAA, false);
                // Values
                context.text(font, values[i], xRight - paddingH - font.width(values[i]), y, valueColors[i], false);
            }

            // Outline
            context.fill(xLeft, yTop, xRight, yTop + 1, borderColor); // top
            context.fill(xLeft, yTop + boxHeight - 1, xRight, yTop + boxHeight, borderColor); // bottom
            context.fill(xLeft, yTop, xLeft + 1, yTop + boxHeight, borderColor); // left
            context.fill(xRight - 1, yTop, xRight, yTop + boxHeight, borderColor); // right
        };

        // Register the element via the registry (Identifier.of replaces new Identifier)
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("loadstone", "loader_overlay"),
                loaderHud
        );
    }
}