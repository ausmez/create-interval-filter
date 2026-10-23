package dev.ausmez.createintervalfilter.init;

import com.simibubi.create.AllItems;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.item.IntervalFilterItem;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

public class ModItems {
    public static final ItemEntry<IntervalFilterItem> INTERVAL_FILTER_ITEM = CreateIntervalFilter.REGISTRATE
            .item("interval_filter", IntervalFilterItem::new)
            .recipe((ctx, prov) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 1)
                        .define('A', AllItems.ANDESITE_ALLOY)
                        .define('W', ItemTags.WOOL)
                        .pattern("   ")
                        .pattern("AWA")
                        .pattern("   ")
                        .unlockedBy("has_andesite_alloy", RegistrateRecipeProvider.has(AllItems.ANDESITE_ALLOY))
                        .save(prov);
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.get(), 1)
                        .requires(ctx.get())
                        .unlockedBy("has_interval_filter", RegistrateRecipeProvider.has(ctx.get()))
                        .save(prov, new ResourceLocation(CreateIntervalFilter.MODID, "interval_filter_clear"));
            })
            .register();

    public static void register() {}
}
