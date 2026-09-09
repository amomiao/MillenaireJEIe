package com.momos.millenairejeimm.jei.villageof;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.culture.VillageType;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.List;

// region 类的定义与静态渲染器
/**
 * 千年村庄类型 {@link VillageType} 的 JEI 详情分类页面[cite: 20]。
 * 结合 {@link DebugGuiUtils} 提供村庄基础数值、城墙防御、生成规则、暗色 Inspector 终端与全量 JSON Dump 功能[cite: 20]。
 */
public class VillageCategory implements IRecipeCategory<VillageType> {

    /** 村庄详情分类的 JEI 唯一 RecipeType 标识 */
    public static final RecipeType<VillageType> RECIPE_TYPE = RecipeType.create("millenaire", "village_details", VillageType.class);

    private final IDrawable background;
    private final IDrawable icon;

    /**
     * 村庄成分专用“仅图标”渲染器，解析 {@link VillageType#icon()}[cite: 20]。
     */
    private static final MillIconOnlyRenderer<VillageType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });
// endregion 类的定义与静态渲染器

// region 构造函数与 JEI 分类初始化
    /**
     * 构建村庄 JEI 分类页面[cite: 20]。
     *
     * @param guiHelper JEI GUI 辅助接口 {@link IGuiHelper}[cite: 20]
     */
    public VillageCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 160x110 背景扩展至 160x140，底端留出空间用于展现开发者 Debug 控制台与全量属性 Inspector[cite: 20]
        this.background = guiHelper.createBlankDrawable(160, 140);

        Item travelBookItem = ItemHelper.resolve("millenaire:travel_book");
        if (travelBookItem == null) {
            travelBookItem = ItemHelper.resolve("millenaire:norman_amulet");
        }
        ItemStack iconStack = (travelBookItem != null) ? new ItemStack(travelBookItem) : new ItemStack(Items.MAP);

        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<VillageType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.village_details");
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
        // [醒目新注释] 同步修改面板总高度为 140px[cite: 20]
        return 140;
    }
// endregion 构造函数与 JEI 分类初始化

    // region JEI 槽位与成分配置 (setRecipe)
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillageType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // [醒目新注释] 左上角 (6, 6) 注册村庄查询成分，支持玩家在 JEI 中按 R/U 快捷键关联[cite: 20]
        // [醒目新注释] 为槽位绑定自定义仅图标渲染器，阻止触发 JEI 侧边栏携带的文字渲染[cite: 20]
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGE, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGE, ICON_RENDERER);
    }
