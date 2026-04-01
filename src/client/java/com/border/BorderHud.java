package com.border;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class BorderHud {

    private static final Identifier BORDER_TIMER_HUD_ID = Objects.requireNonNull(
            Identifier.fromNamespaceAndPath(TimerMod.MOD_ID, "border_timer_hud"),
            "Failed to create Identifier for border timer HUD");

    @SuppressWarnings("null")
    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                BORDER_TIMER_HUD_ID,
                BorderHud::render);
    }

    private static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null) {
            return;
        }

        TimerMod.ImpactInfo impact = TimerMod.computeImpact();
        if (impact == null || impact.msUntilEvent < 0) {
            return;
        }

        Config config = Config.load();

        long totalSec = impact.msUntilEvent / 1000;
        long mins = totalSec / 60;
        long secs = totalSec % 60;

        String time = String.format(config.getTimerFormat(), mins, secs);
        Component txt = Component.literal(
                config.getTimerPrefix(!impact.safeInsideFinalBorder) + time);

        int windowWidth = client.getWindow().getGuiScaledWidth();
        int windowHeight = client.getWindow().getGuiScaledHeight();

        int x = (int) (config.getTimerAnchorX() * (float) windowWidth);
        int y = (int) (config.getTimerAnchorY() * (float) windowHeight);

        x += config.getTimerPixelOffsetX();
        y -= config.getTimerPixelOffsetY();

        int color = impact.safeInsideFinalBorder ? 0xFF00FF00 : 0xFFFF5555;

        context.text(client.font, txt, x, y, color);
    }
}