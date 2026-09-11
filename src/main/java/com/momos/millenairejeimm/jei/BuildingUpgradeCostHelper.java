// region 提取文化名与NBT文件加载及双重缓存

package com.momos.millenairejeimm.jei;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.millenaire.building.BuildingCostCalculator;
import org.millenaire.building.BuildingPlan;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.building.BuildingPlanSet.LevelDef;
import org.millenaire.building.TemplateLoader;
import org.millenaire.content.ContentFs;
import org.millenaire.content.CustomContentIndex;
import org.millenaire.content.Resource;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 建筑升级道具消耗计算工具类。
 * <p>
 * 专门用于提取和计算 {@link BuildingPlanSet} 建筑升满等级所需的建材消耗，
 * 并提供基于文化文件系统 {@link ContentFs} 的 NBT 结构加载与双重缓存机制。
 * </p>
 */
public final class BuildingUpgradeCostHelper {

    // -------------------------------------------------------------
    // ★ 新增缓存容器：用于加速 JEI 频繁查询与避免重复磁盘/Zip IO
    // -------------------------------------------------------------

    /**
     * 建筑集升满等级消耗结果缓存。
     * Key 为建筑集的 ID {@link BuildingPlanSet#id()}，
     * Value 为变体名称与其升满所需的 {@link ItemStack} 列表映射。
     */
    private static final Map<ResourceLocation, Map<String, List<ItemStack>>> UPGRADE_COST_CACHE = new ConcurrentHashMap<>();

    /**
     * NBT 标签文件数据缓存。
     * Key 为文化与相对路径构成的组合键（例如 "seljuk:buildings/houses/mosque_a_0.nbt"），
     * Value 为解析后的 {@link CompoundTag}。
     */
    private static final Map<String, CompoundTag> NBT_TAG_CACHE = new ConcurrentHashMap<>();

    private BuildingUpgradeCostHelper() {}

    /**
     * 清空所有本地缓存。
     * <p>可在切换数据包、重载资源或需要重新计算建材消耗时调用。</p>
     */
    public static void clearCache() {
        UPGRADE_COST_CACHE.clear();
        NBT_TAG_CACHE.clear();
    }

