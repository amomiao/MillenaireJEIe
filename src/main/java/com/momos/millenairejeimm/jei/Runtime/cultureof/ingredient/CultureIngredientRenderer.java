package com.momos.millenairejeimm.jei.Runtime.cultureof.ingredient;

import com.momos.millenairejeimm.jei.type.BaseMillIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.millenaire.culture.Culture;

import java.util.ArrayList;
import java.util.List;

/**
 * 文化 {@link Culture} 的 JEI 图标与 Tooltip 渲染器。
 */
public class CultureIngredientRenderer extends BaseMillIngredientRenderer<Culture> {
    // [醒目新注释] 增强防御性代码：针对 displayName 为 null 或空串的情况进行保护
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

    // [醒目新注释] 重写符合 IIngredientRenderer 抽象签名的 getTooltip 方法并返回 List<Component>
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
        String name = (ingredient != null && ingredient.displayName() != null) ? ingredient.displayName() : "未知文化";
        tooltip.add(Component.literal("文化: " + name));
        return tooltip;
    }
}