// endregion JEI 槽位与成分配置 (setRecipe)

    // region 面板渲染与 Inspector Tooltip 数据 Dump (draw)
    @Override
    public void draw(VillageType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        // 1. 自定义图标与名称[cite: 20]
        // [醒目新注释] 槽位图标已通过 setCustomRenderer 渲染完毕，无需再次 renderFakeItem[cite: 20]

        String displayName = recipe.name() != null && !recipe.name().isEmpty() ? recipe.name() : recipe.id().getPath();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);

        // [醒目新注释] 绘制标题行右侧的极简 JSON Dump 开发者徽章按钮 (X: 128, Y: 6)[cite: 20]
        DebugGuiUtils.drawJsonDevButton(guiGraphics, font, 128, 6, mouseX, mouseY);

        // 2. 所属文化与村庄类型[cite: 20]
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";
        String typeStr = recipe.isMarvel() ? "奇观" : (recipe.isHamlet() ? "哈姆雷特/子村" : (recipe.loneBuilding() ? "独立建筑" : "常规村庄"));
        guiGraphics.drawString(font, Component.literal("§8" + culture + " | " + typeStr), 28, 18, 0x444444, false);

        // 3. 分隔线[cite: 20]
        guiGraphics.fill(6, 28, 154, 29, 0xFFCCCCCC);

        // 4. 范围与同时建造数[cite: 20]
        guiGraphics.drawString(font, Component.literal("作用半径: §2" + recipe.radius() + "m §r| 并行建造上限: §1" + recipe.maxSimultaneousConstructions()), 6, 34, 0x333333, false);

        // 5. 生成权重与出生点最小距离[cite: 20]
        guiGraphics.drawString(font, Component.literal("生成权重: §6" + recipe.weight() + " §r| 距出生点: §c" + recipe.minDistanceFromSpawn() + "m"), 6, 48, 0x333333, false);

        // 6. 城墙防护与袭营机制[cite: 20]
        StringBuilder wallInfo = new StringBuilder("防御属性: ");
        if (recipe.outerWallType() != null) wallInfo.append("§2[外城墙] ");
        if (recipe.innerWallType() != null) wallInfo.append("§a[内城墙] ");
        if (recipe.carriesRaid()) wallInfo.append("§c[袭营] ");
        if (recipe.outerWallType() == null && recipe.innerWallType() == null && !recipe.carriesRaid()) {
            wallInfo.append("§8无防具/无袭营");
        }
        guiGraphics.drawString(font, Component.literal(wallInfo.toString()), 6, 62, 0x333333, false);

        // 7. 玩家控制与额外建筑扩展[cite: 20]
        String ctrlText = recipe.playerControlled() ? "§d[玩家控制]" : "§8[自主发展]";
        String extraText = recipe.allowExtraBuildings() ? " §b[允许扩建]" : "";
        guiGraphics.drawString(font, Component.literal("控制规则: " + ctrlText + extraText), 6, 76, 0x333333, false);

        // 8. 图鉴收录标记[cite: 20]
        if (recipe.travelBookDisplay()) {
            guiGraphics.drawString(font, Component.literal("§1✔ 已收录至千年图鉴"), 6, 90, 0x1E90FF, false);
        }

        // [醒目新注释] 绘制暗色 Debug 终端控制台 (Y: 78~132)，利用 {@link DebugGuiUtils} 通用面板绘制[cite: 20]
        DebugGuiUtils.drawDebugConsoleBackground(guiGraphics, 6, 78, 148, 56);
        guiGraphics.drawString(font, Component.literal("§a[VILLAGE DEV INSPECTOR]"), 10, 82, 0x00FF00, false);

        int layoutCount = recipe.layout() != null ? recipe.layout().size() : 0;
        int hamletsCount = recipe.hamlets() != null ? recipe.hamlets().size() : 0;
        guiGraphics.drawString(font, Component.literal("§7LayoutSlots: §f" + layoutCount + " §7| Hamlets: §f" + hamletsCount), 10, 93, 0xCCCCCC, false);

        String wallDetails = (recipe.outerWallType() != null ? recipe.outerWallType().getPath() : "无") + " / " + (recipe.innerWallType() != null ? recipe.innerWallType().getPath() : "无");
        guiGraphics.drawString(font, Component.literal("§7Walls (Out/In): §f" + wallDetails), 10, 104, 0xCCCCCC, false);
        guiGraphics.drawString(font, Component.literal("§e💡 悬停此处或点击上方 JSON 按钮"), 10, 118, 0xFFCC00, false);

        // [醒目新注释] 深度 Dump VillageType 的 40+ 字段信息，生成悬浮 Hover Inspector Tooltip[cite: 20]
        List<Component> debugTooltip = new ArrayList<>();
        debugTooltip.add(Component.literal("§a=== VillageType Record Field Inspector ==="));
        debugTooltip.add(Component.literal("§7ID {@link VillageType#id()}: §f" + recipe.id()));
        debugTooltip.add(Component.literal("§7Culture {@link VillageType#culture()}: §f" + recipe.culture()));
        debugTooltip.add(Component.literal("§7Name / NameList: §f" + recipe.name() + " / " + recipe.nameList()));
        debugTooltip.add(Component.literal("§7SpecialType / Icon: §f" + recipe.specialType() + " / " + recipe.icon()));

        // 生成规则与限制 Dump[cite: 20]
        debugTooltip.add(Component.literal("§e--- 生成规则与地理限制 ---"));
        debugTooltip.add(Component.literal(" §7Weight / Spawnable: §f" + recipe.weight() + " / " + recipe.spawnable()));
        debugTooltip.add(Component.literal(" §7MinDistanceFromSpawn / Max: §f" + recipe.minDistanceFromSpawn() + "m / " + recipe.max()));
        debugTooltip.add(Component.literal(" §7BiomeValidityThreshold: §f" + recipe.minimumBiomeValidity()));
        debugTooltip.add(Component.literal(" §7BiomeTags: §f" + recipe.biomeTags()));
        debugTooltip.add(Component.literal(" §7KeyLoneBuilding / Tag: §f" + recipe.keyLoneBuilding() + " / " + recipe.keyLoneBuildingGenerateTag()));
        debugTooltip.add(Component.literal(" §7GeneratedForPlayer / PlayerControlled: §f" + recipe.generatedForPlayer() + " / " + recipe.playerControlled()));

        // 城墙与防卫系统 Dump[cite: 20]
        debugTooltip.add(Component.literal("§c--- 城墙与防御系统 ---"));
        debugTooltip.add(Component.literal(" §7OuterWallType {@link VillageType#outerWallType()}: §f" + recipe.outerWallType()));
        debugTooltip.add(Component.literal(" §7InnerWallType {@link VillageType#innerWallType()}: §f" + recipe.innerWallType() + " (Radius: " + recipe.innerWallRadius() + "m)"));
        debugTooltip.add(Component.literal(" §7MaxSimultaneousWallConstructions: §f" + recipe.maxSimultaneousWallConstructions()));
        debugTooltip.add(Component.literal(" §7CarriesRaid / BannerJsons: §f" + recipe.carriesRaid() + " / " + (recipe.bannerJsons() != null ? recipe.bannerJsons().size() : 0) + " 个"));

        // 布局模板 Slot Dump[cite: 20]
        if (recipe.layout() != null && !recipe.layout().isEmpty()) {
            debugTooltip.add(Component.literal("§b--- 建筑布局槽位 {@link VillageType#layout()} (" + recipe.layout().size() + " 个) ---"));
            for (int i = 0; i < Math.min(recipe.layout().size(), 5); i++) {
                VillageType.LayoutSlot slot = recipe.layout().get(i);
                debugTooltip.add(Component.literal(" §7Slot[" + i + "]: §f" + slot.plan().getPath() + " §8(Role: " + slot.role() + ", Prio: " + slot.priority() + ")"));
            }
            if (recipe.layout().size() > 5) {
                debugTooltip.add(Component.literal(" §8... 剩余 " + (recipe.layout().size() - 5) + " 项 Slot 省略"));
            }
        }

        // 地形判定限定 TerrainQualifiers Dump[cite: 20]
        if (recipe.terrainQualifiers() != null) {
            debugTooltip.add(Component.literal("§d--- 地形需求限定 (TerrainQualifiers) ---"));
            debugTooltip.add(Component.literal(" §7Forest / Hill / Mountain: §f" + recipe.forestQualifier() + " / " + recipe.hillQualifier() + " / " + recipe.mountainQualifier()));
            debugTooltip.add(Component.literal(" §7Desert / Lava / Lake / Ocean: §f" + recipe.desertQualifier() + " / " + recipe.lavaQualifier() + " / " + recipe.lakeQualifier() + " / " + recipe.oceanQualifier()));
        }

        // 价格覆写与材质列表 Dump[cite: 20]
        if (recipe.sellingPriceOverrides() != null && !recipe.sellingPriceOverrides().isEmpty()) {
            debugTooltip.add(Component.literal("§6--- 出售价格覆写 (" + recipe.sellingPriceOverrides().size() + " 项) ---"));
            recipe.sellingPriceOverrides().forEach((item, price) -> debugTooltip.add(Component.literal(" §7- §f" + item + ": " + price + " 钱币")));
        }
        if (recipe.buyingPriceOverrides() != null && !recipe.buyingPriceOverrides().isEmpty()) {
            debugTooltip.add(Component.literal("§6--- 收购价格覆写 (" + recipe.buyingPriceOverrides().size() + " 项) ---"));
            recipe.buyingPriceOverrides().forEach((item, price) -> debugTooltip.add(Component.literal(" §7- §f" + item + ": " + price + " 钱币")));
        }

        // 其它补充属性[cite: 20]
        debugTooltip.add(Component.literal("§1--- 关联建筑与扩展 ---"));
        debugTooltip.add(Component.literal(" §7PlayerBuildings: §f" + recipe.playerBuildings()));
        debugTooltip.add(Component.literal(" §7NeverBuildings: §f" + recipe.neverBuildings()));
        debugTooltip.add(Component.literal(" §7Hamlets: §f" + recipe.hamlets()));
        debugTooltip.add(Component.literal(" §7PathMaterials: §f" + recipe.pathMaterials()));

        // 绘制 Inspector 浮窗[cite: 20]
        DebugGuiUtils.renderHoverInspector(font, guiGraphics, debugTooltip, mouseX, mouseY, 6, 78, 148, 56);
    }
// endregion 面板渲染与 Inspector Tooltip 数据 Dump (draw)

// region 用户交互与点击响应逻辑 (handleInput)
    /**
     * [醒目新注释] 拦截 JEI 输入事件，监听鼠标左键点击右上角 JSON 开发者按钮，弹窗呼出全量 JSON Dump 视图[cite: 20]。
     *
     * @param recipe 当前渲染的 {@link VillageType} 村庄配方对象
     * @param mouseX 鼠标当前相对 X 坐标
     * @param mouseY 鼠标当前相对 Y 坐标
     * @param input 点击或按键输入类型 {@link InputConstants.Key}
     * @return 若该事件被捕获消费则返回 true
     */
    @Override
    @SuppressWarnings("removal")
    public boolean handleInput(VillageType recipe, double mouseX, double mouseY, InputConstants.Key input) {
        // [醒目新注释] 监听鼠标左键按下 (Type.MOUSE, Value: 0)[cite: 20]
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            if (DebugGuiUtils.handleJsonButtonClick(recipe, mouseX, mouseY, 128, 6)) {
                return true;
            }
        }
        return IRecipeCategory.super.handleInput(recipe, mouseX, mouseY, input);
    }
// endregion 用户交互与点击响应逻辑 (handleInput)
}