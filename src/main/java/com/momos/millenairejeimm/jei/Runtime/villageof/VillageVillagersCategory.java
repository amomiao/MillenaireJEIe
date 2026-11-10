package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
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
 * 展示村庄类型 {@link VillageType} 包含的所有村民 {@link VillagerType} 的 JEI 全景大窗口分类。
 */
public class VillageVillagersCategory extends BaseMillPanoramicCategory<VillageType, VillagerType> {

    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.VillageVillagersCategoryPath,
            VillageType.class
    );

    public VillageVillagersCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.PLAYER_HEAD,
                RECIPE_TYPE,
                MillenaireJeimmKeys.VillageVillagersCategory.asComponent(),
                MillIngredientTypes.VILLAGER,
                MillIngredientTypes.VILLAGE,
                recipe -> {
                    String name = recipe.name() != null ? recipe.name() : recipe.id().getPath();
                    return MillenaireJeimmKeys.VillageVillagersCategoryTitle_S.asComponent(name);
                },
                MillenaireAPIHelper::getVillagerFormVillage
        );
    }
}
