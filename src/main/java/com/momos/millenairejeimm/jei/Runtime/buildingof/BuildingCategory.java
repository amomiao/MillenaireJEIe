package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.BuildingUpgradeCostHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
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

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_details", BuildingPlanSet.class);

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

    public BuildingCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("minecraft:glass_bottle") != null ? ItemHelper.resolve("minecraft:glass_bottle") : Items.GLASS_BOTTLE)));

        this.villageBox = new JeiLinkBox<VillageType>("包含此建筑的村庄").useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);
        this.villagerBox = new JeiLinkBox<VillagerType>("居住或工作村民").useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER);
        this.resourceBox0 = new JeiLinkBox<ItemStack>("变体 A 消耗").useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);
        this.resourceBox1 = new JeiLinkBox<ItemStack>("变体 B 消耗").useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);
        this.resourceBox2 = new JeiLinkBox<ItemStack>("变体 C 消耗").useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

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
        return Component.translatable("gui.millenairejeim.category.building_details");
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

    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();

        drawCommonHeader(guiGraphics, font, displayName, recipe.buildingId(), recipe.category(), mouseX, mouseY);

        drawLabelValue(guiGraphics, font, "造价", "§6" + recipe.price(), 6, 42);
        drawLabelValue(guiGraphics, font, "声望需求", "§2" + recipe.reputation(), 6, 52);
        drawLabelValue(guiGraphics, font, "建造上限", "§1" + recipe.maxCount() + " 座", 6, 62);
        drawLabelValue(guiGraphics, font, "搬入优先级", "§d" + recipe.priorityMoveIn(), 6, 72);
        drawLabelValue(guiGraphics, font, "关联文化", recipe.culture() != null ? recipe.culture().getPath() : "通用", 6, 82);

        StringBuilder flags = new StringBuilder();
        if (recipe.isTownHall()) flags.append("[市镇厅] ");
        if (recipe.isResidential()) flags.append("[住宅] ");
        if (recipe.isSubBuilding()) flags.append("[子建筑] ");
        if (recipe.isWallSegment()) flags.append("[城墙] ");
        String flagText = flags.length() > 0 ? flags.toString() : "无";

        drawHoverableText(guiGraphics, font, "标记特性", flagText, 6, 92, mouseX, mouseY);

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