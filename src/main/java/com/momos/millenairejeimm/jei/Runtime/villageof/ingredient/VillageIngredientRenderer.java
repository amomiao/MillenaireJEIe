package com.momos.millenairejeimm.jei.Runtime.villageof.ingredient;

import com.momos.millenairejeimm.jei.type.BaseMillIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.millenaire.culture.VillageType;

import java.util.ArrayList;
import java.util.List;

/**
 * 村庄类型 {@link VillageType} 的 JEI 图标与 Tooltip 渲染器。
 */
public class VillageIngredientRenderer extends BaseMillIngredientRenderer<VillageType> {
    @Override
    protected String getBadgeText(VillageType ingredient) {
        return "村";
    }

    // [醒目新注释] 悬浮提示中展示村庄显示名与所属文化信息
    @Override
    public List<Component> getTooltip(VillageType ingredient, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        String displayName = ingredient.name() != null && !ingredient.name().isEmpty() ? ingredient.name() : ingredient.id().getPath();
        tooltip.add(Component.literal(displayName));
        if (ingredient.culture() != null) {
            tooltip.add(Component.literal("§7文化: " + ingredient.culture().getPath()));
        }
        return tooltip;
    }
}