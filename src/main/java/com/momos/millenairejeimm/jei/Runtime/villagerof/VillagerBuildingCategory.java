package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillPanoramicCategory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillagerType;

/**
 * 展示村民类型 {@link VillagerType} 居住/工作的所有建筑计划集 {@link BuildingPlanSet} 的 JEI 全景大窗口分类。
 */
public class VillagerBuildingCategory extends BaseMillPanoramicCategory<VillagerType, BuildingPlanSet> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillagerBuildingCategoryPath,
            VillagerType.class
    );

    public VillagerBuildingCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.OAK_DOOR,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillagerBuildingCategory.asComponent(),
                MillIngredientTypes.BUILDING,
                MillIngredientTypes.VILLAGER,
                recipe -> {
                    String name = MillenaireLocalizeHelper.getVillagerName(recipe).getString();
                    return MillenaireJeimmKeys.VillagerBuildingCategoryTitle_S.asComponent(name);
                },
                MillenaireAPIHelper::getBuildingFormVillager
        );
    }
}
