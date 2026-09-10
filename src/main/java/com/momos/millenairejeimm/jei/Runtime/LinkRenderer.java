package com.momos.millenairejeimm.jei.Runtime;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 全景分类路由 Link Payload 的哑元 JEI 渲染器 {@link IIngredientRenderer}。
 */
public class LinkRenderer<T> implements IIngredientRenderer<T> {
    // 路由专属隐形 Ingredient，不进行 GUI 绘制
    @Override
    public void render(GuiGraphics guiGraphics, T ingredient) {}
    @Override
    public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {return List.of();}
}