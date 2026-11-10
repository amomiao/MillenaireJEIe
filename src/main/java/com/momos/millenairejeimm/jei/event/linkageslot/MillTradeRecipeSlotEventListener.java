package com.momos.millenairejeimm.jei.event.linkageslot;

import com.momos.millenairejeim.event.MillRecipeSetSlotEvent;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.jei.Trade.MillTradeRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlanSet;

import java.util.List;

/**
 * [醒目新注释] 后置子模组交易槽位事件监听器 {@link MillTradeRecipeSlotEventListener}。
 * 捕获交易 Category 发布的 {@link MillRecipeSetSlotEvent} 并挂载对应的建筑轮播 JEI 要素。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillTradeRecipeSlotEventListener {

    /* =========================================================================================
     * [醒目新注释] 交易建筑轮播图标坐标定义 (124, 43)：
     * 位于页面右下角，水平方向在加号按钮 (X=142, Y=45) 左侧，彻底避免与右上角 [次要] 标签冲突重叠。
     * ========================================================================================= */
    private static final int BUILDING_ICON_X = 124;
    private static final int BUILDING_ICON_Y = 43;

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

        List<BuildingPlanSet> buildingSets = MillenaireAPIHelper.getBuildingPlanSetsByShopId(recipe.getCultureId(), recipe.getShopId());
        if (buildingSets == null || buildingSets.isEmpty()) return;

        event.getBuilder().addSlot(RecipeIngredientRole.OUTPUT, BUILDING_ICON_X, BUILDING_ICON_Y)
                .addIngredients(MillIngredientTypes.BUILDING, buildingSets);
    }
}
