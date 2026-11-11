package com.momos.millenairejeimm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.helper.MillenaireLocalizeHelper;
import com.momos.millenairejeim.util.MMLog;
import com.momos.millenairejeimm.MillenaireJeimm;
import com.momos.millenairejeimm.jei.BuildingUpgradeCostHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingLevelCostCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面向 Agent 过滤排查的千年村庄百科数据导出客户端命令系统。
 * <p>
 * 提供 {@code /milljeimm export encyclopedia} 与 {@code /milljeimm dump} 命令；
 * 输出中英文双轨标签（ID + 本地化名）与行优先（Line-Oriented）数据格式，便于通过
 * {@code Select-String} / {@code grep} 快速检索建筑材料消耗、村庄组成与村民物流。
 * </p>
 */
@EventBusSubscriber(modid = MillenaireJeimm.MODID)
public class MillenaireJeimmCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("milljeimm")
                .then(Commands.literal("export")
                        .then(Commands.literal("encyclopedia").executes(ctx -> exportEncyclopedia(ctx.getSource())))
                        .executes(ctx -> exportEncyclopedia(ctx.getSource()))
                )
                .then(Commands.literal("dump").executes(ctx -> exportEncyclopedia(ctx.getSource())));

        dispatcher.register(cmd);
    }

    /**
     * 导出全量百科数据至独立文本文件。
     */
    public static int exportEncyclopedia(CommandSourceStack source) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<String> lines = new ArrayList<>();

        lines.add("================================================================================");
        lines.add("Millenaire Encyclopedia Dump - Exported at " + timestamp);
        lines.add("Format: Dual-Tag [TYPE:id|LocalizedName] | Single-Line Record");
        lines.add("================================================================================");
        lines.add("");

        // ----------------- 1. BUILDINGS & VARIANT COSTS -----------------
        lines.add("# ----------------- BUILDINGS & VARIANT COSTS -----------------");
        Collection<BuildingPlanSet> allBuildings = MillenaireAPIHelper.getAllBuildings();
        for (BuildingPlanSet building : allBuildings) {
            if (building == null) continue;

            String bName = MillenaireLocalizeHelper.getBuildingName(building).getString();
            String bId = building.id() != null ? building.id().toString() : (building.culture() + ":" + building.buildingId());
            String cultureStr = building.culture() != null
                    ? building.culture() + "(" + MillenaireLocalizeHelper.getCultureName(building.culture()).getString() + ")"
                    : "common";

            // 基本属性行
            lines.add(clean(String.format(
                    "[BUILDING:%s|%s] Culture: %s | Category: %s | Price: %d | Rep: %d | TownHall: %b | Residential: %b",
                    bId, bName, cultureStr, building.category(), building.price(), building.reputation(),
                    building.isTownHall(), building.isResidential()
            )));

            // 允许生成的村庄
            List<VillageType> allowedVillages = MillenaireAPIHelper.getVillageFormBuilding(building);
            if (allowedVillages != null && !allowedVillages.isEmpty()) {
                String vStr = allowedVillages.stream()
                        .map(v -> v.id() + "(" + (v.name() != null ? v.name() : v.id().getPath()) + ")")
                        .collect(Collectors.joining(", "));
                lines.add(clean(String.format("[BUILDING:%s|%s] AllowedVillages: %s", bId, bName, vStr)));
            }

            // 居住/工作村民
            List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormBuilding(building);
            if (villagers != null && !villagers.isEmpty()) {
                String villStr = villagers.stream()
                        .map(v -> v.id() + "(" + MillenaireLocalizeHelper.getVillagerName(v).getString() + ")")
                        .collect(Collectors.joining(", "));
                lines.add(clean(String.format("[BUILDING:%s|%s] InhabitingVillagers: %s", bId, bName, villStr)));
            }

            // 变体材料消耗行
            if (building.variants() != null) {
                for (Map.Entry<String, List<BuildingPlanSet.LevelDef>> varEntry : building.variants().entrySet()) {
                    String variantKey = varEntry.getKey();
                    List<BuildingPlanSet.LevelDef> levelDefs = varEntry.getValue();
                    if (levelDefs == null || levelDefs.isEmpty()) continue;

                    // 变体满级总消耗
                    Map<ResourceLocation, Integer> totalCosts = BuildingUpgradeCostHelper.calculateVariantTotalResources(levelDefs);
                    String totalCostStr = totalCosts.entrySet().stream()
                            .map(e -> formatItemCost(e.getKey(), e.getValue()))
                            .collect(Collectors.joining(", "));
                    lines.add(clean(String.format("[BUILDING:%s|%s] Variant: %s | TOTAL | Cost: %s", bId, bName, variantKey, totalCostStr)));

                    // 各独立等级消耗
                    for (BuildingPlanSet.LevelDef levelDef : levelDefs) {
                        if (levelDef == null) continue;
                        Map<ResourceLocation, Integer> levelCosts = BuildingLevelCostCategory.getAggregatedResources(levelDef);
                        String levelCostStr = levelCosts.entrySet().stream()
                                .map(e -> formatItemCost(e.getKey(), e.getValue()))
                                .collect(Collectors.joining(", "));
                        lines.add(clean(String.format("[BUILDING:%s|%s] Variant: %s | Level: %d | Cost: %s", bId, bName, variantKey, levelDef.level(), levelCostStr)));
                    }
                }
            }
        }
        lines.add("");

        // ----------------- 2. VILLAGES & COMPOSITION -----------------
        lines.add("# ----------------- VILLAGES & COMPOSITION -----------------");
        Collection<VillageType> allVillages = MillenaireAPIHelper.getAllVillages();
        for (VillageType village : allVillages) {
            if (village == null) continue;

            String vName = village.name() != null && !village.name().isEmpty() ? village.name() : village.id().getPath();
            String vId = village.id() != null ? village.id().toString() : "unknown";
            String cultureStr = village.culture() != null
                    ? village.culture() + "(" + MillenaireLocalizeHelper.getCultureName(village.culture()).getString() + ")"
                    : "unknown";

            StringBuilder defs = new StringBuilder();
            if (village.innerWallType() != null) defs.append("[Inner Wall] ");
            if (village.outerWallType() != null) defs.append("[Outer Wall] ");
            if (village.carriesRaid()) defs.append("[Raid] ");
            String defenseStr = defs.length() > 0 ? defs.toString().trim() : "None";

            // 基本属性行
            lines.add(clean(String.format(
                    "[VILLAGE:%s|%s] Culture: %s | Radius: %dm | MaxBuild: %d | Defenses: %s | Controlled: %s",
                    vId, vName, cultureStr, village.radius(), village.maxSimultaneousConstructions(),
                    defenseStr, village.playerControlled() ? "Player" : "Auto"
            )));

            // 包含建筑列表
            List<BuildingPlanSet> buildings = MillenaireAPIHelper.getBuildingFormVillage(village);
            if (buildings != null && !buildings.isEmpty()) {
                String bStr = buildings.stream()
                        .map(b -> b.id() + "(" + MillenaireLocalizeHelper.getBuildingName(b).getString() + ")")
                        .collect(Collectors.joining(", "));
                lines.add(clean(String.format("[VILLAGE:%s|%s] Buildings: %s", vId, vName, bStr)));
            }

            // 包含村民列表
            List<VillagerType> villagers = MillenaireAPIHelper.getVillagerFormVillage(village);
            if (villagers != null && !villagers.isEmpty()) {
                String villStr = villagers.stream()
                        .map(v -> v.id() + "(" + MillenaireLocalizeHelper.getVillagerName(v).getString() + ")")
                        .collect(Collectors.joining(", "));
                lines.add(clean(String.format("[VILLAGE:%s|%s] Villagers: %s", vId, vName, villStr)));
            }
        }
        lines.add("");

        // ----------------- 3. VILLAGERS & GOODS FLOW -----------------
        lines.add("# ----------------- VILLAGERS & GOODS FLOW -----------------");
        Collection<VillagerType> allVillagers = MillenaireAPIHelper.getAllVillagers();
        for (VillagerType villager : allVillagers) {
            if (villager == null) continue;

            String villName = MillenaireLocalizeHelper.getVillagerName(villager).getString();
            String villId = villager.id() != null ? villager.id().toString() : "unknown";
            String cultureStr = villager.culture() != null
                    ? villager.culture() + "(" + MillenaireLocalizeHelper.getCultureName(villager.culture()).getString() + ")"
                    : "unknown";

            String genderStr = villager.gender() != null ? villager.gender().name() : "Unknown";
            String ageStage = villager.isChild() ? "Child" : "Adult";

            StringBuilder tagBuilder = new StringBuilder();
            if (villager.isRaider()) tagBuilder.append("[Raider] ");
            if (villager.isHostile()) tagBuilder.append("[Hostile] ");
            if (villager.isHelpInAttacks()) tagBuilder.append("[Help Attacks] ");
            if (villager.isArcher()) tagBuilder.append("[Archer] ");
            if (villager.isNoTeleport()) tagBuilder.append("[No Teleport] ");
            if (villager.isNoResurrect()) tagBuilder.append("[No Resurrect] ");
            String tags = tagBuilder.length() > 0 ? tagBuilder.toString().trim() : "[Civilian]";

            // 基本属性行
            lines.add(clean(String.format(
                    "[VILLAGER:%s|%s] Culture: %s | Gender: %s(%s) | HP: %d | Attack: %d | HiringCost: %d | Tags: %s",
                    villId, villName, cultureStr, genderStr, ageStage, (int) villager.maxHealth(),
                    villager.baseAttackStrength(), villager.hiringCost(), tags
            )));

            // 带回物资
            if (villager.resolvedBringBackHomeGoods() != null && !villager.resolvedBringBackHomeGoods().isEmpty()) {
                String items = villager.resolvedBringBackHomeGoods().stream()
                        .filter(i -> i != null && i != Items.AIR)
                        .map(i -> BuiltInRegistries.ITEM.getKey(i) + "(" + i.getDescription().getString() + ")")
                        .collect(Collectors.joining(", "));
                if (!items.isEmpty()) {
                    lines.add(clean(String.format("[VILLAGER:%s|%s] BringBackHome: %s", villId, villName, items)));
                }
            }

            // 上交中心物资
            if (villager.resolvedCollectGoods() != null && !villager.resolvedCollectGoods().isEmpty()) {
                String items = villager.resolvedCollectGoods().stream()
                        .filter(Objects::nonNull)
                        .map(rl -> {
                            Item item = BuiltInRegistries.ITEM.get(rl);
                            String name = (item != null && item != Items.AIR) ? item.getDescription().getString() : rl.getPath();
                            return rl + "(" + name + ")";
                        })
                        .collect(Collectors.joining(", "));
                if (!items.isEmpty()) {
                    lines.add(clean(String.format("[VILLAGER:%s|%s] CollectGoods: %s", villId, villName, items)));
                }
            }

            // 职业需求物资
            if (villager.resolvedRequiredGoods() != null && !villager.resolvedRequiredGoods().isEmpty()) {
                String items = villager.resolvedRequiredGoods().entrySet().stream()
                        .filter(e -> e.getKey() != null && e.getKey() != Items.AIR)
                        .map(e -> (e.getValue() != null ? e.getValue() : 1) + "x " + BuiltInRegistries.ITEM.getKey(e.getKey()) + "(" + e.getKey().getDescription().getString() + ")")
                        .collect(Collectors.joining(", "));
                if (!items.isEmpty()) {
                    lines.add(clean(String.format("[VILLAGER:%s|%s] RequiredGoods: %s", villId, villName, items)));
                }
            }

            // 所属建筑
            List<BuildingPlanSet> resBuildings = MillenaireAPIHelper.getBuildingFormVillager(villager);
            if (resBuildings != null && !resBuildings.isEmpty()) {
                String bStr = resBuildings.stream()
                        .map(b -> b.id() + "(" + MillenaireLocalizeHelper.getBuildingName(b).getString() + ")")
                        .collect(Collectors.joining(", "));
                lines.add(clean(String.format("[VILLAGER:%s|%s] ResidingBuildings: %s", villId, villName, bStr)));
            }

            // 所属村庄
            List<VillageType> belVillages = MillenaireAPIHelper.getVillageFormVillager(villager);
            if (belVillages != null && !belVillages.isEmpty()) {
                String vStr = belVillages.stream()
                        .map(v -> v.id() + "(" + (v.name() != null ? v.name() : v.id().getPath()) + ")")
                        .collect(Collectors.joining(", "));
                lines.add(clean(String.format("[VILLAGER:%s|%s] BelongingVillages: %s", villId, villName, vStr)));
            }
        }
        lines.add("");

        // ----------------- 4. HARDCODED SPOT GATHERING GOALS -----------------
        MillenaireJeimmEncyclopediaHelper.appendHardcodedSpotGoals(lines);

        // ----------------- 5. IMPLICIT LOOT, MERCHANTS & RESTOCK -----------------
        MillenaireJeimmEncyclopediaHelper.appendImplicitLootMerchantsAndRestock(lines);

        // ----------------- 6. COST RULES & ACTIVE SUBSTITUTIONS -----------------
        MillenaireJeimmEncyclopediaHelper.appendCostRulesAndSubstitutions(lines);

        // 写入独立排查文件
        Path logFile = Paths.get("logs", "millenairejeimm", "encyclopedia_dump.txt");
        writeFileSafely(logFile, lines);

        source.sendSuccess(() -> Component.literal(String.format(
                "§6[Millenaire-JEI-MM] §f百科数据导出完成: 共 §e%d§f 条记录. 详见: §b%s",
                lines.size(), logFile.toAbsolutePath())), true);

        return lines.size();
    }

    static String formatItemCost(ResourceLocation itemRl, int count) {
        if (itemRl == null) return "";
        Item item = BuiltInRegistries.ITEM.get(itemRl);
        String locName = (item != null && item != Items.AIR) ? item.getDescription().getString() : itemRl.getPath();
        return count + "x " + itemRl + "(" + locName + ")";
    }

    static String clean(String text) {
        if (text == null) return "";
        return text.replace("\r", " ").replace("\n", " ").trim();
    }

    private static void writeFileSafely(Path path, List<String> lines) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            MMLog.warn(
                    "[Millenaire-JEI-MM] 写入百科导出文件失败: {}",
                    "[Millenaire-JEI-MM] Failed to write encyclopedia dump file: {}",
                    "[Millenaire-JEI-MM] Échec de l'écriture du fichier d'export d'encyclopédie: {}",
                    e.getMessage()
            );
        }
    }
}
