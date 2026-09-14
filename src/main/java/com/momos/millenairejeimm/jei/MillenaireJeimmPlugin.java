// region 插件主体定义与初始化
package com.momos.millenairejeimm.jei;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeim.util.MMLog;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingLevelCostCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingVillageCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingVillagerCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureVillagesCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.ingredient.CultureIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.cultureof.ingredient.CultureIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.villageof.VillageBuildingsCategory;
import com.momos.millenairejeimm.jei.Runtime.villageof.VillageCategory;
import com.momos.millenairejeimm.jei.Runtime.villageof.VillageVillagersCategory;
import com.momos.millenairejeimm.jei.Runtime.villageof.ingredient.VillageIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.villageof.ingredient.VillageIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.villagerof.*;
import com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient.VillagerIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.villagerof.ingredient.VillagerIngredientRenderer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
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
import java.util.List;

/**
 * 千年百科高级 JEI 扩展主插件 {@link IModPlugin}。
 */
@JeiPlugin
public class MillenaireJeimmPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("millenairejeimm", "jei_plugin");
    public static IJeiRuntime JeiRuntime;

    // region 日志文本常量
    private static final String LOG_INGREDIENTS_START_ZH = "开始注册 JEI 自定义成分 (Ingredients)...";
    private static final String LOG_INGREDIENTS_START_EN = "Starting registration of JEI custom ingredients...";
    private static final String LOG_INGREDIENTS_START_FR = "Début de l'enregistrement des ingrédients personnalisés JEI...";

    private static final String LOG_CULTURE_ING_SUCCESS_ZH = "已注册文化成分，总数: {}";
    private static final String LOG_CULTURE_ING_SUCCESS_EN = "Registered culture ingredients, total: {}";
    private static final String LOG_CULTURE_ING_SUCCESS_FR = "Ingrédients de culture enregistrés, total: {}";

    private static final String LOG_CULTURE_ING_ERR_ZH = "注册文化成分阶段出现异常: {}";
    private static final String LOG_CULTURE_ING_ERR_EN = "Exception during culture ingredient registration: {}";
    private static final String LOG_CULTURE_ING_ERR_FR = "Exception lors de l'enregistrement de l'ingrédient de culture: {}";

    private static final String LOG_VILLAGE_ING_SUCCESS_ZH = "已注册村庄成分，总数: {}";
    private static final String LOG_VILLAGE_ING_SUCCESS_EN = "Registered village ingredients, total: {}";
    private static final String LOG_VILLAGE_ING_SUCCESS_FR = "Ingrédients de village enregistrés, total: {}";

    private static final String LOG_VILLAGE_ING_ERR_ZH = "注册村庄成分阶段出现异常: {}";
    private static final String LOG_VILLAGE_ING_ERR_EN = "Exception during village ingredient registration: {}";
    private static final String LOG_VILLAGE_ING_ERR_FR = "Exception lors de l'enregistrement de l'ingrédient de village: {}";

    private static final String LOG_BUILDING_ING_SUCCESS_ZH = "已注册建筑集成分，总数: {}";
    private static final String LOG_BUILDING_ING_SUCCESS_EN = "Registered building ingredients, total: {}";
    private static final String LOG_BUILDING_ING_SUCCESS_FR = "Ingrédients de bâtiment enregistrés, total: {}";

    private static final String LOG_BUILDING_ING_ERR_ZH = "注册建筑集成分阶段出现异常: {}";
    private static final String LOG_BUILDING_ING_ERR_EN = "Exception during building ingredient registration: {}";
    private static final String LOG_BUILDING_ING_ERR_FR = "Exception lors de l'enregistrement de l'ingrédient de bâtiment: {}";

    private static final String LOG_VILLAGER_ING_SUCCESS_ZH = "已注册村民成分，总数: {}";
    private static final String LOG_VILLAGER_ING_SUCCESS_EN = "Registered villager ingredients, total: {}";
    private static final String LOG_VILLAGER_ING_SUCCESS_FR = "Ingrédients de villageois enregistrés, total: {}";

    private static final String LOG_VILLAGER_ING_ERR_ZH = "注册村民成分阶段出现异常: {}";
    private static final String LOG_VILLAGER_ING_ERR_EN = "Exception during villager ingredient registration: {}";
    private static final String LOG_VILLAGER_ING_ERR_FR = "Exception lors de l'enregistrement de l'ingrédient de villageois: {}";

    private static final String LOG_CATEGORIES_SUCCESS_ZH = "已成功向 JEI 注册全部 RecipeCategory 类别 (文化、建筑、村民、村庄)。";
    private static final String LOG_CATEGORIES_SUCCESS_EN = "Successfully registered all JEI RecipeCategories (Culture, Building, Villager, Village).";
    private static final String LOG_CATEGORIES_SUCCESS_FR = "Toutes les catégories de recettes JEI ont été enregistrées avec succès.";

    private static final String LOG_RECIPES_START_ZH = "开始向 JEI 注入各分类配方数据...";
    private static final String LOG_RECIPES_START_EN = "Starting registration of JEI recipe data...";
    private static final String LOG_RECIPES_START_FR = "Début de l'enregistrement des données de recettes JEI...";

    private static final String LOG_CULTURE_RECIPE_SUCCESS_ZH = "已向 JEI 注入文化配方，数量: {}";
    private static final String LOG_CULTURE_RECIPE_SUCCESS_EN = "Injected culture recipes to JEI, count: {}";
    private static final String LOG_CULTURE_RECIPE_SUCCESS_FR = "Recettes de culture injectées dans JEI, quantité: {}";

    private static final String LOG_CULTURE_RECIPE_EMPTY_ZH = "文化数据为空，跳过 JEI 文化配方注入。";
    private static final String LOG_CULTURE_RECIPE_EMPTY_EN = "Culture list is empty, skipping JEI culture recipe injection.";
    private static final String LOG_CULTURE_RECIPE_EMPTY_FR = "La liste des cultures est vide, injection ignorée.";

    private static final String LOG_CULTURE_RECIPE_ERR_ZH = "注入文化配方失败: {}";
    private static final String LOG_CULTURE_RECIPE_ERR_EN = "Failed to inject culture recipes: {}";
    private static final String LOG_CULTURE_RECIPE_ERR_FR = "Échec de l'injection des recettes de culture: {}";

    private static final String LOG_VILLAGE_RECIPE_SUCCESS_ZH = "已向 JEI 注入村庄配方，数量: {}";
    private static final String LOG_VILLAGE_RECIPE_SUCCESS_EN = "Injected village recipes to JEI, count: {}";
    private static final String LOG_VILLAGE_RECIPE_SUCCESS_FR = "Recettes de village injectées dans JEI, quantité: {}";

    private static final String LOG_VILLAGE_RECIPE_EMPTY_ZH = "村庄数据为空，跳过 JEI 村庄配方注入。";
    private static final String LOG_VILLAGE_RECIPE_EMPTY_EN = "Village list is empty, skipping JEI village recipe injection.";
    private static final String LOG_VILLAGE_RECIPE_EMPTY_FR = "La liste des villages est vide, injection ignorée.";

    private static final String LOG_VILLAGE_RECIPE_ERR_ZH = "注入村庄配方失败: {}";
    private static final String LOG_VILLAGE_RECIPE_ERR_EN = "Failed to inject village recipes: {}";
    private static final String LOG_VILLAGE_RECIPE_ERR_FR = "Échec de l'injection des recettes de village: {}";

    private static final String LOG_BUILDING_RECIPE_SUCCESS_ZH = "已向 JEI 注入建筑配方，数量: {}";
    private static final String LOG_BUILDING_RECIPE_SUCCESS_EN = "Injected building recipes to JEI, count: {}";
    private static final String LOG_BUILDING_RECIPE_SUCCESS_FR = "Recettes de bâtiment injectées dans JEI, quantité: {}";

    private static final String LOG_BUILDING_RECIPE_EMPTY_ZH = "建筑数据为空，跳过 JEI 建筑配方注入。";
    private static final String LOG_BUILDING_RECIPE_EMPTY_EN = "Building list is empty, skipping JEI building recipe injection.";
    private static final String LOG_BUILDING_RECIPE_EMPTY_FR = "La liste des bâtiments est vide, injection ignorée.";

    private static final String LOG_BUILDING_RECIPE_ERR_ZH = "注入建筑配方失败: {}";
    private static final String LOG_BUILDING_RECIPE_ERR_EN = "Failed to inject building recipes: {}";
    private static final String LOG_BUILDING_RECIPE_ERR_FR = "Échec de l'injection des recettes de bâtiment: {}";

    private static final String LOG_VILLAGER_RECIPE_SUCCESS_ZH = "已向 JEI 注入村民配方，数量: {}";
    private static final String LOG_VILLAGER_RECIPE_SUCCESS_EN = "Injected villager recipes to JEI, count: {}";
    private static final String LOG_VILLAGER_RECIPE_SUCCESS_FR = "Recettes de villageois injectées dans JEI, quantité: {}";

    private static final String LOG_VILLAGER_RECIPE_EMPTY_ZH = "村民数据为空，跳过 JEI 村民配方注入。";
    private static final String LOG_VILLAGER_RECIPE_EMPTY_EN = "Villager list is empty, skipping JEI villager recipe injection.";
    private static final String LOG_VILLAGER_RECIPE_EMPTY_FR = "La liste des villageois est vide, injection ignorée.";

    private static final String LOG_VILLAGER_RECIPE_ERR_ZH = "注入村民配方失败: {}";
    private static final String LOG_VILLAGER_RECIPE_ERR_EN = "Failed to inject villager recipes: {}";
    private static final String LOG_VILLAGER_RECIPE_ERR_FR = "Échec de l'injection des recettes de villageois: {}";
    // endregion 日志文本常量

    @Override
    public ResourceLocation getPluginUid() {return PLUGIN_ID;}

    // 接入 {@link MMLog} 三语控制台日志，对每个 Ingredient 注册阶段实施异常捕获与数量监控
    /**
     * 注册自定义 JEI 成分类型 {@link IModIngredientRegistration}。
     * 采用异常隔离机制，防止单一 Ingredient 注册抛错中断整个 JEI 初始化。
     * @param registration JEI 成分注册器
     */
    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        MMLog.info(LOG_INGREDIENTS_START_ZH, LOG_INGREDIENTS_START_EN, LOG_INGREDIENTS_START_FR);
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
            MMLog.info(LOG_CULTURE_ING_SUCCESS_ZH, LOG_CULTURE_ING_SUCCESS_EN, LOG_CULTURE_ING_SUCCESS_FR, cultures.size());
        } catch (Exception e) {
            MMLog.warn(LOG_CULTURE_ING_ERR_ZH, LOG_CULTURE_ING_ERR_EN, LOG_CULTURE_ING_ERR_FR, e.getMessage());
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
            MMLog.info(LOG_VILLAGE_ING_SUCCESS_ZH, LOG_VILLAGE_ING_SUCCESS_EN, LOG_VILLAGE_ING_SUCCESS_FR, villages.size());
        } catch (Exception e) {
            MMLog.warn(LOG_VILLAGE_ING_ERR_ZH, LOG_VILLAGE_ING_ERR_EN, LOG_VILLAGE_ING_ERR_FR, e.getMessage());
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
            MMLog.info(LOG_BUILDING_ING_SUCCESS_ZH, LOG_BUILDING_ING_SUCCESS_EN, LOG_BUILDING_ING_SUCCESS_FR, buildings.size());
        } catch (Exception e) {
            MMLog.warn(LOG_BUILDING_ING_ERR_ZH, LOG_BUILDING_ING_ERR_EN, LOG_BUILDING_ING_ERR_FR, e.getMessage());
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
            MMLog.info(LOG_VILLAGER_ING_SUCCESS_ZH, LOG_VILLAGER_ING_SUCCESS_EN, LOG_VILLAGER_ING_SUCCESS_FR, villagers.size());
        } catch (Exception e) {
            MMLog.warn(LOG_VILLAGER_ING_ERR_ZH, LOG_VILLAGER_ING_ERR_EN, LOG_VILLAGER_ING_ERR_FR, e.getMessage());
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        IIngredientManager ingredientManager = registration.getJeiHelpers().getIngredientManager();
        // [醒目新注释] 注册 VillageCategory 分类页面
        registration.addRecipeCategories(
                new CultureCategory(guiHelper, ingredientManager),
                new BuildingCategory(guiHelper,ingredientManager),
                new VillageCategory(guiHelper,ingredientManager),
                new VillagerCategory(guiHelper, ingredientManager),
                // 文化及其详细信息
                new CultureVillagesCategory(guiHelper,ingredientManager),
                // 建筑及其详细信息
                new BuildingVillageCategory(guiHelper,ingredientManager),
                new BuildingVillagerCategory(guiHelper,ingredientManager),
                new BuildingLevelCostCategory(guiHelper,ingredientManager),
                // 村庄及其详细信息
                new VillageBuildingsCategory(guiHelper,ingredientManager),
                new VillageVillagersCategory(guiHelper,ingredientManager),
                // 村民及其详细信息
                new VillagerVillageCategory(guiHelper, ingredientManager),
                new VillagerBuildingCategory(guiHelper, ingredientManager),
                new VillagerBringBackHomeCategory(guiHelper, ingredientManager),
                new VillagerCollectCategory(guiHelper, ingredientManager),
                new VillagerRequiredCategory(guiHelper, ingredientManager)
        );
        // [醒目新注释] 使用 {@link MMLog} 确认页签 Class 注册完成
        MMLog.info(LOG_CATEGORIES_SUCCESS_ZH, LOG_CATEGORIES_SUCCESS_EN, LOG_CATEGORIES_SUCCESS_FR);
    }

    // [醒目新注释] 引入 {@link MMLog} 三语埋点，并增加对配方集合的非空判断，避免向 JEI 注入空配方导致页签被自动隐藏
    /** 声明有配方的对象
     * @param registration JEI 配方注册器
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        MMLog.info(LOG_RECIPES_START_ZH, LOG_RECIPES_START_EN, LOG_RECIPES_START_FR);
        // 1. 文化配方注册
        try {
            ArrayList<Culture> cultures = new ArrayList<>(MillenaireAPIHelper.getAllCultureType().values());
            if (!cultures.isEmpty()) {
                registration.addRecipes(CultureCategory.RECIPE_TYPE, new ArrayList<>(cultures));
                registration.addRecipes(CultureVillagesCategory.RECIPE_TYPE, cultures);
                MMLog.info(LOG_CULTURE_RECIPE_SUCCESS_ZH, LOG_CULTURE_RECIPE_SUCCESS_EN, LOG_CULTURE_RECIPE_SUCCESS_FR, cultures.size());
            } else {
                MMLog.warn(LOG_CULTURE_RECIPE_EMPTY_ZH, LOG_CULTURE_RECIPE_EMPTY_EN, LOG_CULTURE_RECIPE_EMPTY_FR);
            }
        } catch (Exception e) {
            MMLog.warn(LOG_CULTURE_RECIPE_ERR_ZH, LOG_CULTURE_RECIPE_ERR_EN, LOG_CULTURE_RECIPE_ERR_FR, e.getMessage());
        }

        // 2. 村庄配方注册
        try {
            ArrayList<VillageType> villages = new ArrayList<>(MillenaireAPIHelper.getAllVillageType().values());
            if (!villages.isEmpty()) {
                registration.addRecipes(VillageCategory.RECIPE_TYPE, villages);
                registration.addRecipes(VillageBuildingsCategory.RECIPE_TYPE, villages);
                registration.addRecipes(VillageVillagersCategory.RECIPE_TYPE, villages);
                MMLog.info(LOG_VILLAGE_RECIPE_SUCCESS_ZH, LOG_VILLAGE_RECIPE_SUCCESS_EN, LOG_VILLAGE_RECIPE_SUCCESS_FR, villages.size());
            } else {
                MMLog.warn(LOG_VILLAGE_RECIPE_EMPTY_ZH, LOG_VILLAGE_RECIPE_EMPTY_EN, LOG_VILLAGE_RECIPE_EMPTY_FR);
            }
        } catch (Exception e) {
            MMLog.warn(LOG_VILLAGE_RECIPE_ERR_ZH, LOG_VILLAGE_RECIPE_ERR_EN, LOG_VILLAGE_RECIPE_ERR_FR, e.getMessage());
        }

        // 3. 建筑配方注册
        try {
            ArrayList<BuildingPlanSet> buildings =  new ArrayList<>(MillenaireAPIHelper.getAllBuildingPlanSets().values());
            if (!buildings.isEmpty()) {
                registration.addRecipes(BuildingCategory.RECIPE_TYPE, buildings);
                registration.addRecipes(BuildingVillageCategory.RECIPE_TYPE, buildings);
                registration.addRecipes(BuildingVillagerCategory.RECIPE_TYPE, buildings);
                List<BuildingPlanSet> buildingsA = buildings.stream()
                        .filter(b -> b.variants() != null && b.variants().size() > 0)
                        .toList();
                List<BuildingPlanSet> buildingsB = buildingsA.stream()
                        .filter(b -> b.variants().size() > 1)
                        .toList();
                List<BuildingPlanSet> buildingsC = buildingsB.stream()
                        .filter(b -> b.variants().size() > 2)
                        .toList();
                MMLog.info(LOG_BUILDING_RECIPE_SUCCESS_ZH, LOG_BUILDING_RECIPE_SUCCESS_EN, LOG_BUILDING_RECIPE_SUCCESS_FR, buildings.size());

                // BuildingPlans页面
                List<BuildingPlanSet.LevelDef> allLevelDefs = new ArrayList<>();
                for (BuildingPlanSet planSet : MillenaireAPIHelper.getAllBuildings()) {
                    if (planSet != null && planSet.variants() != null) {
                        for (List<BuildingPlanSet.LevelDef> levelList : planSet.variants().values()) {
                            if (levelList != null) {
                                allLevelDefs.addAll(levelList);
                            }
                        }
                    }
                }
                registration.addRecipes(BuildingLevelCostCategory.RECIPE_TYPE, allLevelDefs);

            } else {
                MMLog.warn(LOG_BUILDING_RECIPE_EMPTY_ZH, LOG_BUILDING_RECIPE_EMPTY_EN, LOG_BUILDING_RECIPE_EMPTY_FR);
            }
        } catch (Exception e) {
            MMLog.warn(LOG_BUILDING_RECIPE_ERR_ZH, LOG_BUILDING_RECIPE_ERR_EN, LOG_BUILDING_RECIPE_ERR_FR, e.getMessage());
        }

        // 4. 村民配方注册
        try {
            ArrayList<VillagerType> villagers = new ArrayList<>(MillenaireAPIHelper.getAllVillagerType().values());
            if (!villagers.isEmpty()) {
                registration.addRecipes(VillagerCategory.RECIPE_TYPE, villagers);
                registration.addRecipes(VillagerVillageCategory.RECIPE_TYPE, villagers);
                registration.addRecipes(VillagerBuildingCategory.RECIPE_TYPE, villagers);
                registration.addRecipes(VillagerBringBackHomeCategory.RECIPE_TYPE, villagers);
                registration.addRecipes(VillagerCollectCategory.RECIPE_TYPE, villagers);
                //registration.addRecipes(VillagerRequiredCategory.RECIPE_TYPE, villagers);
                List<VillagerType> requiredVillagers = villagers.stream().filter(v -> v.requiredGoods() != null && !v.requiredGoods().isEmpty()).toList();
                registration.addRecipes(VillagerRequiredCategory.RECIPE_TYPE, requiredVillagers);
                MMLog.info(LOG_VILLAGER_RECIPE_SUCCESS_ZH, LOG_VILLAGER_RECIPE_SUCCESS_EN, LOG_VILLAGER_RECIPE_SUCCESS_FR, villagers.size());
            } else {
                MMLog.warn(LOG_VILLAGER_RECIPE_EMPTY_ZH, LOG_VILLAGER_RECIPE_EMPTY_EN, LOG_VILLAGER_RECIPE_EMPTY_FR);
            }
        } catch (Exception e) {
            MMLog.warn(LOG_VILLAGER_RECIPE_ERR_ZH, LOG_VILLAGER_RECIPE_ERR_EN, LOG_VILLAGER_RECIPE_ERR_FR, e.getMessage());
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiRuntime = runtime;
    }
}
// endregion 插件主体定义与初始化