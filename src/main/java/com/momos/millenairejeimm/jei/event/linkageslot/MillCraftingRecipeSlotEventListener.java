package com.momos.millenairejeimm.jei.event.linkageslot;

import com.momos.millenairejeim.jei.crafting.event.MillRecipeSetSlotEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * [醒目新注释] 子模组配方槽位事件监听器 {@link MillCraftingRecipeSlotEventListener}。
 * 明确区分 JEI 原生成分轮播槽位 (Carousel Slots) 与自定义加号点击交互按钮 (Detail Button)。
 */
@EventBusSubscriber(modid = "millenairejeimm")
public class MillCraftingRecipeSlotEventListener {

    /* =========================================================================================
     * [醒目新注释] 坐标排布调整：
     * 1. VILLAGER_ICON_X / Y: 村民轮播图标位置 (124, 72)，对齐“制作村民”文本行，留出 X=142 处的加号按钮空间。
     * 2. BUILDING_ICON_X / Y: 关联建筑轮播图标位置 (124, 92)，对齐“关联建筑”文本行，留出 X=142 处的加号按钮空间。
     * ========================================================================================= */
    private static final int VILLAGER_ICON_X = 124;
    private static final int VILLAGER_ICON_Y = 72;

    private static final int BUILDING_ICON_X = 124;
    private static final int BUILDING_ICON_Y = 92;

    /**
     * [醒目新注释] 获取当前配方对应的所有槽位元组集合 {@link SlotBinding}
     *
     * @param recipe 配方对象 {@link IMillRecipe}
     * @return 槽位元组集合
     */
    public static List<SlotBinding<?>> getSlotBindings(IMillRecipe recipe) {
        List<SlotBinding<?>> bindings = new ArrayList<>();
        if (recipe == null) return bindings;

        if (recipe.getVillagerTypes() != null && !recipe.getVillagerTypes().isEmpty()) {
            bindings.add(new SlotBinding<>(VILLAGER_ICON_X, VILLAGER_ICON_Y, MillIngredientTypes.VILLAGER, recipe.getVillagerTypes()));
        }

        if (recipe.getAssociatedBuildingIds() != null && !recipe.getAssociatedBuildingIds().isEmpty()) {
            bindings.add(new SlotBinding<>(BUILDING_ICON_X, BUILDING_ICON_Y, MillIngredientTypes.BUILDING, recipe.getAssociatedBuildingIds()));
        }

        return bindings;
    }

    /**
     * [醒目新注释] 响应配方槽位设置事件。
     * 方法添加 static 修饰符，以满足 {@link EventBusSubscriber} 的自动注册要求。
     *
     * @param event 配方槽位设置事件 {@link MillRecipeSetSlotEvent}
     */
    @SubscribeEvent
    public static void onSetRecipeSlots(MillRecipeSetSlotEvent event) {
        if (event == null) return;
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null) return;

        for (SlotBinding<?> binding : getSlotBindings(recipe)) {
            if (binding.isValid()) {
                registerSlot(event, binding);
            }
        }
    }

    /**
     * [醒目新注释] 将单一槽位配置元组 {@link SlotBinding} 注册至 JEI 事件构建器 {@link MillRecipeSetSlotEvent} 中。
     *
     * @param event 事件对象 {@link MillRecipeSetSlotEvent}
     * @param binding 槽位绑定数据 {@link SlotBinding}
     * @param <T> 成分类型
     */
    private static <T> void registerSlot(MillRecipeSetSlotEvent event, SlotBinding<T> binding) {
        event.getBuilder().addSlot(RecipeIngredientRole.INPUT, binding.x(), binding.y())
                .addIngredients(binding.ingredientType(), binding.ingredients());
    }
}