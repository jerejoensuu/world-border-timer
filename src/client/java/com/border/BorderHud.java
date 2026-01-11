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

        long ms = TimerMod.computeTimeToImpact();
        if (ms < 0)
            return;

        Config config = Config.load();

        long totalSec = ms / 1000;
        long mins = totalSec / 60;
        long secs = totalSec % 60;

        String time = String.format(config.getTimerFormat(), mins, secs);
        Text txt = Text.literal(config.getTimerPrefix() + time);

        int windowWidth = client.getWindow().getScaledWidth();
        int windowHeight = client.getWindow().getScaledHeight();

        // New positioning: anchor is [0..1] from top-left
        // (0,0) = top-left, (0.5,0.5) = center, (1,1) = bottom-right
        int x = (int) (windowWidth * config.getTimerAnchorX()) + config.getTimerPixelOffsetX();
        int y = (int) (windowHeight * config.getTimerAnchorY()) + config.getTimerPixelOffsetY();

        // Opaque white (ARGB). 0xFFFFFF would be fully transparent in 1.21.11+
        int color = 0xFFFFFFFF;

        context.drawTextWithShadow(client.textRenderer, txt, x, y, color);
    }
}
