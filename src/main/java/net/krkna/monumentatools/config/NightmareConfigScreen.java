package net.krkna.monumentatools.config;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.krkna.monumentatools.nightmare.NightmareHudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class NightmareConfigScreen {
    private NightmareConfigScreen() {
    }

    public static Screen create(Screen parent) {
        MonumentaToolsConfig.NightmareConfig nightmare = MonumentaToolsConfig.INSTANCE.nightmare;

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Monumenta Tools"))
                .save(MonumentaToolsConfig::save)
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Nightmare Timer"))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable timer"))
                                .description(OptionDescription.of(Text.literal(
                                        "Shows the Marina Noir nightmare countdown.")))
                                .binding(true, () -> nightmare.enabled, newValue -> nightmare.enabled = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Squad sync"))
                                .description(OptionDescription.of(Text.literal(
                                        "Uses /wc markers near the end of rounds so nearby teammates stay synchronized.")))
                                .binding(true,
                                        () -> nightmare.chatSyncReceive && nightmare.chatSyncBroadcast,
                                        newValue -> {
                                            nightmare.chatSyncReceive = newValue;
                                            nightmare.chatSyncBroadcast = newValue;
                                        })
                                .controller(TickBoxControllerBuilder::create)))
                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("HUD position"))
                                .text(Text.literal("Edit"))
                                .description(OptionDescription.of(Text.literal(
                                        "Opens a drag screen for moving the timer.")))
                                .action(screen -> {
                                    MonumentaToolsConfig.save();
                                    MinecraftClient.getInstance().setScreen(new NightmareHudEditorScreen(screen));
                                })
                                .build())
                        .option(autosave(Option.<Float>createBuilder()
                                .name(Text.literal("HUD scale"))
                                .description(OptionDescription.of(Text.literal(
                                        "Timer text scale.")))
                                .binding(0.8f, () -> nightmare.hudScale, newValue -> nightmare.hudScale = clamp(newValue))
                                .controller(FloatFieldControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Background"))
                                .description(OptionDescription.of(Text.literal(
                                        "Draws a translucent box behind the timer.")))
                                .binding(true, () -> nightmare.showBackground,
                                        newValue -> nightmare.showBackground = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Hide in loot room (BETA)"))
                                .description(OptionDescription.of(Text.literal(
                                        "Hides the timer after the Marina Noir wipe message until you leave the map or a new round starts.")))
                                .binding(false, () -> nightmare.hideInLootRoomBeta,
                                        newValue -> nightmare.hideInLootRoomBeta = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Developer Settings"))
                        .option(autosave(Option.<MonumentaToolsConfig.MapScope>createBuilder()
                                .name(Text.literal("Map scope"))
                                .description(OptionDescription.of(Text.literal(
                                        "Where the timer is allowed to run. The default is Marina Noir only.")))
                                .binding(MonumentaToolsConfig.MapScope.MARINA_NOIR, () -> nightmare.mapScope,
                                        newValue -> nightmare.mapScope = newValue)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(MonumentaToolsConfig.MapScope.class))))
                        .option(autosave(Option.<Integer>createBuilder()
                                .name(Text.literal("Reset seconds"))
                                .description(OptionDescription.of(Text.literal(
                                        "How many seconds the timer jumps to after a detected mob death.")))
                                .binding(60, () -> nightmare.resetSeconds,
                                        newValue -> nightmare.resetSeconds = Math.max(1, newValue))
                                .controller(IntegerFieldControllerBuilder::create)))
                        .option(autosave(Option.<Integer>createBuilder()
                                .name(Text.literal("Warning seconds"))
                                .description(OptionDescription.of(Text.literal(
                                        "The timer switches to the warning color at or below this value.")))
                                .binding(15, () -> nightmare.warningSeconds,
                                        newValue -> nightmare.warningSeconds = Math.max(0, newValue))
                                .controller(IntegerFieldControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Any living death"))
                                .description(OptionDescription.of(Text.literal(
                                        "Reset on any non-player living entity death instead of only mob entities.")))
                                .binding(true, () -> nightmare.resetOnAnyLivingDeath,
                                        newValue -> nightmare.resetOnAnyLivingDeath = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Debug line"))
                                .description(OptionDescription.of(Text.literal(
                                        "Show the most recent reset source under the timer.")))
                                .binding(false, () -> nightmare.showDebugLine,
                                        newValue -> nightmare.showDebugLine = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Receive chat sync"))
                                .description(OptionDescription.of(Text.literal(
                                        "Reset from visible Monumenta Tools nightmare markers in chat.")))
                                .binding(true, () -> nightmare.chatSyncReceive,
                                        newValue -> nightmare.chatSyncReceive = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Broadcast chat sync"))
                                .description(OptionDescription.of(Text.literal(
                                        "Send visible chat markers for important resets, throttled to avoid spam.")))
                                .binding(true, () -> nightmare.chatSyncBroadcast,
                                        newValue -> nightmare.chatSyncBroadcast = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .option(autosave(Option.<MonumentaToolsConfig.ChatChannel>createBuilder()
                                .name(Text.literal("Sync channel"))
                                .description(OptionDescription.of(Text.literal(
                                        "Chat command used for sync markers when broadcasting is enabled.")))
                                .binding(MonumentaToolsConfig.ChatChannel.WC, () -> nightmare.chatSyncChannel,
                                        newValue -> nightmare.chatSyncChannel = newValue)
                                .controller(option -> EnumControllerBuilder.create(option)
                                        .enumClass(MonumentaToolsConfig.ChatChannel.class))))
                        .option(autosave(Option.<Integer>createBuilder()
                                .name(Text.literal("Reset broadcast window"))
                                .description(OptionDescription.of(Text.literal(
                                        "Only broadcast kill resets when the timer was at or below this many seconds.")))
                                .binding(45, () -> nightmare.chatSyncResetBroadcastBelowSeconds,
                                        newValue -> nightmare.chatSyncResetBroadcastBelowSeconds = Math.max(0, newValue))
                                .controller(IntegerFieldControllerBuilder::create)))
                        .option(autosave(Option.<Boolean>createBuilder()
                                .name(Text.literal("Low timer heartbeat"))
                                .description(OptionDescription.of(Text.literal(
                                        "Broadcasts countdown milestones at 30, 15, 10, and 5 seconds.")))
                                .binding(true, () -> nightmare.chatSyncHeartbeat,
                                        newValue -> nightmare.chatSyncHeartbeat = newValue)
                                .controller(TickBoxControllerBuilder::create)))
                        .option(autosave(Option.<Integer>createBuilder()
                                .name(Text.literal("HUD X"))
                                .description(OptionDescription.of(Text.literal(
                                        "Horizontal timer position.")))
                                .binding(284, () -> nightmare.hudX, newValue -> nightmare.hudX = Math.max(0, newValue))
                                .controller(IntegerFieldControllerBuilder::create)))
                        .option(autosave(Option.<Integer>createBuilder()
                                .name(Text.literal("HUD Y"))
                                .description(OptionDescription.of(Text.literal(
                                        "Vertical timer position.")))
                                .binding(268, () -> nightmare.hudY, newValue -> nightmare.hudY = Math.max(0, newValue))
                                .controller(IntegerFieldControllerBuilder::create)))
                        .build())
                .build()
                .generateScreen(parent);
    }

    private static <T> Option<T> autosave(Option.Builder<T> builder) {
        return builder
                .instant(true)
                .listener((option, value) -> MonumentaToolsConfig.save())
                .build();
    }

    private static float clamp(float value) {
        if (value < 0.5f) {
            return 0.5f;
        }
        return Math.min(value, 4.0f);
    }
}
