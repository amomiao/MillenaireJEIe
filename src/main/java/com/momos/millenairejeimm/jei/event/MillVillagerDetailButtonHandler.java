package com.momos.millenairejeimm.jei.event;

import com.momos.millenairejeim.jei.crafting.MillCraftingCategory;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.culture.VillagerType;

import java.util.List;

/**
 * [醒目新注释] 子模组村民详情按钮事件订阅器。
 * 绑定 JEI 轮播数据源 {@link VillagerType}，动态控制按钮渲染可用性与多焦点跳转。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillVillagerDetailButtonHandler {

    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {
            IMillRecipe recipe = event.getRecipe();

            List<VillagerType> carouselVillagers = (recipe != null) ? recipe.getVillagerTypes() : null;
            boolean hasCarouselObjects = carouselVillagers != null && !carouselVillagers.isEmpty();

            event.setShouldRender(hasCarouselObjects);
        }
    }

    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {

            /* =========================================================================================
             * [醒目新注释] 类型断言转换：
             * 事件内部封装的配方已泛化为 {@link Object}，使用 {@link IMillRecipe} 进行模式匹配提取。
             * ========================================================================================= */
            if (!(event.getRecipe() instanceof IMillRecipe recipe)) {
                return;
            }

            List<VillagerType> villagers = recipe.getVillagerTypes();
            if (villagers == null || villagers.isEmpty() || MillenaireJeimmPlugin.JeiRuntime == null) {
                return;
            }

            IFocusFactory focusFactory = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory();

            List<IFocus<?>> focusList = villagers.stream()
                    .<IFocus<?>>map(villager -> focusFactory.createFocus(
                            RecipeIngredientRole.OUTPUT,
                            MillIngredientTypes.VILLAGER,
                            villager
                    ))
                    .toList();

            MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focusList);
        }
    }
}