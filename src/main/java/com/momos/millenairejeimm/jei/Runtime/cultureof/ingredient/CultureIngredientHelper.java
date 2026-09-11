package com.momos.millenairejeimm.jei.Runtime.cultureof.ingredient;

import com.momos.millenairejeimm.jei.MillIngredientTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.millenaire.culture.Culture;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 文化 {@link Culture} 的 JEI 检索辅助类。
 */
public class CultureIngredientHelper implements IIngredientHelper<Culture> {
    @Override
    public IIngredientType<Culture> getIngredientType() {
        return MillIngredientTypes.CULTURE;
    }

    // [醒目新注释] 增加非空回退逻辑，保证 JEI 搜索界面不因 displayName 为 null 产生空指针
    @Override
    public String getDisplayName(Culture ingredient) {
        if (ingredient == null) return "Unknown Culture";
        return ingredient.displayName() != null ? ingredient.displayName() : ingredient.id().getPath();
    }

    // [醒目新注释] 补全取消注册接口废弃方法，规范返回合法 ID 字符串
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
        return ingredient != null && ingredient.id() != null ? ingredient.id().toString() : "millenaire:unknown_culture";
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
        if (ingredient == null || ingredient.id() == null) {
            return ResourceLocation.fromNamespaceAndPath("millenaire", "unknown_culture");
        }
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

    // [醒目新注释] 拓展 Tag 检索流，注入基础 Tag 及文化专属 Path Tag，支持 JEI $ 符号高级搜索
    @Override
    public Stream<ResourceLocation> getTagStream(Culture ingredient) {
        List<ResourceLocation> tags = new ArrayList<>();
        tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "culture"));
        if (ingredient != null && ingredient.id() != null) {
            tags.add(ingredient.id());
            tags.add(ResourceLocation.fromNamespaceAndPath("millenaire", "culture/" + ingredient.id().getPath().toLowerCase(Locale.ROOT)));
        }
        return tags.stream();
    }
}