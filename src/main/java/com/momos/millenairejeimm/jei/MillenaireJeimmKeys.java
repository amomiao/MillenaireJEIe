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
    public static final String PrefixIngredien = Prefix + "ingredien.";
    // 文化
    public static final KT UnknownCultureIng = new KT(PrefixIngredien + "unknown_culture", "Unknown Culture");
    public static final KT CultureIngR_S = new KT(PrefixIngredien + "culture", "%s");
    // 村庄
    public static final KT UnknownVillageIng = new KT(PrefixIngredien + "unknown_village", "Unknown Village");
    public static final KT VillageIngR_S = new KT(PrefixIngredien + "village", "%s");
    // 建筑
    public static final KT UnknownBuildingIng = new KT(PrefixIngredien + "unknown_building", "Unknown Building");
    public static final KT BuildingIngR_S = new KT(PrefixIngredien + "building", "%s");
    // 村民
    public static final KT UnknownVillagerIng = new KT(PrefixIngredien + "unknown_villager", "Unknown Villager");
    public static final KT VillagerIngR_S = new KT(PrefixIngredien + "villager", "%s");

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
    public static final KT BuildingCostCategory = new KT(PrefixCategory + "building_cost", "Building Construction Costs");
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
    public static final String PrefixSecTitle = Prefix + "sec_title.";
    // 文化
    public static final KT CultureVillagesCategoryTitle_S = new KT(PrefixSecTitle + "culture_villages", "Belong %S");
    // 村庄
    public static final KT VillageBuildingsCategoryTitle_S = new KT(PrefixSecTitle + "village_buildings", "Belong %S");
    public static final KT VillageVillagersCategoryTitle_S = new KT(PrefixSecTitle + "village_villagers", "Belong %S");
    // 建筑
    public static final KT AbstractBuildingCostCategoryTitle_S = new KT(PrefixSecTitle + "building_cost", "Belong %S");
    public static final KT BuildingVillageCategoryTitle_S = new KT(PrefixSecTitle + "building_village", "Belong %S");
    public static final KT BuildingVillagerCategoryTitle_S = new KT(PrefixSecTitle + "building_villager", "Belong %S");
    // 村民
    public static final KT VillagerBuildingCategoryTitle_S = new KT(PrefixSecTitle + "villager_building", "Belong %S");
    public static final KT VillagerVillageCategoryTitle_S = new KT(PrefixSecTitle + "villager_village", "Belong %S");
    public static final KT VillagerBringBackHomeCategoryTitle_S = new KT(PrefixSecTitle + "villager_bring_back_home", "Belong %S");
    public static final KT VillagerCollectCategoryTitle_S = new KT(PrefixSecTitle + "villager_collect", "Belong %S");
    public static final KT VillagerRequiredCategoryTitle_S = new KT(PrefixSecTitle + "villager_required", "Belong %S");

    /* 一级页面的具体文本 */
    public static final String PrefixLabel = Prefix + "label.";
    public static final String PrefixValue = Prefix + "value.";
    public static final String PrefixLink = Prefix + "link.";
    /* CultureCategory 标签 */
    public static final KT NativeLanguageLabel = new KT(PrefixLabel + "native_language", "Native Language: ");
    public static final KT KnownCropsLabel = new KT(PrefixLabel + "known_crops", "Known Crops: ");
    public static final KT HuntingDropsLabel = new KT(PrefixLabel + "hunting_drops", "Hunting Drops: ");
    public static final KT MapIconLabel = new KT(PrefixLabel + "map_icon", "Map Icon: ");
    public static final KT CultureBannerLabel = new KT(PrefixLabel + "culture_banner", "Culture Banner: ");
    public static final KT PanelTextureLabel = new KT(PrefixLabel + "panel_texture", "Panel Texture: ");
    // 状态/数值及其回退值
    public static final KT UnsetValue = new KT(PrefixValue + "unset", "Unset");
    public static final KT ConfiguredValue = new KT(PrefixValue + "configured", "Configured");
    public static final KT UnconfiguredValue = new KT(PrefixValue + "unconfigured", "Unconfigured");
    public static final KT HasNbtValue = new KT(PrefixValue + "has_nbt", "Has NBT");
    public static final KT NoNbtValue = new KT(PrefixValue + "no_nbt", "No NBT");
    public static final KT DefaultValue = new KT(PrefixValue + "default", "Default");
    public static final KT CropCountValue_D = new KT(PrefixValue + "crop_count", "%d types");
    public static final KT DropCountValue_D = new KT(PrefixValue + "drop_count", "%d types");
    // LayoutBox标题
    public static final KT VillageBoxLink = new KT(PrefixLink + "village_box", "Village");
    /* BuildignCategory 标签 */
    public static final KT PriceLabel = new KT(PrefixLabel + "price", "Price: ");
    public static final KT ReputationLabel = new KT(PrefixLabel + "reputation", "Reputation: ");
    public static final KT MaxCountLabel = new KT(PrefixLabel + "max_count", "Max Count: ");
    public static final KT MoveInPriorityLabel = new KT(PrefixLabel + "move_in_priority", "Move-in Priority: ");
    public static final KT CultureLabel = new KT(PrefixLabel + "culture", "Culture: ");
    public static final KT FlagsLabel = new KT(PrefixLabel + "flags", "Flags: ");
    // 状态/数值及其回退值
    public static final KT MaxCountValue_D = new KT(PrefixValue + "max_count", "%d");
    public static final KT CommonCultureValue = new KT(PrefixValue + "common_culture", "Common");
    public static final KT TownHallFlag = new KT(PrefixValue + "town_hall", "Town Hall");
    public static final KT ResidentialFlag = new KT(PrefixValue + "residential", "Residential");
    public static final KT SubBuildingFlag = new KT(PrefixValue + "sub_building", "Sub-Building");
    public static final KT WallSegmentFlag = new KT(PrefixValue + "wall_segment", "Wall Segment");
    public static final KT NoneFlag = new KT(PrefixValue + "none", "None");
    // LayoutBox标题
    public static final KT VillagesBoxLink = new KT(PrefixLink + "villages_box", "Villages");
    public static final KT VillagersBoxLink = new KT(PrefixLink + "villagers_box", "Villagers");
    public static final KT VariantCostBoxLink_S = new KT(PrefixLink + "variant_cost", "Variant %s Cost");
    /* VillageCategory 标签 */
    public static final KT TypeLabel = new KT(PrefixLabel + "type", "Type: ");
    public static final KT RadiusLabel = new KT(PrefixLabel + "radius", "Radius: ");
    public static final KT MaxConstructionsLabel = new KT(PrefixLabel + "max_constructions", "Max Constructions: ");
    public static final KT WeightLabel = new KT(PrefixLabel + "weight", "Weight: ");
    public static final KT MinDistanceSpawnLabel = new KT(PrefixLabel + "min_distance_spawn", "Min Spawn Distance: ");
    public static final KT DefenseLabel = new KT(PrefixLabel + "defense", "Defense: ");
    public static final KT ControlLabel = new KT(PrefixLabel + "control", "Control: ");
    // 状态/数值及其回退值
    public static final KT UnknownCultureValue = new KT(PrefixValue + "unknown_culture", "Unknown Culture");
    public static final KT MarvelValue = new KT(PrefixValue + "marvel", "Marvel");
    public static final KT HamletValue = new KT(PrefixValue + "hamlet", "Hamlet");
    public static final KT LoneBuildingValue = new KT(PrefixValue + "lone_building", "Lone Building");
    public static final KT NormalVillageValue = new KT(PrefixValue + "normal_village", "Normal Village");
    public static final KT InnerWallValue = new KT(PrefixValue + "inner_wall", "[Inner Wall] ");
    public static final KT OuterWallValue = new KT(PrefixValue + "outer_wall", "[Outer Wall] ");
    public static final KT RaidValue = new KT(PrefixValue + "raid", "[Raid] ");
    public static final KT NoDefenseValue = new KT(PrefixValue + "no_defense", "No Defense");
    public static final KT PlayerControlledValue = new KT(PrefixValue + "player_controlled", "Player Controlled");
    public static final KT AutoControlledValue = new KT(PrefixValue + "auto_controlled", "Auto Development");
    public static final KT MeterValue_D = new KT(PrefixValue + "meter_d", "%dm");
    // LayoutBox标题
    public static final KT BuildingBoxLink = new KT(PrefixLink + "building_box", "Buildings");
    public static final KT VillagerBoxLink = new KT(PrefixLink + "villager_box", "Villagers");
    /* VillagerCategory 标签 */
    public static final KT GenderStageLabel = new KT(PrefixLabel + "gender_stage", "Gender / Stage: ");
    public static final KT MaxHealthLabel = new KT(PrefixLabel + "max_health", "Max Health: ");
    public static final KT BaseAttackLabel = new KT(PrefixLabel + "base_attack", "Base Attack: ");
    public static final KT HiringCostLabel = new KT(PrefixLabel + "hiring_cost", "Hiring Cost: ");
    public static final KT SpawnWeightLabel = new KT(PrefixLabel + "spawn_weight", "Spawn Weight: ");
    //public static final KT FlagsLabel = new KT(PrefixLabel + "flags", "Flags: ");
    // 状态/数值及其回退值
    public static final KT UnknownGenderValue = new KT(PrefixValue + "unknown_gender", "Unknown");
    public static final KT ChildValue = new KT(PrefixValue + "child", "Child");
    public static final KT AdultValue = new KT(PrefixValue + "adult", "Adult");
    public static final KT HpValue_D = new KT(PrefixValue + "hp_d", "%d HP");
    public static final KT CoinsValue_D = new KT(PrefixValue + "coins_d", "%d Coins");
    public static final KT UnhirableValue = new KT(PrefixValue + "unhirable", "Unhirable");
    public static final KT RaiderValue = new KT(PrefixValue + "raider", "[Raider] ");
    public static final KT HostileValue = new KT(PrefixValue + "hostile", "[Hostile] ");
    public static final KT HelpAttacksValue = new KT(PrefixValue + "help_attacks", "[Help Attacks] ");
    public static final KT ArcherValue = new KT(PrefixValue + "archer", "[Archer] ");
    public static final KT NoTeleportValue = new KT(PrefixValue + "no_teleport", "[No Teleport] ");
    public static final KT NoResurrectValue = new KT(PrefixValue + "no_resurrect", "[No Resurrect] ");
    public static final KT CivilianValue = new KT(PrefixValue + "civilian", "Civilian");
    // LayoutBox标题
    public static final KT BringBackHomeBoxLink = new KT(PrefixLink + "bring_back_home_box", "Bring Back Home Goods");
    public static final KT CollectBoxLink = new KT(PrefixLink + "collect_box", "Collect Goods");
    public static final KT RequiredBoxLink = new KT(PrefixLink + "required_box", "Required Goods");
}