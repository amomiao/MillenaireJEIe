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
  //: Village
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
* 为建筑绘制`LinkBox`，他的特殊性在于要走Item渲染流程，不确定Item的数量超过一组会不会有制造大的障碍，能通过(原生支持/文本绘制)实现就不难。
* 暂不考虑为设计到的建筑材料去注入标签，有Bug以后再说吧，反正是有解决方案了
* Q86: 为建筑绘制`LinkBox`，他的特殊性在于要走Item渲染流程，不确定Item的数量超过一组会不会有制造大的障碍，能通过(原生支持/文本绘制)实现就不难。查阅源码留下声明资源用量的接口，暂时为其随便赋值一些道具。
  * 建筑的`LinkBox`包括`村庄(Ingredient)`、`村民(Ingredient)`、`升级用量(Item)`
* Q87: 没有声明对应的二级窗口，但是二级窗口本质上就是一个文本然后列出内容，可以封成一个通用窗口吗？不行就算了声明三个新窗口。
* Q88: 对源码不了解、不能修改，难以抽象，放弃修改了，老老实实新建页面吧
* Q89: 为Building新建三个页面并在Plugin声明他们
* Q90: 道具列表的展示也需要独立的窗口，并且这些二级窗口能不能至少封装一个基类？
* Q91: AbstractBuildingSubCategory改成AbstractSubCategory并更新对应成员、引用，功能还是这些，只不过别的页面也会希望调用这个基类
* Q92: 注册这三个页面，并且MMLog三语都变成常量声明在上面用region封起来，下面在一行里调用三个变量
* Q93: 点建筑使用用量Layout在工作，点不进窗口，building下辖的三个页面工作不正常，我要求的基类是正常运行的这个`村庄包含建筑`样式
* Q94: 别抽象基类了，你就每个都按VillageVillagersCategory这样全写声明
* Q95: 参照VillageCategory和他的两个子页面，为BuildingCategory声明BuildingItemsCategory、BuildingVillagersCategory、BuildingVillagesCategory，其中BuildingItemsCategory#setRecipe中items的获取先指向BuildingCategory中的临时静态方法
* Q96: 我不是用标签隔离内容了吗？为什么还要设置不同的RecipeIngredientRole？
* Q97: 那就是还是没解决枚举有限，更多Layout时焦点就是不可能正常运作了？
* Q98: “JEI 的 Subtype（子类型） 机制, 类似于“带 NBT 的物品”或“带不同 Mode 的工具”，你可以让 Village 对象在 JEI 视角下附带不同的子类型 Key”我给道具添加了这么多内部信息就是让你过滤掉其他页签里的内容让他们消失的，现在你告诉我做不到 点点点点点点点点点点点点点点点点点点点点点
* Q99: 那还说啥了，所有对象都注册到Output让玩家自己再点一次呗，一上午又白干，还是我的代替玩家点一次鼠标靠谱
* 不对不能Output，不然点击村庄页签他们仨就出来了
* 不对不对就该Output，输出三个页签可以是对的，这三个就是相关链接一起出来是合理的，现在的问题是想办法让加号比变成切换页签，哪怕是像`user32`一样让玩家点一下，页签就在固定的位置，完全可以做到，但是这个API的安全性...
* Q100: 你还真用鼠标点击啊，JEI真没API吗
* Q101: 肯定不能鼠标模拟啊，拉倒吧让用户多点一下
  //- 页签调用Bug流程终点
  //- 投降感言，感觉回到了我刚入行做二次开发的时候，不过这花了一天得到的结果还算可以，`MillenaireJeimm`上面注解了分析
* 处理: 按钮现在只提供悬浮提示功能，`setOnPlusClick`标记为[废弃]
* 用到的字符串用变量/模板提取出来并替换
* 书写一个获得`升满建筑等级的道具用量`的方法,这个方法暂时不要写在`MillenaireAPIHelper`里
  * 传入的`BuildingPlanSet`对象:有
    * `public Map<String, List<LevelDef>> variants()`:得到
      * 建筑的变体量: Map.kv 的数量
      * 建筑的等级数: Map.v=>list的数量(为1就是无升级，直接返回null或0长度)
      * 建筑的等级: Map.v=>list成员有: `public int level()`
      * 建筑的NBT文件: Map.v=>list成员有: `public String nbtPath()`
    * `public ResourceLocation planId();`:得到`BuildingPlan`
      * `BuildingPlan`:有`public String nbtPath()` 留作备用应该是`variants`的nbtPath
    * 但是我们并不知道这个`nbtPath`对象的值及其可用性，打一个客户端消息监听一下
  * `MillenaireAPIHelper`:有`public static Map<ResourceLocation, BuildingPlan> getAllBuildingPlans() { return ModCultures.getAllBuildingPlans(); }`
  * `BuildingCostCalculator`:有`public static Map<ResourceLocation, Integer> computeCost(CompoundTag templateNbt)`
    * 尝试使用一种`nbtPath()`的用法生成`CompoundTag`访问一下
  * 最终返回做一个`Map<String variants.key,List<ItemStack>> sum(computeCost({nbtPath()}))`
    * `org.millenaire.building.BuildingPlan`
    * `org.millenaire.building.BuildingPlanSet`
    * `org.millenaire.building.BuildingPlanSet.LevelDef`
    * `org.millenaire.building.BuildingCostCalculator`
* Q102: 创造这种查询页，`BuildingVillageCategory`,`BuildingVillagerCategory``BuildingCostCategory`, 并且你不要创造API，就是调用Map<String, List<ItemStack>> map = BuildingUpgradeCostHelper.getBuildingUpgradeCosts(building)为1、2、3赋值
* Q103: `BuildingUpgradeCostHelper`再改一下，每种道具的用量取最高，现在的用量掺水量太大了
* Q104: 不对只有1级也得算上，应该说是初始/非初始，但是现在不好做先算上1级
* Q105: setRecipe里面列个分割线和材料输出，我要看看这个空格是什么
* Q106: 唉果然是空气
* Q107: 抽一个基类先硬编码支持三个变体，使用abc识别三个窗口
  //- 建筑模块结束
  // 村民模块
