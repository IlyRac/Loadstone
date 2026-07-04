package com.ilyrac.loadstone.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LoadstoneConfig {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "loadstone.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ============================
    // VARIABLES
    // ============================
    // HUD Settings
    public HudOpacity hudOpacity = HudOpacity.PERCENT_100;
    public HudSize hudSize = HudSize.PERCENT_100;
    public HudLocation hudLocation = HudLocation.CENTER_TOP;

    // Loader Visuals
    public boolean tintedLoaders = true;
    public boolean interactionParticles = true;
    public AmbientMode ambientParticles = AmbientMode.HIGH;

    // ============================
    // ENUMS
    // ============================
    public enum HudOpacity {
        HIDE("Hide"), PERCENT_50("50%"), PERCENT_75("75%"), PERCENT_100("100%");
        public final String name;
        HudOpacity(String name) { this.name = name; }
    }

    public enum HudSize {
        PERCENT_50("50%"), PERCENT_75("75%"), PERCENT_100("100%");
        public final String name;
        HudSize(String name) { this.name = name; }
    }

    public enum HudLocation {
        TOP_LEFT("Top Left"), TOP_RIGHT("Top Right"), BOTTOM_LEFT("Bottom Left"),
        BOTTOM_RIGHT("Bottom Right"), CENTER_RIGHT("Center Right"),
        CENTER_TOP("Center Top"), CENTER_LEFT("Center Left");
        public final String name;
        HudLocation(String name) { this.name = name; }
    }

    // Renamed Enum
    public enum AmbientMode {
        OFF("Off"), LOW("Low"), HIGH("High");
        public final String name;
        AmbientMode(String name) { this.name = name; }
    }

    // ============================
    // SAVE & LOAD LOGIC
    // ============================
    private static LoadstoneConfig instance;

    public static LoadstoneConfig getInstance() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, LoadstoneConfig.class);
            } catch (IOException e) {
                System.err.println("[Loadstone] Failed to load config; reverting to defaults.");
                instance = new LoadstoneConfig();
            }
        } else {
            instance = new LoadstoneConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) instance = new LoadstoneConfig();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            System.err.println("[Loadstone] Failed to save config data.");
        }
    }
}