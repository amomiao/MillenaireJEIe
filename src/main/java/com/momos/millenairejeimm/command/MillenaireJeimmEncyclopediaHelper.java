package com.momos.millenairejeimm.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.helper.SafeReflectionHelper;
import com.momos.millenairejeim.jei.crafting.MillCraftingRecipeMaker;
import com.momos.millenairejeim.jei.crafting.type.base.IMillCraftingRecipe;
import com.momos.millenairejeim.util.MMLog;
import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.millenaire.building.BlockCostRegistry;
import org.millenaire.building.BuildingCostCalculator;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;
import org.millenaire.goal.GoalRegistry;
import org.millenaire.goal.VillagerGoal;
import org.millenaire.goal.gathering.GatheringGoal;
import org.millenaire.goal.gathering.GatheringType;
import org.millenaire.goal.impl.SpotGatheringGoal;
import org.millenaire.item.ItemHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 百科导出命令扩展助手（负责 Java 硬编码机制、隐式掉落/商队/初始箱库存、建材折算及替代规则审计导出）。
 */
final class MillenaireJeimmEncyclopediaHelper {

    private static final String MAPPING_MOD_ID = "millenairecostombuildingblockmapping";
    private static final String MAPPING_CLASS = "com.momos.millenairecostombuildingblockmapping.mapping.BlockSubstitutionManager";

    private record SpotMarkerMeta(String marker, String note, String fallbackOutputId) {}

    private static final Map<String, SpotMarkerMeta> KNOWN_SPOT_GOALS = Map.of(
            "millenaire:gather_silk", new SpotMarkerMeta(
                    "block=millenaire:silk_worm[age=3]",
                    "Harvest mature silkworm blocks in buildingTag=silkwormfarm",
                    "millenaire:silk"),
            "millenaire:gather_snails", new SpotMarkerMeta(
                    "block=millenaire:snail_soil[age=3]",
                    "Harvest mature snail_soil blocks in buildingTag=snailsfarm",
                    "minecraft:purple_dye"),
            "millenaire:gather_brick", new SpotMarkerMeta(
                    "block=millenaire:mud_brick",
                    "Collect dried mud_brick blocks in buildingTag=brickkiln",
                    "millenaire:mud_brick"),
            "millenaire:dry_brick", new SpotMarkerMeta(
                    "block=millenaire:wet_brick",
                    "Place wet_brick blocks for solar drying into mud_brick in buildingTag=brickkiln",
                    ""),
            "millenaire:harvest_sugar_cane", new SpotMarkerMeta(
                    "soil=sugarcane + block=minecraft:sugar_cane",
                    "Harvest grown sugar cane columns in buildingTag=sugarplantation",
                    "minecraft:sugar_cane"),
            "millenaire:plant_sugar_cane", new SpotMarkerMeta(
                    "soil=sugarcane",
                    "Plant minecraft:sugar_cane above sugarcane soil in buildingTag=sugarplantation",
                    "")
    );

    private MillenaireJeimmEncyclopediaHelper() {}

    static void appendHardcodedSpotGoals(List<String> lines) {
        lines.add("# ----------------- HARDCODED SPOT GATHERING GOALS -----------------");
        GoalRegistry goalRegistry = MillenaireAPIHelper.getGoalRegistry();
        if (goalRegistry == null || goalRegistry.getAllIds() == null) {
            lines.add("[SPOT_GOAL:NONE|Unavailable] Reason: GoalRegistry not initialized");
            lines.add("");
            return;
        }

        Collection<BuildingPlanSet> allBuildings = MillenaireAPIHelper.getAllBuildings();
        Collection<VillagerType> allVillagers = MillenaireAPIHelper.getAllVillagers();

        List<ResourceLocation> sortedIds = goalRegistry.getAllIds().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();

        int exported = 0;
        for (ResourceLocation goalId : sortedIds) {
            VillagerGoal goal = goalRegistry.get(goalId);
            if (!(goal instanceof SpotGatheringGoal spotGoal)) continue;

            String idStr = goalId.toString();
            String locName = MillenaireLocalizeHelper.getGoalName(goalId.getPath()).getString();
            String buildingTag = readStringField(spotGoal, "buildingTag");
            Object stockSourceObj = readField(spotGoal, "stockSource");
            String stockSource = stockSourceObj != null ? stockSourceObj.toString() : "UNKNOWN";
            int townhallLimit = readIntField(spotGoal, "townhallLimit");
            Item stockItem = readSupplierItem(spotGoal, "stockItem");

            SpotMarkerMeta meta = KNOWN_SPOT_GOALS.get(idStr);
            if ((stockItem == null || stockItem == Items.AIR) && meta != null && !meta.fallbackOutputId().isEmpty()) {
                stockItem = ItemHelper.resolve(meta.fallbackOutputId());
            }

            String outputStr = (stockItem != null && stockItem != Items.AIR)
                    ? MillenaireJeimmCommands.formatItemCost(BuiltInRegistries.ITEM.getKey(stockItem), 1)
                    : "None (block placement / drying)";

            String markerStr = meta != null ? meta.marker() : ("tag:" + buildingTag);
            String noteStr = meta != null ? meta.note() : "Hardcoded Java SpotGatheringGoal";

            String matchingBuildings = findBuildingsByTag(allBuildings, buildingTag);
            String performingVillagers = findVillagersByGoal(allVillagers, goalId);

            lines.add(MillenaireJeimmCommands.clean(String.format(
                    "[SPOT_GOAL:%s|%s] BuildingTag: %s | Marker: %s | StockSource: %s(limit:%d) | Outputs: %s | MatchingBuildings: %s | PerformingVillagers: %s | Behavior: %s",
                    idStr, locName, buildingTag, markerStr, stockSource, townhallLimit, outputStr,
                    matchingBuildings, performingVillagers, noteStr
            )));
            exported++;
        }

        if (exported == 0) {
            lines.add("[SPOT_GOAL:NONE|Empty] No SpotGatheringGoal registered");
        }
        lines.add("");
    }

