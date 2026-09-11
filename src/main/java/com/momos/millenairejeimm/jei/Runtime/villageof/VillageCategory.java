package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
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
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.List;

// region 类定义与构造函数
/**
 * 村庄类型 {@link VillageType} JEI 详情分类，继承自 {@link BaseMillCategory}。
 */
public class VillageCategory extends BaseMillCategory<VillageType> {
    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create("millenaire", "village_details", VillageType.class);
    private static final MillIconOnlyRenderer<VillageType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    private final JeiLinkLayout villageLayout;
    private final JeiLinkBox<BuildingPlanSet> buildingBox;
    private final JeiLinkBox<VillagerType> villagerBox;

    public VillageCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.MAP)));

        this.buildingBox = new JeiLinkBox<BuildingPlanSet>("村庄包含建筑").useJeiMode(ingredientManager, MillIngredientTypes.BUILDING);
        this.villagerBox = new JeiLinkBox<VillagerType>("村庄出现村民").useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER);
        this.villageLayout = new JeiLinkLayout(2, 115, 172, 4)
                .addBox(buildingBox)
                .addBox(villagerBox);
    }

    @Override public RecipeType<VillageType> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("gui.millenairejeimm.category.village_details"); }

    // [醒目新注释] 结合当前 JEI Focus 组对 LinkBox 内建筑与村民排序进行过滤置顶
    /**
     * 设置村庄详情分类的 JEI 槽位与 LinkBox 内容。
     *
     * @param builder JEI 配方布局构建器 {@link IRecipeLayoutBuilder}
     * @param recipe 当前村庄类型 {@link VillageType}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGE, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGE, ICON_RENDERER);

        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(recipe);
        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(recipe);

        // [醒目新注释] 如果存在反查焦点（如玩家点击特定 Building 或 Villager），将其优先排序至前排
        if (focuses != null && !focuses.isEmpty()) {
            if (buildings != null && !buildings.isEmpty()) {
                List<BuildingPlanSet> focusedBuildings = focuses.getFocuses(MillIngredientTypes.BUILDING)
                        .map(f -> f.getTypedValue().getIngredient())
                        .toList();
                if (!focusedBuildings.isEmpty()) {
                    buildings = new ArrayList<>(buildings);
                    buildings.sort((a, b) -> Boolean.compare(focusedBuildings.contains(b), focusedBuildings.contains(a)));
                }
            }

            if (villagers != null && !villagers.isEmpty()) {
                List<VillagerType> focusedVillagers = focuses.getFocuses(MillIngredientTypes.VILLAGER)
                        .map(f -> f.getTypedValue().getIngredient())
                        .toList();
                if (!focusedVillagers.isEmpty()) {
                    villagers = new ArrayList<>(villagers);
                    villagers.sort((a, b) -> Boolean.compare(focusedVillagers.contains(b), focusedVillagers.contains(a)));
                }
            }
        }

        if (villageLayout != null) {
            villageLayout.setRecipeForBox(0, builder, buildings);
            villageLayout.setRecipeForBox(1, builder, villagers);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, VillageType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (villageLayout != null) villageLayout.createRecipeExtras(builder, recipe);
    }

    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;
        String displayName = recipe.name() != null && !recipe.name().isEmpty() ? recipe.name() : recipe.id().getPath();
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), culture, mouseX, mouseY);

        String typeStr = recipe.isMarvel() ? "奇观" : (recipe.isHamlet() ? "哈姆雷特/子村" : (recipe.loneBuilding() ? "独立建筑" : "常规村庄"));
        drawLabelValue(guiGraphics, font, "类型分类", typeStr, 6, 42);
        drawLabelValue(guiGraphics, font, "作用半径", "§2" + recipe.radius() + "m", 6, 52);
        drawLabelValue(guiGraphics, font, "并行建造上限", "§1" + recipe.maxSimultaneousConstructions(), 6, 62);
        drawLabelValue(guiGraphics, font, "生成权重", "§6" + recipe.weight(), 6, 72);
        drawLabelValue(guiGraphics, font, "距出生点最小距离", "§c" + recipe.minDistanceFromSpawn() + "m", 6, 82);

        StringBuilder wallInfo = new StringBuilder();
        if (recipe.outerWallType() != null) wallInfo.append("[外城墙] ");
        if (recipe.innerWallType() != null) wallInfo.append("[内城墙] ");
        if (recipe.carriesRaid()) wallInfo.append("[袭营机制] ");
        String defenseText = wallInfo.length() > 0 ? wallInfo.toString() : "无防御结构";

        drawHoverableText(guiGraphics, font, "防御属性", defenseText, 6, 92, mouseX, mouseY);
        drawLabelValue(guiGraphics, font, "控制规则", recipe.playerControlled() ? "§d玩家控制" : "§8自主发展", 6, 102);

        if (villageLayout != null) villageLayout.render(guiGraphics, font, mouseX, mouseY);
    }
}