// region BuildingVillagesCategory 建筑关联村庄分类
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;
import org.millenaire.item.ItemHelper;

import java.util.List;

/**
 * 建筑计划集 {@link BuildingPlanSet} 所属村庄分类页，直接继承自 {@link BaseMillCategory}。
 */
public class BuildingVillagesCategory extends BaseMillCategory<BuildingPlanSet> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_villages", BuildingPlanSet.class);

    private final JeiLinkLayout villageLayout;
    private final JeiLinkBox<VillageType> villageBox;

    private BuildingPlanSet lastRecipe;
    private Component cachedTitleComponent;

    public BuildingVillagesCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(ItemHelper.resolve("minecraft:oak_door") != null ? ItemHelper.resolve("minecraft:oak_door") : Items.OAK_DOOR)));

        this.villageBox = new JeiLinkBox<VillageType>("包含此建筑的村庄")
                .useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof BuildingPlanSet building) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.BUILDING, building);
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                    }
                });

        this.villageLayout = new JeiLinkLayout(2, 30, 172, 4)
                .addBox(villageBox);
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.building_villages");
    }

    /**
     * [醒目新注释] 显式绑定隐形成分并填充所属村庄列表。
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredient(MillIngredientTypes.BUILDING, recipe);
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addIngredient(MillIngredientTypes.BUILDING, recipe);

        List<VillageType> villages = MillenaireAPIHelper.getVillageFormBuilding(recipe);

        if (villageLayout != null && villages != null) {
            villageLayout.setRecipeForBox(0, builder, villages);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (villageLayout != null) villageLayout.createRecipeExtras(builder, recipe);
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

        if (villageLayout != null) {
            villageLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}
// endregion BuildingVillagesCategory 建筑关联村庄分类