package com.momos.millenairejeimm.jei.Runtime.villageof.ingredient;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 村庄类型 {@link VillageType} 的 JEI 检索辅助类。
 */
public class VillageIngredientHelper implements IIngredientHelper<VillageType> {
    @Override
    public IIngredientType<VillageType> getIngredientType() {
        return MillIngredientTypes.VILLAGE;
    }

    @Override
    public String getDisplayName(VillageType ingredient) {
        if (ingredient == null) return "Unknown Village";
        return ingredient.name() != null ? ingredient.name() : ingredient.id().getPath();
    }

    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(VillageType ingredient, UidContext context) {
        return ingredient != null && ingredient.id() != null ? ingredient.id().toString() : "millenaire:unknown_village";
    }

    @Override
    public Object getUid(VillageType ingredient, UidContext context) {
        return getUniqueId(ingredient, context);
    }

    @Override
    public ResourceLocation getResourceLocation(VillageType ingredient) {
        if (ingredient == null || ingredient.id() == null) {
            return ResourceLocation.fromNamespaceAndPath("millenaire", "unknown_village");
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