package com.momos.millenairejeimm.jei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.Culture;
import org.millenaire.culture.ModCultures;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

import java.util.Locale;

/**
 * 千年百科 JEI 自定义成分类型 {@link IIngredientType} 与 {@link Codec} 编解码器注册表。
 */
public final class MillIngredientTypes {
    public static final String MOD_ID = "millenaire";
    // region 槽位与分类 ID 常量声明
    // 文化及其详细信息
    public static final String CultureCategoryPath = "aa_culture_category";
    public static final String CultureVillagesCategoryPath = "ab_culture_villages_category";
    // 建筑及其详细信息
    public static final String BuildingCategoryPath = "ba_building_category";
    public static final String BuildingVillageCategoryPath = "bb_building_village_category";
    public static final String BuildingVillagerCategoryPath = "bc_building_villager_category";
    public static final String BuildingCostCategoryAPath = "bd_building_cost_category_a";
    public static final String BuildingCostCategoryBPath = "be_building_cost_category_b";
    public static final String BuildingCostCategoryCPath = "bf_building_cost_category_c";
    // 村庄及其详细信息
    public static final String VillageCategoryPath = "ca_village_category";
    public static final String VillageBuildingsCategoryPath = "cb_village_buildings_category";
    public static final String VillageVillagersCategoryPath = "cc_village_villagers_category";
    // 村民及其详细信息
    public static final String VillagerCategoryPath = "da_villager_category";
    public static final String VillagerVillageCategoryPath = "db_villager_village_category";
    public static final String VillagerBuildingCategoryPath = "dc_villager_building_category";
    public static final String VillagerBringBackHomeCategoryPath = "dd_villager_bring_back_home_category";
    public static final String VillagerCollectCategoryPath = "de_villager_collect_category";
    public static final String VillagerRequiredCategoryPath = "df_villager_required_category";
    // endregion 槽位与分类 ID 常量声明


    public static final IIngredientType<Culture> CULTURE = () -> Culture.class;
    public static final IIngredientType<BuildingPlanSet> BUILDING = () -> BuildingPlanSet.class;
    public static final IIngredientType<VillagerType> VILLAGER = () -> VillagerType.class;
    public static final IIngredientType<VillageType> VILLAGE = () -> VillageType.class;

    /** 文化 {@link Culture} 的 JEI 序列化编解码器 */
    public static final Codec<Culture> CULTURE_CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> {
                Culture culture = ModCultures.getCulture(id);
                return culture != null
                        ? DataResult.success(culture)
                        : DataResult.error(() -> "Unknown Culture ingredient: " + id);
            },
            Culture::id
    );

    /** 村庄类型 {@link VillageType} 的 JEI 序列化编解码器 */
    public static final Codec<VillageType> VILLAGE_CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> {
                VillageType village = MillenaireAPIHelper.getAllVillageType().get(id);
                return village != null
                        ? DataResult.success(village)
                        : DataResult.error(() -> "Unknown VillageType ingredient: " + id);
            },
            VillageType::id
    );

    /** 建筑集 {@link BuildingPlanSet} 的 JEI 序列化编解码器 */
    /** 建筑集 {@link BuildingPlanSet} 的 JEI 序列化编解码器 */
    public static final Codec<BuildingPlanSet> BUILDING_CODEC = Codec.STRING.comapFlatMap(
            id -> {
                if (id == null || id.isEmpty()) {
                    return DataResult.error(() -> "Building ID is null or empty");
                }

                java.util.Map<ResourceLocation, BuildingPlanSet> allBuildings = MillenaireAPIHelper.getAllBuildingPlanSets();

                // [醒目新注释] 根本原因修复：getAllBuildingPlanSets() 返回 Key 类型为 {@link ResourceLocation}。
                // 必须将输入的 String 转换为 ResourceLocation，否则 Map.get(String) 会因类型不匹配永恒返回 null。

                // 1. 优先尝试将冒号/斜杠转换后构造标准 ResourceLocation ("norman:abbaye_a_altar" -> "millenaire:norman/abbaye_a_altar")
                String cleanPath = id.startsWith("millenaire:") ? id.substring("millenaire:".length()) : id;
                cleanPath = cleanPath.replace(':', '/').toLowerCase(Locale.ROOT);
                ResourceLocation targetRl = ResourceLocation.fromNamespaceAndPath("millenaire", cleanPath);
                BuildingPlanSet building = allBuildings.get(targetRl);

                // 2. 尝试直接按 ResourceLocation 全名解析 (如 "millenaire:norman/abbaye_a_altar")
                if (building == null) {
                    ResourceLocation parsedRl = ResourceLocation.tryParse(id);
                    if (parsedRl != null) {
                        building = allBuildings.get(parsedRl);
                    }
                }

                // 3. 兜底策略：遍历 Map 的 ResourceLocation 键匹配 Path 后半段 (应对纯 buildingId 或无文化前缀的情况)
                if (building == null) {
                    String lowerId = id.toLowerCase(Locale.ROOT);
                    for (java.util.Map.Entry<ResourceLocation, BuildingPlanSet> entry : allBuildings.entrySet()) {
                        ResourceLocation key = entry.getKey();
                        if (key.getPath().equalsIgnoreCase(cleanPath) || key.getPath().endsWith("/" + lowerId)) {
                            building = entry.getValue();
                            break;
                        }
                    }
                }

                // 2. 匹配成功返回 success；匹配失败严格返回 DataResult.error，防止向 JEI 返回 null
                if (building != null) {
                    return DataResult.success(building);
                }
                return DataResult.error(() -> "Unknown BuildingPlanSet ingredient: " + id);
            },
            building -> {
                if (building == null) return "millenaire:unknown/unknown";
                String culturePath = building.culture() != null ? building.culture().getPath() : "unknown";
                String buildingId = building.buildingId() != null ? building.buildingId() : "unknown";
                // [醒目新注释] 序列化时统一输出带有 millenaire 命名空间的标准 ResourceLocation 格式 ("millenaire:culture/building_id")
                return "millenaire:" + culturePath + "/" + buildingId;
            }
    );

    /** 村民类型 {@link VillagerType} 的 JEI 序列化编解码器 */
    public static final Codec<VillagerType> VILLAGER_CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> {
                VillagerType villager = MillenaireAPIHelper.getAllVillagerType().get(id);
                return villager != null
                        ? DataResult.success(villager)
                        : DataResult.error(() -> "Unknown VillagerType ingredient: " + id);
            },
            VillagerType::id
    );

    private MillIngredientTypes() {}
}