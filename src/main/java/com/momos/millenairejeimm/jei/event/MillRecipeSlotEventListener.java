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
     * [醒目新注释] 坐标移动修正：
     * 1. VILLAGER_ICON_X / Y: 村民轮播图标移动至 (128, 70)，与右侧的加号按钮 (148, 73) 错开。
     * 2. BUILDING_ICON_X / Y: 关联建筑轮播图标移动至 (128, 88)，与右侧的加号按钮 (148, 91) 错开。
     * ========================================================================================= */
    private static final int VILLAGER_ICON_X = 128;
    private static final int VILLAGER_ICON_Y = 70;

    private static final int BUILDING_ICON_X = 128;
    private static final int BUILDING_ICON_Y = 88;

    @SubscribeEvent
    public static void onSetRecipeSlots(MillRecipeSetSlotEvent event) {
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null) return;

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