package com.momos.millenairejeimm.jei;

import net.minecraft.network.chat.Component;

/**
 * JEI 模组本地化 Key 集中定义类 {@link MillenaireJeimmKeys}。
 * 提供参数化模板 Key 以减少冗余条目，并统一使用 {@link Component#translatableWithFallback(String, String, Object...)} 处理字符串回退逻辑。
 */
public final class MillenaireJeimmKeys {
    private MillenaireJeimmKeys() {}

    // =========================================================================
    // 提取统一前缀常量与参数化 Key 构建工具函数
    // =========================================================================

    public static final String PREFIX = "gui.millenairejeimm.";
    public static final String PREFIX_JEIM = "gui.millenairejeim.";

    public static String of(String path) {
        return PREFIX + path;
    }

    public static String categoryOf(String categoryName) {
        return PREFIX_JEIM + "category." + categoryName;
    }

    // GUI 提示与通用文本
    public static final String UNKNOWN_BUILDING = PREFIX_JEIM + "unknown_building";
    public static final String UNKNOWN_CULTURE = PREFIX_JEIM + "unknown_culture";
    public static final String CULTURE_PREFIX = PREFIX_JEIM + "culture_prefix";
    public static final String ID_PREFIX = PREFIX_JEIM + "id_prefix";
    public static final String ASSOCIATED_TAGS = PREFIX_JEIM + "associated_tags";

    // JEI 分类标题 (Category Titles)
    public static final String CATEGORY_BUILDING_DETAILS = PREFIX_JEIM + "category.building_details";
    public static final String CATEGORY_BUILDING_COST = PREFIX_JEIM + "category.building_cost";
    public static final String CATEGORY_BUILDING_VILLAGES_FULL = PREFIX_JEIM + "category.building_villages_full";
    public static final String CATEGORY_BUILDING_VILLAGERS_FULL = PREFIX_JEIM + "category.building_villagers_full";
    public static final String CATEGORY_CULTURE_DETAILS = PREFIX_JEIM + "category.culture_details";
    public static final String CATEGORY_CULTURE_VILLAGES_FULL = PREFIX_JEIM + "category.culture_villages_full";

    // Header 标头文本
    public static final String HEADER_CULTURE_MAIN = PREFIX_JEIM + "header.culture_main";

    // 变体与所属文本模板
    public static final String VARIANT_TITLE = PREFIX_JEIM + "variant_title";
    public static final String BELONGING_BUILDING = PREFIX_JEIM + "belonging_building";
    public static final String BELONGING_BUILDING_VARIANT = PREFIX_JEIM + "belonging_building_variant";
    public static final String BELONGING_CULTURE = PREFIX_JEIM + "label.belonging_culture";

    // LinkBox 容器标题
    public static final String BOX_VILLAGES = PREFIX_JEIM + "box.villages";
    public static final String BOX_VILLAGERS = PREFIX_JEIM + "box.villagers";
    public static final String BOX_VARIANT_COST = PREFIX_JEIM + "box.variant_cost";

    // 属性标签 (Labels)
    public static final String LABEL_PRICE = PREFIX_JEIM + "label.price";
    public static final String LABEL_REPUTATION = PREFIX_JEIM + "label.reputation";
    public static final String LABEL_MAX_COUNT = PREFIX_JEIM + "label.max_count";
    public static final String VALUE_MAX_COUNT = PREFIX_JEIM + "value.max_count";
    public static final String LABEL_MOVE_IN_PRIORITY = PREFIX_JEIM + "label.move_in_priority";
    public static final String LABEL_CULTURE = PREFIX_JEIM + "label.culture";
    public static final String VALUE_CULTURE_COMMON = PREFIX_JEIM + "value.culture_common";
    public static final String LABEL_FLAGS = PREFIX_JEIM + "label.flags";
    public static final String LABEL_NATIVE_LANGUAGE = PREFIX_JEIM + "label.native_language";
    public static final String LABEL_KNOWN_CROPS = PREFIX_JEIM + "label.known_crops";
    public static final String LABEL_HUNTING_DROPS = PREFIX_JEIM + "label.hunting_drops";
    public static final String LABEL_MAP_ICON = PREFIX_JEIM + "label.map_icon";
    public static final String LABEL_CULTURE_BANNER = PREFIX_JEIM + "label.culture_banner";
    public static final String LABEL_PANEL_TEXTURE = PREFIX_JEIM + "label.panel_texture";

