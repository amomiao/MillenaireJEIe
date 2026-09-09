package com.momos.millenairejeimm.jei.cultureof;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.culture.Culture;
import org.millenaire.item.ItemHelper;

// region 类定义与静态渲染器
/**
 * 千年村庄文化 {@link Culture} 的 JEI 详情分类页面。
 */
public class CultureCategory implements IRecipeCategory<Culture> {

    public static final RecipeType<Culture> RECIPE_TYPE = RecipeType.create("millenaire", "culture_details", Culture.class);

    private final IDrawable background;
    private final IDrawable icon;

    private static final MillIconOnlyRenderer<Culture> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.mapIcon().isPresent()) {
            ResourceLocation iconRl = recipe.mapIcon().get();
            Item mapIconItem = ItemHelper.resolve(iconRl.toString());
            if (mapIconItem != null) return new ItemStack(mapIconItem);
        }
        return ItemStack.EMPTY;
    });
// endregion 类定义与静态渲染器

    // region 构造函数与 JEI 元数据回调
    public CultureCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 移除暗色控制台后，调整底图尺寸为 160x80
        this.background = guiHelper.createBlankDrawable(160, 80);

        Item cultureIconItem = ItemHelper.resolve("millenaire:village_norman");
        if (cultureIconItem == null) {
            cultureIconItem = ItemHelper.resolve("millenaire:norman_amulet");
        }
        ItemStack iconStack = (cultureIconItem != null) ? new ItemStack(cultureIconItem) : new ItemStack(Items.MAP);

        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<Culture> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.culture_details");
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
// endregion 构造函数与 JEI 元数据回调

    // region JEI 槽位注册 (setRecipe)
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.CULTURE, recipe)
                .setCustomRenderer(MillIngredientTypes.CULTURE, ICON_RENDERER);
    }
// endregion JEI 槽位注册 (setRecipe)

    // region Category 界面绘制主逻辑 (draw)
    @Override
    public void draw(Culture recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        String displayName = recipe.displayName() != null && !recipe.displayName().isEmpty() ? recipe.displayName() : recipe.id().getPath();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);
        guiGraphics.drawString(font, Component.literal("§8标识: " + recipe.id()), 28, 17, 0x444444, false);

        DebugGuiUtils.drawJsonDevButton(guiGraphics, font, 128, 6, mouseX, mouseY);

        guiGraphics.fill(6, 27, 154, 28, 0xFFCCCCCC);

        // 1. 基础字段与计数属性网格 (Y: 30~55)
        String langText = recipe.nativeLanguage() != null ? recipe.nativeLanguage() : "未设母语";
        guiGraphics.drawString(font, Component.literal("原生语言: §8" + langText), 6, 30, 0x333333, false);

        int cropCount = recipe.knownCrops() != null ? recipe.knownCrops().size() : 0;
        int dropCount = recipe.knownHuntingDrops() != null ? recipe.knownHuntingDrops().size() : 0;
        guiGraphics.drawString(font, Component.literal("已知农作物: §2" + cropCount + " 种§r | 狩猎掉落: §c" + dropCount + " 种"), 6, 41, 0x333333, false);

        String mapIconState = recipe.mapIcon().isPresent() ? "§a[已配置]" : "§7[无]";
        String bannerNbtState = recipe.cultureBannerNbt() != null ? "§a[有NBT]" : "§8[无NBT]";
        guiGraphics.drawString(font, Component.literal("地图图标: " + mapIconState + " | 旗帜: " + bannerNbtState), 6, 52, 0x333333, false);

        // 2. 状态徽章 Badges 区域 (Y: 64~74)
        boolean isDefaultTexture = Culture.DEFAULT_PANEL_TEXTURE.equals(recipe.panelTexture());
        String textureBadge = isDefaultTexture ? "§8[默认面板纹理]" : "§b[自定义面板纹理]";
        guiGraphics.drawString(font, Component.literal("属性: " + textureBadge), 6, 64, 0x333333, false);

        // [醒目新注释] 已清理原先的黑色控制台与悬浮 Inspector 浮窗绘制代码
    }
// endregion Category 界面绘制主逻辑 (draw)

    // region 用户交互与点击响应逻辑 (handleInput)
    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(Culture recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            if (DebugGuiUtils.handleJsonButtonClick(recipe, mouseX, mouseY, 128, 6)) {
                return true;
            }
        }
        return IRecipeCategory.super.handleInput(recipe, mouseX, mouseY, input);
    }
// endregion 用户交互与点击响应逻辑 (handleInput)
}