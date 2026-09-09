package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeimm.jei.type.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.type.BaseMillCategory;
import com.momos.millenairejeimm.jei.type.layout.JeiLinkBox;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.item.ItemHelper;

// region 类定义与构造函数
/**
 * 建筑计划集 {@link BuildingPlanSet} JEI 详情分类，继承自 {@link BaseMillCategory}。
 */
public class BuildingCategory extends BaseMillCategory<BuildingPlanSet> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_details", BuildingPlanSet.class);

    private static final MillIconOnlyRenderer<BuildingPlanSet> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    // [醒目新注释] (1) 建筑关联所属村庄 (顺位 1，数量级 5，1 行)
    private final JeiLinkBox<Object> villageTypeLinks = new JeiLinkBox<>("适用村庄类型", 1);
    // [醒目新注释] (2) 建筑关联居住/工作村民 (顺位 2，数量级 5，1 行)
    private final JeiLinkBox<Object> villagerLinks = new JeiLinkBox<>("居住/工作村民", 1);
    // [醒目新注释] (3) 两级可翻页页签 (顺位 3，高层变体/低层升级消耗，数量级 5-20，2 行)
    private final JeiLinkBox<Object> tabBox = new JeiLinkBox<>("[页签] 变体 / 升级总消耗", 2);

    public BuildingCategory(IGuiHelper guiHelper) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("minecraft:glass_bottle") != null ? ItemHelper.resolve("minecraft:glass_bottle") : Items.GLASS_BOTTLE)));
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.building_details");
    }
// endregion 类定义与构造函数

    // region JEI 槽位与绘制实现
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.BUILDING, recipe)
                .setCustomRenderer(MillIngredientTypes.BUILDING, ICON_RENDERER);
    }

    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();

        drawCommonHeader(guiGraphics, font, displayName, recipe.buildingId(), recipe.category(), mouseX, mouseY);

        // [醒目新注释] 正文从 Y: 42 开始紧凑排布
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

        // [醒目新注释] 顺位渲染相关链接框
        int currentY = 105;
//        // (1) 建筑关联所属村庄
//        villageTypeLinks.render(guiGraphics, font, 2, currentY, 172);
//        currentY += villageTypeLinks.getBoxHeight() + 2;
//
//        // (2) 居住/工作村民
//        villagerLinks.render(guiGraphics, font, 2, currentY, 172);
//        currentY += villagerLinks.getBoxHeight() + 2;
//
//        // (3) 两级可翻页页签
//        tabBox.render(guiGraphics, font, 2, currentY, 172);
//        guiGraphics.drawString(font, Component.literal("§b[变体] §7[升级]"), 125, currentY + 3, 0xFFFFFF, false);
    }
}