package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.mojang.blaze3d.platform.InputConstants;
import com.momos.millenairejeimm.MillenaireJeimm;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.type.BaseMillCategory;
import com.momos.millenairejeimm.jei.type.DebugGuiUtils;
import com.momos.millenairejeimm.jei.type.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.type.layout.CultureLinkLayout;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;
import org.millenaire.item.ItemHelper;

import java.util.List;

// region 文化分类主面板类
/**
 * 文化 {@link Culture} JEI 详情主分类，继承自 {@link BaseMillCategory}。
 */
public class CultureCategory extends BaseMillCategory<Culture> {
    public static final RecipeType<Culture> RECIPE_TYPE = RecipeType.create("millenaire", "culture_details", Culture.class);
    private static final MillIconOnlyRenderer<Culture> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.mapIcon().isPresent()) {
            ResourceLocation iconRl = recipe.mapIcon().get();
            Item mapIconItem = ItemHelper.resolve(iconRl.toString());
            if (mapIconItem != null) return new ItemStack(mapIconItem);
        }
        return ItemStack.EMPTY;
    });

    // [原注释保留] 字段声明：改用在构造函数中通过 IIngredientManager 进行 JEI 绑定的 CultureLinkLayout
    private final CultureLinkLayout villageLayout;

    public CultureCategory(IGuiHelper guiHelper, IJeiRuntime jeiRuntime) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:village_norman") != null ? ItemHelper.resolve("millenaire:village_norman") : Items.MAP))
        );
        this.villageLayout = new CultureLinkLayout("文化关联村庄")
                .bindJei(jeiRuntime, MillIngredientTypes.VILLAGE);
    }

    public CultureCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:village_norman") != null ? ItemHelper.resolve("millenaire:village_norman") : Items.MAP))
        );
        this.villageLayout = new CultureLinkLayout("文化关联村庄")
                .bindJei(ingredientManager, MillIngredientTypes.VILLAGE);
    }

    @Override public RecipeType<Culture> getRecipeType() {return RECIPE_TYPE;}
    @Override public Component getTitle() {return Component.translatable("gui.millenairejeim.category.culture_details");}

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // 1. 注册文化主体图标槽位
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.CULTURE, recipe)
                .setCustomRenderer(MillIngredientTypes.CULTURE, ICON_RENDERER);

        // 2. 注册 LinkBox（前 8 个预览槽位）
        List<VillageType> villages = MillenaireJeimm.getVillageFormCulture(recipe);
        villageLayout.setRecipe(builder, villages, 2, 106);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Culture recipe, IFocusGroup focuses) {
        // [醒目新注释] 为底层 villageLayout 绑定 Tooltip 悬浮提示框等 RecipeExtras 区域
        if (villageLayout != null) {
            villageLayout.createRecipeExtras(builder, recipe, 2, 106, 172);
        }
    }

    /**
     * [醒目新注释]
     * 重写 {@link BaseMillCategory#handleInput}，直接在分类面板层统一截获鼠标左键事件。
     * 拦截逻辑委托给 {@link CultureLinkLayout#handlePlusButtonClick} 处理。
     */
    @Override
    public boolean handleInput(Culture recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            if (villageLayout != null && villageLayout.handlePlusButtonClick(recipe, mouseX, mouseY, 2, 106)) {
                return true;
            }
        }
        return super.handleInput(recipe, mouseX, mouseY, input);
    }

    @Override
    public void draw(Culture recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.displayName() != null && !recipe.displayName().isEmpty() ? recipe.displayName() : recipe.id().getPath();

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), "文化主体", mouseX, mouseY);

        // [原注释保留] 统一从 Y: 42 开始罗列
        drawLabelValue(guiGraphics, font, "原生语言", recipe.nativeLanguage() != null ? recipe.nativeLanguage() : "未设", 6, 42);

        int cropCount = recipe.knownCrops() != null ? recipe.knownCrops().size() : 0;
        drawLabelValue(guiGraphics, font, "已知农作物", "§2" + cropCount + " 种", 6, 52);

        int dropCount = recipe.knownHuntingDrops() != null ? recipe.knownHuntingDrops().size() : 0;
        drawLabelValue(guiGraphics, font, "狩猎掉落物", "§c" + dropCount + " 种", 6, 62);

        drawLabelValue(guiGraphics, font, "地图图标", recipe.mapIcon().isPresent() ? "§a已配置" : "§7未配置", 6, 72);
        drawLabelValue(guiGraphics, font, "文化旗帜", recipe.cultureBannerNbt() != null ? "§a有 NBT" : "§8无 NBT", 6, 82);

        boolean isDefaultTexture = Culture.DEFAULT_PANEL_TEXTURE.equals(recipe.panelTexture());
        drawHoverableText(guiGraphics, font, "面板纹理", isDefaultTexture ? "默认" : recipe.panelTexture().toString(), 6, 92, mouseX, mouseY);

        // 绘制底部统一背景及第九个加号按钮上方的 "+" 文本渲染
        villageLayout.render(guiGraphics, font, 2, 106, 172, mouseX, mouseY);
    }
}