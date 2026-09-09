# 第一批文件
* `MillIngredientTypes`
* `BaseMillIngredientRenderer`
* `BuildingIngredientHelper` `BuildingIngredientRenderer`
* `CultureIngredientHelper` `CultureIngredientRenderer`
* `VillagerIngredientHelper` `VillagerIngredientRenderer`
* `MillenaireJeimmPlugin`
# 开始
* Q1: 给文化生成一个对应页面(对应类)
* 得到`CultureCategory`，修改
* Q2: `CultureCategory` 按这样，写建筑(对应类)
* Q3: `CultureCategory` 按这样，写村民，获取村民临时走`ModCultures.getAllVillagerTypes().values()`(对应类)
* Q4: 他们字段里应该都有个类似旅行书图标语义的东西，有的话jei图标换成那个
* Q5: 文化用："map_icon": "millenaire:village_norman" 建筑用："icon": "minecraft:glass_bottle"
* Q6: 把缺失的`村庄类型`补上
  * `MillIngredientIconResolver`和`MillIconOnlyRenderer`管理图标渲染
* Q7: 这个JEI页面尽可能多的去渲染一些内容，拆出来就是倾向于开发者Debug用的，有必要的话可以直接占一页，一页不够就通过悬浮或者页中页拓展。四类对象都要这么搞，你可以根据现有内容提取/预设一些控件。
  * JEI最大标准尺寸（160×140 像素）
* Q8: 调用了虚空API，给API内容
* Q9: 我服了这谷歌没两句话又失忆，给你源码是改这份代码
* Q10: 改一下，每页页签就只有查询对象自己，直接占满页签，然后Debug信息最直接给到icon-标题行，先不用生成代码，告诉我增加一个按钮打开一个巨大的预览页面查看JsonDump简单吗？
* Q11：`DebugGuiUtils.JsonDumpScreen`模糊，是别的东西覆盖了一层并且是在文本框和按钮渲染时机之间
  * 真凶就是 super.render() 内部隐式调用的第二次 renderBackground()
* Q12: 这四个页签的单元格有统一声明的地方吗？ 
  * 总之设定一个规则，每个页签就是占满一整页了，未来可能还有新的信息想要写，然后不要设置`|`同行了都换行，标记这样可能会很长的可变行另起一行，增加悬浮，Json按钮改叫细节，放置在现在ID行到类别新起行之间。
  * 能搭起通用区域优先通用，部分通用可以拆成组件/方法，最后再每个重新自己。
* Q13：Inspector信息没传进来啊，你还记得要传什么吗，忘了我去找找
* Q14：多渲染几个框，是相关链接，总之框先声明缺少的参数后面再补，下面括号指的是框从上到下的顺位，合理安排一页JEI的剩余的空间，相关连接里面都是JEI触发对象
  * 总之每个一两行是能排开的，内部排不开的情况我们之后再讨论，你可以预设一个可以简易更换的方案
  * 文化关联村庄(1 数量级10)
  * 村庄关联文化(1 值唯一但和其他组件一样框起来渲染吧 数量级1)、村庄内建筑(2 数量级10)
  * 建筑关联所属村庄(1 数据级5)、村民(2 数量级5)、两级可翻页页签(高层是建筑变体，低层是建筑升级消耗，总消耗 数据级5-20)
  * 村民关联文化(1 数量级1)、村庄(2 数量级5)、建筑(3 数量级5)
* Q15: 高225像素在x5下超长，压缩到180，不再关联文化
* 换回了225，180压缩之后还是超长没辙
* Q16: 现在的页签没占满垂直空间，会剧中，能让页签在尽量往上贴吗
* Q17: Category是`单例对象`且`draw方法`每帧调用，数据调用应该在`setRecipe`中进行
* Q18: 使用你新写的方法让`JeiLinkBox`为控件默认实现JEI原版的RU点击事件
* Q19: RU点击事件没触发
  * 在 JEI 的交互架构中，IRecipeCategory.handleInput 是接收不到原生 R/U 键事件的。  
  * 当玩家在 JEI 界面中按下 R 键、U 键或 A 键（添加书签）时，JEI 内部的 RecipesGui 会在最顶层优先拦截按键，并主动调用 IRecipeCategory.getIngredientUnderMouse(...)：
  * 若该方法返回了有效成分，JEI 会自动接管并打开配方/用途/添加书签（且能完美兼容玩家自定义的按键绑定）。
  * 若未实现该方法（默认返回 Optional.empty()），JEI 会判定当前鼠标悬停区域为“空白背景”，直接截断按键事件，导致事件根本不会下发到 handleInput 中。  
  * 必须重写 JEI 的 getIngredientUnderMouse 接口，以下是适配后的完整代码实现。