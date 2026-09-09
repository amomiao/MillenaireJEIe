package com.momos.millenairejeimm.jei.villageof;

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
import org.millenaire.culture.VillageType;
import org.millenaire.item.ItemHelper;

// region 类的定义与静态渲染器
/**
 * 千年村庄类型 {@link VillageType} 的 JEI 详情分类页面。
 */
public class VillageCategory implements IRecipeCategory<VillageType> {

    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create("millenaire", "village_details", VillageType.class);

    private final IDrawable background;
    private final IDrawable icon;

    private static final MillIconOnlyRenderer<VillageType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });
// endregion 类的定义与静态渲染器

    // region 构造函数与 JEI 分类初始化
    public VillageCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 移除底部控制台后，根据实际面板文字高度调整为 105px
        this.background = guiHelper.createBlankDrawable(160, 105);

        Item travelBookItem = ItemHelper.resolve("millenaire:travel_book");
        if (travelBookItem == null) {
            travelBookItem = ItemHelper.resolve("millenaire:norman_amulet");
        }
        ItemStack iconStack = (travelBookItem != null) ? new ItemStack(travelBookItem) : new ItemStack(Items.MAP);

        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<VillageType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.village_details");
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
        // [醒目新注释] 同步调整面板高度为 105px
        return 105;
    }
// endregion 构造函数与 JEI 分类初始化

    // region JEI 槽位与成分配置 (setRecipe)
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGE, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGE, ICON_RENDERER);
    }
// endregion JEI 槽位与成分配置 (setRecipe)

    // region 面板渲染 (draw)
    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        // 1. 名称与 JSON 按钮
        String displayName = recipe.name() != null && !recipe.name().isEmpty() ? recipe.name() : recipe.id().getPath();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);

        DebugGuiUtils.drawJsonDevButton(guiGraphics, font, 128, 6, mouseX, mouseY);

        // 2. 所属文化与村庄类型
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";
        String typeStr = recipe.isMarvel() ? "奇观" : (recipe.isHamlet() ? "哈姆雷特/子村" : (recipe.loneBuilding() ? "独立建筑" : "常规村庄"));
        guiGraphics.drawString(font, Component.literal("§8" + culture + " | " + typeStr), 28, 18, 0x444444, false);

        // 3. 分隔线
        guiGraphics.fill(6, 28, 154, 29, 0xFFCCCCCC);

        // 4. 范围与同时建造数
        guiGraphics.drawString(font, Component.literal("作用半径: §2" + recipe.radius() + "m §r| 并行建造上限: §1" + recipe.maxSimultaneousConstructions()), 6, 34, 0x333333, false);

        // 5. 生成权重与出生点最小距离
        guiGraphics.drawString(font, Component.literal("生成权重: §6" + recipe.weight() + " §r| 距出生点: §c" + recipe.minDistanceFromSpawn() + "m"), 6, 48, 0x333333, false);

        // 6. 城墙防护与袭营机制
        StringBuilder wallInfo = new StringBuilder("防御属性: ");
        if (recipe.outerWallType() != null) wallInfo.append("§2[外城墙] ");
        if (recipe.innerWallType() != null) wallInfo.append("§a[内城墙] ");
        if (recipe.carriesRaid()) wallInfo.append("§c[袭营] ");
        if (recipe.outerWallType() == null && recipe.innerWallType() == null && !recipe.carriesRaid()) {
            wallInfo.append("§8无防具/无袭营");
        }
        guiGraphics.drawString(font, Component.literal(wallInfo.toString()), 6, 62, 0x333333, false);

        // 7. 玩家控制与额外建筑扩展
        String ctrlText = recipe.playerControlled() ? "§d[玩家控制]" : "§8[自主发展]";
        String extraText = recipe.allowExtraBuildings() ? " §b[允许扩建]" : "";
        guiGraphics.drawString(font, Component.literal("控制规则: " + ctrlText + extraText), 6, 76, 0x333333, false);

        // 8. 图鉴收录标记
        if (recipe.travelBookDisplay()) {
            guiGraphics.drawString(font, Component.literal("§1✔ 已收录至千年图鉴"), 6, 90, 0x1E90FF, false);
        }

        // [醒目新注释] 已清理原先底部控制台与全量字段悬浮 Inspector Tooltip 渲染
    }
// endregion 面板渲染 (draw)

    // region 用户交互与点击响应逻辑 (handleInput)
    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(VillageType recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            if (DebugGuiUtils.handleJsonButtonClick(recipe, mouseX, mouseY, 128, 6)) {
                return true;
            }
        }
        return IRecipeCategory.super.handleInput(recipe, mouseX, mouseY, input);
    }
// endregion 用户交互与点击响应逻辑 (handleInput)
}