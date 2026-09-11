// region 建筑详情分类定义与构造函数
package com.momos.millenairejeimm.jei.Runtime.buildingof;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import com.momos.millenairejeimm.jei.Runtime.MillIconOnlyRenderer;
import mezz.jei.api.constants.VanillaTypes;
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

/**
 * 建筑计划集 {@link BuildingPlanSet} JEI 详情分类，继承自 {@link BaseMillCategory}。
 */
public class BuildingCategory extends BaseMillCategory<BuildingPlanSet> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_details", BuildingPlanSet.class);

    private static final MillIconOnlyRenderer<BuildingPlanSet> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    // [醒目新注释] 关联布局与三项核心 LinkBox 组件定义
    private final JeiLinkLayout buildingLayout;
    private final JeiLinkBox<VillageType> villageBox;
    private final JeiLinkBox<VillagerType> villagerBox;
    private final JeiLinkBox<ItemStack> resourceBox;

    // [醒目新注释] 构造函数升级：引入 {@link IIngredientManager} 以支持原生 ItemStack 模式与 LinkBox 交互
    public BuildingCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager) {
        super(guiHelper, guiHelper.createDrawableItemStack(
                new ItemStack(ItemHelper.resolve("minecraft:glass_bottle") != null ? ItemHelper.resolve("minecraft:glass_bottle") : Items.GLASS_BOTTLE)));

        // [醒目新注释] 1. 建筑所属村庄 LinkBox (Ingredient: VILLAGE)
        this.villageBox = new JeiLinkBox<VillageType>("包含此建筑的村庄")
                .useJeiMode(ingredientManager, MillIngredientTypes.VILLAGE)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof BuildingPlanSet building) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.BUILDING, building);
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                    }
                });

        // [醒目新注释] 2. 居住/工作村民 LinkBox (Ingredient: VILLAGER)
        this.villagerBox = new JeiLinkBox<VillagerType>("居住或工作村民")
                .useJeiMode(ingredientManager, MillIngredientTypes.VILLAGER)
                .setOnPlusClick((contextObject, items) -> {
                    if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof BuildingPlanSet building) {
                        var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory()
                                .createFocus(RecipeIngredientRole.INPUT, MillIngredientTypes.BUILDING, building);
                        MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);
                    }
                });

        // [醒目新注释] 3. 升级/建造用量 LinkBox (Vanilla ITEM_STACK 原生物品渲染)
        // MC 原生 GuiGraphics 与 JEI 槽位天然支持 count > 64 的 ItemStack 文本绘制（如 128、256）
        this.resourceBox = new JeiLinkBox<ItemStack>("升级与建造用量")
                .useJeiMode(ingredientManager, VanillaTypes.ITEM_STACK);

        // [醒目新注释] 初始化链接布局，起点坐标 Y: 105，总宽度: 172，项间距: 4
        this.buildingLayout = new JeiLinkLayout(2, 105, 172, 4)
                .addBox(villageBox)
                .addBox(villagerBox)
                .addBox(resourceBox);
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.building_details");
    }
// endregion 建筑详情分类定义与构造函数

    // region JEI 槽位与绘制实现
    // [醒目新注释] 注册配方槽位与 LinkBox 关联数据（村庄、村民、资源消耗）
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.BUILDING, recipe)
                .setCustomRenderer(MillIngredientTypes.BUILDING, ICON_RENDERER);

        // [醒目新注释] 提取关联村庄、村民列表以及升级/建造所需物品用量
        List<VillageType> villages = MillenaireAPIHelper.getVillageFormBuilding(recipe);
        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormBuilding(recipe);
        List<ItemStack> upgradeCosts = getBuildingUpgradeCosts(recipe);

        if (buildingLayout != null) {
            buildingLayout.setRecipeForBox(0, builder, villages);
            buildingLayout.setRecipeForBox(1, builder, villagers);
            buildingLayout.setRecipeForBox(2, builder, upgradeCosts);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        if (buildingLayout != null) buildingLayout.createRecipeExtras(builder, recipe);
    }

    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();

        drawCommonHeader(guiGraphics, font, displayName, recipe.buildingId(), recipe.category(), mouseX, mouseY);

        // [醒目新注释] 正文从 Y: 42 开始紧凑排布
        drawLabelValue(guiGraphics, font, "造价", "§6" + recipe.price(), 6, 42);
        drawLabelValue(guiGraphics, font, "声望需求", "§2" + recipe.reputation(), 6, 52);
        drawLabelValue(guiGraphics, font, "建造上限", "§1" + recipe.maxCount() + " 座", 6, 62);
        drawLabelValue(guiGraphics, font, "搬入优先级", "§d" + recipe.priorityMoveIn(), 6, 72);
        drawLabelValue(guiGraphics, font, "关联文化", recipe.culture() != null ? recipe.culture().getPath() : "通用", 6, 82);

        StringBuilder flags = new StringBuilder();
        if (recipe.isTownHall()) flags.append("[市镇厅] ");
        if (recipe.isResidential()) flags.append("[住宅] ");
        if (recipe.isSubBuilding()) flags.append("[子建筑] ");
        if (recipe.isWallSegment()) flags.append("[城墙] ");
        String flagText = flags.length() > 0 ? flags.toString() : "无";

        drawHoverableText(guiGraphics, font, "标记特性", flagText, 6, 92, mouseX, mouseY);

        // [醒目新注释] 保留原代码注释，并在下方激活 JeiLinkLayout 动态绘制链路
        // (1) 建筑关联所属村庄
        // (2) 居住/工作村民
        // (3) 两级可翻页页签 / 升级资源用量 LinkBox
        if (buildingLayout != null) {
            buildingLayout.render(guiGraphics, font, mouseX, mouseY);
        }
    }

    // [醒目新注释] 声明资源用量接口存根，便于后续对接 {@link BuildingPlanSet} 真实等级与材料解析逻辑
    /**
     * 获取建筑计划集 {@link BuildingPlanSet} 升级/建造所需的资源消耗列表。
     *
     * @param building 建筑计划集对象 {@link BuildingPlanSet}
     * @return 升级消耗的 {@link ItemStack} 列表
     */
    public static List<ItemStack> getBuildingUpgradeCosts(BuildingPlanSet building) {
        List<ItemStack> costs = new ArrayList<>();
        if (building == null) return costs;

        // [醒目新注释] 临时分配测试道具（验证数字大于 64 的大额 ItemStack 堆叠显示）
        costs.add(new ItemStack(Items.OAK_LOG, 128));       // 测试：128 个橡木原木
        costs.add(new ItemStack(Items.COBBLESTONE, 256));   // 测试：256 个圆石
        costs.add(new ItemStack(Items.IRON_INGOT, 32));     // 测试：32 个铁锭
        costs.add(new ItemStack(Items.GLASS, 64));          // 测试：64 个玻璃
        costs.add(new ItemStack(Items.BRICKS, 96));         // 测试：96 个砖块

        return costs;
    }
// endregion JEI 槽位与绘制实现
}