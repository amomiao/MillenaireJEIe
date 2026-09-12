// region BuildingIngredientRenderer Implementation
package com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.Runtime.MillIngredientIconResolver;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.millenaire.building.BuildingPlanSet;

import java.util.ArrayList;
import java.util.List;

/**
 * 建筑集 {@link BuildingPlanSet} 的 JEI 图标与 Tooltip 渲染器。
 */
public class BuildingIngredientRenderer extends BaseMillIngredientRenderer<BuildingPlanSet> {

    // [醒目新注释] 逻辑运行与格式化字符串封装
    private static final String UNKNOWN_CULTURE = "unknown";
    private static final String ID_PREFIX = "ID: ";

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

    // [醒目新注释] 移除高级提示框 (isAdvanced) 判断，默认直接在 Tooltip 底部列表打印全量关联 Tag，并将字符串抽离至 {@link MillenaireJeimmKeys}
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
        if (ingredient == null) {
            tooltip.add(MillenaireJeimmKeys.UnknownBuildingIng.asComponent().withStyle(ChatFormatting.RED));
            return tooltip;
        }

        tooltip.add(MillenaireLocalizeHelper.getBuildingName(ingredient).copy().withStyle(ChatFormatting.WHITE));

        String cname = (ingredient.culture() != null) ? ingredient.culture().getPath() : UNKNOWN_CULTURE;
        // tooltip.add(MillenaireJeimmKeys.CultureIngR_S.asComponent(cname).withStyle(ChatFormatting.GRAY));
        // 始终输出建筑全路径 ID
        String fullId = cname + ":" + (ingredient.buildingId() != null ? ingredient.buildingId() : UNKNOWN_CULTURE);
        tooltip.add(Component.literal(ID_PREFIX + fullId).withStyle(ChatFormatting.DARK_GRAY));

//        // [醒目新注释] 调用 {@link BuildingIngredientHelper#getTagStream} 遍历该建筑拥有的所有标签（文化、关联村庄、关联村民等）
//        BuildingIngredientHelper helper = new BuildingIngredientHelper();
//        List<ResourceLocation> tags = helper.getTagStream(ingredient).toList();
//        if (!tags.isEmpty()) {
//            tooltip.add(Component.translatableWithFallback(MillenaireJeimmKeys.ASSOCIATED_TAGS, "Associated Tags ($ Tag):").withStyle(ChatFormatting.GRAY));
//            for (ResourceLocation tag : tags) {
//                tooltip.add(Component.literal("  $" + tag).withStyle(ChatFormatting.DARK_GRAY));
//            }
//        }

        return tooltip;
    }
}
// endregion BuildingIngredientRenderer Implementation