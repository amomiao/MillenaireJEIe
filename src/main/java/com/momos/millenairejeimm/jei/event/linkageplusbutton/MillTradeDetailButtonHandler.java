package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.jei.Trade.MillTradeCategory;
import com.momos.millenairejeim.jei.Trade.MillTradeRecipe;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.JeiNavigationHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlanSet;

import java.util.List;

/**
 * 后置子模组交易详情按钮事件订阅器。
 * 响应交易加号按钮的渲染与点击事件，通过现代 JEI 定向导航契约 {@link JeiNavigationHelper}
 * 确定性直达建筑详情主分类 {@link BuildingCategory}。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillTradeDetailButtonHandler {

    /**
     * 校验交易建筑加号按钮可用性与渲染状态。
     *
     * @param event 父模组发出的按钮渲染事件 {@link MillRecipeButtonRenderEvent}
     */
    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        Object rawRecipe = event.getRecipe();
        if (!(rawRecipe instanceof MillTradeRecipe recipe)) return;

        if (MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            List<BuildingPlanSet> planSets = MillenaireAPIHelper.getBuildingPlanSetsByShopId(recipe.getCultureId(), recipe.getShopId());
            boolean hasBuildings = planSets != null && !planSets.isEmpty();
            event.setShouldRender(hasBuildings);
        }
    }

    /**
     * 处理交易建筑加号按钮点击，精准定向唤起 JEI 建筑详情分类。
     *
     * @param event 父模组发出的按钮点击事件 {@link MillRecipeButtonClickEvent}
     */
    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        Object rawRecipe = event.getRecipe();
        if (!(rawRecipe instanceof MillTradeRecipe recipe) || MillenaireJeimmPlugin.JeiRuntime == null) return;

        if (MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            List<BuildingPlanSet> planSets = MillenaireAPIHelper.getBuildingPlanSetsByShopId(recipe.getCultureId(), recipe.getShopId());
            if (planSets == null || planSets.isEmpty()) return;

            JeiNavigationHelper.navigateToRecipes(BuildingCategory.RECIPE_TYPE, planSets, List.of());
        }
    }
}
