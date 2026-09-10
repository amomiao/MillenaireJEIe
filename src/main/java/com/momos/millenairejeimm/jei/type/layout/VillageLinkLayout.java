package com.momos.millenairejeimm.jei.type.layout;

import com.momos.millenairejeimm.jei.type.JeiLinkBox;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

// region 村庄布局容器类定义
/**
 * 村庄组合联动布局容器 {@link VillageLinkLayout}。
 * 统一调度与管理村庄分类页面下多个 {@link JeiLinkBox}（如村庄包含建筑、村庄出现村民等）的垂直堆叠布局、槽位注册、渲染与输入响应注册。
 */
public class VillageLinkLayout {
    private final List<JeiLinkBox<?>> boxes = new ArrayList<>();
    private final int startX;
    private final int startY;
    private final int width;
    private final int spacing;

    /**
     * 构造村庄联动布局容器 {@link VillageLinkLayout}。
     *
     * @param startX  组件绘制与逻辑绑定的起始 X 轴像素坐标
     * @param startY  组件绘制与逻辑绑定的起始 Y 轴像素坐标
     * @param width   组件的总体渲染宽度
     * @param spacing 多个 {@link JeiLinkBox} 之间的垂直间距
     */
    public VillageLinkLayout(int startX, int startY, int width, int spacing) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.spacing = spacing;
    }

    /**
     * [醒目新注释] 链式添加一个属于村庄界面的子 {@link JeiLinkBox}。
     *
     * @param box 需要由村庄布局管理的 {@link JeiLinkBox} 实例
     * @return 当前 {@link VillageLinkLayout} 实例，便于链式调用
     */
    public VillageLinkLayout addBox(JeiLinkBox<?> box) {
        this.boxes.add(box);
        return this;
    }

    /**
     * [醒目新注释] 为指定索引处的 {@link JeiLinkBox} 设置数据并在对应计算坐标下注册 JEI 槽位。
     *
     * @param <T>       成分数据类型
     * @param boxIndex  子 LinkBox 的索引位置
     * @param builder   JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param targets   需要注册入槽位的成分数据列表
     */
    public <T> void setRecipeForBox(int boxIndex, IRecipeLayoutBuilder builder, List<T> targets) {
        if (boxIndex >= 0 && boxIndex < boxes.size()) {
            int currentY = calculateBoxY(boxIndex);
            @SuppressWarnings("unchecked")
            JeiLinkBox<T> box = (JeiLinkBox<T>) boxes.get(boxIndex);
            box.setRecipe(builder, targets, startX, currentY);
        }
    }

    /**
     * [醒目新注释] 自动向 JEI 注册所有包含子项的物理输入监听器（如点击 "+" 唤起全景大窗口）。
     *
     * @param builder        JEI 配方额外组件构建器 {@link IRecipeExtrasBuilder}
     * @param recipeContext  当前配方的上下文对象（如 {@link org.millenaire.culture.VillageType}）
     */
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Object recipeContext) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            box.registerInputHandler(builder, recipeContext, startX, currentY);
            currentY += box.getBoxHeight() + spacing;
        }
    }

    /**
     * [醒目新注释] 统一绘制村庄布局包含的所有盒体背景与子组件。
     *
     * @param guiGraphics 渲染图形上下文 {@link GuiGraphics}
     * @param font        字体渲染器 {@link Font}
     * @param mouseX      当前鼠标 X 坐标
     * @param mouseY      当前鼠标 Y 坐标
     */
    public void render(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            box.render(guiGraphics, font, startX, currentY, width, mouseX, mouseY);
            currentY += box.getBoxHeight() + spacing;
        }
    }

    /**
     * 根据索引动态计算指定 {@link JeiLinkBox} 的垂直起始 Y 坐标。
     *
     * @param index 子 LinkBox 的索引
     * @return 计算后的起始 Y 坐标
     */
    private int calculateBoxY(int index) {
        int currentY = startY;
        for (int i = 0; i < index; i++) {
            currentY += boxes.get(i).getBoxHeight() + spacing;
        }
        return currentY;
    }
}
// endregion 村庄布局容器类定义