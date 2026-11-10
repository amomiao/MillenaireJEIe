package com.momos.millenairejeimm.jei;

import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingLevelCostCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.building.BuildingPlanSet.LevelDef;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 建筑升级道具消耗计算工具类。
 * <p>
 * 专门用于提取 {@link BuildingPlanSet} 建筑各变体等级所需的建材消耗，
 * 支持合成满级汇总记录 {@code a_All}（level = -1）并提供并发安全的结果缓存机制。
 * </p>
 */
public final class BuildingUpgradeCostHelper {
    /**
     * 建筑集升满等级消耗结果缓存。
     * Key 为建筑集的 ID {@link BuildingPlanSet#id()}，
     * Value 为变体名称与其升满所需的 {@link ItemStack} 列表映射。
     */
    private static final Map<ResourceLocation, Map<String, List<ItemStack>>> UPGRADE_COST_CACHE = new ConcurrentHashMap<>();

    /**
     * 建筑全变体含汇总页的 LevelDef 列表缓存。
     */
    private static final Map<ResourceLocation, List<LevelDef>> FULL_VARIANT_LEVELS_CACHE = new ConcurrentHashMap<>();

    private BuildingUpgradeCostHelper() {}

    /**
     * 清空所有本地缓存。
     * <p>可在切换数据包、重载资源或需要重新计算建材消耗时调用。</p>
     */
    public static void clearCache() {
        UPGRADE_COST_CACHE.clear();
        FULL_VARIANT_LEVELS_CACHE.clear();
    }

    /**
     * 计算指定变体所有等级的建材峰值/累计用量映射（复用既有算法口径）。
     *
     * @param levelDefs 该变体包含的所有等级定义列表
     * @return 道具标识符到所需最大数量的映射
     */
    public static Map<ResourceLocation, Integer> calculateVariantTotalResources(List<LevelDef> levelDefs) {
        Map<ResourceLocation, Integer> totalMap = new LinkedHashMap<>();
        if (levelDefs == null || levelDefs.isEmpty()) return totalMap;

        for (LevelDef levelDef : levelDefs) {
            if (levelDef == null) continue;
            Map<ResourceLocation, Integer> requiredResources = levelDef.requiredResources();
            if (requiredResources != null && !requiredResources.isEmpty()) {
                requiredResources.forEach((itemRl, count) -> {
                    if (itemRl != null && count != null && count > 0) {
                        totalMap.merge(itemRl, count, Math::max);
                    }
                });
            }
        }
        return totalMap;
    }

    /**
     * 为指定建筑变体构造 level = -1 的聚合汇总记录 {@code a_All}。
     *
     * @param parentSet  所属建筑计划集
     * @param variantKey 变体标识键（如 "a", "b", "c"）
     * @param levelDefs  该变体包含的所有等级列表
     * @return 聚合生成的 {@link LevelDef} 对象
     */
    public static LevelDef createVariantSummaryLevel(BuildingPlanSet parentSet, String variantKey, List<LevelDef> levelDefs) {
        if (parentSet == null || levelDefs == null || levelDefs.isEmpty()) return null;

        LevelDef sample = levelDefs.get(0);
        Map<ResourceLocation, Integer> totalResources = calculateVariantTotalResources(levelDefs);

        ResourceLocation samplePlanId = sample.planId();
        String samplePath = samplePlanId != null ? samplePlanId.getPath() : (parentSet.buildingId() + "_" + variantKey + "_0");
        String allPath = samplePath.replaceAll("_[0-9]+$", "_all");
        ResourceLocation allPlanId = ResourceLocation.fromNamespaceAndPath(
                samplePlanId != null ? samplePlanId.getNamespace() : parentSet.id().getNamespace(),
                allPath
        );

        LevelDef summaryLevel = new LevelDef(
                -1, // level == -1 标识为全等级聚合总和
                allPlanId,
                sample.nbtPath(),
                sample.width(),
                sample.height(),
                sample.depth(),
                sample.groundLevel(),
                sample.priority(),
                parentSet.nativeName() != null ? parentSet.nativeName() : sample.nativeName(),
                sample.requiredTags(),
                sample.forbiddenTagsInVillage(),
                sample.requiredVillageTags(),
                sample.parentTags(),
                sample.requiredParentTags(),
                sample.clearTags(),
                sample.villageTags(),
                totalResources,
                sample.subBuildings(),
                sample.signOrder(),
                sample.pathLevel(),
                sample.rebuildPath(),
                sample.pathWidth(),
                sample.abstractedProduction(),
                sample.irrigation()
        );

        BuildingLevelCostCategory.registerSyntheticParent(summaryLevel, parentSet);
        return summaryLevel;
    }

