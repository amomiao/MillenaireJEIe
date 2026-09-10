package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.type.JeiLinkBox;
import com.momos.millenairejeimm.jei.type.layout.VillageLinkLayout;
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

    private final VillageLinkLayout villageLayout;
    private final JeiLinkBox<BuildingPlanSet> buildingBox;
    private final JeiLinkBox<VillagerType> villagerBox;

    public VillageCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.MAP)));

        // [醒目新注释] 1. 点击建筑 LinkBox "+": 使用 CATALYST 避开当前 VillageCategory，结合 BUILDING 焦点排除村民全景窗口，实现单页签高亮
        // [醒目新注释] 修复：改用与 CultureCategory 一致的单一焦点范式。使用 RecipeIngredientRole.INPUT 角色的 MillIngredientTypes.VILLAGE 焦点，
        // [醒目新注释] 精准且唯一匹配绑定了 INPUT 角色村庄隐形成分的全景建筑分类 {@link VillageBuildingsCategory}。
        this.buildingBox = new JeiLinkBox<BuildingPlanSet>("村庄包含建筑")
                .useJeiMode(ingredientManager, MillIngredientTypes.BUILDING)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof VillageType village) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.VILLAGE, village);

                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                    }
                });

        // [醒目新注释] 2. 点击村民 LinkBox "+": 使用 CATALYST + VILLAGER 组合 Focus，精准跳转并聚焦 VillageVillagersCategory 中的对应村庄
        // [醒目新注释] 修复：使用 RecipeIngredientRole.CATALYST 角色的 MillIngredientTypes.VILLAGE 焦点，
        // [醒目新注释] 精准匹配绑定了 CATALYST 角色村庄隐形成分的全景村民分类 {@link VillageVillagersCategory}，排除其余分类，实现单页签高亮与精确定位。
        this.villagerBox = new JeiLinkBox<VillagerType>("村庄出现村民")
                .useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof VillageType village) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(RecipeIngredientRole.CATALYST, MillIngredientTypes.VILLAGE, village);

                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                    }
                });

        this.villageLayout = new VillageLinkLayout(2, 115, 172, 4)
                .addBox(buildingBox)
                .addBox(villagerBox);
    }

    @Override public RecipeType<VillageType> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("gui.millenairejeimm.category.village_details"); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        // [醒目新注释] 严禁修改：村庄图标保持 OUTPUT 角色注册
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGE, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGE, ICON_RENDERER);

        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(recipe);
        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(recipe);

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