    // 特性 Flag 标语与通用状态值
    public static final String FLAG_TOWN_HALL = PREFIX_JEIM + "flag.town_hall";
    public static final String FLAG_RESIDENTIAL = PREFIX_JEIM + "flag.residential";
    public static final String FLAG_SUB_BUILDING = PREFIX_JEIM + "flag.sub_building";
    public static final String FLAG_WALL_SEGMENT = PREFIX_JEIM + "flag.wall_segment";
    public static final String FLAG_NONE = PREFIX_JEIM + "flag.none";

    public static final String VALUE_UNSET = PREFIX_JEIM + "value.unset";
    public static final String VALUE_CONFIGURED = PREFIX_JEIM + "value.configured";
    public static final String VALUE_UNCONFIGURED = PREFIX_JEIM + "value.unconfigured";
    public static final String VALUE_HAS_NBT = PREFIX_JEIM + "value.has_nbt";
    public static final String VALUE_NO_NBT = PREFIX_JEIM + "value.no_nbt";
    public static final String VALUE_DEFAULT = PREFIX_JEIM + "value.default";
    public static final String VALUE_CROP_COUNT = PREFIX_JEIM + "value.crop_count";
    public static final String VALUE_DROP_COUNT = PREFIX_JEIM + "value.drop_count";

    // region 村庄相关本地化键定义
    public static final String VILLAGE_BADGE = PREFIX + "village.badge";
    public static final String VILLAGE_UNKNOWN = PREFIX + "village.unknown";
    public static final String VILLAGE_CULTURE = PREFIX + "village.culture";
    public static final String VILLAGE_ID = PREFIX + "village.id";
    public static final String VILLAGE_TAGS = PREFIX + "village.tags";
    public static final String VILLAGE_LABEL_BELONGS = PREFIX + "village.label.belongs";
    public static final String VILLAGE_UNKNOWN_CULTURE = PREFIX + "village.unknown_culture";

    public static final String CATEGORY_VILLAGE_DETAILS = PREFIX + "category.village_details";
    public static final String CATEGORY_VILLAGE_BUILDINGS_FULL = PREFIX + "category.village_buildings_full";
    public static final String CATEGORY_VILLAGE_VILLAGERS_FULL = PREFIX + "category.village_villagers_full";

    public static final String VILLAGE_BOX_BUILDINGS = PREFIX + "village.box.buildings";
    public static final String VILLAGE_BOX_VILLAGERS = PREFIX + "village.box.villagers";

    public static final String VILLAGE_TYPE_LABEL = PREFIX + "village.type";
    public static final String VILLAGE_TYPE_MARVEL = PREFIX + "village.type.marvel";
    public static final String VILLAGE_TYPE_HAMLET = PREFIX + "village.type.hamlet";
    public static final String VILLAGE_TYPE_LONE = PREFIX + "village.type.lone";
    public static final String VILLAGE_TYPE_NORMAL = PREFIX + "village.type.normal";

    public static final String VILLAGE_RADIUS = PREFIX + "village.radius";
    public static final String VILLAGE_MAX_CONSTRUCTIONS = PREFIX + "village.max_constructions";
    public static final String VILLAGE_WEIGHT = PREFIX + "village.weight";
    public static final String VILLAGE_MIN_DISTANCE_SPAWN = PREFIX + "village.min_distance_spawn";

    public static final String VILLAGE_DEFENSE = PREFIX + "village.defense";
    public static final String VILLAGE_WALL_OUTER = PREFIX + "village.wall.outer";
    public static final String VILLAGE_WALL_INNER = PREFIX + "village.wall.inner";
    public static final String VILLAGE_WALL_RAID = PREFIX + "village.wall.raid";
    public static final String VILLAGE_WALL_NONE = PREFIX + "village.wall.none";

    public static final String VILLAGE_CONTROL = PREFIX + "village.control";
    public static final String VILLAGE_CONTROL_PLAYER = PREFIX + "village.control.player";
    public static final String VILLAGE_CONTROL_AUTO = PREFIX + "village.control.auto";
    // endregion

