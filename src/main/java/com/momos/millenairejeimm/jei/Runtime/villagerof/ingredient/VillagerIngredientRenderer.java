package com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.type.BaseMillIngredientRenderer;
import net.minecraft.network.chat.Component;
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
        return "村";
    }

    // [醒目新注释] 重写符合 IIngredientRenderer 抽象签名的 getTooltip 方法并返回 List<Component>
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
        tooltip.add(MillenaireLocalizeHelper.getVillagerName(ingredient));
        if (ingredient.culture() != null) {
            tooltip.add(Component.literal("文化: " + ingredient.culture().getPath()));
        }
        return tooltip;
    }
}