package com.momos.millenairejeimm.jei.Runtime;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 千年 JEI 二级全景大窗口通用泛型抽象基类 {@link BaseMillPanoramicCategory}。
 * <p>
 * 彻底解除 40 格上限限制，有多少画多少，对齐建材消耗页绘制风格；
 * 采用动态纯色半透明槽位边框与自适应渲染，消灭空置幽灵槽，杜绝 UV 纹理拉伸与错位。
 * </p>
 *
 * @param <R> 配方数据对象类型（如 VillageType, BuildingPlanSet, VillagerType, Culture）
 * @param <I> 网格槽位成分数据类型（如 BuildingPlanSet, VillagerType, VillageType, ItemStack）
 */
public abstract class BaseMillPanoramicCategory<R, I> extends BaseMillCategory<R> {

    public static final int SLOT_SIZE = 18;
    public static final int COLUMNS = 8;
    public static final int ROWS = 5;
    public static final int PAGE_SIZE = COLUMNS * ROWS;

    protected static final int GUI_START_X = 14;
    protected static final int GUI_START_Y = 24;
    protected static final int HEADER_X = 14;
    protected static final int HEADER_Y = 8;
    protected static final int HEADER_COLOR = 0x000000;

    private final RecipeType<R> recipeType;
    private final Component categoryTitle;
    private final IIngredientType<I> slotIngredientType;
    @Nullable
    private final IIngredientType<R> recipeIngredientType;
    private final Function<R, Component> titleProvider;
    private final Function<R, List<I>> itemsProvider;

    private R lastRecipe;
    private Component cachedDisplayNameComponent;

    public BaseMillPanoramicCategory(
            IGuiHelper guiHelper,
            ItemLike iconItem,
            RecipeType<R> recipeType,
            Component categoryTitle,
            IIngredientType<I> slotIngredientType,
            @Nullable IIngredientType<R> recipeIngredientType,
            Function<R, Component> titleProvider,
            Function<R, List<I>> itemsProvider
    ) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(iconItem)));
        this.recipeType = recipeType;
        this.categoryTitle = categoryTitle;
        this.slotIngredientType = slotIngredientType;
        this.recipeIngredientType = recipeIngredientType;
        this.titleProvider = titleProvider;
        this.itemsProvider = itemsProvider;
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return this.categoryTitle;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // 若配置了配方自身的成分类型，则注册为不可见输出端，便于标准 R/U 索引
        if (recipeIngredientType != null) {
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(recipeIngredientType, recipe);
        }

        List<I> items = itemsProvider != null ? itemsProvider.apply(recipe) : List.of();
        if (items == null || items.isEmpty()) return;

        // 焦点感知：若当前存在针对槽位成分的焦点，置顶该成分
        if (focuses != null && !focuses.isEmpty() && slotIngredientType != null) {
            List<I> focusedIngredients = focuses.getFocuses(slotIngredientType)
                    .map(f -> f.getTypedValue().getIngredient())
                    .toList();
            if (!focusedIngredients.isEmpty()) {
                items = new ArrayList<>(items);
                items.sort((a, b) -> Boolean.compare(
                        matchesAnyFocused(b, focusedIngredients),
                        matchesAnyFocused(a, focusedIngredients)
                ));
            }
        }

        // 解除 40 格硬编码截断，有多少注册多少
        for (int i = 0; i < items.size(); i++) {
            I item = items.get(i);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = GUI_START_X + (col * SLOT_SIZE);
            int slotY = GUI_START_Y + (row * SLOT_SIZE);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX + 1, slotY + 1)
                    .addIngredient(slotIngredientType, item);
        }
    }

    private boolean matchesAnyFocused(I candidate, List<I> focusedList) {
        if (candidate == null || focusedList == null) return false;
        if (candidate instanceof ItemStack stackCand) {
            return focusedList.stream().anyMatch(f -> f instanceof ItemStack sf && ItemStack.isSameItemSameComponents(stackCand, sf));
        }
        return focusedList.contains(candidate);
    }

    @Override
    public void draw(R recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            this.cachedDisplayNameComponent = titleProvider != null
                    ? titleProvider.apply(recipe)
                    : Component.literal(recipe.toString());
        }

        if (this.cachedDisplayNameComponent != null) {
            guiGraphics.drawString(font, this.cachedDisplayNameComponent, HEADER_X, HEADER_Y, HEADER_COLOR, false);
        }

        // 对齐 BuildingLevelCostCategory：按实际 slotViews 数量绘制，不画空置幽灵槽，纯色高光避免 UV 错乱
        int total = recipeSlotsView.getSlotViews().size();
        for (int i = 0; i < total; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = GUI_START_X + (col * SLOT_SIZE);
            int slotY = GUI_START_Y + (row * SLOT_SIZE);

            guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x20000000);
            guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0x50000000);
            guiGraphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0x50000000);
            guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0x50000000);
            guiGraphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0x50000000);
        }
    }
}
