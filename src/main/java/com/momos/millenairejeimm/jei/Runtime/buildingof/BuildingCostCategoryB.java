package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import org.millenaire.building.BuildingPlanSet;

public class BuildingCostCategoryB extends AbstractBuildingCostCategory {
    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.BuildingCostCategoryBPath, BuildingPlanSet.class);

    public BuildingCostCategoryB(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'b', MillenaireJeimmKeys.BuildingCostCategoryB.asComponent());
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}