    // region 村民 (Villager) 相关本地化键定义与回退逻辑封装
    public static final String VILLAGER_BADGE = PREFIX + "villager.badge";
    public static final String VILLAGER_UNKNOWN = PREFIX + "villager.unknown";
    public static final String VILLAGER_CULTURE = PREFIX + "villager.culture";
    public static final String VILLAGER_ID = PREFIX + "villager.id";
    public static final String VILLAGER_TAGS = PREFIX + "villager.tags";
    public static final String VILLAGER_LABEL_BELONGS = PREFIX + "villager.label.belongs";
    public static final String VILLAGER_UNKNOWN_CULTURE = PREFIX + "villager.unknown_culture";

    public static final String CATEGORY_VILLAGER_DETAILS = PREFIX_JEIM + "category.villager_details";
    public static final String CATEGORY_VILLAGER_VILLAGES_FULL = PREFIX_JEIM + "category.villager_villages";
    public static final String CATEGORY_VILLAGER_BUILDINGS_FULL = PREFIX_JEIM + "category.villager_buildings";
    public static final String CATEGORY_VILLAGER_BRING_BACK_HOME = PREFIX_JEIM + "category.villager_bring_back_home";
    public static final String CATEGORY_VILLAGER_COLLECT_GOODS = PREFIX_JEIM + "category.villager_collect_goods";
    public static final String CATEGORY_VILLAGER_REQUIRED_GOODS = PREFIX_JEIM + "category.villager_required_goods";

    public static final String VILLAGER_BOX_VILLAGES = PREFIX + "villager.box.villages";
    public static final String VILLAGER_BOX_BUILDINGS = PREFIX + "villager.box.buildings";
    public static final String VILLAGER_BOX_BRING_BACK_HOME = PREFIX + "villager.box.bring_back_home";
    public static final String VILLAGER_BOX_COLLECT = PREFIX + "villager.box.collect";
    public static final String VILLAGER_BOX_REQUIRED = PREFIX + "villager.box.required";

    public static final String VILLAGER_LABEL_GENDER_STAGE = PREFIX + "villager.label.gender_stage";
    public static final String VILLAGER_LABEL_MAX_HEALTH = PREFIX + "villager.label.max_health";
    public static final String VILLAGER_LABEL_BASE_ATTACK = PREFIX + "villager.label.base_attack";
    public static final String VILLAGER_LABEL_HIRING_COST = PREFIX + "villager.label.hiring_cost";
    public static final String VILLAGER_LABEL_SPAWN_WEIGHT = PREFIX + "villager.label.spawn_weight";
    public static final String VILLAGER_LABEL_FLAGS = PREFIX + "villager.label.flags";

    public static final String VILLAGER_VALUE_CHILD = PREFIX + "villager.value.child";
    public static final String VILLAGER_VALUE_ADULT = PREFIX + "villager.value.adult";
    public static final String VILLAGER_VALUE_UNKNOWN = PREFIX + "villager.value.unknown";
    public static final String VILLAGER_VALUE_UNHIRABLE = PREFIX + "villager.value.unhirable";
    public static final String VILLAGER_VALUE_HIRABLE_COST = PREFIX + "villager.value.hirable_cost";
    public static final String VILLAGER_VALUE_FLAGS_DEFAULT = PREFIX + "villager.value.flags_default";

    public static final String VILLAGER_FLAG_RAIDER = PREFIX + "villager.flag.raider";
    public static final String VILLAGER_FLAG_HOSTILE = PREFIX + "villager.flag.hostile";
    public static final String VILLAGER_FLAG_HELP_ATTACKS = PREFIX + "villager.flag.help_attacks";
    public static final String VILLAGER_FLAG_ARCHER = PREFIX + "villager.flag.archer";
    public static final String VILLAGER_FLAG_NO_TELEPORT = PREFIX + "villager.flag.no_teleport";
    public static final String VILLAGER_FLAG_NO_RESURRECT = PREFIX + "villager.flag.no_resurrect";
    // endregion

    // =========================================================================
    // 字符串/Component 工厂方法
    // =========================================================================

    public static Component translatable(String key, String fallback, Object... args) {
        return Component.translatableWithFallback(key, fallback, args);
    }

