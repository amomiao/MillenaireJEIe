package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
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
    public static final RecipeType<Culture> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.CultureCategoryPath, Culture.class);

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

        // 绑定全景跳转至 CultureVillagesCategory
        this.villageBox = new JeiLinkBox<VillageType>(MillenaireJeimmKeys.VillageBoxLink.asComponent())
                .useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE)
                .setTargetCategory(CultureVillagesCategory.RECIPE_TYPE);
        this.cultureLayout = new JeiLinkLayout(2, 106, 172, 4).addBox(villageBox);
    }

    @Override public RecipeType<Culture> getRecipeType() { return RECIPE_TYPE; }

    @Override
    public Component getTitle() { return MillenaireJeimmKeys.CultureCategory.asComponent(); }

    private void updateLayoutData(Culture recipe) {
        if (cultureLayout != null && recipe != null) {
            List<VillageType> villages = MillenaireAPIHelper.getVillageFormCulture(recipe);
            cultureLayout.setBoxData(0, villages);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.CULTURE, recipe)
                .setCustomRenderer(MillIngredientTypes.CULTURE, ICON_RENDERER);

        updateLayoutData(recipe);
        List<VillageType> villages = MillenaireAPIHelper.getVillageFormCulture(recipe);
        cultureLayout.setRecipeForBox(0, builder, villages);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Culture recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (cultureLayout != null) {
            updateLayoutData(recipe);
            cultureLayout.createRecipeExtras(builder, recipe);
        }
    }

    @Override
    public void draw(Culture recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.displayName() != null && !recipe.displayName().isEmpty() ? recipe.displayName() : recipe.id().getPath();

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), "Culture", mouseX, mouseY);

        // 1. NativeLanguage
        String nativeLangVal = recipe.nativeLanguage() != null
                ? recipe.nativeLanguage()
                : MillenaireJeimmKeys.UnsetValue.asComponent().getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.NativeLanguageLabel.asComponent(), nativeLangVal, 6, 42);

        // 2. CropCount
        int cropCount = recipe.knownCrops() != null ? recipe.knownCrops().size() : 0;
        String cropVal = MillenaireJeimmKeys.CropCountValue_D.asComponent(cropCount).getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.KnownCropsLabel.asComponent(), "§2" + cropVal, 6, 52);

        // 3. DropCount
        int dropCount = recipe.knownHuntingDrops() != null ? recipe.knownHuntingDrops().size() : 0;
        String dropVal = MillenaireJeimmKeys.DropCountValue_D.asComponent(dropCount).getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.HuntingDropsLabel.asComponent(), "§c" + dropVal, 6, 62);

        // 4. MapIcon
        String mapIconVal = recipe.mapIcon().isPresent()
                ? "§a" + MillenaireJeimmKeys.ConfiguredValue.asComponent().getString()
                : "§7" + MillenaireJeimmKeys.UnconfiguredValue.asComponent().getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.MapIconLabel.asComponent(), mapIconVal, 7, 72);

        // 5. Banner
        String bannerVal = recipe.cultureBannerNbt() != null
                ? "§a" + MillenaireJeimmKeys.HasNbtValue.asComponent().getString()
                : "§8" + MillenaireJeimmKeys.NoNbtValue.asComponent().getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.CultureBannerLabel.asComponent(), bannerVal, 6, 82);

        // 6. PanelTexture
        boolean isDefaultTexture = Culture.DEFAULT_PANEL_TEXTURE.equals(recipe.panelTexture());
        String textureVal = isDefaultTexture
                ? MillenaireJeimmKeys.DefaultValue.asComponent().getString()
                : recipe.panelTexture().toString();
        drawHoverableText(guiGraphics, font, MillenaireJeimmKeys.PanelTextureLabel.asComponent(), Component.literal(textureVal), 6, 92, mouseX, mouseY);

        if (cultureLayout != null) {
            updateLayoutData(recipe);
            cultureLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}