    static void appendImplicitLootMerchantsAndRestock(List<String> lines) {
        lines.add("# ----------------- IMPLICIT LOOT, MERCHANTS & RESTOCK -----------------");

        GoalRegistry goalRegistry = MillenaireAPIHelper.getGoalRegistry();
        Collection<BuildingPlanSet> allBuildings = MillenaireAPIHelper.getAllBuildings();
        Collection<VillagerType> allVillagers = MillenaireAPIHelper.getAllVillagers();

        if (goalRegistry != null && goalRegistry.getGatheringGoals() != null) {
            List<GatheringGoal> sortedGoals = goalRegistry.getGatheringGoals().stream()
                    .filter(g -> g != null && g.getGatheringType() != null && g.getGatheringType().id() != null)
                    .sorted(Comparator.comparing(g -> g.getGatheringType().id().toString()))
                    .toList();

            for (GatheringGoal gg : sortedGoals) {
                GatheringType type = gg.getGatheringType();
                String handler = type.handlerId() != null ? type.handlerId().toLowerCase(Locale.ROOT) : "";
                if ("slaughter".equals(handler)) {
                    exportSlaughterGoal(lines, type, allBuildings, allVillagers);
                } else if ("fishing_inuit".equals(handler)) {
                    exportFishingInuitGoal(lines, type, allVillagers);
                }
            }
        }

        exportInuitHuntingDrops(lines);
        exportForeignMerchantStocks(lines, allVillagers);
        exportGoodsRestockInventories(lines, allBuildings);

        lines.add("");
    }

    static void appendCostRulesAndSubstitutions(List<String> lines) {
        lines.add("# ----------------- COST RULES & ACTIVE SUBSTITUTIONS -----------------");

        exportBuildingCostCalculatorRules(lines);
        exportBlockCostRegistryEntries(lines);
        exportBlockSubstitutionsAndConfigAudit(lines);

        lines.add("");
    }

    // region Section 5 helpers
    private static void exportSlaughterGoal(
            List<String> lines,
            GatheringType type,
            Collection<BuildingPlanSet> allBuildings,
            Collection<VillagerType> allVillagers
    ) {
        ResourceLocation goalId = type.id();
        String locName = MillenaireLocalizeHelper.getGoalName(goalId.getPath()).getString();
        JsonObject params = type.handlerParams();
        String animalType = params != null ? GsonHelper.getAsString(params, "animalType", "unknown") : "unknown";
        String buildingTag = params != null ? GsonHelper.getAsString(params, "buildingTag", "") : "";

        List<String> bonusItems = new ArrayList<>();
        if (params != null && params.has("bonusItems") && params.get("bonusItems").isJsonArray()) {
            for (JsonElement elem : params.getAsJsonArray("bonusItems")) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                String itemId = GsonHelper.getAsString(obj, "item", "");
                int chance = GsonHelper.getAsInt(obj, "chance", 100);
                Item resolved = ItemHelper.resolve(itemId);
                String name = (resolved != null && resolved != Items.AIR)
                        ? resolved.getDescription().getString()
                        : itemId;
                bonusItems.add(itemId + "(" + name + ")@" + chance + "%");
            }
        }

