package com.ilyrac.loadstone.hud;

import com.ilyrac.loadstone.ClientLoaderCache;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Blocks;

public final class LoaderHudOverlay {

    private LoaderHudOverlay() {}

    public static void Initializer() {
        //noinspection deprecation
        HudRenderCallback.EVENT.register((gui, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

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
            int statusColor = isActive ? 0xFF00FF00 : 0xFFFFA500; // keep these as requested

            // Tier name and radius text
            String tierName = maybeTier.map(t -> switch (t) {
                case IRON -> "Iron";
                case DIAMOND -> "Diamond";
                case NETHERITE -> "Netherite";
            }).orElse("—");

            String radiusText = maybeTier.map(t -> {
                int side = 2 * t.getRadius() + 1;
                return side + "x" + side;
            }).orElse("—");

            // Tier-based color (for tier text, radius text and border when active)
            int tierColor = maybeTier.map(t -> switch (t) {
                case IRON -> 0xFFFFFFFF;        // white
                case DIAMOND -> 0xFF00FFFF;     // cyan
                case NETHERITE -> 0xFFAA55FF;   // purple (adjust if you prefer a different purple)
            }).orElse(0xFFBBBBBB); // fallback grey when unknown/inactive

            // Border color: use tier color when active, otherwise a neutral grey
            int borderColor = isActive ? tierColor : 0xFF888888;

            // Labels and values
            String[] labels = {"Status:", "Tier:", "Radius:"};
            String[] values = {statusText, tierName, radiusText};

            // Value colors: keep statusColor, use tierColor for tier/radius
            int[] valueColors = {statusColor, tierColor, tierColor};

            // Padding and layout
            int paddingH = 6;
            int paddingV = 4;

            // Compute max label width
            int maxLabelWidth = 0;
            for (String label : labels) {
                maxLabelWidth = Math.max(maxLabelWidth, font.width(label));
            }

            // Fixed max value width to avoid long radius expanding the box
            int maxValueWidth = 72; // increase if you want more space for radius
            // truncate value text if too long
            for (int i = 0; i < values.length; i++) {
                if (font.width(values[i]) > maxValueWidth) {
                    values[i] = font.plainSubstrByWidth(values[i], maxValueWidth - font.width("…")) + "…";
                }
            }

            int boxWidth = paddingH * 3 + maxLabelWidth + maxValueWidth;
            int rowHeight = font.lineHeight;
            int boxHeight = paddingV * 2 + rowHeight * labels.length;

            // Position on right-center
            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();
            int xRight = screenW - 5;
            int xLeft = xRight - boxWidth;
            int yTop = (screenH - boxHeight) / 2;

            // Background (semi-transparent)
            gui.fill(xLeft, yTop, xRight, yTop + boxHeight, 0xC0000000);

            // Draw labels and values
            for (int i = 0; i < labels.length; i++) {
                int y = yTop + paddingV + i * rowHeight;
                gui.drawString(font, labels[i], xLeft + paddingH, y, 0xFFAAAAAA, false);
                gui.drawString(font, values[i], xRight - paddingH - font.width(values[i]), y, valueColors[i], false);
            }

            // Draw outline using filled rectangles (top, bottom, left, right)
            gui.fill(xLeft, yTop, xRight, yTop + 1, borderColor); // top
            gui.fill(xLeft, yTop + boxHeight - 1, xRight, yTop + boxHeight, borderColor); // bottom
            gui.fill(xLeft, yTop, xLeft + 1, yTop + boxHeight, borderColor); // left
            gui.fill(xRight - 1, yTop, xRight, yTop + boxHeight, borderColor); // right
        });
    }
}