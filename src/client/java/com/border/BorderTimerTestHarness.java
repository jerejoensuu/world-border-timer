package com.border;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

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
    private static void onTick(Minecraft client) {
        if (!ENABLED) {
            return;
        }

        if (client == null || client.level == null || client.player == null) {
            return;
        }

        ticks++;

        try {
            if (!started && ticks == 20) {
                client.player.connection.sendCommand("worldborder set 50");
                started = true;
                TimerMod.LOGGER.info("[WorldBorderTimer] Compat test: setting border");
            }

            if (started && ticks == 40) {
                client.player.connection.sendCommand("worldborder set 20 500");
                TimerMod.LOGGER.info("[WorldBorderTimer] Compat test: shrinking border");
            }

            if (ticks > 80) {
                TimerMod.LOGGER.info("[WorldBorderTimer] Compat test: stopping client");
                client.stop();
            }
        } catch (Throwable t) {
            TimerMod.LOGGER.error("[WorldBorderTimer] Compat test threw exception", t);
            throw t;
        }
    }
}
