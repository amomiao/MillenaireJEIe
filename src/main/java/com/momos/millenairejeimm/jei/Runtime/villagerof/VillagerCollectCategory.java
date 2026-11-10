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
import org.millenaire.item.ItemHelper;

import java.util.List;

/**
 * 展示村民类型 {@link VillagerType} 收集（Collect）所有道具的全景大窗口分类。
 */
public class VillagerCollectCategory extends BaseMillPanoramicCategory<VillagerType, ItemStack> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillagerCollectCategoryPath,
            VillagerType.class
    );

    public VillagerCollectCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.HOPPER,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillagerCollectCategory.asComponent(),
                VanillaTypes.ITEM_STACK,
                MillIngredientTypes.VILLAGER,
                recipe -> {
                    String name = MillenaireLocalizeHelper.getVillagerName(recipe).getString();
                    return MillenaireJeimmKeys.VillagerCollectCategoryTitle_S.asComponent(name);
                },
                recipe -> recipe.resolvedCollectGoods() != null
                        ? recipe.resolvedCollectGoods().stream()
                        .map(ItemHelper::resolve)
                        .filter(item -> item != null && item != Items.AIR)
                        .map(ItemStack::new)
                        .toList()
                        : List.of()
        );
    }
}
