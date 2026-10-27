package com.dualfishing.mixin;

import com.dualfishing.DualFishingPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUseHead(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (user.isSneaking() && hand == Hand.MAIN_HAND) {
            if (user.getStackInHand(Hand.OFF_HAND).getItem() instanceof FishingRodItem) {
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
        }

        if (hand == Hand.OFF_HAND) {
            FishingBobberEntity temp = user.fishHook;
            user.fishHook = ((DualFishingPlayer) user).getOffHandFishHook();
            ((DualFishingPlayer) user).setOffHandFishHook(temp);
        }
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void onUseReturn(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (hand == Hand.OFF_HAND) {
            FishingBobberEntity temp = user.fishHook;
            user.fishHook = ((DualFishingPlayer) user).getOffHandFishHook();
            ((DualFishingPlayer) user).setOffHandFishHook(temp);
        }
    }
}
