package com.momos.millenairejeimm.jei.type;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

// region 抽象基类定义与通用尺寸配置
/**
 * 千年 JEI 详情分类通用抽象基类 {@link BaseMillCategory}。
 *
 * @param <T> JEI Recipe 数据类型
 */
public abstract class BaseMillCategory<T> implements IRecipeCategory<T> {

    public static final int PAGE_WIDTH = 172;
    public static final int PAGE_HEIGHT = 225;

    protected final IDrawable background;
    protected final IDrawable icon;

    public BaseMillCategory(IGuiHelper guiHelper, IDrawable icon) {
        this.background = guiHelper.createBlankDrawable(PAGE_WIDTH, PAGE_HEIGHT);
        this.icon = icon;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return PAGE_WIDTH;
    }

    @Override
    public int getHeight() {
        return PAGE_HEIGHT;
    }
// endregion 抽象基类定义与通用尺寸配置

// region 通用 UI 渲染与辅助组件
    /**
     * 绘制通用的卡片 Header。
     */
    protected void drawCommonHeader(GuiGraphics guiGraphics, Font font, String title, String idText, String categoryText, double mouseX, double mouseY) {
        // [醒目新注释] 1. 图标正下方绘制"细节"按钮 (X: 3, Y: 26)
        DebugGuiUtils.drawDetailsButton(guiGraphics, font, 3, 26, mouseX, mouseY);

        // [醒目新注释] 2. 右侧流畅排布：标题 (Y: 6) -> ID (Y: 17) -> 类别 (Y: 27)
        guiGraphics.drawString(font, Component.literal("§l" + title), 28, 6, 0x111111, false);
        guiGraphics.drawString(font, Component.literal("§8ID: " + idText), 28, 17, 0x555555, false);
        guiGraphics.drawString(font, Component.literal("§8类别: " + categoryText), 28, 27, 0x555555, false);

        // [醒目新注释] 3. 分割线向上微调至 Y: 38
        guiGraphics.fill(6, 38, PAGE_WIDTH - 6, 39, 0xFFCCCCCC);
    }

    protected void drawLabelValue(GuiGraphics guiGraphics, Font font, String label, String value, int x, int y) {
        guiGraphics.drawString(font, Component.literal("§8" + label + ": §0" + value), x, y, 0x333333, false);
    }

    protected void drawHoverableText(GuiGraphics guiGraphics, Font font, String label, String fullValueText, int x, int y, double mouseX, double mouseY) {
        String lineStr = "§8" + label + ": " + fullValueText;
        int textWidth = font.width(lineStr);

        if (textWidth > PAGE_WIDTH - x - 6) {
            String trimmed = font.plainSubstrByWidth(lineStr, PAGE_WIDTH - x - 14) + "...";
            guiGraphics.drawString(font, Component.literal(trimmed), x, y, 0x333333, false);
        } else {
            guiGraphics.drawString(font, Component.literal(lineStr), x, y, 0x333333, false);
        }

        if (mouseX >= x && mouseX <= PAGE_WIDTH - 6 && mouseY >= y && mouseY < y + 9) {
            guiGraphics.renderComponentTooltip(font, Collections.singletonList(Component.literal("§f" + label + ":\n" + fullValueText)), (int) mouseX, (int) mouseY);
        }
    }
// endregion 通用 UI 渲染与辅助组件

    // 在 BaseMillCategory 中提供可选的 Inspector 属性生成钩子
    /**为具体 Recipe 构造特定的 Inspector 行信息（若子类未重写则由 {@link DebugGuiUtils} 自动反射生成）。*/
    protected List<Component> getCustomInspectorLines(T recipe) {
        return List.of();
    }

    // region 交互逻辑统一处理
    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(T recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            // [醒目新注释] 统一处理图标正下方按钮坐标 (3, 26) 的点击响应
            if (DebugGuiUtils.handleDetailsButtonClick(recipe, mouseX, mouseY, 3, 26)) {
                return true;
            }
        }
        return IRecipeCategory.super.handleInput(recipe, mouseX, mouseY, input);
    }
// endregion 交互逻辑统一处理
}