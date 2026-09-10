package com.momos.millenairejeimm.jei.Runtime.villageof.link;

import com.mojang.serialization.Codec;
import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import net.minecraft.resources.ResourceLocation;
import org.millenaire.culture.VillageType;

import java.util.Objects;

/**
 * 村庄-建筑全景分类路由 Payload 载体 {@link VillageBuildingLink}。
 * 用于解除 JEI Role 物理上限（4个），支撑精准全景分类跳转与自动聚焦。
 *
 * @param village 绑定的目标村庄类型 {@link VillageType}
 */
public record VillageBuildingLink(VillageType village) {
    // [醒目新注释] 参照 MillIngredientTypes 补充 Codec，用于解决 JEI 19.9.0+ 注册弃用报错
    public static final Codec<VillageBuildingLink> CODEC = ResourceLocation.CODEC.xmap(
            id -> new VillageBuildingLink(MillenaireAPIHelper.getAllVillageType().get(id)),
            link -> link.village() != null ? link.village().id() : ResourceLocation.fromNamespaceAndPath("millenaire", "unknown")
    );
    // [醒目新注释] 关键修复：重写 equals，强制基于 village.id() 比较，解决内存对象引用不一致导致 JEI 聚焦失败的问题
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VillageBuildingLink that = (VillageBuildingLink) o;
        if (this.village == null || that.village == null) return false;
        return Objects.equals(this.village.id(), that.village.id());
    }

    // [醒目新注释] 关键修复：重写 hashCode，与 equals 保持一致
    @Override
    public int hashCode() {
        return village != null && village.id() != null ? village.id().hashCode() : 0;
    }
}