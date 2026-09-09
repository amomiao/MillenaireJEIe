// region 文化分类主面板类
package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.mojang.blaze3d.platform.InputConstants;
import com.momos.millenairejeimm.MillenaireJeimm;
import com.momos.millenairejeimm.debug.DebugGuiUtils;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.type.layout.CultureLinkLayout;
import com.momos.millenairejeimm.jei.type.JeiLinkBox;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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

    private final CultureLinkLayout cultureLayout;
    private final JeiLinkBox<VillageType> villageBox;

    // [醒目新注释] 第 9 格（细节/导航触发区）的准确点击响应边界
    private static final int SLOT_9_X = 6;
    private static final int SLOT_9_Y = 32;
    private static final int SLOT_9_WIDTH = 48;
    private static final int SLOT_9_HEIGHT = 14;

    public CultureCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:village_norman") != null ? ItemHelper.resolve("millenaire:village_norman") : Items.MAP))
        );
        this.villageBox = new JeiLinkBox<VillageType>("文化关联村庄").useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE);
        this.cultureLayout = new CultureLinkLayout(2, 106, 172, 4).addBox(villageBox);
    }

    @Override public RecipeType<Culture> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("gui.millenairejeim.category.culture_details"); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.CULTURE, recipe)
                .setCustomRenderer(MillIngredientTypes.CULTURE, ICON_RENDERER);

        List<VillageType> villages = MillenaireJeimm.getVillageFormCulture(recipe);
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
        // [醒目新注释] 监听第 9 格（细节）的点击事件，触发 DebugGuiUtils#openJsonDumpScreen
        builder.addInputHandler(new IJeiInputHandler() {
            @Override
            public ScreenRectangle getArea() {
                return new ScreenRectangle(SLOT_9_X, SLOT_9_Y, SLOT_9_WIDTH, SLOT_9_HEIGHT);
            }
            @Override
            public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
                if (mouseX < SLOT_9_X || mouseX > SLOT_9_X + SLOT_9_WIDTH ||
                        mouseY < SLOT_9_Y || mouseY > SLOT_9_Y + SLOT_9_HEIGHT) {
                    return false;
                }
                InputConstants.Key key = input.getKey();
                if (key.getType() == InputConstants.Type.MOUSE && key.getValue() == 0) {
                    // [醒目新注释] 细节按钮点击修正：直接调用 {@link DebugGuiUtils#openJsonDumpScreen(Object)} 打开 Dump 调试界面
                    // [醒目新注释] 修正：不再打开 Dump 界面，改为向 JEI Runtime 提交 Focus 焦点，打开 {@link CultureVillagesCategory} 全景大窗口
                    if (MillenaireJeimmPlugin.JeiRuntime != null) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory().createFocus(
                                RecipeIngredientRole.INPUT,
                                MillIngredientTypes.CULTURE,
                                recipe
                        );
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                        return true;
                    }
                    return true;
                }
                return false;
            }
        });
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