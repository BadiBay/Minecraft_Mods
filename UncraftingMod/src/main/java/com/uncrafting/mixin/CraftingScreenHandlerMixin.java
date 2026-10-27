package com.uncrafting.mixin;

import com.uncrafting.UncraftingHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {

    @Inject(method = "onContentChanged", at = @At("HEAD"), cancellable = true)
    private void onContentChanged(Inventory inventory, CallbackInfo ci) {
        if (UncraftingHelper.IS_UNCRAFTING.get()) {
            ci.cancel();
        }
    }
}
