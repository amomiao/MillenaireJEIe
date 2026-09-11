package com.momos.millenairejeimm.jei.Runtime.cultureof.ingredient;

import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.millenaire.culture.Culture;

import java.util.ArrayList;
import java.util.List;

/**
 * 文化 {@link Culture} 的 JEI 图标与 Tooltip 渲染器。
 */
public class CultureIngredientRenderer extends BaseMillIngredientRenderer<Culture> {
    /**
     * 获取文化显示的徽章文本。
     *
     * @param ingredient 文化对象 {@link Culture}
     * @return 渲染的徽章字符串
     */
    @Override
    protected String getBadgeText(Culture ingredient) {
        if (ingredient == null || ingredient.displayName() == null || ingredient.displayName().isEmpty()) {
            return "文";
        }
        String name = ingredient.displayName();
        return name.substring(0, Math.min(2, name.length()));
    }

    // [醒目新注释] 移除 F3+H 限制，无条件全量输出文化 ID 与关联检索 Tag 列表
    // [醒目新注释] 替换硬编码字符串，全量使用 {@link MillenaireJeimmKeys} 本地化 Key 构建文本组件
    /**
     * 获取文化的悬浮提示组件列表。
     *
     * @param ingredient 文化配置对象 {@link Culture}
     * @param tooltipFlag Tooltip 标记 {@link TooltipFlag}
     * @return 悬浮提示文本列表 {@link List}
     */
    @Override
    public List<Component> getTooltip(Culture ingredient, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        if (ingredient == null) {
            tooltip.add(Component.translatable(MillenaireJeimmKeys.UNKNOWN_CULTURE).withStyle(ChatFormatting.RED));
            return tooltip;
        }

        String name = ingredient.displayName() != null ? ingredient.displayName() : Component.translatable(MillenaireJeimmKeys.UNKNOWN_CULTURE).getString();
        tooltip.add(Component.translatable(MillenaireJeimmKeys.CULTURE_PREFIX, name).withStyle(ChatFormatting.GOLD));

        // [醒目新注释] 始终渲染 ResourceLocation 内部 ID
        if (ingredient.id() != null) {
            tooltip.add(Component.translatable(MillenaireJeimmKeys.ID_PREFIX, ingredient.id().toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        // [醒目新注释] 全量提取 {@link CultureIngredientHelper#getTagStream} 中的 Tag 并展示
        CultureIngredientHelper helper = new CultureIngredientHelper();
        List<ResourceLocation> tags = helper.getTagStream(ingredient).toList();
        if (!tags.isEmpty()) {
            tooltip.add(Component.translatable(MillenaireJeimmKeys.ASSOCIATED_TAGS).withStyle(ChatFormatting.GRAY));
            for (ResourceLocation tag : tags) {
                tooltip.add(Component.literal("  $" + tag).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return tooltip;
    }
}