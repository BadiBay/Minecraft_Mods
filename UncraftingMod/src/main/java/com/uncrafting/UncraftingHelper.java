package com.uncrafting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class UncraftingHelper {

    public static final ThreadLocal<Boolean> IS_UNCRAFTING = ThreadLocal.withInitial(() -> false);

    /**
     * Data class holding the result of finding a recipe:
     * the recipe's output stack (for count comparison) and the ingredient stacks.
     */
    public static class UncraftResult {
        public final ItemStack recipeOutput;
        public final List<ItemStack> ingredients; // size 9, mapped to 3x3 grid

        public UncraftResult(ItemStack recipeOutput, List<ItemStack> ingredients) {
            this.recipeOutput = recipeOutput;
            this.ingredients = ingredients;
        }
    }

    /**
     * Find the first crafting recipe whose output matches the given item
     * and return the ingredients mapped to a 3x3 grid.
     *
     * In 1.21.11, the Recipe API has changed:
     * - RecipeManager is minimal; ServerRecipeManager has values()
     * - Recipe no longer has getResult(). ShapedRecipe/ShapelessRecipe have a package-private `result` field.
     *   We use craft() to get the output.
     * - Ingredient no longer has getMatchingStacks(). Use getMatchingItems() which returns Stream<RegistryEntry<Item>>.
     * - ShapedRecipe.getIngredients() returns List<Optional<Ingredient>>.
     */
    public static UncraftResult findRecipeFor(ItemStack outputStack, ServerWorld world) {
        if (outputStack == null || outputStack.isEmpty()) {
            return null;
        }

        ServerRecipeManager recipeManager = world.getRecipeManager();
        Collection<RecipeEntry<?>> allRecipes = recipeManager.values();

        for (RecipeEntry<?> entry : allRecipes) {
            Recipe<?> recipe = entry.value();
            if (!(recipe instanceof CraftingRecipe craftingRecipe)) continue;

            // Get the recipe output by checking the result field via cast
            ItemStack recipeOutput;
            List<ItemStack> gridStacks = new ArrayList<>();
            for (int i = 0; i < 9; i++) gridStacks.add(ItemStack.EMPTY);

            if (craftingRecipe instanceof ShapedRecipe shaped) {
                recipeOutput = shaped.result.copy();
                if (recipeOutput.getItem() != outputStack.getItem()) continue;

                // Map ingredients using width/height
                List<Optional<Ingredient>> ingredients = shaped.getIngredients();
                int width = shaped.getWidth();
                int height = shaped.getHeight();
                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        int ingredientIndex = row * width + col;
                        int gridIndex = row * 3 + col;
                        if (ingredientIndex < ingredients.size()) {
                            Optional<Ingredient> optIngredient = ingredients.get(ingredientIndex);
                            if (optIngredient.isPresent()) {
                                Ingredient ingredient = optIngredient.get();
                                ItemStack stack = ingredientToStack(ingredient);
                                if (!stack.isEmpty()) {
                                    gridStacks.set(gridIndex, stack);
                                }
                            }
                        }
                    }
                }
                return new UncraftResult(recipeOutput, gridStacks);

            } else if (craftingRecipe instanceof ShapelessRecipe shapeless) {
                recipeOutput = shapeless.result.copy();
                if (recipeOutput.getItem() != outputStack.getItem()) continue;

                // Shapeless: use IngredientPlacement to get ingredients
                List<Ingredient> ingredients = shapeless.getIngredientPlacement().getIngredients();
                for (int i = 0; i < ingredients.size() && i < 9; i++) {
                    Ingredient ingredient = ingredients.get(i);
                    ItemStack stack = ingredientToStack(ingredient);
                    if (!stack.isEmpty()) {
                        gridStacks.set(i, stack);
                    }
                }
                return new UncraftResult(recipeOutput, gridStacks);
            }
        }
        return null;
    }

    /**
     * Convert an Ingredient to the first matching ItemStack.
     * In 1.21.11, Ingredient.getMatchingItems() returns Stream<RegistryEntry<Item>>.
     */
    private static ItemStack ingredientToStack(Ingredient ingredient) {
        if (ingredient.isEmpty()) return ItemStack.EMPTY;
        Optional<RegistryEntry<Item>> first = ingredient.getMatchingItems().findFirst();
        if (first.isPresent()) {
            return new ItemStack(first.get());
        }
        return ItemStack.EMPTY;
    }
}
