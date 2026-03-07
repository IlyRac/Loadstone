package com.ilyrac.loadstone;

import com.ilyrac.loadstone.hud.LoaderHudOverlay;
import com.ilyrac.loadstone.network.ClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class LoadstoneClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientNetworking.Initializer();
		LoaderHudOverlay.Initializer();
	}
}