* 村民模块的基础功能链接到`村庄`和`建筑`,这一段可以参照`CultureCategory`和`CultureVillagesCategory`文档资料很齐了
  * 高级功能参照:[行为](村民行为调查.md),简单来说是四种物品流向的展示:
    * bring_back_home: `身上的物品`放入`所属建筑`
    * deliver_resources_shop: `所属建筑的物品`给到`村庄中心`
    * get_resources_for_shops: `村庄中心的物品`给到`商店(有交易行为的建筑)`
    * get_goods_for_household: `村庄中心/商店的物品`给到`所属建筑`
  * 但是目前的UI容量只能再塞下两条，这里考虑对玩家越隐形的内容展示的权重越高
    * bring_back_home_goods(json:同名): 落选,因为伐木工有木头/采桑人有丝绸，说的是废话
    * deliver_resources_shop(json:collect_goods): 入选,因为他有一个特殊设定`collect_goods`是白名单，而`collect_goods`为空时此名单不生效
    * get_resources_for_shops(json:无): 落选,首先前置的轻量化JEI已经展示物品交易内容，其次需要到更大的作用域里去查询商店表，总结为吃力不讨好
    * millenaire:get_goods_for_household(json:household_goods): 落选,没有观测到实际用例这个json字段说不定都是编的
      * 但确实存在一个[GetGoodsForHouseholdGoal]其中常出现参数[Map<Item, Integer> requiredGoods]总之不是重点不再研究跳过了
    * 那`bring_back_home_goods`也加进来了，他和`deliver_resources_shop`的处理是一样的，共用基础设施即可。
      * Json用例
        ```json5
        {
          "bring_back_home_goods": ["millenaire:olives"],
          "collect_goods": ["minecraft:wheat_seeds"],  
        }
        ```
  * `VillagerType`:获得的难度基本属于直接拿
    * `VillagerType`有:
      * public List<String> bringBackHomeGoods()
      * public Set<Item> resolvedBringBackHomeGoods()
      * public Set<ResourceLocation> resolvedCollectGoods()
      * public List<String> collectGoods()
    * 获取很简单，需要搞清楚的是`resolved`是为什么?以及其实暴露出了`Map<String, Integer> requiredGoods()`和`Map<Item, Integer> resolvedRequiredGoods`能简单的获得他那也可将其加入展示。
  * org.millenaire.culture.VillagerType
    * 总之先搞清楚一件事这六个数据结构里都存了什么？
    * 在`VillagerCategory#setRecipe`中触发`VillagerType`的相关内容输出，可调用的其他api有:
      * `public ResourceLocation id()`
      * `public ResourceLocation culture()`
      * 使用这八个API使用客户端聊天栏进行debug内容输出。
* 取样调查报告[VillagerType调查](VillagerType调查.md)
  * resolved语义就是safe，直接`三个方法resolved`调用即可,编写代码时需要遵循调查报告
* Q108: 其中`BuildingCategory和CultureCategory`包下的内容是编写参考，除`VillagerCategory`都是基类上下文，以及一个助手类，参考他们，生成最终的方案。
* 唉击穿上下文了
* Q109: 根据文字及文件要求，生成最终的VillagerCategory及其二级页面：VillagerVillageCategory/VillagerBuildingCategory/VillagerBringBackHomeCategory/VillagerCollectCategory/VillagerRequiredCategory，并将代码补充进MillenaireJeimmPlugin。
* Q110: 参照这些二级页签重构五个VillagerXXXCategory页签，你现在为这五个套用了一级页签的格式，这不对。
  * 我说registerCategories里构造方法没有要求ingredientManager
    //- 功能完成
* 现在开始修Bug
  * 1.建筑书签每次进入游戏会消失,且只有建筑书签有这个问题，但好像有一个报告
    * 日志显示 JEI 在从 bookmarks.json 读取建筑书签 "seljuk:toolsmith" 时抛出了 NullPointerException: ingredient must not be null。这是由于 BUILDING_CODEC 在反序列化建筑对象失败时返回了 null 结果，导致 JEI 校验崩溃并丢弃了整份书签配置。
    * `MillIngredientTypes`的问题
    * 查阅`BuildingPlanSetLoader`解决
      * `MillenaireAPIHelper#getAllBuildingPlanSets` 返回的是 Map<ResourceLocation, BuildingPlanSet>，
      * 但在之前的 BUILDING_CODEC 里一直直接拿着 String 去调用 Map.get(Object)。
      * 由于 Java 中 String 和 ResourceLocation 类型不同，Map.get 比较 equals 时必定永远返回 null！
      * 修改 MillIngredientTypes.java 中的 BUILDING_CODEC，将传入的 String 正确解析/转换为 ResourceLocation 后再进行 Map 查询
  * 2.通过全局页签查看的方式，查看一个页签的所有对象时，CultureCategory、VillageCategory、BuildingCategory、VillagerCategory，这四个一级页签里的Layout框架不会得到更新，且内容会更新导致有一定错乱、闪烁，Layout错误的作为了内容载体。
