// region 建筑 JEI 详情分类面板实现 (Building Category Implementation)
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
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
 * <p>
 * 经过治理升级：收敛 3 个独立的变体建材 Box 为单一自适应变体建材 Box，
 * 标题动态标注变体总数（例如“变体建材 (共 3 个变体)”），总高 221px <= 225px，彻底消灭 76px 画布溢出；
 * 点击 “...” 按钮可一次性展开该建筑全量变体（含汇总页 a_All 与各等级细项），支持 JEI 原生无缝翻页。
 * </p>
 */
public class BuildingCategory extends BaseMillCategory<BuildingPlanSet> {
    private static final String FALLBACK_ITEM_GLASS_BOTTLE = "minecraft:glass_bottle";
    private static final String COLOR_GOLD = "§6";
    private static final String COLOR_GREEN = "§2";
    private static final String COLOR_BLUE = "§1";
    private static final String COLOR_PURPLE = "§d";

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(
            MillIngredientTypes.MOD_ID,
            MillIngredientTypes.BuildingCategoryPath,
            BuildingPlanSet.class
    );

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
    private final JeiLinkBox<ItemStack> variantCostBox;

    private int currentVariantCount = 0;

    public BuildingCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve(FALLBACK_ITEM_GLASS_BOTTLE) != null ? ItemHelper.resolve(FALLBACK_ITEM_GLASS_BOTTLE) : Items.GLASS_BOTTLE)));

        // 1. 关联村庄 Box
        this.villageBox = new JeiLinkBox<VillageType>(
                MillenaireJeimmKeys.VillagesBoxLink.asComponent()
        ).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE)
         .setTargetCategory(BuildingVillageCategory.RECIPE_TYPE);

        // 2. 相关村民 Box
        this.villagerBox = new JeiLinkBox<VillagerType>(
                MillenaireJeimmKeys.VillagersBoxLink.asComponent()
        ).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER)
         .setTargetCategory(BuildingVillagerCategory.RECIPE_TYPE);

        // 3. 变体建材消耗 Box：动态显示变体数量，定向跳转送入全变体配方列表（[A_All, a_0, a_1, B_All, ...]）
        this.variantCostBox = new JeiLinkBox<ItemStack>(
                () -> MillenaireJeimmKeys.VariantCostBoxWithCount_S.asComponent(currentVariantCount)
        ).useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK)
         .setTargetRecipeList(
                 BuildingLevelCostCategory.RECIPE_TYPE,
                 b -> (b instanceof BuildingPlanSet bps) ? BuildingUpgradeCostHelper.getFullVariantLevelsForBuilding(bps) : null
         );

        // 3 个 Box：总高度 105 + 3 * 40 - 4 = 221px <= 225px，彻底消除 76px 溢出
        this.buildingLayout = new JeiLinkLayout(2, 105, 172, 4)
                .addBox(villageBox)
                .addBox(villagerBox)
                .addBox(variantCostBox);
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

        this.currentVariantCount = (recipe.variants() != null) ? recipe.variants().size() : 0;

        buildingLayout.setBoxData(0, MillenaireAPIHelper.getVillageFormBuilding(recipe));
        buildingLayout.setBoxData(1, MillenaireAPIHelper.getVillagerFormBuilding(recipe));
        buildingLayout.setBoxData(2, getPrimaryVariantCosts(recipe));
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
            buildingLayout.setRecipeForBox(2, builder, getPrimaryVariantCosts(recipe));
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

    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        drawCommonHeader(guiGraphics, font, MillenaireLocalizeHelper.getBuildingName(recipe).getString(), recipe.buildingId(), recipe.category(), mouseX, mouseY);

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

    /**
     * 提取当前建筑的首选变体（通常为变体 A）升满所需的材料列表供卡片预览。
     *
     * @param building 建筑计划集
     * @return 首选变体的满级材料用量列表
     */
    public static List<ItemStack> getPrimaryVariantCosts(BuildingPlanSet building) {
        Map<String, List<ItemStack>> map = BuildingUpgradeCostHelper.getBuildingUpgradeCosts(building);
        if (map != null && !map.isEmpty()) {
            for (List<ItemStack> list : map.values()) {
                if (list != null && !list.isEmpty()) {
                    List<ItemStack> cleaned = new ArrayList<>(list);
                    cleaned.removeIf(stack -> stack == null || stack.isEmpty());
                    if (!cleaned.isEmpty()) {
                        return cleaned;
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
// endregion 建筑 JEI 详情分类面板实现 (Building Category Implementation)
