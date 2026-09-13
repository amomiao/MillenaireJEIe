package com.momos.millenairejeimm.jei.event.linkageslot;

import com.momos.millenairejeim.jei.crafting.event.MillRecipeSetSlotEvent;
import com.momos.millenairejeim.jei.crafting.type.base.IMillRecipe;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

/**
 * [醒目新注释] 配方 JEI 轮播槽位监听器接口 {@link ILinkageSlotEventListener}。
 * 纯契约接口，通过 {@link SlotBinding} 元组提取所有需要注册的槽位映射。
 */
public interface ILinkageSlotEventListener {

    /**
     * [醒目新注释] 获取当前配方对应的所有槽位元组集合 {@link SlotBinding}
     *
     * @param recipe 配方对象 {@link IMillRecipe}
     * @return 槽位元组集合
     */
    List<SlotBinding<?>> getSlotBindings(IMillRecipe recipe);

    /**
     * [醒目新注释] 响应配方槽位设置事件 {@link MillRecipeSetSlotEvent}
     */
    @SubscribeEvent
    void onSetRecipeSlots(MillRecipeSetSlotEvent event);
}