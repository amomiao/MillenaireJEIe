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
 * 统筹 JEI 自定义成分图标与点缀文字渲染规则的顶级解析器。
 */
public class MillIngredientIconResolver {

    /**
     * 自定义成分图标渲染信息载体。
     *
     * @param iconStack 渲染的主体图标物品堆栈 {@link ItemStack}
     * @param badgeText 点缀徽章字符串
     * @param badgeColor 点缀徽章文字颜色 (ARGB/RGB)
     */
    public record IngredientIconInfo(ItemStack iconStack, String badgeText, int badgeColor) {}

    /**
     * 通用入口：根据任意成分对象解析图标与点缀信息。
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
        return new IngredientIconInfo(new ItemStack(Items.BARRIER), "未", 0xFF5555);
    }

    private static IngredientIconInfo resolveBuilding(BuildingPlanSet building) {
        ItemStack iconStack = ItemStack.EMPTY;
        if (building.icon() != null && !building.icon().isEmpty()) {
            Item item = ItemHelper.resolve(building.icon());
            if (item != null) {
                iconStack = new ItemStack(item);
            }
        }
        if (iconStack.isEmpty()) {
            iconStack = new ItemStack(Items.PAPER);
        }

        if (building.isTownHall()) {
            return new IngredientIconInfo(iconStack, "厅", 0xFFD700);
        } else if (building.isResidential()) {
            return new IngredientIconInfo(iconStack, "居", 0x55FF55);
        }

        String badge = (building.nativeName() != null && !building.nativeName().isEmpty())
                ? building.nativeName().substring(0, 1)
                : "建";
        return new IngredientIconInfo(iconStack, badge, 0xFFDD55);
    }

    private static IngredientIconInfo resolveCulture(Culture culture) {
        String name = (culture != null && culture.displayName() != null) ? culture.displayName() : "";
        String badge = name.isEmpty() ? "文" : name.substring(0, Math.min(2, name.length()));
        return new IngredientIconInfo(new ItemStack(Items.WRITABLE_BOOK), badge, 0x55FFFF);
    }

    private static IngredientIconInfo resolveVillage(VillageType village) {
        return new IngredientIconInfo(new ItemStack(Items.BELL), "村", 0xFFAA00);
    }

    private static IngredientIconInfo resolveVillager(VillagerType villager) {
        return new IngredientIconInfo(new ItemStack(Items.EMERALD), "民", 0x55FF55);
    }
}