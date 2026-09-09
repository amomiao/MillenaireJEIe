package com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.type.MillIngredientIconResolver;
import com.momos.millenairejeimm.jei.type.BaseMillIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.millenaire.building.BuildingPlanSet;

import java.util.ArrayList;
import java.util.List;

/**
 * 建筑集 {@link BuildingPlanSet} 的 JEI 图标与 Tooltip 渲染器。
 */
public class BuildingIngredientRenderer extends BaseMillIngredientRenderer<BuildingPlanSet> {
    // [醒目新注释] 重写 getBadgeText，统一调用顶级统筹类 MillIngredientIconResolver 解释点缀文字
    /**
     * 获取建筑集显示的徽章点缀文本。
     *
     * @param ingredient 建筑集对象 {@link BuildingPlanSet}
     * @return 徽章点缀字符串
     */
    @Override
    protected String getBadgeText(BuildingPlanSet ingredient) {
        return MillIngredientIconResolver.resolve(ingredient).badgeText();
    }

    // [醒目新注释] 重写符合 IIngredientRenderer 抽象签名的 getTooltip 方法并返回 List<Component>
    // [醒目新注释] 增加 ingredient.culture() 的判空保护
    /**
     * 获取建筑集的悬浮提示组件列表。
     *
     * @param ingredient 建筑集对象 {@link BuildingPlanSet}
     * @param tooltipFlag Tooltip 标记 {@link TooltipFlag}
     * @return 悬浮提示文本列表 {@link List}
     */
    @Override
    public List<Component> getTooltip(BuildingPlanSet ingredient, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(MillenaireLocalizeHelper.getBuildingName(ingredient));
        String cultureName = (ingredient != null && ingredient.culture() != null) ? ingredient.culture().getPath() : "unknown";
        tooltip.add(Component.translatableWithFallback("gui.millenairejeim.culture_prefix", "所属文化: %s", cultureName));
        return tooltip;
    }
}