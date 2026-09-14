package com.momos.millenairejeimm.jei.event.linkageslot;

import com.momos.millenairejeim.event.MillRecipeSetSlotEvent;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.jei.Trade.MillTradeRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlan;
import org.millenaire.building.BuildingPlanSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * [醒目新注释] 后置子模组交易槽位事件监听器 {@link MillTradeRecipeSlotEventListener}。
 * 捕获交易 Category 发布的 {@link MillRecipeSetSlotEvent} 并挂载对应的建筑轮播 JEI 要素。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillTradeRecipeSlotEventListener {

    /* =========================================================================================
     * [醒目新注释] 交易建筑轮播图标坐标定义 (124, 11)：
     * 水平方向在加号按钮 (X=142) 左侧，垂直方向与 Y=15 建筑文本行同高对齐。
     * ========================================================================================= */
    private static final int BUILDING_ICON_X = 124;
    private static final int BUILDING_ICON_Y = 4;

    /**
     * [醒目新注释] 响应交易配方槽位设置事件。
     *
     * @param event 配方槽位设置事件 {@link MillRecipeSetSlotEvent}
     */
    @SubscribeEvent
    public static void onSetTradeRecipeSlots(MillRecipeSetSlotEvent event) {
        if (event == null) return;
        Object rawRecipe = event.getRecipe();
        if (!(rawRecipe instanceof MillTradeRecipe recipe)) return;

        Set<BuildingPlan> plans = MillenaireAPIHelper.getBuildingsByShopId(recipe.getCultureId(), recipe.getShopId());
        if (plans == null || plans.isEmpty()) return;

        List<BuildingPlanSet> buildingSets = new ArrayList<>();
        for (BuildingPlan plan : plans) {
            BuildingPlanSet planSet = MillenaireAPIHelper.getBuildingPlanSetByPlan(plan);
            if (planSet != null && !buildingSets.contains(planSet)) {
                buildingSets.add(planSet);
            }
        }

        if (!buildingSets.isEmpty()) {
            event.getBuilder().addSlot(RecipeIngredientRole.OUTPUT, BUILDING_ICON_X, BUILDING_ICON_Y)
                    .addIngredients(MillIngredientTypes.BUILDING, buildingSets);
        }
    }
}