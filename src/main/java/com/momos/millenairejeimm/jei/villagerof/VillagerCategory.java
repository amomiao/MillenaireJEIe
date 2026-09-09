package com.momos.millenairejeimm.jei.villagerof;

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
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 千年村庄村民类型 {@link VillagerType} 的 JEI 详情分类页面[cite: 12]。
 */
public class VillagerCategory implements IRecipeCategory<VillagerType> {

    /** 村民详情分类的 JEI 唯一 RecipeType 标识[cite: 12] */
    public static final RecipeType<VillagerType> RECIPE_TYPE = RecipeType.create("millenaire", "villager_details", VillagerType.class);

    private final IDrawable background;
    private final IDrawable icon;

    /**
     * 村民成分专用“仅图标”渲染器，解析 {@link VillagerType#icon()}。
     */
    private static final MillIconOnlyRenderer<VillagerType> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.icon() != null && !recipe.icon().isEmpty()) {
            Item item = ItemHelper.resolve(recipe.icon());
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    });

    /**
     * 构建村民 JEI 分类页面[cite: 12]。
     *
     * @param guiHelper JEI GUI 辅助接口 {@link IGuiHelper}[cite: 12]
     */
    public VillagerCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 创建 160x110 像素底图用于展现村民的原名、战斗与行为标签等属性[cite: 12]
        // [醒目新注释] 扩展页面尺寸至 160x140，提供开发者 Debug 控制台与全量属性悬浮 Inspector
        this.background = guiHelper.createBlankDrawable(160, 140);

        // [醒目新注释] 通过 ItemHelper 解析千年村庄旅行书物品 "millenaire:travel_book"；若未获取到则尝试解析 "norman_amulet" 或回退至原版书籍[cite: 12]
        Item travelBookItem = ItemHelper.resolve("millenaire:travel_book");
        if (travelBookItem == null) {
            travelBookItem = ItemHelper.resolve("millenaire:norman_amulet");
        }
        ItemStack iconStack = (travelBookItem != null) ? new ItemStack(travelBookItem) : new ItemStack(Items.BOOK);

        // [醒目新注释] 使用 guiHelper 将物品堆栈转换为 JEI 标签页分类图标 IDrawable[cite: 12]
        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<VillagerType> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.villager_details");
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
        // [醒目新注释] 高度同步调整为 140px
        return 140;
    }

    /**
     * 在 JEI 布局中设置成分与交互槽位[cite: 12]。
     *
     * @param builder JEI 布局构建器 {@link IRecipeLayoutBuilder}[cite: 12]
     * @param recipe 村民类型对象 {@link VillagerType}[cite: 12]
     * @param focuses 焦点组 {@link IFocusGroup}[cite: 12]
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VillagerType recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // [醒目新注释] 左上角 (6, 6) 放置村民自定义成分，支持玩家通过 JEI 直接查询该村民[cite: 12]
        // [醒目新注释] 显式绑定仅图标渲染器，避免加载全局列表中“图标+名字”引发的叠影[cite: 12]
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.VILLAGER, recipe)
                .setCustomRenderer(MillIngredientTypes.VILLAGER, ICON_RENDERER);
    }

    /**
     * 绘制村民类型 {@link VillagerType} 的详细参数（包含原生名称、文化、性别、血量、攻击力、雇佣价格及战斗行为标记等）[cite: 12]。
     *
     * @param recipe 村民类型对象 {@link VillagerType}[cite: 12]
     * @param recipeSlotsView 槽位视图 {@link IRecipeSlotsView}[cite: 12]
     * @param guiGraphics GUI 渲染上下文 {@link GuiGraphics}[cite: 12]
     * @param mouseX 鼠标 X 轴坐标[cite: 12]
     * @param mouseY 鼠标 Y 轴坐标[cite: 12]
     */
    @Override
    public void draw(VillagerType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        // [醒目新注释] 若 VillagerType 内部定义了 icon() 字符串（如物品ID），解析并在槽位旁渲染该专属图标[cite: 12]
        // [醒目新注释] 槽位图标渲染已收归至 setCustomRenderer 统一调度，此处省略 renderFakeItem 调用的重复逻辑[cite: 12]

        // 1. 渲染村民原名与显示名称[cite: 12]
        String displayName = recipe.nativeName() != null && !recipe.nativeName().isEmpty() ? recipe.nativeName() : recipe.id().getPath();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);

        // 2. 渲染文化与性别/年龄[cite: 12]
        String culture = recipe.culture() != null ? recipe.culture().getPath() : "未知文化";
        String genderText = recipe.gender() != null ? recipe.gender().name() : "未知";
        String ageText = recipe.isChild() ? "幼年/儿童" : "成年";
        guiGraphics.drawString(font, Component.literal("§8" + culture + " | " + genderText + " (" + ageText + ")"), 28, 17, 0x444444, false);

        // 3. 渲染分隔线[cite: 12]
        guiGraphics.fill(6, 27, 154, 28, 0xFFCCCCCC);

        // 4. 渲染基础战斗属性：最大生命值与基础攻击力[cite: 12]
        guiGraphics.drawString(font, Component.literal("生命: §c" + (int) recipe.maxHealth() + " HP §r| 攻击: §4" + recipe.baseAttackStrength() + " §r| 缩放: §b" + recipe.baseScale()), 6, 30, 0x333333, false);

        // 5. 渲染雇佣费用与生成权重[cite: 12]
        String hiringText = recipe.hiringCost() > 0 ? "§6" + recipe.hiringCost() + " 钱币" : "§8不可雇佣";
        guiGraphics.drawString(font, Component.literal("雇佣费用: " + hiringText + " §r| 权重: §1" + recipe.spawnWeight()), 6, 41, 0x333333, false);

        // 6. 解析并渲染行为与战斗标签[cite: 12]
        StringBuilder tagsText = new StringBuilder("特性: ");
        if (recipe.isRaider()) tagsText.append("§c[掠夺] ");
        if (recipe.isHostile()) tagsText.append("§4[敌对] ");
        if (recipe.isHelpInAttacks()) tagsText.append("§9[协防] ");
        if (recipe.isArcher()) tagsText.append("§2[弓手] ");
        if (recipe.isNoTeleport()) tagsText.append("§7[禁传送] ");
        if (recipe.isNoResurrect()) tagsText.append("§8[不可复活] ");
        if (!recipe.isRaider() && !recipe.isHostile() && !recipe.isHelpInAttacks() && !recipe.isArcher()) {
            tagsText.append("§8平民/日常");
        }
        guiGraphics.drawString(font, Component.literal(tagsText.toString()), 6, 52, 0x333333, false);

        // 7. 渲染别名或配置标识 Key[cite: 12]
        if (recipe.altNativeName() != null && !recipe.altNativeName().isEmpty()) {
            guiGraphics.drawString(font, Component.literal("别名: §5" + recipe.altNativeName() + " §8(" + (recipe.altKey() != null ? recipe.altKey() : "") + ")"), 6, 64, 0x333333, false);
        } else if (recipe.villagerConfigKey() != null) {
            guiGraphics.drawString(font, Component.literal("配置标识: §8" + recipe.villagerConfigKey()), 6, 64, 0x555555, false);
        } else {
            // 8. 旅行图鉴收录标记与旅行书持有物渲染[cite: 12]
            if (recipe.travelBookDisplay()) {
                String category = recipe.travelBookCategory() != null ? recipe.travelBookCategory() : "通用";
                String heldItemInfo = recipe.travelBookHeldItem() != null ? " (手持: " + recipe.travelBookHeldItem() + ")" : "";
                guiGraphics.drawString(font, Component.literal("§1✔ 已收录至千年图鉴 (" + category + ")" + heldItemInfo), 6, 64, 0x1E90FF, false);
            }
        }

        // [醒目新注释] 绘制暗色 Debug 终端控制台 (Y: 78~132)，使用通用工具类 {@link DebugGuiUtils}
        DebugGuiUtils.drawDebugConsoleBackground(guiGraphics, 6, 78, 148, 56);
        guiGraphics.drawString(font, Component.literal("§a[VILLAGER DEV INSPECTOR]"), 10, 82, 0x00FF00, false);

        int goalCount = recipe.goals() != null ? recipe.goals().size() : 0;
        int tagCount = recipe.tags() != null ? recipe.tags().size() : 0;
        guiGraphics.drawString(font, Component.literal("§7Model: §f" + recipe.model() + " §7(Goals: §f" + goalCount + "§7, Tags: §f" + tagCount + "§7)"), 10, 93, 0xCCCCCC, false);

        String weaponPath = recipe.defaultWeapon() != null ? recipe.defaultWeapon().getPath() : "无默认武器";
        guiGraphics.drawString(font, Component.literal("§7Weapon: §f" + weaponPath), 10, 104, 0xCCCCCC, false);
        guiGraphics.drawString(font, Component.literal("§e💡 悬停此处查看村民全量数据 Dump"), 10, 118, 0xFFCC00, false);

        // [醒目新注释] 构建全量字段悬浮 Hover Tooltip Inspector 浮窗
        List<Component> debugTooltip = new ArrayList<>();
        debugTooltip.add(Component.literal("§a=== VillagerType Record Field Inspector ==="));
        debugTooltip.add(Component.literal("§7Memory Record: §f" + recipe.getClass().getName()));
        debugTooltip.add(Component.literal("§7ID {@link VillagerType#id()}: §f" + recipe.id()));
        debugTooltip.add(Component.literal("§7Culture {@link VillagerType#culture()}: §f" + recipe.culture()));
        debugTooltip.add(Component.literal("§7Model {@link VillagerType#model()}: §f" + recipe.model() + " (Type: " + recipe.modelType() + ")"));
        debugTooltip.add(Component.literal("§7NativeName {@link VillagerType#nativeName()}: §f" + recipe.nativeName()));
        debugTooltip.add(Component.literal("§7AltNativeName / AltKey: §f" + recipe.altNativeName() + " / " + recipe.altKey()));
        debugTooltip.add(Component.literal("§7Gender / IsChild: §f" + recipe.gender() + " / " + recipe.isChild()));
        debugTooltip.add(Component.literal("§7BaseScale {@link VillagerType#baseScale()}: §f" + recipe.baseScale()));
        debugTooltip.add(Component.literal("§7Health / Attack: §f" + recipe.maxHealth() + " HP / " + recipe.baseAttackStrength()));
        debugTooltip.add(Component.literal("§7DefaultWeapon {@link VillagerType#defaultWeapon()}: §f" + recipe.defaultWeapon()));
        debugTooltip.add(Component.literal("§7HiringCost / SpawnWeight: §f" + recipe.hiringCost() + " / " + recipe.spawnWeight()));
        debugTooltip.add(Component.literal("§7VillagerConfigKey: §f" + recipe.villagerConfigKey()));

        // 名字模板表单 Dump
        debugTooltip.add(Component.literal("§e--- 姓名与后代配置 ---"));
        debugTooltip.add(Component.literal(" §7FirstNameList: §f" + recipe.firstNameList()));
        debugTooltip.add(Component.literal(" §7FamilyNameList: §f" + recipe.familyNameList()));
        debugTooltip.add(Component.literal(" §7Child Types (M/F): §f" + recipe.maleChild() + " / " + recipe.femaleChild() + " (HasChildren: " + recipe.hasChildren() + ")"));

        // AI Goal 目标逻辑 Dump
        if (recipe.goals() != null && !recipe.goals().isEmpty()) {
            debugTooltip.add(Component.literal("§b--- AI Goals 目标表 {@link VillagerType#goals()} ---"));
            for (ResourceLocation goal : recipe.goals()) {
                debugTooltip.add(Component.literal(" §7- §f" + goal));
            }
        }

        // Tags 行为标记 Dump
        if (recipe.tags() != null && !recipe.tags().isEmpty()) {
            debugTooltip.add(Component.literal("§d--- 行为 Tag 标记集 {@link VillagerType#tags()} ---"));
            for (String tag : recipe.tags()) {
                debugTooltip.add(Component.literal(" §7- §f" + tag));
            }
        }

        // 服饰套件 ClothSet Dump
        if (recipe.clothes() != null && !recipe.clothes().isEmpty()) {
            debugTooltip.add(Component.literal("§a--- 服饰层 ClothSet Map {@link VillagerType#clothes()} ---"));
            recipe.clothes().forEach((key, clothSet) -> {
                int l0 = clothSet.layer0() != null ? clothSet.layer0().size() : 0;
                int l1 = clothSet.layer1() != null ? clothSet.layer1().size() : 0;
                debugTooltip.add(Component.literal(" §7Cloth [" + key + "]: §fLayer0=" + l0 + " 纹理, Layer1=" + l1 + " 纹理"));
            });
        }

        // 初始背包与异国商人库存 Dump
        if (recipe.initialInventory() != null && !recipe.initialInventory().isEmpty()) {
            debugTooltip.add(Component.literal("§6--- 初始物品栏 {@link VillagerType#initialInventory()} ---"));
            recipe.initialInventory().forEach((itemRl, count) -> debugTooltip.add(Component.literal(" §7- §f" + itemRl + " x" + count)));
        }

        if (recipe.foreignMerchantStock() != null && !recipe.foreignMerchantStock().isEmpty()) {
            debugTooltip.add(Component.literal("§6--- 外国商人售卖库存 {@link VillagerType#foreignMerchantStock()} ---"));
            recipe.foreignMerchantStock().forEach((itemRl, count) -> debugTooltip.add(Component.literal(" §7- §f" + itemRl + " x" + count)));
        }

        // 解析后（Resolved）的依赖需求数据 Dump
        debugTooltip.add(Component.literal("§c--- 已解析的物品需求关系 (Resolved Goods) ---"));
        if (recipe.resolvedRequiredGoods() != null) {
            debugTooltip.add(Component.literal(" §7RequiredGoods {@link VillagerType#resolvedRequiredGoods()}: §f" + recipe.resolvedRequiredGoods().size() + " 项"));
        }
        if (recipe.resolvedBringBackHomeGoods() != null) {
            debugTooltip.add(Component.literal(" §7BringBackHomeGoods {@link VillagerType#resolvedBringBackHomeGoods()}: §f" + recipe.resolvedBringBackHomeGoods().size() + " 项"));
        }
        if (recipe.resolvedCollectGoods() != null) {
            debugTooltip.add(Component.literal(" §7CollectGoods {@link VillagerType#resolvedCollectGoods()}: §f" + recipe.resolvedCollectGoods().size() + " 项"));
        }

        // 图鉴相关
        debugTooltip.add(Component.literal("§1--- 图鉴 TravelBook 属性 ---"));
        debugTooltip.add(Component.literal(" §7Display / MainCulture: §f" + recipe.travelBookDisplay() + " / " + recipe.travelBookMainCultureVillager()));
        debugTooltip.add(Component.literal(" §7Category: §f" + recipe.travelBookCategory()));
        debugTooltip.add(Component.literal(" §7MainHand / OffHand: §f" + recipe.travelBookHeldItem() + " / " + recipe.travelBookHeldItemOffHand()));

        // 渲染悬浮提示框
        DebugGuiUtils.renderHoverInspector(font, guiGraphics, debugTooltip, mouseX, mouseY, 6, 78, 148, 56);
    }
}