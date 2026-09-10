package com.momos.millenairejeimm;

import com.momos.millenairejeimm.debug.DebugGuiUtils;
import com.momos.millenairejeimm.jei.MillIngredientTypes;
import com.momos.millenairejeimm.jei.MillenaireJeimmPlugin;
import com.momos.millenairejeimm.jei.Runtime.BaseMillIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.buildingof.BuildingCategory;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientHelper;
import com.momos.millenairejeimm.jei.Runtime.buildingof.ingredient.BuildingIngredientRenderer;
import com.momos.millenairejeimm.jei.Runtime.BaseMillCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureCategory;
import com.momos.millenairejeimm.jei.Runtime.cultureof.CultureVillagesCategory;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkBox;
import com.momos.millenairejeimm.jei.Runtime.JeiLinkLayout;
import com.momos.millenairejeimm.jei.Runtime.villageof.VillageBuildingsCategory;
import com.momos.millenairejeimm.jei.Runtime.villageof.VillageCategory;
import com.momos.millenairejeimm.jei.Runtime.villageof.VillageVillagersCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.millenaire.building.BuildingPlanSet;
import org.millenaire.culture.VillageType;

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
    /// 3.1 [JeiLinkBox]声明的样式是`一标题`加`一行内容`，而各个`页签(Category)`可能会需要封装或处理， 所以实装到时应该封装一下，也就是[JeiLinkLayout]
    /// 3.1.1 自定义渲染:[JeiLinkLayout#render] 渲染的JEI页面对象`要素`(Ingredient)
    /// 3.1.2 自定义事件:受限于API,`按钮事件`无法委托给[JeiLinkLayout]所以由`页签`(Category)自己处理,在[CultureCategory#createRecipeExtras]
    /// 3.1.3 在[JeiLinkBox#executeClickAction]有一个退化逻辑
    /// 3.1.4 在[JeiLinkBox#PREVIEW_SLOTS]钳制[JeiLinkBox#setRecipe]只能为最多8个对象赋值`int limit = Math.min(total, PREVIEW_SLOTS)`
    /// 3.1.5 在[JeiLinkBox#setOnPlusClick]注册点击事件，开启二级窗口
    /// 3.1.6 点击事件的调用位置在[CultureCategory#CultureCategory]构造方法
    /// 3.1.6.1 赋值[CultureCategory#villageBox]的方法是链式调用中包含[JeiLinkBox#setOnPlusClick]为其提供一个事件`void onClick(Object contextObject, List<T> items)`
    /// 3.2 事件触发后会展开`页签`(Category)[CultureVillagesCategory]，注意这个页签也必须在[MillenaireJeimmPlugin#registerCategories]和[MillenaireJeimmPlugin#registerRecipes]中声明
    /// ================
    /// 4.0 一级页签到二级页签的逻辑
    /// [过时] ~~4.1~~ 构建一个`LinkLayout`为`页签`(Categor)封装一份渲染对象: 审阅[JeiLinkLayout]没什么难点
    /// 4.1 受限于固定的宽度，[JeiLinkBox#PREVIEW_SLOTS]是常量=8，他在[JeiLinkBox#setRecipe]中声明了八个`JEI格`;
    /// 4.1.1 第九个格子不是`JEI格`，仅在[JeiLinkBox#registerPlusButtonInputHandler]中声明了他的位置，这样做的原因是`原生的单元格`对`事件的处理`更加强大,可以支持`任意的点击事件`;
    /// 4.1.2 [JeiLinkBox#render]中使用了文字渲染了一个`"§a+"`
    /// 4.1.3 其下的`guiGraphics.fill(plusX, startY, plusX + 18, startY + 18, btnBg);`,这个按钮是一个`gui图`添加了按钮的行为构成的,并不是一个`显式的Button控件`
    /// 4.1.3.1 按钮在[JeiLinkBox#registerPlusButtonInputHandler]中监听了(18x18)的小区域的点击, 触发[JeiLinkBox#executeClickAction]
    /// 4.1.3.2 点击正常会触发[JeiLinkBox#customClickListener],这个变量由外部调用,回到[3.1.6.1]
    /// 4.1.3.3 退化逻辑是`点击输出'1'`
    /// 4.2 [JeiLinkLayout]与[JeiLinkBox]的使用方法:为Layout传入Box,管理布局
    /// [过时] 现有[CultureLinkLayout]这样的单独实现被整体封装[JeiLinkLayout],通过`变长参数`实现多态,但与旧的实现方式无语义差异
    /// (更新) [JeiLinkLayout]增加一个设定，渲染时长度为0不渲染(默认)，`bool`[JeiLinkLayout#hideEmptyBoxes]控制此行为
    /// 4.3 [JeiLinkBox]的声明,以[VillageCategory#VillageCategory]为例
    /// 4.3.1 构造参数[IGuiHelper]`guiHelper`,JEI的GUI绘图工具
    /// 4.3.2 构造参数[IIngredientManager]`ingredientManager`,JEI`要素`(Ingredient)管理器
    /// 4.3.3 `this.buildingBox= new JeiLinkBox<BuildingPlanSet>("村庄包含建筑")` => 赋值[JeiLinkBox] 这个Box标题为'村庄包含建筑',存储一些[BuildingPlanSet]
    /// 4.3.4 `.useJeiMode(ingredientManager, MillIngredientTypes.BUILDING)` => 构造后链式调用[JeiLinkBox#useJeiMode],从管理器类中取用所有属于[MillIngredientTypes#BUILDING]的`JEI`要素(Ingredient)
    /// 4.3.5 `.setOnPlusClick((contextObject, items) -> { ... })` => 注册第九格监听[3.1.6.1],其中
    /// 4.3.5.1 (contextObject)是事件调用者[VillageCategory]实例
    /// 4.3.5.2 (items)是[JeiLinkBox#targetList]，赋值入口在[VillageCategory#setRecipe]时,触发的[JeiLinkLayout#setRecipeForBox]
    /// 4.3.6 [VillageCategory#VillageCategory] => `if (MillenaireJeimmPlugin.JeiRuntime != null && contextObject instanceof VillageType village)` => 添加判断,保险逻辑通常不会空
    /// 4.3.7 `var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.RENDER_ONLY, MillIngredientTypes.VILLAGE, village);`
    /// 4.3.7.1 (var focus) 构筑一个检索焦点, 作用是跳转过去之后页面会默认转到`焦点对象`
    /// [过时]4.3.7.2 (getJeiHelpers().getFocusFactory().createFocus(...)) 转到焦点构建的方法上
    /// 4.3.7.3 最重要的`.createFocus(RecipeIngredientRole.RENDER_ONLY, MillIngredientTypes.VILLAGE, village)`
    /// 4.3.7.3.1 [RecipeIngredientRole]是一个`枚举`,控制页签的流向,如果多次[JeiLinkBox#setOnPlusClick]声明时为两个`.createFocus`赋值了同一类型，那么就会唤起两组数据并且使得聚焦失败，超过4个值时逻辑垮塌(:D因为枚举只有四个值)
    /// 4.3.7.3.1 且这些值不是在这里作为自变量的，而是受制于对应的[VillageBuildingsCategory#setRecipe]中调用的`builder.addInvisibleIngredients(RecipeIngredientRole.RENDER_ONLY).addIngredient(MillIngredientTypes.VILLAGE, recipe);`
    /// 4.3.7.3.1.1 [RecipeIngredientRole#INPUT] 以U的方式检索对象
    /// 4.3.7.3.1.2 [RecipeIngredientRole#OUTPUT] 以R的方式检索对象
    /// 4.3.7.3.1.3 [RecipeIngredientRole#CATALYST] 以用法(工作台/熔炉/...)的方式检索对象
    /// 4.3.7.3.1.4 [RecipeIngredientRole#RENDER_ONLY] 以只渲染的方式检索就行
    /// 4.3.7.3.2 [MillIngredientTypes#VILLAGE] 告知页面准备为`VILLAGE`下的所有JEI`要素`(Ingredient)提供列举(就是页签内有很多页可以翻)
    /// 4.3.7.3.3 (village) 参数3位于参数2的集合里时, 页面会自己翻页到`参数3`的所在页面
    /// (更新)4.3.7.2b 优化`.createFocus`: 难以为继的根本原因是
    /// 4.3.7.2b.1 如[VillageCategory]:希望展示的两个子页面[VillageBuildingsCategory#setRecipe]和[VillageVillagersCategory#setRecipe]都调用了
    ///     builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addIngredient([MillIngredientTypes#VILLAGE], recipe);
    ///     builder.addInvisibleIngredients(RecipeIngredientRole.RENDER_ONLY).addIngredient([MillIngredientTypes#VILLAGE], recipe);
    ///     也就是把自己注册为了[MillIngredientTypes#VILLAGE]的查询结果
    ///     让他们与[MillIngredientTypes#VILLAGE]脱钩，在更高层隔离`查询作用域`，就能解决查询焦点的复杂情况
    ///
    /// 4.3.8 `MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(focus);` 传入焦点，调用`Jei运行时`，产生页面跳转。
    public MillenaireJeimm(IEventBus modEventBus, ModContainer modContainer) {

    }
}