// region 文化分类主面板类
package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;
import org.millenaire.item.ItemHelper;


import java.util.List;

/**
 * 文化 {@link Culture} JEI 详情主分类面板。
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

    private final JeiLinkLayout cultureLayout;
    private final JeiLinkBox<VillageType> villageBox;

    public CultureCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:village_norman") != null ? ItemHelper.resolve("millenaire:village_norman") : Items.MAP))
        );

        // [醒目新注释] 在构造函数中链式调用 {@link JeiLinkBox#setOnPlusClick}，引入 try-catch 拦截与 JEI 内部深层状态检测
        this.villageBox = new JeiLinkBox<VillageType>("文化关联村庄").useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);
        this.cultureLayout = new JeiLinkLayout(2, 106, 172, 4).addBox(villageBox);
    }

    @Override public RecipeType<Culture> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("gui.millenairejeim.category.culture_details"); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.CULTURE, recipe)
                .setCustomRenderer(MillIngredientTypes.CULTURE, ICON_RENDERER);

        List<VillageType> villages = MillenaireAPIHelper.getVillageFormCulture(recipe);
        cultureLayout.setRecipeForBox(0, builder, villages);
    }

    // [醒目新注释] 重写 {@link BaseMillCategory#createRecipeExtras} 方法，构建细节按钮与关联控件的点击响应
    /**
     * 为 {@link Culture} 界面附加交互控件，监听细节按钮并唤起 {@link CultureVillagesCategory} 全景大窗口。
     *
     * @param builder 配方额外组件构建器 {@link IRecipeExtrasBuilder}
     * @param recipe  当前展示的文化配方对象 {@link Culture}
     * @param focuses 当前配方的焦点组 {@link IFocusGroup}
     */
    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Culture recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (cultureLayout != null) {
            cultureLayout.createRecipeExtras(builder, recipe);
        }
    }

    @Override
    public void draw(Culture recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.displayName() != null && !recipe.displayName().isEmpty() ? recipe.displayName() : recipe.id().getPath();

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), "文化主体", mouseX, mouseY);

        drawLabelValue(guiGraphics, font, "原生语言", recipe.nativeLanguage() != null ? recipe.nativeLanguage() : "未设", 6, 42);

        int cropCount = recipe.knownCrops() != null ? recipe.knownCrops().size() : 0;
        drawLabelValue(guiGraphics, font, "已知农作物", "§2" + cropCount + " 种", 6, 52);

        int dropCount = recipe.knownHuntingDrops() != null ? recipe.knownHuntingDrops().size() : 0;
        drawLabelValue(guiGraphics, font, "狩猎掉落物", "§c" + dropCount + " 种", 6, 62);

        drawLabelValue(guiGraphics, font, "地图图标", recipe.mapIcon().isPresent() ? "§a已配置" : "§7未配置", 6, 72);
        drawLabelValue(guiGraphics, font, "文化旗帜", recipe.cultureBannerNbt() != null ? "§a有 NBT" : "§8无 NBT", 6, 82);

        boolean isDefaultTexture = Culture.DEFAULT_PANEL_TEXTURE.equals(recipe.panelTexture());
        drawHoverableText(guiGraphics, font, "面板纹理", isDefaultTexture ? "默认" : recipe.panelTexture().toString(), 6, 92, mouseX, mouseY);

        if (cultureLayout != null) {
            cultureLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}
// endregion 文化分类主面板类