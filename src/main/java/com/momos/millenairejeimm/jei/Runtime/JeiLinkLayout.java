package com.momos.millenairejeimm.jei.Runtime;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

// region 通用布局容器类定义
/**
 * 通用 JEI 联动槽位垂直布局容器 {@link JeiLinkLayout}。
 * <p>
 * 统一调度与管理多个 {@link JeiLinkBox} 的动态高度计算、垂直堆叠布局、槽位注册、渲染与输入响应拦截。
 * 支持空盒体自动隐藏与高度紧凑吸附排版。
 */
public class JeiLinkLayout {
    private final List<JeiLinkBox<?>> boxes = new ArrayList<>();
    private final int startX;
    private final int startY;
    private final int width;
    private final int spacing;

    // [醒目新注释] 标记当 JeiLinkBox 内部数据列表长度为 0 时是否自动隐藏并跳过渲染与布局排版（默认为 true）
    private boolean hideEmptyBoxes = true;

    /**
     * 构造通用 JEI 联动布局容器 {@link JeiLinkLayout}。
     *
     * @param startX  组件绘制与逻辑绑定的起始 X 轴像素坐标
     * @param startY  组件绘制与逻辑绑定的起始 Y 轴像素坐标
     * @param width   组件的总体渲染宽度
     * @param spacing 多个 {@link JeiLinkBox} 之间的垂直间距
     */
    public JeiLinkLayout(int startX, int startY, int width, int spacing) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.spacing = spacing;
    }

    /**
     * [醒目新注释] 设置当子 {@link JeiLinkBox} 数据长度为 0 时是否自动隐藏且不参与渲染排版。
     *
     * @param hide true 表示长度为 0 时自动隐藏并折叠排版（默认值），false 表示强制绘制空盒
     * @return 当前 {@link JeiLinkLayout} 实例，便于链式调用
     */
    public JeiLinkLayout setHideEmptyBoxes(boolean hide) {
        this.hideEmptyBoxes = hide;
        return this;
    }

    /**
     * [醒目新注释] 获取当前是否启用了空盒自动隐藏设定。
     *
     * @return 若启用自动隐藏则返回 true，否则返回 false
     */
    public boolean isHideEmptyBoxes() {
        return hideEmptyBoxes;
    }

    /**
     * 链式添加一个子 {@link JeiLinkBox}。
     *
     * @param box 需要由当前布局管理的 {@link JeiLinkBox} 实例
     * @return 当前 {@link JeiLinkLayout} 实例，便于链式调用
     */
    public JeiLinkLayout addBox(JeiLinkBox<?> box) {
        this.boxes.add(box);
        return this;
    }

    /**
     * [醒目新注释] 为指定索引处的 {@link JeiLinkBox} 设置数据并在对应计算坐标下注册 JEI 槽位。
     * 会根据前面盒体的有效性动态调整 Y 轴相对偏移量。
     *
     * @param <T>       成分数据类型
     * @param boxIndex  子 LinkBox 的索引位置
     * @param builder   JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param targets   需要注册入槽位的成分数据列表
     */
    public <T> void setRecipeForBox(int boxIndex, IRecipeLayoutBuilder builder, List<T> targets) {
        if (boxIndex >= 0 && boxIndex < boxes.size()) {
            @SuppressWarnings("unchecked")
            JeiLinkBox<T> box = (JeiLinkBox<T>) boxes.get(boxIndex);

            // 先按动态计算的 Y 坐标设置配方（如果 targets 为空，JeiLinkBox 内部会自动跳过槽位添加）
            int currentY = calculateBoxY(boxIndex);
            box.setRecipe(builder, targets, startX, currentY);
        }
    }

    /**
     * [醒目新注释] 自动向 JEI 注册所有有效子项的物理输入监听器。
     * 若启用了 {@link #hideEmptyBoxes} 且盒体长度为 0，则跳过输入拦截注册。
     *
     * @param builder        JEI 配方额外组件构建器 {@link IRecipeExtrasBuilder}
     * @param recipeContext  当前配方的上下文对象
     */
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Object recipeContext) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            // [醒目新注释] 判定空盒隐藏逻辑，自动跳过无数据的盒体
            if (shouldSkipBox(box)) {
                continue;
            }
            box.registerInputHandler(builder, recipeContext, startX, currentY);
            currentY += box.getBoxHeight() + spacing;
        }
    }

    /**
     * [醒目新注释] 统一绘制整个布局包含的所有有效盒体背景与组件。
     * 若启用了 {@link #hideEmptyBoxes} 且盒体长度为 0，则跳过渲染并吸附紧凑后续元素。
     *
     * @param guiGraphics 渲染图形上下文 {@link GuiGraphics}
     * @param font        字体渲染器 {@link Font}
     * @param mouseX      当前鼠标 X 坐标
     * @param mouseY      当前鼠标 Y 坐标
     */
    public void render(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            // [醒目新注释] 判定空盒隐藏逻辑，跳过长度为 0 的盒体渲染
            if (shouldSkipBox(box)) {
                continue;
            }
            box.render(guiGraphics, font, startX, currentY, width, mouseX, mouseY);
            currentY += box.getBoxHeight() + spacing;
        }
    }

    /**
     * [醒目新注释] 获取布局当前占用的实际物理高度（含间距）。
     * 自动剔除已被隐藏的空盒高度。
     *
     * @return 总体占用的高度像素值
     */
    public int getTotalHeight() {
        int totalHeight = 0;
        int visibleCount = 0;
        for (JeiLinkBox<?> box : boxes) {
            if (shouldSkipBox(box)) {
                continue;
            }
            totalHeight += box.getBoxHeight() + spacing;
            visibleCount++;
        }
        return visibleCount > 0 ? totalHeight - spacing : 0;
    }

    /**
     * [醒目新注释] 根据索引动态计算指定 {@link JeiLinkBox} 的垂直起始 Y 坐标。
     * 会自动跳过前面已被隐藏的空盒体的高度与间距。
     *
     * @param index 子 LinkBox 的索引
     * @return 计算后的起始 Y 坐标
     */
    private int calculateBoxY(int index) {
        int currentY = startY;
        for (int i = 0; i < index; i++) {
            JeiLinkBox<?> box = boxes.get(i);
            if (shouldSkipBox(box)) {
                continue;
            }
            currentY += box.getBoxHeight() + spacing;
        }
        return currentY;
    }

    /**
     * [醒目新注释] 内部辅助方法：判断当前盒体是否应当在渲染/布局中被忽略。
     *
     * @param box 待判定的 {@link JeiLinkBox} 实例
     * @return 若启用了隐藏空盒且盒体数据长度为 0 则返回 true，否则返回 false
     */
    private boolean shouldSkipBox(JeiLinkBox<?> box) {
        return this.hideEmptyBoxes && box.getSize() == 0;
    }
}
// endregion 通用布局容器类定义