* 进行多语言优化收尾
* Q111: 把这些类里的本地化Key提取到一个新类`MillenaireJeimmKeys`里，为Component.translatableWithFallback声明key，注意key如果能做成模板就尽量减少单个字词的出现；把逻辑运行依赖的字符串封装到类的顶部
* Q112: 同样的为文化提供字符串提取变成了、进行本地化填写到MillenaireJeimmKeys中
* Q113: 同样的为村庄提供字符串提取变成了、进行本地化填写到MillenaireJeimmKeys中
* Q114: 往这里面加写，常量提取为参数怎么没做？
* Q115: 为村庄提供字符串提取成变量、进行本地化填写到MillenaireJeimmKeys中
* Q116: key为Component.translatableWithFallback声明，注意key如果能做成模板就尽量减少单个字词的出现；把逻辑运行依赖的字符串封装到类的顶部
* Q117: Component.translatableWithFallback声明Key，注意key如果能做成模板就尽量减少单个字词的出现；把逻辑运行依赖的字符串封装到类的顶部
* Q118: 这个`类别`你找个单词/简写硬编码成英文算了
* Q119: 把JeiLink的title也放进更新里，现在的方式在游戏中更改语言后无法刷新title
* Q120: 对`BuildingPlanSet#variants`进行过滤，大于0的进入BuildingCostCategoryA，大于1的进入B，大于2的进入C，A包含B包含C是特意的设计
* Q121: JEI能不能做到一个页签里的一个展示框里再增加一个左右箭头显示自己的翻页内容？
* Q122: 太麻烦翻页先不做了，你改动后的JEILink让BuildingCostCategory系列页面的ABC没了
* Q123: 卡片隐藏是特意设计，是不是你给他们仨复用Key了
  //- 1.0结束
  // 开始本地化
* 你把下面的内容像上面一样放进一行里
* Q124: Keys太臃肿了重构一下，现在没有任何key要以`gui.millenairejeim.`为前缀，所有key的前缀都要多一个m为`gui.millenairejeimm.`
* Q125: 我受不了Java的静态(常量)命名方式了，你以C#的静态变量标准，首字母大写的驼峰格式重构Keys
* 重构了Keys的架构，现在由一个内部类KT(KeyTuple)封装了
  * public Component asComponent(Object... args) { return Component.translatableWithFallback(key, fallback, args); }
  * 收尾居然是AI失控的，我在编写Key，令人唏嘘
  * 我不甘心，我要让AI写
* Q126: 按图文，给我构造这部分代码
  * 哈哈成了
* 我擦，每个类型负责的对话不一样类名标准有微小差距，算了不管了
* Q127:图1=>[Keys新版本]、图2=>[Category页面里的旧本吧]-迁移到新版本
  * 以C#的静态变量标准，首字母大写的驼峰格式重构Keys
  * 我去怎么直接写cs了
* Q128:我让你变量名写VillagerCategory这样的格式，不是让你写cs代码
* 算了我手改吧
* Q129:改动BuildingCategory内的`本地化`逻辑，参照另外的两个软件，记住key使用`首字母大写的驼峰格式`，对于`MillenaireJeimmKeys`内的内容只需要输出增量内容，且每行不需要注释。
* Q130:改动BuildingCategory内的`本地化`逻辑，参照`CultureCategory`和下述`截取的文本内容`
  * 记住key使用`首字母大写的驼峰格式`，对于文本内容只需要输出增量内容，每行不需要注释此文本的上下文。
  * Label/Value/Link新建KT是严格遵守(PrefixLabel/Value/Link + "原文名","原文的合理输出")
  * ```java```[移至下文]

* Q130:改动BuildingCategory内的`本地化`逻辑，参照`CultureCategory`和下述`截取的文本内容`
  * 记住key使用`首字母大写的驼峰格式`，对于文本内容只需要输出增量内容，每行不需要注释此文本的上下文。
  * Label/Value/Link新建KT是严格遵守(PrefixLabel/Value/Link + "原文名","原文的合理输出")

* Q130:改动VillageCategory内的`本地化`逻辑，参照`CultureCategory`和下述`截取的文本内容`
  * 记住key使用`首字母大写的驼峰格式`，对于文本内容只需要输出增量内容，每行不需要注释此文本的上下文，但是`// {CultureCategory} 标签`这个功能文本依旧进行对应输出`{}`内容同输出类名。
  * Label/Value/Link新建KT是严格遵守(PrefixLabel/Value/Link + "原文名","原文的合理输出")，变量名'_'前的`主名`的后缀必须是对应的Label/Value/Link,变量名在文本有`%`占位符号时后缀大写占位标识
    * 如`public static final KT MaxCountValue_DS = new KT(PrefixValue + "helloworld", "%d%s");`
  * getTitle()中的内容已经得到管理无需声明
      ```java
        /* CultureCategory 标签 */
        public static final KT NativeLanguageLabel = new KT(PrefixLabel + "native_language", "Native Language: ");
        public static final KT KnownCropsLabel = new KT(PrefixLabel + "known_crops", "Known Crops: ");
        public static final KT HuntingDropsLabel = new KT(PrefixLabel + "hunting_drops", "Hunting Drops: ");
        public static final KT MapIconLabel = new KT(PrefixLabel + "map_icon", "Map Icon: ");
        public static final KT CultureBannerLabel = new KT(PrefixLabel + "culture_banner", "Culture Banner: ");
        public static final KT PanelTextureLabel = new KT(PrefixLabel + "panel_texture", "Panel Texture: ");
        // 状态/数值及其回退值
        public static final KT UnsetValue = new KT(PrefixValue + "unset", "Unset");
        public static final KT ConfiguredValue = new KT(PrefixValue + "configured", "Configured");
        public static final KT UnconfiguredValue = new KT(PrefixValue + "unconfigured", "Unconfigured");
        public static final KT HasNbtValue = new KT(PrefixValue + "has_nbt", "Has NBT");
        public static final KT NoNbtValue = new KT(PrefixValue + "no_nbt", "No NBT");
        public static final KT DefaultValue = new KT(PrefixValue + "default", "Default");
        public static final KT CropCountValue_D = new KT(PrefixValue + "crop_count", "%d types");
        public static final KT DropCountValue_D = new KT(PrefixValue + "drop_count", "%d types");
        // LayoutBox标题
        public static final KT VillageBoxLink = new KT(PrefixLink + "village_box", "Village");
      ```
