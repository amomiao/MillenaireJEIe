package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.BuildingUpgradeCostHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 展示建筑计划集 {@link BuildingPlanSet} 建造与升级消耗原生物品 {@link ItemStack} 的 JEI 全景大窗口分类。
 */
public class BuildingCostCategory extends BaseMillCategory<BuildingPlanSet> {
    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_cost_full", BuildingPlanSet.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    /**
     * [醒目新注释] 缓存上一次绘制的 {@link BuildingPlanSet} 对象。
     */
    private BuildingPlanSet lastRecipe;

    /**
     * [醒目新注释] 缓存渲染文本组件 {@link Component}。
     */
    private Component cachedDisplayNameComponent;

    public BuildingCostCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(Items.CHEST)));
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("建筑消耗材料 (全景大窗口)");
    }

    /**
     * 设置材料消耗分类的 JEI 槽位布局与物品成分。
     * <p>
     * 遵循要求：重构为与 {@link BuildingVillagerCategory} 一致的简洁流式处理结构。
     * 若存在建材消耗，直接获取第一个变体结果的物品消耗列表进行展示。
     * </p>
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe 当前建筑计划集对象 {@link BuildingPlanSet}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(MillIngredientTypes.BUILDING, recipe);

        // [醒目新注释] 不使用额外 API，直接调用 BuildingUpgradeCostHelper 获取 Map
        Map<String, List<ItemStack>> map = BuildingUpgradeCostHelper.getBuildingUpgradeCosts(recipe);
        if (map == null || map.isEmpty()) return;

        // [醒目新注释] 原有的 cost1, cost2, cost3 按照 "0","1","2" 提取的代码存在 Key 不匹配问题（Map 的 Key 为变体标识名）
        // [醒目新注释] 修改为：如果有消耗，直接取第一个结果（即 Map 的第一个 Entry 的 Value）的消耗列表
        List<ItemStack> allCosts = new ArrayList<>(map.values().iterator().next());
        if (allCosts.isEmpty()) return;

        // [醒目新注释] 针对输入焦点进行筛选重排，置顶玩家点击的 ItemStack
        if (focuses != null && !focuses.isEmpty()) {
            List<ItemStack> focusedItems = focuses.getFocuses(VanillaTypes.ITEM_STACK)
                    .map(f -> f.getTypedValue().getIngredient())
                    .toList();
            if (!focusedItems.isEmpty()) {
                allCosts.sort((a, b) -> {
                    boolean matchA = focusedItems.stream().anyMatch(f -> ItemStack.isSameItemSameComponents(f, a));
                    boolean matchB = focusedItems.stream().anyMatch(f -> ItemStack.isSameItemSameComponents(f, b));
                    return Boolean.compare(matchB, matchA);
                });
            }
        }

        int startX = 14;
        int startY = 24;

        for (int i = 0; i < Math.min(allCosts.size(), PAGE_SIZE); i++) {
            ItemStack stack = allCosts.get(i);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX + 1, slotY + 1)
                    .addIngredient(VanillaTypes.ITEM_STACK, stack);
        }
    }

    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            String name = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();
            this.cachedDisplayNameComponent = Component.literal("§1所属建筑: §f" + name);
        }

        guiGraphics.drawString(font, this.cachedDisplayNameComponent, 14, 8, 0x000000, false);

        int total = recipeSlotsView.getSlotViews().size();
        int startX = 14;
        int startY = 24;

        for (int i = 0; i < PAGE_SIZE; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            if (i < total) {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x20000000);
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0x50000000);
                guiGraphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0x50000000);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0x50000000);
                guiGraphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0x50000000);
            }
        }
    }
}