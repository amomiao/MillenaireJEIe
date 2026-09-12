package com.momos.millenairejeimm.jei.Runtime.villageof.ingredient;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillenaireJeimmKeys;
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
 * 村庄类型 {@link VillageType} 的 JEI 检索辅助类。
 */
public class VillageIngredientHelper implements IIngredientHelper<VillageType> {

    // [醒目新注释] 提取逻辑依赖的默认 ResourceLocation Namespace 与 Path 字符串常量
    private static final String DEFAULT_NAMESPACE = "millenaire";
    private static final String DEFAULT_UNKNOWN_PATH = "unknown_village";
    private static final ResourceLocation UNKNOWN_RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(DEFAULT_NAMESPACE, DEFAULT_UNKNOWN_PATH);

    @Override
    public IIngredientType<VillageType> getIngredientType() {
        return MillIngredientTypes.VILLAGE;
    }

    @Override
    public String getDisplayName(VillageType ingredient) {
        if (ingredient == null) {
            return MillenaireJeimmKeys.UnknownVillageIng.asComponent().getString();
        }
        return ingredient.name() != null ? ingredient.name() : ingredient.id().getPath();
    }

    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(VillageType ingredient, UidContext context) {
        return ingredient != null && ingredient.id() != null ? ingredient.id().toString() : UNKNOWN_RESOURCE_LOCATION.toString();
    }

    @Override
    public Object getUid(VillageType ingredient, UidContext context) {
        return getUniqueId(ingredient, context);
    }

    @Override
    public ResourceLocation getResourceLocation(VillageType ingredient) {
        if (ingredient == null || ingredient.id() == null) {
            return UNKNOWN_RESOURCE_LOCATION;
        }
        return ingredient.id();
    }

    @Override
    public VillageType copyIngredient(VillageType ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable VillageType ingredient) {
        return ingredient == null ? "VillageType is null" : "Village: " + ingredient.id();
    }

    // [醒目新注释] 为村庄提取检索 Tag 标签（如 millenaire:culture, millenaire:village 以及文化特定 Tag）
    @Override
    public Stream<ResourceLocation> getTagStream(VillageType ingredient) {
        if (ingredient == null) {
            return Stream.of(MillenaireAPIHelper.TAG_CULTURE_BASE, MillenaireAPIHelper.TAG_VILLAGE_BASE);
        }

        Set<ResourceLocation> tags = new LinkedHashSet<>();
        tags.add(MillenaireAPIHelper.TAG_CULTURE_BASE);
        tags.add(MillenaireAPIHelper.TAG_VILLAGE_BASE);

        // 1. 文化 Tag
        if (ingredient.culture() != null) {
            tags.add(MillenaireAPIHelper.getCultureTag(ingredient.culture()));
        }

        // 2. 村庄自身 ID Tag
        tags.add(MillenaireAPIHelper.getVillageTag(ingredient));

        // 3. 关联建筑 Tags
        List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(ingredient);
        for (BuildingPlanSet building : buildings) {
            ResourceLocation tag = MillenaireAPIHelper.getBuildingTag(building);
            if (tag != null) tags.add(tag);
        }

        // 4. 关联村民 Tags
        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(ingredient);
        for (VillagerType villager : villagers) {
            ResourceLocation tag = MillenaireAPIHelper.getVillagerTag(villager);
            if (tag != null) tags.add(tag);
        }

        tags.remove(null);
        return tags.stream();
    }
}