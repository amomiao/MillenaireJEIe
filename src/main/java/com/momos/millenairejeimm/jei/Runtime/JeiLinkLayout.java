// region 布局容器数据同步
package com.momos.millenairejeimm.jei.Runtime;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用 JEI 联动槽位垂直布局容器 {@link JeiLinkLayout}。
 */
public class JeiLinkLayout {
    private final List<JeiLinkBox<?>> boxes = new ArrayList<>();
    private final int startX;
    private final int startY;
    private final int width;
    private final int spacing;

    private boolean hideEmptyBoxes = true;

    public JeiLinkLayout(int startX, int startY, int width, int spacing) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.spacing = spacing;
    }

    public void Clear() {
        for (var box : boxes){
            box.Clear();
        }
    }

    // [醒目新注释] 新增槽位数据同步更新接口，用于不触发槽位重建的情况下更新指定 Box 的数据列表
    /**
     * 仅更新指定索引 {@link JeiLinkBox} 的成分数据列表，不触发 JEI 槽位重新构建。
     *
     * @param <T>      成分数据类型
     * @param boxIndex 子 LinkBox 的索引位置
     * @param targets  需要同步更新的成分数据列表
     */
    public <T> void setBoxData(int boxIndex, List<T> targets) {
        if (boxIndex >= 0 && boxIndex < boxes.size()) {
            @SuppressWarnings("unchecked")
            JeiLinkBox<T> box = (JeiLinkBox<T>) boxes.get(boxIndex);
            box.setTargets(targets);
        }
    }

    public JeiLinkLayout setHideEmptyBoxes(boolean hide) {
        this.hideEmptyBoxes = hide;
        return this;
    }

    public boolean isHideEmptyBoxes() {
        return hideEmptyBoxes;
    }

    public JeiLinkLayout addBox(JeiLinkBox<?> box) {
        this.boxes.add(box);
        return this;
    }

    public <T> void setRecipeForBox(int boxIndex, IRecipeLayoutBuilder builder, List<T> targets) {
        if (boxIndex >= 0 && boxIndex < boxes.size()) {
            @SuppressWarnings("unchecked")
            JeiLinkBox<T> box = (JeiLinkBox<T>) boxes.get(boxIndex);

            int currentY = calculateBoxY(boxIndex);
            box.setRecipe(builder, targets, startX, currentY);
        }
    }

    public void createRecipeExtras(IRecipeExtrasBuilder builder, Object recipeContext) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            if (shouldSkipBox(box)) {
                continue;
            }
            box.registerInputHandler(builder, recipeContext, startX, currentY);
            currentY += box.getBoxHeight() + spacing;
        }
    }

    public void render(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY) {
        int currentY = startY;
        for (JeiLinkBox<?> box : boxes) {
            if (shouldSkipBox(box)) {
                continue;
            }
            box.render(guiGraphics, font, startX, currentY, width, mouseX, mouseY);
            currentY += box.getBoxHeight() + spacing;
        }
    }

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

    private boolean shouldSkipBox(JeiLinkBox<?> box) {
        return this.hideEmptyBoxes && box.getSize() == 0;
    }
}
// endregion 布局容器数据同步