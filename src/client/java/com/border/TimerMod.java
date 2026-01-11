package com.border;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.border.WorldBorder;

public class TimerMod implements ClientModInitializer {
    private static WorldBorder border;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                border = client.world.getWorldBorder();
            }
        });
        BorderHud.register();
    }

    /**
     * @return latest WorldBorder or null if none
     */
    public static WorldBorder getBorder() {
        return border;
    }

    /**
     * Compute milliseconds until the shrinking border intersects the player's
     * position.
     * 
     * @return ms until impact, or -1 if not shrinking or no player
     */
    public static long computeTimeToImpact() {
        if (border == null)
            return -1;

        double currentSize = border.getSize();
        double targetSize = border.getSizeLerpTarget();
        long shrinkTime = border.getSizeLerpTime(); // ms
        if (targetSize >= currentSize || shrinkTime <= 0)
            return -1;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return -1;

        double cx = border.getCenterX();
        double cz = border.getCenterZ();
        double px = client.player.getX();
        double pz = client.player.getZ();

        // distance from center
        double dx = Math.abs(px - cx);
        double dz = Math.abs(pz - cz);

        // half-extents
        double halfStart = currentSize / 2.0;
        double totalDelta = currentSize - targetSize;

        // time for edge on X axis to reach px:
        // tX = shrinkTime * (halfStart - dx) / (totalDelta/2)
        double tX = shrinkTime * (halfStart - dx) / (totalDelta * 0.5);
        double tZ = shrinkTime * (halfStart - dz) / (totalDelta * 0.5);

        // whichever edge you collide with first:
        long msUntilImpact = (long) Math.min(tX, tZ);

        return msUntilImpact;
    }
}