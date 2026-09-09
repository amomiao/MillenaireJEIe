package com.momos.millenairejeimm.jei.type.layout;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * JEI 链接框定义与渲染辅助类 {@link JeiLinkBox}。
 * 集成 R/U 键与鼠标左右键交互，自动弹出 JEI 配方/用途窗口。
 *
 * @param <T> 关联对象类型
 */
public class JeiLinkBox<T> {
    private static final int SLOT_SIZE = 18;
    private static final int SLOTS_PER_ROW = 8;

    private final String title;
    private final int maxRows;
    private final int capacity;

    private int currentPage = 0;
    private List<T> targetList = new ArrayList<>();

    private IngredientRenderer<T> ingredientRenderer;
    private Function<T, List<Component>> tooltipMapper;
    private OnClickHandler<T> onClickHandler;

    @FunctionalInterface
    public interface IngredientRenderer<T> {
        void render(GuiGraphics guiGraphics, T ingredient, int x, int y);
    }

    @FunctionalInterface
    public interface OnClickHandler<T> {
        boolean onClick(T ingredient, int button);
    }

    public JeiLinkBox(String title, int maxRows) {
        this.title = title;
        this.maxRows = Math.min(2, Math.max(1, maxRows));
        this.capacity = this.maxRows * SLOTS_PER_ROW;
    }

    /**
     * [醒目新注释]
     * 模式一：JEI 全功能绑定模式。
     * 自动绑定渲染与 Tooltip，并开启 R/U 键与左右键触发配方/用途界面 {@link IJeiRuntime}。
     *
     * @param jeiRuntime JEI 运行时实例 {@link IJeiRuntime}
     * @param ingredientType JEI 成分类型 {@link IIngredientType}
     * @return 当前 {@link JeiLinkBox} 实例
     */
    public JeiLinkBox<T> useJeiMode(IJeiRuntime jeiRuntime, IIngredientType<T> ingredientType) {
        IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
        useJeiMode(ingredientManager, ingredientType);
        enableJeiRecipeTrigger(jeiRuntime, ingredientType);
        return this;
    }

    public JeiLinkBox<T> useJeiMode(IIngredientManager ingredientManager, IIngredientType<T> ingredientType) {
        IIngredientRenderer<T> renderer = ingredientManager.getIngredientRenderer(ingredientType);
        IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(ingredientType);

        this.ingredientRenderer = (guiGraphics, ingredient, x, y) -> {
            if (ingredient == null) return;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 0);
            renderer.render(guiGraphics, ingredient);
            guiGraphics.pose().popPose();
        };