    /**
     * 获取指定建筑的所有变体配方列表（每个变体开头插入对应的 {@code a_All} 汇总页，紧接着其各等级细项）。
     *
     * @param planSet 建筑计划集
     * @return 完整有序的 LevelDef 列表
     */
    public static List<LevelDef> getFullVariantLevelsForBuilding(BuildingPlanSet planSet) {
        if (planSet == null || planSet.variants() == null || planSet.variants().isEmpty()) {
            return List.of();
        }

        ResourceLocation id = planSet.id();
        if (id == null) {
            return buildFullVariantLevels(planSet);
        }

        return FULL_VARIANT_LEVELS_CACHE.computeIfAbsent(id, k -> buildFullVariantLevels(planSet));
    }

    private static List<LevelDef> buildFullVariantLevels(BuildingPlanSet planSet) {
        List<LevelDef> result = new ArrayList<>();
        for (Map.Entry<String, List<LevelDef>> entry : planSet.variants().entrySet()) {
            String variantKey = entry.getKey();
            List<LevelDef> levelDefs = entry.getValue();
            if (levelDefs == null || levelDefs.isEmpty()) continue;

            LevelDef summary = createVariantSummaryLevel(planSet, variantKey, levelDefs);
            if (summary != null) {
                result.add(summary);
            }
            result.addAll(levelDefs);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * 获取传入建筑集 {@link BuildingPlanSet} 中各个变体升级所需的道具用量列表（带结果缓存）。
     *
     * @param planSet 传入的建筑集对象 {@link BuildingPlanSet}
     * @return 映射 {@link Map}，Key 为变体名称 {@link String}，Value 为该变体所需的所有道具用量列表 {@link List}&lt;{@link ItemStack}&gt;。
     */
    public static Map<String, List<ItemStack>> getBuildingUpgradeCosts(BuildingPlanSet planSet) {
        if (planSet == null || planSet.variants() == null || planSet.variants().isEmpty()) {
            return Collections.emptyMap();
        }

        ResourceLocation planId = planSet.id();
        if (planId == null) {
            return calculateCosts(planSet);
        }

        return UPGRADE_COST_CACHE.computeIfAbsent(planId, k -> calculateCosts(planSet));
    }

    /**
     * 内部实际计算建材消耗逻辑。
     *
     * @param planSet 建筑集对象 {@link BuildingPlanSet}
     * @return 各变体的材料消耗映射
     */
    private static Map<String, List<ItemStack>> calculateCosts(BuildingPlanSet planSet) {
        Map<String, List<ItemStack>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<LevelDef>> entry : planSet.variants().entrySet()) {
            String variantKey = entry.getKey();
            List<LevelDef> levelDefs = entry.getValue();

            if (levelDefs == null || levelDefs.isEmpty()) {
                continue;
            }

            Map<ResourceLocation, Integer> maxCostMap = calculateVariantTotalResources(levelDefs);

            if (!maxCostMap.isEmpty()) {
                List<ItemStack> stacks = new ArrayList<>();
                maxCostMap.forEach((itemRl, count) -> {
                    Item item = BuiltInRegistries.ITEM.get(itemRl);
                    if (item != null) {
                        stacks.add(new ItemStack(item, count));
                    }
                });
                result.put(variantKey, stacks);
            }
        }

        return result;
    }
}
