package net.krkna.monumentatools.mixin;

import net.krkna.monumentatools.MonumentaToolsClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityStatusMixin {
    private static final byte DEATH_STATUS = 3;

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void monumentaTools$handleStatus(byte status, CallbackInfo ci) {
        if (status == DEATH_STATUS) {
            MonumentaToolsClient.NIGHTMARE_TIMER.onEntityDeathStatus((LivingEntity) (Object) this);
        }
    }
}
