package com.border;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class BorderHud {

    @SuppressWarnings("deprecation") // HudRenderCallback is deprecated in newer versions but needed for 1.21.0-1.21.5
    public static void register() {
        HudRenderCallback.EVENT.register(BorderHud::onHudRender);
    }

    private static void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
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
                ? 0xFF00FF00
                : 0xFFFF5555;

        context.drawTextWithShadow(client.textRenderer, txt.getString(), x, y, color);
    }
}
