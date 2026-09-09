package com.momos.millenairejeimm.jei.buildingof;

import com.mojang.blaze3d.platform.InputConstants;
import com.momos.millenairejeimm.jei.DebugGuiUtils;
import com.momos.millenairejeimm.jei.MillIconOnlyRenderer;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.List;

// region 类的定义与静态渲染器配置
/**
 * 开发者调试导向的千年村庄建筑计划集 {@link BuildingPlanSet} JEI 详情分类页面[cite: 18]。
 * 提供建筑基础数值参数、布尔标志 Badge、交互式暗色终端以及集成 {@link DebugGuiUtils} 的全量 JSON Dump 支持。
 */
public class BuildingCategory implements IRecipeCategory<BuildingPlanSet> {

    public static final RecipeType<BuildingPlanSet> RECIPE_TYPE = RecipeType.create("millenaire", "building_details", BuildingPlanSet.class);

    private final IDrawable background;
    private final IDrawable icon;

    private static final MillIconOnlyRenderer<BuildingPlanSet> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });
// endregion 类的定义与静态渲染器配置

// region 构造函数与 JEI 元数据接口回调
    /**
     * 构造 JEI 建筑分类 Category 面板，初始化图形助手与背景图。
     *
     * @param guiHelper JEI GUI 界面助手 {@link IGuiHelper}
     */
    public BuildingCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 扩展页面尺寸至 160x140，满足开发者 Debug 高密度显示需求[cite: 18]
        this.background = guiHelper.createBlankDrawable(160, 140);

        Item buildingIconItem = ItemHelper.resolve("minecraft:glass_bottle");
        ItemStack iconStack = (buildingIconItem != null) ? new ItemStack(buildingIconItem) : new ItemStack(Items.GLASS_BOTTLE);
        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<BuildingPlanSet> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.building_details");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return 160;
    }

    @Override
    public int getHeight() {
        return 140;
    }
// endregion 构造函数与 JEI 元数据接口回调

    // region JEI 槽位构建 (setRecipe)
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BuildingPlanSet recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.BUILDING, recipe)
                .setCustomRenderer(MillIngredientTypes.BUILDING, ICON_RENDERER);
    }
