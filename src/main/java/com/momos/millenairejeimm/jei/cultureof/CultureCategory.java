package com.momos.millenairejeimm.jei.cultureof;

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
import org.millenaire.culture.Culture;
import org.millenaire.item.ItemHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 千年村庄文化 {@link Culture} 的 JEI 详情分类页面。
 */
public class CultureCategory implements IRecipeCategory<Culture> {

    /** 文化详情分类的 JEI 唯一 RecipeType 标识 */
    public static final RecipeType<Culture> RECIPE_TYPE = RecipeType.create("millenaire", "culture_details", Culture.class);

    private final IDrawable background;
    private final IDrawable icon;

    /**
     * 文化成分专用“仅图标”渲染器，解析 {@link Culture#mapIcon()}。
     */
    private static final MillIconOnlyRenderer<Culture> ICON_RENDERER = new MillIconOnlyRenderer<>(recipe -> {
        if (recipe != null && recipe.mapIcon().isPresent()) {
            ResourceLocation iconRl = recipe.mapIcon().get();
            Item mapIconItem = ItemHelper.resolve(iconRl.toString());
            if (mapIconItem != null) return new ItemStack(mapIconItem);
        }
        return ItemStack.EMPTY;
    });

    /**
     * 构建文化 JEI 分类页面。
     *
     * @param guiHelper JEI GUI 辅助接口 {@link IGuiHelper}
     */
    public CultureCategory(IGuiHelper guiHelper) {
        // [醒目新注释] 扩展页面尺寸至 160x140，与 BuildingCategory 保持一致，充当开发者 Debug 高密度数据面板
        this.background = guiHelper.createBlankDrawable(160, 140);

        // [醒目新注释] 采用指定图标 "millenaire:village_norman"；若未解析成功则尝试 "millenaire:norman_amulet" 或原版地图
        Item cultureIconItem = ItemHelper.resolve("millenaire:village_norman");
        if (cultureIconItem == null) {
            cultureIconItem = ItemHelper.resolve("millenaire:norman_amulet");
        }
        ItemStack iconStack = (cultureIconItem != null) ? new ItemStack(cultureIconItem) : new ItemStack(Items.MAP);

        // [醒目新注释] 将物品堆栈转为 JEI 分类标签图标
        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<Culture> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.millenairejeim.category.culture_details");
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

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Culture recipe, IFocusGroup focuses) {
        if (recipe == null) return;

        // [醒目新注释] 在 (6, 6) 槽位注册 Culture 自定义成分
        // [醒目新注释] 通过 setCustomRenderer 单独指定图标渲染器，避免加载全局列表中包含文字的绘制效果
        builder.addSlot(RecipeIngredientRole.OUTPUT, 6, 6)
                .addIngredient(MillIngredientTypes.CULTURE, recipe)
                .setCustomRenderer(MillIngredientTypes.CULTURE, ICON_RENDERER);
    }

    @Override
    public void draw(Culture recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null) return;

        Font font = Minecraft.getInstance().font;

        // [醒目新注释] 对接准确的 Record 方法 mapIcon() (返回 Optional<ResourceLocation>)，解析并渲染 mapIcon 对应的物品图标
        // [醒目新注释] 已通过 setCustomRenderer 在槽位完成绘制，清理 draw 中的手动绘制

        // [醒目新注释] 严格对接 Record 方法 displayName() 与 id()
        String displayName = recipe.displayName() != null && !recipe.displayName().isEmpty() ? recipe.displayName() : recipe.id().getPath();
        guiGraphics.drawString(font, Component.literal("§l" + displayName), 28, 6, 0x111111, false);
        guiGraphics.drawString(font, Component.literal("§8标识: " + recipe.id()), 28, 17, 0x444444, false);

        // 渲染基础信息分割线与母语
        guiGraphics.fill(6, 27, 154, 28, 0xFFCCCCCC);

        // [醒目新注释] 1. 基础字段与计数属性网格 (Y: 30~55)
        String langText = recipe.nativeLanguage() != null ? recipe.nativeLanguage() : "未设母语";
        guiGraphics.drawString(font, Component.literal("原生语言: §8" + langText), 6, 30, 0x333333, false);

        int cropCount = recipe.knownCrops() != null ? recipe.knownCrops().size() : 0;
        int dropCount = recipe.knownHuntingDrops() != null ? recipe.knownHuntingDrops().size() : 0;
        guiGraphics.drawString(font, Component.literal("已知农作物: §2" + cropCount + " 种§r | 狩猎掉落: §c" + dropCount + " 种"), 6, 41, 0x333333, false);

        String mapIconState = recipe.mapIcon().isPresent() ? "§a[已配置]" : "§7[无]";
        String bannerNbtState = recipe.cultureBannerNbt() != null ? "§a[有NBT]" : "§8[无NBT]";
        guiGraphics.drawString(font, Component.literal("地图图标: " + mapIconState + " | 旗帜: " + bannerNbtState), 6, 52, 0x333333, false);

