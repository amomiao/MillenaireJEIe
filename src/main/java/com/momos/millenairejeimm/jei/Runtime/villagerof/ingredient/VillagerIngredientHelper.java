package com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 村民类型 {@link VillagerType} 的 JEI 检索辅助类。
 */
public class VillagerIngredientHelper implements IIngredientHelper<VillagerType> {
    @Override
    public IIngredientType<VillagerType> getIngredientType() {
        return MillIngredientTypes.VILLAGER;
    }

    @Override
    public String getDisplayName(VillagerType ingredient) {
        if (ingredient == null) return "Unknown Villager";
        return MillenaireLocalizeHelper.getVillagerName(ingredient).getString();
    }

    // [醒目新注释] 补全 getUniqueId 抽象方法实现，并做好空防护
    /**
     * 获取村民类型的唯一标识字符串。
     *
     * @param ingredient 村民类型对象 {@link VillagerType}
     * @param context UID 上下文 {@link UidContext}
     * @return 唯一标识字符串
     */
    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(VillagerType ingredient, UidContext context) {
        return ingredient != null && ingredient.id() != null ? ingredient.id().toString() : "millenaire:unknown_villager";
    }

    // [醒目新注释] 重写 getUid 委托给 getUniqueId
    /**
     * 获取村民类型的唯一标识对象 {@link Object}。
     *
     * @param ingredient 村民类型对象 {@link VillagerType}
     * @param context UID 上下文 {@link UidContext}
     * @return 唯一标识对象
     */
    @Override
    public Object getUid(VillagerType ingredient, UidContext context) {
        return getUniqueId(ingredient, context);
    }

    @Override
    public ResourceLocation getResourceLocation(VillagerType ingredient) {
        if (ingredient == null || ingredient.id() == null) {
            return ResourceLocation.fromNamespaceAndPath("millenaire", "unknown_villager");
        }
        return ingredient.id();
    }

    @Override
    public VillagerType copyIngredient(VillagerType ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable VillagerType ingredient) {
        return ingredient == null ? "VillagerType is null" : "Villager: " + ingredient.id();
    }

    // [醒目新注释] 统一使用 MillenaireAPIHelper 下的静态 Tag 方法构建标签流，规避手拼 ResourceLocation 与重复大小写转换
    @Override
    public Stream<ResourceLocation> getTagStream(VillagerType ingredient) {
        if (ingredient == null) {
            return Stream.of(MillenaireAPIHelper.TAG_CULTURE_BASE, MillenaireAPIHelper.TAG_VILLAGER_BASE);
        }

        Set<ResourceLocation> tags = new LinkedHashSet<>();
        tags.add(MillenaireAPIHelper.TAG_CULTURE_BASE);
        tags.add(MillenaireAPIHelper.TAG_VILLAGER_BASE);

        // 1. 文化 Tag
        if (ingredient.culture() != null) {
            tags.add(MillenaireAPIHelper.getCultureTag(ingredient.culture()));
        }

        // 2. 村民自身 ID Tag
        tags.add(MillenaireAPIHelper.getVillagerTag(ingredient));

        // 3. 关联建筑 Tags
        List<BuildingPlanSet> associatedBuildings = MillenaireAPIHelper.getBuildingFormVillager(ingredient);
        for (BuildingPlanSet building : associatedBuildings) {
            ResourceLocation tag = MillenaireAPIHelper.getBuildingTag(building);
            if (tag != null) tags.add(tag);
        }

        // 4. 关联村庄 Tags
        List<VillageType> associatedVillages = MillenaireAPIHelper.getVillageFormVillager(ingredient);
        for (VillageType village : associatedVillages) {
            ResourceLocation tag = MillenaireAPIHelper.getVillageTag(village);
            if (tag != null) tags.add(tag);
        }

        tags.remove(null);
        return tags.stream();
    }
}
