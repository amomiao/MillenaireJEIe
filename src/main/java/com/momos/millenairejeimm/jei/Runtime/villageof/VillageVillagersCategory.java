package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.villageof.link.VillageVillagerLink;
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
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

import java.util.List;

// region 村庄关联村民全景大窗口面板类
/**
 * 展示村庄类型 {@link VillageType} 包含的所有村民职业/类型 {@link VillagerType} 的 JEI 全景大窗口分类。
 */
public class VillageVillagersCategory extends BaseMillCategory<VillageType> {
    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create("millenaire", "village_villagers_full", VillageType.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    /**
     * [醒目新注释]
     * 缓存上一次绘制的 {@link VillageType} 对象，用于判断当前帧是否切换了配方。
     */
    private VillageType lastRecipe;

    /**
     * [醒目新注释]
     * 缓存已构造好的村庄名称文本 {@link Component}，规避渲染循环中的分配开销。
     */
    private Component cachedDisplayNameComponent;

    public VillageVillagersCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new ItemStack(Items.PLAYER_HEAD)));
    }

    @Override public RecipeType<VillageType> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.literal("村庄居住村民 (全景大窗口)"); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addIngredient(MillIngredientTypes.VILLAGE, recipe);
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredient(MillIngredientTypes.VILLAGE_VILLAGER_LINK, new VillageVillagerLink(recipe));

        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(recipe);
        if (villagers == null || villagers.isEmpty()) return;

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

    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        /**
         * [醒目新注释]
         * 优化：配方变化时才重构 {@link Component}，减少 GUI 绘制帧率开销。
         */
        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            this.cachedDisplayNameComponent = Component.literal("§1所属村庄: §f" + recipe.name());
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
// endregion 村庄关联村民全景大窗口面板类