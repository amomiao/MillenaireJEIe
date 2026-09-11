// region BuildingIngredientHelper Implementation
package com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient;

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

import java.util.*;
import java.util.stream.Stream;

/**
 * 建筑集 {@link BuildingPlanSet} 的 JEI 检索辅助类。
 */
public class BuildingIngredientHelper implements IIngredientHelper<BuildingPlanSet> {

    // [醒目新注释] 逻辑运行依赖的字符串常量封装
    private static final String MOD_ID = "millenaire";
    private static final String UNKNOWN_BUILDING_ID = "unknown_building";
    private static final String UNKNOWN_PATH = "unknown";
    private static final String DEFAULT_DISPLAY_NAME = "Unknown Building";
    private static final String ERR_NULL_INGREDIENT = "BuildingPlanSet is null";
    private static final String ERR_BUILDING_PREFIX = "Building: ";

    @Override
    public IIngredientType<BuildingPlanSet> getIngredientType() {
        return MillIngredientTypes.BUILDING;
    }

    // [醒目新注释] 采用常量做空值降级处理
    /**
     * 获取建筑集显示名称 {@link BuildingPlanSet}。
     *
     * @param ingredient 建筑集对象
     * @return 显示名称字符串
     */
    @Override
    public String getDisplayName(BuildingPlanSet ingredient) {
        if (ingredient == null) return DEFAULT_DISPLAY_NAME;
        return MillenaireLocalizeHelper.getBuildingName(ingredient).getString();
    }

    // [醒目新注释] 补全 getUniqueId 抽象方法实现，并对 ingredient.culture() 进行防空判空保护
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
        if (ingredient == null) return MOD_ID + ":" + UNKNOWN_BUILDING_ID;
        String culture = ingredient.culture() != null ? ingredient.culture().getPath() : UNKNOWN_PATH;
        String building = ingredient.buildingId() != null ? ingredient.buildingId() : UNKNOWN_PATH;
        return culture + ":" + building;
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
        if (ingredient == null) return ResourceLocation.fromNamespaceAndPath(MOD_ID, UNKNOWN_BUILDING_ID);
        String culturePath = ingredient.culture() != null ? ingredient.culture().getPath() : UNKNOWN_PATH;
        String buildingId = ingredient.buildingId() != null ? ingredient.buildingId() : UNKNOWN_PATH;
        String fullPath = (culturePath + "/" + buildingId).toLowerCase(Locale.ROOT);
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, fullPath);
    }

    @Override
    public BuildingPlanSet copyIngredient(BuildingPlanSet ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable BuildingPlanSet ingredient) {
        return ingredient == null ? ERR_NULL_INGREDIENT : ERR_BUILDING_PREFIX + ingredient.buildingId();
    }

    @Override
    public Stream<ResourceLocation> getTagStream(BuildingPlanSet ingredient) {
        if (ingredient == null) {
            return Stream.of(MillenaireAPIHelper.TAG_CULTURE_BASE, MillenaireAPIHelper.TAG_BUILDING_BASE);
        }

        Set<ResourceLocation> tags = new LinkedHashSet<>();
        tags.add(MillenaireAPIHelper.TAG_CULTURE_BASE);
        tags.add(MillenaireAPIHelper.TAG_BUILDING_BASE);

        // 1. 文化 Tag
        if (ingredient.culture() != null) {
            tags.add(MillenaireAPIHelper.getCultureTag(ingredient.culture()));
        }

        // 2. 建筑自身 ID Tag
        tags.add(MillenaireAPIHelper.getBuildingTag(ingredient));

        // 3. 关联村庄 Tags
        List<VillageType> villages = MillenaireAPIHelper.getVillageFormBuilding(ingredient);
        for (VillageType village : villages) {
            ResourceLocation tag = MillenaireAPIHelper.getVillageTag(village);
            if (tag != null) tags.add(tag);
        }

        // 4. 关联村民 Tags
        List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormBuilding(ingredient);
        for (VillagerType villager : villagers) {
            ResourceLocation tag = MillenaireAPIHelper.getVillagerTag(villager);
            if (tag != null) tags.add(tag);
        }

        tags.remove(null);
        return tags.stream();
    }
}
// endregion BuildingIngredientHelper Implementation