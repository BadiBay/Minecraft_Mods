package com.dualfishing.mixin;

import com.dualfishing.DualFishingPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        FishingBobberEntity self = (FishingBobberEntity)(Object)this;
        PlayerEntity owner = self.getPlayerOwner();
        if (owner != null) {
            if (((DualFishingPlayer) owner).getOffHandFishHook() == self) {
                ((DualFishingPlayer) owner).setOffHandFishHook(null);
            }
        }
    }
}
