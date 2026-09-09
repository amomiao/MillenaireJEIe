package com.momos.millenairejeimm;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.millenaire.culture.Culture;
import org.millenaire.culture.VillageType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Mod(MillenaireJeimm.MODID)
public class MillenaireJeimm {
    public static <S, T> List<T> filterByResourceLocation(
            S source,
            Collection<T> targets,
            Function<S, ResourceLocation> sourceIdExtractor,
            Function<T, ResourceLocation> targetIdExtractor) {
        if (source == null || targets == null || targets.isEmpty()
                || sourceIdExtractor == null || targetIdExtractor == null) {
            return List.of();
        }
        ResourceLocation sourceId = sourceIdExtractor.apply(source);
        if (sourceId == null) {
            return List.of();
        }
        // [醒目新注释] 使用 Stream 过滤非空项，并基于传入的两个提取器比较 ResourceLocation
        return targets.stream()
                .filter(Objects::nonNull)
                .filter(target -> sourceId.equals(targetIdExtractor.apply(target)))
                .toList();
    }

    public static List<VillageType> getVillageFormCulture(Culture cultrue){
        return filterByResourceLocation(cultrue,MillenaireAPIHelper.getAllVillages(),culture -> cultrue.id(),v->v.culture());
    }

    public static final String MODID = "millenairejeimm";
    public MillenaireJeimm(IEventBus modEventBus, ModContainer modContainer) {

    }
}