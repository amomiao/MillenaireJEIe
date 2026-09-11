package com.momos.millenairejeimm.jei.Runtime.buildingof;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import org.millenaire.building.BuildingPlanSet;

/**
 * 变体 C 建筑消耗材料 JEI 分类窗口。
 */
public class BuildingCostCategoryC extends AbstractBuildingCostCategory {
    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_cost_c", BuildingPlanSet.class);

    public BuildingCostCategoryC(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'c', "变体 C");
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}