package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.jei.Trade.MillTradeCategory;
import com.momos.millenairejeim.jei.Trade.MillTradeRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlan;
import org.millenaire.building.BuildingPlanSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * [醒目新注释] 后置子模组交易详情按钮事件订阅器。
 * 响应交易加号按钮的渲染与点击事件，构建多焦点列表唤起 JEI 界面[cite: 7]。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillTradeDetailButtonHandler {

    /**
     * [醒目新注释] 校验交易建筑加号按钮可用性与渲染状态。
     *
     * @param event 父模组发出的按钮渲染事件 {@link MillRecipeButtonRenderEvent}
     */
    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        Object rawRecipe = event.getRecipe();
        if (!(rawRecipe instanceof MillTradeRecipe recipe)) return;

        if (MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            Set<BuildingPlan> plans = MillenaireAPIHelper.getBuildingsByShopId(recipe.getCultureId(), recipe.getShopId());
            boolean hasBuildings = plans != null && !plans.isEmpty();
            event.setShouldRender(hasBuildings);
        }
    }

    /**
     * [醒目新注释] 处理交易建筑加号按钮点击，唤起 JEI 建筑要素查询[cite: 7]。
     *
     * @param event 父模组发出的按钮点击事件 {@link MillRecipeButtonClickEvent}
     */
    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        Object rawRecipe = event.getRecipe();
        if (!(rawRecipe instanceof MillTradeRecipe recipe) || MillenaireJeimmPlugin.JeiRuntime == null) return;

        if (MillTradeCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            Set<BuildingPlan> plans = MillenaireAPIHelper.getBuildingsByShopId(recipe.getCultureId(), recipe.getShopId());
            if (plans == null || plans.isEmpty()) return;

            IFocusFactory focusFactory = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory();
            List<IFocus<?>> focusList = new ArrayList<>();
            for (BuildingPlan plan : plans) {
                BuildingPlanSet planSet = MillenaireAPIHelper.getBuildingPlanSetByPlan(plan);
                if (planSet != null) {
                    focusList.add(focusFactory.createFocus(
                            RecipeIngredientRole.OUTPUT,
                            MillIngredientTypes.BUILDING,
                            planSet
                    ));
                }
            }

            if (!focusList.isEmpty()) {
                MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focusList);
            }
        }
    }
}