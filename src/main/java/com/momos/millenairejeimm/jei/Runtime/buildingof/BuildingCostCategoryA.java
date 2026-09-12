// region BuildingCostCategory Variants Implementation
package com.momos.millenairejeimm.jei.Runtime.buildingof;

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
    private static final String MOD_ID = "millenaire";
    private static final String RECIPE_TYPE_ID = "building_cost_a";
    private static final String VARIANT_LETTER = "A";

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MOD_ID, RECIPE_TYPE_ID, BuildingPlanSet.class);

    public BuildingCostCategoryA(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'a', MillenaireJeimmKeys.BuildingCostCategoryA.asComponent());
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}
// endregion BuildingCostCategory Variants Implementation