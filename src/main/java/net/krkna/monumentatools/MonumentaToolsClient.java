package net.krkna.monumentatools;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.krkna.monumentatools.config.MonumentaToolsConfig;
import net.krkna.monumentatools.nightmare.NightmareHudEditorScreen;
import net.krkna.monumentatools.nightmare.NightmareHudRenderer;
import net.krkna.monumentatools.nightmare.NightmareTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MonumentaToolsClient implements ClientModInitializer {
    public static final String MOD_ID = "monumenta_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final NightmareTimer NIGHTMARE_TIMER = new NightmareTimer();

    private static KeyBinding openNightmareEditor;

    @Override
    public void onInitializeClient() {
        MonumentaToolsConfig.load();

        openNightmareEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.monumenta_tools.open_nightmare_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.monumenta_tools"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            NIGHTMARE_TIMER.tick(client);
            while (openNightmareEditor.wasPressed()) {
                client.setScreen(new NightmareHudEditorScreen());
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                NightmareHudRenderer.render(drawContext, NIGHTMARE_TIMER, false));

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                NIGHTMARE_TIMER.onIncomingMessage(message.getString()));
        ClientReceiveMessageEvents.GAME.register((message, overlay) ->
                NIGHTMARE_TIMER.onIncomingMessage(message.getString()));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("monumentatools")
                        .then(literal("nightmare")
                                .then(literal("edit").executes(context -> {
                                    MinecraftClient.getInstance().setScreen(new NightmareHudEditorScreen());
                                    return 1;
                                }))
                                .then(literal("reset").executes(context -> {
                                    NIGHTMARE_TIMER.reset("manual reset");
                                    context.getSource().sendFeedback(Text.literal("Nightmare timer reset."));
                                    return 1;
                                }))
                                .then(literal("status").executes(context -> {
                                    context.getSource().sendFeedback(Text.literal(
                                            NIGHTMARE_TIMER.statusLine(MinecraftClient.getInstance())));
                                    return 1;
                                }))
                                .then(literal("set")
                                        .then(argument("seconds", IntegerArgumentType.integer(0, 3600))
                                                .executes(context -> {
                                                    int seconds = IntegerArgumentType.getInteger(context, "seconds");
                                                    NIGHTMARE_TIMER.setRemainingSeconds(seconds, "manual set");
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Nightmare timer set to " + seconds + " seconds."));
                                                    return 1;
                                                })))
                                .then(literal("toggle").executes(context -> {
                                    MonumentaToolsConfig.INSTANCE.nightmare.enabled =
                                            !MonumentaToolsConfig.INSTANCE.nightmare.enabled;
                                    MonumentaToolsConfig.save();
                                    context.getSource().sendFeedback(Text.literal(
                                            "Nightmare timer " +
                                                    (MonumentaToolsConfig.INSTANCE.nightmare.enabled ? "enabled." : "disabled.")));
                                    return 1;
                                })))));
    }
}
