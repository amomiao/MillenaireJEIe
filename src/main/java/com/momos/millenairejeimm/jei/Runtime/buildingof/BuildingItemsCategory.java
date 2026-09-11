// region BuildingItemsCategory 建筑关联道具分类
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 建筑计划集 {@link BuildingPlanSet} 关联/交易/生产道具分类页，直接继承自 {@link BaseMillCategory}。
 */
public class BuildingItemsCategory extends BaseMillCategory<BuildingPlanSet> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_items", BuildingPlanSet.class);

    private final JeiLinkLayout itemLayout;
    private final JeiLinkBox<ItemStack> itemBox;

    private BuildingPlanSet lastRecipe;
    private Component cachedTitleComponent;

    public BuildingItemsCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(ItemHelper.resolve("minecraft:chest") != null ? ItemHelper.resolve("minecraft:chest") : Items.CHEST)));

        this.itemBox = new JeiLinkBox<ItemStack>("建筑关联与交易道具")
                .useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof BuildingPlanSet building) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.BUILDING, building);
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                    }
                });

        this.itemLayout = new JeiLinkLayout(2, 30, 172, 4)
                .addBox(itemBox);
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.building_items");
    }

    /**
     * [醒目新注释] 显式绑定隐形成分并解禁 itemLayout.setRecipeForBox 注册，确保数据及 Focus 按键通畅。
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredient(MillIngredientTypes.BUILDING, recipe);
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addIngredient(MillIngredientTypes.BUILDING, recipe);

        //List<ItemStack> items = MillenaireAPIHelper.getItemsFromBuilding(recipe);
        List<ItemStack> items = new ArrayList<>();
        items.add(new ItemStack(Items.OAK_LOG, 128));
        items.add(new ItemStack(Items.COBBLESTONE, 256));
        items.add(new ItemStack(Items.IRON_INGOT, 32));
        items.add(new ItemStack(Items.GLASS, 64));
        items.add(new ItemStack(Items.BRICKS, 96));

        if (itemLayout != null && items != null) {
            itemLayout.setRecipeForBox(0, builder, items);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (itemLayout != null) itemLayout.createRecipeExtras(builder, recipe);
    }

    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        if (this.lastRecipe != recipe) {
            this.lastRecipe = recipe;
            String name = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();
            this.cachedTitleComponent = Component.literal("§1所属建筑: §f" + name);
        }
        guiGraphics.drawString(font, this.cachedTitleComponent, 6, 8, 0x000000, false);

        if (itemLayout != null) {
            itemLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}
// endregion BuildingItemsCategory 建筑关联道具分类