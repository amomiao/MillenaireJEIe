package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.jei.crafting.MillCraftingCategory;
import com.momos.millenairejeim.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillCraftingRecipe;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.JeiNavigationHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import com.momos.millenairejeimm.jei.Runtime.villagerof.VillagerCategory;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillagerType;

import java.util.List;

/**
 * 子模组详情按钮事件订阅器。
 * 绑定 JEI 轮播数据源 {@link VillagerType} 与 {@link BuildingPlanSet}，动态控制按钮渲染可用性，
 * 并基于现代 JEI 定向导航契约 {@link JeiNavigationHelper} 实现 100% 确定性的主分类跳转。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillVillagerDetailButtonHandler {
    /**
     * 校验按钮可用性与渲染状态（区分村民按钮与建筑按钮）。
     *
     * @param event 父模组发出的按钮渲染事件 {@link MillRecipeButtonRenderEvent}
     */
    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        Object rawRecipe = event.getRecipe();
        if (!(rawRecipe instanceof IMillCraftingRecipe recipe)) return;

        // 1. 村民详情按钮校验
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {
            List<VillagerType> carouselVillagers = recipe.getVillagerTypes();
            boolean hasCarouselObjects = carouselVillagers != null && !carouselVillagers.isEmpty();
            event.setShouldRender(hasCarouselObjects);
        }
        // 2. 建筑详情按钮校验
        else if (MillCraftingCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            List<BuildingPlanSet> carouselBuildings = recipe.getAssociatedBuildingIds();
            boolean hasCarouselObjects = carouselBuildings != null && !carouselBuildings.isEmpty();
            event.setShouldRender(hasCarouselObjects);
        }
    }

    /**
     * 处理按钮点击并精准定向唤起 JEI 主详情页（区分村民与建筑跳转）。
     *
     * @param event 父模组发出的按钮点击事件 {@link MillRecipeButtonClickEvent}
     */
    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        Object rawRecipe = event.getRecipe();
        if (!(rawRecipe instanceof IMillCraftingRecipe recipe) || MillenaireJeimmPlugin.JeiRuntime == null) return;

        // 1. 处理村民详情按钮点击：精准定向跳转至村民详情主分类
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {
            List<VillagerType> villagers = recipe.getVillagerTypes();
            if (villagers == null || villagers.isEmpty()) return;

            JeiNavigationHelper.navigateToRecipes(VillagerCategory.RECIPE_TYPE, villagers, List.of());
        }
        // 2. 处理建筑详情按钮点击：精准定向跳转至建筑详情主分类
        else if (MillCraftingCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            List<BuildingPlanSet> buildings = recipe.getAssociatedBuildingIds();
            if (buildings == null || buildings.isEmpty()) return;

            JeiNavigationHelper.navigateToRecipes(BuildingCategory.RECIPE_TYPE, buildings, List.of());
        }
    }
}
