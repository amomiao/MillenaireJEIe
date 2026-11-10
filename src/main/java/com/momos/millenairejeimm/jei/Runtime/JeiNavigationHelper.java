package com.momos.millenairejeimm.jei.Runtime;

import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

/**
 * JEI 精确定向导航门面辅助类 {@link JeiNavigationHelper}。
 * <p>
 * 彻底弃用脆弱的 {@code RecipeIngredientRole} 模糊反查机制，
 * 直接基于现代 JEI 的 {@code IRecipesGui.showRecipes} 与 {@code IRecipesGui.showTypes}
 * 实现 100% 确定性的二级全景页签定向跳转与精准配方锁定。
 * </p>
 */
public final class JeiNavigationHelper {

    private JeiNavigationHelper() {}

    /**
     * 播放标准的原版 UI 点击音效。
     */
    public static void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getSoundManager() != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    /**
     * 精确定向跳转至指定分类，并展示指定的单一配方。
     *
     * @param targetType   目标分类配方类型 {@link RecipeType}
     * @param targetRecipe 目标配方数据对象
     * @param <T>          配方泛型
     * @return 是否成功唤起跳转
     */
    public static <T> boolean navigateToRecipe(RecipeType<T> targetType, T targetRecipe) {
        if (targetRecipe == null) return false;
        return navigateToRecipes(targetType, List.of(targetRecipe), List.of());
    }

    /**
     * 精确定向跳转至指定分类，并展示指定配方集与焦点。
     *
     * @param targetType 目标分类配方类型 {@link RecipeType}
     * @param recipes    要展示的配方集合列表
     * @param focuses    焦点集合列表（可为空列表）
     * @param <T>        配方泛型
     * @return 是否成功唤起跳转
     */
    public static <T> boolean navigateToRecipes(RecipeType<T> targetType, List<T> recipes, List<IFocus<?>> focuses) {
        if (MillenaireJeimmPlugin.JeiRuntime == null || targetType == null || recipes == null || recipes.isEmpty()) {
            return false;
        }

        try {
            IRecipeManager recipeManager = MillenaireJeimmPlugin.JeiRuntime.getRecipeManager();
            IRecipeCategory<T> category = recipeManager.getRecipeCategory(targetType);
            if (category == null) {
                return false;
            }

            List<IFocus<?>> safeFocuses = focuses != null ? focuses : List.of();
            MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().showRecipes(category, recipes, safeFocuses);
            playClickSound();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 定向切换到指定的 RecipeType 分类页签。
     *
     * @param targetType 目标分类配方类型 {@link RecipeType}
     * @return 是否成功切换
     */
    public static boolean navigateToCategory(RecipeType<?> targetType) {
        if (MillenaireJeimmPlugin.JeiRuntime == null || targetType == null) {
            return false;
        }

        try {
            MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().showTypes(List.of(targetType));
            playClickSound();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
