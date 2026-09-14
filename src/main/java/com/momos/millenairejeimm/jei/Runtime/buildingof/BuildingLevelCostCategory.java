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
 * 针对单个 {@link BuildingPlanSet.LevelDef} 变体等级独立资源消耗的 JEI 分类窗口。
 */
public class BuildingLevelCostCategory extends BaseMillCategory<BuildingPlanSet.LevelDef> {

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

    public BuildingLevelCostCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
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
     * [醒目新注释] 从 ResourceLocation 的 Path 中提取从倒数第二个 '_' 开始截断的变体与等级标识（如从 xx_a_0 截取 a_0）。
     *
     * @param planId 建筑等级配方标识符 {@link ResourceLocation}
     * @return 截取后的变体与等级后缀（例如 "a_0"）
     */
    public static String extractVariantAndLevelSuffix(ResourceLocation planId) {
        if (planId == null) return "";
        String path = planId.getPath();
        int lastUnderscore = path.lastIndexOf('_');
        if (lastUnderscore > 0) {
            int secondLastUnderscore = path.lastIndexOf('_', lastUnderscore - 1);
            if (secondLastUnderscore >= 0) {
                return path.substring(secondLastUnderscore + 1);
            }
        }
        return path;
    }

    /**
     * [醒目新注释] 提取当前 {@link BuildingPlanSet.LevelDef} 自身的独立资源消耗映射（不再计算前面等级的累加）。
     *
     * @param levelDef 当前目标建筑等级定义对象 {@link BuildingPlanSet.LevelDef}
     * @return 物品资源标识符 {@link ResourceLocation} 到当前等级所需数量 {@link Integer} 的映射表
     */
    public static Map<ResourceLocation, Integer> getAggregatedResources(BuildingPlanSet.LevelDef levelDef) {
        Map<ResourceLocation, Integer> totalResources = new LinkedHashMap<>();
        if (levelDef == null || levelDef.requiredResources() == null) {
            return totalResources;
        }

        // [醒目新注释] 仅提取当前 LevelDef 自身的 requiredResources
        levelDef.requiredResources().forEach((resLoc, count) -> {
            if (resLoc != null && count != null && count > 0) {
                totalResources.merge(resLoc, count, Integer::sum);
            }
        });

        return totalResources;
    }

    /**
     * [醒目新注释] 根据 {@link BuildingPlanSet.LevelDef} 实例反查其所属的父级 {@link BuildingPlanSet}。
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

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet.LevelDef recipe, IFocusGroup focuses) {
        if (recipe == null) return;

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
    public void draw(BuildingPlanSet.LevelDef recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;

            String nativeName = recipe.nativeName();
            BuildingPlanSet parentSet = getParentBuildingPlanSet(recipe);
            if ((nativeName == null || nativeName.isEmpty()) && parentSet != null) {
                nativeName = parentSet.nativeName();
            }
            if (nativeName == null) {
                nativeName = "";
            }

            String suffix = extractVariantAndLevelSuffix(recipe.planId());
            String titleStr = nativeName.isEmpty() ? suffix : nativeName + ":" + suffix;

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