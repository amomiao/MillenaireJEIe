package com.momos.millenairejeimm.jei.Runtime.villageof;

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
 * 展示村庄类型 {@link VillageType} 包含的所有建筑 {@link BuildingPlanSet} 的 JEI 全景大窗口分类。
 */
public class VillageBuildingsCategory extends BaseMillPanoramicCategory<VillageType, BuildingPlanSet> {

    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillageBuildingsCategoryPath,
            VillageType.class
    );

    public VillageBuildingsCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.BRICKS,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillageBuildingsCategory.asComponent(),
                MillIngredientTypes.BUILDING,
                MillIngredientTypes.VILLAGE,
                recipe -> {
                    String name = recipe.name() != null ? recipe.name() : recipe.id().getPath();
                    return MillenaireJeimmKeys.VillageBuildingsCategoryTitle_S.asComponent(name);
                },
                MillenaireAPIHelper::getBuildingFormVillage
        );
    }
}
