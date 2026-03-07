package com.ilyrac.loadstone;

import com.ilyrac.loadstone.event.PlayerInteractHandler;
import com.ilyrac.loadstone.loader.ChunkLoaderManager;
import com.ilyrac.loadstone.network.NetworkPackets;
import com.ilyrac.loadstone.network.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loadstone implements ModInitializer {
	public static final String MOD_ID = "loadstone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		NetworkPackets.Initializer();
		PlayerInteractHandler.Initializer();
		ServerNetworking.Initializer();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 5 == 0) { // 100 ticks = 5 seconds
				for (ServerLevel level : server.getAllLevels()) {
					ChunkLoaderManager.validateAllLoaders(level);
				}
			}
		});

		LOGGER.info("Loadstone Loaded Successfully!");
	}
}