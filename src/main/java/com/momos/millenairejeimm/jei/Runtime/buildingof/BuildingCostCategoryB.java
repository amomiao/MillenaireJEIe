package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import org.millenaire.building.BuildingPlanSet;

public class BuildingCostCategoryB extends AbstractBuildingCostCategory {
    private static final String MOD_ID = "millenaire";
    private static final String RECIPE_TYPE_ID = "building_cost_b";
    private static final String VARIANT_LETTER = "B";

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MOD_ID, RECIPE_TYPE_ID, BuildingPlanSet.class);

    public BuildingCostCategoryB(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, ingredientManager, 'b', Component.translatableWithFallback(MillenaireJeimmKeys.VARIANT_TITLE, "Variant %s", VARIANT_LETTER).getString());
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }
}