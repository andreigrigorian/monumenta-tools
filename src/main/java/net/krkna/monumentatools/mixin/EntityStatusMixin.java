package net.krkna.monumentatools.mixin;

import net.krkna.monumentatools.MonumentaToolsClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityStatusMixin {
    private static final byte DEATH_STATUS = 3;

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void monumentaTools$handleStatus(byte status, CallbackInfo ci) {
        if (status == DEATH_STATUS && (Object) this instanceof LivingEntity livingEntity) {
            MonumentaToolsClient.NIGHTMARE_TIMER.onEntityDeathStatus(livingEntity);
        }
    }
}
