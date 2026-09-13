// region BuildingCostCategory Variants Implementation
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import org.millenaire.building.BuildingPlanSet;

/**
 * 变体 A 建筑消耗材料 JEI 分类窗口。
 */
public class BuildingCostCategoryA extends AbstractBuildingCostCategory {
    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.BuildingCostCategoryAPath, BuildingPlanSet.class);

    public BuildingCostCategoryA(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'a', MillenaireJeimmKeys.BuildingCostCategoryA.asComponent());
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}
// endregion BuildingCostCategory Variants Implementation