        this.tooltipMapper = ingredient -> {
            if (ingredient == null) return List.of();
            return List.of(
                    Component.literal("§f" + helper.getDisplayName(ingredient)),
                    Component.literal("§7ID: " + helper.getResourceLocation(ingredient))
            );
        };
        return this;
    }

    public JeiLinkBox<T> useItemMode(IJeiRuntime jeiRuntime, Function<T, ItemStack> itemProvider) {
        useItemMode(itemProvider);
        this.onClickHandler = (ingredient, button) -> {
            if (ingredient == null || jeiRuntime == null) return false;
            ItemStack stack = itemProvider.apply(ingredient);
            if (stack.isEmpty()) return false;

            IFocusFactory focusFactory = jeiRuntime.getJeiHelpers().getFocusFactory();
            RecipeIngredientRole role = (button == 0) ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
            IFocus<ItemStack> focus = focusFactory.createFocus(role, VanillaTypes.ITEM_STACK, stack);
            jeiRuntime.getRecipesGui().show(focus);
            return true;
        };
        return this;
    }

    public JeiLinkBox<T> useItemMode(Function<T, ItemStack> itemProvider) {
        this.ingredientRenderer = (guiGraphics, ingredient, x, y) -> {
            if (ingredient == null) return;
            ItemStack stack = itemProvider.apply(ingredient);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, x, y);
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
            }
        };

        this.tooltipMapper = ingredient -> {
            if (ingredient == null) return List.of();
            ItemStack stack = itemProvider.apply(ingredient);
            return List.of(stack.getHoverName());
        };
        return this;
    }

    public JeiLinkBox<T> enableJeiRecipeTrigger(IJeiRuntime jeiRuntime, IIngredientType<T> ingredientType) {
        this.onClickHandler = (ingredient, button) -> {
            if (ingredient == null || jeiRuntime == null) return false;

            IFocusFactory focusFactory = jeiRuntime.getJeiHelpers().getFocusFactory();
            // [醒目新注释] button = 0 (左键/R键) -> OUTPUT (查看合成/配方)；button = 1 (右键/U键) -> INPUT (查看用途)
            RecipeIngredientRole role = (button == 0) ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;

            IFocus<T> focus = focusFactory.createFocus(role, ingredientType, ingredient);
            jeiRuntime.getRecipesGui().show(focus);
            return true;
        };
        return this;
    }

    public JeiLinkBox<T> setTargets(List<T> targets) {
        this.targetList = (targets != null) ? targets : new ArrayList<>();
        this.currentPage = 0;
        return this;
    }

    public int getSize() { return targetList == null ? 0 : targetList.size(); }
    public int getBoxHeight() { return 12 + (maxRows * SLOT_SIZE); }
    public int getTotalPages() {
        if (targetList == null || targetList.isEmpty()) return 1;
        return (int) Math.ceil((double) targetList.size() / capacity);
    }

    /**
     * [醒目新注释]
     * 响应 R 键 (查看配方) / U 键 (查看用途) 键盘事件。
     */
    public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers, int x, int y, int width) {
        if (targetList == null || targetList.isEmpty()) return false;

        boolean isRKey = (keyCode == InputConstants.KEY_R);
        boolean isUKey = (keyCode == InputConstants.KEY_U);

        if (!isRKey && !isUKey) return false;

        int startX = x + 4;
        int startY = y + 13;
        int startIndex = currentPage * capacity;
        int totalSize = getSize();

        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotX = startX + (col * SLOT_SIZE);
                int slotY = startY + (row * SLOT_SIZE);
                int itemIndex = startIndex + (row * SLOTS_PER_ROW + col);

                if (itemIndex < totalSize) {
                    if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                        T itemObj = targetList.get(itemIndex);
                        if (itemObj != null && onClickHandler != null) {
                            return onClickHandler.onClick(itemObj, isRKey ? 0 : 1);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * [醒目新注释]
     * 响应鼠标左右键点击 (左键: 翻页/配方, 右键: 用途)。
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (targetList == null || targetList.isEmpty()) return false;

        int totalPages = getTotalPages();
        int ctrlX = x + width - 18;
        int startY = y + 13;

        // 1. 处理右侧上下翻页按钮 (仅左键)
        if (button == 0 && totalPages > 1) {
            if (mouseX >= ctrlX && mouseX <= ctrlX + 16 && mouseY >= startY && mouseY <= startY + 12) {
                if (currentPage > 0) { currentPage--; return true; }
            }
            int downY = startY + (maxRows * SLOT_SIZE) - 12;
            if (mouseX >= ctrlX && mouseX <= ctrlX + 16 && mouseY >= downY && mouseY <= downY + 12) {
                if (currentPage < totalPages - 1) { currentPage++; return true; }
            }
        }

        // 2. 处理槽位图标点击 (0: 左键/配方, 1: 右键/用途)
        int startX = x + 4;
        int startIndex = currentPage * capacity;
        int totalSize = getSize();

        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotX = startX + (col * SLOT_SIZE);
                int slotY = startY + (row * SLOT_SIZE);
                int itemIndex = startIndex + (row * SLOTS_PER_ROW + col);

                if (itemIndex < totalSize) {
                    if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                        T itemObj = targetList.get(itemIndex);
                        if (itemObj != null && onClickHandler != null) {
                            return onClickHandler.onClick(itemObj, button);
                        }
                    }
                }
            }
        }
        return false;
    }

    // [醒目新注释] 补全缺失的 render 简写重载方法，解决 CultureLinkLayout.java:51 编译报错
    public void render(GuiGraphics guiGraphics, Font font, int y) { render(guiGraphics, font, 2, y, 172, -1, -1); }
    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width) { render(guiGraphics, font, x, y, width, -1, -1); }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, double mouseX, double mouseY) {
        int height = getBoxHeight();
        int totalSize = getSize();
        int totalPages = getTotalPages();

        guiGraphics.fill(x, y, x + width, y + height + 1, 0x22000000);
        guiGraphics.fill(x, y, x + width, y + 1, 0x40FFFFFF);

        String countText = totalSize > 0 ? " §a(" + totalSize + ")" : " §7(Empty)";
        guiGraphics.drawString(font, "§e" + title + countText, x + 4, y + 3, 0xFFFFFF, false);

        int startX = x + 4;
        int startY = y + 13;
        int startIndex = currentPage * capacity;

        T hoveredTarget = null;

        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotX = startX + (col * SLOT_SIZE);
                int slotY = startY + (row * SLOT_SIZE);
                int itemIndex = startIndex + (row * SLOTS_PER_ROW + col);

                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x25FFFFFF);
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0x40FFFFFF);
                guiGraphics.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, 0x40FFFFFF);
                guiGraphics.fill(slotX - 1, slotY, slotX, slotY + 16, 0x40FFFFFF);
                guiGraphics.fill(slotX + 16, slotY, slotX + 17, slotY + 16, 0x40FFFFFF);

                if (itemIndex < totalSize) {
                    T itemObj = targetList.get(itemIndex);

                    if (ingredientRenderer != null && itemObj != null) {
                        ingredientRenderer.render(guiGraphics, itemObj, slotX, slotY);
                    }

                    if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x50FFFFFF);
                        hoveredTarget = itemObj;
                    }
                }
            }
        }

        if (totalPages > 1) {
            int ctrlX = x + width - 18;
            int upY = startY;
            int downY = startY + (maxRows * SLOT_SIZE) - 12;

            boolean upHover = mouseX >= ctrlX && mouseX <= ctrlX + 16 && mouseY >= upY && mouseY <= upY + 12;
            boolean downHover = mouseX >= ctrlX && mouseX <= ctrlX + 16 && mouseY >= downY && mouseY <= downY + 12;

            guiGraphics.fill(ctrlX, upY, ctrlX + 16, upY + 12, upHover ? 0x60FFFFFF : 0x30FFFFFF);
            guiGraphics.drawString(font, "▲", ctrlX + 5, upY + 2, currentPage > 0 ? 0xFFFFFF : 0x808080, false);

            String pageStr = (currentPage + 1) + "/" + totalPages;
            guiGraphics.drawString(font, pageStr, ctrlX + 8 - (font.width(pageStr) / 2), startY + (maxRows * SLOT_SIZE / 2) - 4, 0xAAAAAA, false);

            guiGraphics.fill(ctrlX, downY, ctrlX + 16, downY + 12, downHover ? 0x60FFFFFF : 0x30FFFFFF);
            guiGraphics.drawString(font, "▼", ctrlX + 5, downY + 2, currentPage < totalPages - 1 ? 0xFFFFFF : 0x808080, false);
        }

        if (hoveredTarget != null && tooltipMapper != null) {
            List<Component> tooltip = tooltipMapper.apply(hoveredTarget);
            if (tooltip != null && !tooltip.isEmpty()) {
                guiGraphics.renderComponentTooltip(font, tooltip, (int) mouseX, (int) mouseY);
            }
        }
    }
}
