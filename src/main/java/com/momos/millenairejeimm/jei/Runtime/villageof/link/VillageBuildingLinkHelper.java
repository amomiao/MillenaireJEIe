package com.momos.millenairejeimm.jei.Runtime.villageof.link;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * {@link VillageBuildingLink} 的 JEI 成分助手实现类 {@link VillageBuildingLinkHelper}。
 */
public class VillageBuildingLinkHelper implements IIngredientHelper<VillageBuildingLink> {
    @Override
    public IIngredientType<VillageBuildingLink> getIngredientType() {
        return MillIngredientTypes.VILLAGE_BUILDING_LINK;
    }

    @Override
    public String getDisplayName(VillageBuildingLink ingredient) {
        return ingredient != null && ingredient.village() != null ? ingredient.village().name() : "";
    }

    /**
     * @deprecated JEI 19.9.0+ 已废弃此方法，由 {@link #getUid(VillageBuildingLink, UidContext)} 替代。
     */
    @Override
    @Deprecated(since = "19.9.0", forRemoval = true)
    @SuppressWarnings("removal")
    // [醒目新注释] 关键修复：添加 @SuppressWarnings("removal") 消除 Java 9+ 编译器对 forRemoval 抽象方法重写的警告
    public String getUniqueId(VillageBuildingLink ingredient, UidContext context) {
        return getResourceLocation(ingredient).toString();
    }

    /**
     * 获取成分唯一标识 UID。
     * JEI 19.x 检索核心依靠此方法返回的 {@link ResourceLocation} 进行匹配比对。
     */
    @Override
    public Object getUid(VillageBuildingLink ingredient, UidContext context) {
        return getResourceLocation(ingredient);
    }

    @Override
    public ResourceLocation getResourceLocation(VillageBuildingLink ingredient) {
        return ingredient != null && ingredient.village() != null && ingredient.village().id() != null
                ? ingredient.village().id()
                : ResourceLocation.fromNamespaceAndPath("millenaire", "unknown");
    }

    @Override
    public VillageBuildingLink copyIngredient(VillageBuildingLink ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable VillageBuildingLink ingredient) {
        return ingredient == null || ingredient.village() == null ? "VillageBuildingLink is null" : "VillageBuildingLink: " + ingredient.village().id();
    }
}