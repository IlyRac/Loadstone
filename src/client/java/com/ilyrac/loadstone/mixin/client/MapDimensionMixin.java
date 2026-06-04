package com.ilyrac.loadstone.mixin.client;

import com.ilyrac.loadstone.ClientLoaderCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(targets = "dev.ftb.mods.ftbchunks.client.map.MapDimension")
public class MapDimensionMixin {
    @Inject(method = "getLoadedView", at = @At("HEAD"), cancellable = true, remap = false)
    private void loadstone_getLoadedView(@Coerce Object region, int cx, int cz, CallbackInfoReturnable<Integer> cir) {
        try {
            Field posField = region.getClass().getField("pos");
            Object pos = posField.get(region);
            Method xMethod = pos.getClass().getMethod("x");
            Method zMethod = pos.getClass().getMethod("z");
            int absoluteCx = (((Integer) xMethod.invoke(pos)) << 5) + cx;
            int absoluteCz = (((Integer) zMethod.invoke(pos)) << 5) + cz;
            if (ClientLoaderCache.isChunkLoaded(absoluteCx, absoluteCz)) {
                cir.setReturnValue(2); // LoadedChunkViewPacket.FORCE_LOADED
            }
        } catch (Exception e) {}
    }
}
