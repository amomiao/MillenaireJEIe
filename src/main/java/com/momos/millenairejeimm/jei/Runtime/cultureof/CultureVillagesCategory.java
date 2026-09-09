package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.momos.millenairejeimm.MillenaireJeimm;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.type.BaseMillCategory;
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
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;

import java.util.List;

// region 文化关联村庄全景大窗口面板类
/**
 * [醒目新注释]
 * 点击 "+" 后唤起的全景大窗口 JEI 分类 {@link CultureVillagesCategory}。
 */
public class CultureVillagesCategory extends BaseMillCategory<Culture> {
    public static final RecipeType<Culture> RECIPE_TYPE = RecipeType.create("millenaire", "culture_villages_full", Culture.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    public CultureVillagesCategory(IGuiHelper guiHelper, IJeiRuntime jeiRuntime) {
        super(guiHelper, guiHelper.createDrawableItemStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK)));
    }

    public CultureVillagesCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK)));
    }

    @Override public RecipeType<Culture> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.literal("文化关联村庄 (全景大窗口)"); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        List<VillageType> villages = MillenaireJeimm.getVillageFormCulture(recipe);
        if (villages == null || villages.isEmpty()) return;

        int startX = 14;
        int startY = 24;

        for (int i = 0; i < Math.min(villages.size(), PAGE_SIZE); i++) {
            VillageType village = villages.get(i);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = startX + (col * SLOT_SIZE);
            int slotY = startY + (row * SLOT_SIZE);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX + 1, slotY + 1)
                    .addIngredient(MillIngredientTypes.VILLAGE, village);
        }
    }

    @Override
    public void draw(Culture recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        String displayName = recipe.displayName() != null ? recipe.displayName() : recipe.id().getPath();
        guiGraphics.drawString(font, "§1所属文化: §f" + displayName, 14, 8, 0x000000, false);

        List<VillageType> villages = MillenaireJeimm.getVillageFormCulture(recipe);
        int total = villages != null ? villages.size() : 0;

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