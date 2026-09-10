package com.momos.millenairejeimm.jei.Runtime.villageof.link;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * {@link VillageVillagerLink} 的 JEI 成分助手实现类 {@link VillageVillagerLinkHelper}。
 */
public class VillageVillagerLinkHelper implements IIngredientHelper<VillageVillagerLink> {
    @Override
    public IIngredientType<VillageVillagerLink> getIngredientType() {
        return MillIngredientTypes.VILLAGE_VILLAGER_LINK;
    }

    @Override
    public String getDisplayName(VillageVillagerLink ingredient) {
        return ingredient != null && ingredient.village() != null ? ingredient.village().name() : "";
    }

    /**
     * @deprecated JEI 19.9.0+ 已废弃此方法，由 {@link #getUid(VillageVillagerLink, UidContext)} 替代。
     */
    @Override
    @Deprecated(since = "19.9.0", forRemoval = true)
    @SuppressWarnings("removal")
    // [醒目新注释] 消除 Java 9+ 编译器对 JEI 19.9.0+ 标记为 forRemoval 的抽象方法重写警告
    public String getUniqueId(VillageVillagerLink ingredient, UidContext context) {
        return getResourceLocation(ingredient).toString();
    }

    /**
     * 获取成分唯一标识 UID。
     * JEI 19.x 检索核心依靠此方法返回的 {@link ResourceLocation} 进行匹配比对。
     */
    @Override
    public Object getUid(VillageVillagerLink ingredient, UidContext context) {
        // [醒目新注释] 直接返回 ResourceLocation，利用 ResourceLocation 原生完备的 equals 与 hashCode 进行精确索引比对
        return getResourceLocation(ingredient);
    }

    @Override
    public ResourceLocation getResourceLocation(VillageVillagerLink ingredient) {
        return ingredient != null && ingredient.village() != null && ingredient.village().id() != null
                ? ingredient.village().id()
                : ResourceLocation.fromNamespaceAndPath("millenaire", "unknown");
    }

    @Override
    public VillageVillagerLink copyIngredient(VillageVillagerLink ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable VillageVillagerLink ingredient) {
        return ingredient == null || ingredient.village() == null ? "VillageVillagerLink is null" : "VillageVillagerLink: " + ingredient.village().id();
    }
}