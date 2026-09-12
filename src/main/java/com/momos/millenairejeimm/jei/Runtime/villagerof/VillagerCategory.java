package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
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
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

import java.util.List;

/**
 * 村民 {@link VillagerType} JEI 详情主分类面板。
 */
public class VillagerCategory extends BaseMillCategory<VillagerType> {

    // region 静态字段与调试输出辅助方法

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create("millenaire", "villager_details", VillagerType.class);

    private static final MillIconOnlyRenderer<VillagerType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    // endregion 静态字段与调试输出辅助方法

    // region 成员变量与构造函数

    private final JeiLinkLayout villagerLayout;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public VillagerCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.BOOK)));

        // [醒目新注释] 必须与 BuildingCategory 保持一致，后三个 ItemStack 框必须绑定 VanillaTypes.ITEM_STACK
        JeiLinkBox<VillageType> villageBox = new JeiLinkBox<VillageType>(MillenaireJeimmKeys.VillageBoxLink.asComponent())
                .useJeiMode(ingredientManager, (IIngredientType) MillIngredientTypes.VILLAGE);

        JeiLinkBox<BuildingPlanSet> buildingBox = new JeiLinkBox<BuildingPlanSet>(MillenaireJeimmKeys.BuildingBoxLink.asComponent())
                .useJeiMode(ingredientManager, (IIngredientType) MillIngredientTypes.BUILDING);

        JeiLinkBox<ItemStack> bringBackHomeBox = new JeiLinkBox<ItemStack>(MillenaireJeimmKeys.BringBackHomeBoxLink.asComponent())
                .useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        JeiLinkBox<ItemStack> collectBox = new JeiLinkBox<ItemStack>(MillenaireJeimmKeys.CollectBoxLink.asComponent())
                .useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        JeiLinkBox<ItemStack> requiredBox = new JeiLinkBox<ItemStack>(MillenaireJeimmKeys.RequiredBoxLink.asComponent())
                .useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        this.villagerLayout = new JeiLinkLayout(2, 105, 172, 4)
                .addBox(villageBox)
                .addBox(buildingBox)
                .addBox(bringBackHomeBox)
                .addBox(collectBox)
                .addBox(requiredBox);
    }

    @Override
    public RecipeType<VillagerType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() { return MillenaireJeimmKeys.VillagerCategory.asComponent(); }

    // endregion 成员变量与构造函数

    // region 数据转换与安全提取辅助方法

    /**
     * 安全提取村民带回家中的物品清单并转换为 {@link ItemStack} 列表。
     *
     * @param recipe 村民配方 {@link VillagerType}
     * @return 转换后的 {@link ItemStack} 列表，非空
     */
    private List<ItemStack> extractBringBackHomeItems(VillagerType recipe) {
        if (recipe == null || recipe.resolvedBringBackHomeGoods() == null) {
            return List.of();
        }
        return recipe.resolvedBringBackHomeGoods().stream()
                .filter(item -> item != null && item != Items.AIR)
                .map(ItemStack::new)
                .toList();
    }

    /**
     * 安全提取村民收集的物品清单并转换为 {@link ItemStack} 列表。
     *
     * @param recipe 村民配方 {@link VillagerType}
     * @return 转换后的 {@link ItemStack} 列表，非空
     */
    private List<ItemStack> extractCollectItems(VillagerType recipe) {
        if (recipe == null || recipe.resolvedCollectGoods() == null) {
            return List.of();
        }
        return recipe.resolvedCollectGoods().stream()
                .filter(loc -> loc != null)
                .map(loc -> {
                    Item item = ItemHelper.resolve(loc.toString());
                    if (item == null || item == Items.AIR) {
                        item = BuiltInRegistries.ITEM.get(loc);
                    }
                    return (item != null && item != Items.AIR) ? new ItemStack(item) : ItemStack.EMPTY;
                })
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    /**
     * 安全提取村民生存/建房所需物品清单并转换为包含数量的 {@link ItemStack} 列表。
     *
     * @param recipe 村民配方 {@link VillagerType}
     * @return 转换后的 {@link ItemStack} 列表，包含具体堆叠数量
     */
    private List<ItemStack> extractRequiredItems(VillagerType recipe) {
        if (recipe == null || recipe.requiredGoods() == null || recipe.requiredGoods().isEmpty() || recipe.resolvedRequiredGoods() == null) {
            return List.of();
        }
        return recipe.resolvedRequiredGoods().entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getKey() != Items.AIR)
                .map(entry -> new ItemStack(entry.getKey(), entry.getValue() != null ? entry.getValue() : 1))
                .toList();
    }

    // endregion 数据转换与安全提取辅助方法

    // region JEI 布局与 Recipe 注册逻辑

    private void updateLayoutData(VillagerType recipe) {
        if (villagerLayout == null || recipe == null) return;

        List<VillageType> villages = MillenaireAPIHelper.getVillageFormVillager(recipe);
        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillager(recipe);

        List<ItemStack> bringBackHomeItems = extractBringBackHomeItems(recipe);
        List<ItemStack> collectItems = extractCollectItems(recipe);
        List<ItemStack> requiredItems = extractRequiredItems(recipe);

        villagerLayout.setBoxData(0, villages);
        villagerLayout.setBoxData(1, buildings);
        villagerLayout.setBoxData(2, bringBackHomeItems);
        villagerLayout.setBoxData(3, collectItems);
        villagerLayout.setBoxData(4, requiredItems);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGER, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGER, ICON_RENDERER);

        if (villagerLayout != null) {
            villagerLayout.Clear();
            updateLayoutData(recipe);

            List<VillageType> villages = MillenaireAPIHelper.getVillageFormVillager(recipe);
            List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillager(recipe);

            List<ItemStack> bringBackHomeItems = extractBringBackHomeItems(recipe);
            List<ItemStack> collectItems = extractCollectItems(recipe);
            List<ItemStack> requiredItems = extractRequiredItems(recipe);

            villagerLayout.setRecipeForBox(0, builder, villages);
            villagerLayout.setRecipeForBox(1, builder, buildings);
            villagerLayout.setRecipeForBox(2, builder, bringBackHomeItems);
            villagerLayout.setRecipeForBox(3, builder, collectItems);
            if (!requiredItems.isEmpty()) {
                villagerLayout.setRecipeForBox(4, builder, requiredItems);
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (villagerLayout != null) {
            updateLayoutData(recipe);
            villagerLayout.createRecipeExtras(builder, recipe);
        }
    }

    // endregion JEI 布局与 Recipe 注册逻辑

    // region 页面渲染方法

    @Override
    public void draw(VillagerType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.id().getPath();

        String culture = recipe.culture() != null ? recipe.culture().getPath() : MillenaireJeimmKeys.UnknownCultureValue.asComponent().getString();

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), culture, mouseX, mouseY);

        String genderStr = recipe.gender() != null ? recipe.gender().name() : MillenaireJeimmKeys.UnknownGenderValue.asComponent().getString();
        String ageStageStr = recipe.isChild() ?
                MillenaireJeimmKeys.ChildValue.asComponent().getString() :
                MillenaireJeimmKeys.AdultValue.asComponent().getString();
        String genderText = genderStr + " (" + ageStageStr + ")";

        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.GenderStageLabel.asComponent(), genderText, 6, 42);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.MaxHealthLabel.asComponent(), "§c" + MillenaireJeimmKeys.HpValue_D.asComponent((int) recipe.maxHealth()).getString(), 6, 52);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.BaseAttackLabel.asComponent(), "§4" + recipe.baseAttackStrength(), 6, 62);

        String costStr = recipe.hiringCost() > 0 ?
                "§6" + MillenaireJeimmKeys.CoinsValue_D.asComponent(recipe.hiringCost()).getString() :
                MillenaireJeimmKeys.UnhirableValue.asComponent().getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.HiringCostLabel.asComponent(), costStr, 6, 72);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.SpawnWeightLabel.asComponent(), "§1" + recipe.spawnWeight(), 6, 82);

        StringBuilder tagsText = new StringBuilder();
        if (recipe.isRaider()) tagsText.append(MillenaireJeimmKeys.RaiderValue.asComponent().getString());
        if (recipe.isHostile()) tagsText.append(MillenaireJeimmKeys.HostileValue.asComponent().getString());
        if (recipe.isHelpInAttacks()) tagsText.append(MillenaireJeimmKeys.HelpAttacksValue.asComponent().getString());
        if (recipe.isArcher()) tagsText.append(MillenaireJeimmKeys.ArcherValue.asComponent().getString());
        if (recipe.isNoTeleport()) tagsText.append(MillenaireJeimmKeys.NoTeleportValue.asComponent().getString());
        if (recipe.isNoResurrect()) tagsText.append(MillenaireJeimmKeys.NoResurrectValue.asComponent().getString());
        String flagsStr = tagsText.length() > 0 ? tagsText.toString() : MillenaireJeimmKeys.CivilianValue.asComponent().getString();

        drawHoverableText(guiGraphics, font, MillenaireJeimmKeys.FlagsLabel.asComponent(), flagsStr, 6, 92, mouseX, mouseY);

        if (villagerLayout != null) {
            updateLayoutData(recipe);
            villagerLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }

    // endregion 页面渲染方法
}