        String vanillaDrops = getExpectedEntityDeathDrops(animalType);
        String matchingBuildings = findBuildingsByTag(allBuildings, buildingTag);
        String performingVillagers = findVillagersByGoal(allVillagers, goalId);

        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[IMPLICIT_LOOT:%s|%s] Handler: slaughter | Animal: %s | BuildingTag: %s | BonusItems: %s | PickupNearbyItems(3x2x3): %s | MatchingBuildings: %s | PerformingVillagers: %s",
                goalId, locName, animalType, buildingTag.isEmpty() ? "None" : buildingTag,
                bonusItems.isEmpty() ? "None" : String.join(", ", bonusItems),
                vanillaDrops, matchingBuildings, performingVillagers
        )));
    }

    private static void exportFishingInuitGoal(
            List<String> lines,
            GatheringType type,
            Collection<VillagerType> allVillagers
    ) {
        ResourceLocation goalId = type.id();
        String locName = MillenaireLocalizeHelper.getGoalName(goalId.getPath()).getString();
        JsonObject params = type.handlerParams();

        List<String> lootList = new ArrayList<>();
        if (params != null && params.has("loot") && params.get("loot").isJsonArray()) {
            for (JsonElement elem : params.getAsJsonArray("loot")) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                String itemId = GsonHelper.getAsString(obj, "item", "");
                int count = GsonHelper.getAsInt(obj, "count", 1);
                ResourceLocation rl = ResourceLocation.tryParse(itemId);
                if (rl != null) lootList.add(MillenaireJeimmCommands.formatItemCost(rl, count));
            }
        }

        String boneBlockStr = MillenaireJeimmCommands.formatItemCost(BuiltInRegistries.ITEM.getKey(Items.BONE_BLOCK), 1);
        String codStr = MillenaireJeimmCommands.formatItemCost(BuiltInRegistries.ITEM.getKey(Items.COD), 1);
        String performingVillagers = findVillagersByGoal(allVillagers, goalId);

        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[IMPLICIT_LOOT:%s|%s] Handler: fishing_inuit | ConfiguredLoot: %s | JavaHardcodedFallback: %s | JavaHardcodedBonus(25%%): %s | PerformingVillagers: %s",
                goalId, locName, lootList.isEmpty() ? "None" : String.join(", ", lootList),
                codStr, boneBlockStr, performingVillagers
        )));
    }

    private static void exportInuitHuntingDrops(List<String> lines) {
        String seafoodId = "millenaire:seafood_raw";
        String wolfMeatId = "millenaire:wolfmeat_raw";
        String bearMeatId = "millenaire:bearmeat_raw";

        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[JAVA_DROP:millenaire:inuit_hunting_squid|Inuit Hunting: Squid] Source: InuitHuntingDropHandler | Entity: minecraft:squid | Output: %s | Chance: 10%% (1x) | Condition: ServerPlayer kill + learned(seafood_raw)",
                formatItemIdWithLoc(seafoodId)
        )));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[JAVA_DROP:millenaire:inuit_hunting_guardian|Inuit Hunting: Guardian] Source: InuitHuntingDropHandler | Entity: minecraft:guardian / minecraft:elder_guardian | Output: %s | Quantity: 2-3x (Guardian), 5-9x (Elder Guardian) | Condition: ServerPlayer kill + learned(seafood_raw)",
                formatItemIdWithLoc(seafoodId)
        )));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[JAVA_DROP:millenaire:inuit_hunting_wolf|Inuit Hunting: Wolf] Source: InuitHuntingDropHandler | Entity: minecraft:wolf | Output: %s | Quantity: 0-2x | Condition: ServerPlayer kill + learned(wolfmeat_raw)",
                formatItemIdWithLoc(wolfMeatId)
        )));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[JAVA_DROP:millenaire:inuit_hunting_polar_bear|Inuit Hunting: Polar Bear] Source: InuitHuntingDropHandler | Entity: minecraft:polar_bear | Output: %s | Quantity: 1-2x | Condition: LivingDropsEvent (any death)",
                formatItemIdWithLoc(bearMeatId)
        )));
    }

    private static void exportForeignMerchantStocks(List<String> lines, Collection<VillagerType> allVillagers) {
        if (allVillagers == null) return;
        List<VillagerType> merchants = allVillagers.stream()
                .filter(v -> v != null && v.foreignMerchantStock() != null && !v.foreignMerchantStock().isEmpty())
                .sorted(Comparator.comparing(v -> v.id() != null ? v.id().toString() : ""))
                .toList();

        for (VillagerType merchant : merchants) {
            String mId = merchant.id() != null ? merchant.id().toString() : "unknown";
            String mName = MillenaireLocalizeHelper.getVillagerName(merchant).getString();
            String culture = merchant.culture() != null
                    ? merchant.culture() + "(" + MillenaireLocalizeHelper.getCultureName(merchant.culture()).getString() + ")"
                    : "unknown";

            String stockStr = merchant.foreignMerchantStock().entrySet().stream()
                    .filter(e -> e.getKey() != null)
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                    .map(e -> MillenaireJeimmCommands.formatItemCost(e.getKey(), e.getValue() != null ? e.getValue() : 1))
                    .collect(Collectors.joining(", "));

            List<VillageType> villages = MillenaireAPIHelper.getVillageFormVillager(merchant);
            String villageStr = (villages == null || villages.isEmpty())
                    ? "Wandering / None"
                    : villages.stream()
                            .map(v -> v.id() + "(" + (v.name() != null ? v.name() : v.id().getPath()) + ")")
                            .collect(Collectors.joining(", "));

            lines.add(MillenaireJeimmCommands.clean(String.format(
                    "[MERCHANT_STOCK:%s|%s] Culture: %s | Villages: %s | Stock: %s",
                    mId, mName, culture, villageStr, stockStr
            )));
        }
    }

    private static void exportGoodsRestockInventories(List<String> lines, Collection<BuildingPlanSet> allBuildings) {
        if (allBuildings == null) return;
        List<BuildingPlanSet> buildingsWithRestock = allBuildings.stream()
                .filter(b -> b != null && b.startingGoods() != null && !b.startingGoods().isEmpty())
                .sorted(Comparator.comparing(b -> b.id() != null ? b.id().toString() : b.buildingId()))
                .toList();

        for (BuildingPlanSet building : buildingsWithRestock) {
            String bId = building.id() != null ? building.id().toString() : (building.culture() + ":" + building.buildingId());
            String bName = MillenaireLocalizeHelper.getBuildingName(building).getString();
            String culture = building.culture() != null
                    ? building.culture() + "(" + MillenaireLocalizeHelper.getCultureName(building.culture()).getString() + ")"
                    : "common";
            List<VillageType> villages = MillenaireAPIHelper.getVillageFormBuilding(building);
            boolean isLone = "lone".equalsIgnoreCase(building.category())
                    || (villages != null && villages.stream().anyMatch(VillageType::loneBuilding));

            String goodsStr = building.startingGoods().stream()
                    .filter(Objects::nonNull)
                    .map(sg -> {
                        Item resolved = ItemHelper.resolve(sg.item());
                        String label;
                        if (resolved != null && resolved != Items.AIR) {
                            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(resolved);
                            label = rl + "(" + resolved.getDescription().getString() + ")";
                        } else {
                            label = sg.item() + "(UNRESOLVED)";
                        }
                        return String.format("%s[prob=%.2f,fixed=%d,rand=%d]",
                                label, sg.probability(), sg.fixedNumber(), sg.randomNumber());
                    })
                    .collect(Collectors.joining(", "));

            lines.add(MillenaireJeimmCommands.clean(String.format(
                    "[RESTOCK_INVENTORY:%s|%s] Source: GoodsRestockHelper | Culture: %s | Category: %s | LoneBuilding: %b | StartingGoods: %s",
                    bId, bName, culture, building.category(), isLone, goodsStr
            )));
        }
    }
    // endregion

    // region Section 6 helpers
    @SuppressWarnings("unchecked")
    private static void exportBuildingCostCalculatorRules(List<String> lines) {
        Set<String> freeBlocks = readStaticField(BuildingCostCalculator.class, "FREE_BLOCKS", Set.class);
        Set<String> freeMillBlocks = readStaticField(BuildingCostCalculator.class, "FREE_MILLENAIRE_BLOCKS", Set.class);
        Set<String> freeMockBlocks = readStaticField(BuildingCostCalculator.class, "FREE_MOCK_BLOCKS", Set.class);
        Map<String, String> costAliases = readStaticField(BuildingCostCalculator.class, "COST_ALIASES", Map.class);
        Map<String, String> variantOverrides = readStaticField(BuildingCostCalculator.class, "VARIANT_OVERRIDES", Map.class);
        Set<String> notVariants = readStaticField(BuildingCostCalculator.class, "NOT_VARIANTS", Set.class);
        Set<String> tileSlabHalfCost = readStaticField(BuildingCostCalculator.class, "TILE_SLAB_HALF_COST", Set.class);
        Map<String, String> woodTypes = readStaticField(BuildingCostCalculator.class, "WOOD_TYPES", Map.class);
        Map<String, String> woodLogItems = readStaticField(BuildingCostCalculator.class, "WOOD_LOG_ITEMS", Map.class);
        Map<String, Integer> planksPerLog = readStaticField(BuildingCostCalculator.class, "PLANKS_PER_LOG", Map.class);
        String[] variantSuffixes = readStaticField(BuildingCostCalculator.class, "VARIANT_SUFFIXES", String[].class);

        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[COST_RULE:FREE_BLOCKS|Vanilla Free Blocks] Count: %d | Blocks: %s",
                freeBlocks != null ? freeBlocks.size() : 0,
                joinSorted(freeBlocks)
        )));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[COST_RULE:FREE_MILLENAIRE_BLOCKS|Millenaire Free Blocks] Count: %d | Blocks: %s",
                freeMillBlocks != null ? freeMillBlocks.size() : 0,
                joinSorted(freeMillBlocks)
        )));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[COST_RULE:FREE_MOCK_BLOCKS|Mock Marker Free Blocks] Count: %d | Explicit: %s | Fallback: All unrecognized millenaire:mock_* are free; Exceptions: millenaire:mock_chest=8x anywood_planks, millenaire:mock_marker(type=torch->oak_planks/4, type=furnace->8x cobblestone)",
                freeMockBlocks != null ? freeMockBlocks.size() : 0,
                joinSorted(freeMockBlocks)
        )));
        lines.add(MillenaireJeimmCommands.clean(
                "[COST_RULE:BRICK_COLOR_COLLAPSE|Painted/Decorated Brick Folding] Rules: millenaire:painted_brick_* (16 colors) -> 1x millenaire:painted_brick_white; millenaire:decorated_brick_* (16 colors) -> 1x millenaire:decorated_brick_white"
        ));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[COST_RULE:COST_ALIASES|Direct Cost Aliases] Aliases: %s | TileSlabHalfCost(prefix->fullBlock): %s",
                formatSortedMap(costAliases),
                joinSorted(tileSlabHalfCost)
        )));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[COST_RULE:VARIANT_COLLAPSE|Variant Collapse Rules] Suffixes: %s | Overrides: %s | NotVariants: %s | BuiltInStoneFolding: mossy_stone_brick*->mossy_stone_bricks, mossy_cobblestone*->mossy_cobblestone, stone*/smooth_stone*->stone, cobblestone*->cobblestone, stone_brick*->stone_bricks | MiscFolding: glass_pane->glass(ceil(n*6/16)), *_bed->3 white_wool + 3 anywood_planks, furnace->8 cobblestone, oak_beam->4 oak_planks, wooden_bars_dark->4 spruce_planks, stairs_steep_stonebrick->stone",
                variantSuffixes != null ? String.join(", ", variantSuffixes) : "None",
                formatSortedMap(variantOverrides),
                joinSorted(notVariants)
        )));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[COST_RULE:WOOD_COLLAPSE|Wood & Anywood Folding] PlanksToLogs(postProcess): millenaire:anywood_planks -> millenaire:anywood_log (ceil/4), <wood>_planks -> log | WoodTypes: %s | SpecialLogs: %s | PlanksPerLog: %s",
                formatSortedMap(woodTypes),
                formatSortedMap(woodLogItems),
                formatSortedMap(planksPerLog)
        )));
    }

    @SuppressWarnings("unchecked")
    private static void exportBlockCostRegistryEntries(List<String> lines) {
        Map<String, Object> costs = readStaticField(BlockCostRegistry.class, "COSTS", Map.class);
        if (costs == null || costs.isEmpty()) {
            lines.add("[COST_RULE:BLOCK_COST_REGISTRY|Custom BlockCostRegistry] Count: 0 | Entries: None");
            return;
        }
        String entries = costs.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Object val = e.getValue();
                    if (val instanceof BlockCostRegistry.BlockCost bc) {
                        return e.getKey() + " -> " + bc.quantity() + "x " + bc.costItem();
                    }
                    return e.getKey() + " -> " + val;
                })
                .collect(Collectors.joining(", "));
        lines.add(MillenaireJeimmCommands.clean(String.format(
                "[COST_RULE:BLOCK_COST_REGISTRY|Custom BlockCostRegistry] Count: %d | Entries: %s",
                costs.size(), entries
        )));
    }

    @SuppressWarnings("unchecked")
    private static void exportBlockSubstitutionsAndConfigAudit(List<String> lines) {
        boolean loaded = ModList.get().isLoaded(MAPPING_MOD_ID);
        Class<?> managerClass = null;
        if (loaded) {
            managerClass = SafeReflectionHelper.findClass(MAPPING_CLASS);
        }

        if (!loaded || managerClass == null) {
            lines.add(MillenaireJeimmCommands.clean(String.format(
                    "[SUBSTITUTION_STATUS:%s|BlockSubstitutionManager] Loaded: false | LiveGlobalRules: 0 | LiveCultureRules: 0",
                    MAPPING_MOD_ID
            )));
        } else {
            CalculatorProbe probe = new CalculatorProbe();
            Map<Item, ?> globalMap = readStaticField(managerClass, "GLOBAL_ITEM_SUBSTITUTION_MAP", Map.class);
            Map<String, Map<Item, ?>> cultureMaps = readStaticField(managerClass, "CULTURE_ITEM_SUBSTITUTION_MAPS", Map.class);

            lines.add(MillenaireJeimmCommands.clean(String.format(
                    "[SUBSTITUTION_STATUS:%s|BlockSubstitutionManager] Loaded: true | LiveGlobalRules: %d | LiveCultureScopes: %d",
                    MAPPING_MOD_ID,
                    globalMap != null ? globalMap.size() : 0,
                    cultureMaps != null ? cultureMaps.size() : 0
            )));

            if (globalMap != null) {
                globalMap.entrySet().stream()
                        .filter(e -> e.getKey() != null)
                        .sorted(Comparator.comparing(e -> BuiltInRegistries.ITEM.getKey(e.getKey()).toString()))
                        .forEach(e -> lines.add(formatLiveSubstitutionLine("GLOBAL", e.getKey(), e.getValue(), probe)));
            }

            if (cultureMaps != null) {
                cultureMaps.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(scopeEntry -> {
                            String culture = scopeEntry.getKey();
                            Map<Item, ?> map = scopeEntry.getValue();
                            if (map == null) return;
                            map.entrySet().stream()
                                    .filter(e -> e.getKey() != null)
                                    .sorted(Comparator.comparing(e -> BuiltInRegistries.ITEM.getKey(e.getKey()).toString()))
                                    .forEach(e -> lines.add(formatLiveSubstitutionLine(culture, e.getKey(), e.getValue(), probe)));
                        });
            }
        }

        auditSubstitutionConfigDirectory(lines);
    }

    private static String formatLiveSubstitutionLine(String scope, Item targetItem, Object resultObj, CalculatorProbe probe) {
        ResourceLocation targetId = BuiltInRegistries.ITEM.getKey(targetItem);
        String targetName = targetItem.getDescription().getString();
        Item baseItem = readResultBaseItem(resultObj);
        int cost = readResultCost(resultObj);
        String baseStr = (baseItem != null && baseItem != Items.AIR)
                ? MillenaireJeimmCommands.formatItemCost(BuiltInRegistries.ITEM.getKey(baseItem), cost)
                : "UNKNOWN(x" + cost + ")";

        String status = probe.diagnoseTarget(targetId);
        String prefix = "GLOBAL".equals(scope) ? "[SUBSTITUTION_GLOBAL:" : "[SUBSTITUTION_CULTURE:" + scope + ":";
        return MillenaireJeimmCommands.clean(String.format(
                "%s%s|%s] BaseSubstitute: %s | CalculatorStatus: %s",
                prefix, targetId, targetName, baseStr, status
        ));
    }

    private static void auditSubstitutionConfigDirectory(List<String> lines) {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(MAPPING_MOD_ID);
        if (!Files.isDirectory(configDir)) {
            return;
        }
        CalculatorProbe probe = new CalculatorProbe();
        try (Stream<Path> walk = Files.walk(configDir)) {
            List<Path> jsonFiles = walk
                    .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .sorted()
                    .toList();

            for (Path jsonPath : jsonFiles) {
                String fileName = jsonPath.getFileName().toString();
                String scope = "global.json".equalsIgnoreCase(fileName)
                        ? "GLOBAL"
                        : fileName.substring(0, fileName.length() - 5);
                auditSingleConfigFile(lines, configDir.relativize(jsonPath).toString(), scope, jsonPath, probe);
            }
        } catch (Exception e) {
            lines.add(MillenaireJeimmCommands.clean(
                    "[SUBSTITUTION_CONFIG_AUDIT:ERROR|Config Directory Scan Failed] Error: " + e.getMessage()
            ));
        }
    }

    private static void auditSingleConfigFile(
            List<String> lines,
            String relFile,
            String scope,
            Path jsonPath,
            CalculatorProbe probe
    ) {
        try {
            String raw = Files.readString(jsonPath, StandardCharsets.UTF_8);
            JsonElement parsed = JsonParser.parseString(raw);
            if (!parsed.isJsonObject()) return;
            JsonObject root = parsed.getAsJsonObject();
            if (!root.has("substitutions") || !root.get("substitutions").isJsonArray()) return;

            for (JsonElement elem : root.getAsJsonArray("substitutions")) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                String baseId = GsonHelper.getAsString(obj, "base", "");
                if (baseId.isEmpty() || !obj.has("target")) continue;

                boolean baseValid = isRegisteredItem(baseId);
                JsonElement targetElem = obj.get("target");
                List<String> targetIds = new ArrayList<>();
                if (targetElem.isJsonArray()) {
                    JsonArray arr = targetElem.getAsJsonArray();
                    // Check for legacy 2-element [id, cost] tuple vs array of strings/tuples
                    if (arr.size() >= 2 && arr.get(0).isJsonPrimitive() && arr.get(1).isJsonPrimitive()
                            && arr.get(1).getAsJsonPrimitive().isNumber()) {
                        targetIds.add(arr.get(0).getAsString());
                    } else {
                        for (JsonElement t : arr) {
                            extractConfigTargetId(t, targetIds);
                        }
                    }
                } else {
                    extractConfigTargetId(targetElem, targetIds);
                }

                for (String targetId : targetIds) {
                    boolean targetValid = isRegisteredItem(targetId);
                    if (!baseValid || !targetValid) {
                        lines.add(MillenaireJeimmCommands.clean(String.format(
                                "[SUBSTITUTION_CONFIG_INVALID:%s|%s] Scope: %s | File: %s | Base: %s(valid=%b) | Target: %s(valid=%b) | Reason: Resolved to Items.AIR",
                                targetId, baseId, scope, relFile, baseId, baseValid, targetId, targetValid
                        )));
                        continue;
                    }
                    ResourceLocation targetRl = ResourceLocation.tryParse(targetId);
                    if (targetRl != null) {
                        String diag = probe.diagnoseTarget(targetRl);
                        if (!"ACTIVE".equals(diag)) {
                            lines.add(MillenaireJeimmCommands.clean(String.format(
                                    "[SUBSTITUTION_CONFIG_DEAD:%s|%s] Scope: %s | File: %s | Base: %s | Reason: %s",
                                    targetId, baseId, scope, relFile, baseId, diag
                            )));
                        }
                    }
                }
            }
        } catch (Exception e) {
            lines.add(MillenaireJeimmCommands.clean(
                    "[SUBSTITUTION_CONFIG_AUDIT:" + relFile + "|Parse Error] Error: " + e.getMessage()
            ));
        }
    }

    private static void extractConfigTargetId(JsonElement elem, List<String> out) {
        if (elem == null) return;
        if (elem.isJsonPrimitive()) {
            out.add(elem.getAsString());
        } else if (elem.isJsonArray() && elem.getAsJsonArray().size() >= 1 && elem.getAsJsonArray().get(0).isJsonPrimitive()) {
            out.add(elem.getAsJsonArray().get(0).getAsString());
        }
    }

    private static boolean isRegisteredItem(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return false;
        return BuiltInRegistries.ITEM.getOptional(rl).filter(i -> i != Items.AIR).isPresent();
    }

    /**
     * 通过反射调用 {@link BuildingCostCalculator} 内部原生的分类与后处理管线（绕过 Mixin 的返回值拦截），
     * 检测替代规则的 target 是否已被前序免费或折叠规则吞并。
     */
    private static final class CalculatorProbe {
        private Constructor<?> paletteCtor;
        private Method classifyMethod;
        private Method postProcessMethod;
        private boolean ready;

        CalculatorProbe() {
            Class<?> paletteClass = SafeReflectionHelper.findClass("org.millenaire.building.BuildingCostCalculator$PaletteEntry");
            if (paletteClass == null) {
                ready = false;
                return;
            }
            paletteCtor = SafeReflectionHelper.findConstructor(paletteClass, String.class, Map.class);
            classifyMethod = SafeReflectionHelper.findMethod(
                    BuildingCostCalculator.class, "classifyAndAccumulate", paletteClass, int.class, Map.class);
            postProcessMethod = SafeReflectionHelper.findMethod(BuildingCostCalculator.class, "postProcess", Map.class);
            ready = paletteCtor != null && classifyMethod != null && postProcessMethod != null;
        }

        String diagnoseTarget(ResourceLocation targetId) {
            if (!ready || targetId == null) return "ACTIVE(unprobed)";
            try {
                Object entry = SafeReflectionHelper.newInstance(paletteCtor, targetId.toString(), Map.of());
                if (entry == null) {
                    return "ACTIVE(probe_error)";
                }
                Map<ResourceLocation, Integer> costs = new LinkedHashMap<>();
                SafeReflectionHelper.invokeMethod(null, classifyMethod, entry, 1, costs);
                SafeReflectionHelper.invokeMethod(null, postProcessMethod, costs);
                if (costs.isEmpty()) {
                    return "DEAD_FREE_BLOCK";
                }
                if (costs.size() == 1 && costs.containsKey(targetId)) {
                    return "ACTIVE";
                }
                String folded = costs.entrySet().stream()
                        .map(e -> e.getValue() + "x " + e.getKey())
                        .collect(Collectors.joining(", "));
                return "DEAD_PREFOLDED_TO(" + folded + ")";
            } catch (Throwable t) {
                MMLog.warn("[JEImm百科诊断] 建筑方块成本诊断反射执行失败 ({}): {}",
                        "[JEImm Encyclopedia] Building cost probe failed ({}): {}",
                        "[Encyclopédie JEImm] Échec de la sonde de coût ({}) : {}",
                        targetId, t.getMessage());
                return "ACTIVE(probe_error)";
            }
        }
    }

    private record RecipeBindingRow(
            String primaryOutputKey,
            String category,
            String goalId,
            String handlerId,
            String line
    ) {}

    /**
     * 导出全部工艺/定点采集/外部配置目标的运行时绑定（含 tag 约束、解析建筑与执行村民，按主产物与 ID 确定性排序）。
     */
    static void appendRecipeAndGoalBindings(List<String> lines) {
        lines.add("# ----------------- RECIPE & GOAL RUNTIME BINDINGS -----------------");
        GoalRegistry goalRegistry = MillenaireAPIHelper.getGoalRegistry();
        List<RecipeBindingRow> rows = new ArrayList<>();

        if (goalRegistry != null) {
            for (IMillCraftingRecipe recipe : MillCraftingRecipeMaker.initStandardCraftingRecipes(goalRegistry)) {
                addRecipeBindingRow(rows, "crafting", recipe);
            }
            for (IMillCraftingRecipe recipe : MillCraftingRecipeMaker.getSpotGatheringRecipes(goalRegistry)) {
                addRecipeBindingRow(rows, "spot_gathering", recipe);
            }
        }
        for (IMillCraftingRecipe recipe : MillCraftingRecipeMaker.getCustomGoalRecipes(goalRegistry)) {
            addRecipeBindingRow(rows, "custom", recipe);
        }

        if (rows.isEmpty()) {
            lines.add(goalRegistry == null
                    ? "[RECIPE:NONE|Unavailable] Reason: GoalRegistry not initialized"
                    : "[RECIPE:NONE|Empty] Reason: No resolved crafting, spot_gathering, or custom recipes");
            lines.add("");
            return;
        }

        rows.sort(Comparator.comparing(RecipeBindingRow::primaryOutputKey)
                .thenComparing(RecipeBindingRow::category)
                .thenComparing(RecipeBindingRow::goalId)
                .thenComparing(RecipeBindingRow::handlerId));

        for (RecipeBindingRow row : rows) {
            lines.add(row.line());
        }
        lines.add("");
    }

    private static void addRecipeBindingRow(List<RecipeBindingRow> rows, String category, IMillCraftingRecipe recipe) {
        if (recipe == null) return;
        String goalId = recipe.getId() != null
                ? recipe.getId().toString()
                : ("millenaire:" + (recipe.getGoalKey() != null ? recipe.getGoalKey() : "unknown"));
        String handlerId = recipe.getHandlerId() != null && !recipe.getHandlerId().isBlank()
                ? recipe.getHandlerId()
                : "unknown";
        String buildingTag = recipe.getBuildingTag() != null && !recipe.getBuildingTag().isBlank()
                ? recipe.getBuildingTag()
                : "None";
        String requiredTag = recipe.getRequiredTag() != null && !recipe.getRequiredTag().isBlank()
                ? recipe.getRequiredTag()
                : "None";

        String inputsStr = formatRecipeInputs(recipe.getInputs());

        List<ItemStack> sortedOutputs = recipe.getOutputs() != null
                ? recipe.getOutputs().stream()
                        .filter(s -> s != null && !s.isEmpty())
                        .sorted(Comparator.comparing(s -> String.valueOf(BuiltInRegistries.ITEM.getKey(s.getItem()))))
                        .toList()
                : List.of();
        String primaryOutputKey = sortedOutputs.isEmpty()
                ? "~"
                : String.valueOf(BuiltInRegistries.ITEM.getKey(sortedOutputs.get(0).getItem()));
        String outputsStr = sortedOutputs.isEmpty()
                ? "None"
                : sortedOutputs.stream()
                        .map(s -> s.getCount() + "x " + BuiltInRegistries.ITEM.getKey(s.getItem())
                                + "(" + s.getItem().getDescription().getString() + ")")
                        .collect(Collectors.joining(", "));

        String resolvedBuildingsStr = recipe.getAssociatedBuildingIds() != null && !recipe.getAssociatedBuildingIds().isEmpty()
                ? recipe.getAssociatedBuildingIds().stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted(Comparator.comparing(b -> b.id() != null ? b.id().toString() : String.valueOf(b.buildingId())))
                        .map(b -> b.id() + "(" + MillenaireLocalizeHelper.getBuildingName(b).getString() + ")")
                        .collect(Collectors.joining(", "))
                : "None";
        if (resolvedBuildingsStr.isEmpty()) {
            resolvedBuildingsStr = "None";
        }

        String performingVillagersStr = recipe.getVillagerTypes() != null && !recipe.getVillagerTypes().isEmpty()
                ? recipe.getVillagerTypes().stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted(Comparator.comparing(v -> v.id() != null ? v.id().toString() : ""))
                        .map(v -> v.id() + "(" + MillenaireLocalizeHelper.getVillagerName(v).getString() + ")")
                        .collect(Collectors.joining(", "))
                : "None";
        if (performingVillagersStr.isEmpty()) {
            performingVillagersStr = "None";
        }

        String line = MillenaireJeimmCommands.clean(String.format(
                "[RECIPE:%s|%s] Category: %s | BuildingTag: %s | RequiredTag: %s | Inputs: %s | Outputs: %s | ResolvedBuildings: %s | PerformingVillagers: %s",
                goalId, handlerId, category, buildingTag, requiredTag,
                inputsStr, outputsStr, resolvedBuildingsStr, performingVillagersStr
        ));
        rows.add(new RecipeBindingRow(primaryOutputKey, category, goalId, handlerId, line));
    }

    private static String formatRecipeInputs(List<IMillCraftingRecipe.IngredientWithCount> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return "None";
        }
        List<String> formatted = new ArrayList<>();
        for (IMillCraftingRecipe.IngredientWithCount iwc : inputs) {
            if (iwc == null || iwc.ingredient() == null) continue;
            int count = Math.max(1, iwc.count());
            ItemStack[] matches;
            try {
                matches = iwc.ingredient().getItems();
            } catch (Throwable t) {
                matches = new ItemStack[0];
            }
            Map<String, String> optMap = new TreeMap<>();
            if (matches != null) {
                for (ItemStack s : matches) {
                    if (s != null && !s.isEmpty()) {
                        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(s.getItem());
                        String id = rl != null ? rl.toString() : "unknown";
                        optMap.putIfAbsent(id, id + "(" + s.getItem().getDescription().getString() + ")");
                    }
                }
            }
            if (optMap.isEmpty()) {
                formatted.add(count + "x empty");
            } else if (optMap.size() == 1) {
                formatted.add(count + "x " + optMap.values().iterator().next());
            } else {
                formatted.add(count + "x [" + String.join(", ", optMap.values()) + "]");
            }
        }
        if (formatted.isEmpty()) {
            return "None";
        }
        formatted.sort(String::compareTo);
        return String.join(", ", formatted);
    }
    // endregion

    // region Shared lookup and reflection helpers
    private static String findBuildingsByTag(Collection<BuildingPlanSet> allBuildings, String buildingTag) {
        if (allBuildings == null || buildingTag == null || buildingTag.isBlank()) return "None";
        String res = allBuildings.stream()
                .filter(b -> b != null && MillenaireAPIHelper.buildingHasTag(b, buildingTag))
                .sorted(Comparator.comparing(b -> b.id() != null ? b.id().toString() : b.buildingId()))
                .map(b -> b.id() + "(" + MillenaireLocalizeHelper.getBuildingName(b).getString() + ")")
                .collect(Collectors.joining(", "));
        return res.isEmpty() ? "None" : res;
    }

    private static String findVillagersByGoal(Collection<VillagerType> allVillagers, ResourceLocation goalId) {
        if (allVillagers == null || goalId == null) return "None";
        String res = allVillagers.stream()
                .filter(v -> v != null && v.goals() != null && v.goals().contains(goalId))
                .sorted(Comparator.comparing(v -> v.id() != null ? v.id().toString() : ""))
                .map(v -> v.id() + "(" + MillenaireLocalizeHelper.getVillagerName(v).getString() + ")")
                .collect(Collectors.joining(", "));
        return res.isEmpty() ? "None" : res;
    }

    private static String getExpectedEntityDeathDrops(String animalType) {
        return switch (animalType) {
            case "minecraft:cow" -> "minecraft:beef, minecraft:leather";
            case "minecraft:pig" -> "minecraft:porkchop";
            case "minecraft:sheep" -> "minecraft:mutton, minecraft:white_wool";
            case "minecraft:chicken" -> "minecraft:chicken, minecraft:feather";
            case "minecraft:squid" -> "minecraft:ink_sac";
            default -> "Vanilla loot of " + animalType;
        };
    }

    private static String formatItemIdWithLoc(String itemId) {
        Item resolved = ItemHelper.resolve(itemId);
        if (resolved != null && resolved != Items.AIR) {
            return BuiltInRegistries.ITEM.getKey(resolved) + "(" + resolved.getDescription().getString() + ")";
        }
        return itemId;
    }

    private static Object readField(Object obj, String name) {
        return SafeReflectionHelper.readField(obj, name);
    }

    private static String readStringField(Object obj, String name) {
        return SafeReflectionHelper.readStringField(obj, name);
    }

    private static int readIntField(Object obj, String name) {
        return SafeReflectionHelper.readIntField(obj, name, 0);
    }

    private static Item readSupplierItem(Object obj, String name) {
        return SafeReflectionHelper.readSupplierField(obj, name, Item.class);
    }

    private static <T> T readStaticField(Class<?> clazz, String name, Class<T> type) {
        return SafeReflectionHelper.readStaticField(clazz, name, type, null);
    }

    private static Item readResultBaseItem(Object resultObj) {
        if (resultObj == null) return null;
        if (hasNoArgMethod(resultObj.getClass(), "baseItem")) {
            return SafeReflectionHelper.invokeNoArgMethod(resultObj, "baseItem", Item.class, null);
        }
        return SafeReflectionHelper.readField(resultObj, "baseItem", Item.class, null);
    }

    private static int readResultCost(Object resultObj) {
        if (resultObj == null) return 1;
        if (hasNoArgMethod(resultObj.getClass(), "cost")) {
            Number num = SafeReflectionHelper.invokeNoArgMethod(resultObj, "cost", Number.class, null);
            if (num != null && num.intValue() > 0) {
                return num.intValue();
            }
            return 1;
        }
        int fVal = SafeReflectionHelper.readIntField(resultObj, "cost", 1);
        return fVal > 0 ? fVal : 1;
    }

    private static boolean hasNoArgMethod(Class<?> clazz, String methodName) {
        try {
            for (Method m : clazz.getDeclaredMethods()) {
                if (methodName.equals(m.getName()) && m.getParameterCount() == 0) {
                    return true;
                }
            }
        } catch (Throwable t) {
            MMLog.warn("[JEImm百科诊断] 扫描 {} 方法列表失败: {}",
                    "[JEImm Encyclopedia] Failed scanning methods on {}: {}",
                    "[Encyclopédie JEImm] Échec d'analyse des méthodes sur {} : {}",
                    clazz.getName(), t.getMessage());
        }
        return false;
    }

    private static String joinSorted(Collection<String> set) {
        if (set == null || set.isEmpty()) return "None";
        return set.stream().filter(Objects::nonNull).sorted().collect(Collectors.joining(", "));
    }

    private static String formatSortedMap(Map<?, ?> map) {
        if (map == null || map.isEmpty()) return "None";
        return map.entrySet().stream()
                .sorted(Comparator.comparing(e -> String.valueOf(e.getKey())))
                .map(e -> e.getKey() + " -> " + e.getValue())
                .collect(Collectors.joining(", "));
    }
    // endregion
}
