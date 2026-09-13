// region 建筑 JEI 详情分类面板实现 (Building Category Implementation)
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

/**
 * 建筑 {@link BuildingPlanSet} JEI 详情主分类面板。
 */
public class BuildingCategory extends BaseMillCategory<BuildingPlanSet> {
    private static final String FALLBACK_ITEM_GLASS_BOTTLE = "minecraft:glass_bottle";
    private static final String COLOR_GOLD = "§6";
    private static final String COLOR_GREEN = "§2";
    private static final String COLOR_BLUE = "§1";
    private static final String COLOR_PURPLE = "§d";

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.BuildingCategoryPath, BuildingPlanSet.class);

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

    // [醒目新注释] 构造函数中所有 Box 标题统一改用首字母大写驼峰格式的 {@link MillenaireJeimmKeys} 常量并配合 .asComponent()，替换旧版 translatableWithFallback 字符串
    public BuildingCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve(FALLBACK_ITEM_GLASS_BOTTLE) != null ? ItemHelper.resolve(FALLBACK_ITEM_GLASS_BOTTLE) : Items.GLASS_BOTTLE)));

        this.villageBox = new JeiLinkBox<VillageType>(
                MillenaireJeimmKeys.VillagesBoxLink.asComponent()
        ).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);

        this.villagerBox = new JeiLinkBox<VillagerType>(
                MillenaireJeimmKeys.VillagersBoxLink.asComponent()
        ).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER);

        this.resourceBox0 = new JeiLinkBox<ItemStack>(
                MillenaireJeimmKeys.VariantCostBoxLink_S.asComponent("A")
        ).useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        this.resourceBox1 = new JeiLinkBox<ItemStack>(
                MillenaireJeimmKeys.VariantCostBoxLink_S.asComponent("B")
        ).useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        this.resourceBox2 = new JeiLinkBox<ItemStack>(
                MillenaireJeimmKeys.VariantCostBoxLink_S.asComponent("C")
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
        return MillenaireJeimmKeys.BuildingCategory.asComponent();
    }

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
            updateLayoutData(recipe);
            buildingLayout.createRecipeExtras(builder, recipe);
        }
    }

    // [醒目新注释] 重构 draw 内本地化逻辑：全面对标 CultureCategory 风格，使用大驼峰命名（如 PriceLabel, MaxCountValue, FlagsLabel 等）配合 .asComponent() / .getString()
    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();

        drawCommonHeader(guiGraphics, font, displayName, recipe.buildingId(), recipe.category(), mouseX, mouseY);

        // 1. Price
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.PriceLabel.asComponent(), COLOR_GOLD + recipe.price(), 6, 42);

        // 2. Reputation
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.ReputationLabel.asComponent(), COLOR_GREEN + recipe.reputation(), 6, 52);

        // 3. MaxCount
        String valMaxCount = MillenaireJeimmKeys.MaxCountValue_D.asComponent(recipe.maxCount()).getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.MaxCountLabel.asComponent(), COLOR_BLUE + valMaxCount, 6, 62);

        // 4. MoveInPriority
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.MoveInPriorityLabel.asComponent(), COLOR_PURPLE + recipe.priorityMoveIn(), 6, 72);

        // 5. Culture
        String cultureName = recipe.culture() != null
                ? recipe.culture().getPath()
                : MillenaireJeimmKeys.CommonCultureValue.asComponent().getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.CultureLabel.asComponent(), cultureName, 6, 82);

        // 6. Flags
        StringBuilder flags = new StringBuilder();
        if (recipe.isTownHall()) flags.append("[").append(MillenaireJeimmKeys.TownHallFlag.asComponent().getString()).append("] ");
        if (recipe.isResidential()) flags.append("[").append(MillenaireJeimmKeys.ResidentialFlag.asComponent().getString()).append("] ");
        if (recipe.isSubBuilding()) flags.append("[").append(MillenaireJeimmKeys.SubBuildingFlag.asComponent().getString()).append("] ");
        if (recipe.isWallSegment()) flags.append("[").append(MillenaireJeimmKeys.WallSegmentFlag.asComponent().getString()).append("] ");

        String flagText = flags.length() > 0
                ? flags.toString().trim()
                : MillenaireJeimmKeys.NoneFlag.asComponent().getString();

        drawHoverableText(guiGraphics, font, MillenaireJeimmKeys.FlagsLabel.asComponent(), Component.literal(flagText), 6, 92, mouseX, mouseY);

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
// endregion 建筑 JEI 详情分类面板实现 (Building Category Implementation)