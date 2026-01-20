package com.border;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static Config config;

    private float timerAnchorX = 0f;
    private float timerAnchorY = 1f;
    private int timerPixelOffsetX = 5;
    private int timerPixelOffsetY = 15;
    private String timerFormat = "%02d:%02d";
    private String timerPrefixDanger = "Border here in: ";
    private String timerPrefixSafe = "Border stopping in: ";

    @SuppressWarnings("null") // Gson#fromJson may return null; this is handled explicitly
    public static Config load() {
        if (config != null) {
            return config;
        }

        try {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path configPath = configDir.resolve("world_border_timer.json");

            Files.createDirectories(configDir);

            if (!Files.exists(configPath)) {
                Config defaultConfig = new Config();
                Files.writeString(configPath, gson.toJson(defaultConfig));
                TimerMod.LOGGER.info("Created default config file at: {}", configPath);
                config = defaultConfig;
                return config;
            }

            String json = Files.readString(configPath);
            Config loaded = gson.fromJson(json, Config.class);

            config = loaded;
            // TimerMod.LOGGER.info("Loaded config from: {}", configPath);
            return config;
        } catch (Exception e) {
            TimerMod.LOGGER.error("Error loading config, falling back to defaults", e);
            config = new Config();
            return config;
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

    public String getTimerPrefix(boolean isDanger) {
        return isDanger ? timerPrefixDanger : timerPrefixSafe;
    }
}
