package com.dualfishing.mixin.client;

import com.dualfishing.DualFishingPlayer;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.render.entity.state.FishingBobberEntityState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntityRenderer.class)
public class FishingBobberEntityRendererMixin {

    private static final ThreadLocal<FishingBobberEntity> CURRENT_BOBBER = new ThreadLocal<>();

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/projectile/FishingBobberEntity;Lnet/minecraft/client/render/entity/state/FishingBobberEntityState;F)V", at = @At("HEAD"))
    private void onUpdateRenderStateHead(FishingBobberEntity bobber, FishingBobberEntityState state, float tickDelta, CallbackInfo ci) {
        CURRENT_BOBBER.set(bobber);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/projectile/FishingBobberEntity;Lnet/minecraft/client/render/entity/state/FishingBobberEntityState;F)V", at = @At("RETURN"))
    private void onUpdateRenderStateReturn(FishingBobberEntity bobber, FishingBobberEntityState state, float tickDelta, CallbackInfo ci) {
        CURRENT_BOBBER.remove();
    }

    @Redirect(method = "getHandPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/FishingBobberEntityRenderer;getArmHoldingRod(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/Arm;"))
    private Arm redirectGetArmHoldingRod(PlayerEntity player) {
        FishingBobberEntity currentBobber = CURRENT_BOBBER.get();
        if (currentBobber != null) {
            int offHandId = player.getDataTracker().get(com.dualfishing.DualFishingMod.OFF_HAND_FISH_HOOK_ID);
            if (currentBobber.getId() == offHandId) {
                return player.getMainArm().getOpposite();
            }
        }
        return FishingBobberEntityRenderer.getArmHoldingRod(player);
    }
}
