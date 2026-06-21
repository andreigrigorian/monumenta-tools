package net.krkna.monumentatools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.NameableEnum;
import net.fabricmc.loader.api.FabricLoader;
import net.krkna.monumentatools.MonumentaToolsClient;
import net.minecraft.text.Text;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MonumentaToolsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("monumenta-tools.json");
    private static final int CURRENT_SCHEMA_VERSION = 5;

    public static MonumentaToolsConfig INSTANCE = new MonumentaToolsConfig();

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public NightmareConfig nightmare = new NightmareConfig();

    public static MonumentaToolsConfig load() {
        if (!Files.exists(FILE)) {
            INSTANCE = new MonumentaToolsConfig();
            save();
            return INSTANCE;
        }

        try (Reader reader = Files.newBufferedReader(FILE)) {
            MonumentaToolsConfig loaded = GSON.fromJson(reader, MonumentaToolsConfig.class);
            INSTANCE = loaded == null ? new MonumentaToolsConfig() : loaded;
            if (INSTANCE.nightmare == null) {
                INSTANCE.nightmare = new NightmareConfig();
            }
            migrate();
        } catch (Exception e) {
            MonumentaToolsClient.LOGGER.error("Failed to read config. Using defaults.", e);
            INSTANCE = new MonumentaToolsConfig();
        }
        return INSTANCE;
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            MonumentaToolsClient.LOGGER.error("Failed to save config.", e);
        }
    }

    private static void migrate() {
        boolean changed = false;
        int loadedSchemaVersion = INSTANCE.schemaVersion;
        if (loadedSchemaVersion < 2) {
            INSTANCE.nightmare.mapScope = MapScope.MARINA_NOIR;
            INSTANCE.nightmare.chatSyncBroadcast = true;
            changed = true;
        }
        if (loadedSchemaVersion < 3) {
            INSTANCE.nightmare.mapScope = MapScope.MARINA_NOIR;
            INSTANCE.nightmare.chatSyncBroadcast = true;
            INSTANCE.nightmare.resetOnAnyLivingDeath = true;
            INSTANCE.nightmare.hudX = 284;
            INSTANCE.nightmare.hudY = 268;
            INSTANCE.nightmare.hudScale = 0.8f;
            changed = true;
        }
        if (loadedSchemaVersion < 4) {
            changed = true;
        }
        if (loadedSchemaVersion < 5) {
            changed = true;
        }
        if (INSTANCE.nightmare.mapScope == null) {
            INSTANCE.nightmare.mapScope = MapScope.MARINA_NOIR;
            changed = true;
        }
        if (INSTANCE.nightmare.chatSyncChannel == null) {
            INSTANCE.nightmare.chatSyncChannel = ChatChannel.WC;
            changed = true;
        }
        if (changed) {
            INSTANCE.schemaVersion = CURRENT_SCHEMA_VERSION;
            save();
        }
    }

    public static class NightmareConfig {
        public boolean enabled = true;
        public MapScope mapScope = MapScope.MARINA_NOIR;
        @Deprecated
        public boolean onlyInGallery = true;
        public boolean resetOnAnyLivingDeath = true;
        public boolean showWhenInactive = true;
        public boolean showBackground = true;
        public boolean showDebugLine = false;
        public boolean hideInLootRoomBeta = false;
        public boolean chatSyncReceive = true;
        public boolean chatSyncBroadcast = true;
        public boolean chatSyncHeartbeat = true;
        public ChatChannel chatSyncChannel = ChatChannel.WC;
        public int resetSeconds = 60;
        public int warningSeconds = 15;
        public int chatSyncResetBroadcastBelowSeconds = 45;
        public int hudX = 284;
        public int hudY = 268;
        public float hudScale = 0.8f;
        public int normalTextColor = 0x55DDE0;
        public int warningTextColor = 0xFFAA00;
        public int expiredTextColor = 0xFF5555;
        public int backgroundColor = 0xAA101820;

        public int resetTicks() {
            return Math.max(1, resetSeconds) * 20;
        }

        public int warningSeconds() {
            return Math.max(0, warningSeconds);
        }

        public int chatSyncResetBroadcastBelowSeconds() {
            return Math.max(0, chatSyncResetBroadcastBelowSeconds);
        }

        public float hudScale() {
            if (hudScale < 0.5f) {
                return 0.5f;
            }
            return Math.min(hudScale, 4.0f);
        }
    }

    public enum MapScope implements NameableEnum {
        MARINA_NOIR("Marina Noir only"),
        SANGUINE_HALLS("Sanguine Halls only"),
        GALLERY_MAPS("Both Gallery maps"),
        ANY_WORLD("Any world");

        private final String label;

        MapScope(String label) {
            this.label = label;
        }

        @Override
        public Text getDisplayName() {
            return Text.literal(label);
        }
    }

    public enum ChatChannel implements NameableEnum {
        WC("wc", "Shard /wc"),
        LOCAL("l", "Local /l"),
        GLOBAL("g", "Global /g");

        private final String command;
        private final String label;

        ChatChannel(String command, String label) {
            this.command = command;
            this.label = label;
        }

        public String command() {
            return command;
        }

        @Override
        public Text getDisplayName() {
            return Text.literal(label);
        }
    }
}