* 同样的处理Villager
* Q131: 根据修正值AbstractBuildingCostCategoryTitle_S，确保第一个参数key准确，参数2填写Belong %S即可，后续再调整
* Q132: AbstractBuildingCostCategoryTitle_S，他是绝对的正确的，所有页面和他一样都要使用PrefixSecTitle，且不要注释
* Q133: 根据过时的中文原生，迁移生成一份新的zh_cn
* Q134: 只取用MillenaireJeimmKeys中有定义的key
  //- 本地化完成，但是现在美国是周六早八，发了也卡审核，再修点bug吧
* Bug
  1. [一级窗口]描述里重复出现的"::"
  2. [一级窗口]村民详情的JeiLinkBox，其下Item类型的没有正确渲染Item
  3. [Ingredient]悬浮信息至少把对象简单的能给出的给一下
* 优化
  1. [二级窗口]如`村庄出现村民`，过滤空？不过滤显式的告诉玩家这个对象指向空，过滤则是更好的体验
  2. [二级页面]如`村庄出现村民`，这些汉化不好，等会改一下
  3. [Ingredient]图标可以更好看一点
  4. 建材统计好像有API，等会测试一下
* Bug.1: `代码拼合`和`本地化`各有一个`:`，去掉谁？-`本地化`不再写`:`
* Bug.2: 不知道为什么，问一下AI
  * Q135: 对渲染`ItemStack`的JeiLinkBox，`BuildingCategory`正确渲染了，而`VillagerCategory`却没有，查找一下原因。
  * Q136: 只是JeiLinkBox没有渲染Item数据是好的，VillagerBringBackHomeCategory能正确渲染item
  * 希望得到`ItemStack`但参数是`Item`
    * JeiLinkBox.useJeiMode 方法接收的参数类型为 IIngredientType<Object>（或无通配符的泛型定义），而 MillIngredientTypes.VILLAGE 的实际类型是 IIngredientType<VillageType>。在 Java 中，IIngredientType<VillageType> 不能隐式向上转型为 IIngredientType<Object>，导致编译器拒绝匹配该方法。
    * 通过对 MillIngredientTypes.VILLAGE 和 MillIngredientTypes.BUILDING 进行原始类型强转（Raw Type Cast） (mezz.jei.api.ingredients.IIngredientType) 绕过泛型检查即可解决
      * Q137: 这个问题在C#有吗，翻译成C#是什么意思
        * 哦协变，叽里呱啦不说这俩字没反应过来
  * Q138：问题没有解决还是空白，你在VillagerCategory加一点客户端输出聊天消息看看什么情况
    * ItemStack 槽位漏掉了 .useJeiMode(...)
    * 我擦，抄都能抄歪了，链式调用的孬处就在这
* Bug.3: 给个`ID(ResourceLocation)`算了，`ID`带文明了，其他的都是一对多的关系
  * 我去，第一个tooltip.add是名字不能删
* 优化.3: 现在一个[Ingredient]是由1个可变图标、1小串字组合渲染的
  * 右上角再渲染一个小图标，文化是钻石，村庄是金锭，建筑是铁锭，村民是绿宝石，主体内容全都尝试从旅行书中获得
  * `MillIngredientIconResolver`负责这段代码
  * 角标两个字溢出太厉害了，缩成一个算了，在`Math.min`后修改
* 优化.4 现在的资源消耗是从`BuildingPlanSet`中得到`LevelDef`得到`NbtPath`然后计算得到的，但是我发现`LevelDef`有`public Map<ResourceLocation, Integer> requiredResources()`我不确定他是什么
  * 查阅了`BuildGoal`得知他就是升级材料计数，那么BuildingUpgradeCostHelper就可以过时了
  * `BuildingUpgradeCostHelper`不再依赖计算器，直接依赖这个API
* 当前版本只剩下汉化优化了
* Q137: ("§a✔ Copied view to clipboard!")移动到右下角
* Q138: 根据zh_cn生成一份en_us，现有的en_us可以作为语义参考
  * 不对我的失败回调不就是英语写的吗
* 优化.2: 汉化/英化改善完成
* Q139: 现在的Debug窗口的唤起会关闭JEI窗口，你能仅简单修改这三个文件就解决问题吗？
  * Minecraft 的 GUI 渲染机制为单屏幕模式（同时仅有一个 Screen 激活）。当调用 Minecraft.getInstance().setScreen(...) 打开全新的 JsonDumpScreen 时，原有的 JEI 配方界面 (RecipesGui) 会被替换。如果在关闭 JsonDumpScreen 时直接触发默认的 Screen#onClose()，Minecraft 会将屏幕置空（setScreen(null)），导致无法返回 JEI 界面。
  * JsonDumpScreen：新增 parentScreen 变量与构造函数参数；重写 onClose() 方法，在关闭调试窗口时恢复原先的 parentScreen。
  * DebugGuiUtils：在打开 JsonDumpScreen 时传入 Minecraft.getInstance().screen（即当前正在显示的 JEI 界面）。
  * BaseMillCategory：唤起 Debug 窗口时将当前激活的 JEI Screen 传递给 DebugGuiUtils。
* 主模组和子模组进行一个联动
* Q140: 我有一个千年工艺配方，我想让千年工艺有个按钮可以进行对村民详情的指向，也就是把村民详情的设置为`RecipeIngredientRole#CATALYST`我具体应该怎么做？现在先只做一件事：建立`RecipeIngredientRole#CATALYST`关系。
* Q141: 检查存在模组：millenairejeimm时进行这个行为，且现在书写内容为父模组联动子模组，这样书写会不会出现调用时机错误的问题？
* Q142: 父子模组都是我的，你设计一个逻辑安全的实现这个行为，最好不要注入
* Q143
  ```text
  在MillCraftingCategory中绘制一个原生的GUI，以按钮的样式绘制，上面有个绿色的`＋`，只需要绘制一个，为村民唤起村民详情的查看。
  按钮也是提供事件以运行，提供给子模组。
  事件1：是否绘制
  事件2：触发内容
  由于父模组在别处这样制作内容，可以把这个按钮封装成一个控件。
  ```
