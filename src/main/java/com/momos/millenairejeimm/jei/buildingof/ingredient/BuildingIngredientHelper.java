package com.momos.millenairejeimm.jei.buildingof.ingredient;

import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.millenaire.building.BuildingPlanSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 建筑集 {@link BuildingPlanSet} 的 JEI 检索辅助类。
 */
public class BuildingIngredientHelper implements IIngredientHelper<BuildingPlanSet> {
    @Override
    public IIngredientType<BuildingPlanSet> getIngredientType() {
        return MillIngredientTypes.BUILDING;
    }

    @Override
    public String getDisplayName(BuildingPlanSet ingredient) {
        return MillenaireLocalizeHelper.getBuildingName(ingredient).getString();
    }

    // [醒目新注释] 补全 getUniqueId 抽象方法实现
    /**
     * 获取建筑集的唯一标识字符串。
     *
     * @param ingredient 建筑集对象 {@link BuildingPlanSet}
     * @param context UID 上下文 {@link UidContext}
     * @return 唯一标识字符串
     */
    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(BuildingPlanSet ingredient, UidContext context) {
        return ingredient.culture().getPath() + ":" + ingredient.buildingId();
    }

    // [醒目新注释] 重写 getUid 委托给 getUniqueId
    /**
     * 获取建筑集的唯一标识对象 {@link Object}。
     *
     * @param ingredient 建筑集对象 {@link BuildingPlanSet}
     * @param context UID 上下文 {@link UidContext}
     * @return 唯一标识对象
     */
    @Override
    public Object getUid(BuildingPlanSet ingredient, UidContext context) {
        return getUniqueId(ingredient, context);
    }

    // [醒目新注释] 修复崩溃隐患：对 Path 进行 toLowerCase 转化，规避 MC 1.21+ ResourceLocation 严格校验
    /**
     * 获取建筑集对应的资源位置 {@link ResourceLocation}。
     *
     * @param ingredient 建筑集对象 {@link BuildingPlanSet}
     * @return 转换后的合法资源位置 {@link ResourceLocation}
     */
    @Override
    public ResourceLocation getResourceLocation(BuildingPlanSet ingredient) {
        String culturePath = ingredient.culture() != null ? ingredient.culture().getPath() : "unknown";
        String buildingId = ingredient.buildingId() != null ? ingredient.buildingId() : "unknown";
        String fullPath = (culturePath + "/" + buildingId).toLowerCase(Locale.ROOT);
        return ResourceLocation.fromNamespaceAndPath("millenaire", fullPath);
    }

    @Override
    public BuildingPlanSet copyIngredient(BuildingPlanSet ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable BuildingPlanSet ingredient) {
        return ingredient == null ? "BuildingPlanSet is null" : "Building: " + ingredient.buildingId();
    }

    @Override
    public Stream<ResourceLocation> getTagStream(BuildingPlanSet ingredient) {
        List<ResourceLocation> tags = new ArrayList<>();
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "culture"));
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "building"));
        if (ingredient != null) {
            if (ingredient.culture() != null) {
                tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", ingredient.culture().getPath().toLowerCase(Locale.ROOT)));
            }
            if (ingredient.buildingId() != null) {
                tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", ingredient.buildingId().toLowerCase(Locale.ROOT)));
            }
        }
        return tags.stream();
    }
}