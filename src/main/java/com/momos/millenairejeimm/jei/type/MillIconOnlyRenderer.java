package com.momos.millenairejeimm.jei.type;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 专用于 JEI 分类详情页槽位的“仅图标”成分渲染器 {@link IIngredientRenderer}。
 * 隔离 JEI 全局列表中的“图标+文字”绘制逻辑，避免在页面槽位中产生文本重叠遮挡。
 *
 * @param <T> 自定义成分类型
 */
public class MillIconOnlyRenderer<T> implements IIngredientRenderer<T> {

    private final Function<T, ItemStack> iconExtractor;

    /**
     * 构建仅图标渲染器。
     *
     * @param iconExtractor 从成分对象提取渲染用 {@link ItemStack} 的函数
     */
    public MillIconOnlyRenderer(Function<T, ItemStack> iconExtractor) {
        this.iconExtractor = iconExtractor;
    }

    @Override
    public void render(GuiGraphics guiGraphics, T ingredient) {
        if (ingredient == null) return;
        ItemStack stack = iconExtractor.apply(ingredient);
        if (!stack.isEmpty()) {
            guiGraphics.renderFakeItem(stack, 0, 0);
        }
    }

    @Override
    public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
        return Collections.emptyList();
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }
}