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

/**
 * 村民类型 {@link VillagerType} JEI 主分类，继承自 {@link BaseMillCategory}。
 */
public class VillagerCategory extends BaseMillCategory<VillagerType> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create("millenaire", "villager_details", VillagerType.class);

    private static final MillIconOnlyRenderer<VillagerType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    // [醒目新注释] 关联布局与村民相关的 5 个 LinkBox 组件定义
    private final JeiLinkLayout villagerLayout;
    private final JeiLinkBox<VillageType> villageBox;
    private final JeiLinkBox<BuildingPlanSet> buildingBox;
    private final JeiLinkBox<ItemStack> bringBackHomeBox;
    private final JeiLinkBox<ItemStack> collectBox;
    private final JeiLinkBox<ItemStack> requiredBox;

    /**
     * [醒目新注释] 构造函数升级：引入 {@link IIngredientManager} 以支持原生 LinkBox 动态联动
     *
     * @param guiHelper JEI GUI 辅助接口 {@link IGuiHelper}
     * @param ingredientManager JEI 成分管理器 {@link IIngredientManager}
     */
    public VillagerCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.BOOK)));

        // [醒目新注释] 1. 生成/居住村庄 LinkBox (Ingredient: VILLAGE)
        this.villageBox = new JeiLinkBox<VillageType>("居住村庄").useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);
        // [醒目新注释] 2. 关联工作/居住建筑 LinkBox (Ingredient: BUILDING)
        this.buildingBox = new JeiLinkBox<BuildingPlanSet>("工作与居住建筑").useJeiMode(ingredientManager, MillIngredientTypes.BUILDING);
        // [醒目新注释] 3. 产物带回建筑 LinkBox
        this.bringBackHomeBox = new JeiLinkBox<ItemStack>("带回建筑物资");
        // [醒目新注释] 4. 上交中心白名单 LinkBox
        this.collectBox = new JeiLinkBox<ItemStack>("上交中心白名单");
        // [醒目新注释] 5. 职业专属需求 LinkBox
        this.requiredBox = new JeiLinkBox<ItemStack>("职业专属需求");

        // [醒目新注释] 初始化链接布局，起点坐标 Y: 105，总宽度: 172，项间距: 4
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

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGER, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGER, ICON_RENDERER);

        // [醒目新注释] 调试输出：在客户端聊天栏向玩家打印 VillagerType 的 8 个相关数据结构 API 内容
        if (Minecraft.getInstance().player != null) {
            var player = Minecraft.getInstance().player;
            player.sendSystemMessage(Component.literal("§e=== VillagerType 调试数据: " + recipe.id() + " ==="));
            player.sendSystemMessage(Component.literal("§b1. id(): §r" + recipe.id()));
            player.sendSystemMessage(Component.literal("§b2. culture(): §r" + recipe.culture()));
            player.sendSystemMessage(Component.literal("§b3. bringBackHomeGoods(): §r" + recipe.bringBackHomeGoods()));
            player.sendSystemMessage(Component.literal("§b4. resolvedBringBackHomeGoods(): §r" + recipe.resolvedBringBackHomeGoods()));
            player.sendSystemMessage(Component.literal("§b5. collectGoods(): §r" + recipe.collectGoods()));
            player.sendSystemMessage(Component.literal("§b6. resolvedCollectGoods(): §r" + recipe.resolvedCollectGoods()));
            player.sendSystemMessage(Component.literal("§b7. requiredGoods(): §r" + recipe.requiredGoods()));
            player.sendSystemMessage(Component.literal("§b8. resolvedRequiredGoods(): §r" + recipe.resolvedRequiredGoods()));
        }

        // [醒目新注释] 绑定动态联动 LinkBox 数据，遵守调查报告，使用安全 resolved 系列 API
        if (villagerLayout != null) {
            villagerLayout.Clear();

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

            // [醒目新注释] 根据调查报告，以 requiredGoods().isEmpty() 作为前置过滤器剔除全局通用饮食库
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

        if (villagerLayout != null) {
            villagerLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}