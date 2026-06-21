package net.krkna.monumentatools.nightmare;

import net.krkna.monumentatools.config.MonumentaToolsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NightmareTimer {
    private static final Pattern SYNC_MARKER = Pattern.compile("\\[MT-NM:(\\d{1,4})]");
    private static final Pattern ROUND_START = Pattern.compile("(?i)\\bStarting round:\\s*(\\d+)\\b");
    private static final Pattern LOOT_ROOM = Pattern.compile("(?i)has succumbed to the Nightmare in Marina Noir!?");
    private static final int[] HEARTBEAT_MILESTONES = {30, 15, 10, 5};

    private int remainingTicks = MonumentaToolsConfig.INSTANCE.nightmare.resetTicks();
    private boolean active;
    private boolean waitingForRoundStart;
    private String lastResetReason = "waiting";
    private long lastResetMillis;
    private int lastDeathEntityId = Integer.MIN_VALUE;
    private long lastDeathEntityMillis;
    private boolean lootRoomSuppressed;
    private final boolean[] heartbeatMilestonesSent = new boolean[HEARTBEAT_MILESTONES.length];

    public void tick(MinecraftClient client) {
        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        if (!config.enabled) {
            active = false;
            waitingForRoundStart = false;
            return;
        }

        if (!GalleryDetector.isInScope(client)) {
            active = false;
            waitingForRoundStart = false;
            lootRoomSuppressed = false;
            remainingTicks = config.resetTicks();
            lastResetReason = "outside supported map";
            return;
        }

        if (lootRoomSuppressed) {
            active = false;
            return;
        }

        if (!active) {
            waitingForRoundStart = true;
            remainingTicks = config.resetTicks();
            lastResetReason = "waiting for round";
            return;
        }

        if (remainingTicks > 0) {
            remainingTicks--;
        }
        maybeScheduleHeartbeat();
    }

    public void onEntityDeathStatus(Entity entity) {
        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        if (!config.enabled || lootRoomSuppressed || !GalleryDetector.isEntityInScope(entity) || !isRelevantDeath(entity, config)) {
            return;
        }
        int beforeResetSeconds = remainingSeconds();
        reset(entity.getName().getString() + " death");
        if (beforeResetSeconds <= config.chatSyncResetBroadcastBelowSeconds()) {
            sendChatSync(remainingSeconds());
        }
    }

    public void onIncomingMessage(String message) {
        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        if (!config.enabled || !GalleryDetector.isInScope(MinecraftClient.getInstance())) {
            return;
        }

        Matcher roundMatcher = ROUND_START.matcher(message);
        if (roundMatcher.find()) {
            lootRoomSuppressed = false;
            reset("round " + roundMatcher.group(1));
            return;
        }

        if (config.hideInLootRoomBeta && LOOT_ROOM.matcher(message).find()) {
            lootRoomSuppressed = true;
            active = false;
            lastResetReason = "loot room";
            return;
        }

        if (lootRoomSuppressed) {
            return;
        }

        if (!config.chatSyncReceive) {
            return;
        }

        Matcher matcher = SYNC_MARKER.matcher(message);
        if (!matcher.find()) {
            return;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return;
        }
        if (seconds <= 0) {
            return;
        }

        markHeartbeatMilestone(seconds);

        int incomingTicks = Math.max(0, seconds) * 20;
        if (!active || incomingTicks > remainingTicks) {
            setRemainingSeconds(seconds, "chat sync");
        }
    }

    public void reset(String reason) {
        remainingTicks = MonumentaToolsConfig.INSTANCE.nightmare.resetTicks();
        active = true;
        waitingForRoundStart = false;
        lootRoomSuppressed = false;
        lastResetReason = reason;
        lastResetMillis = System.currentTimeMillis();
        resetHeartbeatMilestones();
    }

    public void setRemainingSeconds(int seconds, String reason) {
        remainingTicks = Math.max(0, seconds) * 20;
        active = true;
        waitingForRoundStart = false;
        lootRoomSuppressed = false;
        lastResetReason = reason;
        lastResetMillis = System.currentTimeMillis();
        if (seconds >= MonumentaToolsConfig.INSTANCE.nightmare.resetSeconds) {
            resetHeartbeatMilestones();
        }
    }

    public boolean shouldRender() {
        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        if (!config.enabled) {
            return false;
        }
        if (lootRoomSuppressed) {
            return false;
        }
        if (!GalleryDetector.isInScope(MinecraftClient.getInstance())) {
            return false;
        }
        return active || waitingForRoundStart || config.showWhenInactive;
    }

    public List<String> hudLines() {
        List<String> lines = new ArrayList<>();
        lines.add(primaryLine());
        if (MonumentaToolsConfig.INSTANCE.nightmare.showDebugLine) {
            lines.add(debugLine());
        }
        return lines;
    }

    public List<String> previewHudLines() {
        List<String> lines = new ArrayList<>();
        lines.add("Nightmares: 00:42");
        if (MonumentaToolsConfig.INSTANCE.nightmare.showDebugLine) {
            lines.add("reset: preview");
        }
        return lines;
    }

    public int textColor() {
        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        if (remainingSeconds() <= 0) {
            return config.expiredTextColor;
        }
        if (remainingSeconds() <= config.warningSeconds()) {
            return config.warningTextColor;
        }
        return config.normalTextColor;
    }

    public String statusLine(MinecraftClient client) {
        String worldKey = "unknown";
        if (client.player != null) {
            worldKey = client.player.getWorld().getRegistryKey().getValue().toString();
        }

        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        return "Nightmare timer: enabled=" + config.enabled +
                ", inScope=" + GalleryDetector.isInScope(client) +
                ", active=" + active +
                ", waitingForRound=" + waitingForRoundStart +
                ", remaining=" + remainingSeconds() + "s" +
                ", world=" + worldKey +
                ", mapScope=" + config.mapScope.name() +
                ", lastReset=" + lastResetReason +
                ", chatReceive=" + config.chatSyncReceive +
                ", chatBroadcast=" + config.chatSyncBroadcast +
                ", resetBroadcastBelow=" + config.chatSyncResetBroadcastBelowSeconds() + "s" +
                ", heartbeat=" + config.chatSyncHeartbeat +
                ", heartbeatMilestones=30/15/10/5" +
                ", lootHideBeta=" + config.hideInLootRoomBeta +
                ", lootSuppressed=" + lootRoomSuppressed +
                ", channel=/" + config.chatSyncChannel.command();
    }

    private boolean isRelevantDeath(Entity entity, MonumentaToolsConfig.NightmareConfig config) {
        if (!(entity instanceof LivingEntity) || entity instanceof PlayerEntity) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (entity.getId() == lastDeathEntityId && now - lastDeathEntityMillis < 1000L) {
            return false;
        }
        lastDeathEntityId = entity.getId();
        lastDeathEntityMillis = now;
        return config.resetOnAnyLivingDeath || entity instanceof MobEntity;
    }

    private String primaryLine() {
        if (waitingForRoundStart && !active) {
            return "Nightmares: waiting";
        }
        int seconds = remainingSeconds();
        if (seconds <= 0) {
            return "Nightmares: now";
        }
        return "Nightmares: " + format(seconds);
    }

    private int remainingSeconds() {
        return Math.max(0, (remainingTicks + 19) / 20);
    }

    private String debugLine() {
        if (lastResetMillis == 0) {
            return "reset: " + lastResetReason;
        }
        long ageSeconds = Math.max(0, (System.currentTimeMillis() - lastResetMillis) / 1000);
        return "reset: " + lastResetReason + " " + ageSeconds + "s ago";
    }

    private void sendChatSync(int markerSeconds) {
        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        if (!config.chatSyncBroadcast || lootRoomSuppressed || markerSeconds <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !GalleryDetector.isInScope(client)) {
            return;
        }

        client.player.networkHandler.sendCommand(config.chatSyncChannel.command() + " [MT-NM:" + markerSeconds + "]");
    }

    private void maybeScheduleHeartbeat() {
        MonumentaToolsConfig.NightmareConfig config = MonumentaToolsConfig.INSTANCE.nightmare;
        if (!config.chatSyncBroadcast || !config.chatSyncHeartbeat || !active) {
            return;
        }
        int seconds = remainingSeconds();
        if (seconds <= 0) {
            return;
        }

        for (int index = 0; index < HEARTBEAT_MILESTONES.length; index++) {
            int milestone = HEARTBEAT_MILESTONES[index];
            if (!heartbeatMilestonesSent[index] && seconds == milestone) {
                heartbeatMilestonesSent[index] = true;
                if (shouldSendMilestoneHeartbeat()) {
                    sendChatSync(milestone);
                }
                return;
            }
        }
    }

    private boolean shouldSendMilestoneHeartbeat() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return true;
        }

        String ownName = client.player.getGameProfile().getName();
        for (var player : client.world.getPlayers()) {
            String otherName = player.getGameProfile().getName();
            if (otherName.compareToIgnoreCase(ownName) < 0) {
                return false;
            }
        }
        return true;
    }

    private void resetHeartbeatMilestones() {
        for (int index = 0; index < heartbeatMilestonesSent.length; index++) {
            heartbeatMilestonesSent[index] = false;
        }
    }

    private void markHeartbeatMilestone(int seconds) {
        for (int index = 0; index < HEARTBEAT_MILESTONES.length; index++) {
            if (HEARTBEAT_MILESTONES[index] == seconds) {
                heartbeatMilestonesSent[index] = true;
                return;
            }
        }
    }

    private static String format(int seconds) {
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return String.format("%02d:%02d", minutes, remainder);
    }
}
