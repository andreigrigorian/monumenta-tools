package net.krkna.monumentatools.nightmare;

import net.krkna.monumentatools.MonumentaToolsClient;
import net.krkna.monumentatools.config.MonumentaToolsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class NightmareHudEditorScreen extends Screen {
    private final Screen parent;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public NightmareHudEditorScreen() {
        this(null);
    }

    public NightmareHudEditorScreen(Screen parent) {
        super(Text.literal("Nightmare Timer HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                .dimensions(width / 2 - 50, height - 28, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        NightmareHudRenderer.render(context, MonumentaToolsClient.NIGHTMARE_TIMER, true);

        HudBounds bounds = NightmareHudRenderer.measure(MonumentaToolsClient.NIGHTMARE_TIMER, true);
        context.drawBorder(bounds.x() - 2, bounds.y() - 2, bounds.width() + 4, bounds.height() + 4, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag the nightmare timer"), width / 2, 18, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        HudBounds bounds = NightmareHudRenderer.measure(MonumentaToolsClient.NIGHTMARE_TIMER, true);
        if (button == 0 && bounds.contains(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - bounds.x();
            dragOffsetY = mouseY - bounds.y();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            HudBounds bounds = NightmareHudRenderer.measure(MonumentaToolsClient.NIGHTMARE_TIMER, true);
            MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
            config.hudX = clamp((int) Math.round(mouseX - dragOffsetX), 0, Math.max(0, width - bounds.width()));
            config.hudY = clamp((int) Math.round(mouseY - dragOffsetY), 0, Math.max(0, height - bounds.height()));
            MonumentaToolsConfig.save();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            MonumentaToolsConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        MonumentaToolsConfig.save();
        if (client != null && parent != null) {
            client.setScreen(parent);
        } else {
            super.close();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }
}
