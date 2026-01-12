package com.border;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.border.WorldBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerMod implements ClientModInitializer {

    public static final String MOD_ID = "world_border_timer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[WorldBorderTimer] Client initialization");
        BorderHud.register();
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
     * Logic:
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }

        ClientWorld world = client.world;
        PlayerEntity player = client.player;

        if (world == null || player == null) {
            return null;
        }

        WorldBorder border = world.getWorldBorder();
        if (border == null) {
            return null;
        }

        double currentSize = border.getSize();
        double targetSize = border.getSizeLerpTarget();
        long lerpTicks = border.getSizeLerpTime(); // ticks remaining

        // Not shrinking or nothing left to do
        if (targetSize >= currentSize || lerpTicks <= 0) {
            return null;
        }

        double cx = border.getCenterX();
        double cz = border.getCenterZ();
        double px = player.getX();
        double pz = player.getZ();

        double dx = Math.abs(px - cx);
        double dz = Math.abs(pz - cz);

        double halfStart = currentSize / 2.0;
        double halfEnd = targetSize / 2.0;

        // Guard against unexpected non-shrinking scenarios
        if (halfEnd >= halfStart) {
            return null;
        }

        double totalSeconds = lerpTicks / 20.0;
        if (totalSeconds <= 0.0) {
            return null;
        }

        double shrinkPerSecond = (halfStart - halfEnd) / totalSeconds;
        if (shrinkPerSecond <= 0.0) {
            return null;
        }

        // Distance from center along the limiting axis
        double d = Math.max(dx, dz);

        // Already outside the border now or exactly at the edge
        if (d >= halfStart) {
            return null;
        }

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

    // Backwards compatible helper if only the number is needed
    public static long computeTimeToImpact() {
        ImpactInfo info = computeImpact();
        return (info == null) ? -1L : info.msUntilEvent;
    }
}
