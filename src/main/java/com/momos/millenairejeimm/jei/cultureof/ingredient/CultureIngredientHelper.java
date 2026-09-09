package com.momos.millenairejeimm.jei.cultureof.ingredient;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.millenaire.culture.Culture;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 文化 {@link Culture} 的 JEI 检索辅助类。
 */
public class CultureIngredientHelper implements IIngredientHelper<Culture> {
    @Override
    public IIngredientType<Culture> getIngredientType() {
        return MillIngredientTypes.CULTURE;
    }

    @Override
    public String getDisplayName(Culture ingredient) {
        return ingredient.displayName();
    }

    // [醒目新注释] 补全接口无 default 实现的废弃抽象方法，防止 Java 编译报错
    /**
     * 获取 Ingredient 的唯一标识字符串。
     *
     * @param ingredient 文化配置对象 {@link Culture}
     * @param context UID 上下文 {@link UidContext}
     * @return 唯一标识字符串
     */
    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(Culture ingredient, UidContext context) {
        return ingredient.id().toString();
    }

    // [醒目新注释] JEI 新版推荐重写 getUid 方法，直接复用 getUniqueId 逻辑
    /**
     * 获取 Ingredient 的唯一标识对象 {@link Object}。
     *
     * @param ingredient 文化配置对象 {@link Culture}
     * @param context UID 上下文 {@link UidContext}
     * @return 唯一标识对象
     */
    @Override
    public Object getUid(Culture ingredient, UidContext context) {
        return getUniqueId(ingredient, context);
    }

    @Override
    public ResourceLocation getResourceLocation(Culture ingredient) {
        return ingredient.id();
    }

    @Override
    public Culture copyIngredient(Culture ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable Culture ingredient) {
        return ingredient == null ? "Culture is null" : "Culture: " + ingredient.id();
    }

    @Override
    public Stream<ResourceLocation> getTagStream(Culture ingredient) {
        List<ResourceLocation> tags = new ArrayList<>();
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "culture"));
        if (ingredient != null && ingredient.id() != null) {
            tags.add(ingredient.id());
        }
        return tags.stream();
    }
}