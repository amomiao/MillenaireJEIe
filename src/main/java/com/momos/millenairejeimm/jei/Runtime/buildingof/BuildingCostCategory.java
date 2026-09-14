// region 建筑资源消耗 JEI 分类面板实现 (Building Cost Category Implementation)
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
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
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 针对单个 {@link BuildingPlanSet.LevelDef} 变体等级的资源消耗 JEI 分类窗口。
 * <p>
 * 保留类名 {@link BuildingCostCategory}，配方泛型调整为内部类 {@link BuildingPlanSet.LevelDef}。
 * </p>
 */
public class BuildingCostCategory extends BaseMillCategory<BuildingPlanSet.LevelDef> {

    // [醒目新注释] 8x5 网格布局参数
    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    // [醒目新注释] 配方泛型类型调整为 BuildingPlanSet.LevelDef
    public static final RecipeType<BuildingPlanSet.LevelDef> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.BuildingCostCategoryPath,
            BuildingPlanSet.LevelDef.class
    );

    /**
     * [醒目新注释] 缓存上一次绘制的 {@link BuildingPlanSet.LevelDef} 配方对象。
     */
    private BuildingPlanSet.LevelDef lastRecipe;

    /**
     * [醒目新注释] 缓存的分类标题渲染文本组件 {@link Component}。
     */
    private Component cachedDisplayNameComponent;

    public BuildingCostCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(Items.CHEST)));
    }

    @Override
    public RecipeType<BuildingPlanSet.LevelDef> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return MillenaireJeimmKeys.BuildingCostCategory.asComponent();
    }

    /**
     * 根据传入的 {@link BuildingPlanSet.LevelDef} 提取其所需的资源消耗映射。
     *
     * @param levelDef 当前建筑等级定义对象 {@link BuildingPlanSet.LevelDef}
     * @return 物品资源标识符 {@link ResourceLocation} 到所需数量 {@link Integer} 的映射表
     */
    public static Map<ResourceLocation, Integer> getAggregatedResources(BuildingPlanSet.LevelDef levelDef) {
        Map<ResourceLocation, Integer> totalResources = new LinkedHashMap<>();
        if (levelDef == null || levelDef.requiredResources() == null) {
            return totalResources;
        }

        // [醒目新注释] 直接提取 LevelDef#requiredResources() 中的资源并按需合并汇总
        levelDef.requiredResources().forEach((resLoc, count) -> {
            if (resLoc != null && count != null && count > 0) {
                totalResources.merge(resLoc, count, Integer::sum);
            }
        });

        return totalResources;
    }

    /**
     * 根据 {@link BuildingPlanSet.LevelDef} 实例反查其所属的父级 {@link BuildingPlanSet}。
     *
     * @param levelDef 等级定义对象 {@link BuildingPlanSet.LevelDef}
     * @return 所属的 {@link BuildingPlanSet} 实例，若未匹配成功则返回 null
     */
    private static BuildingPlanSet getParentBuildingPlanSet(BuildingPlanSet.LevelDef levelDef) {
        if (levelDef == null) return null;
        for (BuildingPlanSet set : MillenaireAPIHelper.getAllBuildings()) {
            if (set != null && set.variants() != null) {
                for (List<BuildingPlanSet.LevelDef> levelList : set.variants().values()) {
                    if (levelList != null && levelList.contains(levelDef)) {
                        return set;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 设置 JEI 槽位布局并填充统计后的资源物品成分。
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe  当前建筑等级定义配方对象 {@link BuildingPlanSet.LevelDef}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet.LevelDef recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        // [醒目新注释] 通过 LevelDef 反查其父级 BuildingPlanSet，并绑定 MillIngredientTypes.BUILDING 不可见输出
        BuildingPlanSet planSet = getParentBuildingPlanSet(recipe);
        if (planSet != null) {
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                    .addIngredient(MillIngredientTypes.BUILDING, planSet);
        }

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
     * @param recipe           当前建筑等级定义配方对象 {@link BuildingPlanSet.LevelDef}
     * @param recipeSlotsView  JEI 槽位视图 {@link IRecipeSlotsView}
     * @param guiGraphics      GUI 渲染工具 {@link GuiGraphics}
     * @param mouseX           鼠标 X 坐标
     * @param mouseY           鼠标 Y 坐标
     */
    @Override
    public void draw(BuildingPlanSet.LevelDef recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        // [醒目新注释] 调用 LevelDef#nativeName() 与 LevelDef#planId() 拼合标题格式 nativeName:planId
        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            String nativeName = recipe.nativeName();
            if (nativeName == null || nativeName.isEmpty()) {
                BuildingPlanSet parentSet = getParentBuildingPlanSet(recipe);
                if (parentSet != null && parentSet.nativeName() != null) {
                    nativeName = parentSet.nativeName();
                } else {
                    nativeName = "";
                }
            }
            ResourceLocation planId = recipe.planId();
            String resIdStr = planId != null ? planId.toString() : "";
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
// endregion 建筑资源消耗 JEI 分类面板实现 (Building Cost Category Implementation)