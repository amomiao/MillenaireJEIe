package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
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
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;

import java.util.List;

// region 村庄关联建筑全景大窗口面板类
/**
 * 点击村庄建筑 LinkBox "+" 后唤起的全景大窗口 JEI 分类 {@link VillageBuildingsCategory}。
 */
public class VillageBuildingsCategory extends BaseMillCategory<VillageType> {
    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create("millenaire", "village_buildings_full", VillageType.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    /** 缓存上一次绘制的 {@link VillageType} 对象，用于判断当前帧是否切换了配方。 */
    private VillageType lastRecipe;

    /** 缓存已构造好的村庄名称文本 {@link Component}，规避渲染循环中的垃圾分配。 */
    private Component cachedDisplayNameComponent;

    public VillageBuildingsCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK)));
    }

    @Override public RecipeType<VillageType> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.literal("村庄包含建筑 (全景大窗口)"); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // [醒目新注释] 注册隐形焦点成分，使 JEI 能依据 Focus 定位村庄配方
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredient(MillIngredientTypes.VILLAGE, recipe);

        // [醒目新注释] 调用 {@link MillenaireAPIHelper#getBuildingFormVillage} 获取该村庄的所有包含建筑
        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(recipe);
        if (buildings == null || buildings.isEmpty()) return;

        int startX = 14;
        int startY = 24;

        for (int i = 0; i < Math.min(buildings.size(), PAGE_SIZE); i++) {
            BuildingPlanSet building = buildings.get(i);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX + 1, slotY + 1)
                    .addIngredient(MillIngredientTypes.BUILDING, building);
        }
    }

    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            String displayName = recipe.name() != null ? recipe.name() : recipe.id().getPath();
            this.cachedDisplayNameComponent = Component.literal("§1所属村庄: §f" + displayName);
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
// endregion 村庄关联建筑全景大窗口面板类