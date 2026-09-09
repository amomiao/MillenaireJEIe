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
import org.millenaire.culture.VillagerType;

import java.util.ArrayList;
import java.util.List;
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
        return MillenaireLocalizeHelper.getVillagerName(ingredient).getString();
    }

    // [醒目新注释] 补全 getUniqueId 抽象方法实现
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
        return ingredient.id().toString();
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

    @Override
    public Stream<ResourceLocation> getTagStream(VillagerType ingredient) {
        List<ResourceLocation> tags = new ArrayList<>();
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "culture"));
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "villager"));
        if (ingredient != null) {
            if (ingredient.culture() != null) {
                tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", ingredient.culture().getPath().toLowerCase()));
            }
            List<BuildingPlanSet> associatedBuildings = MillenaireAPIHelper.MappingVillagerBuilding(ingredient);
            for (BuildingPlanSet building : associatedBuildings) {
                if (building != null && building.buildingId() != null) {
                    tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", building.buildingId().toLowerCase()));
                }
            }
        }
        return tags.stream();
    }
}