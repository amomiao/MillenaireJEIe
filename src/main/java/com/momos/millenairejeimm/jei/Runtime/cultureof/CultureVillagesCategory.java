package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillPanoramicCategory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.Items;
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;

/**
 * 文化关联村庄全景大窗口分类 {@link CultureVillagesCategory}。
 */
public class CultureVillagesCategory extends BaseMillPanoramicCategory<Culture, VillageType> {

    public static final RecipeType<Culture> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.CultureVillagesCategoryPath,
            Culture.class
    );

    public CultureVillagesCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(
                guiHelper,
                Items.BOOK,
                RECIPE_TYPE,
                MillenaireJeimmKeys.CultureVillagesCategory.asComponent(),
                MillIngredientTypes.VILLAGE,
                MillIngredientTypes.CULTURE,
                recipe -> {
                    String name = recipe.displayName() != null ? recipe.displayName() : recipe.id().getPath();
                    return MillenaireJeimmKeys.CultureVillagesCategoryTitle_S.asComponent(name);
                },
                MillenaireAPIHelper::getVillageFormCulture
        );
    }
}
