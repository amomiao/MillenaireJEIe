package com.momos.millenairejeimm.jei.Runtime.villageof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.type.JeiLinkBox;
// [醒目新注释] 引入专用的 VillageLinkLayout 布局容器
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
    // static
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

    /**
     * [醒目新注释] 带有 {@link IIngredientManager} 的构造函数，用于初始化建筑与村民的 {@link JeiLinkBox} 槽位组件与联动布局。
     * @param guiHelper          JEI GUI 辅助接口 {@link IGuiHelper}
     * @param ingredientManager  JEI 成分管理器 {@link IIngredientManager}
     */
    public VillageCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("millenaire:travel_book") != null ? ItemHelper.resolve("millenaire:travel_book") : Items.MAP)));

        // [醒目新注释] 1. 初始化村庄包含建筑 LinkBox，同时传入 INPUT 与 OUTPUT 双向 Focus 解决全景窗口打开后无法自动聚焦当前村庄的 Bug
        this.buildingBox = new JeiLinkBox<BuildingPlanSet>("村庄包含建筑")
                .useJeiMode(ingredientManager, MillIngredientTypes.BUILDING)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof VillageType village) {
                        var focusFactory = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory();
                        var focusInput = focusFactory.createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.VILLAGE, village);
                        var focusOutput = focusFactory.createFocus(RecipeIngredientRole.OUTPUT, MillIngredientTypes.VILLAGE, village);
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(List.of(focusInput, focusOutput));
                    }
                });

        // [醒目新注释] 2. 初始化村庄出现村民 LinkBox，同时传入 INPUT 与 OUTPUT 双向 Focus，确保全景大窗口精准过滤并聚焦到该村庄
        this.villagerBox = new JeiLinkBox<VillagerType>("村庄出现村民")
                .useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof VillageType village) {
                        var focusFactory = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory();
                        var focusInput = focusFactory.createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.VILLAGE, village);
                        var focusOutput = focusFactory.createFocus(RecipeIngredientRole.OUTPUT, MillIngredientTypes.VILLAGE, village);
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(List.of(focusInput, focusOutput));
                    }
                });

        // [醒目新注释] 3. 使用 VillageLinkLayout 管理建筑与村民 LinkBox，起始坐标 Y: 115
        this.villageLayout = new VillageLinkLayout(2, 115, 172, 4)
                .addBox(buildingBox)
                .addBox(villagerBox);
    }

    @Override
    public RecipeType<VillageType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        // [醒目新注释] 修正语言文件 Key 拼写（millenairejeim -> millenairejeimm），解决左上角未翻译 Raw Key 的显示问题
        return Component.translatable("gui.millenairejeimm.category.village_details");
    }
// endregion 类定义与构造函数

    // region JEI 槽位与绘制实现
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;
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
        if (villageLayout != null) {
            villageLayout.createRecipeExtras(builder, recipe);
        }
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

        if (villageLayout != null) {
            villageLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }
}
// endregion JEI 槽位与绘制实现