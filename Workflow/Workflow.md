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