* Q144: 按钮不起作用，并且一堆目标图标被放置在奇怪的位置，按钮的目的是跳转到有对应页面，且只有与唤起页面参数相关的条目。
  * 1. 按钮点击无响应：坐标二次校验冲突
       原因：在 createRecipeExtras 中，getArea() 返回的 ScreenRectangle 已被 JEI 用于自动拦截点击。但在 handleInput 中又调用了 villagerPlusButton.isHovered(...)，而 handleInput 传入的 mouseX/mouseY 是 配方面板局部坐标，与 isHovered 内部期待的全局坐标不匹配，导致判定始终返回 false。
       修复：移除 handleInput 内部多余的 isHovered 校验，依赖 ScreenRectangle 完成区域拦截。
* Q145: 修改，这个三个方法都在此类吗？
* Q146: 回忆整理代码，准备再做一组相同用法的按钮
  ```text
  复用开发时的避坑清单
  Role 规范匹配： 跳转 Focus (createFocus) 与目标 Category (setRecipe) 使用的 Role 必须完全一致（通常为 RecipeIngredientRole.OUTPUT），否则 JEI 无法准确过滤目标 Category。
  禁用负坐标： 切勿设置 (-100, -100) 这类视口外坐标，JEI 会将其安全裁剪限制在屏幕左上角 (0, 0) 处渲染出浮空图案。
  多焦点接口： 现代 JEI 版本的 {@link mezz.jei.api.runtime.IRecipesGui#show} 接受的是 List<IFocus<?>>，使用 Stream 映射时必须加上 .<IFocus<?>>map(...) 显式泛型标记。
  ```
* Q147: 这个按钮没有检查可用性，移除了子模组仍然存在，且应当和轮播对象有一定关联，没有轮播对象的时候也不要显示，重构中修复这部分问题。
* Q148: 同样的结构，增加唤起的建筑的搜索
* Q149: 差不多到预定位置了，千年工艺这个框移到外面，且按钮事件还是没有生效
  * 按钮点击失效且位置错位的根本原因在于：JEI 的槽位图标与加号按钮坐标重叠。之前 JEI 槽位设在 (140, 72)，加号按钮设在 (142, 74)，使得 JEI 槽位的鼠标判定覆盖了按钮，点击时直接被 JEI 的“按 R/U 查配方”逻辑拦截，导致点击事件无法传递到按钮。  只需将 JEI 槽位图标左移至 128，并将 加号按钮移至右侧外面 148（错开坐标），即可恢复按钮点击并解决排版重叠。
* Q150: 交易页面同样需要把按钮移到框外
* Q151: 进行修改，注意当前`MillTradeCategory`是无报错的，确保不要调用到错误的API
* Q152: 按钮还是不能按，你写一点客户端聊天栏输出看看到底怎么回事
* Q153: 你得想办法找到事件为什么不触发，渲染是正常的。 [Q144]这是之前不触发的原因及其解决方案，看看有没有帮助
  * 缺失 getBackground() 重写：MillTradeCategory 中声明了 178 宽度的 background，但未重写 IRecipeCategory#getBackground() 方法。JEI 19.x+ 依赖 getBackground() 获取配方卡片的裁剪与点击判定区域（ScreenRectangle），缺失该方法会导致 JEI 将点击有效区裁剪回默认尺寸（160px），导致位于 X=163 的框外按钮直接被 JEI 拦截过滤。
    * 这一条不用测肯定是假的，getBackground() 已经过时了。
  * 控件局部坐标与全局 isHovered 冲突：若 MillPlusButtonWidget 内部包含了 isHovered 或全局坐标校验，JEI 传递给 createRecipeExtras 控件的 mouseX/mouseY 是配方卡片相对坐标（0~178），会导致控件内部坐标判定失效。
* Q154: 找不到原因，回到可用的版本看看
  控件定位混淆（渲染与事件注册解耦缺失）
  MillPlusButtonWidget 只是一个普通的 2D 绘制与碰撞检测辅助类，并非 JEI 原生的 IRecipeWidget。在 MillCraftingCategory 中：  视觉渲染：在 draw(...) 方法中由 villagerPlusButton.render(...) 显式完成绘制。  点击拦截：在 createRecipeExtras(...) 中显式调用 builder.addInputHandler(...)，使用 IJeiInputHandler 的 getArea() 返回 ScreenRectangle 告知 JEI 碰撞矩形。
  在最新的 MillTradeCategory 中，直接尝试将 MillPlusButtonWidget 注册至 builder.addWidget(...) 或在 createRecipeExtras 内挂载回调，导致 JEI 无法感知该区域的鼠标点击，从而完全拦截了事件。  缺少 IJeiInputHandler 的 ScreenRectangle 显式绑定
  JEI 19.x+ 的交互拦截严重依赖 IJeiInputHandler#getArea() 返回的 ScreenRectangle 矩形区域。MillCraftingCategory 能够精确拦截点击，正是因为使用匿名类实现了 getArea() 并校验了 InputConstants.Type.MOUSE 点击按键。
