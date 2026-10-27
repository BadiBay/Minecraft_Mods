package com.uncrafting.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void onCanInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // Allow insertion into the output slot to enable uncrafting
        cir.setReturnValue(true);
    }
}
