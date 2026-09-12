package com.momos.millenairejeimm.jei;

import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

/**
 * JEI 模组本地化 Key 集中定义类 {@link MillenaireJeimmKeys}。
 * 提供参数化模板 Key 以减少冗余条目，并统一使用 {@link Component#translatableWithFallback(String, String, Object...)} 处理字符串回退逻辑。
 */
public final class MillenaireJeimmKeys {
    /** KeyTuple */
    public record KT(String key, String fallback) {
        public KT {
            if (key == null) key = "";
            if (fallback == null) fallback = "";
        }
        public MutableComponent asComponent(Object... args) {
            return Component.translatableWithFallback(key, fallback, args);
        }
    }

    private MillenaireJeimmKeys() {}
    public static final String Prefix = "gui.millenairejeimm.";

    public static String of(String path) {return Prefix + path;}
    static MutableComponent tf(String key, @Nullable String fallback) {
        return Component.translatableWithFallback(key,fallback);
    }

    /* 要素(Ingredien) */
    /* 页签-类别(Category) */
    /// 此类key调用位置在:[BuildingCategory#getTitle()]
    public static final String PrefixCategory = Prefix + "category.";
    // 文化: 2 个分类
    public static final KT CultureCategory = new KT(PrefixCategory + "culture", "Culture Info");
    public static final KT CultureVillagesCategory = new KT(PrefixCategory + "culture_villages", "Culture's Villages");
    // 村庄: 3 个分类
    public static final KT VillageCategory = new KT(PrefixCategory + "village", "Village Info");
    public static final KT VillageBuildingsCategory = new KT(PrefixCategory + "village_buildings", "Village's Buildings");
    public static final KT VillageVillagersCategory = new KT(PrefixCategory + "village_villagers", "Village's Villagers");
    // 建筑: 6 个分类
    public static final KT BuildingCategory = new KT(PrefixCategory + "building", "Building Info");
    public static final KT BuildingCostCategoryA = new KT(PrefixCategory + "building_cost_a", "Building Construction Costs (Stage A)");
    public static final KT BuildingCostCategoryB = new KT(PrefixCategory + "building_cost_b", "Building Construction Costs (Stage B)");
    public static final KT BuildingCostCategoryC = new KT(PrefixCategory + "building_cost_c", "Building Construction Costs (Stage C)");
    public static final KT BuildingVillageCategory = new KT(PrefixCategory + "building_village", "Building Belongs To Village");
    public static final KT BuildingVillagerCategory = new KT(PrefixCategory + "building_villager", "Building Inhabited Villagers");
    // 村民: 6 个分类
    public static final KT VillagerCategory = new KT(PrefixCategory + "villager", "Villager Info");
    public static final KT VillagerBuildingCategory = new KT(PrefixCategory + "villager_building", "Villager Residing Building");
    public static final KT VillagerVillageCategory = new KT(PrefixCategory + "villager_village", "Villager Belongs To Village");
    public static final KT VillagerBringBackHomeCategory = new KT(PrefixCategory + "villager_bring_back_home", "Villager Bring Back Home");
    public static final KT VillagerCollectCategory = new KT(PrefixCategory + "villager_collect", "Villager Collect Items");
    public static final KT VillagerRequiredCategory = new KT(PrefixCategory + "villager_required", "Villager Required Items");

    /* 二级页签的第二标题 */


    /* label 各页面的具体文本 */
    public static final String PrefixLabel = Prefix + "label.";
    public static final String PrefixValue = Prefix + "value.";
    public static final String PrefixLink = Prefix + "link.";
    // CultureCategory 标签
    public static final KT NativeLanguageLabel = new KT(PrefixLabel + "native_language", "Native Language: ");
    public static final KT KnownCropsLabel = new KT(PrefixLabel + "known_crops", "Known Crops: ");
    public static final KT HuntingDropsLabel = new KT(PrefixLabel + "hunting_drops", "Hunting Drops: ");
    public static final KT MapIconLabel = new KT(PrefixLabel + "map_icon", "Map Icon: ");
    public static final KT CultureBannerLabel = new KT(PrefixLabel + "culture_banner", "Culture Banner: ");
    public static final KT PanelTextureLabel = new KT(PrefixLabel + "panel_texture", "Panel Texture: ");
    // 状态/数值回退值
    public static final KT UnsetValue = new KT(PrefixValue + "unset", "Unset");
    public static final KT ConfiguredValue = new KT(PrefixValue + "configured", "Configured");
    public static final KT UnconfiguredValue = new KT(PrefixValue + "unconfigured", "Unconfigured");
    public static final KT HasNbtValue = new KT(PrefixValue + "has_nbt", "Has NBT");
    public static final KT NoNbtValue = new KT(PrefixValue + "no_nbt", "No NBT");
    public static final KT DefaultValue = new KT(PrefixValue + "default", "Default");
    public static final KT CropCountValue = new KT(PrefixValue + "crop_count", "%d types");
    public static final KT DropCountValue = new KT(PrefixValue + "drop_count", "%d types");
    // LayoutBox标题
    public static final KT VillageBoxLink = new KT(PrefixLink + "village_box", "Village");
}