* Q155: 这个新版本的按钮类，你觉得旧的好还是新的好？
* Q156: MillTradeCategory中如何使用他
* Q157: 还是没修好，抛弃当前分支，回到可用版本从长计议
* Q158: 帮我梳理一下声明加号和轮播JEI图标的流程 => MillenaireJeimm
* 理清楚了:在`联动逻辑注释`提及处查看`MillenaireJeimm`以了解详细代码
  ```text
    /// 5.0a 联动逻辑-加号按钮
    /// 5.1 前置里封装了一个(加号按钮)[MillPlusButtonWidget]
    ///     尺寸为12x12[MillPlusButtonWidget#MillPlusButtonWidget()]
    ///     检测鼠标悬浮[MillPlusButtonWidget#isHovered]
    ///     外部调用渲染[MillPlusButtonWidget#render(GuiGraphics, Font, int, int, double, double)]，其中`int int`是`按钮位置`
    /// 5.2 [MillCraftingCategory]中创建了一个对象[MillPlusButtonWidget]
    ///     [MillCraftingCategory#VILLAGER_BUTTON_X]和[MillCraftingCategory#VILLAGER_BUTTON_Y]定义了他的位置
    ///     [MillCraftingCategory#draw]在末尾调用了[MillPlusButtonWidget#render]
    ///     [MillPlusButtonWidget#render]之上,有检测事件[MillRecipeButtonRenderEvent]的发布,[MillVillagerDetailButtonHandler#onCheckButtonRender]在监听着此事件
    ///         检测源是否为期望的目标: `MillCraftingCategory.BUTTON_ID_VILLAGER_DETAIL.equals(event.getButtonId())`
    ///         符合则准许(加号按钮)的绘制，此绘制也包含着`Jei要素轮播`
    /// 5.3 [MillCraftingCategory#createRecipeExtras] 再次确认渲染性，通过后传入一个[IJeiInputHandler]控制实际的控件逻辑(按钮作用域、按钮事件)
    ///     当点击触发时,投送[MillRecipeButtonClickEvent]事件，由[MillVillagerDetailButtonHandler#onButtonClick]捕获
    ///     触发`focusFactory.createFocus(RecipeIngredientRole.OUTPUT, MillIngredientTypes.VILLAGER, villager)`，即对`千年村民`(要素)触发(右键Output)点击事件
    /// 5.0b 联动逻辑-轮播要素
    /// 5.1 [MillCraftingCategory#setRecipe]在末尾发布了[MillRecipeSetSlotEvent]事件
    ///     [MillRecipeSlotEventListener#onSetRecipeSlots(MillRecipeSetSlotEvent)]监听到了事件，其后在硬编码的位置上创建轮播`JEI要素栏位`，参数带了轮播对象
    ///     其后构造即可`event.getBuilder().addSlot(RecipeIngredientRole.INPUT, VILLAGER_ICON_X, VILLAGER_ICON_Y).addIngredients(MillIngredientTypes.VILLAGER, villagers);`
    /// 总结:
    /// 对象:[MillCraftingCategory]
    /// 公共类(1:n):
    ///     [MillPlusButtonWidget]:按钮控件,完全封装不应修改
    ///     [MillRecipeButtonRenderEvent]:请求事件-绘制按钮,完全封装不应修改
    ///     [MillRecipeButtonClickEvent]:请求事件-按钮点击后做,完全封装不应修改
    ///     [MillRecipeSetSlotEvent]:请求事件-绘制Recipe时做,完全封装不应修改
    /// 专职类(1:1):
    ///     [MillRecipeSlotEventListener]`1对象:1声明`:事件逻辑=执行事件-绘制Recipe时做
    ///         声明[MillRecipeSlotEventListener#BUILDING_ICON_X]这样成员变量,设置`按钮的绘制位置`
    ///         声明[MillRecipeSlotEventListener#onSetRecipeSlots]内的逻辑,创建轮播`JEI要素`
    ///     [MillVillagerDetailButtonHandler]`1对象中=>n加号:n声明`:事件逻辑
    ///         执行事件-判断绘制按钮
    ///             声明[MillVillagerDetailButtonHandler#onCheckButtonRender],是否有要素后调用`event.setShouldRender(hasCarouselObjects);`
    ///             注意: 子模组未实装则根本没有此监听，而导致的加号不被渲染
    ///         执行事件-按钮点击后做
    ///             声明[MillVillagerDetailButtonHandler],对自己职能的目标进行跳转
  ```
* Q159: 对1:1的专职类抽象基类或接口
* Q160: 我让你给MillVillagerDetailButtonHandler抽个接口，你怎么弄出这么多东西你在干什么
* Q161: 你写default是嫌自己工作量太少了吗
* Q162: 你把default改成get接口啊
* Q163: 你知道写default你为什么不知道把声明抹了全改成必须要实现的成员
* 气死我了，要不是java看着头晕我要每个类跟十个接口
* Q164: 如上提取接口，注意每x、y变量和一组list是一个逻辑关联
* Q165: 我有一瞬间以为你听懂了要把xy get提取为小元组
* Q166: `MillenaireJeimm`的第五章节内容
* Q167: 我要把现在的建筑按钮移到建筑要素右边，然后为村民要素新建一个按钮指向它
* Q168: MillRecipeSlotEventListener被更名为了MillCraftingRecipeSlotEventListener，
* 提示词没有告知清楚类位于`前置-后置`的信息
* Q169: 为MillTradeCategory创建一组同样的代码，来创建轮播与加号，注意`包名`特别是`m和mm后缀`
* Q170: 又偷偷造API
* Q171: 你再想想MillTradeRecipeli怎么用MillenaireAPIHelper.getBuildingPlanSet
* 提示词还得改
* Q172: 为MillTradeCategory创建一组同样的代码，来创建轮播与加号，注意`包名`特别是`m和mm后缀`
* 我擦忘说控件位置在那了，看看位置合适吗
* Q173: 怎么炸了
  * 崩溃原因在于 NeoForge 的 @EventBusSubscriber 注解要求类中所有声明了 @SubscribeEvent 的方法必须是 static（静态）方法。
