package com.momos.millenairejeimm.jei.Runtime.buildingof;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import org.millenaire.building.BuildingPlanSet;

/**
 * 变体 B 建筑消耗材料 JEI 分类窗口。
 */
public class BuildingCostCategoryB extends AbstractBuildingCostCategory {
    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_cost_b", BuildingPlanSet.class);

    public BuildingCostCategoryB(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'b', "变体 B");
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}