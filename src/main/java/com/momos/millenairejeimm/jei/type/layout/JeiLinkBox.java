package com.momos.millenairejeimm.jei.type.layout;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 固定视口与滚动指示器链接框 {@link JeiLinkBox}。
 */
public class JeiLinkBox<T> {
    private static final int SLOT_SIZE = 18;
    private static final int SLOTS_PER_ROW = 8;
    private static final int MAX_VISIBLE_ROWS = 2; // [醒目新注释] 强制受限区域：最大展示 2 行
    private static final int MAX_VISIBLE_SLOTS = SLOTS_PER_ROW * MAX_VISIBLE_ROWS; // 16 个可视槽位

    private final String title;
    private List<T> targetList = new ArrayList<>();

    private IIngredientType<T> boundIngredientType;
    private IIngredientRenderer<T> boundRenderer;

    public JeiLinkBox(String title) {
        this.title = title;
    }

    public JeiLinkBox<T> useJeiMode(IJeiRuntime jeiRuntime, IIngredientType<T> ingredientType) {
        return useJeiMode(jeiRuntime.getIngredientManager(), ingredientType);
    }

    public JeiLinkBox<T> useJeiMode(IIngredientManager ingredientManager, IIngredientType<T> ingredientType) {
        this.boundIngredientType = ingredientType;
        this.boundRenderer = ingredientManager.getIngredientRenderer(ingredientType);
        return this;
    }

    /**
     * [醒目新注释]
     * 核心逻辑：固定仅注册 16 个真实的 JEI 槽位。
     * 当总量超出 16 时，将关联数据交错压入槽位的 Ingredient 列表中，利用 JEI 原生轮播能力实现多项折叠展示。
     */
    public void setRecipe(IRecipeLayoutBuilder builder, List<T> targets, int x, int y) {
        this.targetList = (targets != null) ? targets : new ArrayList<>();
        int total = targetList.size();
        if (boundIngredientType == null || total == 0) return;

        int visibleSlotsCount = Math.min(total, MAX_VISIBLE_SLOTS);
        int startX = x + 6;
        int startY = y + 14;

        for (int i = 0; i < visibleSlotsCount; i++) {
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY);

            // 将 mapping 到该槽位位置的所有元素塞入槽位候选池
            List<T> slotCandidates = new ArrayList<>();
            for (int j = i; j < total; j += MAX_VISIBLE_SLOTS) {
                slotCandidates.add(targetList.get(j));
            }

            slotBuilder.addIngredients(boundIngredientType, slotCandidates);

            if (boundRenderer != null) {
                slotBuilder.setCustomRenderer(boundIngredientType, boundRenderer);
            }
        }
    }

    public int getSize() { return targetList.size(); }
    /** [醒目新注释] 容器高度固定受控：最深不超过 54px */
    public int getBoxHeight() { return 14 + (MAX_VISIBLE_ROWS * SLOT_SIZE); }

    /**
     * [醒目新注释]
     * 渲染固定区域边框、背景及右侧滚动指示条 (Scrollbar Track)。
     */
    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width) {
        int height = getBoxHeight();
        int totalSize = getSize();

        // 1. 绘制固定受控区域半透明背景
        guiGraphics.fill(x, y, x + width, y + height, 0x22000000);
        guiGraphics.fill(x, y, x + width, y + 1, 0x40FFFFFF);

        // 2. 绘制标题栏（包含折叠/轮播提示）
        String countText = totalSize > MAX_VISIBLE_SLOTS
                ? " §a(" + totalSize + ") §8[折叠轮播中]"
                : (totalSize > 0 ? " §a(" + totalSize + ")" : " §7(无)");
        guiGraphics.drawString(font, "§e" + title + countText, x + 4, y + 3, 0xFFFFFF, false);

        // 3. 绘制 2 行固定网格槽位背景
        int startX = x + 6;
        int startY = y + 14;

        for (int i = 0; i < MAX_VISIBLE_SLOTS; i++) {
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            if (i < totalSize || totalSize == 0) {
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x25FFFFFF);
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0x30FFFFFF);
                guiGraphics.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, 0x30FFFFFF);
                guiGraphics.fill(slotX - 1, slotY, slotX, slotY + 16, 0x30FFFFFF);
                guiGraphics.fill(slotX + 16, slotY, slotX + 17, slotY + 16, 0x30FFFFFF);
            }
        }

        // 4. [醒目新注释] 绘制右侧微型滚动条指示器
        if (totalSize > MAX_VISIBLE_SLOTS) {
            int trackX = x + width - 8;
            int trackY = y + 14;
            int trackHeight = MAX_VISIBLE_ROWS * SLOT_SIZE;

            // 滚动条轨道背景
            guiGraphics.fill(trackX, trackY, trackX + 4, trackY + trackHeight, 0x30FFFFFF);

            // 动态计算平滑滑块位置
            long time = System.currentTimeMillis() / 80;
            int thumbHeight = Math.max(8, (int) ((float) MAX_VISIBLE_SLOTS / totalSize * trackHeight));
            int maxTravel = trackHeight - thumbHeight;
            int thumbY = trackY + (int) (Math.abs(time % (maxTravel * 2) - maxTravel));

            // 滚动条滑块
            guiGraphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbHeight, 0xAAFFFFFF);
        }
    }
}