package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeimm.jei.type.BaseMillCategory;
import com.momos.millenairejeimm.jei.type.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
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
import org.millenaire.culture.VillageType;
import org.millenaire.item.ItemHelper;

// region 类定义与构造函数
/**
 * 村庄类型 {@link VillageType} JEI 详情分类，继承自 {@link BaseMillCategory}。
 */
public class VillageCategory extends BaseMillCategory<VillageType> {
    // static
    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create("millenaire", "village_details", VillageType.class);
    private static final MillIconOnlyRenderer<VillageType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    public VillageCategory(IGuiHelper guiHelper) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.MAP)));
    }

    @Override
    public RecipeType<VillageType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.village_details");
    }
// endregion 类定义与构造函数

    // region JEI 槽位与绘制实现
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGE, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGE, ICON_RENDERER);
    }

    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.name() != null && !recipe.name().isEmpty() ? recipe.name() : recipe.id().getPath();
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), culture, mouseX, mouseY);

        // [醒目新注释] 统一从 Y: 42 开始罗列
        String typeStr = recipe.isMarvel() ? "奇观" : (recipe.isHamlet() ? "哈姆雷特/子村" : (recipe.loneBuilding() ? "独立建筑" : "常规村庄"));
        drawLabelValue(guiGraphics, font, "类型分类", typeStr, 6, 42);
        drawLabelValue(guiGraphics, font, "作用半径", "§2" + recipe.radius() + "m", 6, 52);
        drawLabelValue(guiGraphics, font, "并行建造上限", "§1" + recipe.maxSimultaneousConstructions(), 6, 62);
        drawLabelValue(guiGraphics, font, "生成权重", "§6" + recipe.weight(), 6, 72);
        drawLabelValue(guiGraphics, font, "距出生点最小距离", "§c" + recipe.minDistanceFromSpawn() + "m", 6, 82);

        StringBuilder wallInfo = new StringBuilder();
        if (recipe.outerWallType() != null) wallInfo.append("[外城墙] ");
        if (recipe.innerWallType() != null) wallInfo.append("[内城墙] ");
        if (recipe.carriesRaid()) wallInfo.append("[袭营机制] ");
        String defenseText = wallInfo.length() > 0 ? wallInfo.toString() : "无防御结构";

        drawHoverableText(guiGraphics, font, "防御属性", defenseText, 6, 92, mouseX, mouseY);
        drawLabelValue(guiGraphics, font, "控制规则", recipe.playerControlled() ? "§d玩家控制" : "§8自主发展", 6, 102);

        // [醒目新注释] 顺位渲染相关链接框
        int currentY = 115;

        // 村庄内建筑
        // buildingLinks.render(guiGraphics, font, 2, currentY, 172);
    }

}