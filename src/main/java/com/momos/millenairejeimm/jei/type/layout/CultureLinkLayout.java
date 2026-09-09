package com.momos.millenairejeimm.jei.type.layout;

import com.momos.millenairejeimm.jei.type.JeiLinkBox;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

// region 文化布局容器类定义
/**
 * 文化组合联动布局容器 {@link CultureLinkLayout}。
 * 统一调度管理多个 {@link JeiLinkBox} 的垂直堆叠布局、槽位注册、渲染与输入响应注册。
 */
public class CultureLinkLayout {
    private final List<JeiLinkBox<?>> boxes = new ArrayList<>();
    private final int startX;
    private final int startY;
    private final int width;
    private final int spacing;

    public CultureLinkLayout(int startX, int startY, int width, int spacing) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.spacing = spacing;
    }

    /**
     * [醒目新注释] 链式添加一个子 {@link JeiLinkBox}。
     */
    public CultureLinkLayout addBox(JeiLinkBox<?> box) {
        this.boxes.add(box);
        return this;
    }

    /**
     * [醒目新注释] 为指定索引处的 {@link JeiLinkBox} 设置数据并在对应计算坐标下注册 JEI 槽位。
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
     * [醒目新注释] 自动向 JEI 注册所有包含子项的物理输入监听器。
     */
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Object recipeContext) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            box.registerInputHandler(builder, recipeContext, startX, currentY);
            currentY += box.getBoxHeight() + spacing;
            
        }
    }

    /**
     * [醒目新注释] 统一绘制整个布局包含的所有盒体背景与组件。
     */
    public void render(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            box.render(guiGraphics, font, startX, currentY, width, mouseX, mouseY);
            currentY += box.getBoxHeight() + spacing;
        }
    }

    private int calculateBoxY(int index) {
        int currentY = startY;
        for (int i = 0; i < index; i++) {
            currentY += boxes.get(i).getBoxHeight() + spacing;
        }
        return currentY;
    }
}
// endregion 文化布局容器类定义