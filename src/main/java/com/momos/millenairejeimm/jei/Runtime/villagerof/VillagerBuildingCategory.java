package com.momos.millenairejeimm.jei.Runtime.villagerof;

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
import org.millenaire.culture.VillagerType;

import java.util.ArrayList;
import java.util.List;

/**
 * 展示村民类型 {@link VillagerType} 居住与工作关联建筑计划集 {@link BuildingPlanSet} 的 JEI 全景大窗口分类。
 */
public class VillagerBuildingCategory extends BaseMillCategory<VillagerType> {
    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create("millenaire", "villager_buildings_full", VillagerType.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    public VillagerBuildingCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(Items.OAK_DOOR)));
    }

    @Override
    public RecipeType<VillagerType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() { return MillenaireJeimmKeys.VillagerBuildingCategory.asComponent(); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(MillIngredientTypes.VILLAGER, recipe);

        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillager(recipe);
        if (buildings == null || buildings.isEmpty()) return;

        if (focuses != null && !focuses.isEmpty()) {
            List<BuildingPlanSet> focusedBuildings = focuses.getFocuses(MillIngredientTypes.BUILDING)
                    .map(f -> f.getTypedValue().getIngredient())
                    .toList();
            if (!focusedBuildings.isEmpty()) {
                buildings = new ArrayList<>(buildings);
                buildings.sort((a, b) -> Boolean.compare(focusedBuildings.contains(b), focusedBuildings.contains(a)));
            }
        }

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
    public void draw(VillagerType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        String name = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.id().getPath();
        Component displayNameComponent =  MillenaireJeimmKeys.VillagerBuildingCategoryTitle_S.asComponent(name);

        guiGraphics.drawString(font, displayNameComponent, 14, 8, 0x000000, false);

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