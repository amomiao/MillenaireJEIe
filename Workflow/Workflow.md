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
* Q20: JeiLinkBox的上下小按钮失效了
* Q21: 别编了，你要什么类的源码告诉我再写
  * `mezz.jei.api.gui.inputs.IJeiInputHandler`、`mezz.jei.api.gui.widgets.IRecipeExtrasBuilder`、`getArea() 的返回类型`
* Q22: 这下点了按钮光标可用性都不变了，你自己相关办法让JeiLinkBox在自己的区域里实现某个拓展显示吧，使用最稳妥的方案。
* Q23: 现在是有多少列多少，有限区域在哪，哪怕整个滚动条呢，这三个类你有完全修改权限
* Q24: 可惜你没法看视频不知道这个假滚动条多好笑，这个方案能实现，你再想办法给滚动条做成真的，JEI没有ScrollView吗
  * => 要做出支持鼠标滚轮 + 真实鼠标拖拽滑块的“真·滚动条”，最稳妥的工程解法是：固定 16 个物理槽位的 Hitbox 坐标，通过 IJeiInputHandler 监听滚轮与拖拽事件，动态切换槽位内的数据行映射（Slot Row Paging）。
* Q25: 那还是不如翻页，你再尝试一下，比如控制页码值，控制轮播，第一页锁第一播，第二页锁第二播...总之根本目标是在有限空间里列举出所有内容，做不到我们宁愿给个小按钮再打开一个大窗口。
  * => JEI 底层的 Slot Hitbox（槽位碰撞箱）是在 setRecipe 阶段一次性固化在全局树里的，
    * 在同一个配方卡片内试图通过自定义变量强行做“单卡片内翻页/轮播切换”，永远无法同步更新 JEI 内部的真实鼠标焦点，这也是为什么光标可用性与 R/U 键会失效。
    * 在 JEI 与 Minecraft 模组开发中，解决海量关联元素最地道、最符合官方规范的“大窗口方案”是：主卡片预览 + 触发 JEI 二级全景大窗口（Sub-Recipe Category）。
    * 在 JEI 与 Minecraft 模组开发中，解决海量关联元素最地道、最符合官方规范的“大窗口方案”是：主卡片预览 + 触发 JEI 二级全景大窗口（Sub-Recipe Category）。
* Q26: 背景框歪了，点击大窗口没出来，下面这个大按钮改成一个加号行放在八个图片右边作为第九个`+`
  * 还是木有事件，感觉是JEI的格子不能简单的就当按钮用，`+`这个框不用JEI的，随便渲染一个不就行吗，对了悬浮文本提示一个`查看全部`
* Q27: 没有消息，是不是这个鼠标浮在上面时发绿的框挡住了鼠标事件？总之就留一个可以检测鼠标射线的控件
* Q28: IRecipeSlotBuilder#addTooltipCallback被弃用了，并且你button==0是对的吗
* Q29: 还是有绿框，疑似阻碍鼠标事件，放个`DebugGuiUtils`里的`Button`能用吗？第9格你随便放个一定能触发的东西让我看看大页面。
  * 原生 Button 直接 new 在 draw() 里是不能用的。JEI 视图并不是独立的 Screen，在 draw 周期里临时创建的 Widget 不会被当前界面的 children() 监听捕获；而且如果在 setRecipe 中为第 9 格注册了 builder.addSlot()，JEI 的槽位（绿框）会直接抢占点击事件用于检索配方。
* Q30: 在这里面实现{指`JeiLinkBox`}，你写哪去了{`DebugGuiUtils`}
* Q31: 你真的在第九格用上了`细节`同款按钮了吗？都是一个窗口怎么这里就不行。 你别管原生按钮能不能用，你在右上角给我声明一个按钮点击之后客户端发个一，我倒要看看是不是真的不行。
* Q32: 上面的按钮能触发事件下面的不行根源在那里？不行就把这个按钮委托到给`CultureCategory`自己检测超过九个(下面的第九个按钮转成正常显示)触发事件，这总行了吧。
* Q21: 你向前努力回忆一下，把遇到的坑点整理一份回执，不需要长文总结，尽可能多的把每条问题指明`类和方法的坑点`，简单列举出来，比如`谁过时了`应该`换什么方法再实现`。