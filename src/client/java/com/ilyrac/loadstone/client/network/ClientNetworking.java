package com.ilyrac.loadstone.client.network;

import com.ilyrac.loadstone.client.ClientLoaderCache;
import com.ilyrac.loadstone.loader.LoaderTier;
import com.ilyrac.loadstone.network.LoaderSnapshotS2CPacket;
import com.ilyrac.loadstone.network.LoaderUpdateS2CPacket;
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