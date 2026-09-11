package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
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

import java.util.List;

public class VillagerCategory extends BaseMillCategory<VillagerType> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create("millenaire", "villager_details", VillagerType.class);

    private static final MillIconOnlyRenderer<VillagerType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    private final JeiLinkLayout villagerLayout;
    private final JeiLinkBox<VillageType> villageBox;
    private final JeiLinkBox<BuildingPlanSet> buildingBox;
    private final JeiLinkBox<ItemStack> bringBackHomeBox;
    private final JeiLinkBox<ItemStack> collectBox;
    private final JeiLinkBox<ItemStack> requiredBox;

    public VillagerCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.BOOK)));

        this.villageBox = new JeiLinkBox<VillageType>("居住村庄").useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);
        this.buildingBox = new JeiLinkBox<BuildingPlanSet>("工作与居住建筑").useJeiMode(ingredientManager, MillIngredientTypes.BUILDING);
        this.bringBackHomeBox = new JeiLinkBox<ItemStack>("带回建筑物资");
        this.collectBox = new JeiLinkBox<ItemStack>("上交中心白名单");
        this.requiredBox = new JeiLinkBox<ItemStack>("职业专属需求");

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
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.villager_details");
    }

    // [醒目新注释] 提取村民布局数据同步方法，避免单例 Category 跨配方渲染错位
    /**
     * 根据当前 {@link VillagerType} 配方强行同步刷新 {@link #villagerLayout} 内部数据。
     *
     * @param recipe 当前村民类型配方对象 {@link VillagerType}
     */
    private void updateLayoutData(VillagerType recipe) {
        if (villagerLayout == null || recipe == null) return;

        List<VillageType> villages = MillenaireAPIHelper.getVillageFormVillager(recipe);
        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillager(recipe);

        List<ItemStack> bringBackHomeItems = recipe.resolvedBringBackHomeGoods().stream()
                .filter(item -> item != null && item != Items.AIR)
                .map(ItemStack::new)
                .toList();

        List<ItemStack> collectItems = recipe.resolvedCollectGoods().stream()
                .map(ItemHelper::resolve)
                .filter(item -> item != null && item != Items.AIR)
                .map(ItemStack::new)
                .toList();

        List<ItemStack> requiredItems = recipe.requiredGoods().isEmpty() ? List.of() :
                recipe.resolvedRequiredGoods().entrySet().stream()
                        .filter(entry -> entry.getKey() != null && entry.getKey() != Items.AIR)
                        .map(entry -> new ItemStack(entry.getKey(), entry.getValue()))
                        .toList();

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

            List<ItemStack> bringBackHomeItems = recipe.resolvedBringBackHomeGoods().stream()
                    .filter(item -> item != null && item != Items.AIR)
                    .map(ItemStack::new)
                    .toList();

            List<ItemStack> collectItems = recipe.resolvedCollectGoods().stream()
                    .map(ItemHelper::resolve)
                    .filter(item -> item != null && item != Items.AIR)
                    .map(ItemStack::new)
                    .toList();

            List<ItemStack> requiredItems = recipe.requiredGoods().isEmpty() ? List.of() :
                    recipe.resolvedRequiredGoods().entrySet().stream()
                            .filter(entry -> entry.getKey() != null && entry.getKey() != Items.AIR)
                            .map(entry -> new ItemStack(entry.getKey(), entry.getValue()))
                            .toList();

            villagerLayout.setRecipeForBox(0, builder, villages);
            villagerLayout.setRecipeForBox(1, builder, buildings);
            villagerLayout.setRecipeForBox(2, builder, bringBackHomeItems);
            villagerLayout.setRecipeForBox(3, builder, collectItems);
            if (!recipe.requiredGoods().isEmpty()) {
                villagerLayout.setRecipeForBox(4, builder, requiredItems);
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (villagerLayout != null) {
            // [醒目新注释] 注册 Extras 前同步数据
            updateLayoutData(recipe);
            villagerLayout.createRecipeExtras(builder, recipe);
        }
    }

    @Override
    public void draw(VillagerType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.id().getPath();
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), culture, mouseX, mouseY);

        String genderText = (recipe.gender() != null ? recipe.gender().name() : "未知") + " (" + (recipe.isChild() ? "幼年" : "成年") + ")";
        drawLabelValue(guiGraphics, font, "性别阶段", genderText, 6, 42);
        drawLabelValue(guiGraphics, font, "最大生命", "§c" + (int) recipe.maxHealth() + " HP", 6, 52);
        drawLabelValue(guiGraphics, font, "基础攻击", "§4" + recipe.baseAttackStrength(), 6, 62);
        drawLabelValue(guiGraphics, font, "雇佣费用", recipe.hiringCost() > 0 ? "§6" + recipe.hiringCost() + " 钱币" : "不可雇佣", 6, 72);
        drawLabelValue(guiGraphics, font, "生成权重", "§1" + recipe.spawnWeight(), 6, 82);

        StringBuilder tagsText = new StringBuilder();
        if (recipe.isRaider()) tagsText.append("[掠夺] ");
        if (recipe.isHostile()) tagsText.append("[敌对] ");
        if (recipe.isHelpInAttacks()) tagsText.append("[协防] ");
        if (recipe.isArcher()) tagsText.append("[弓手] ");
        if (recipe.isNoTeleport()) tagsText.append("[禁传送] ");
        if (recipe.isNoResurrect()) tagsText.append("[不可复活] ");
        String flagsStr = tagsText.length() > 0 ? tagsText.toString() : "平民/日常";

        drawHoverableText(guiGraphics, font, "特性标记", flagsStr, 6, 92, mouseX, mouseY);

        // [醒目新注释] 渲染前强行按当前配方同步 Layout 状态
        if (villagerLayout != null) {
            updateLayoutData(recipe);
            villagerLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}