// region BuildingVillagerCategory Implementation
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
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
import org.millenaire.culture.VillagerType;

import java.util.ArrayList;
import java.util.List;

/**
 * 展示建筑计划集 {@link BuildingPlanSet} 居住或工作的所有村民类型 {@link VillagerType} 的 JEI 全景大窗口分类。
 */
public class BuildingVillagerCategory extends BaseMillCategory<BuildingPlanSet> {
    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.BuildingVillagerCategoryPath, BuildingPlanSet.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    /**
     * [醒目新注释] 缓存上一次绘制的 {@link BuildingPlanSet} 对象。
     */
    private BuildingPlanSet lastRecipe;

    /**
     * [醒目新注释] 缓存组件文本 {@link Component}。
     */
    private Component cachedDisplayNameComponent;

    public BuildingVillagerCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(Items.PLAYER_HEAD)));
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() { return MillenaireJeimmKeys.BuildingVillagerCategory.asComponent(); }

    /**
     * 设置全景村民分类的 JEI 槽位布局与隐形成分。
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe 当前建筑计划集对象 {@link BuildingPlanSet}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(MillIngredientTypes.BUILDING, recipe);

        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormBuilding(recipe);
        if (villagers == null || villagers.isEmpty()) return;

        if (focuses != null && !focuses.isEmpty()) {
            List<VillagerType> focusedVillagers = focuses.getFocuses(MillIngredientTypes.VILLAGER)
                    .map(f -> f.getTypedValue().getIngredient())
                    .toList();
            if (!focusedVillagers.isEmpty()) {
                villagers = new ArrayList<>(villagers);
                villagers.sort((a, b) -> Boolean.compare(focusedVillagers.contains(b), focusedVillagers.contains(a)));
            }
        }

        int startX = 14;
        int startY = 24;

        for (int i = 0; i < Math.min(villagers.size(), PAGE_SIZE); i++) {
            VillagerType villager = villagers.get(i);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX + 1, slotY + 1)
                    .addIngredient(MillIngredientTypes.VILLAGER, villager);
        }
    }

    // [醒目新注释] 改用 translatableWithFallback 构建所属建筑名称文本
    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            String name = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();
            this.cachedDisplayNameComponent = MillenaireJeimmKeys.BuildingVillagerCategoryTitle_S.asComponent(name);
        }

        guiGraphics.drawString(font, this.cachedDisplayNameComponent, 14, 8, 0x000000, false);

        int total = recipeSlotsView.getSlotViews().size();
        int startX = 14;
        int startY = 24;

        for (int i = 0; i < PAGE_SIZE; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            if (i < total) {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x20000000);
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0x50000000);
                guiGraphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0x50000000);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0x50000000);
                guiGraphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0x50000000);
            }
        }
    }
}
// endregion BuildingVillagerCategory Implementation