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
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

import java.util.ArrayList;
import java.util.List;

// region 村庄关联村民全景大窗口面板类
/**
 * 展示村庄类型 {@link VillageType} 包含的所有村民职业/类型 {@link VillagerType} 的 JEI 全景大窗口分类。
 */
public class VillageVillagersCategory extends BaseMillCategory<VillageType> {

    // [醒目新注释] 抽取 RecipeType 字符串常量与 GUI 颜色/边距数值常量
    private static final String RECIPE_TYPE_NAMESPACE = "millenaire";
    private static final String RECIPE_TYPE_PATH = "village_villagers_full";
    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create(RECIPE_TYPE_NAMESPACE, RECIPE_TYPE_PATH, VillageType.class);

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

    @Override
    public Component getTitle() { return MillenaireJeimmKeys.VillageVillagersCategory.asComponent(); }

    // [醒目新注释] 将隐形成分修改为 CATALYST 角色，形成全景村民分类的独占类型区分
    /**
     * 设置全景村民分类的 JEI 槽位布局与隐形成分。
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe 当前村庄类型对象 {@link VillageType}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(MillIngredientTypes.VILLAGE, recipe);

        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(recipe);
        if (villagers == null || villagers.isEmpty()) return;

        // [醒目新注释] 根据当前焦点过滤/重排，置顶被选中的村民
        if (focuses != null && !focuses.isEmpty()) {
            List<VillagerType> focusedVillagers = focuses.getFocuses(MillIngredientTypes.VILLAGER)
                    .map(f -> f.getTypedValue().getIngredient())
                    .toList();
            if (!focusedVillagers.isEmpty()) {
                villagers = new ArrayList<>(villagers);
                villagers.sort((a, b) -> Boolean.compare(focusedVillagers.contains(b), focusedVillagers.contains(a)));
            }
        }

        for (int i = 0; i < Math.min(villagers.size(), PAGE_SIZE); i++) {
            VillagerType villager = villagers.get(i);
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int slotX = GUI_START_X + (col * SLOT_SIZE);
            int slotY = GUI_START_Y + (row * SLOT_SIZE);

            builder.addSlot(RecipeIngredientRole.INPUT, slotX + 1, slotY + 1)
                    .addIngredient(MillIngredientTypes.VILLAGER, villager);
        }
    }

    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;

        /** 优化：配方变化时才重构 {@link Component}，减少 GUI 绘制帧率开销。*/
        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            // [醒目新注释] 使用 {@link MillenaireJeimmKeys#getVillageLabelBelongs} 格式化显示所属村庄
            this.cachedDisplayNameComponent = MillenaireJeimmKeys.VillageVillagersCategoryTitle_S.asComponent(recipe.name());
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
// endregion 村庄关联村民全景大窗口面板类