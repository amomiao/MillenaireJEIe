package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillPanoramicCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 展示外来商人（{@link VillagerType#foreignMerchantStock()}）携带货箱物品的全景大窗口分类。
 */
public class VillagerMerchantStockCategory extends BaseMillPanoramicCategory<VillagerType, ItemStack> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillagerMerchantStockCategoryPath,
            VillagerType.class
    );

    public VillagerMerchantStockCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.CHEST,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillagerMerchantStockCategory.asComponent(),
                VanillaTypes.ITEM_STACK,
                MillIngredientTypes.VILLAGER,
                recipe -> {
                    String name = MillenaireLocalizeHelper.getVillagerName(recipe).getString();
                    return MillenaireJeimmKeys.VillagerMerchantStockCategoryTitle_S.asComponent(name);
                },
                VillagerMerchantStockCategory::extractMerchantStockItems
        );
    }

    /**
     * 公共静态提取工具：从 {@link VillagerType#foreignMerchantStock()} 解析所有货物与携带数量。
     */
    public static List<ItemStack> extractMerchantStockItems(VillagerType recipe) {
        if (recipe == null || recipe.foreignMerchantStock() == null || recipe.foreignMerchantStock().isEmpty()) {
            return List.of();
        }
        List<ItemStack> items = new ArrayList<>();
        recipe.foreignMerchantStock().entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> {
                    ResourceLocation itemId = entry.getKey();
                    Item resolved = BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.AIR);
                    if (resolved == Items.AIR) {
                        resolved = ItemHelper.resolve(itemId.toString());
                    }
                    if (resolved != null && resolved != Items.AIR) {
                        int count = entry.getValue() != null && entry.getValue() > 0 ? entry.getValue() : 1;
                        items.add(new ItemStack(resolved, count));
                    }
                });
        return items;
    }
}
