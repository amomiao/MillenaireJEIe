package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
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
import org.millenaire.culture.VillageType;

import java.util.ArrayList;
import java.util.List;

// region 村庄关联建筑全景大窗口面板类
/**
 * 展示村庄类型 {@link VillageType} 包含的所有建筑 {@link BuildingPlanSet} 的 JEI 全景大窗口分类。
 */
public class VillageBuildingsCategory extends BaseMillCategory<VillageType> {

    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.VillageBuildingsCategoryPath, VillageType.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    private static final int GUI_START_X = 14;
    private static final int GUI_START_Y = 24;
    private static final int HEADER_X = 14;
    private static final int HEADER_Y = 8;
    private static final int HEADER_COLOR = 0x000000;
    private static final int BG_FILL_COLOR = 0x20000000;
    private static final int BORDER_FILL_COLOR = 0x50000000;

    private VillageType lastRecipe;
    private Component cachedDisplayNameComponent;

    public VillageBuildingsCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(Items.BRICKS)));
    }

    @Override public RecipeType<VillageType> getRecipeType() { return RECIPE_TYPE; }

    @Override
    public Component getTitle() { return MillenaireJeimmKeys.VillageBuildingsCategory.asComponent(); }

    // [醒目新注释] 将隐形成分修改为 RENDER_ONLY 角色，形成全景建筑分类的独占类型区分
    /**
     * 设置全景建筑分类的 JEI 槽位布局与隐形成分。
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe 当前村庄类型对象 {@link VillageType}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(MillIngredientTypes.VILLAGE, recipe);

        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(recipe);
        if (buildings == null || buildings.isEmpty()) return;

        // [醒目新注释] 根据当前焦点过滤/重排，置顶被选中的建筑
        if (focuses != null && !focuses.isEmpty()) {
            List<BuildingPlanSet> focusedBuildings = focuses.getFocuses(MillIngredientTypes.BUILDING)
                    .map(f -> f.getTypedValue().getIngredient())
                    .toList();
            if (!focusedBuildings.isEmpty()) {
                buildings = new ArrayList<>(buildings);
                buildings.sort((a, b) -> Boolean.compare(focusedBuildings.contains(b), focusedBuildings.contains(a)));
            }
        }

        for (int i = 0; i < Math.min(buildings.size(), PAGE_SIZE); i++) {
            BuildingPlanSet building = buildings.get(i);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = GUI_START_X + (col * SLOT_SIZE);
            int slotY = GUI_START_Y + (row * SLOT_SIZE);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX + 1, slotY + 1).addIngredient(MillIngredientTypes.BUILDING, building);
        }
    }

    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            // [醒目新注释] 使用 {@link MillenaireJeimmKeys#getVillageLabelBelongs} 格式化并缓存显示名称
            this.cachedDisplayNameComponent =  MillenaireJeimmKeys.VillageBuildingsCategoryTitle_S.asComponent(recipe.name());
        }

        guiGraphics.drawString(font, this.cachedDisplayNameComponent, HEADER_X, HEADER_Y, HEADER_COLOR, false);

        int total = recipeSlotsView.getSlotViews().size();

        for (int i = 0; i < PAGE_SIZE; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = GUI_START_X + (col * SLOT_SIZE);
            int slotY = GUI_START_Y + (row * SLOT_SIZE);

            if (i < total) {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, BG_FILL_COLOR);
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 1, BORDER_FILL_COLOR);
                guiGraphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, BORDER_FILL_COLOR);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 18, BORDER_FILL_COLOR);
                guiGraphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, BORDER_FILL_COLOR);
            }
        }
    }
}
// endregion 村庄关联建筑全景大窗口面板类