package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.jei.crafting.MillCraftingCategory;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.jei.IMillRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillagerType;

import java.util.List;

/**
 * [醒目新注释] 子模组详情按钮事件订阅器。
 * 绑定 JEI 轮播数据源 {@link VillagerType} 与 {@link BuildingPlanSet}，动态控制按钮渲染可用性与多焦点跳转。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillVillagerDetailButtonHandler {
    /**
     * [醒目新注释] 校验按钮可用性与渲染状态（区分村民按钮与建筑按钮）。
     *
     * @param event 父模组发出的按钮渲染事件 {@link MillRecipeButtonRenderEvent}
     */
    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null) return;

        // 1. 村民详情按钮校验
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {
            List<VillagerType> carouselVillagers = recipe.getVillagerTypes();
            boolean hasCarouselObjects = carouselVillagers != null && !carouselVillagers.isEmpty();
            event.setShouldRender(hasCarouselObjects);
        }
        // 2. [醒目新注释] 建筑详情按钮校验
        else if (MillCraftingCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            List<BuildingPlanSet> carouselBuildings = recipe.getAssociatedBuildingIds();
            boolean hasCarouselObjects = carouselBuildings != null && !carouselBuildings.isEmpty();
            event.setShouldRender(hasCarouselObjects);
        }
    }

    /**
     * [醒目新注释] 处理按钮点击并唤起 JEI 多焦点查询（区分村民与建筑跳转）。
     *
     * @param event 父模组发出的按钮点击事件 {@link MillRecipeButtonClickEvent}
     */
    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null || MillenaireJeimmPlugin.JeiRuntime == null) return;
        IFocusFactory focusFactory = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory();
        // 1. 处理村民详情按钮点击：跳转至对应村民要素
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {
            List<VillagerType> villagers = recipe.getVillagerTypes();
            if (villagers == null || villagers.isEmpty()) return;
            List<IFocus<?>> focusList = villagers.stream()
                    .<IFocus<?>>map(villager -> focusFactory.createFocus(
                            RecipeIngredientRole.OUTPUT,
                            MillIngredientTypes.VILLAGER,
                            villager
                    ))
                    .toList();

            MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focusList);
        }
        // 2. [醒目新注释] 处理建筑详情按钮点击：跳转至对应建筑要素
        else if (MillCraftingCategory.BUTTON_ID_BUILDING_DETAIL.equals(event.getButtonId())) {
            List<BuildingPlanSet> buildings = recipe.getAssociatedBuildingIds();
            if (buildings == null || buildings.isEmpty()) return;

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
}