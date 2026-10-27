package com.dualfishing.mixin;

import com.dualfishing.DualFishingMod;
import com.dualfishing.DualFishingPlayer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements DualFishingPlayer {
    @Unique
    private FishingBobberEntity offHandFishHook;
    @Unique
    private boolean isCastingOffhand = false;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(DualFishingMod.OFF_HAND_FISH_HOOK_ID, 0);
    }

    @Override
    public FishingBobberEntity getOffHandFishHook() {
        return this.offHandFishHook;
    }

    @Override
    public void setOffHandFishHook(FishingBobberEntity fishHook) {
        this.offHandFishHook = fishHook;
        if (!((PlayerEntity)(Object)this).getEntityWorld().isClient()) {
            ((PlayerEntity)(Object)this).getDataTracker().set(DualFishingMod.OFF_HAND_FISH_HOOK_ID, fishHook == null ? 0 : fishHook.getId());
        }
    }

    @Override
    public boolean isCastingOffhand() {
        return this.isCastingOffhand;
    }

    @Override
    public void setCastingOffhand(boolean castingOffhand) {
        this.isCastingOffhand = castingOffhand;
    }
}
