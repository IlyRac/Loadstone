package com.ilyrac.loadstone.client;

import com.ilyrac.loadstone.client.hud.LoaderHudOverlay;
import com.ilyrac.loadstone.client.network.ClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class LoadstoneClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientNetworking.Initializer();
		LoaderHudOverlay.Initializer();
	}
}