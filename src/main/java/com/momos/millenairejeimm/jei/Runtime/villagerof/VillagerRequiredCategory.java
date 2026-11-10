package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillPanoramicCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.culture.VillagerType;

import java.util.List;

/**
 * 展示村民类型 {@link VillagerType} 需求（Required）所有道具的全景大窗口分类。
 */
public class VillagerRequiredCategory extends BaseMillPanoramicCategory<VillagerType, ItemStack> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillagerRequiredCategoryPath,
            VillagerType.class
    );

    public VillagerRequiredCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.CRAFTING_TABLE,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillagerRequiredCategory.asComponent(),
                VanillaTypes.ITEM_STACK,
                MillIngredientTypes.VILLAGER,
                recipe -> {
                    String name = MillenaireLocalizeHelper.getVillagerName(recipe).getString();
                    return MillenaireJeimmKeys.VillagerRequiredCategoryTitle_S.asComponent(name);
                },
                recipe -> (recipe.resolvedRequiredGoods() != null && !recipe.requiredGoods().isEmpty())
                        ? recipe.resolvedRequiredGoods().entrySet().stream()
                        .filter(entry -> entry.getKey() != null && entry.getKey() != Items.AIR)
                        .map(entry -> new ItemStack(entry.getKey(), entry.getValue() != null ? entry.getValue() : 1))
                        .toList()
                        : List.of()
        );
    }
}
