// region JeiLinkBox Implementation
package com.momos.millenairejeimm.jei.Runtime;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 千年 JEI 通用联动槽位容器组件 {@link JeiLinkBox}。
 *
 * @param <T> 槽位绑定的成分数据类型
 */
public class JeiLinkBox<T> {
    private static final int SLOT_SIZE = 18;
    private static final int PREVIEW_SLOTS = 8;

    private static final String TEXT_COUNT_ALL_FMT = " §aAll(%d)";
    private static final String TEXT_COUNT_NONE = " §7(None)";
    private static final String TEXT_TITLE_FMT = "§e%s%s";
    private static final String TEXT_PLUS_ICON = "§a...";
    private static final String TOOLTIP_ALL_FMT = "§aAll(%d)";

    // [醒目新注释] 改用 Supplier<Component> 动态提供标题，支持游戏中切换语言时实时刷新标题文本
    private final Supplier<Component> titleSupplier;
    private final int layoutMode;
    private List<T> targetList = new ArrayList<>();

    private IIngredientType<T> boundIngredientType;
    private IIngredientRenderer<T> boundRenderer;
    private OnPlusClickListener<T> customClickListener;

    @FunctionalInterface
    public interface OnPlusClickListener<T> {
        void onClick(Object contextObject, List<T> items);
    }

    // [醒目新注释] 重载构造方法，支持直接传入 Component 与 Supplier<Component> 以实现动态语言刷新
    public JeiLinkBox(Supplier<Component> titleSupplier) {
        this(titleSupplier, 0);
    }

    public JeiLinkBox(Component titleComponent) {
        this(() -> titleComponent, 0);
    }

    public JeiLinkBox(String title) {
        this(() -> Component.literal(title), 0);
    }

    public JeiLinkBox(Supplier<Component> titleSupplier, int layoutMode) {
        this.titleSupplier = titleSupplier != null ? titleSupplier : () -> Component.empty();
        this.layoutMode = layoutMode;
    }

    public JeiLinkBox(Component titleComponent, int layoutMode) {
        this(() -> titleComponent, layoutMode);
    }

    public JeiLinkBox(String title, int layoutMode) {
        this(() -> Component.literal(title), layoutMode);
    }

    @Deprecated(since = "1.0", forRemoval = true)
    public JeiLinkBox<T> setOnPlusClick(OnPlusClickListener<T> listener) {
        this.customClickListener = listener;
        return this;
    }

    public void Clear() { this.targetList = new ArrayList<>(); }

    // [醒目新注释] 新增动态数据同步接口，用于在单例 Category 渲染与事件拦截前强行同步 targetList 状态，防止跨配方污染
    /**
     * 设置当前 {@link JeiLinkBox} 持有的成分数据列表。
     *
     * @param targets 成分数据列表
     */
    public void setTargets(List<T> targets) {
        this.targetList = (targets != null) ? targets : new ArrayList<>();
    }

    public JeiLinkBox<T> useJeiMode(IIngredientManager ingredientManager, IIngredientType<T> ingredientType) {
        this.boundIngredientType = ingredientType;
        this.boundRenderer = ingredientManager.getIngredientRenderer(ingredientType);
        return this;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, List<T> targets, int x, int y) {
        this.targetList = (targets != null) ? targets : new ArrayList<>();
        int total = targetList.size();
        if (boundIngredientType == null || total == 0) return;

        int startX = x + 5;
        int startY = y + 15;

        int limit = Math.min(total, PREVIEW_SLOTS);
        for (int i = 0; i < limit; i++) {
            T itemObj = targetList.get(i);
            int slotX = startX + (i * SLOT_SIZE) + 1;

            var slot = builder.addSlot(RecipeIngredientRole.INPUT, slotX, startY + 1)
                    .addIngredient(boundIngredientType, itemObj);

            if (boundRenderer != null) {
                slot.setCustomRenderer(boundIngredientType, boundRenderer);
            }
        }
    }

    public void registerInputHandler(IRecipeExtrasBuilder builder, Object contextObject, int x, int y) {
        int startX = x + 5;
        int startY = y + 15;
        int plusX = startX + (PREVIEW_SLOTS * SLOT_SIZE);
        registerPlusButtonInputHandler(builder, contextObject, plusX, startY);
    }

    public void registerPlusButtonInputHandler(IRecipeExtrasBuilder builder, Object contextObject, int plusX, int plusY) {
        builder.addInputHandler(new IJeiInputHandler() {
            @Override
            public ScreenRectangle getArea() {
                return new ScreenRectangle(plusX, plusY, 18, 18);
            }
            @Override
            public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
                InputConstants.Key key = input.getKey();
                if (key.getType() == InputConstants.Type.MOUSE && key.getValue() == 0) {
                    if (!input.isSimulate()) {
                        executeClickAction(contextObject);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void executeClickAction(Object contextObject) {
        if (this.customClickListener != null) {
            this.customClickListener.onClick(contextObject, targetList);
        }
    }

    public int getSize() { return targetList.size(); }
    public int getBoxHeight() { return 36; }
    public int getLayoutMode() { return layoutMode; }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, double mouseX, double mouseY) {
        int height = getBoxHeight();
        int totalSize = getSize();

        guiGraphics.fill(x, y, x + width, y + height, 0x22000000);
        guiGraphics.fill(x, y, x + width, y + 1, 0x40FFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0x40FFFFFF);

        String countText = totalSize > 0 ? String.format(TEXT_COUNT_ALL_FMT, totalSize) : TEXT_COUNT_NONE;
        // [醒目新注释] 渲染时实时调用 titleSupplier.get().getString() 获取当前语言环境下的文本
        String currentTitle = this.titleSupplier != null ? this.titleSupplier.get().getString() : "";
        String headerText = String.format(TEXT_TITLE_FMT, currentTitle, countText);
        guiGraphics.drawString(font, headerText, x + 5, y + 3, 0xFFFFFF, false);

        int startX = x + 5;
        int startY = y + 15;

        for (int i = 0; i < PREVIEW_SLOTS; i++) {
            int slotX = startX + (i * SLOT_SIZE);
            guiGraphics.fill(slotX, startY, slotX + 18, startY + 18, 0x20FFFFFF);
            guiGraphics.fill(slotX, startY, slotX + 18, startY + 1, 0x40000000);
            guiGraphics.fill(slotX, startY + 17, slotX + 18, startY + 18, 0x40FFFFFF);
            guiGraphics.fill(slotX, startY, slotX + 1, startY + 18, 0x40000000);
            guiGraphics.fill(slotX + 17, startY, slotX + 18, startY + 18, 0x40FFFFFF);
        }

        int plusX = startX + (PREVIEW_SLOTS * SLOT_SIZE);
        boolean isHovered = mouseX >= plusX && mouseX <= plusX + 18 && mouseY >= startY && mouseY <= startY + 18;

        int btnBg = isHovered ? 0x8000AA00 : 0x35000000;
        guiGraphics.fill(plusX, startY, plusX + 18, startY + 18, btnBg);

        int strWidth = font.width(TEXT_PLUS_ICON);
        guiGraphics.drawString(font, TEXT_PLUS_ICON, plusX + (18 - strWidth) / 2 + 1, startY + 5, 0xFFFFFF, false);

        if (isHovered) {
            guiGraphics.renderComponentTooltip(
                    font,
                    List.of(Component.literal(String.format(TOOLTIP_ALL_FMT, totalSize))),
                    (int) mouseX,
                    (int) mouseY
            );
        }
    }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width) {
        render(guiGraphics, font, x, y, width, -1, -1);
    }
}
// endregion JeiLinkBox Implementation