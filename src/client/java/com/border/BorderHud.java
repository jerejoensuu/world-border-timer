package com.border;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class BorderHud {
    public static void register() {
        System.out.println("[BorderTimer] Registering HUD callback");
        HudRenderCallback.EVENT.register(BorderHud::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null)
            return;

        TimerMod.ImpactInfo impact = TimerMod.computeImpact();
        if (impact == null || impact.msUntilEvent < 0)
            return;

        Config config = Config.load();

        long totalSec = impact.msUntilEvent / 1000;
        long mins = totalSec / 60;
        long secs = totalSec % 60;

        String time = String.format(config.getTimerFormat(), mins, secs);
        Text txt = Text.literal(config.getTimerPrefix() + time);

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();

        // Bottom left of the screen
        int x = config.getTimerPixelOffsetX();
        int y = windowHeight - client.textRenderer.fontHeight;

        x += config.getTimerPixelOffsetX();
        y -= config.getTimerPixelOffsetY();

        // Color: green if safe inside final border, red if you will be hit
        int color = impact.safeInsideFinalBorder
                ? 0xFF00FF00 // opaque green
                : 0xFFFF5555; // opaque red-ish

        context.drawTextWithShadow(client.textRenderer, txt, x, y, color);
    }
}
