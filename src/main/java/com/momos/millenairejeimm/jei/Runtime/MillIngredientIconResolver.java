package com.momos.millenairejeimm.jei.Runtime;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;
import org.millenaire.item.ItemHelper;

/**
 * 统筹 JEI 自定义成分图标、右上角角标物品与点缀文字渲染规则的顶级解析器。
 */
public class MillIngredientIconResolver {

    // [醒目新注释] 扩展 IngredientIconInfo：新增 overlayIconStack (右上角角标物品，如钻石/金锭/铁锭/绿宝石)
    /**
     * 自定义成分图标渲染信息载体。
     *
     * @param mainIconStack    渲染的主体图标物品堆栈 {@link ItemStack}（优先从自身提取，无则回退旅行书）
     * @param overlayIconStack 右上角微缩角标物品堆栈 {@link ItemStack}（文化:钻石, 村庄:金锭, 建筑:铁锭, 村民:绿宝石）
     * @param badgeText        点缀徽章短文本
     * @param badgeColor       点缀徽章文字颜色 (ARGB/RGB)
     */
    public record IngredientIconInfo(
            ItemStack mainIconStack,
            ItemStack overlayIconStack,
            String badgeText,
            int badgeColor
    ) {}

    // region 辅助方法：旅行书安全获取

    /**
     * [醒目新注释] 尝试从 Millenaire 注册表解析旅行书物品 "millenaire:travel_book"，若不可用则回退至指定默认物品。
     *
     * @param fallbackItem 备用物品 {@link Item}
     * @return 包含旅行书或备用物品的 {@link ItemStack}
     */
    private static ItemStack getTravelBookOrDefault(Item fallbackItem) {
        Item travelBook = ItemHelper.resolve("millenaire:travel_book");
        if (travelBook != null && travelBook != Items.AIR) {
            return new ItemStack(travelBook);
        }
        return new ItemStack(fallbackItem != null ? fallbackItem : Items.BOOK);
    }

    // endregion 辅助方法：旅行书安全获取

    /**
     * 通用入口：根据任意成分对象解析主图标、右上角角标与点缀信息。
     *
     * @param ingredient 成分对象 {@link Object}
     * @return 图标与点缀渲染数据 {@link IngredientIconInfo}
     */
    public static IngredientIconInfo resolve(Object ingredient) {
        if (ingredient instanceof BuildingPlanSet building) {
            return resolveBuilding(building);
        } else if (ingredient instanceof Culture culture) {
            return resolveCulture(culture);
        } else if (ingredient instanceof VillageType village) {
            return resolveVillage(village);
        } else if (ingredient instanceof VillagerType villager) {
            return resolveVillager(villager);
        }
        return new IngredientIconInfo(
                getTravelBookOrDefault(Items.BARRIER),
                new ItemStack(Items.BARRIER),
                "None",
                0xFF5555
        );
    }

    // region 细分类型解析逻辑

    /**
     * 解析建筑 {@link BuildingPlanSet} 成分：角标为铁锭 {@link Items#IRON_INGOT}。
     */
    private static IngredientIconInfo resolveBuilding(BuildingPlanSet building) {
        ItemStack mainStack = ItemStack.EMPTY;

        // [醒目新注释] 优先从建筑配方自带的 icon 路径解析主体物品，若为空则回退从旅行书获取
        if (building != null && building.icon() != null && !building.icon().isEmpty()) {
            Item item = ItemHelper.resolve(building.icon());
            if (item != null) {
                mainStack = new ItemStack(item);
            }
        }
        if (mainStack.isEmpty()) {
            mainStack = getTravelBookOrDefault(Items.PAPER);
        }

        // 角标固定为铁锭
        ItemStack overlayStack = new ItemStack(Items.IRON_INGOT);

        String badge = "B";
        int color = 0xFFDD55;

        if (building != null) {
            if (building.isTownHall()) {
                badge = "T";
                color = 0xFFD700;
            } else if (building.isResidential()) {
                badge = "R";
                color = 0x55FF55;
            } else if (building.nativeName() != null && !building.nativeName().isEmpty()) {
                badge = building.nativeName().substring(0, 1).toUpperCase();
            }
        }

        return new IngredientIconInfo(mainStack, overlayStack, badge, color);
    }

    /**
     * 解析文化 {@link Culture} 成分：角标为钻石 {@link Items#DIAMOND}，主体优先使用旅行书。
     */
    private static IngredientIconInfo resolveCulture(Culture culture) {
        // [醒目新注释] 文化无独立物品图标，主体内容直接尝试从旅行书中获得
        ItemStack mainStack = getTravelBookOrDefault(Items.WRITABLE_BOOK);

        // 角标固定为钻石
        ItemStack overlayStack = new ItemStack(Items.DIAMOND);

        String name = (culture != null && culture.displayName() != null) ? culture.displayName() : "";
        String badge = name.isEmpty() ? "C" : name.substring(0, Math.min(1, name.length())).toUpperCase();

        return new IngredientIconInfo(mainStack, overlayStack, badge, 0x55FFFF);
    }

    /**
     * 解析村庄 {@link VillageType} 成分：角标为金锭 {@link Items#GOLD_INGOT}。
     */
    private static IngredientIconInfo resolveVillage(VillageType village) {
        ItemStack mainStack = ItemStack.EMPTY;

        // [醒目新注释] 尝试获取村庄图标（如警钟或特有标记），无则回退从旅行书中获得
        if (village != null && village.icon() != null && !village.icon().isEmpty()) {
            Item item = ItemHelper.resolve(village.icon());
            if (item != null) {
                mainStack = new ItemStack(item);
            }
        }
        if (mainStack.isEmpty()) {
            mainStack = getTravelBookOrDefault(Items.BELL);
        }

        // 角标固定为金锭
        ItemStack overlayStack = new ItemStack(Items.GOLD_INGOT);

        String name = (village != null && village.name() != null) ? village.name() : "";
        String badge = name.isEmpty() ? "V" : name.substring(0, 1).toUpperCase();

        return new IngredientIconInfo(mainStack, overlayStack, badge, 0xFFAA00);
    }

    /**
     * 解析村民 {@link VillagerType} 成分：角标为绿宝石 {@link Items#EMERALD}。
     */
    private static IngredientIconInfo resolveVillager(VillagerType villager) {
        ItemStack mainStack = ItemStack.EMPTY;

        // [醒目新注释] 尝试解析村民对应图标，无则回退从旅行书中获得
        if (villager != null && villager.icon() != null && !villager.icon().isEmpty()) {
            Item item = ItemHelper.resolve(villager.icon());
            if (item != null) {
                mainStack = new ItemStack(item);
            }
        }
        if (mainStack.isEmpty()) {
            mainStack = getTravelBookOrDefault(Items.EMERALD);
        }

        // 角标固定为绿宝石
        ItemStack overlayStack = new ItemStack(Items.EMERALD);

        String name = (villager != null && villager.nativeName() != null) ? villager.nativeName() : "";
        String badge = name.isEmpty() ? "Vr" : name.substring(0, Math.min(1, name.length())).toUpperCase();

        return new IngredientIconInfo(mainStack, overlayStack, badge, 0x55FF55);
    }

    // endregion 细分类型解析逻辑
}