* Q174: 那Crafting为什么没注册静态也没炸？
* Q175: 不可能，这份代码之前100%正常运行
  * [此份回答错误]
    ```text
    1. 手动注册 vs 自动扫描注册 的底层差异
    NeoForge 的事件总线有两种完全不同的注册途径：
    手动注册对象实例（NeoForge.EVENT_BUS.register(new MyListener())）：
    允许非静态方法。因为你把 new 出来的对象传进去了，NeoForge 反射时知道该用哪个实例（this）去调用 @SubscribeEvent 方法。
    注解自动扫描（@EventBusSubscriber）：
    强制要求 static 静态方法。因为 FML 加载器在游戏启动阶段反射扫描字节码时，根本不会（也不能）去实例化你的类。它底层调用的是 EVENT_BUS.register(Class<?>)，没有对象实例，就只能去调用静态方法。
    2. 为什么你之前测试 100% 正常？
       之前 Crafting 能够正常运行，通常是因为满足了以下两种情况之一：
    之前使用的是手动注册：在之前的代码提交里，Crafting 的监听器类头上并没有标注 @EventBusSubscriber 注解，而是在 JEI 插件初始化（如 onRuntimeAvailable）或模组构造器里写了 NeoForge.EVENT_BUS.register(new MillCraftingRecipeSlotEventListener())。这种情况下，非静态方法是 100% 合法且能正常运行的。
    重构/复制时顺手加了注解：在本次引入 Trade 模块、重构统一接口（如 ILinkageSlotEventListener）的过程中，给类头上统一补上了 @EventBusSubscriber(modid = "millenairejeimm")。这一加，就直接触发了 FML 的 AutomaticEventSubscriber 静态校验。
    ```
* Q176: 代码都给你了，找出证据
  ```text
  给米仔细比对了你给出的这三份源码，确实是我之前的推断有误！Crafting 模块之前能 100% 正常运行 绝非偶然，源码中存在非常确凿的架构设计证据：
  1. Crafting 模块完全没有触发 @EventBusSubscriber 的自动扫描雷区
     查看 MillCraftingCategory_3.java 可以发现，MillCraftingCategory 根本没有在类头上标记 @EventBusSubscriber。它在架构中担当的是事件发布者（Publisher）的角色：
  在 setRecipe 中调用 NeoForge.EVENT_BUS.post(new MillRecipeSetSlotEvent(...))；
  在 draw 和 createRecipeExtras 中主动 post MillRecipeButtonRenderEvent 与 MillRecipeButtonClickEvent。
  它并没有让 FML 在启动阶段通过反射去自动扫描注册它的方法，因此完全不会触发 NeoForge AutomaticEventSubscriber 对静态方法的强制校验。
 
  2. 按钮交互使用了 JEI 19.x 标准的 createRecipeExtras
     在 MillCraftingCategory_3.java 的 createRecipeExtras 方法中，加号按钮的点击是直接通过 IRecipeExtrasBuilder.addInputHandler(...) 挂载 IJeiInputHandler 内部类处理的。这是 JEI 19.x+ 官方推荐的 GUI 交互机制，生命周期完全由 JEI 托管，根本不需要挂载全局 NeoForge 事件监听。
  3. 配方解析器使用纯 Java 静态注册表
     查看 MillCraftingRecipe.java，配方解析采用的是标准的面向对象设计模式：
     private static final List<AbstractMillCraftingType> PARSERS = new ArrayList<>();
     通过 registerParser 手动维护列表，完全不依赖任何 NeoForge 的注解或反射加载器。
  4. Plugin 入口使用动态高级注册
     在 MillenaireJeimPlugin.java 中，Crafting 模块通过 registerAdvanced 向 JEI 注册了 MillCraftingRecipeManagerPlugin，接管了按需懒加载逻辑，注册链路非常清晰干净。
  Crafting 模块的底层逻辑从配方解析、界面绘制到 GUI 交互，全都严格遵循了 JEI 规范与纯 Java 实例模式，之前能够 100% 稳定运行是完全合理且必然的。崩溃确实只源于 Trade 监听器类误加了 @EventBusSubscriber 注解。给米向你道歉，这次以这份源码证据为准！
  ```
* 就是没有读到[MillCraftingRecipe]导致选择了不同的实现路线。
* Q177: 重写Trade模块
* Q178: 爆了，破坏性修改太多代码没法维护了
* 回档
* Q179: [带截图]为MillTradeCategory创建一组同样的代码，来创建轮播与加号，注意`包名`特别是`m和mm后缀`
  * 给我参数炸没一个感觉没戏
* Q180: 我让你用类里自己有的API，缺什么告诉我
* Q181: MillTradeRecipe哪里来的getAssociatedBuildingIds我不是说了缺API告诉我吗
* Q182: 你干嘛了
  * 这是由于在标注了 @EventBusSubscriber 的类中，@SubscribeEvent 事件监听方法没有声明为 static 导致的。
* 取缔了mm.event中的接口化，他们两个处理器依赖 IJeiRuntime 运行时实例，它们不能改写为 static 静态类影响了上面的static,把接口的逻辑还给了子类
* 我得缓缓
* 二战
* Q183: 把现在的建筑按钮移到建筑要素右边，然后为村民要素新建一个按钮指向它
* 按钮的参数穿错了，首先焦点应该是input，其次上方的按钮指向了建筑而非村民，下方的按钮指向了错误页签，但还好，自己改了赶紧提交吧
* 页签顺序怎么变了，毁了
* Q184: 怎么指定页签更靠前？
  * RecipeCategorySortingConfig#getDefaultSortOrder
  * 我日jei有config能自定义排序，之前报错排序文件也错了
  * 好像是按RECIPE_TYPE排的，参数1是modid不能变，改参数2
  * 修复了排序问题，清除了部分冗余字符串，`MillIngredientTypes`现在负责模组所有的CategoryID，之前的正常可能是开发期间配置没刷新，而不正常源自崩溃后配置刷新，总之忙这一阵是好事。
