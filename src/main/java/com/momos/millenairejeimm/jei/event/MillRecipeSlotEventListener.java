package com.momos.millenairejeimm.jei.event;

import com.momos.millenairejeim.jei.crafting.event.MillRecipeSetSlotEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillagerType;

import java.util.List;

/**
 * [醒目新注释] 子模组配方槽位事件监听器。
 * 明确区分 JEI 原生成分轮播槽位 (Carousel Slots) 与自定义加号点击交互按钮 (Detail Button)。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillRecipeSlotEventListener {

    /* =========================================================================================
     * [醒目新注释] 坐标明确拆分：
     * 1. VILLAGER_ICON_X / Y: 村民轮播图标位置 (140, 72)，对齐“制作村民”文本行。
     * 2. BUILDING_ICON_X / Y: 关联建筑轮播图标位置 (140, 92)，对齐“关联建筑”文本行。
     * ========================================================================================= */
    private static final int VILLAGER_ICON_X = 140;
    private static final int VILLAGER_ICON_Y = 72;

    private static final int BUILDING_ICON_X = 140;
    private static final int BUILDING_ICON_Y = 92;

    /**
     * [醒目新注释] 响应配方槽位设置事件。
     * 向 JEI 注册轮播槽位，传入多元素列表后 JEI 会在该位置自动按时间轮播渲染图标。
     *
     * @param event 配方槽位设置事件 {@link MillRecipeSetSlotEvent}
     */
    @SubscribeEvent
    public static void onSetRecipeSlots(MillRecipeSetSlotEvent event) {
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null) return;

        /* =========================================================================================
         * [醒目新注释] 功能一：JEI 原生轮播 (Carousel) 与 R/U 快捷键查询
         * 传入列表后，JEI 会自动在 (140, 72) 处轮播显示各个村民。
         * 玩家鼠标悬停在轮播图标上按 R/U，可直接查询当前正在展示的那个村民。
         * ========================================================================================= */
        List<VillagerType> villagers = recipe.getVillagerTypes();
        if (villagers != null && !villagers.isEmpty()) {
            event.getBuilder().addSlot(RecipeIngredientRole.INPUT, VILLAGER_ICON_X, VILLAGER_ICON_Y)
                    .addIngredients(MillIngredientTypes.VILLAGER, villagers);
        }

        List<BuildingPlanSet> buildings = recipe.getAssociatedBuildingIds();
        if (buildings != null && !buildings.isEmpty()) {
            event.getBuilder().addSlot(RecipeIngredientRole.INPUT, BUILDING_ICON_X, BUILDING_ICON_Y)
                    .addIngredients(MillIngredientTypes.BUILDING, buildings);
        }
    }
}