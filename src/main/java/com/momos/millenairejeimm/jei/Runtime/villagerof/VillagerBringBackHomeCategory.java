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

/**
 * 展示村民类型 {@link VillagerType} 带回家（Bring Back Home）所有道具的全景大窗口分类。
 */
public class VillagerBringBackHomeCategory extends BaseMillPanoramicCategory<VillagerType, ItemStack> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillagerBringBackHomeCategoryPath,
            VillagerType.class
    );

    public VillagerBringBackHomeCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.CHEST,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillagerBringBackHomeCategory.asComponent(),
                VanillaTypes.ITEM_STACK,
                MillIngredientTypes.VILLAGER,
                recipe -> {
                    String name = MillenaireLocalizeHelper.getVillagerName(recipe).getString();
                    return MillenaireJeimmKeys.VillagerBringBackHomeCategoryTitle_S.asComponent(name);
                },
                recipe -> recipe.resolvedBringBackHomeGoods() != null
                        ? recipe.resolvedBringBackHomeGoods().stream()
                        .filter(item -> item != null && item != Items.AIR)
                        .map(ItemStack::new)
                        .toList()
                        : java.util.List.of()
        );
    }
}
