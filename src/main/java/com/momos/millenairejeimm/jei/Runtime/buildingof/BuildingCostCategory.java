// region 建筑资源消耗总和 JEI 分类面板实现 (Building Cost Category Implementation)
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.building.BuildingPlanSet.LevelDef;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇总统计 {@link BuildingPlanSet} 下所有变体与等级材料消耗总和的 JEI 统一分类窗口。
 * <p>
 * 替代了原先按变体 A/B/C 分流的 {@link AbstractBuildingCostCategory} 及其子类，
 * 直接基于 {@link BuildingPlanSet#variants()} 迭代聚合全部 {@link LevelDef#requiredResources()}。
 * </p>
 */
public class BuildingCostCategory extends BaseMillCategory<BuildingPlanSet> {

    // [醒目新注释] 参照 AbstractBuildingCostCategory 保持完全一致的 8x5 槽位布局与网格渲染参数
    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.BuildingCostCategoryPath,
            BuildingPlanSet.class
    );

    /**
     * [醒目新注释] 缓存上一次绘制的 {@link BuildingPlanSet} 配方对象。
     */
    private BuildingPlanSet lastRecipe;

    /**
     * [醒目新注释] 缓存的分类标题渲染文本组件 {@link Component}。
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
        return MillenaireJeimmKeys.BuildingCostCategory.asComponent();
    }

    /**
     * 遍历 {@link BuildingPlanSet#variants()} 中的所有变体列表，
     * 累加各个 {@link LevelDef#requiredResources()} 中的资源映射，生成全局资源消耗总和。
     *
     * @param buildingPlanSet 当前建筑计划集对象 {@link BuildingPlanSet}
     * @return 物品资源标识符 {@link ResourceLocation} 到所需总数量 {@link Integer} 的映射表
     */
    public static Map<ResourceLocation, Integer> getAggregatedResources(BuildingPlanSet buildingPlanSet) {
        Map<ResourceLocation, Integer> totalResources = new LinkedHashMap<>();
        if (buildingPlanSet == null || buildingPlanSet.variants() == null) {
            return totalResources;
        }
        // [醒目新注释] 遍历 BuildingPlanSet 下所有变体的 LevelDef 列表，合并并累加 requiredResources
        for (List<LevelDef> levelDefs : buildingPlanSet.variants().values()) {
            if (levelDefs == null) continue;
            for (LevelDef levelDef : levelDefs) {
                if (levelDef != null && levelDef.requiredResources() != null) {
                    levelDef.requiredResources().forEach((resLoc, count) -> {
                        if (resLoc != null && count != null && count > 0) {
                            totalResources.merge(resLoc, count, Integer::sum);
                        }
                    });
                }
            }
        }

        return totalResources;
    }

    /**
     * 设置 JEI 槽位布局并填充统计后的资源物品成分。
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe  当前建筑计划集配方对象 {@link BuildingPlanSet}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // [醒目新注释] 严格参照 AbstractBuildingCostCategory 实现，复用既有的 MillIngredientTypes.BUILDING 绑定不可见输出成分
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addIngredient(MillIngredientTypes.BUILDING, recipe);

        Map<ResourceLocation, Integer> aggregatedResources = getAggregatedResources(recipe);
        if (aggregatedResources.isEmpty()) return;

        List<ItemStack> allCosts = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Integer> entry : aggregatedResources.entrySet()) {
            Item item = ItemHelper.resolve(entry.getKey().toString());
            if (item != null && item != Items.AIR) {
                allCosts.add(new ItemStack(item, entry.getValue()));
            }
        }

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

    /**
     * 绘制 JEI 界面标题及槽位背景网格。
     *
     * @param recipe           当前建筑计划集配方对象 {@link BuildingPlanSet}
     * @param recipeSlotsView  JEI 槽位视图 {@link IRecipeSlotsView}
     * @param guiGraphics      GUI 渲染工具 {@link GuiGraphics}
     * @param mouseX           鼠标 X 坐标
     * @param mouseY           鼠标 Y 坐标
     */
    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        // [醒目新注释] 拼合 BuildingPlanSet#nativeName() 与 BuildingPlanSet#id() 构建标题文本
        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            String nativeName = (recipe.nativeName() != null && !recipe.nativeName().isEmpty())
                    ? recipe.nativeName()
                    : (recipe.buildingId() != null ? recipe.buildingId() : "");
            String resIdStr = recipe.id() != null ? recipe.id().toString() : "";
            String titleStr = nativeName + ":" + resIdStr;
            this.cachedDisplayNameComponent = Component.literal(titleStr);
        }

        if (this.cachedDisplayNameComponent != null) {
            guiGraphics.drawString(font, this.cachedDisplayNameComponent, 14, 8, 0x000000, false);
        }

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
// endregion 建筑资源消耗总和 JEI 分类面板实现 (Building Cost Category Implementation)