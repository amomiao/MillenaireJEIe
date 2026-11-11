# Millenaire JEI Expansion (millenairejeimm) 更新日志

---

## [v1.3.3] - 2026-11-11

### 🧳 外来商人货箱展示 (`VillagerMerchantStockCategory`)
- 新增全景二级分类 **外来商人货箱**（`dg_villager_merchant_stock_category`），读取 `VillagerType#foreignMerchantStock()` 展示所有配置行商货箱的村民携带货物与数量。
- 在村民详情主页（`VillagerCategory`）集成 **外来商人货箱** 联动 `JeiLinkBox`（空盒自动隐藏，保持 `225px` 画布零溢出），并支持从货箱物品/村民精准跳转。

### 🛡️ 反射安全加固
- 全面改造 `MillenaireJeimmEncyclopediaHelper`（含 `CalculatorProbe`、`MAPPING_CLASS` 及字段/方法读取）与 `DebugGuiUtils` 接入前置 `SafeReflectionHelper` / `MMLog.warn` 防护，确保未来前置或附属模组字段/方法变更时清晰告警且不崩溃、不中断游戏运行。

---

## [v1.3.2] - 2026-11-11

### 📦 依赖与版本对齐
- **前置编译依赖升级**：将 `build.gradle` 中的 Millénaire 编译依赖切换为 `millenaire-9.1.0.jar`（保持 `neoforge.mods.toml` 中的适用版本范围不变）。
- **版本递进**：按解耦规范（Q19）从 `1.3.1` 升级至 `1.3.2`。

### 🤖 百科导出命令扩展 (`/milljeimm export encyclopedia` / `/milljeimm dump`)
- **硬编码定点采集目标 (`# HARDCODED SPOT GATHERING GOALS`)**：导出 `SpotGatheringGoal`（`gather_silk`、`gather_snails`、`gather_brick`、`dry_brick`、`harvest_sugar_cane`、`plant_sugar_cane`）的标记方块/土壤、产生物品、匹配建筑及执行村民。
- **隐式掉落、行商与初始箱库存 (`# IMPLICIT LOOT, MERCHANTS & RESTOCK`)**：导出 `SlaughterHandler`（`bonusItems` 与 `pickupNearbyItems`）、`FishingInuitHandler`、`InuitHuntingDropHandler`、各文化 `foreignMerchantStock` 与 `GoodsRestockHelper` 建筑/独立建筑初始箱补货清单。
- **建材折算规则与运行时替代审计 (`# COST RULES & ACTIVE SUBSTITUTIONS`)**：反射导出 `BuildingCostCalculator` 免费/折算/折叠常量与 `BlockCostRegistry`，并在存在 `millenairecostombuildingblockmapping` 时导出在线全局/分文化替代规则及配置文件中解析为 `Items.AIR` 或被前序折叠吞并的死规则诊断。

---

## [v1.3.1] - 2026-11-10

### 🎨 界面与排版优化
- **交易页建筑轮播控件重构**：将交易建筑轮播槽位移动至页面右下角（`X=124, Y=43`），与前置加号按钮（`X=142, Y=45`）在底栏水平居中对齐，彻底解决与右上角 `[次要]` 交易标签叠放的问题。

---

## [v1.3.0] - 2026-11-10

### 🔗 联动交互重大修复
- **加号按钮跳转彻底修复**：摒弃旧式模糊的 `createFocus` + `show` 查询逻辑，全面接入 `JeiNavigationHelper` 定向导航，点击工艺页与交易页的加号按钮可 100% 精准直达对应的村民主分类（`VillagerCategory`）与建筑主分类（`BuildingCategory`）。

### 🌐 本地化更正
- **货币单位校准**：语言文件中将雇佣费用词条中的“钱币”统一修正为“铜币”（`zh_cn.json` 与 `zh_tw.json`）。

### 📦 版本同步约束
- 版本号对齐前置升至 `1.3.0`，确立后续维护仅更新小版本号（`1.3.x`）的规范。

---

## [v1.2.0] - 2026-11-10

### 📐 画布溢出治理与变体整合
- **建筑详情页 Box 收敛**：合并 3 个独立的变体建材 Box 为单一自适应变体建材 Box（总高 221px <= 225px），标题动态展示变体总数，根治 76px 画布溢出。
- **满级总和页 (a_All) 与内部分页**：修复变体大小写识别问题，新增满级汇总页面，建筑全量变体支持在 JEI 内部无缝翻页。
- **村民语义安全**：明确区分村民职业前置需求与产出带回物资，避免混淆。

### 🖼️ 网格与视觉对齐
- **全景网格动态化**：解除 40 格固定数量截断，改为按实际数据动态绘制槽位，纯色半透高光边框对齐建材页视觉风格。
- **本地化样式解耦**：LinkBox 全面解耦样式控制符与文本，支持 9 国语言包。

### 🤖 百科导出命令
- **面向 Agent 检索导出**：新增客户端指令 `/milljeimm export encyclopedia`，按单行行优先输出中英双轨标签化百科数据。

---

## [v1.1.0] - 2026-11-10

### 🧭 现代导航门面与代码收敛
- **定向导航门面落地**：实现 `JeiNavigationHelper`（封装 `showRecipes` 与 `showTypes` API，配合原版点击音效）。
- **全景分类泛型基类**：抽象 `BaseMillPanoramicCategory`，统一收敛 11 个二级子分类，消除重复样板代码。
- **声明式组件化**：升级 `JeiLinkBox` 与 `JeiLinkLayout`，支持函数式提取与空 Box 自动隐藏。

---

## [v1.0.0]
- 初始版本：提供千年村庄文化、村庄、建筑、村民要素的 JEI 扩展展示。
