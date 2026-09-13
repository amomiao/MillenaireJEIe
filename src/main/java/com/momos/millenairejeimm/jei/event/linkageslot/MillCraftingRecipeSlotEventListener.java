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
public class MillCraftingRecipeSlotEventListener implements ILinkageSlotEventListener {

    /* =========================================================================================
     * [醒目新注释] 坐标排布调整：
     * 1. VILLAGER_ICON_X / Y: 村民轮播图标位置 (124, 72)，对齐“制作村民”文本行，留出 X=142 处的加号按钮空间。
     * 2. BUILDING_ICON_X / Y: 关联建筑轮播图标位置 (124, 92)，对齐“关联建筑”文本行，留出 X=142 处的加号按钮空间。
     * ========================================================================================= */
    private static final int VILLAGER_ICON_X = 124;
    private static final int VILLAGER_ICON_Y = 72;

    private static final int BUILDING_ICON_X = 124;
    private static final int BUILDING_ICON_Y = 92;

    @Override
    public List<SlotBinding<?>> getSlotBindings(IMillRecipe recipe) {
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

    @Override
    @SubscribeEvent
    public void onSetRecipeSlots(MillRecipeSetSlotEvent event) {
        if (event == null) return;
        IMillRecipe recipe = event.getRecipe();
        if (recipe == null) return;

        for (SlotBinding<?> binding : getSlotBindings(recipe)) {
            if (binding.isValid()) {
                registerSlot(event, binding);
            }
        }
    }

    private <T> void registerSlot(MillRecipeSetSlotEvent event, SlotBinding<T> binding) {
        event.getBuilder().addSlot(RecipeIngredientRole.INPUT, binding.x(), binding.y())
                .addIngredients(binding.ingredientType(), binding.ingredients());
    }
}