package com.border;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.border.WorldBorder;

public class TimerMod implements ClientModInitializer {
    private static WorldBorder border;

    @Override
    public void onInitializeClient() {
        System.out.println("[BorderTimer] onInitializeClient");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                border = client.world.getWorldBorder();
            }
        });

        BorderHud.register();
    }

    public static WorldBorder getBorder() {
        return border;
    }

    /**
     * Computes milliseconds until the shrinking border reaches the player's
     * X/Z position. Returns -1 if:
     * - no border
     * - not currently shrinking
     * - player / world missing
     * - math says "already inside / past border" or nonsense.
     */
    public static long computeTimeToImpact() {
        if (border == null)
            return -1;

        double currentSize = border.getSize();
        double targetSize = border.getSizeLerpTarget();
        long lerpTicks = border.getSizeLerpTime(); // TICKS remaining

        // Not shrinking or no remaining interpolation
        if (targetSize >= currentSize || lerpTicks <= 0)
            return -1;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null)
            return -1;

        double cx = border.getCenterX();
        double cz = border.getCenterZ();
        double px = client.player.getX();
        double pz = client.player.getZ();

        // Distance from center along each axis
        double dx = Math.abs(px - cx);
        double dz = Math.abs(pz - cz);

        // Half-sizes now and at the end
        double halfStart = currentSize / 2.0;
        double halfEnd = targetSize / 2.0;

        // If the target half-size is not smaller, something is off
        if (halfEnd >= halfStart)
            return -1;

        // Total time in seconds for the shrink
        double totalSeconds = lerpTicks / 20.0;
        if (totalSeconds <= 0.0)
            return -1;

        // Linear shrink rate of half-size per second
        double shrinkPerSecond = (halfStart - halfEnd) / totalSeconds;
        if (shrinkPerSecond <= 0.0)
            return -1;

        // Time until the border reaches the player's X and Z distances
        double secX = (halfStart - dx) / shrinkPerSecond;
        double secZ = (halfStart - dz) / shrinkPerSecond;

        double secUntilImpact = Math.min(secX, secZ);

        // If time is negative/NaN/Infinite, treat as "no valid impact"
        if (secUntilImpact <= 0.0
                || Double.isNaN(secUntilImpact)
                || Double.isInfinite(secUntilImpact)) {
            return -1;
        }

        // Convert seconds to milliseconds for the HUD
        long msUntilImpact = (long) (secUntilImpact * 1000.0);

        // Optional debug:
        // System.out.printf("[BorderTimer] cur=%.2f target=%.2f ticks=%d dx=%.2f
        // dz=%.2f sec=%.2f ms=%d%n",
        // currentSize, targetSize, lerpTicks, dx, dz, secUntilImpact, msUntilImpact);

        return msUntilImpact;
    }
}
