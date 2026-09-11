// region BuildingVillagersCategory 建筑关联村民分类
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

import java.util.List;

/**
 * 建筑计划集 {@link BuildingPlanSet} 居住/工作村民分类页 3，继承自 {@link BaseMillCategory}。
 */
public class BuildingVillagersCategory extends BaseMillCategory<BuildingPlanSet> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_villagers", BuildingPlanSet.class);

    private static final MillIconOnlyRenderer<BuildingPlanSet> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    // [醒目新注释] 关联村民布局与 LinkBox 定义
    private final JeiLinkLayout villagerLayout;
    private final JeiLinkBox<VillagerType> villagerBox;

    /**
     * 构造函数：初始化居住与工作村民分类及其 LinkBox 组件。
     *
     * @param guiHelper JEI GUI 辅助接口 {@link IGuiHelper}
     * @param ingredientManager JEI 成分管理器 {@link IIngredientManager}
     */
    public BuildingVillagersCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("minecraft:player_head") != null ? ItemHelper.resolve("minecraft:player_head") : Items.PLAYER_HEAD)));

        // [醒目新注释] 居住/工作村民 LinkBox (Ingredient: VILLAGER)
        this.villagerBox = new JeiLinkBox<VillagerType>("居住或工作村民")
                .useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof BuildingPlanSet building) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.BUILDING, building);
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                    }
                });

        this.villagerLayout = new JeiLinkLayout(2, 45, 172, 4)
                .addBox(villagerBox);
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.building_villagers");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.BUILDING, recipe)
                .setCustomRenderer(MillIngredientTypes.BUILDING, ICON_RENDERER);

        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormBuilding(recipe);

        if (villagerLayout != null) {
            villagerLayout.setRecipeForBox(0, builder, villagers);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (villagerLayout != null) villagerLayout.createRecipeExtras(builder, recipe);
    }

    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();

        drawCommonHeader(guiGraphics, font, displayName, recipe.buildingId(), recipe.category(), mouseX, mouseY);

        if (villagerLayout != null) {
            villagerLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}
// endregion BuildingVillagersCategory 建筑关联村民分类