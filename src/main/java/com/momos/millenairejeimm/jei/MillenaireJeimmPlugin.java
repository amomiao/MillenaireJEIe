package com.momos.millenairejeimm.jei;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.util.MMLog;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.ingredient.CultureIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.cultureof.ingredient.CultureIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.villageof.VillageCategory;
import com.momos.millenairejeimm.jei.Runtime.villageof.ingredient.VillageIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.villageof.ingredient.VillageIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.villagerof.VillagerCategory;
import com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient.VillagerIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient.VillagerIngredientRenderer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 千年百科高级 JEI 扩展主插件 {@link IModPlugin}。
 */
@JeiPlugin
public class MillenaireJeimmPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("millenairejeimm", "jei_plugin");
    public static IJeiRuntime JeiRuntime;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    // [醒目新注释] 接入 {@link MMLog} 三语控制台日志，对每个 Ingredient 注册阶段实施异常捕获与数量监控
    /**
     * 注册自定义 JEI 成分类型 {@link IModIngredientRegistration}。
     * 采用异常隔离机制，防止单一 Ingredient 注册抛错中断整个 JEI 初始化。
     *
     * @param registration JEI 成分注册器
     */
    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        MMLog.info(
                "开始注册 JEI 自定义成分 (Ingredients)...",
                "Starting registration of JEI custom ingredients...",
                "Début de l'enregistrement des ingrédients personnalisés JEI..."
        );

        // 1. 文化成分注册
        try {
            Collection<Culture> cultures = MillenaireAPIHelper.getAllCultureType().values();
            registration.register(
                    MillIngredientTypes.CULTURE,
                    cultures,
                    new CultureIngredientHelper(),
                    new CultureIngredientRenderer(),
                    MillIngredientTypes.CULTURE_CODEC
            );
            MMLog.info(
                    "已注册文化成分，总数: {}",
                    "Registered culture ingredients, total: {}",
                    "Ingrédients de culture enregistrés, total: {}",
                    cultures.size()
            );
        } catch (Exception e) {
            MMLog.warn(
                    "注册文化成分阶段出现异常: {}",
                    "Exception during culture ingredient registration: {}",
                    "Exception lors de l'enregistrement de l'ingrédient de culture: {}",
                    e.getMessage()
            );
        }

        // 2. 村庄类型成分注册
        try {
            Collection<VillageType> villages = MillenaireAPIHelper.getAllVillageType().values();
            registration.register(
                    MillIngredientTypes.VILLAGE,
                    villages,
                    new VillageIngredientHelper(),
                    new VillageIngredientRenderer(),
                    MillIngredientTypes.VILLAGE_CODEC
            );
            MMLog.info(
                    "已注册村庄成分，总数: {}",
                    "Registered village ingredients, total: {}",
                    "Ingrédients de village enregistrés, total: {}",
                    villages.size()
            );
        } catch (Exception e) {
            MMLog.warn(
                    "注册村庄成分阶段出现异常: {}",
                    "Exception during village ingredient registration: {}",
                    "Exception lors de l'enregistrement de l'ingrédient de village: {}",
                    e.getMessage()
            );
        }

        // 3. 建筑集成分注册
        try {
            Collection<BuildingPlanSet> buildings = MillenaireAPIHelper.getAllBuildingPlanSets().values();
            registration.register(
                    MillIngredientTypes.BUILDING,
                    buildings,
                    new BuildingIngredientHelper(),
                    new BuildingIngredientRenderer(),
                    MillIngredientTypes.BUILDING_CODEC
            );
            MMLog.info(
                    "已注册建筑集成分，总数: {}",
                    "Registered building ingredients, total: {}",
                    "Ingrédients de bâtiment enregistrés, total: {}",
                    buildings.size()
            );
        } catch (Exception e) {
            MMLog.warn(
                    "注册建筑集成分阶段出现异常: {}",
                    "Exception during building ingredient registration: {}",
                    "Exception lors de l'enregistrement de l'ingrédient de bâtiment: {}",
                    e.getMessage()
            );
        }

        // 4. 村民类型成分注册
        try {
            Collection<VillagerType> villagers = MillenaireAPIHelper.getAllVillagerType().values();
            registration.register(
                    MillIngredientTypes.VILLAGER,
                    villagers,
                    new VillagerIngredientHelper(),
                    new VillagerIngredientRenderer(),
                    MillIngredientTypes.VILLAGER_CODEC
            );
            MMLog.info(
                    "已注册村民成分，总数: {}",
                    "Registered villager ingredients, total: {}",
                    "Ingrédients de villageois enregistrés, total: {}",
                    villagers.size()
            );
        } catch (Exception e) {
            MMLog.warn(
                    "注册村民成分阶段出现异常: {}",
                    "Exception during villager ingredient registration: {}",
                    "Exception lors de l'enregistrement de l'ingrédient de villageois: {}",
                    e.getMessage()
            );
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IIngredientManager ingredientManager = registration.getJeiHelpers().getIngredientManager();
        // [醒目新注释] 注册 VillageCategory 分类页面
        registration.addRecipeCategories(
                new CultureCategory(registration.getJeiHelpers().getGuiHelper(), ingredientManager),
                new BuildingCategory(registration.getJeiHelpers().getGuiHelper()),
                new VillagerCategory(registration.getJeiHelpers().getGuiHelper()),
                new VillageCategory(registration.getJeiHelpers().getGuiHelper())
        );
        // [醒目新注释] 使用 {@link MMLog} 确认页签 Class 注册完成
        MMLog.info(
                "已成功向 JEI 注册全部 RecipeCategory 类别 (文化、建筑、村民、村庄)。",
                "Successfully registered all JEI RecipeCategories (Culture, Building, Villager, Village).",
                "Toutes les catégories de recettes JEI ont été enregistrées avec succès."
        );
    }

    // [醒目新注释] 引入 {@link MMLog} 三语埋点，并增加对配方集合的非空判断，避免向 JEI 注入空配方导致页签被自动隐藏
    /**
     * 向 JEI 注册具体配方内容 {@link IRecipeRegistration}。
     *
     * @param registration JEI 配方注册器
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        MMLog.info(
                "开始向 JEI 注入各分类配方数据...",
                "Starting registration of JEI recipe data...",
                "Début de l'enregistrement des données de recettes JEI..."
        );

        // 1. 文化配方注册
        try {
            Collection<Culture> cultures = MillenaireAPIHelper.getAllCultureType().values();
            if (!cultures.isEmpty()) {
                registration.addRecipes(
                        CultureCategory.RECIPE_TYPE,
                        new ArrayList<>(cultures)
                );
                MMLog.info(
                        "已向 JEI 注入文化配方，数量: {}",
                        "Injected culture recipes to JEI, count: {}",
                        "Recettes de culture injectées dans JEI, quantité: {}",
                        cultures.size()
                );
            } else {
                MMLog.warn(
                        "文化数据为空，跳过 JEI 文化配方注入。",
                        "Culture list is empty, skipping JEI culture recipe injection.",
                        "La liste des cultures est vide, injection ignorée."
                );
            }
        } catch (Exception e) {
            MMLog.warn(
                    "注入文化配方失败: {}",
                    "Failed to inject culture recipes: {}",
                    "Échec de l'injection des recettes de culture: {}",
                    e.getMessage()
            );
        }

        // 2. 村庄配方注册
        try {
            Collection<VillageType> villages = MillenaireAPIHelper.getAllVillageType().values();
            if (!villages.isEmpty()) {
                registration.addRecipes(
                        VillageCategory.RECIPE_TYPE,
                        new ArrayList<>(villages)
                );
                MMLog.info(
                        "已向 JEI 注入村庄配方，数量: {}",
                        "Injected village recipes to JEI, count: {}",
                        "Recettes de village injectées dans JEI, quantité: {}",
                        villages.size()
                );
            } else {
                MMLog.warn(
                        "村庄数据为空，跳过 JEI 村庄配方注入。",
                        "Village list is empty, skipping JEI village recipe injection.",
                        "La liste des villages est vide, injection ignorée."
                );
            }
        } catch (Exception e) {
            MMLog.warn(
                    "注入村庄配方失败: {}",
                    "Failed to inject village recipes: {}",
                    "Échec de l'injection des recettes de village: {}",
                    e.getMessage()
            );
        }

        // 3. 建筑配方注册
        try {
            Collection<BuildingPlanSet> buildings = MillenaireAPIHelper.getAllBuildingPlanSets().values();
            if (!buildings.isEmpty()) {
                registration.addRecipes(
                        BuildingCategory.RECIPE_TYPE,
                        new ArrayList<>(buildings)
                );
                MMLog.info(
                        "已向 JEI 注入建筑配方，数量: {}",
                        "Injected building recipes to JEI, count: {}",
                        "Recettes de bâtiment injectées dans JEI, quantité: {}",
                        buildings.size()
                );
            } else {
                MMLog.warn(
                        "建筑数据为空，跳过 JEI 建筑配方注入。",
                        "Building list is empty, skipping JEI building recipe injection.",
                        "La liste des bâtiments est vide, injection ignorée."
                );
            }
        } catch (Exception e) {
            MMLog.warn(
                    "注入建筑配方失败: {}",
                    "Failed to inject building recipes: {}",
                    "Échec de l'injection des recettes de bâtiment: {}",
                    e.getMessage()
            );
        }

        // 4. 村民配方注册
        try {
            Collection<VillagerType> villagers = MillenaireAPIHelper.getAllVillagerType().values();
            if (!villagers.isEmpty()) {
                registration.addRecipes(
                        VillagerCategory.RECIPE_TYPE,
                        new ArrayList<>(villagers)
                );
                MMLog.info(
                        "已向 JEI 注入村民配方，数量: {}",
                        "Injected villager recipes to JEI, count: {}",
                        "Recettes de villageois injectées dans JEI, quantité: {}",
                        villagers.size()
                );
            } else {
                MMLog.warn(
                        "村民数据为空，跳过 JEI 村民配方注入。",
                        "Villager list is empty, skipping JEI villager recipe injection.",
                        "La liste des villageois est vide, injection ignorée."
                );
            }
        } catch (Exception e) {
            MMLog.warn(
                    "注入村民配方失败: {}",
                    "Failed to inject villager recipes: {}",
                    "Échec de l'injection des recettes de villageois: {}",
                    e.getMessage()
            );
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiRuntime = runtime;
    }
}