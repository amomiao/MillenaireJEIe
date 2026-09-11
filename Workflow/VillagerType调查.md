# Millénaire 模组 VillagerType 数据结构分析与 JEI UI 展示策略报告

## 一、 背景与研究目标

在开发 Millénaire 的 JEI 扩展模组（*MillenaireJEI*）过程中，需要对村民类型（`VillagerType`）的行为与需求数据进行可视化展示。由于 JEI 界面图层空间有限（最多仅能额外扩展 2~3 条展示行），需要筛选出对玩家最有价值、最隐形且最具代表性的信息。

本报告对 `VillagerType` 暴露的 6 个核心数据 API 进行深入调试、日志跟踪与行为对比，并给出最终的数据渲染结论与过滤策略。

---

## 二、 6 个核心数据字段对比与调试日志验证

通过在 `VillagerCategory#setRecipe` 中插入客户端聊天栏与控制台 Debug 输出现场日志，对比分析如下：

| 数据字段类别 | API 方法 | 数据类型 | 调试观察结果（基于多职业 Log） |
| :--- | :--- | :--- | :--- |
| **带回建筑** | `bringBackHomeGoods()` | `List<String>` | **原始字符串**：包含 JSON 中的原始配置，可能存在未注册/无效 ID（如 `millenairepolishculture:polishsausage`）。 |
| | `resolvedBringBackHomeGoods()` | `Set<Item>` | **安全过滤集**：自动剔除了未注册的无效物品，直接绑定为 Minecraft `Item` 实例。 |
| **上交中心** | `collectGoods()` | `List<String>` | **白名单原始串**：决定村民是否会将特定物品送到村庄中心（当此白名单为空时该上交行为不触发）。 |
| | `resolvedCollectGoods()` | `Set<ResourceLocation>` | **已解析 ResourceLocation 集**：强类型标识，需通过 `ItemHelper.resolve` 转化为 `Item`。 |
| **工作/生活需求** | `requiredGoods()` | `Map<String, Integer>` | **专属需求映射**：仅包含 JSON 中该职业显式配置的特定物资（如雕刻师需要 20 个石头、农夫需要种子）。 |
| | `resolvedRequiredGoods()` | `Map<Item, Integer>` | **全局扩展映射**：对于无专属需求的平民/主妇，Millénaire 会自动注入全局全文化通用的基础饮食与消耗库（15+种食物）。 |

### 典型日志测试用例对比：

1. **波兰鸡猪农夫 (`polish/chickenpigfarmer`)**:
   - `bringBackHomeGoods`: 包含未注册的 `polishsausage`。
   - `resolvedBringBackHomeGoods`: 自动剔除 `polishsausage`，仅保留 `cooked_porkchop`, `feather`, `egg` 等 8 种有效物品。
2. **拜占庭妻子 (`byzantines/wife_byzantine`)**:
   - `requiredGoods()`: `{}`（为空）
   - `resolvedRequiredGoods()`: 包含 `calva=2`, `feta=2`, `sake=2`, `winefancy=2`, `cake=2` 等 15 种全文化通用消耗品。
3. **印第安雕刻师 (`indian/sculptor`)**:
   - `requiredGoods()`: `{red_sandstone_carved=5, stone=20, sandstone_carved=15...}`
   - `resolvedRequiredGoods()`: 与原始集合完全对应（5 种专属建造材料）。

---

## 三、 四大关键判定结论

1. **`resolved` 具备自动容错与类型安全机制**：
   - 在模组预加载阶段完成后，`resolved` 集合自动过滤了无效或拼写错误的物品 Identifier。
   - **结论**：JEI 界面渲染必须 100% 依赖 `resolved` 系列 API，防止因空指针或未知物品导致渲染崩盘。
2. **`requiredGoods` 存在“通用饮食库自动注入”现象**：
   - 绝不能无脑展示 `resolvedRequiredGoods()`！否则所有主妇/无工作村民都会在 JEI 界面展示一长串相同的蛋糕、美酒和奶酪。
   - **结论**：必须以 `requiredGoods().isEmpty()` 作为前置过滤器。仅当原始配置非空（即明确定义了职业工作需求）时才进行渲染。
3. **`bringBackHome` 与 `collectGoods` 职责彻底解耦**：
   - `bringBackHome`（产物带回）：代表**生产/收获**行为（如伐木工拿木头、农夫拿小麦）。
   - `collectGoods`（白名单上交）：代表**物流周转**行为（将所属建筑的积压物资调拨给村庄中心）。
4. **展示权重与最终选牌**：
   - **第一优先级（入选）**：`resolvedBringBackHomeGoods`（产物带回建筑）
   - **第二优先级（入选）**：`resolvedCollectGoods`（白名单上交中心）
   - **第三优先级（条件入选）**：`requiredGoods`（经过非空过滤后的职业专属工程/建造需求）