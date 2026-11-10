package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillPanoramicCategory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;

/**
 * 展示建筑计划集 {@link BuildingPlanSet} 所属/允许包含的所有村庄类型 {@link VillageType} 的 JEI 全景大窗口分类。
 */
public class BuildingVillageCategory extends BaseMillPanoramicCategory<BuildingPlanSet, VillageType> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.BuildingVillageCategoryPath,
            BuildingPlanSet.class
    );

    public BuildingVillageCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.OAK_DOOR,
                RECIPE_TYPE,
                MillenaireJeimmKeys.BuildingVillageCategory.asComponent(),
                MillIngredientTypes.VILLAGE,
                MillIngredientTypes.BUILDING,
                recipe -> {
                    String name = recipe.buildingId() != null ? recipe.buildingId() : recipe.id().getPath();
                    return MillenaireJeimmKeys.BuildingVillageCategoryTitle_S.asComponent(name);
                },
                MillenaireAPIHelper::getVillageFormBuilding
        );
    }
}
