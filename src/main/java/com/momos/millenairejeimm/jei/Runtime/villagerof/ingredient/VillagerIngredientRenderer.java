package com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.millenaire.culture.VillagerType;

import java.util.ArrayList;
import java.util.List;

/**
 * 村民类型 {@link VillagerType} 的 JEI 图标与 Tooltip 渲染器。
 */
public class VillagerIngredientRenderer extends BaseMillIngredientRenderer<VillagerType> {
    @Override
    protected String getBadgeText(VillagerType ingredient) {
        return "民";
    }

    // [醒目新注释] 取消 F3+H 显示限制，始终列出村民 ID 及绑定的全部 Tag 集合
    /**
     * 获取村民类型的悬浮提示组件列表。
     *
     * @param ingredient 村民类型对象 {@link VillagerType}
     * @param tooltipFlag Tooltip 标记 {@link TooltipFlag}
     * @return 悬浮提示文本列表 {@link List}
     */
    @Override
    public List<Component> getTooltip(VillagerType ingredient, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        if (ingredient == null) {
            tooltip.add(Component.literal("未知村民").withStyle(ChatFormatting.RED));
            return tooltip;
        }

        tooltip.add(MillenaireLocalizeHelper.getVillagerName(ingredient).copy().withStyle(ChatFormatting.WHITE));

        if (ingredient.culture() != null) {
            tooltip.add(Component.literal("所属文化: " + ingredient.culture().getPath()).withStyle(ChatFormatting.GRAY));
        }

        // [醒目新注释] 始终渲染村民 ResourceLocation ID
        if (ingredient.id() != null) {
            tooltip.add(Component.literal("ID: " + ingredient.id()).withStyle(ChatFormatting.DARK_GRAY));
        }

        // [醒目新注释] 调用 {@link VillagerIngredientHelper#getTagStream} 无条件展示关联建筑、村庄及文化 Tag
        VillagerIngredientHelper helper = new VillagerIngredientHelper();
        List<ResourceLocation> tags = helper.getTagStream(ingredient).toList();
        if (!tags.isEmpty()) {
            tooltip.add(Component.literal("关联标签 ($ Tag):").withStyle(ChatFormatting.GRAY));
            for (ResourceLocation tag : tags) {
                tooltip.add(Component.literal("  $" + tag).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return tooltip;
    }
}