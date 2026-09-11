# 第一批文件
* `MillIngredientTypes`
* `BaseMillIngredientRenderer`
* `BuildingIngredientHelper` `BuildingIngredientRenderer`
* `CultureIngredientHelper` `CultureIngredientRenderer`
* `VillagerIngredientHelper` `VillagerIngredientRenderer`
* `MillenaireJeimmPlugin`
# 重要记录
* 注解`@SuppressWarnings("removal")`可以屏蔽
  1. [IRecipeCategory#handleInput]被`JEI 19.x+废弃移除`
    * 替代方案是同类成员`default void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses)`
    * 通过[IRecipeExtrasBuilder#addInputHandler]`void addInputHandler(IJeiInputHandler var1)`向配方面板挂载声明式的`IJeiInputHandler`
      ```java
      /// 重写 [#createRecipeExtras] 以替代废弃的 `handleInput`。
      /// 使用具备区域定义 [ScreenRectangle] 的 [IJeiInputHandler] 与包含 [IJeiUserInput] 的点击方法。
      @Override
      public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
          builder.addInputHandler(new IJeiInputHandler() {
              @Override
              public ScreenRectangle getArea() { return new ScreenRectangle(3, 26, 22, 10);}
              @Override
              public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
                  InputConstants.Key key = input.getKey();
                  if (key.getType() == InputConstants.Type.MOUSE && key.getValue() == 0) {
                      // 统一处理图标正下方按钮坐标 (3, 26) 的点击响应
                      if (DebugGuiUtils.handleDetailsButtonClick(recipe, mouseX, mouseY, 3, 26)) {
                          return true;
                      }
                  }
                  return false;
              }
          });
      }
      ```
  2. [IRecipeExtrasBuilder#addTooltip]已被取缔
    * 由组件化的 IRecipeWidget 统一管理，或者交由 UI 渲染阶段的 GuiGraphics 原生处理
    * 最轻量且不依赖额外的 IRecipeWidget 封装的方式是：移除 createRecipeExtras 中的 addTooltip 注册，直接在 render 方法的悬浮判断中通过 guiGraphics.renderComponentTooltip 绘制 Tooltip。
      ```text
       boolean isHovered = mouseX >= plusX && mouseX <= plusX + 18 && mouseY >= startY && mouseY <= startY + 18;
       // JEI 19+ 替代方案：在 render 阶段判断悬浮并使用原生 GuiGraphics 绘制 Component 悬浮提示
       if (isHovered) {
           guiGraphics.renderComponentTooltip(
                   font,
                   List.of(
                           Component.literal("§a查看全部/深度诊断 (" + totalSize + " 个)"),
                           Component.literal("§7单击以打开 JSON 诊断大窗口")
                   ),
                   (int) mouseX,
                   (int) mouseY
           );
       }
      ```
  3. [IJeiRuntime#getFocusFactory] 没有`方法`
    * 应该是[IJeiHelpers#getFocusFactory]即`IJeiRuntime#getJeiHelpers#getFocusFactory`
  4. [IJeiRuntime#getRecipeManager#getRecipeCategory#ifPresent] 不能`ifPresent`,返回值错误
    * 直接对`IJeiRuntime#getRecipeManager#getRecipeCategory`的`返回值`判断`!=null`
  5. [IRecipeLayoutBuilder#addSlot#addIngredient#setInvisible] 没有方法
    * 被`IRecipeLayoutBuilder#addInvisibleIngredients`的`addIngredient`取缔
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
* Q33: 设计一个小回调或是参数，我们还是把`CultureCategory`外部的这些按钮和悬浮监听位置在JeiLinkBox/CultureLinkLayout里一起注册一下，哪怕只是参数，因为他们在逻辑上应该是一体的，只不过API没有提供接口。
* Q34: 我还要怎么说，你为什么监听到细节去了，我要知道的是为什么CultureCategory打不开，现在全乱套了。 第九格是细节窗口，细节按钮坏了
* Q34: 我说呢，你把事件绑错了，这个细节才是打开debug大窗口（DebugGuiUtils静态类）的，加号负责打开JEI大窗口（CultureVillagesCategory），绑对以后可能还打不开给CultureVillagesCategory窗口的开启相关上下文加入客户端聊天栏事件debug
* Q35: 你听懂什么，第九格的事件是可以正常触发的，我要你去监听第九个的点击事件尝试打开CultureVillagesCategory
* Q36: 我问你，打开窗口的事件你写在哪里了，给出对应的哪一行
* Q37: 我擦你小子真行，给openJsonDumpScreen特化窗口扔到人家JeiLinkBox里当退化结果，你把退化结果改成客户端消息：安全输出`contextObject`的前几十个字符
* Q38: 我去，走`CultureCategory`错构造方法了，根本没给jeiRuntime赋值，你说到底要那个构造方法，还是三参？
* Q39: createRecipeExtras必须在BaseMillIngredientRenderer有声明，细节按钮的点击要调用`DebugGuiUtils#openJsonDumpScreen(Object)`事件
* Q40: 你写什么，我给错了,createRecipeExtras必须在BaseMillCategory有声明，细节按钮的点击要调用`DebugGuiUtils#openJsonDumpScreen(Object)`事件
* AI一直在尝试`第九格`打开`DebugGuiUtils.openJsonDumpScreen`，但是应该打开的是`CultureVillagesCategory`
* Q41: 在`registerInputHandler`单独声明一个监听第九格被点击事件方法 
* Q42: 你加一个被点击客户端输出1给我看看
* Q43: 我说怎么找不到哪里注册事件，我让你加事件监听是让外部传入的，this.villageBox = new JeiLinkBox的时候注册一个第九格监听输出123456
* Q44: 点击按钮没反应，并且下面的dispalyClientMessage也没反应，方法里增加多个displayClientMessage全路径测试看看到底是为什么
  ```java
  LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
        player.displayClientMessage(Component.literal("123456"), false);
  }
  ```
* Q45: 增加颗粒度
  * JEI 没有找到名为 millenaire:culture_villages_full 的 Category 注册记录。
* 我草
* Q46: 页面展开了回收debug吧，但是还有问题，新的页面没有进入聚焦到对应对象的页签上
* Q47: 重写的createRecipeExtras调用了super为什么父类中的按钮事件还是不触发了
  * 子类 Handler 强行截断了事件传播（Event Interception）
  * 代码残余，删除无用代码即可
* Q48: super在上为什么不是上拦下而是下拦上？
  * super.createRecipeExtras(...) 控制的是注册顺序（谁先加入事件列表），而 GUI 框架在处理事件分发时，采用的是“后进先出 / 后者居上”（LIFO / Z-Index 优先）的触发机制。
* Q49: 还是点不开DebugGuiUtils#openJsonDumpScreen调用的窗口，严查
  * JEI 输入事件与客户端 GUI 主线程切换的调度冲突 以及 忽略了 JEI 的模拟预测阶段 (input.isSimulated())
* Q50: 又想害我，使用if (key.getType() == InputConstants.Type.MOUSE && key.getValue() == 0)和input.isSimulate()，并且我输出1也不会给我退化成默认打开那个窗口。
* 没救了换对话
* Q51: BaseMillCategory#createRecipeExtras中的按钮没有触发DebugGuiUtils#openJsonDumpScreen(Object)的窗口，但是CultureCategory#createRecipeExtras注册的成功调用了
  * JEI 的 IRecipeExtrasBuilder#addInputHandler 中，IJeiInputHandler#getArea() 已经向 JEI 注册了配方相对坐标范围 new ScreenRectangle(3, 26, 22, 10)，
  * JEI 内部会自动进行点击碰撞检测，只有点击命中该区域时，JEI 才会调用 handleInput 方法。
  * 但 JEI 传入 handleInput(double mouseX, double mouseY, ...) 的 mouseX 与 mouseY 参数是屏幕绝对坐标（例如 X=480.0, Y=320.0）。 
  * BaseMillCategory 中的二次判断恒为 false
  * DebugGuiUtils.isMouseOver(mouseX, mouseY, 3, 26, ...) 进行二次校验。
  * 这里拿屏幕绝对坐标（如 480）去和配方相对坐标（3 至 25）做比较，导致条件判定恒为 false，使得 DebugGuiUtils#openJsonDumpScreen 永远不会被触发。
  * 只需移除 BaseMillCategory#createRecipeExtras 中冗余且坐标系不匹配的 DebugGuiUtils.isMouseOver 判断即可
* Q52: 尝试实现`关联查询`内容
* Q53: [MillenaireAPIHelper#MappingVillagerBuilding]和[filterByResourceLocation]怎么没用？其中[filterByResourceLocation]为了封装可以进行一定修改
* Q54: 把这段内容`// endregion 物品与货币系统 API`和`// region 交易` API之间
* Q55: 你改我API名了？
* Q56: 无罪释放
* Q57: 为`VillageCategory`补充如`CultureVillagesCategory`的内容，村庄需要展示建筑与村民,
  * `MillenaireJeimm`中有帮助性内容
  * 数据源自`MillenaireAPIHelper#getBuildingFormVillage`和`MillenaireAPIHelper#getVillagerFormVillage`
// 页签调用Bug流程起点
* Q58: 聚焦了但是只聚焦了一半，与之关联的四个页签出来了，但是比如点击`村庄的村民`的加号，默认指向了第一个文化页签
* Q59: 应该指向的页签对象在这里面，你看看是不是简单的填错了对象
  * 确实是传入 JEI 的 RecipeType 列表顺序或目标 Category 对象填错导致的。 
  * 原因诊断 
    * 当调用 JEI 的 IJeiRuntime.getRecipesGui().showTypes(List<RecipeType<?>> recipeTypes) 打开多页签全景视图时，JEI 的底层逻辑是默认激活并展示传入 List 中索引为 0（第 1 个）的 RecipeType。
    * 如果各个 + 按钮在触发跳转时，传给 JEI 的都是同一个静态 List（例如 [文化, 建筑, 物品, 村民]），且 CULTURE_TYPE（文化页签）固定排在第 0 位，JEI 就会成功加载全部 4 个关联页签，但无论点哪个 + 号都必定默认停留于第 1 个文化页签。 
  * 解决方案 
    * 在 + 按钮的点击事件响应代码中，需要将该按钮真正对应的目标 RecipeType（如村民大窗口页签）调整到传入 List 的第 0 位（首位）。
* Q60: 哦还没给你上下文
* Q61: 额，原来另一个会话的你偷懒了，借用了别的模块的单元，声明村庄自己的LinkLayout
* Q62: 别光顾着移植啊，把没有聚焦的bug修复了啊
  * => `VillageCategory`构造方法中,告知`new JeiLinkBox`时调用`setOnPlusClick`指向`MillenaireJeimmPlugin.JeiRuntime.getRecipesGui().show(List.of(focusInput, focusOutput));`
* Q63: 页签就在新页面里你为什么就是聚焦不了呢？而且为什么有好几个页签，人家文化点出来就一个
* Q64: 类都在你手里，你想怎么改怎么改
  * 解决方案
  * 无需修改两个全景大窗口类（它们内部通过 addInvisibleIngredients 注册的 INPUT 槽位已经完全正确），只需要修正 VillageCategory.java 中的点击事件：
  * 点击“村庄包含建筑”时，直接使用列表中的第一个 BUILDING (INPUT) 发起 Focus；点击“村庄出现村民”时，使用第一个 VILLAGER (INPUT) 发起 Focus。 
  * 这样 JEI 只能匹配到对应的唯一全景分类，顶上有且仅有 1 个页签，且直接高亮当前村庄。
* Q65: 多个页签聚焦点击+对应的页签难吗？
* Q66: 村民的事件调用错了，打开的是村庄自己的这个窗口
* Q67: 页签聚焦了，但是又聚焦不到页签里的对象了，output不能删，你解决一下，整体结构可以改
  * 此前点击 + 后“页签能切过去，但页面里的对象取消了高亮/聚焦”的根本原因是：调用 recipesGui.showTypes(...) 会直接清空 JEI 刚刚建立的 Focus 检索状态。showTypes 仅用于无过滤的分类展示，它覆盖掉了对象的筛选上下文。  为了在绝对保留 VillageCategory 中 VillageType 的 OUTPUT 槽位前提下，同时实现 “直接切到目标全景大窗口” + “精准高亮当前村庄对象” + “顶栏只有一个目标页签”，架构层面的最佳解法是利用 CATALYST（催化剂）角色协同检索。
  * VillageCategory (保持 OUTPUT 不变)：村庄图标槽位继续保留 RecipeIngredientRole.OUTPUT。  点击 + 发起 Focus 时，使用 RecipeIngredientRole.CATALYST 角色标记当前 VillageType。由于 VillageCategory 本身没注册 CATALYST 槽位，JEI 会自动排除当前页面，不再原地跳回。  结合 VILLAGER / BUILDING 的 INPUT 焦点，精准剔除另一个不相干的全景大窗口，使顶部页签数量降为 1 个。  全景大窗口（VillageVillagersCategory & VillageBuildingsCategory）：将 addInvisibleIngredients 扩充为包含 INPUT, OUTPUT, CATALYST 全角色。无论外部以何种 Focus 角色传入，JEI 都能成功匹配并高亮锁定对应的村庄对象。 
* Q68: 没生效，但是别慌我有一份生效了的文件，大概可以借鉴
* Q69: 你再去修改一下VillageBuildingsCategory和VillageVillagersCategory看看你是真会了吗
* Q70: 全乱套了怎么办，不仅没解决问题原本修好的单页签现在也变成双页签了
* Q71: 我去你在改什么，Culture是好是，我给你代码是让你参照着去修改VillageBuildingsCategory、VillageVillagersCategory、VillageCategory体系下对象的
* 这个对话也毁了，next
* Q72: VillageCategory系列代码存在layout事件触发后页签有两个，且既没有对焦到正确的页签，页签里也是全部的内容问题。 而CultureCategory没有这些问题是正常的，你修复一下，一定记住是修改VillageCategory系列，CultureCategory系列是好的，你只读的。
  * 问题出在 JEI 焦点（Focus）角色匹配机制 与 隐形成分（Invisible Ingredients）注册 上：
    * 双页签与错位原因：原代码中 VillageBuildingsCategory 和 VillageVillagersCategory 都将 MillIngredientTypes.VILLAGE 注册为了 RecipeIngredientRole.INPUT 隐形成分。同时在 VillageCategory 中点击加号时，构造了包含多重角色的焦点组（CATALYST 角色村庄 + INPUT 角色建筑/村民），导致 JEI 检索失配，无法唯一排除另一个分类，从而同时弹出了两个页签。
    * 内容未过滤/对焦失败原因：由于 CATALYST 焦点的村庄与全景类中注册的 INPUT 隐形成分角色不匹配，JEI 无法将配方正确锁定在当前 VillageType 上，导致过滤失效而展示了全部村庄内容。
  * 修复方案
    * 参考 CultureCategory 的做法，通过为两个全景窗口分配独占的隐形成分 Role 来进行隔离：
    * 建筑全景（VillageBuildingsCategory）：使用 RecipeIngredientRole.INPUT 角色的 VILLAGE 隐形成分。
    * 村民全景（VillageVillagersCategory）：使用 RecipeIngredientRole.CATALYST 角色的 VILLAGE 隐形成分。
    * 主面板跳转（VillageCategory）：点击建筑加号时发送 INPUT 焦点；点击村民加号时发送 CATALYST 焦点。
* Q73: `VillageVillagersCategory`现在是正确的，单页签、过滤掉了其他对象；`VillageBuildingsCategory`现在是半对的，三个页签(文化、文化关联村庄、村庄包含建筑)，但都对非关联对象进行了正确的过滤，现在你需要做的是事情是：让`VillageBuildingsCategory`被唤起后只有单页签(Category)
* Q74: 唉页签多了一个，过滤也失效了，又得我下场review了吗，最后给你一次机会
* Q75: 我只要一个村庄建筑页签你在干什么
  * 抱歉是我绕远了。不需要修改任何文化相关代码，直接利用 JEI 的 RecipeIngredientRole.RENDER_ONLY 角色给 VillageBuildingsCategory 做物理隔离即可。
  * 隔离原理  JEI 的 VILLAGE 隐形成分角色重新划定为三方绝对互斥：
  * 村庄详情（VillageCategory）：OUTPUT 角色。
  * 村民全景（VillageVillagersCategory）：CATALYST 角色。
  * 建筑全景（VillageBuildingsCategory）：RENDER_ONLY 角色。
  * 由于全局没有任何其他 Category 将 VILLAGE 设为 RENDER_ONLY，发送该焦点时 JEI 只会精准命中 VillageBuildingsCategory 这唯一一个页签，且过滤 100% 正确。
* 修好了，可以少做功了
// 新目标 Village
* Q76:村民属性里有没有显式的承接bring_back_home_goods的成员
  * [VillagerType#bringBackHomeGoods]返回的是`List<String>`,实际运行中这个参数绑定了一个可能需要的对象:会把小屋的{什么}材料带到市政厅
  * 这两个Goal可能与之有关:"millenaire:deliver_resources_shop","millenaire:get_resources_for_shops",
    * deliver_resources_shop确实是送货
  * 搞错了是`collect_goods`字段介入运输
* 结合现有的模块研究一下API
* Q77: 为了突破 Role 只有 4 个的物理限制，同时完美保留“自动聚焦定位”的能力，最正统的做法是为不同的关联关系定义独立的 IIngredientType（或包装 Payload）。record 写在 plugin里，你改一下
  * 彻底失败了，抛弃整个分支
* Q78: 回到页签问题上，除了我说的两条解决方案，还有什么办法能解决 var focus = MillenaireJeimmPlugin.JeiRuntime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.RENDER_ONLY, MillIngredientTypes.VILLAGE, village); 的缺点。
  * 为[RecipeIngredientRole]注入更多的值，逻辑上最简单但是可用性难以保证;
  * 接受多`页签`(Categor)的现实，{[RecipeIngredientRole#INPUT]+`多页签`(Categor)+`正确过滤`}是最接近完美方案的情况，他只缺少一个页签跳转，哪怕是进行一次`模拟鼠标点击`也比注入好得多;
  1. 从 IIngredientType 上做文章。JEI 允许你注册任意数量的自定义 Ingredient 类型 —— 不可行因为会对一个对象反复声明，就是把`建筑`描述为多个`村庄.建筑`
  2. `.showTypes(List.of(targetType));` —— `指定show`以后可以再过滤吗？AI调用了好几次`此API`没有进行过`指定show`+`过滤`可实现性存疑，但能实现的话是一种解决方案
  3. JEI 的 Subtype（子类型） 机制, 类似于“带 NBT 的物品”或“带不同 Mode 的工具”，你可以让 Village 对象在 JEI 视角下附带不同的子类型 Key —— 检索带NBT标签的特殊对象？很有可为，是方法(1)的可接受版本
  4. 顺应 JEI 的多 Tab 设计 —— 还有投降派，本质上是我的方案(2)
* Q79: 使用标签方案，先做一下前置工作，现在完全有必要把所有的引用关系保存下来，在前置模组里声明一个数据库类(拆解MillenaireAPIHelper并放置在同级目录，但是方法调用本身还是Helper负责)，在合适的时机进行一次初始化(或懒加载)
* Q80: 为除文化以外，另外三个JEI`要素`的注册增写NBT标签
* Q81: 为MillenaireAPIHelper下增加一个Tag命名规范方法，声明和调用时都走Helper而不是都手柄再拼合一次，这些方法在对应的获得XX信息下声明。
* Q82: 只添加方法，不要产生破坏性内容
* Q83: 还需要处理`XXIngredientRenderer`，重构这些给予的类，进行功能实现和用户友好功能添加
* Q84: Renderer里把所有关联tag都输出出来，不用对开F3+H的省略内容
* Q85: 现在有标签了，再回到VillageCategory进行过滤调用
//- 页签调用Bug流程重点
// 为建筑绘制`LinkBox`，他的特殊性在于要走Item渲染流程，不确定Item的数量超过一组会不会有制造大的障碍，能通过(原生支持/文本绘制)实现就不难。
* 暂不考虑为设计到的建筑材料去注入标签，有Bug以后再说吧，反正是有解决方案了
* Q86: 为建筑绘制`LinkBox`，他的特殊性在于要走Item渲染流程，不确定Item的数量超过一组会不会有制造大的障碍，能通过(原生支持/文本绘制)实现就不难。查阅源码留下声明资源用量的接口，暂时为其随便赋值一些道具。
  * 建筑的`LinkBox`包括`村庄(Ingredient)`、`村民(Ingredient)`、`升级用量(Item)`
* 87: 没有声明对应的二级窗口，但是二级窗口本质上就是一个文本然后列出内容，可以封成一个通用窗口吗？不行就算了声明三个新窗口。