// region BuildingCategory Implementation
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.BuildingUpgradeCostHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

import java.util.*;

public class BuildingCategory extends BaseMillCategory<BuildingPlanSet> {

    // [醒目新注释] 逻辑依赖的资源路径与 RecipeType 标识抽离至顶层常量
    private static final String MOD_ID = "millenaire";
    private static final String RECIPE_TYPE_ID = "building_details";
    private static final String FALLBACK_ITEM_GLASS_BOTTLE = "minecraft:glass_bottle";
    private static final String COLOR_GOLD = "§6";
    private static final String COLOR_GREEN = "§2";
    private static final String COLOR_BLUE = "§1";
    private static final String COLOR_PURPLE = "§d";

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MOD_ID, RECIPE_TYPE_ID, BuildingPlanSet.class);

    private static final MillIconOnlyRenderer<BuildingPlanSet> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    private final JeiLinkLayout buildingLayout;
    private final JeiLinkBox<VillageType> villageBox;
    private final JeiLinkBox<VillagerType> villagerBox;
    private final JeiLinkBox<ItemStack> resourceBox0;
    private final JeiLinkBox<ItemStack> resourceBox1;
    private final JeiLinkBox<ItemStack> resourceBox2;

    // [醒目新注释] 构造方法中使用 MillenaireJeimmKeys 与 translatableWithFallback 构建动态 Box 标题
    public BuildingCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve(FALLBACK_ITEM_GLASS_BOTTLE) != null ? ItemHelper.resolve(FALLBACK_ITEM_GLASS_BOTTLE) : Items.GLASS_BOTTLE)));

        this.villageBox = new JeiLinkBox<VillageType>(
                Component.translatableWithFallback(MillenaireJeimmKeys.BOX_VILLAGES, "Villages Containing Building").getString()
        ).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);

        this.villagerBox = new JeiLinkBox<VillagerType>(
                Component.translatableWithFallback(MillenaireJeimmKeys.BOX_VILLAGERS, "Resident or Worker Villagers").getString()
        ).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER);

        this.resourceBox0 = new JeiLinkBox<ItemStack>(
                Component.translatableWithFallback(MillenaireJeimmKeys.BOX_VARIANT_COST, "Variant %s Costs", "A").getString()
        ).useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        this.resourceBox1 = new JeiLinkBox<ItemStack>(
                Component.translatableWithFallback(MillenaireJeimmKeys.BOX_VARIANT_COST, "Variant %s Costs", "B").getString()
        ).useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        this.resourceBox2 = new JeiLinkBox<ItemStack>(
                Component.translatableWithFallback(MillenaireJeimmKeys.BOX_VARIANT_COST, "Variant %s Costs", "C").getString()
        ).useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        this.buildingLayout = new JeiLinkLayout(2, 105, 172, 4)
                .addBox(villageBox)
                .addBox(villagerBox)
                .addBox(resourceBox0)
                .addBox(resourceBox1)
                .addBox(resourceBox2);
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback(MillenaireJeimmKeys.CATEGORY_BUILDING_DETAILS, "Building Details");
    }

    // [醒目新注释] 提取建筑布局数据同步方法，防止跨配方卡片渲染时 Layout 数据污染
    /**
     * 根据当前 {@link BuildingPlanSet} 配方强行同步刷新 {@link #buildingLayout} 内部数据。
     *
     * @param recipe 当前建筑计划集配方对象 {@link BuildingPlanSet}
     */
    private void updateLayoutData(BuildingPlanSet recipe) {
        if (buildingLayout == null || recipe == null) return;

        buildingLayout.setBoxData(0, MillenaireAPIHelper.getVillageFormBuilding(recipe));
        buildingLayout.setBoxData(1, MillenaireAPIHelper.getVillagerFormBuilding(recipe));

        List<List<ItemStack>> upgradeCosts = getBuildingCosts(recipe);
        buildingLayout.setBoxData(2, upgradeCosts.size() > 0 ? upgradeCosts.get(0) : List.of());
        buildingLayout.setBoxData(3, upgradeCosts.size() > 1 ? upgradeCosts.get(1) : List.of());
        buildingLayout.setBoxData(4, upgradeCosts.size() > 2 ? upgradeCosts.get(2) : List.of());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.BUILDING, recipe)
                .setCustomRenderer(MillIngredientTypes.BUILDING, ICON_RENDERER);

        if (buildingLayout != null) {
            buildingLayout.Clear();
            updateLayoutData(recipe);

            buildingLayout.setRecipeForBox(0, builder, MillenaireAPIHelper.getVillageFormBuilding(recipe));
            buildingLayout.setRecipeForBox(1, builder, MillenaireAPIHelper.getVillagerFormBuilding(recipe));

            List<List<ItemStack>> upgradeCosts = getBuildingCosts(recipe);
            if (upgradeCosts.size() > 0) {
                buildingLayout.setRecipeForBox(2, builder, upgradeCosts.get(0));
            }
            if (upgradeCosts.size() > 1) {
                buildingLayout.setRecipeForBox(3, builder, upgradeCosts.get(1));
            }
            if (upgradeCosts.size() > 2) {
                buildingLayout.setRecipeForBox(4, builder, upgradeCosts.get(2));
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (buildingLayout != null) {
            // [醒目新注释] 注册 Extras 前强行同步当前配方 Layout
            updateLayoutData(recipe);
            buildingLayout.createRecipeExtras(builder, recipe);
        }
    }

    // [醒目新注释] 替换所有 draw 内的本地化文本渲染，使用 MillenaireJeimmKeys 与 Component.translatableWithFallback
    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();

        drawCommonHeader(guiGraphics, font, displayName, recipe.buildingId(), recipe.category(), mouseX, mouseY);

        String labelPrice = Component.translatableWithFallback(MillenaireJeimmKeys.LABEL_PRICE, "Price").getString();
        String labelRep = Component.translatableWithFallback(MillenaireJeimmKeys.LABEL_REPUTATION, "Reputation Req.").getString();
        String labelMax = Component.translatableWithFallback(MillenaireJeimmKeys.LABEL_MAX_COUNT, "Max Count").getString();
        String labelPriority = Component.translatableWithFallback(MillenaireJeimmKeys.LABEL_MOVE_IN_PRIORITY, "Move-in Priority").getString();
        String labelCulture = Component.translatableWithFallback(MillenaireJeimmKeys.LABEL_CULTURE, "Culture").getString();

        String valMaxCount = Component.translatableWithFallback(MillenaireJeimmKeys.VALUE_MAX_COUNT, "%s Buildings", recipe.maxCount()).getString();
        String cultureName = recipe.culture() != null ? recipe.culture().getPath() : Component.translatableWithFallback(MillenaireJeimmKeys.VALUE_CULTURE_COMMON, "Common").getString();

        drawLabelValue(guiGraphics, font, labelPrice, COLOR_GOLD + recipe.price(), 6, 42);
        drawLabelValue(guiGraphics, font, labelRep, COLOR_GREEN + recipe.reputation(), 6, 52);
        drawLabelValue(guiGraphics, font, labelMax, COLOR_BLUE + valMaxCount, 6, 62);
        drawLabelValue(guiGraphics, font, labelPriority, COLOR_PURPLE + recipe.priorityMoveIn(), 6, 72);
        drawLabelValue(guiGraphics, font, labelCulture, cultureName, 6, 82);

        StringBuilder flags = new StringBuilder();
        if (recipe.isTownHall()) flags.append(Component.translatableWithFallback(MillenaireJeimmKeys.FLAG_TOWN_HALL, "[Town Hall] ").getString());
        if (recipe.isResidential()) flags.append(Component.translatableWithFallback(MillenaireJeimmKeys.FLAG_RESIDENTIAL, "[Residential] ").getString());
        if (recipe.isSubBuilding()) flags.append(Component.translatableWithFallback(MillenaireJeimmKeys.FLAG_SUB_BUILDING, "[Sub Building] ").getString());
        if (recipe.isWallSegment()) flags.append(Component.translatableWithFallback(MillenaireJeimmKeys.FLAG_WALL_SEGMENT, "[Wall] ").getString());
        String flagText = flags.length() > 0 ? flags.toString() : Component.translatableWithFallback(MillenaireJeimmKeys.FLAG_NONE, "None").getString();

        String labelFlags = Component.translatableWithFallback(MillenaireJeimmKeys.LABEL_FLAGS, "Flags").getString();
        drawHoverableText(guiGraphics, font, labelFlags, flagText, 6, 92, mouseX, mouseY);

        // [醒目新注释] 渲染前按当前配方强行同步 Layout 状态
        if (buildingLayout != null) {
            updateLayoutData(recipe);
            buildingLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }

    public static List<List<ItemStack>> getBuildingCosts(BuildingPlanSet building) {
        Map<String, List<ItemStack>> map = BuildingUpgradeCostHelper.getBuildingUpgradeCosts(building);
        if (map != null && !map.isEmpty()) {
            List<List<ItemStack>> result = new ArrayList<>();
            for (List<ItemStack> list : map.values()) {
                if (list != null) {
                    List<ItemStack> cleaned = new ArrayList<>(list);
                    cleaned.removeIf(stack -> stack == null || stack.isEmpty());
                    if (!cleaned.isEmpty()) {
                        result.add(cleaned);
                    }
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
// endregion BuildingCategory Implementation