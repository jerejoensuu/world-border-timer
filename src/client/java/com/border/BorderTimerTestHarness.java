package com.border;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Test harness used only during compatibility sweeps.
 * It is completely inert unless the JVM property "wbt.compatTest" is set to
 * true.
 */
public final class BorderTimerTestHarness {

    // Guard flag: only active in compat test runs
    private static final boolean ENABLED = Boolean.getBoolean("wbt.compatTest");

    private static boolean started = false;
    private static int ticks = 0;

    private BorderTimerTestHarness() {
    }

    public static void init() {
        if (!ENABLED) {
            return;
        }

        TimerMod.LOGGER.info("[WorldBorderTimer] Compat test harness enabled");
        ClientTickEvents.END_CLIENT_TICK.register(BorderTimerTestHarness::onTick);
    }

    @SuppressWarnings("null") // client is allowed to be null in this context
    private static void onTick(MinecraftClient client) {
        if (!ENABLED) {
            return;
        }

        if (client == null || client.world == null || client.player == null) {
            return;
        }

        ticks++;

        try {
            // Wait a bit so the world is fully loaded
            if (!started && ticks == 20) {
                client.player.networkHandler.sendChatCommand("worldborder set 50");
                started = true;
                TimerMod.LOGGER.info("[WorldBorderTimer] Compat test: setting border");
            }

            if (started && ticks == 40) {
                client.player.networkHandler.sendChatCommand("worldborder set 20 500");
                TimerMod.LOGGER.info("[WorldBorderTimer] Compat test: shrinking border");
            }

            // After some time, stop the client so the process exits
            if (ticks > 80) {
                TimerMod.LOGGER.info("[WorldBorderTimer] Compat test: stopping client");
                client.scheduleStop(); // or client.stop() on older versions if needed
            }
        } catch (Throwable t) {
            // Let it crash hard so the sweeper sees a failure
            TimerMod.LOGGER.error("[WorldBorderTimer] Compat test threw exception", t);
            throw t;
        }
    }
}
