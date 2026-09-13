package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import org.millenaire.building.BuildingPlanSet;

public class BuildingCostCategoryC extends AbstractBuildingCostCategory {
    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.BuildingCostCategoryCPath, BuildingPlanSet.class);

    public BuildingCostCategoryC(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'c', MillenaireJeimmKeys.BuildingCostCategoryC.asComponent());
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}