package com.momos.millenairejeimm.jei;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * JEI 开发者 Debug 模式通用 GUI 绘制工具类 {@link DebugGuiUtils}。
 * 提供暗色 Debug 终端面板、属性徽章（Badge）及动态悬浮 Inspector 的渲染支持。
 */
public class DebugGuiUtils {

    /**
     * 绘制暗色开发者 Debug 控制台面板底图。
     *
     * @param guiGraphics GUI 渲染上下文 {@link GuiGraphics}
     * @param x 相对 X 坐标
     * @param y 相对 Y 坐标
     * @param width 面板宽度
     * @param height 面板高度
     */
    public static void drawDebugConsoleBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // [醒目新注释] 绘制终端暗色背景 (0xFF1E1E1E) 与灰色边框 (0xFF3C3C3C)
        guiGraphics.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF3C3C3C);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF3C3C3C);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF3C3C3C);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF3C3C3C);
    }

    /**
     * 判断鼠标是否处于指定坐标区域内。
     *
     * @param mouseX 鼠标当前 X 坐标
     * @param mouseY 鼠标当前 Y 坐标
     * @param x 矩形起点 X
     * @param y 矩形起点 Y
     * @param width 矩形宽度
     * @param height 矩形高度
     * @return 若鼠标在矩形范围内则返回 true
     */
    public static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /**
     * 当鼠标悬停在 Debug 控制台面板上时，弹出全量字段结构的悬浮 Tooltip。
     *
     * @param font 字体渲染器 {@link Font}
     * @param guiGraphics GUI 上下文 {@link GuiGraphics}
     * @param debugLines 扩展字段文本列表
     * @param mouseX 鼠标当前 X 坐标
     * @param mouseY 鼠标当前 Y 坐标
     * @param panelX 控制台 X
     * @param panelY 控制台 Y
     * @param panelW 控制台宽度
     * @param panelH 控制台高度
     */
    public static void renderHoverInspector(Font font, GuiGraphics guiGraphics, List<Component> debugLines, double mouseX, double mouseY, int panelX, int panelY, int panelW, int panelH) {
        if (isMouseOver(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
            guiGraphics.renderComponentTooltip(font, debugLines, (int) mouseX, (int) mouseY);
        }
    }
}