    /**
     * 计算并获取传入建筑集 {@link BuildingPlanSet} 中各个变体升满等级所需的道具用量列表（带结果缓存）。
     *
     * @param planSet 传入的建筑集对象 {@link BuildingPlanSet}
     * @return 映射 {@link Map}，Key 为变体名称 {@link String}，Value 为升满该变体所需的所有道具用量列表 {@link List}&lt;{@link ItemStack}&gt;。
     */
    public static Map<String, List<ItemStack>> getBuildingUpgradeCosts(BuildingPlanSet planSet) {
        if (planSet == null || planSet.variants() == null || planSet.variants().isEmpty()) {
            return Collections.emptyMap();
        }

        // ★ 新增：优先查结果缓存，若已计算过则直接返回缓存结果
        ResourceLocation planId = planSet.id();
        if (planId != null && UPGRADE_COST_CACHE.containsKey(planId)) {
            return UPGRADE_COST_CACHE.get(planId);
        }

        Map<String, List<ItemStack>> result = new HashMap<>();

        // ★ 新增加强注释：精准提取文化名（直接使用 planSet.culture().getPath()）
        String cultureName = extractCultureName(planSet);

        BuildingPlan fallbackPlan = null;
        if (planId != null) {
            fallbackPlan = MillenaireAPIHelper.getAllBuildingPlans().get(planId);
        }

        for (Map.Entry<String, List<LevelDef>> entry : planSet.variants().entrySet()) {
            String variantKey = entry.getKey();
            List<LevelDef> levelDefs = entry.getValue();

            // [醒目新注释] 修正：移除原先 levelDefs.size() <= 1 的跳过条件，使得仅有 1 级的单级建筑变体也能正常计算初始消耗
            if (levelDefs == null || levelDefs.isEmpty()) {
                continue;
            }

            // [醒目新注释] 存储当前变体各道具的最大用量映射（消除 NBT 结构快照重复累加导致的建材水分）
            Map<ResourceLocation, Integer> maxCostMap = new HashMap<>();

            for (LevelDef levelDef : levelDefs) {
                if (levelDef == null) continue;

                String nbtPath = levelDef.nbtPath();
                if ((nbtPath == null || nbtPath.isBlank()) && fallbackPlan != null) {
                    nbtPath = fallbackPlan.nbtPath();
                }

                if (nbtPath != null && !nbtPath.isBlank()) {
                    // 调用带缓存的 NBT 加载方法
                    CompoundTag tag = loadCompoundTag(cultureName, nbtPath);
                    if (tag != null) {
                        Map<ResourceLocation, Integer> costMap = BuildingCostCalculator.computeCost(tag);
                        if (costMap != null && !costMap.isEmpty()) {
                            // [醒目新注释] 取各等级中的峰值最大用量能反映最真实准确的材料需求
                            costMap.forEach((itemRl, count) ->
                                    maxCostMap.merge(itemRl, count, Math::max)
                            );
                        }
                    }
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

        // ★ 新增：将本次计算好的结果写入结果缓存
        if (planId != null && !result.isEmpty()) {
            UPGRADE_COST_CACHE.put(planId, result);
        }

        return result;
    }

    /**
     * 从 {@link BuildingPlanSet} 中安全提取文化标识字符串。
     *
     * ★ 新加注释：修复原 contains("/") 判断导致的 seljuk 等单级路径文化名取空问题。
     *
     * @param planSet 建筑集对象 {@link BuildingPlanSet}
     * @return 文化标识名称（如 "seljuk", "norman"），提取失败则返回空字符串
     */
    private static String extractCultureName(BuildingPlanSet planSet) {
        if (planSet == null) {
            return "";
        }

        // 1. 优先使用 record 的 culture() 字段
        if (planSet.culture() != null) {
            String path = planSet.culture().getPath();
            if (!path.isBlank()) {
                // 若路径为 "seljuk"，直接返回 "seljuk"；若带斜杠如 "seljuk/sub"，取第一段
                return path.contains("/") ? path.split("/")[0] : path;
            }
        }

        // 2. 备用逻辑：尝试从 id() 提取
        if (planSet.id() != null) {
            String path = planSet.id().getPath();
            if (!path.isBlank()) {
                return path.contains("/") ? path.split("/")[0] : path;
            }
        }

        return "";
    }

    /**
     * 根据文化名与相对路径，加载对应的 NBT 结构标签 {@link CompoundTag}（带文件解压缓存，去除了调试日志）。
     *
     * @param cultureName 文化名称（如 "seljuk"）
     * @param nbtPath     相对 NBT 路径（如 "buildings/houses/mosque_a_0"）
     * @return 成功加载的 {@link CompoundTag}，失败返回 {@code null}
     */
    public static CompoundTag loadCompoundTag(String cultureName, String nbtPath) {
        if (nbtPath == null || nbtPath.isBlank()) return null;

        String formattedPath = nbtPath.endsWith(".nbt") ? nbtPath : nbtPath + ".nbt";
        String cacheKey = cultureName + ":" + formattedPath;

        // ★ 新增：优先查询 NBT 标签缓存，避免重复进行 IO 与流解压
        if (NBT_TAG_CACHE.containsKey(cacheKey)) {
            return NBT_TAG_CACHE.get(cacheKey);
        }

        String culturePrefix = cultureName.isBlank() ? "" : cultureName + "/";

        // -------------------------------------------------------------
        // 通道 1: Millénaire CustomContentIndex / ContentFs (主要加载途径)
        // -------------------------------------------------------------
        try {
            CustomContentIndex index = CustomContentIndex.current();
            if (index != null) {
                ContentFs cultureFs = index.forCulture(cultureName);
                if (cultureFs != null) {
                    Optional<Resource> nbtRes = cultureFs.findFirst(formattedPath);
                    if (nbtRes.isPresent()) {
                        Resource res = nbtRes.get();
                        try (InputStream is = res.open()) {
                            NbtAccounter accounter = TemplateLoader.accounterFor(res.kind());
                            CompoundTag tag = NbtIo.readCompressed(is, accounter);
                            if (tag != null) {
                                NBT_TAG_CACHE.put(cacheKey, tag);
                                return tag;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}

        // -------------------------------------------------------------
        // 通道 2: Minecraft Client ResourceManager (备用途径)
        // -------------------------------------------------------------
        List<ResourceLocation> mcCandidates = List.of(
                ResourceLocation.fromNamespaceAndPath("millenaire", "cultures/" + culturePrefix + formattedPath),
                ResourceLocation.fromNamespaceAndPath("millenaire", culturePrefix + formattedPath),
                ResourceLocation.fromNamespaceAndPath("millenaire", "structures/" + culturePrefix + formattedPath)
        );

        var rm = Minecraft.getInstance().getResourceManager();
        for (ResourceLocation rl : mcCandidates) {
            try {
                var resOpt = rm.getResource(rl);
                if (resOpt.isPresent()) {
                    try (InputStream is = resOpt.get().open()) {
                        CompoundTag tag = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                        if (tag != null) {
                            NBT_TAG_CACHE.put(cacheKey, tag);
                            return tag;
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        // -------------------------------------------------------------
        // 通道 3: Java Classpath (备用途径)
        // -------------------------------------------------------------
        List<String> cpCandidates = List.of(
                "/millenaire/cultures/" + culturePrefix + formattedPath,
                "/assets/millenaire/cultures/" + culturePrefix + formattedPath
        );

        for (String cpPath : cpCandidates) {
            try (InputStream is = BuildingUpgradeCostHelper.class.getResourceAsStream(cpPath)) {
                if (is != null) {
                    CompoundTag tag = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                    if (tag != null) {
                        NBT_TAG_CACHE.put(cacheKey, tag);
                        return tag;
                    }
                }
            } catch (Exception ignored) {}
        }

        return null;
    }
}

// endregion