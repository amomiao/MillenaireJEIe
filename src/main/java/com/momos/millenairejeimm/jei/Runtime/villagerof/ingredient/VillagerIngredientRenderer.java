package com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.MillIngredientIconResolver;
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
        return MillIngredientIconResolver.resolve(ingredient).badgeText();
    }

    @Override
    public List<Component> getTooltip(VillagerType ingredient, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        if (ingredient == null) {
            tooltip.add(MillenaireJeimmKeys.UnknownVillagerIng.asComponent().copy().withStyle(ChatFormatting.RED));
            return tooltip;
        }
        tooltip.add(MillenaireLocalizeHelper.getVillagerName(ingredient).copy().withStyle(ChatFormatting.WHITE));
//        if (ingredient.culture() != null) {
//            tooltip.add(MillenaireJeimmKeys.getVillagerCulture(ingredient.culture().getPath()).copy().withStyle(ChatFormatting.GRAY));
//        }
//        if (ingredient.id() != null) {
//            tooltip.add(MillenaireJeimmKeys.getVillagerId(ingredient.id()).copy().withStyle(ChatFormatting.DARK_GRAY));
//        }
//        VillagerIngredientHelper helper = new VillagerIngredientHelper();
//        List<ResourceLocation> tags = helper.getTagStream(ingredient).toList();
//        if (!tags.isEmpty()) {
//            tooltip.add(MillenaireJeimmKeys.getVillagerTags().copy().withStyle(ChatFormatting.GRAY));
//            for (ResourceLocation tag : tags) {
//                tooltip.add(Component.literal("  $" + tag).withStyle(ChatFormatting.DARK_GRAY));
//            }
//        }

        return tooltip;
    }
}