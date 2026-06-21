package net.krkna.monumentatools.nightmare;

import net.krkna.monumentatools.config.MonumentaToolsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public final class NightmareHudRenderer {
    private static final int PADDING_X = 5;
    private static final int PADDING_Y = 4;
    private static final int LINE_GAP = 2;

    private NightmareHudRenderer() {
    }

    public static void render(DrawContext context, NightmareTimer timer, boolean preview) {
        if (!preview && !timer.shouldRender()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.textRenderer == null) {
            return;
        }

        List<String> lines = preview ? timer.previewHudLines() : timer.hudLines();
        if (lines.isEmpty()) {
            return;
        }

        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        HudBounds bounds = measure(lines, client.textRenderer, config);
        float scale = config.hudScale();

        if (config.showBackground) {
            context.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(),
                    config.backgroundColor);
        }

        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);

        int drawX = Math.round((bounds.x() + PADDING_X) / scale);
        int drawY = Math.round((bounds.y() + PADDING_Y) / scale);
        int lineHeight = client.textRenderer.fontHeight + LINE_GAP;
        int color = preview ? config.normalTextColor : timer.textColor();

        for (int index = 0; index < lines.size(); index++) {
            int lineColor = index == 0 ? color : 0xC8D3D8;
            context.drawTextWithShadow(client.textRenderer, Text.literal(lines.get(index)),
                    drawX, drawY + index * lineHeight, lineColor);
        }

        matrices.pop();
    }

    public static HudBounds measure(NightmareTimer timer, boolean preview) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        List<String> lines = preview ? timer.previewHudLines() : timer.hudLines();
        return measure(lines, textRenderer, MonumentaToolsConfig.INSTANCE.nightmare);
    }

    private static HudBounds measure(List<String> lines, TextRenderer textRenderer,
                                     MonumentaToolsConfig.NightmareConfig config) {
        float scale = config.hudScale();
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(line));
        }

        int rawHeight = lines.size() * textRenderer.fontHeight + Math.max(0, lines.size() - 1) * LINE_GAP;
        int width = Math.round(maxWidth * scale) + PADDING_X * 2;
        int height = Math.round(rawHeight * scale) + PADDING_Y * 2;
        return new HudBounds(Math.max(0, config.hudX), Math.max(0, config.hudY), width, height);
    }
}
