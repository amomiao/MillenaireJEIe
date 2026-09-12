package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import org.millenaire.building.BuildingPlanSet;

public class BuildingCostCategoryC extends AbstractBuildingCostCategory {
    private static final String MOD_ID = "millenaire";
    private static final String RECIPE_TYPE_ID = "building_cost_c";
    private static final String VARIANT_LETTER = "C";

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MOD_ID, RECIPE_TYPE_ID, BuildingPlanSet.class);

    public BuildingCostCategoryC(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'c', MillenaireJeimmKeys.BuildingCostCategoryC.asComponent());
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}