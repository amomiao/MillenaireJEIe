package com.momos.millenairejeimm.jei.Runtime;

import com.mojang.blaze3d.platform.InputConstants;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 千年 JEI 通用联动槽位容器组件 {@link JeiLinkBox}。
 * <p>
 * 支持预览 8 个前置要素并在第 9 格提供精准跳转到二级全景大窗口的交互按钮。
 * 重构后内建 {@link JeiNavigationHelper} 现代化定向跳转体系，支持函数式数据提取，彻底消灭跨配方数据污染。
 * 本地化文本与颜色样式解耦，全量使用参数化 Key 体系。
 * </p>
 *
 * @param <T> 槽位绑定的成分数据类型
 */
public class JeiLinkBox<T> {
    private static final int SLOT_SIZE = 18;
    private static final int PREVIEW_SLOTS = 8;

    private static final String TEXT_TITLE_FMT = "§e%s%s";
    private static final String TEXT_PLUS_ICON = "§a...";

    private Supplier<Component> titleSupplier;
    private final int layoutMode;
    private List<T> targetList = new ArrayList<>();
    private Function<Object, List<T>> dataProvider;

    private IIngredientType<T> boundIngredientType;
    private IIngredientRenderer<T> boundRenderer;
    private OnPlusClickListener<T> customClickListener;

    private RecipeType<?> targetRecipeType;
    private Function<Object, ?> targetRecipeExtractor;

    @FunctionalInterface
    public interface OnPlusClickListener<T> {
        void onClick(Object contextObject, List<T> items);
    }

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

    public JeiLinkBox<T> setTitleSupplier(Supplier<Component> titleSupplier) {
        this.titleSupplier = titleSupplier != null ? titleSupplier : () -> Component.empty();
        return this;
    }

    @Deprecated(since = "1.0", forRemoval = true)
    public JeiLinkBox<T> setOnPlusClick(OnPlusClickListener<T> listener) {
        this.customClickListener = listener;
        return this;
    }

    /**
     * 设置动态函数式数据提供者。
     *
     * @param provider 从配方上下文对象提取成分列表的函数
     */
    public JeiLinkBox<T> setDataProvider(Function<Object, List<T>> provider) {
        this.dataProvider = provider;
        return this;
    }

    /**
     * 绑定定向跳转的二级分类（配方对象直接由上下文传入）。
     *
     * @param targetType 目标分类类型 {@link RecipeType}
     */
    public JeiLinkBox<T> setTargetCategory(RecipeType<?> targetType) {
        this.targetRecipeType = targetType;
        this.targetRecipeExtractor = null;
        return this;
    }

    /**
     * 绑定定向跳转的二级分类并附带配方转换提取函数。
     *
     * @param targetType      目标分类类型 {@link RecipeType}
     * @param recipeExtractor 从当前配方上下文提取目标分类配方的转换函数
     * @param <R>             目标配方类型
     */
    public <R> JeiLinkBox<T> setTargetCategory(RecipeType<R> targetType, Function<Object, R> recipeExtractor) {
        this.targetRecipeType = targetType;
        this.targetRecipeExtractor = recipeExtractor;
        return this;
    }

    /**
     * 绑定定向跳转的二级分类并附带列表提取函数（用于展示一组配方）。
     *
     * @param targetType    目标分类类型 {@link RecipeType}
     * @param listExtractor 从当前配方上下文提取目标分类配方列表的转换函数
     * @param <R>           目标配方类型
     */
    public <R> JeiLinkBox<T> setTargetRecipeList(RecipeType<R> targetType, Function<Object, List<R>> listExtractor) {
        this.targetRecipeType = targetType;
        this.targetRecipeExtractor = listExtractor;
        return this;
    }

    public void Clear() {
        this.targetList = new ArrayList<>();
    }

    /**
     * 设置当前 {@link JeiLinkBox} 持有的成分数据列表。
     *
     * @param targets 成分数据列表
     */
    public void setTargets(List<T> targets) {
        this.targetList = (targets != null) ? targets : new ArrayList<>();
    }

    /**
     * 获取指定上下文对应的成分列表。
     */
    public List<T> resolveTargets(Object contextObject) {
        if (dataProvider != null && contextObject != null) {
            List<T> provided = dataProvider.apply(contextObject);
            return provided != null ? provided : List.of();
        }
        return targetList != null ? targetList : List.of();
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
                if (getSize() == 0 && targetRecipeType == null) return false;
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeClickAction(Object contextObject) {
        if (targetRecipeType != null) {
            if (targetRecipeExtractor != null) {
                Object extracted = targetRecipeExtractor.apply(contextObject);
                if (extracted instanceof List<?> list) {
                    JeiNavigationHelper.navigateToRecipes((RecipeType) targetRecipeType, list, List.of());
                } else if (extracted != null) {
                    JeiNavigationHelper.navigateToRecipe((RecipeType) targetRecipeType, extracted);
                }
            } else if (contextObject != null && targetRecipeType.getRecipeClass().isInstance(contextObject)) {
                JeiNavigationHelper.navigateToRecipe((RecipeType) targetRecipeType, contextObject);
            } else {
                JeiNavigationHelper.navigateToCategory(targetRecipeType);
            }
        } else if (this.customClickListener != null) {
            this.customClickListener.onClick(contextObject, targetList);
            JeiNavigationHelper.playClickSound();
        }
    }

    public int getSize() {
        return targetList.size();
    }

    public int getBoxHeight() {
        return 36;
    }

    public int getLayoutMode() {
        return layoutMode;
    }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, double mouseX, double mouseY) {
        int height = getBoxHeight();
        int totalSize = getSize();

        guiGraphics.fill(x, y, x + width, y + height, 0x22000000);
        guiGraphics.fill(x, y, x + width, y + 1, 0x40FFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0x40FFFFFF);

        String countText = totalSize > 0
                ? " §a" + MillenaireJeimmKeys.CountAll_S.asComponent(String.valueOf(totalSize)).getString()
                : " §7" + MillenaireJeimmKeys.CountNone.asComponent().getString();
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
        boolean hasTargets = totalSize > 0 || targetRecipeType != null;

        int btnBg = hasTargets ? (isHovered ? 0x8000AA00 : 0x35000000) : 0x15000000;
        guiGraphics.fill(plusX, startY, plusX + 18, startY + 18, btnBg);
        guiGraphics.fill(plusX, startY, plusX + 18, startY + 1, hasTargets ? 0x60FFFFFF : 0x20FFFFFF);
        guiGraphics.fill(plusX, startY + 17, plusX + 18, startY + 18, hasTargets ? 0x60FFFFFF : 0x20FFFFFF);
        guiGraphics.fill(plusX, startY, plusX + 1, startY + 18, hasTargets ? 0x60FFFFFF : 0x20FFFFFF);
        guiGraphics.fill(plusX + 17, startY, plusX + 18, startY + 18, hasTargets ? 0x60FFFFFF : 0x20FFFFFF);

        String iconStr = hasTargets ? TEXT_PLUS_ICON : "§8...";
        int strWidth = font.width(iconStr);
        guiGraphics.drawString(font, iconStr, plusX + (18 - strWidth) / 2 + 1, startY + 5, 0xFFFFFF, false);

        if (isHovered && hasTargets) {
            Component tooltip = MillenaireJeimmKeys.ViewAllTooltip_S.asComponent(String.valueOf(totalSize));
            guiGraphics.renderComponentTooltip(font, List.of(tooltip), (int) mouseX, (int) mouseY);
        }
    }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width) {
        render(guiGraphics, font, x, y, width, -1, -1);
    }
}
