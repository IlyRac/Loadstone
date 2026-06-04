package com.ilyrac.loadstone.mixin.client;

import com.ilyrac.loadstone.ClientLoaderCache;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.lang.reflect.Method;

@Mixin(targets = "dev.ftb.mods.ftbchunks.client.gui.map.ChunkScreenPanel$ChunkButton")
public abstract class ChunkButtonMixin {

    @Inject(method = "drawBackground", at = @At("TAIL"), remap = false)
    private void loadstone_drawBackground(GuiGraphicsExtractor graphics, @Coerce Object theme, int x, int y, int w, int h, CallbackInfo ci) {
        try {
            Method getChunkPos = this.getClass().getMethod("getChunkPos");
            Object chunkPos = getChunkPos.invoke(this);
            Method xMethod = chunkPos.getClass().getMethod("x");
            Method zMethod = chunkPos.getClass().getMethod("z");
            int cx = (Integer) xMethod.invoke(chunkPos);
            int cz = (Integer) zMethod.invoke(chunkPos);

            if (ClientLoaderCache.isChunkLoaded(cx, cz)) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse("ftbchunks:textures/force_loaded.png"), x, y, 0.0f, 0.0f, w, h, 16, 16, ARGB.color(255, 255, 34, 34));
            }
        } catch (Exception e) {}
    }
}
