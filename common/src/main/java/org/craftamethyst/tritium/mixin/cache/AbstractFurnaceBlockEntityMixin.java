package org.craftamethyst.tritium.mixin.cache;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I"
            )
    )
    private static int redirectGetTotalCookTime(Level level, AbstractFurnaceBlockEntity blockEntity) {
        AbstractFurnaceBlockEntityMixin self = (AbstractFurnaceBlockEntityMixin) (Object) blockEntity;

        if (self != null && self.items.isEmpty()) {
            self.tritium$cachedInput = ItemStack.EMPTY;
            self.tritium$cacheMissed = false;
            return DEFAULT_COOK_TIME;
        }

        assert self != null;
        ItemStack currentInput = self.items.getFirst();
        if (currentInput.isEmpty()) {
            self.tritium$cachedInput = ItemStack.EMPTY;
            self.tritium$cacheMissed = false;
            return DEFAULT_COOK_TIME;
        }

        RecipeHolder<? extends AbstractCookingRecipe> recipe = self.tritium$getCachedRecipe(currentInput, level);

        return recipe == null ? DEFAULT_COOK_TIME : recipe.value().getCookingTime();
    }

    @Unique
    @Nullable
    private RecipeHolder<? extends AbstractCookingRecipe> tritium$getCachedRecipe(ItemStack currentInput, Level level) {
        if (ItemStack.isSameItemSameComponents(this.tritium$cachedInput, currentInput)) {
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
    private void onSetItem(int pIndex, ItemStack pStack, CallbackInfo ci) {
        if (pIndex == 0 && !ItemStack.isSameItemSameComponents(this.tritium$cachedInput, pStack)) {
            this.tritium$cachedInput = ItemStack.EMPTY;
            this.tritium$cacheMissed = false;
            this.tritium$cachedRecipe = null;
        }
    }
}