package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.mojang.blaze3d.platform.InputConstants;
import com.momos.millenairejeimm.MillenaireJeimm;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.type.BaseMillCategory;
import com.momos.millenairejeimm.jei.type.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.type.layout.CultureLinkLayout;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
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
import org.millenaire.item.ItemHelper;

// region 类定义与构造函数
/**
 * 文化 {@link Culture} JEI 详情分类，继承自 {@link BaseMillCategory}。
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

    // [原注释保留] 字段声明：改用在构造函数中通过 IIngredientManager 进行 JEI 绑定的 CultureLinkLayout[cite: 11]
    private final CultureLinkLayout villageLayout;

    /**
     * [醒目新注释] 构造函数（全功能）：注入 {@link IJeiRuntime} 以绑定原生 R/U 键与鼠标左右键查找。
     */
    public CultureCategory(IGuiHelper guiHelper, IJeiRuntime jeiRuntime) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:village_norman") != null ? ItemHelper.resolve("millenaire:village_norman") : Items.MAP))
        );
        this.villageLayout = new CultureLinkLayout("文化关联村庄", 2)
                .bindJei(jeiRuntime, MillIngredientTypes.VILLAGE);
    }

    /**
     * [原注释保留] 构造函数：注入 {@link IIngredientManager}，将 {@link CultureLinkLayout} 绑定至 JEI 的 {@link MillIngredientTypes#VILLAGE}。[cite: 11]
     */
    public CultureCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:village_norman") != null ? ItemHelper.resolve("millenaire:village_norman") : Items.MAP))
        );
        this.villageLayout = new CultureLinkLayout("文化关联村庄", 2)
                .bindJei(ingredientManager, MillIngredientTypes.VILLAGE);
    }

    @Override public RecipeType<Culture> getRecipeType() {return RECIPE_TYPE;}
    @Override public Component getTitle() {return Component.translatable("gui.millenairejeim.category.culture_details");}

    // region JEI 槽位与绘制实现
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.CULTURE, recipe)
                .setCustomRenderer(MillIngredientTypes.CULTURE, ICON_RENDERER);
        villageLayout.set(MillenaireJeimm.getVillageFormCulture(recipe));
    }

    @Override
    public void draw(Culture recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.displayName() != null && !recipe.displayName().isEmpty() ? recipe.displayName() : recipe.id().getPath();

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), "文化主体", mouseX, mouseY);

        // [原注释保留] 统一从 Y: 42 开始罗列[cite: 11]
        drawLabelValue(guiGraphics, font, "原生语言", recipe.nativeLanguage() != null ? recipe.nativeLanguage() : "未设", 6, 42);

        int cropCount = recipe.knownCrops() != null ? recipe.knownCrops().size() : 0;
        drawLabelValue(guiGraphics, font, "已知农作物", "§2" + cropCount + " 种", 6, 52);

        int dropCount = recipe.knownHuntingDrops() != null ? recipe.knownHuntingDrops().size() : 0;
        drawLabelValue(guiGraphics, font, "狩猎掉落物", "§c" + dropCount + " 种", 6, 62);

        drawLabelValue(guiGraphics, font, "地图图标", recipe.mapIcon().isPresent() ? "§a已配置" : "§7未配置", 6, 72);
        drawLabelValue(guiGraphics, font, "文化旗帜", recipe.cultureBannerNbt() != null ? "§a有 NBT" : "§8无 NBT", 6, 82);

        boolean isDefaultTexture = Culture.DEFAULT_PANEL_TEXTURE.equals(recipe.panelTexture());
        drawHoverableText(guiGraphics, font, "面板纹理", isDefaultTexture ? "默认" : recipe.panelTexture().toString(), 6, 92, mouseX, mouseY);

        // [醒目新注释] 传入渲染位置与当前鼠标坐标，保证 Hover 高亮与 Tooltip 精确判定
        villageLayout.render(guiGraphics, font, 2, 106, 172, mouseX, mouseY);
    }

    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(Culture recipe, double mouseX, double mouseY, InputConstants.Key input) {
        // [原注释保留] 输入事件托管：直接交给 Layout 判断并处理上下翻页点击[cite: 11]
        // [醒目新注释] 自动解析输入事件：鼠标左/右键与键盘 R/U 键全量触发，无缝呼出配方/用途 GUI
        if (villageLayout.handleInput(mouseX, mouseY, input)) {
            return true;
        }
        return super.handleInput(recipe, mouseX, mouseY, input);
    }
}
// endregion 类定义与构造函数
// endregion JEI链接框原生RU触发组件修复