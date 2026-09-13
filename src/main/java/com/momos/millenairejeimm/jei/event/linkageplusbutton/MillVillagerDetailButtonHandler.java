package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.jei.crafting.MillCraftingCategory;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonRenderEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.millenaire.culture.VillagerType;

import java.util.List;

/**
 * [醒目新注释] 子模组村民详情按钮事件订阅器。
 * 绑定 JEI 轮播数据源 {@link VillagerType}，动态控制按钮渲染可用性与多焦点跳转。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillVillagerDetailButtonHandler {
    /**
     * [醒目新注释] 校验按钮可用性与渲染状态。
     *
     * @param event 父模组发出的按钮渲染事件 {@link MillRecipeButtonRenderEvent}
     */
    @SubscribeEvent
    public static void onCheckButtonRender(MillRecipeButtonRenderEvent event) {
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {
            IMillRecipe recipe = event.getRecipe();

            /* =========================================================================================
             * [醒目新注释] 轮播数据源联锁校验：
             * 1. 严格获取该配方提供给 JEI 轮播槽位的村民列表 {@link IMillRecipe#getVillagerTypes()}。
             * 2. 仅当配方不为空且关联的轮播村民数量 > 0 时，才允许将渲染状态置为 true。
             * 3. 无轮播对象时保持 false，不进行渲染。
             * ========================================================================================= */
            List<VillagerType> carouselVillagers = (recipe != null) ? recipe.getVillagerTypes() : null;
            boolean hasCarouselObjects = carouselVillagers != null && !carouselVillagers.isEmpty();

            event.setShouldRender(hasCarouselObjects);
        }
    }

    /**
     * [醒目新注释] 处理按钮点击并唤起 JEI 多焦点查询。
     *
     * @param event 父模组发出的按钮点击事件 {@link MillRecipeButtonClickEvent}
     */
    @SubscribeEvent
    public static void onButtonClick(MillRecipeButtonClickEvent event) {
        if (MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())) {
            IMillRecipe recipe = event.getRecipe();
            if (recipe == null) return;

            /* =========================================================================================
             * [醒目新注释] 点击安全阻断校验：
             * 直接基于轮播数据源 {@link IMillRecipe#getVillagerTypes()} 进行二次防护，
             * 若没有轮播对象或 JEI 运行时 {@link MillenaireJeimmPlugin#JeiRuntime} 未就绪则直接拦截。
             * ========================================================================================= */
            List<VillagerType> villagers = recipe.getVillagerTypes();
            if (villagers == null || villagers.isEmpty() || MillenaireJeimmPlugin.JeiRuntime == null) {
                return;
            }
            IFocusFactory focusFactory = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory();
            /* =========================================================================================
             * [醒目新注释] 生成多焦点列表：
             * 将轮播数据源中的全部 {@link VillagerType} 映射为 {@link IFocus} 并提交至 {@link mezz.jei.api.runtime.IRecipesGui#show(List)}。
             * ========================================================================================= */
            List<IFocus<?>> focusList = villagers.stream()
                    .<IFocus<?>>map(villager -> focusFactory.createFocus(
                            RecipeIngredientRole.OUTPUT,
                            MillIngredientTypes.VILLAGER,
                            villager
                    ))
                    .toList();

            MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focusList);
        }
    }
}