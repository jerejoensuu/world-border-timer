package com.border;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class BorderHud {

    // Identifier is forced non-null via Objects.requireNonNull, which the null
    // checker understands
    private static final Identifier BORDER_TIMER_HUD_ID = Objects.requireNonNull(
            Identifier.of(TimerMod.MOD_ID, "border_timer_hud"),
            "Failed to create Identifier for border timer HUD");

    public static void register() {
        TimerMod.LOGGER.info("[WorldBorderTimer] Registering HUD element");

        if (BORDER_TIMER_HUD_ID == null) {
            throw new IllegalStateException("Failed to create HUD identifier. MOD_ID might be invalid.");
        }

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                BORDER_TIMER_HUD_ID,
                BorderHud::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
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
        Text txt = Text.literal(config.getTimerPrefix(!impact.safeInsideFinalBorder) + time);

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();

        int x = (int) (config.getTimerAnchorX() * (float) windowWidth);
        int y = (int) (config.getTimerAnchorY() * (float) windowHeight);

        x += config.getTimerPixelOffsetX();
        y -= config.getTimerPixelOffsetY();

        int color = impact.safeInsideFinalBorder
                ? 0xFF00FF00 // opaque green
                : 0xFFFF5555; // opaque red-ish

        context.drawTextWithShadow(client.textRenderer, txt, x, y, color);
    }
}
