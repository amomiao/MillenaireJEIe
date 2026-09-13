package com.momos.millenairejeimm.jei;

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
 * 直接依赖 {@link LevelDef#requiredResources()} 原生 API 并提供并发安全的结果缓存机制。
 * </p>
 */
public final class BuildingUpgradeCostHelper {
    /**
     * 建筑集升满等级消耗结果缓存。
     * Key 为建筑集的 ID {@link BuildingPlanSet#id()}，
     * Value 为变体名称与其升满所需的 {@link ItemStack} 列表映射。
     */
    private static final Map<ResourceLocation, Map<String, List<ItemStack>>> UPGRADE_COST_CACHE = new ConcurrentHashMap<>();

    private BuildingUpgradeCostHelper() {}

    /**
     * 清空所有本地缓存。
     * <p>可在切换数据包、重载资源或需要重新计算建材消耗时调用。</p>
     */
    public static void clearCache() {
        UPGRADE_COST_CACHE.clear();
    }

    /**
     * 获取传入建筑集 {@link BuildingPlanSet} 中各个变体升级所需的道具用量列表（带结果缓存）。
     * <p>
     * <b>[醒目重构注释]</b> 本方法已放弃原有的 NBT 加载与建材计算器逻辑，
     * 直接通过 {@link LevelDef#requiredResources()} 提取升级材料计数。
     * </p>
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

        // [醒目优化注释] 使用 computeIfAbsent 保证 JEI 多线程检索时的线程安全与并发效率
        return UPGRADE_COST_CACHE.computeIfAbsent(planId, k -> calculateCosts(planSet));
    }

    /**
     * 内部实际计算建材消耗逻辑。
     *
     * @param planSet 建筑集对象 {@link BuildingPlanSet}
     * @return 各变体的材料消耗映射
     */
    private static Map<String, List<ItemStack>> calculateCosts(BuildingPlanSet planSet) {
        Map<String, List<ItemStack>> result = new HashMap<>();

        for (Map.Entry<String, List<LevelDef>> entry : planSet.variants().entrySet()) {
            String variantKey = entry.getKey();
            List<LevelDef> levelDefs = entry.getValue();

            if (levelDefs == null || levelDefs.isEmpty()) {
                continue;
            }

            // [醒目新注释] 存储当前变体各道具的最大用量映射（按各等级中的峰值计算需求）
            Map<ResourceLocation, Integer> maxCostMap = new HashMap<>();

            for (LevelDef levelDef : levelDefs) {
                if (levelDef == null) continue;

                // [醒目重构注释] 直接通过 API 获取 LevelDef 内部的升级材料计数 Map
                Map<ResourceLocation, Integer> requiredResources = levelDef.requiredResources();
                if (requiredResources != null && !requiredResources.isEmpty()) {
                    requiredResources.forEach((itemRl, count) ->
                            maxCostMap.merge(itemRl, count, Math::max)
                    );
                }
            }

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