package com.momos.millenairejeimm.jei.villagerof;

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
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

// region 类的定义与静态渲染器
/**
 * 千年村庄村民类型 {@link VillagerType} 的 JEI 详情分类页面。
 */
public class VillagerCategory implements IRecipeCategory<VillagerType> {

    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create("millenaire", "villager_details", VillagerType.class);

    private final IDrawable background;
    private final IDrawable icon;

    private static final MillIconOnlyRenderer<VillagerType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });
// endregion 类的定义与静态渲染器

    // region 构造函数与 JEI 分类初始化
    public VillagerCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 移除暗色控制台后，将面板尺寸修正为 160x80
        this.background = guiHelper.createBlankDrawable(160, 80);

        Item travelBookItem = ItemHelper.resolve("millenaire:travel_book");
        if (travelBookItem == null) {
            travelBookItem = ItemHelper.resolve("millenaire:norman_amulet");
        }
        ItemStack iconStack = (travelBookItem != null) ? new ItemStack(travelBookItem) : new ItemStack(Items.BOOK);

        this.icon = guiHelper.createDrawableItemStack(iconStack);
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
// endregion 构造函数与 JEI 分类初始化

    // region JEI 槽位与成分配置 (setRecipe)
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGER, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGER, ICON_RENDERER);
    }
// endregion JEI 槽位与成分配置 (setRecipe)

    // region 面板渲染 (draw)
    @Override
    public void draw(VillagerType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        // 1. 渲染村民原名与显示名称
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.id().getPath();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);

        DebugGuiUtils.drawJsonDevButton(guiGraphics, font, 128, 6, mouseX, mouseY);

        // 2. 渲染文化与性别/年龄
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";
        String genderText = recipe.gender() != null ? recipe.gender().name() : "未知";
        String ageText = recipe.isChild() ? "幼年/儿童" : "成年";
        guiGraphics.drawString(font, Component.literal("§8" + culture + " | " + genderText + " (" + ageText + ")"), 28, 17, 0x444444, false);

        // 3. 渲染分隔线
        guiGraphics.fill(6, 27, 154, 28, 0xFFCCCCCC);

        // 4. 渲染基础战斗属性：最大生命值与基础攻击力
        guiGraphics.drawString(font, Component.literal("生命: §c" + (int) recipe.maxHealth() + " HP §r| 攻击: §4" + recipe.baseAttackStrength() + " §r| 缩放: §b" + recipe.baseScale()), 6, 30, 0x333333, false);

        // 5. 渲染雇佣费用与生成权重
        String hiringText = recipe.hiringCost() > 0 ? "§6" + recipe.hiringCost() + " 钱币" : "§8不可雇佣";
        guiGraphics.drawString(font, Component.literal("雇佣费用: " + hiringText + " §r| 权重: §1" + recipe.spawnWeight()), 6, 41, 0x333333, false);

        // 6. 解析并渲染行为与战斗标签
        StringBuilder tagsText = new StringBuilder("特性: ");
        if (recipe.isRaider()) tagsText.append("§c[掠夺] ");
        if (recipe.isHostile()) tagsText.append("§4[敌对] ");
        if (recipe.isHelpInAttacks()) tagsText.append("§9[协防] ");
        if (recipe.isArcher()) tagsText.append("§2[弓手] ");
        if (recipe.isNoTeleport()) tagsText.append("§7[禁传送] ");
        if (recipe.isNoResurrect()) tagsText.append("§8[不可复活] ");
        if (!recipe.isRaider() && !recipe.isHostile() && !recipe.isHelpInAttacks() && !recipe.isArcher()) {
            tagsText.append("§8平民/日常");
        }
        guiGraphics.drawString(font, Component.literal(tagsText.toString()), 6, 52, 0x333333, false);

        // 7. 渲染别名或配置标识 Key
        if (recipe.altNativeName() != null && !recipe.altNativeName().isEmpty()) {
            guiGraphics.drawString(font, Component.literal("别名: §5" + recipe.altNativeName() + " §8(" + (recipe.altKey() != null ? recipe.altKey() : "") + ")"), 6, 64, 0x333333, false);
        } else if (recipe.villagerConfigKey() != null) {
            guiGraphics.drawString(font, Component.literal("配置标识: §8" + recipe.villagerConfigKey()), 6, 64, 0x555555, false);
        } else {
            if (recipe.travelBookDisplay()) {
                String category = recipe.travelBookCategory() != null ? recipe.travelBookCategory() : "通用";
                String heldItemInfo = recipe.travelBookHeldItem() != null ? " (手持: " + recipe.travelBookHeldItem() + ")" : "";
                guiGraphics.drawString(font, Component.literal("§1✔ 已收录至千年图鉴 (" + category + ")" + heldItemInfo), 6, 64, 0x1E90FF, false);
            }
        }

        // [醒目新注释] 已清理原先的暗色 Debug 终端控制台与全量字段悬浮 Hover Inspector 浮窗
    }
// endregion 面板渲染 (draw)

    // region 用户交互与点击响应逻辑 (handleInput)
    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(VillagerType recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            if (DebugGuiUtils.handleJsonButtonClick(recipe, mouseX, mouseY, 128, 6)) {
                return true;
            }
        }
        return IRecipeCategory.super.handleInput(recipe, mouseX, mouseY, input);
    }
// endregion 用户交互与点击响应逻辑 (handleInput)
}