        // [醒目新注释] 2. 状态徽章 Badges 区域 (Y: 64~74)
        boolean isDefaultTexture = Culture.DEFAULT_PANEL_TEXTURE.equals(recipe.panelTexture());
        String textureBadge = isDefaultTexture ? "§8[默认面板纹理]" : "§b[自定义面板纹理]";
        guiGraphics.drawString(font, Component.literal("属性: " + textureBadge), 6, 64, 0x333333, false);

        // [醒目新注释] 3. 暗色 Debug 终端控制台 (Y: 78~132)，利用 {@link DebugGuiUtils} 进行绘制
        DebugGuiUtils.drawDebugConsoleBackground(guiGraphics, 6, 78, 148, 56);
        guiGraphics.drawString(font, Component.literal("§a[CULTURE DEV INSPECTOR]"), 10, 82, 0x00FF00, false);
        guiGraphics.drawString(font, Component.literal("§7Panel: §f" + (recipe.panelTexture() != null ? recipe.panelTexture().getPath() : "N/A")), 10, 93, 0xCCCCCC, false);

        String mapIconPath = recipe.mapIcon().map(ResourceLocation::toString).orElse("None");
        guiGraphics.drawString(font, Component.literal("§7MapIcon: §f" + mapIconPath), 10, 104, 0xCCCCCC, false);
        guiGraphics.drawString(font, Component.literal("§e💡 悬停此处查看文化全量数据 Dump"), 10, 118, 0xFFCC00, false);

        // [醒目新注释] 4. 构建全量字段悬浮 Hover Tooltip Inspector 浮窗
        List<Component> debugTooltip = new ArrayList<>();
        debugTooltip.add(Component.literal("§a=== Culture Record Field Inspector ==="));
        debugTooltip.add(Component.literal("§7Memory Record: §f" + recipe.getClass().getName()));
        debugTooltip.add(Component.literal("§7ID {@link Culture#id()}: §f" + recipe.id()));
        debugTooltip.add(Component.literal("§7DisplayName {@link Culture#displayName()}: §f" + recipe.displayName()));
        debugTooltip.add(Component.literal("§7NativeLanguage {@link Culture#nativeLanguage()}: §f" + recipe.nativeLanguage()));
        debugTooltip.add(Component.literal("§7PanelTexture {@link Culture#panelTexture()}: §f" + recipe.panelTexture()));
        debugTooltip.add(Component.literal("§7Is Default Texture: §f" + isDefaultTexture));
        debugTooltip.add(Component.literal("§7MapIcon {@link Culture#mapIcon()}: §f" + mapIconPath));

        // 文化旗帜 NBT 展开
        if (recipe.cultureBannerNbt() != null) {
            debugTooltip.add(Component.literal("§7CultureBannerNbt {@link Culture#cultureBannerNbt()}:"));
            debugTooltip.add(Component.literal("  §8" + recipe.cultureBannerNbt()));
        } else {
            debugTooltip.add(Component.literal("§7CultureBannerNbt: §8null"));
        }

        // 农作物已知列表 Dump
        if (recipe.knownCrops() != null && !recipe.knownCrops().isEmpty()) {
            debugTooltip.add(Component.literal("§e--- 已知农作物列表 (knownCrops) ---"));
            for (String crop : recipe.knownCrops()) {
                debugTooltip.add(Component.literal(" §7- §f" + crop));
            }
        } else {
            debugTooltip.add(Component.literal("§7knownCrops {@link Culture#knownCrops()}: §8[空/无记录]"));
        }

        // 狩猎掉落物 Dump
        if (recipe.knownHuntingDrops() != null && !recipe.knownHuntingDrops().isEmpty()) {
            debugTooltip.add(Component.literal("§c--- 已知狩猎掉落物 (knownHuntingDrops) ---"));
            for (String drop : recipe.knownHuntingDrops()) {
                debugTooltip.add(Component.literal(" §7- §f" + drop));
            }
        } else {
            debugTooltip.add(Component.literal("§7knownHuntingDrops {@link Culture#knownHuntingDrops()}: §8[空/无记录]"));
        }

        // 旅行手册分类 Dump
        if (recipe.travelBookCategories() != null) {
            debugTooltip.add(Component.literal("§b--- 旅行手册分类 {@link Culture#travelBookCategories()} ---"));
            debugTooltip.add(Component.literal(" §7Categories Obj: §f" + recipe.travelBookCategories().toString()));
        }

        // 渲染悬浮提示框
        DebugGuiUtils.renderHoverInspector(font, guiGraphics, debugTooltip, mouseX, mouseY, 6, 78, 148, 56);
    }
}