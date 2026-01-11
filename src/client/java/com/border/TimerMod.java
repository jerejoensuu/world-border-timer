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

    public static final class ImpactInfo {
        public final long msUntilEvent;
        public final boolean safeInsideFinalBorder;

        public ImpactInfo(long msUntilEvent, boolean safeInsideFinalBorder) {
            this.msUntilEvent = msUntilEvent;
            this.safeInsideFinalBorder = safeInsideFinalBorder;
        }
    }

    /**
     * New logic:
     * - If the player will remain inside the final border:
     * safeInsideFinalBorder = true
     * msUntilEvent = time until the border reaches its target size
     * - If the border will hit the player before that:
     * safeInsideFinalBorder = false
     * msUntilEvent = time until the edge reaches the player
     *
     * Returns null if there is no meaningful timer (no border, not shrinking, etc).
     */
    public static ImpactInfo computeImpact() {
        if (border == null)
            return null;

        double currentSize = border.getSize();
        double targetSize = border.getSizeLerpTarget();
        long lerpTicks = border.getSizeLerpTime(); // ticks remaining

        // Not shrinking or nothing left to do
        if (targetSize >= currentSize || lerpTicks <= 0)
            return null;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null)
            return null;

        double cx = border.getCenterX();
        double cz = border.getCenterZ();
        double px = client.player.getX();
        double pz = client.player.getZ();

        double dx = Math.abs(px - cx);
        double dz = Math.abs(pz - cz);

        double halfStart = currentSize / 2.0;
        double halfEnd = targetSize / 2.0;

        if (halfEnd >= halfStart)
            return null;

        double totalSeconds = lerpTicks / 20.0;
        if (totalSeconds <= 0.0)
            return null;

        double shrinkPerSecond = (halfStart - halfEnd) / totalSeconds;
        if (shrinkPerSecond <= 0.0)
            return null;

        // Distance from center along the limiting axis
        double d = Math.max(dx, dz);

        // Already outside the border now or exactly at the edge
        if (d >= halfStart)
            return null;

        boolean safe = d <= halfEnd;

        double secondsUntilEvent;
        if (safe) {
            // Border never reaches the player, so show time until it reaches final size
            secondsUntilEvent = totalSeconds;
        } else {
            // Time when halfSize(t) == d
            secondsUntilEvent = (halfStart - d) / shrinkPerSecond;
        }

        if (secondsUntilEvent <= 0.0
                || Double.isNaN(secondsUntilEvent)
                || Double.isInfinite(secondsUntilEvent)) {
            return null;
        }

        long ms = (long) (secondsUntilEvent * 1000.0);
        return new ImpactInfo(ms, safe);
    }

    // Backwards compatible helper if you still want just a number
    public static long computeTimeToImpact() {
        ImpactInfo info = computeImpact();
        return (info == null) ? -1L : info.msUntilEvent;
    }
}