// endregion JEI 槽位构建 (setRecipe)

    // region Category 界面绘制主逻辑 (draw)
    @Override
    public void draw(BuildingPlanSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        // 1. 顶部 Header 区 (Y: 6~22)[cite: 18]
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.buildingId();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);
        guiGraphics.drawString(font, Component.literal("§8ID: " + recipe.buildingId() + " | 类别: " + recipe.category()), 28, 17, 0x555555, false);

        // [醒目新注释] 绘制标题行右侧的极简 JSON Dump 开发者徽章按钮 (X: 128, Y: 6)[cite: 18]
        DebugGuiUtils.drawJsonDevButton(guiGraphics, font, 128, 6, mouseX, mouseY);

        guiGraphics.fill(6, 27, 154, 28, 0xFFCCCCCC);

        // 2. 基础数值参数网格 (Y: 30~58)[cite: 18]
        guiGraphics.drawString(font, Component.literal("造价: §6" + recipe.price() + "§r | 声望需求: §2" + recipe.reputation()), 6, 30, 0x333333, false);
        guiGraphics.drawString(font, Component.literal("最大上限: §1" + recipe.maxCount() + " 座§r | 搬入优先级: §d" + recipe.priorityMoveIn()), 6, 41, 0x333333, false);
        guiGraphics.drawString(font, Component.literal("关联文化: §8" + (recipe.culture() != null ? recipe.culture() : "通用/未指定")), 6, 52, 0x333333, false);

        // 3. 布尔属性状态徽章 Badges (Y: 64~76)[cite: 18]
        StringBuilder flagBadges = new StringBuilder("标记: ");
        flagBadges.append(recipe.isTownHall() ? "§c[市镇厅] " : "§8[非市镇厅] ");
        flagBadges.append(recipe.isResidential() ? "§a[住宅] " : "§8[非住宅] ");
        flagBadges.append(recipe.isSubBuilding() ? "§e[子建筑] " : "");
        flagBadges.append(recipe.isWallSegment() ? "§7[城墙] " : "");
        guiGraphics.drawString(font, Component.literal(flagBadges.toString()), 6, 64, 0x333333, false);

        // 4. 暗色 Debug 终端控制台 (Y: 78~132)[cite: 18]
        DebugGuiUtils.drawDebugConsoleBackground(guiGraphics, 6, 78, 148, 56);
        guiGraphics.drawString(font, Component.literal("§a[DEV DEBUG INSPECTOR]"), 10, 82, 0x00FF00, false);
        guiGraphics.drawString(font, Component.literal("§7Raw Icon: §f" + (recipe.icon() != null ? recipe.icon() : "N/A")), 10, 93, 0xCCCCCC, false);

        // [醒目新注释] 将原错误的 recipe.plans() 替换为 {@link BuildingPlanSet#variants()} 变体字典的大小[cite: 18]
        int variantCount = recipe.variants() != null ? recipe.variants().size() : 0;
        guiGraphics.drawString(font, Component.literal("§7Plan Variations: §f" + variantCount + " 个变体"), 10, 104, 0xCCCCCC, false);
        guiGraphics.drawString(font, Component.literal("§e💡 悬停此处或点击上方 JSON 按钮"), 10, 118, 0xFFCC00, false);

        // 5. 悬浮 Inspector Pop-over (全量 Debug 属性提示框)[cite: 18]
        List<Component> debugTooltip = new ArrayList<>();
        debugTooltip.add(Component.literal("§a=== BuildingPlanSet Debug Inspector ==="));
        debugTooltip.add(Component.literal("§7Memory Class: §f" + recipe.getClass().getName()));
        debugTooltip.add(Component.literal("§7Building ID: §f" + recipe.buildingId()));
        debugTooltip.add(Component.literal("§7Native Name: §f" + recipe.nativeName()));
        debugTooltip.add(Component.literal("§7Category: §f" + recipe.category()));
        debugTooltip.add(Component.literal("§7Price / Rep: §f" + recipe.price() + " / " + recipe.reputation()));
        debugTooltip.add(Component.literal("§7MaxCount / Priority: §f" + recipe.maxCount() + " / " + recipe.priorityMoveIn()));
        debugTooltip.add(Component.literal("§7IsTownHall: §f" + recipe.isTownHall()));
        debugTooltip.add(Component.literal("§7IsResidential: §f" + recipe.isResidential()));
        debugTooltip.add(Component.literal("§7IsSubBuilding: §f" + recipe.isSubBuilding()));
        debugTooltip.add(Component.literal("§7IsWallSegment: §f" + recipe.isWallSegment()));

        /**
         * [醒目新注释] 展开打印 {@link BuildingPlanSet#variants()} 中每个变体及其对应等级 {@link BuildingPlanSet.LevelDef} 的图纸标识[cite: 18]
         */
        if (recipe.variants() != null && !recipe.variants().isEmpty()) {
            debugTooltip.add(Component.literal("§e--- 建筑变体与等级列表 (Variants Dump) ---"));
            recipe.variants().forEach((variantKey, levelList) -> {
                int levelCount = levelList != null ? levelList.size() : 0;
                debugTooltip.add(Component.literal(" §7Variant [" + variantKey + "]: §f" + levelCount + " 个等级"));
                if (levelList != null) {
                    for (BuildingPlanSet.LevelDef levelDef : levelList) {
                        debugTooltip.add(Component.literal("   §8- Lv" + levelDef.level() + " Plan: §f" + (levelDef.planId() != null ? levelDef.planId() : "N/A")));
                    }
                }
            });
        }

        DebugGuiUtils.renderHoverInspector(font, guiGraphics, debugTooltip, mouseX, mouseY, 6, 78, 148, 56);
    }
// endregion Category 界面绘制主逻辑 (draw)

// region 用户交互与点击响应逻辑 (handleInput)
    /**
     * 拦截 JEI 输入事件，监听鼠标左键点击右上角 JSON 开发者按钮，弹窗呼出全量 JSON Dump 视图。
     * 注意：在 JEI 19.6.0+ 中 handleInput 被标记为待移除，但仍可正常工作[cite: 20, 21]。此处未命中时直接返回 false，避免接口 super 调用引发的语法异常[cite: 20, 21]。
     * @param recipe 当前渲染的配方/数据对象 {@link T}[cite: 20, 21]
     * @param mouseX 鼠标当前相对 X 坐标[cite: 20, 21]
     * @param mouseY 鼠标当前相对 Y 坐标[cite: 20, 21]
     * @param input 点击或按键输入类型 {@link InputConstants.Key}[cite: 20, 21]
     * @return 若该点击事件被 JSON 按钮拦截并消费则返回 true[cite: 20, 21]
     */
    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(BuildingPlanSet recipe, double mouseX, double mouseY, InputConstants.Key input) {
        // [醒目新注释] 监听鼠标左键按下 (Type.MOUSE, Value: 0)[cite: 20, 21]
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            return DebugGuiUtils.handleJsonButtonClick(recipe, mouseX, mouseY, 128, 6); // 成功响应点击，阻止 JEI 继续响应其他快捷键或事件[cite: 20, 21]
        }
        // [醒目新注释] 替换原本的 IRecipeCategory.super.handleInput(...)，直接返回 false[cite: 20, 21]
        return false;
    }
// endregion 用户交互与点击响应逻辑 (handleInput)
}