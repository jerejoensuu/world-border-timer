package com.border;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
import java.nio.file.Files;

public class Config {
    private static Config config;

    private float timerAnchorX = 0f;
    private float timerAnchorY = 1f;
    private int timerPixelOffsetX = 5;
    private int timerPixelOffsetY = 15;
    private String timerFormat = "%02d:%02d";
    private String timerPrefix = "Border here in: ";

    public static Config load() {
        if (config != null)
            return config;

        try {
            Gson gson = new GsonBuilder().create();
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve("timer-config.json");
            Path projectRoot = Path.of("..");
            Path sourceConfigPath = projectRoot.resolve("config/timer-config.json").normalize();
            
            // Create config directory if it doesn't exist
            Files.createDirectories(configPath.getParent());
            
            // If config file doesn't exist in run directory, check if it exists in source directory
            if (!Files.exists(configPath)) {
                if (Files.exists(sourceConfigPath)) {
                    // Copy from source to run directory
                    Files.copy(sourceConfigPath, configPath);
                    System.out.println("Copied config from source to: " + configPath);
                } else {
                    // Create with default values
                    Config defaultConfig = new Config();
                    Files.writeString(configPath, gson.toJson(defaultConfig));
                    System.out.println("Created default config file at: " + configPath);
                    return defaultConfig;
                }
            }

            // Read the config file
            config = gson.fromJson(Files.readString(configPath), Config.class);
            System.out.println("Loaded config from: " + configPath);
            return config;
        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
            e.printStackTrace();
            // If loading fails, return default values
            return new Config();
        }
    }

    public float getTimerAnchorX() {
        return timerAnchorX;
    }

    public float getTimerAnchorY() {
        return timerAnchorY;
    }

    public int getTimerPixelOffsetX() {
        return timerPixelOffsetX;
    }

    public int getTimerPixelOffsetY() {
        return timerPixelOffsetY;
    }

    public String getTimerFormat() {
        return timerFormat;
    }

    public String getTimerPrefix() {
        return timerPrefix;
    }
}
