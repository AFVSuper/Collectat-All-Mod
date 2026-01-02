package org.afv.collectatall.client.mixin;

import net.minecraft.recipe.book.RecipeBookOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeBookOptions.CategoryOption.class)
public abstract class CategoryOptionMixin {
    @Shadow
    @Final
    @Mutable
    public static RecipeBookOptions.CategoryOption DEFAULT;

    static {
        DEFAULT = new RecipeBookOptions.CategoryOption(false, true);
    }
}