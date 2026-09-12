package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
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

    public VillagerCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.BOOK)));

        // [醒目新注释] 直接传入 Component 实例而非调用 .getString() 提取固化字符串，使标题可随游戏语言切换动态更新
        JeiLinkBox<VillageType> villageBox = new JeiLinkBox<VillageType>(MillenaireJeimmKeys.getVillagerBoxVillages()).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);
        JeiLinkBox<BuildingPlanSet> buildingBox = new JeiLinkBox<BuildingPlanSet>(MillenaireJeimmKeys.getVillagerBoxBuildings()).useJeiMode(ingredientManager, MillIngredientTypes.BUILDING);
        JeiLinkBox<ItemStack> bringBackHomeBox = new JeiLinkBox<ItemStack>(MillenaireJeimmKeys.getVillagerBoxBringBackHome());
        JeiLinkBox<ItemStack> collectBox = new JeiLinkBox<ItemStack>(MillenaireJeimmKeys.getVillagerBoxCollect());
        JeiLinkBox<ItemStack> requiredBox = new JeiLinkBox<ItemStack>(MillenaireJeimmKeys.getVillagerBoxRequired());

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
            updateLayoutData(recipe);
            villagerLayout.createRecipeExtras(builder, recipe);
        }
    }

    @Override
    public void draw(VillagerType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.id().getPath();
        String culture = recipe.culture() != null ? recipe.culture().getPath() : MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_UNKNOWN_CULTURE, "未知文化").getString();

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), culture, mouseX, mouseY);

        String genderStr = recipe.gender() != null ? recipe.gender().name() : MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_VALUE_UNKNOWN, "未知").getString();
        String ageStageStr = recipe.isChild() ?
                MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_VALUE_CHILD, "幼年").getString() :
                MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_VALUE_ADULT, "成年").getString();
        String genderText = genderStr + " (" + ageStageStr + ")";

        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_LABEL_GENDER_STAGE, "性别阶段").getString(), genderText, 6, 42);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_LABEL_MAX_HEALTH, "最大生命").getString(), "§c" + (int) recipe.maxHealth() + " HP", 6, 52);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_LABEL_BASE_ATTACK, "基础攻击").getString(), "§4" + recipe.baseAttackStrength(), 6, 62);

        String costStr = recipe.hiringCost() > 0 ?
                "§6" + recipe.hiringCost() + " " + MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_VALUE_HIRABLE_COST, "钱币").getString() :
                MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_VALUE_UNHIRABLE, "不可雇佣").getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_LABEL_HIRING_COST, "雇佣费用").getString(), costStr, 6, 72);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_LABEL_SPAWN_WEIGHT, "生成权重").getString(), "§1" + recipe.spawnWeight(), 6, 82);

        StringBuilder tagsText = new StringBuilder();
        if (recipe.isRaider()) tagsText.append("[").append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_FLAG_RAIDER, "掠夺").getString()).append("] ");
        if (recipe.isHostile()) tagsText.append("[").append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_FLAG_HOSTILE, "敌对").getString()).append("] ");
        if (recipe.isHelpInAttacks()) tagsText.append("[").append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_FLAG_HELP_ATTACKS, "协防").getString()).append("] ");
        if (recipe.isArcher()) tagsText.append("[").append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_FLAG_ARCHER, "弓手").getString()).append("] ");
        if (recipe.isNoTeleport()) tagsText.append("[").append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_FLAG_NO_TELEPORT, "禁传送").getString()).append("] ");
        if (recipe.isNoResurrect()) tagsText.append("[").append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_FLAG_NO_RESURRECT, "不可复活").getString()).append("] ");
        String flagsStr = tagsText.length() > 0 ? tagsText.toString() : MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_VALUE_FLAGS_DEFAULT, "平民/日常").getString();

        drawHoverableText(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGER_LABEL_FLAGS, "特性标记").getString(), flagsStr, 6, 92, mouseX, mouseY);

        if (villagerLayout != null) {
            updateLayoutData(recipe);
            villagerLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}