* 为MillTradeCategory图上位置预留轮播和加号按钮，只加写，你想要改动的内容列个清单后面的对话一次次执行
* `VILLAGER_BUTTON_Y`值更小的位置在更上面
  * `MillCraftingCategory`中按钮的位置是没错的，但是功能错了，村民的按钮调用了建筑的`r`，而建筑的按钮调用了村庄的`r`,也就是应该改回output
* 联动1彻底修好了
* [MillTradeCategory#buildShopSummaryComponent]负责商店->建筑(string)，支持一下他对负责商店->建筑(buildingPlanSet)的支持
  * 新增了[MillenaireAPIHelper]为`public static Set<BuildingPlan> getBuildingsByShopId(ResourceLocation cultureId, String shopId)`
* 之前的问题很大的原因是Helper没有提供一个`BuildingPlan->BuildingPlanSet`
  * [MillenaireAPIHelper]有`public Map<ResourceLocation, BuildingPlanSet> getAllBuildingPlanSets()`
    * [BuildingPlanSet]有`Map<String, List<LevelDef>> variants()`
    * [LevelDef]有`public ResourceLocation planId()`
  * [BuildingPlan]有`public ResourceLocation id()`
  1.新建一个Map懒加载`BuildingPlan到BuildingPlanSet`的反查
  2.更新[MillTradeCategory#getBuildingKeysByShopId]
    * 移除[MillTradeCategory#extractBaseBuildingKey]
    * 直接调用[BuildingPlanSet]的`public String nativeName()`
  3. 给出代码改动量，不需要全量输出
* Q185: 实现MillTradeRecipe对IMillRecipe的继承，MillCraftingRecipe是参考文件
* Q186: 别编接口，字符串拼接都敢编你想害死我吗
* 坏了IMillCraftingRecipe不是一个全局接口，是工艺自己的，但也是好事BuildingPlanSet不需要一个容器，传自己的就能运行
* Q187: Review发现[MillRecipeButtonRenderEvent] [MillRecipeButtonClickEvent] [MillRecipeSetSlotEvent]所`依赖的重要成员`[IMillCraftingRecipe]`旧名IMillRecipe`是专职服务于Crafting的各个细分的,根据[MillVillagerDetailButtonHandler]、[MillCraftingRecipeSlotEventListener]的实现尝试让[MillRecipeButtonRenderEvent] [MillRecipeButtonClickEvent] [MillRecipeSetSlotEvent]支持泛型
* [重复Q166]不争馒头争口气，再次尝试AIGC
* Q188: 为MillTradeCategory创建一组同样的代码，来创建轮播与加号按钮(水平位置在配方框右侧，垂直位置在建筑文本同高处)，注意`包名`特别是`m和mm后缀`，注意调用Helper里的API
* 成了，加号我自己给他往上挪挪，但是还有一个问题，争取200Q以内结束
* Q189: 资源消耗总和应该通过`BuildingPlan`来创建一个总查询而不是现在的ABC分流
  * [MillenaireAPIHelper]有`public Map<ResourceLocation, BuildingPlanSet> getAllBuildingPlanSets()`
    * [BuildingPlanSet]有`Map<String, List<LevelDef>> variants()`
    * [LevelDef]有`public ResourceLocation planId()`以及`public Map<ResourceLocation, Integer> requiredResources()`(当前代码已提供用例)
  * [BuildingPlan]有`public ResourceLocation id()`
  * `BuildingCategory`的`JEILayoutBox`保持原逻辑不变
  * 移除`AbstractBuildingCostCategory`及其ABC子类，转而声明一个`recipe`统计`BuildingPlan`的`BuildingCostCategory`
  * `BuildingCostCategory`配方内标题的本地化在编码中拼合`BuildingPlanSet#nativeName()`:`public ResourceLocation id()`的字符串，查看实际输出后再处理
* Q190: 你参照AbstractBuildingCostCategory的页面实现方式，不存在MillIngredientTypes.BUILDING_PLAN这类要素，你也不能去实现这个要素
* Q191: `recipe`的对象是`BuildingPlan`而非`BuildingPlanSet`,`BuildingCostCategory`改叫`BuildingPlanCostCategory`并修改相关逻辑;
  * 配方内标题的本地化在编码调用`BuildingPlanSet#nativeName()`:`BuildingPlan#id()`
  * BuildingPlan在Helper有getall方法,BuildingPlanSet通过BuildingPlan参数调用Helper反查获取，id的`在BuildingPlan的实现`是`public ResourceLocation id()`
* Q192: `Buildingplan`没有自己的消耗表，所以要通过反查`BuildingPlanSet`进行资源统计
* Q193: 哦不对，我要统计每个变体之和，传入的参数应该是LevelDef，按照当前代码改编一下，同时给出注册实现
  * 暂时保留原名`BuildingPlanCostCategory`; 
  * LevelDef是BuildingPlanSet的内部类;
  * `BuildingPlanSet#variants()`=>public Map<String, List<LevelDef>> variants():通过BuildingPlanSet的成员构造;
  * `LevelDef#planId()`=>ResourceLocation planId():配方内标题的本地化在编码调用`LevelDef#nativeName()`:`Level#id()`;
* Q194: 根据实际效果可以断定`LevelDef`就是1:1的封装了`BuildingPlan`，我们保留现在的设计，只是进行优化，不需要对资源进行合并统计了;
  * 并且`LevelDef#planId`已查明格式为`xx_a_0`,你从倒数第二个`_`截断，拼合`LevelDef#nativeName()`:`a_0`;
* 没有遵循保留`BuildingPlanCostCategory`的约定写为了`BuildingCostCategory`,改为了`BuildingLevelCostCategory`
* 逻辑完成，构造本地化，de_de.json en_us.json es_es.json fr_fr.json ja_jp.json pt_br.json ru_ru.json zh_cn.json zh_tw.json
* Q195[重复]结合:zh_cn与en_us编写de_de的本地化