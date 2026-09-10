package com.momos.millenairejeimm.jei;

import com.mojang.serialization.Codec;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.Culture;
import org.millenaire.culture.ModCultures;
import org.millenaire.culture.VillageType;
import org.millenaire.culture.VillagerType;

/**
 * 千年百科 JEI 自定义成分类型 {@link IIngredientType} 与 {@link Codec} 编解码器注册表。
 */
public final class MillIngredientTypes {
    public static final IIngredientType<Culture> CULTURE = () -> Culture.class;
    public static final IIngredientType<BuildingPlanSet> BUILDING = () -> BuildingPlanSet.class;
    public static final IIngredientType<VillagerType> VILLAGER = () -> VillagerType.class;
    public static final IIngredientType<VillageType> VILLAGE = () -> VillageType.class;

    /** 文化 {@link Culture} 的 JEI 序列化编解码器 */
    public static final Codec<Culture> CULTURE_CODEC = ResourceLocation.CODEC.xmap(
            ModCultures::getCulture,
            Culture::id
    );

    /** 村庄类型 {@link VillageType} 的 JEI 序列化编解码器 */
    public static final Codec<VillageType> VILLAGE_CODEC = ResourceLocation.CODEC.xmap(
            id -> MillenaireAPIHelper.getAllVillageType().get(id),
            VillageType::id
    );

    /** 建筑集 {@link BuildingPlanSet} 的 JEI 序列化编解码器 */
    public static final Codec<BuildingPlanSet> BUILDING_CODEC = Codec.STRING.xmap(
            id -> MillenaireAPIHelper.getAllBuildingPlanSets().get(id),
            building -> {
                String culturePath = building.culture() != null ? building.culture().getPath() : "unknown";
                return culturePath + ":" + building.buildingId();
            }
    );

    /** 村民类型 {@link VillagerType} 的 JEI 序列化编解码器 */
    public static final Codec<VillagerType> VILLAGER_CODEC = ResourceLocation.CODEC.xmap(
            id -> MillenaireAPIHelper.getAllVillagerType().get(id),
            VillagerType::id
    );
    private MillIngredientTypes() {}
}