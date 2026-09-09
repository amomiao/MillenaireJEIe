package com.momos.millenairejeimm.jei.buildingof;

import com.mojang.blaze3d.platform.InputConstants;
import com.momos.millenairejeimm.jei.DebugGuiUtils;
import com.momos.millenairejeimm.jei.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.item.ItemHelper;

// region 类的定义与静态渲染器配置
/**
 * 千年村庄建筑计划集 {@link BuildingPlanSet} JEI 详情分类页面。
 */
public class BuildingCategory implements IRecipeCategory<BuildingPlanSet> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_details", BuildingPlanSet.class);

    private final IDrawable background;
    private final IDrawable icon;

    private static final MillIconOnlyRenderer<BuildingPlanSet> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });
// endregion 类的定义与静态渲染器配置

// region 构造函数与 JEI 元数据接口回调
    /**
     * 构造 JEI 建筑分类 Category 面板。
     *
     * @param guiHelper JEI GUI 界面助手 {@link IGuiHelper}
     */
    public BuildingCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 移除暗色控制台后，将页面高度缩减回 80px 紧凑尺寸
        this.background = guiHelper.createBlankDrawable(160, 80);

        Item buildingIconItem = ItemHelper.resolve("minecraft:glass_bottle");
        ItemStack iconStack = (buildingIconItem != null) ? new ItemStack(buildingIconItem) : new ItemStack(Items.GLASS_BOTTLE);
        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.building_details");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        // [醒目新注释] 高度同步调整为 80px
        return 80;
    }
// endregion 构造函数与 JEI 元数据接口回调

    // region JEI 槽位构建 (setRecipe)
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.BUILDING, recipe)
                .setCustomRenderer(MillIngredientTypes.BUILDING, ICON_RENDERER);
    }
// endregion JEI 槽位构建 (setRecipe)

    // region Category 界面绘制主逻辑 (draw)
    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        // 1. 顶部 Header 区 (Y: 6~22)
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);
        guiGraphics.drawString(font, Component.literal("§8ID: " + recipe.buildingId() + " | 类别: " + recipe.category()), 28, 17, 0x555555, false);

        // [醒目新注释] 绘制标题行右侧的极简 JSON Dump 开发者徽章按钮 (X: 128, Y: 6)
        DebugGuiUtils.drawJsonDevButton(guiGraphics, font, 128, 6, mouseX, mouseY);

        guiGraphics.fill(6, 27, 154, 28, 0xFFCCCCCC);

        // 2. 基础数值参数网格 (Y: 30~58)
        guiGraphics.drawString(font, Component.literal("造价: §6" + recipe.price() + "§r | 声望需求: §2" + recipe.reputation()), 6, 30, 0x333333, false);
        guiGraphics.drawString(font, Component.literal("最大上限: §1" + recipe.maxCount() + " 座§r | 搬入优先级: §d" + recipe.priorityMoveIn()), 6, 41, 0x333333, false);
        guiGraphics.drawString(font, Component.literal("关联文化: §8" + (recipe.culture() != null ? recipe.culture() : "通用/未指定")), 6, 52, 0x333333, false);

        // 3. 布尔属性状态徽章 Badges (Y: 64~76)
        StringBuilder flagBadges = new StringBuilder("标记: ");
        flagBadges.append(recipe.isTownHall() ? "§c[市镇厅] " : "§8[非市镇厅] ");
        flagBadges.append(recipe.isResidential() ? "§a[住宅] " : "§8[非住宅] ");
        flagBadges.append(recipe.isSubBuilding() ? "§e[子建筑] " : "");
        flagBadges.append(recipe.isWallSegment() ? "§7[城墙] " : "");
        guiGraphics.drawString(font, Component.literal(flagBadges.toString()), 6, 64, 0x333333, false);

        // [醒目新注释] 已清理原先的 78~132 黑色控制台方块与 renderHoverInspector 悬浮文本逻辑
    }
// endregion Category 界面绘制主逻辑 (draw)

    // region 用户交互与点击响应逻辑 (handleInput)
    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(BuildingPlanSet recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            return DebugGuiUtils.handleJsonButtonClick(recipe, mouseX, mouseY, 128, 6);
        }
        return false;
    }
// endregion 用户交互与点击响应逻辑 (handleInput)
}