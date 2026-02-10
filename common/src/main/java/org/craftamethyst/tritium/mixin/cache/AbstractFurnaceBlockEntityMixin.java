package org.craftamethyst.tritium.mixin.cache;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Unique
    private static final int DEFAULT_COOK_TIME = 200;
    @Shadow
    protected NonNullList<ItemStack> items;
    @Unique
    @Nullable
    private RecipeHolder<? extends AbstractCookingRecipe> tritium$cachedRecipe;
    @Unique
    private ItemStack tritium$cachedInput = ItemStack.EMPTY;
    @Unique
    private boolean tritium$cacheMissed;
    @Shadow
    @Final
    private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

    @Inject(
            method = "getTotalCookTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tritium$injectGetTotalCookTime(ServerLevel level, AbstractFurnaceBlockEntity furnace, CallbackInfoReturnable<Integer> cir) {
        AbstractFurnaceBlockEntityMixin self = (AbstractFurnaceBlockEntityMixin) (Object) furnace;

        assert self != null;
        if (self.items.isEmpty()) {
            self.tritium$cachedInput = ItemStack.EMPTY;
            self.tritium$cacheMissed = false;
            cir.setReturnValue(DEFAULT_COOK_TIME);
            return;
        }

        ItemStack currentInput = self.items.getFirst();
        if (currentInput.isEmpty()) {
            self.tritium$cachedInput = ItemStack.EMPTY;
            self.tritium$cacheMissed = false;
            cir.setReturnValue(DEFAULT_COOK_TIME);
            return;
        }

        RecipeHolder<? extends AbstractCookingRecipe> recipe = self.tritium$getCachedRecipe(currentInput, level);
        cir.setReturnValue(recipe == null ? DEFAULT_COOK_TIME : recipe.value().cookingTime());
    }

    @Unique
    @Nullable
    private RecipeHolder<? extends AbstractCookingRecipe> tritium$getCachedRecipe(ItemStack currentInput, ServerLevel level) {
        if (ItemStack.isSameItem(this.tritium$cachedInput, currentInput)) {
            return this.tritium$cacheMissed ? null : this.tritium$cachedRecipe;
        }

        this.tritium$cachedInput = currentInput.copy();

        if (currentInput.isEmpty()) {
            this.tritium$cacheMissed = false;
            this.tritium$cachedRecipe = null;
            return null;
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(currentInput);
        Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> recipe =
                this.quickCheck.getRecipeFor(recipeInput, level);

        if (recipe.isPresent()) {
            this.tritium$cachedRecipe = recipe.get();
            this.tritium$cacheMissed = false;
            return this.tritium$cachedRecipe;
        } else {
            this.tritium$cachedRecipe = null;
            this.tritium$cacheMissed = true;
            return null;
        }
    }

    @Inject(
            method = "setItem",
            at = @At("HEAD")
    )
    private void onSetItem(int index, ItemStack stack, CallbackInfo ci) {
        if (index == 0 && !ItemStack.isSameItem(this.tritium$cachedInput, stack)) {
            this.tritium$cachedInput = ItemStack.EMPTY;
            this.tritium$cacheMissed = false;
            this.tritium$cachedRecipe = null;
        }
    }
}