package com.momos.millenairejeimm;

import com.momos.millenairejeimm.debug.DebugGuiUtils;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureVillagesCategory;
import com.momos.millenairejeimm.jei.type.JeiLinkBox;
import com.momos.millenairejeimm.jei.type.layout.CultureLinkLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(MillenaireJeimm.MODID)
public class MillenaireJeimm {
    public static final String MODID = "millenairejeimm";

    /// 如果改动内容在`com.momos.millenairejeim`(比当前少一个`m`)那么要确保`替换前置jar`到最新版本
    /// 1.0 注册:JEI页面对象`要素`(Ingredient)
    /// 1.1 编写一个[BuildingIngredientHelper]继承[IIngredientHelper] 用来描述数据
    /// 1.2 编写一个[BuildingIngredientRenderer]继承[BaseMillIngredientRenderer]继承[IIngredientRenderer] 用来描述渲染
    /// 1.3 通过[MillenaireJeimmPlugin#registerIngredients]调用上述类，完成`要素`(Ingredient)注册
    /// ================
    /// 2.0 注册:JEI页签对象`类别`(Category)
    /// 2.1 由于不需要额外的数据处理，可以把原数据直接给作为`Recipe`(配方)
    /// 2.1.1 因此我们可以仅声明: [BuildingCategory]继承[BaseMillCategory]继承[IRecipeCategory]
    /// 2.2 对象的Json数据通过[DebugGuiUtils]管理
    /// 2.2.1 其中启动窗口的方法是[DebugGuiUtils#openJsonDumpScreen], 调用者是[BaseMillCategory#createRecipeExtras]，因此重写时要回调`super`
    /// ================
    /// 3.0 [JeiLinkBox]是用来解决`渲染对象较多`与`JEI窗口较小之间的矛盾`, 使用一个渲染一行8个对象，对象更多时进行`二级页面查看`;
    /// 3.1 [JeiLinkBox]声明的样式是`一标题`加`一行内容`，而各个`页签(Category)`可能会需要封装或处理， 所以实装到时应该封装一下，也就是[CultureLinkLayout]
    /// 3.1.1 自定义渲染:[CultureLinkLayout#render] 渲染的JEI页面对象`要素`(Ingredient)
    /// 3.1.2 自定义事件:受限于API,`按钮事件`无法委托给[CultureLinkLayout]所以由`页签`(Category)自己处理,在[CultureCategory#createRecipeExtras]
    /// 3.1.3 在[JeiLinkBox#executeClickAction]有一个退化逻辑
    /// 3.1.4 在[JeiLinkBox#PREVIEW_SLOTS]钳制[JeiLinkBox#setRecipe]只能为最多8个对象赋值`int limit = Math.min(total, PREVIEW_SLOTS)`
    /// 3.1.5 在[JeiLinkBox#setOnPlusClick]注册点击事件，开启二级窗口
    /// 3.1.6 点击事件的调用位置在[CultureCategory#CultureCategory]构造方法
    /// 3.1.6.1 赋值[CultureCategory#villageBox]的方法是链式调用中包含[JeiLinkBox#setOnPlusClick]为其提供一个事件`void onClick(Object contextObject, List<T> items)`
    /// 3.2 事件触发后会展开`页签`(Category)[CultureVillagesCategory]，注意这个页签也必须在[MillenaireJeimmPlugin#registerCategories]和[MillenaireJeimmPlugin#registerRecipes]中声明
    public MillenaireJeimm(IEventBus modEventBus, ModContainer modContainer) {

    }
}