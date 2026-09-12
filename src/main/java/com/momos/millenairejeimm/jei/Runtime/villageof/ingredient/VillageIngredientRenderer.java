package com.momos.millenairejeimm.jei.Runtime.villageof.ingredient;

import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.MillIngredientIconResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
        return MillIngredientIconResolver.resolve(ingredient).badgeText();
    }

    // [醒目新注释] 移除 F3+H 条件，直接将村庄 ID 及所有关联 Tag 输出在悬浮提示中
    @Override
    public List<Component> getTooltip(VillageType ingredient, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        if (ingredient == null) {
            // [醒目新注释] 改用本地化键 {@link MillenaireJeimmKeys#VILLAGE_UNKNOWN} 渲染未知村庄提示
            tooltip.add(MillenaireJeimmKeys.UnknownVillagerIng.asComponent().withStyle(ChatFormatting.RED));
            return tooltip;
        }

        String displayName = ingredient.name() != null && !ingredient.name().isEmpty() ? ingredient.name() : ingredient.id().getPath();
        tooltip.add(Component.literal(displayName).withStyle(ChatFormatting.WHITE));

//        if (ingredient.culture() != null) {
//            // [醒目新注释] 使用 {@link MillenaireJeimmKeys#VILLAGE_CULTURE} 参数化拼装文化提示信息
//            tooltip.add(Component.translatable(MillenaireJeimmKeys.VILLAGE_CULTURE, ingredient.culture().getPath()).withStyle(ChatFormatting.GRAY));
//        }
//        // [醒目新注释] 始终渲染村庄 ResourceLocation ID
//        if (ingredient.id() != null) {
//            // [醒目新注释] 使用 {@link MillenaireJeimmKeys#VILLAGE_ID} 参数化输出村庄 ResourceLocation ID
//            tooltip.add(Component.translatable(MillenaireJeimmKeys.VILLAGE_ID, ingredient.id().toString()).withStyle(ChatFormatting.DARK_GRAY));
//        }
//        // [醒目新注释] 调用 {@link VillageIngredientHelper#getTagStream} 展开村庄关联的所有 Tags
//        VillageIngredientHelper helper = new VillageIngredientHelper();
//        List<ResourceLocation> tags = helper.getTagStream(ingredient).toList();
//        if (!tags.isEmpty()) {
//            // [醒目新注释] 使用 {@link MillenaireJeimmKeys#VILLAGE_TAGS} 输出关联标签头部文本
//            tooltip.add(Component.translatable(MillenaireJeimmKeys.VILLAGE_TAGS).withStyle(ChatFormatting.GRAY));
//            for (ResourceLocation tag : tags) {
//                tooltip.add(Component.literal("  $" + tag).withStyle(ChatFormatting.DARK_GRAY));
//            }
//        }

        return tooltip;
    }
}