    public static Component getVillageBadge() {
        return translatable(VILLAGE_BADGE, "VILLAGE");
    }

    public static Component getVillageUnknown() {
        return translatable(VILLAGE_UNKNOWN, "Unknown Village");
    }

    public static Component getVillageCulture(Object culturePath) {
        return translatable(VILLAGE_CULTURE, "Culture: %s", culturePath);
    }

    public static Component getVillageId(Object villageId) {
        return translatable(VILLAGE_ID, "ID: %s", villageId);
    }

    public static Component getVillageTags() {
        return translatable(VILLAGE_TAGS, "Tags:");
    }

    public static Component getVillageLabelBelongs(Object villageName) {
        return translatable(VILLAGE_LABEL_BELONGS, "§1所属村庄: §f%s", villageName);
    }

    public static Component getCategoryVillageBuildingsFullTitle() {
        return translatable(CATEGORY_VILLAGE_BUILDINGS_FULL, "村庄包含建筑 (全景大窗口)");
    }

    public static Component getCategoryVillageVillagersFullTitle() {
        return translatable(CATEGORY_VILLAGE_VILLAGERS_FULL, "村庄居住村民 (全景大窗口)");
    }

    public static Component getCategoryVillageDetailsTitle() {
        return translatable(CATEGORY_VILLAGE_DETAILS, "村庄详情");
    }

    public static Component getVillageBoxBuildings() {
        return translatable(VILLAGE_BOX_BUILDINGS, "村庄包含建筑");
    }

    public static Component getVillageBoxVillagers() {
        return translatable(VILLAGE_BOX_VILLAGERS, "村庄出现村民");
    }

    // Villager 工厂方法
    public static Component getVillagerBadge() {
        return translatable(VILLAGER_BADGE, "民");
    }

    public static Component getVillagerUnknown() {
        return translatable(VILLAGER_UNKNOWN, "未知村民");
    }

    public static Component getVillagerCulture(Object culturePath) {
        return translatable(VILLAGER_CULTURE, "所属文化: %s", culturePath);
    }

    public static Component getVillagerId(Object villagerId) {
        return translatable(VILLAGER_ID, "ID: %s", villagerId);
    }

    public static Component getVillagerTags() {
        return translatable(VILLAGER_TAGS, "关联标签 ($ Tag):");
    }

    public static Component getVillagerLabelBelongs(Object villagerName) {
        return translatable(VILLAGER_LABEL_BELONGS, "§1所属村民: §f%s", villagerName);
    }

    public static Component getCategoryVillagerDetailsTitle() {
        return translatable(CATEGORY_VILLAGER_DETAILS, "村民详情");
    }

    public static Component getCategoryVillagerVillagesFullTitle() {
        return translatable(CATEGORY_VILLAGER_VILLAGES_FULL, "村民生成/居住村庄");
    }

    public static Component getCategoryVillagerBuildingsFullTitle() {
        return translatable(CATEGORY_VILLAGER_BUILDINGS_FULL, "村民居住与工作建筑");
    }

    public static Component getCategoryVillagerBringBackHomeTitle() {
        return translatable(CATEGORY_VILLAGER_BRING_BACK_HOME, "带回建筑物资");
    }

    public static Component getCategoryVillagerCollectGoodsTitle() {
        return translatable(CATEGORY_VILLAGER_COLLECT_GOODS, "上交中心白名单");
    }

    public static Component getCategoryVillagerRequiredGoodsTitle() {
        return translatable(CATEGORY_VILLAGER_REQUIRED_GOODS, "职业专属需求");
    }

    public static Component getVillagerBoxVillages() {
        return translatable(VILLAGER_BOX_VILLAGES, "居住村庄");
    }

    public static Component getVillagerBoxBuildings() {
        return translatable(VILLAGER_BOX_BUILDINGS, "工作与居住建筑");
    }

    public static Component getVillagerBoxBringBackHome() {
        return translatable(VILLAGER_BOX_BRING_BACK_HOME, "带回建筑物资");
    }

    public static Component getVillagerBoxCollect() {
        return translatable(VILLAGER_BOX_COLLECT, "上交中心白名单");
    }

    public static Component getVillagerBoxRequired() {
        return translatable(VILLAGER_BOX_REQUIRED, "职业专属需求");
    }
}