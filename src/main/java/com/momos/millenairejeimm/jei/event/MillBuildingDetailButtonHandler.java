package com.momos.millenairejeimm.jei.event;

// [醒目新注释] 导入父模组交易与事件相关类
import com.momos.millenairejeim.jei.Trade.MillTradeCategory;
import com.momos.millenairejeim.jei.Trade.MillTradeRecipe;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import com.momos.millenairejeim.util.VillagerBuildingMapper;

// [醒目新注释] 导入子模组 JEI 插件与成分类型
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
 * 绑定 JEI 交易配方关联的 {@link BuildingPlanSet} 建筑轮播对象，实现渲染可用性校验与多建筑焦点跳转。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillBuildingDetailButtonHandler {

    /**
     * [醒目新注释] 校验建筑按钮可用性与渲染状态。
     *
     * @param event 父模组发出的按钮渲染事件 {@link MillRecipeButtonRenderEvent}
     */
    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        if (MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {

            /* =========================================================================================
             * [醒目新注释] 建筑轮播数据源联锁校验：
             * 1. 解析交易配方 {@link MillTradeRecipe} 对应的建筑列表（例如利用 {@link VillagerBuildingMapper} 查找）。
             * 2. 仅当关联的建筑列表非空 (count > 0) 时才允许置为 true。
             * 3. 无轮播对象时保持 false，不进行渲染。
             * ========================================================================================= */
            List<BuildingPlanSet> buildings = getAssociatedBuildings(event.getRecipe());
            boolean hasCarouselBuildings = buildings != null && !buildings.isEmpty();

            event.setShouldRender(hasCarouselBuildings);
        }
    }

    /**
     * [醒目新注释] 处理建筑按钮点击并唤起 JEI 多焦点查询。
     *
     * @param event 父模组发出的按钮点击事件 {@link MillRecipeButtonClickEvent}
     */
    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        if (MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {

            List<BuildingPlanSet> buildings = getAssociatedBuildings(event.getRecipe());

            /* =========================================================================================
             * [醒目新注释] 点击安全阻断与多焦点跳转：
             * 1. 若建筑列表为空或 {@link MillenaireJeimmPlugin#JeiRuntime} 未就绪则安全阻断。
             * 2. 将所有关联建筑打包为 List<IFocus<?>> 提交给 JEI 视图 {@link mezz.jei.api.runtime.IRecipesGui#show(List)}。
             * ========================================================================================= */
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
     * [醒目新注释] 辅助方法：从事件配方对象解析关联建筑列表。
     */
    private static List<BuildingPlanSet> getAssociatedBuildings(Object recipeObj) {
        if (recipeObj instanceof IMillRecipe millRecipe) {
            return millRecipe.getAssociatedBuildingIds();
        }
        return List.of();
    }
}