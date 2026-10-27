package com.uncrafting.mixin;

import com.uncrafting.UncraftingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    /**
     * BEFORE the click is processed by vanilla.
     * If the player is about to place an item into the result slot (slot 0)
     * of a CraftingScreenHandler, we set IS_UNCRAFTING = true.
     * This prevents CraftingScreenHandler.onContentChanged -> updateResult
     * from immediately clearing slot 0 before our afterSlotClick can run.
     */
    @Inject(method = "onSlotClick", at = @At("HEAD"))
    private void beforeSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler) (Object) this;
        if (handler instanceof CraftingScreenHandler) {
            if (slotIndex == 0
                    && actionType == SlotActionType.PICKUP
                    && !handler.getCursorStack().isEmpty()
                    && isGridEmpty(handler)) {
                UncraftingHelper.IS_UNCRAFTING.set(true);
            }
        }
    }

    /**
     * AFTER the click is processed by vanilla.
     * At this point, the item should be in slot 0 (because onContentChanged
     * was cancelled by our CraftingScreenHandlerMixin). We now look up
     * the recipe and populate the 3x3 grid.
     */
    @Inject(method = "onSlotClick", at = @At("RETURN"))
    private void afterSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!UncraftingHelper.IS_UNCRAFTING.get()) return;

        ScreenHandler handler = (ScreenHandler) (Object) this;
        if (handler instanceof CraftingScreenHandler) {
            if (slotIndex == 0) {
                ItemStack outputStack = handler.getSlot(0).getStack();
                if (!outputStack.isEmpty() && player.getEntityWorld() instanceof ServerWorld serverWorld) {
                    UncraftingHelper.UncraftResult result = UncraftingHelper.findRecipeFor(outputStack, serverWorld);
                    if (result != null) {
                        ItemStack recipeOutput = result.recipeOutput;

                        if (outputStack.getCount() >= recipeOutput.getCount()) {
                            // Handle excess: if they put too many, return the extra
                            if (outputStack.getCount() > recipeOutput.getCount()) {
                                ItemStack excess = outputStack.copy();
                                excess.setCount(outputStack.getCount() - recipeOutput.getCount());
                                ItemStack kept = outputStack.copy();
                                kept.setCount(recipeOutput.getCount());
                                handler.getSlot(0).setStack(kept);
                                if (!player.getInventory().insertStack(excess)) {
                                    player.dropStack(serverWorld, excess);
                                }
                            }

                            // Populate the 3x3 grid with ingredients
                            // IS_UNCRAFTING is still true, so setStack won't trigger updateResult
                            List<ItemStack> ingredients = result.ingredients;
                            for (int i = 0; i < 9 && (i + 1) < handler.slots.size(); i++) {
                                handler.getSlot(i + 1).setStack(ingredients.get(i));
                            }
                        }
                    }
                }
            }
        }
        // Always clear the flag at the end
        UncraftingHelper.IS_UNCRAFTING.set(false);
    }

    private boolean isGridEmpty(ScreenHandler handler) {
        for (int i = 1; i <= 9 && i < handler.slots.size(); i++) {
            if (!handler.getSlot(i).getStack().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
