package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.jei.crafting.MillCraftingCategory;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.culture.VillagerType;

import java.util.List;

/**
 * [醒目新注释] 村民详情加号按钮专职处理器。
 * 显式实现 {@link ILinkagePlusButtonHandler} 规定的所有抽象成员。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillVillagerDetailButtonHandler implements ILinkagePlusButtonHandler {

    private final IJeiRuntime jeiRuntime;

    public MillVillagerDetailButtonHandler(IJeiRuntime jeiRuntime) {
        this.jeiRuntime = jeiRuntime;
    }

    @Override
    public String getButtonId() {
        return MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL;
    }

    @Override
    @SubscribeEvent
    public void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        if (event != null && getButtonId().equals(event.getButtonId())) {
            IMillRecipe recipe = event.getRecipe();
            boolean hasVillagers = recipe != null && recipe.getVillagerTypes() != null && !recipe.getVillagerTypes().isEmpty();
            event.setShouldRender(hasVillagers);
        }
    }

    @Override
    @SubscribeEvent
    public void onButtonClick(MillRecipeButtonClickEvent event) {
        if (event != null && getButtonId().equals(event.getButtonId())) {
            onVillagerButtonClick(event);
        }
    }

    @Override
    public void onVillagerButtonClick(MillRecipeButtonClickEvent event) {
        if (jeiRuntime == null) return;
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null) return;

        List<VillagerType> villagers = recipe.getVillagerTypes();
        if (villagers != null && !villagers.isEmpty()) {
            jeiRuntime.getRecipesGui().show(
                    jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(
                            RecipeIngredientRole.OUTPUT,
                            MillIngredientTypes.VILLAGER,
                            villagers.get(0)
                    )
            );
        }
    }
}