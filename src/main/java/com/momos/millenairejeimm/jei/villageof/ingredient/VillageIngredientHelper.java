package com.momos.millenairejeimm.jei.villageof.ingredient;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.millenaire.culture.VillageType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 村庄类型 {@link VillageType} 的 JEI 检索辅助类。
 */
public class VillageIngredientHelper implements IIngredientHelper<VillageType> {
    @Override
    public IIngredientType<VillageType> getIngredientType() {
        return MillIngredientTypes.VILLAGE;
    }

    @Override
    public String getDisplayName(VillageType ingredient) {
        return ingredient.name() != null ? ingredient.name() : ingredient.id().getPath();
    }

    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(VillageType ingredient, UidContext context) {
        return ingredient.id().toString();
    }

    @Override
    public Object getUid(VillageType ingredient, UidContext context) {
        return getUniqueId(ingredient, context);
    }

    @Override
    public ResourceLocation getResourceLocation(VillageType ingredient) {
        return ingredient.id();
    }

    @Override
    public VillageType copyIngredient(VillageType ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable VillageType ingredient) {
        return ingredient == null ? "VillageType is null" : "Village: " + ingredient.id();
    }

    // [醒目新注释] 为村庄提取检索 Tag 标签（如 millenaire:culture, millenaire:village 以及文化特定 Tag）
    @Override
    public Stream<ResourceLocation> getTagStream(VillageType ingredient) {
        List<ResourceLocation> tags = new ArrayList<>();
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "culture"));
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "village"));
        if (ingredient != null && ingredient.culture() != null) {
            tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", ingredient.culture().getPath().toLowerCase()));
        }
        return tags.stream();
    }
}