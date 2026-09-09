package com.momos.millenairejeimm;

import com.momos.millenairejeim.helper.MillenaireAPIHelper;
import com.momos.millenairejeimm.debug.DebugGuiUtils;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureCategory;
import com.momos.millenairejeimm.jei.type.JeiLinkBox;
import com.momos.millenairejeimm.jei.type.layout.CultureLinkLayout;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

    /// 1.0 注册:JEI页面对象`要素`(Ingredient)
    /// 1.1 编写一个[BuildingIngredientHelper]继承[IIngredientHelper] 用来描述数据
    /// 1.2 编写一个[BuildingIngredientRenderer]继承[BaseMillIngredientRenderer]继承[IIngredientRenderer] 用来描述渲染
    /// 1.3 通过[MillenaireJeimmPlugin#registerIngredients]调用上述类，完成`要素`(Ingredient)注册
    /// 2.0 注册:JEI页签对象`类别`(Category)
    /// 2.1 由于不需要额外的数据处理，可以把原数据直接给作为`Recipe`(配方)
    /// 2.1.1 因此我们可以仅声明: [BuildingCategory]继承[BaseMillCategory]继承[IRecipeCategory]
    /// 2.2 对象的Json数据通过[DebugGuiUtils]管理，其中启动窗口的方法是[DebugGuiUtils#openJsonDumpScreen(Object)], 调用者是[BaseMillCategory#createRecipeExtras(IRecipeExtrasBuilder, Object, IFocusGroup)]
    /// 3.0 [JeiLinkBox]是用来解决`渲染对象较多`与`JEI窗口较小之间的矛盾`, 使用一个渲染一行8个对象，对象更多时进行`二级页面查看`;
    /// 3.1 [JeiLinkBox]声明的样式是`一标题`加`一行内容`，而各个`页签(Category)`可能会需要封装或处理， 所以实装到时应该封装一下，也就是[CultureLinkLayout]
    /// 3.1.1 自定义渲染:[CultureLinkLayout#render] 渲染的JEI页面对象`要素`(Ingredient)
    /// 3.1.2 自定义事件:受限于API,`按钮事件`无法委托给[CultureLinkLayout]所以由`页签`(Category)自己处理,在[CultureCategory#createRecipeExtras]
    /// 注意在[JeiLinkBox#executeClickAction]有一个退化逻辑, 在[JeiLinkBox#PREVIEW_SLOTS]要求只能为8个对象赋值，而[CultureLinkLayout#createRecipeExtras]第9个空位就是`唤起二级整合窗口的入口`
    public MillenaireJeimm(IEventBus modEventBus, ModContainer modContainer) {

    }
}