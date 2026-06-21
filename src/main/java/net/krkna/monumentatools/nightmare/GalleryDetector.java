package net.krkna.monumentatools.nightmare;

import net.krkna.monumentatools.config.MonumentaToolsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public final class GalleryDetector {
    private GalleryDetector() {
    }

    public static boolean isInScope(MinecraftClient client) {
        if (client.player == null) {
            return false;
        }
        return isSupportedWorld(client.player.getWorld());
    }

    public static boolean isEntityInScope(Entity entity) {
        return isSupportedWorld(entity.getWorld());
    }

    private static boolean isSupportedWorld(World world) {
        String worldKey = world.getRegistryKey().getValue().toString();
        return switch (MonumentaToolsConfig.INSTANCE.nightmare.mapScope) {
            case MARINA_NOIR -> isMarina(worldKey);
            case SANGUINE_HALLS -> isSanguine(worldKey);
            case GALLERY_MAPS -> isMarina(worldKey) || isSanguine(worldKey);
            case ANY_WORLD -> true;
        };
    }

    private static boolean isMarina(String worldKey) {
        return worldKey.endsWith("marina") || worldKey.contains(":marina");
    }

    private static boolean isSanguine(String worldKey) {
        return worldKey.endsWith("sanguine")
                || worldKey.contains(":sanguine")
                || worldKey.endsWith("sanguine_halls")
                || worldKey.contains(":sanguine_halls");
    }
}
