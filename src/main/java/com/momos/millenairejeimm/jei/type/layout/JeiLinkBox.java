package com.momos.millenairejeimm.jei.type.layout;

import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureVillagesCategory;
// [醒目新注释] 引入 Debug 诊断工具类 {@link DebugGuiUtils}
import com.momos.millenairejeimm.jei.type.DebugGuiUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.millenaire.culture.Culture;

import java.util.ArrayList;
import java.util.List;

/**
 * [醒目新注释]
 * 修正 API 弃用问题，并参考 {@link DebugGuiUtils} 实现原生级别的像素碰撞按钮交互。
 */
public class JeiLinkBox<T> {
    private static final int SLOT_SIZE = 18;
    private static final int PREVIEW_SLOTS = 8; // 前 8 个元素预览槽位

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

    public void setRecipe(IRecipeLayoutBuilder builder, List<T> targets, int x, int y) {
        this.targetList = (targets != null) ? targets : new ArrayList<>();
        int total = targetList.size();
        if (boundIngredientType == null || total == 0) return;

        int startX = x + 5;
        int startY = y + 15;

        // 1. 注册前 8 个普通物品预览槽位
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

        // [醒目新注释] 移除第 9 格的 builder.addSlot(...)，防止 JEI 绘制绿色选中框与接管鼠标事件
    }

    /**
     * [醒目新注释]
     * 在 {@link #createRecipeExtras} 中注册第 9 格的 Tooltip 浮动提示。
     */
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Culture culture, int x, int y, int width) {
        int startX = x + 5;
        int startY = y + 15;
        int plusX = startX + (PREVIEW_SLOTS * SLOT_SIZE);

//        // 第 9 格 "+/细节" 悬浮提示区 (宽高: 18x18)
//        builder.addTooltip(
//                List.of(
//                        Component.literal("§a查看全部/深度诊断 (" + targetList.size() + " 个)"),
//                        Component.literal("§7单击以打开 JSON 诊断大窗口")
//                ),
//                new ScreenRectangle(plusX, startY, 18, 18)
//        );
    }

    /**
     * [醒目新注释]
     * 物理碰撞区域点击检测与处理，直接由 Category 类（如 {@link com.momos.millenairejeimm.jei.Runtime.cultureof.CultureCategory#handleInput}）委托调用。
     *
     * @param contextObject 上下文数据对象（例如 {@link Culture}）
     * @param mouseX        鼠标 X 坐标
     * @param mouseY        鼠标 Y 坐标
     * @param x             组件起始 X 坐标
     * @param y             组件起始 Y 坐标
     * @return 是否成功触发并处理了点击
     */
    public boolean handlePlusButtonClick(Object contextObject, double mouseX, double mouseY, int x, int y) {
        int startX = x + 5;
        int startY = y + 15;
        int plusX = startX + (PREVIEW_SLOTS * SLOT_SIZE);

        // 检测鼠标是否命中第 9 格加号按钮物理范围 (18x18)
        if (DebugGuiUtils.isMouseOver(mouseX, mouseY, plusX, startY, 18, 18)) {
            // [醒目新注释] 点击后唤起 DebugGuiUtils JSON 诊断界面
            DebugGuiUtils.openJsonDumpScreen(
                    targetList.isEmpty() ? contextObject : targetList,
                    List.of(
                            Component.literal("§e[分类标题]: §f" + title),
                            Component.literal("§e[关联数据总量]: §f" + targetList.size() + " 个")
                    )
            );
            return true;
        }
        return false;
    }

    public int getSize() { return targetList.size(); }
    public int getBoxHeight() { return 36; }

    /**
     * [醒目新注释]
     * 渲染 UI 背景、列表槽位与第 9 格按键外观。
     */
    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, double mouseX, double mouseY) {
        int height = getBoxHeight();
        int totalSize = getSize();

        guiGraphics.fill(x, y, x + width, y + height, 0x22000000);
        guiGraphics.fill(x, y, x + width, y + 1, 0x40FFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0x40FFFFFF);

        String countText = totalSize > 0 ? " §a(" + totalSize + " 个)" : " §7(无)";
        guiGraphics.drawString(font, "§e" + title + countText, x + 5, y + 3, 0xFFFFFF, false);

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

        // 第 9 格渲染
        int plusX = startX + (PREVIEW_SLOTS * SLOT_SIZE);
        boolean isHovered = mouseX >= plusX && mouseX <= plusX + 18 && mouseY >= startY && mouseY <= startY + 18;

        int btnBg = isHovered ? 0x8000AA00 : 0x35000000;
        guiGraphics.fill(plusX, startY, plusX + 18, startY + 18, btnBg);

        String plusStr = "§a+";
        int strWidth = font.width(plusStr);
        guiGraphics.drawString(font, plusStr, plusX + (18 - strWidth) / 2 + 1, startY + 5, 0xFFFFFF, false);
    }
}