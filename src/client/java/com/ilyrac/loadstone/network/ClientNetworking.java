package com.ilyrac.loadstone.network;

import com.ilyrac.loadstone.ClientLoaderCache;
import com.ilyrac.loadstone.loader.LoaderTier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class ClientNetworking {
    private ClientNetworking() {}

    public static void Initializer() {
        ClientPlayNetworking.registerGlobalReceiver(LoaderUpdateS2CPacket.TYPE, (payload, _) -> {
            // apply on client thread
            Minecraft.getInstance().execute(() -> {
                if (payload.hasLoader()) {
                    ClientLoaderCache.put(payload.pos(), LoaderTier.values()[payload.ordinal()]);
                } else {
                    ClientLoaderCache.remove(payload.pos());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(
            LoaderSnapshotS2CPacket.TYPE,
            (payload, _) -> Minecraft.getInstance().execute(()
            ->{
                ClientLoaderCache.clear();
                for (LoaderUpdateS2CPacket e : payload.entries()) {
                    if (e.hasLoader()) ClientLoaderCache.put(e.pos(), LoaderTier.values()[e.ordinal()]);
                }
            })
        );
    }
}