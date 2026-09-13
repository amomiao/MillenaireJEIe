package com.momos.millenairejeimm.jei.Runtime.cultureof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.MillenaireJeimm;
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
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;

import java.util.List;

// region 文化关联村庄全景大窗口面板类
/**
 * [醒目新注释]
 * 点击 "+" 后唤起的全景大窗口 JEI 分类 {@link CultureVillagesCategory}。
 */
public class CultureVillagesCategory extends BaseMillCategory<Culture> {
    public static final RecipeType<Culture> RECIPE_TYPE = RecipeType.create(MillIngredientTypes.MOD_ID, MillIngredientTypes.CultureVillagesCategoryPath, Culture.class);

    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLUMNS * ROWS;

    /**
     * [醒目新注释]
     * 缓存上一次绘制的 {@link Culture} 对象，用于判断当前帧是否切换了配方。
     */
    private Culture lastRecipe;

    /**
     * [醒目新注释]
     * 缓存已构造好的文化名称文本 {@link Component}，规避渲染循环中的垃圾分配。
     */
    private Component cachedDisplayNameComponent;

    public CultureVillagesCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK)));
    }

    @Override public RecipeType<Culture> getRecipeType() { return RECIPE_TYPE; }

    // [醒目新注释] 使用 {@link MillenaireJeimmKeys#CATEGORY_CULTURE_VILLAGES_FULL} 替换硬编码标题
    @Override
    public Component getTitle() { return MillenaireJeimmKeys.CultureVillagesCategory.asComponent(); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // [醒目新注释] 正确的 JEI 隐形成分注册方式：使用 {@link IRecipeLayoutBuilder#addInvisibleIngredients} 获得 {@link IIngredientAcceptor}
        // [醒目新注释] 这会将 {@link Culture} 注册入 Focus 索引链，使 JEI 能够聚焦定位，但不会在 GUI 上创建或渲染任何槽位
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addIngredient(MillIngredientTypes.CULTURE, recipe);

        List<VillageType> villages = MillenaireAPIHelper.getVillageFormCulture(recipe);
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

        /**
         * [醒目新注释]
         * 优化：仅在配方示例更改时重新生成 {@link Component}，消除帧循环中的字符串拼接与对象创建开销。
         * 现改用 {@link MillenaireJeimmKeys#BELONGING_CULTURE} 进行参数化本地化组件构造。
         */
        if (recipe != this.lastRecipe) {
            this.lastRecipe = recipe;
            String displayName = recipe.displayName() != null ? recipe.displayName() : recipe.id().getPath();
            this.cachedDisplayNameComponent =  MillenaireJeimmKeys.CultureVillagesCategoryTitle_S.asComponent(displayName);
        }

        guiGraphics.drawString(font, this.cachedDisplayNameComponent, 14, 8, 0x000000, false);

        /**
         * [醒目新注释]
         * 优化：通过 {@link IRecipeSlotsView#getSlotViews()} 直接获取当前已添加的槽位数量，
         * 避免每帧重复调用 {@link MillenaireAPIHelper#getVillageFormCulture(Culture)} 或 {@link MillenaireJeimm#getVillageFormCulture(Culture)}。
         */
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
// endregion 文化关联村庄全景大窗口面板类