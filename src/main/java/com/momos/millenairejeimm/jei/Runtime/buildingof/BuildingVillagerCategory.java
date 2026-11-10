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
import org.millenaire.culture.VillagerType;

/**
 * 展示建筑计划集 {@link BuildingPlanSet} 关联/居住的所有村民类型 {@link VillagerType} 的 JEI 全景大窗口分类。
 */
public class BuildingVillagerCategory extends BaseMillPanoramicCategory<BuildingPlanSet, VillagerType> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.BuildingVillagerCategoryPath,
            BuildingPlanSet.class
    );

    public BuildingVillagerCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.PLAYER_HEAD,
                RECIPE_TYPE,
                MillenaireJeimmKeys.BuildingVillagerCategory.asComponent(),
                MillIngredientTypes.VILLAGER,
                MillIngredientTypes.BUILDING,
                recipe -> {
                    String name = recipe.buildingId() != null ? recipe.buildingId() : recipe.id().getPath();
                    return MillenaireJeimmKeys.BuildingVillagerCategoryTitle_S.asComponent(name);
                },
                MillenaireAPIHelper::getVillagerFormBuilding
        );
    }
}
