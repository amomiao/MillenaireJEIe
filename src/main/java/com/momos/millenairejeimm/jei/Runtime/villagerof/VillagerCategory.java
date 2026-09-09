package com.momos.millenairejeimm.jei.Runtime.villagerof;

import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
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
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

// region 类定义与构造函数

/**
 * 村民类型 {@link VillagerType} JEI 详情分类，继承自 {@link BaseMillCategory}。
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

    public VillagerCategory(IGuiHelper guiHelper) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.BOOK)));
    }

    @Override
    public RecipeType<VillagerType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.villager_details");
    }
// endregion 类定义与构造函数

    // region JEI 槽位与绘制实现
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGER, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGER, ICON_RENDERER);
    }

    @Override
    public void draw(VillagerType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.id().getPath();
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), culture, mouseX, mouseY);

        // [醒目新注释] 统一从 Y: 42 开始罗列
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

        // [醒目新注释] 顺位渲染相关链接框
        int currentY = 105;
        // 生成村庄
//        villageLinks.render(guiGraphics, font, 2, currentY, 172);
//        currentY += villageLinks.getBoxHeight() + 2;
//
//        // 关联建筑
//        buildingLinks.render(guiGraphics, font, 2, currentY, 172);
    }
}