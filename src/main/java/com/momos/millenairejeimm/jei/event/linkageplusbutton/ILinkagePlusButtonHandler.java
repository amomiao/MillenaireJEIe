package com.momos.millenairejeimm.jei.event.linkageplusbutton;

import com.momos.millenairejeim.jei.crafting.MillCraftingCategory;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonClickEvent;
import com.momos.millenairejeim.jei.crafting.event.MillRecipeButtonRenderEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * [醒目新注释] 村民详情按钮处理器接口 {@link ILinkagePlusButtonHandler}。
 * 纯契约接口，抹除所有 default 实现体，强制实现类必须实现所有属性 getter 与事件响应成员。
 */
public interface ILinkagePlusButtonHandler {
    /**
     * [醒目新注释] 获取绑定的按钮 ID，默认匹配 {@link MillCraftingCategory#BUTTON_ID_VILLAGER_DETAIL}[cite: 7]
     * [醒目新注释] 强制实现类重写并提供具体的按钮 ID
     */
    String getButtonId();

    /**
     * [醒目新注释] 监听绘制询问事件[cite: 3]
     * [醒目新注释] 强制实现类重写具体的渲染判断逻辑
     */
    @SubscribeEvent
    void onCheckButtonRender(MillRecipeButtonRenderEvent event);

    /**
     * [醒目新注释] 监听按钮点击事件[cite: 1]
     * [醒目新注释] 强制实现类重写具体的点击分发逻辑
     */
    @SubscribeEvent
    void onButtonClick(MillRecipeButtonClickEvent event);

    /**
     * [醒目新注释] 点击后的具体跳转响应行为
     * [醒目新注释] 强制实现类重写具体的 JEI GUI 跳转实现
     */
    void onVillagerButtonClick(MillRecipeButtonClickEvent event);
}