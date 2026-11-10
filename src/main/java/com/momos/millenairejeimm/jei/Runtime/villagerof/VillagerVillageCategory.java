package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillPanoramicCategory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.Items;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

/**
 * 展示村民类型 {@link VillagerType} 所属/允许出现的所有村庄类型 {@link VillageType} 的 JEI 全景大窗口分类。
 */
public class VillagerVillageCategory extends BaseMillPanoramicCategory<VillagerType, VillageType> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillagerVillageCategoryPath,
            VillagerType.class
    );

    public VillagerVillageCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.VILLAGER_SPAWN_EGG,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillagerVillageCategory.asComponent(),
                MillIngredientTypes.VILLAGE,
                MillIngredientTypes.VILLAGER,
                recipe -> {
                    String name = MillenaireLocalizeHelper.getVillagerName(recipe).getString();
                    return MillenaireJeimmKeys.VillagerVillageCategoryTitle_S.asComponent(name);
                },
                MillenaireAPIHelper::getVillageFormVillager
        );
    }
}
