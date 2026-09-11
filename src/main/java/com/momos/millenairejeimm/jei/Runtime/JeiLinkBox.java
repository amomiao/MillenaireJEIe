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

// 注意:[+]现在改成了[...]所有加号内容已过时
// region 通用槽位组件与回调接口
/**
 * 千年 JEI 通用联动槽位容器组件 {@link JeiLinkBox}。
 * 本组件为纯粹的 UI 原子控件，不绑定任何特定业务（文化/建筑/村民/村庄均可通用）。
 *
 * @param <T> 槽位绑定的成分数据类型
 */
public class JeiLinkBox<T> {
    // region UI 常量与字符串模板
    private static final int SLOT_SIZE = 18;
    private static final int PREVIEW_SLOTS = 8;

    private static final String TEXT_COUNT_ALL_FMT = " §aAll(%d)";
    private static final String TEXT_COUNT_NONE = " §7(None)";
    private static final String TEXT_TITLE_FMT = "§e%s%s";
    private static final String TEXT_PLUS_ICON = "§a...";
    private static final String TOOLTIP_ALL_FMT = "§aAll(%d)";

    // endregion UI 常量与字符串模板

    private final String title;
    private final int layoutMode; // 兼容多页签/布局模式标记
    private List<T> targetList = new ArrayList<>();

    private IIngredientType<T> boundIngredientType;
    private IIngredientRenderer<T> boundRenderer;
    private OnPlusClickListener<T> customClickListener;

    @FunctionalInterface
    public interface OnPlusClickListener<T> {
        /**
         * 当用户点击尾部加号按钮时触发的回调接口。
         *
         * @param contextObject 当前配方上下文对象
         * @param items 当前 LinkBox 包含的数据列表
         */
        void onClick(Object contextObject, List<T> items);
    }

    public JeiLinkBox(String title) {
        this(title, 0);
    }

    /**
     * [醒目新注释] 兼容带 mode 参数的构造函数，用于支持变体/页签等扩展样式。
     */
    public JeiLinkBox(String title, int layoutMode) {
        this.title = title;
        this.layoutMode = layoutMode;
    }

    /**
     * @deprecated 本方法已废弃 (Since 1.0)。
     * <p>
     * <b>废弃原因：</b><br>
     * 在 JEI 19+ 架构中，企图通过第九格 {@code +} 按钮触发自定义回调并调用
     * {@link mezz.jei.api.runtime.IRecipesGui#showTypes(java.util.List)}
     * 或强行注入多重 {@link mezz.jei.api.recipe.RecipeIngredientRole} 来实现“单页签精准聚焦”的方案已证实不可行。<br>
     * JEI 底层的 Focus Context 与 Slot Hitbox 强绑定，强行拦截或切换焦点会导致上下文丢失、R/U 键交互失效及多页签错位。<br>
     * </p>
     * <p>
     * <b>替代方案：</b><br>
     * 请直接将关联对象统一注册为 {@link mezz.jei.api.recipe.RecipeIngredientRole#OUTPUT} 角色，
     * 遵循 JEI 原生的多页签检索与分发机制，由玩家在顶级页签栏自行点击切换。
     * </p>
     * @param listener 废弃的加号点击事件监听器
     * @return 当前 {@link JeiLinkBox} 实例
     * @see <a href="https://github.com/mezz/JustEnoughItems">JEI API Specification</a>
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public JeiLinkBox<T> setOnPlusClick(OnPlusClickListener<T> listener) {
        // 新增醒目警告注释：此回调逻辑存在 JEI 19+ 焦点上下文冲突风险，保留仅用于兼容旧版退化逻辑
        this.customClickListener = listener;
        return this;
    }

    public JeiLinkBox<T> useJeiMode(IIngredientManager ingredientManager, IIngredientType<T> ingredientType) {
        this.boundIngredientType = ingredientType;
        this.boundRenderer = ingredientManager.getIngredientRenderer(ingredientType);
        return this;
    }

// endregion 通用槽位组件与回调接口

// region JEI 数据与事件注册
    /**
     * [醒目新注释] 将成分列表注册至 JEI 槽位构建器。
     */
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

    /**
     * [醒目新注释] 注册物理按键拦截器，匹配加号按钮的 18x18 范围。
     */
    public void registerInputHandler(IRecipeExtrasBuilder builder, Object contextObject, int x, int y) {
        int startX = x + 5;
        int startY = y + 15;
        int plusX = startX + (PREVIEW_SLOTS * SLOT_SIZE);

        // [醒目新注释] 委托调用专门处理第 9 格（加号按钮）点击事件的方法
        registerPlusButtonInputHandler(builder, contextObject, plusX, startY);
    }

    /**
     * 独立注册第 9 格（加号按钮）物理点击事件的监听器。
     * [醒目新注释] 抽取为独立方法，便于二次扩展或重写第 9 格的响应逻辑。
     *
     * @param builder       JEI 配方额外组件构建器 {@link IRecipeExtrasBuilder}
     * @param contextObject 当前配方上下文对象
     * @param plusX         加号按钮（第 9 格）在像素网格中的物理 X 轴起始坐标
     * @param plusY         加号按钮（第 9 格）在像素网格中的物理 Y 轴起始坐标
     */
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

    /**
     * 执行加号按钮被点击后的具体响应逻辑。
     * [醒目新注释] 优先触发 {@link #customClickListener}，未配置时向客户端聊天框输出简易测试文本。
     *
     * @param contextObject 当前上下文对象
     */
    private void executeClickAction(Object contextObject) {
        if (this.customClickListener != null) {
            this.customClickListener.onClick(contextObject, targetList);
        } else {
            // 按钮事件已废弃
        }
    }

// endregion JEI 数据与事件注册

    // region UI 渲染与通用提示
    public int getSize() { return targetList.size(); }
    public int getBoxHeight() { return 36; }
    public int getLayoutMode() { return layoutMode; }

    /**
     * [醒目新注释] 通用组件渲染逻辑，展示标题、网格槽位与末尾加号按钮。
     */
    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, double mouseX, double mouseY) {
        int height = getBoxHeight();
        int totalSize = getSize();

        guiGraphics.fill(x, y, x + width, y + height, 0x22000000);
        guiGraphics.fill(x, y, x + width, y + 1, 0x40FFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0x40FFFFFF);

        String countText = totalSize > 0 ? String.format(TEXT_COUNT_ALL_FMT, totalSize) : TEXT_COUNT_NONE;
        String headerText = String.format(TEXT_TITLE_FMT, title, countText);
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
                    List.of(
                            Component.literal(String.format(TOOLTIP_ALL_FMT, totalSize))
                    ),
                    (int) mouseX,
                    (int) mouseY
            );
        }
    }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width) {
        render(guiGraphics, font, x, y, width, -1, -1);
    }
// endregion UI 渲染与通用提示
}