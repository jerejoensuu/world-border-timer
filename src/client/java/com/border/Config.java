package com.border;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Config {
    private static final String CONFIG_FILE_NAME = "world_border_timer.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Config config;

    // Defaults
    private float timerAnchorX = 0f;
    private float timerAnchorY = 1f;
    private int timerPixelOffsetX = 5;
    private int timerPixelOffsetY = 15;
    private String timerFormat = "%02d:%02d";
    private String timerPrefix = "Border here in: ";

    /**
     * Loads the config from .minecraft/config/world_border_timer.json.
     * If the file does not exist, a default one is created.
     * If the file is malformed, it is backed up and a fresh default is written.
     */
    public static Config load() {
        if (config != null) {
            return config;
        }

        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);

        try {
            Files.createDirectories(configDir);

            if (!Files.exists(configPath)) {
                // No file yet: create with defaults
                config = new Config();
                writeConfig(configPath, config);
                System.out.println("[WorldBorderTimer] Created default config at: " + configPath);
                return config;
            }

            // Existing file: try to read and parse
            String json = Files.readString(configPath);
            Config loaded = GSON.fromJson(json, Config.class);

            if (loaded == null) {
                // Parsed to null somehow; treat as malformed
                throw new IOException("Parsed config is null");
            }

            config = loaded;
            System.out.println("[WorldBorderTimer] Loaded config from: " + configPath);
            return config;
        } catch (Exception e) {
            System.err.println("[WorldBorderTimer] Error loading config: " + e.getMessage());
            e.printStackTrace();

            // Backup old file if possible
            backupBrokenConfig(configDir, configPath);

            // Fallback to defaults
            config = new Config();
            try {
                writeConfig(configPath, config);
                System.out.println("[WorldBorderTimer] Wrote fresh default config after error.");
            } catch (IOException ioException) {
                System.err.println("[WorldBorderTimer] Failed to write default config: " + ioException.getMessage());
                ioException.printStackTrace();
            }
            return config;
        }
    }

    private static void writeConfig(Path configPath, Config cfg) throws IOException {
        String json = GSON.toJson(cfg);
        Files.writeString(configPath, json);
    }

    private static void backupBrokenConfig(Path configDir, Path configPath) {
        try {
            if (Files.exists(configPath)) {
                String backupFileName = CONFIG_FILE_NAME + ".broken-" + System.currentTimeMillis();
                Path backupPath = configDir.resolve(backupFileName);
                Files.move(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.err.println("[WorldBorderTimer] Backed up broken config to: " + backupPath);
            }
        } catch (Exception backupEx) {
            System.err.println("[WorldBorderTimer] Failed to backup broken config: " + backupEx.getMessage());
            backupEx.printStackTrace();
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
