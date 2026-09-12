package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
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

public class VillageCategory extends BaseMillCategory<VillageType> {

    // [醒目新注释] 抽离 RecipeType ID、物品 ID 及渲染属性依赖常量
    private static final String RECIPE_TYPE_NAMESPACE = "millenaire";
    private static final String RECIPE_TYPE_PATH = "village_details";
    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create(RECIPE_TYPE_NAMESPACE, RECIPE_TYPE_PATH, VillageType.class);

    private static final String ITEM_TRAVEL_BOOK = "millenaire:travel_book";

    private static final String PREFIX_INNER_WALL = "[内城墙] ";
    private static final String PREFIX_OUTER_WALL = "[外城墙] ";
    private static final String PREFIX_RAID = "[袭营机制] ";

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
                new ItemStack(ItemHelper.resolve(ITEM_TRAVEL_BOOK) != null ? ItemHelper.resolve(ITEM_TRAVEL_BOOK) : Items.MAP)));

        // [醒目新注释] 传入 Component 实例而非调用 .getString()，实现语言实时动态刷新
        this.buildingBox = new JeiLinkBox<BuildingPlanSet>(MillenaireJeimmKeys.getVillageBoxBuildings()).useJeiMode(ingredientManager, MillIngredientTypes.BUILDING);
        this.villagerBox = new JeiLinkBox<VillagerType>(MillenaireJeimmKeys.getVillageBoxVillagers()).useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER);
        this.villageLayout = new JeiLinkLayout(2, 115, 172, 4)
                .addBox(buildingBox)
                .addBox(villagerBox);
    }

    @Override public RecipeType<VillageType> getRecipeType() { return RECIPE_TYPE; }

    @Override
    public Component getTitle() { return MillenaireJeimmKeys.VillageCategory.asComponent(); }

    // [醒目新注释] 提取村庄布局数据同步逻辑，包含焦点组排序计算
    /**
     * 根据当前 {@link VillageType} 配方及焦点强行同步刷新 {@link #villageLayout} 数据。
     *
     * @param recipe  当前村庄类型配方对象 {@link VillageType}
     * @param focuses 当前 JEI 焦点组 {@link IFocusGroup}
     */
    private void updateLayoutData(VillageType recipe, IFocusGroup focuses) {
        if (villageLayout == null || recipe == null) return;

        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(recipe);
        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(recipe);

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

        villageLayout.setBoxData(0, buildings);
        villageLayout.setBoxData(1, villagers);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGE, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGE, ICON_RENDERER);

        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(recipe);
        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(recipe);

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
            villageLayout.Clear();
            updateLayoutData(recipe, focuses);
            villageLayout.setRecipeForBox(0, builder, buildings);
            villageLayout.setRecipeForBox(1, builder, villagers);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, VillageType recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (villageLayout != null) {
            // [醒目新注释] 注册额外拦截器前同步数据
            updateLayoutData(recipe, focuses);
            villageLayout.createRecipeExtras(builder, recipe);
        }
    }

    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;
        Font font = Minecraft.getInstance().font;
        String displayName = recipe.name() != null && !recipe.name().isEmpty() ? recipe.name() : recipe.id().getPath();
        // [醒目新注释] 默认文本改用 {@link MillenaireJeimmKeys#VILLAGE_UNKNOWN_CULTURE} 获取
        String culture = recipe.culture() != null ? recipe.culture().getPath() : MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_UNKNOWN_CULTURE, "未知文化").getString();

        drawCommonHeader(guiGraphics, font, displayName, recipe.id().toString(), culture, mouseX, mouseY);

        // [醒目新注释] 村庄类型描述模板化提取，使用 {@link MillenaireJeimmKeys} 中的 Key
        String typeStr = recipe.isMarvel() ? MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_TYPE_MARVEL, "奇观").getString()
                : (recipe.isHamlet() ? MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_TYPE_HAMLET, "哈姆雷特/子村").getString()
                : (recipe.loneBuilding() ? MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_TYPE_LONE, "独立建筑").getString()
                : MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_TYPE_NORMAL, "常规村庄").getString()));

        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_TYPE_LABEL, "类型分类").getString(), typeStr, 6, 42);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_RADIUS, "作用半径").getString(), "§2" + recipe.radius() + "m", 6, 52);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_MAX_CONSTRUCTIONS, "并行建造上限").getString(), "§1" + recipe.maxSimultaneousConstructions(), 6, 62);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_WEIGHT, "生成权重").getString(), "§6" + recipe.weight(), 6, 72);
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_MIN_DISTANCE_SPAWN, "距出生点最小距离").getString(), "§c" + recipe.minDistanceFromSpawn() + "m", 6, 82);

        StringBuilder wallInfo = new StringBuilder();
        if (recipe.outerWallType() != null) wallInfo.append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_WALL_OUTER, PREFIX_OUTER_WALL).getString());
        if (recipe.innerWallType() != null) wallInfo.append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_WALL_INNER, PREFIX_INNER_WALL).getString());
        if (recipe.carriesRaid()) wallInfo.append(MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_WALL_RAID, PREFIX_RAID).getString());
        String defenseText = wallInfo.length() > 0 ? wallInfo.toString() : MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_WALL_NONE, "无防御结构").getString();

        drawHoverableText(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_DEFENSE, "防御属性").getString(), defenseText, 6, 92, mouseX, mouseY);

        String controlText = recipe.playerControlled() ? MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_CONTROL_PLAYER, "§d玩家控制").getString()
                : MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_CONTROL_AUTO, "§8自主发展").getString();
        drawLabelValue(guiGraphics, font, MillenaireJeimmKeys.translatable(MillenaireJeimmKeys.VILLAGE_CONTROL, "控制规则").getString(), controlText, 6, 102);

        // [醒目新注释] 渲染前按当前配方强行同步数据，防止多配方平铺时渲染错位
        if (villageLayout != null) {
            updateLayoutData(recipe, null);
            villageLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}