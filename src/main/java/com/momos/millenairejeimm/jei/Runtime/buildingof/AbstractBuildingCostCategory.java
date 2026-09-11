// region 基类定义与变体匹配实现
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
 * 展示建筑计划集 {@link BuildingPlanSet} 建造与升级材料消耗的 JEI 全景大窗口分类基类。
 */
public abstract class AbstractBuildingCostCategory extends BaseMillCategory<BuildingPlanSet> {

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    /**
     * [醒目新注释] 变体标识字符 (例如 'a', 'b', 'c')。
     */
    protected final char variantKey;

    /**
     * [醒目新注释] 变体回退索引序号 (0, 1, 2)。
     */
    protected final int variantIndex;

    /**
     * [醒目新注释] 分类标题组件 {@link Component}。
     */
    protected final Component titleComponent;

    /**
     * [醒目新注释] 缓存上一次绘制的 {@link BuildingPlanSet} 对象。
     */
    private BuildingPlanSet lastRecipe;

    /**
     * [醒目新注释] 缓存渲染文本组件 {@link Component}。
     */
    private Component cachedDisplayNameComponent;

    public AbstractBuildingCostCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager, char variantKey, String variantTitle) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(Items.CHEST)));
        this.variantKey = Character.toLowerCase(variantKey);
        this.variantIndex = this.variantKey - 'a';
        this.titleComponent = Component.literal("建筑消耗材料 (" + variantTitle + ")");
    }

    @Override
    public Component getTitle() {
        return this.titleComponent;
    }

    /**
     * 设置材料消耗分类的 JEI 槽位布局与物品成分。
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe 当前建筑计划集对象 {@link BuildingPlanSet}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(MillIngredientTypes.BUILDING, recipe);

        // [醒目新注释] 调用 BuildingUpgradeCostHelper 获取 Map
        Map<String, List<ItemStack>> map = BuildingUpgradeCostHelper.getBuildingUpgradeCosts(recipe);
        if (map == null || map.isEmpty()) return;

        List<ItemStack> rawCosts = null;
        String keyStr = String.valueOf(this.variantKey);

        // [醒目新注释] 匹配策略：1. 优先按 key 匹配 ("a", "b", "c")；2. 回退按变体索引序号提取
        if (map.containsKey(keyStr)) {
            rawCosts = map.get(keyStr);
        } else if (map.containsKey(keyStr.toUpperCase())) {
            rawCosts = map.get(keyStr.toUpperCase());
        } else {
            List<List<ItemStack>> allValues = new ArrayList<>(map.values());
            if (this.variantIndex >= 0 && this.variantIndex < allValues.size()) {
                rawCosts = allValues.get(this.variantIndex);
            }
        }

        if (rawCosts == null || rawCosts.isEmpty()) return;

        List<ItemStack> allCosts = new ArrayList<>(rawCosts);

        // [醒目新注释] 过滤 null 与 isEmpty() 为 true 的物品（包含回退为 minecraft:air 的无效项），消除空槽位
        allCosts.removeIf(stack -> stack == null || stack.isEmpty());
        if (allCosts.isEmpty()) return;

        // [醒目新注释] 针对输入焦点进行筛选重排，置顶玩家点击的 {@link ItemStack}
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
            this.cachedDisplayNameComponent = Component.literal("§1所属建筑: §f" + name + " §8[变体 " + Character.toUpperCase(this.variantKey) + "]");
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
// endregion 基类定义与变体匹配实现