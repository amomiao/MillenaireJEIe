package com.momos.millenairejeimm.jei.event;

import com.momos.millenairejeim.jei.Trade.MillTradeCategory;
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
import org.millenaire.building.BuildingPlanSet;

import java.util.List;

/**
 * [醒目新注释] 子模组建筑详情按钮事件订阅器。
 * 支持合成分类 {@link MillCraftingCategory} 与交易分类 {@link MillTradeCategory} 的建筑详情 (+) 按钮触发。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillBuildingDetailButtonHandler {

    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        // [醒目新注释] 兼容交易分类与合成分类的建筑按钮标识
        if (MillCraftingCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())
                || MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {

            List<BuildingPlanSet> buildings = getAssociatedBuildings(event.getRecipe());
            boolean hasCarouselBuildings = buildings != null && !buildings.isEmpty();

            event.setShouldRender(hasCarouselBuildings);
        }
    }

    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        // [醒目新注释] 响应来自交易界面或合成界面的建筑加号按钮点击
        if (MillCraftingCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())
                || MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {

            /* =========================================================================================
             * [醒目新注释] 安全解析与跳转：
             * 从泛化的 {@link MillRecipeButtonClickEvent#getRecipe()} 中解析关联建筑列表并唤起 JEI 焦点视图。
             * ========================================================================================= */
            List<BuildingPlanSet> buildings = getAssociatedBuildings(event.getRecipe());

            if (buildings == null || buildings.isEmpty() || MillenaireJeimmPlugin.JeiRuntime == null) {
                return;
            }

            IFocusFactory focusFactory = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory();

            List<IFocus<?>> focusList = buildings.stream()
                    .<IFocus<?>>map(building -> focusFactory.createFocus(
                            RecipeIngredientRole.OUTPUT,
                            MillIngredientTypes.BUILDING,
                            building
                    ))
                    .toList();

            MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focusList);
        }
    }

    /**
     * [醒目新注释] 辅助方法：多态解析配方对象关联的建筑列表。
     */
    private static List<BuildingPlanSet> getAssociatedBuildings(Object recipeObj) {
        if (recipeObj instanceof IMillRecipe millRecipe) {
            return millRecipe.getAssociatedBuildingIds();
        }
        // [醒目新注释] 若交易配方包含关联建筑，也可在此扩展支持：
        // if (recipeObj instanceof MillTradeRecipe tradeRecipe) { return tradeRecipe.getAssociatedBuildingIds(); }
        return List.of();
    }
}