package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.jei.crafting.MillCraftingCategory;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.building.BuildingPlanSet;

import java.util.List;

/**
 * [醒目新注释] 建筑详情加号按钮专职处理器 {@link MillBuildingDetailButtonHandler}。
 * 实现 {@link ILinkagePlusButtonHandler} 接口，负责响应关联建筑详情加号按钮的绘制及点击跳转事件。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillBuildingDetailButtonHandler implements ILinkagePlusButtonHandler {

    private final IJeiRuntime jeiRuntime;

    public MillBuildingDetailButtonHandler(IJeiRuntime jeiRuntime) {
        this.jeiRuntime = jeiRuntime;
    }

    @Override
    public String getButtonId() {
        return MillCraftingCategory.BUTTON_ID_BUILDING_DETAIL;
    }

    @Override
    @SubscribeEvent
    public void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        if (event != null && getButtonId().equals(event.getButtonId())) {
            IMillRecipe recipe = event.getRecipe();
            boolean hasBuildings = recipe != null && recipe.getAssociatedBuildingIds() != null && !recipe.getAssociatedBuildingIds().isEmpty();
            event.setShouldRender(hasBuildings);
        }
    }

    @Override
    @SubscribeEvent
    public void onButtonClick(MillRecipeButtonClickEvent event) {
        if (event != null && getButtonId().equals(event.getButtonId())) {
            onVillagerButtonClick(event);
        }
    }

    /**
     * [醒目新注释] 点击建筑加号按钮后，触发向 JEI 界面展示【关联建筑】的 Output 点击焦点的跳转逻辑。
     *
     * @param event 配方按钮点击事件 {@link MillRecipeButtonClickEvent}
     */
    @Override
    public void onVillagerButtonClick(MillRecipeButtonClickEvent event) {
        if (jeiRuntime == null) return;
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null) return;

        List<BuildingPlanSet> buildings = recipe.getAssociatedBuildingIds();
        if (buildings != null && !buildings.isEmpty()) {
            jeiRuntime.getRecipesGui().show(
                    jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(
                            RecipeIngredientRole.OUTPUT,
                            MillIngredientTypes.BUILDING,
                            buildings.get(0)
                    )
            );
        }
    }
}