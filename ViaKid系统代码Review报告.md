# ViaKid 系统代码 Review 与优化建议报告

## 1. Review 概况

**Review 时间**：2026-05-19  
**Review 范围**：`ViaKid-Server` 后端、`ViaKidDriver` Android 司机端、`miniprogram-3` 微信小程序家长端。  
**参考资料**：三端源码、数据库设计文档与本机复现情况。  
**审查定位**：本报告面向已有 ViaKid 项目的代码质量、需求完整度、安全性和后续迭代优化，不以本地 Git 工作区状态作为评价依据。

### 1.1 审查对象

| 模块 | 目录 | 主要关注点 |
| --- | --- | --- |
| 后端服务 | `ViaKid-Server` | 认证、订单、司机、培训、配置、安全与数据库访问 |
| 司机端 Android | `ViaKidDriver` | 登录态、网络请求、任务看板、构建配置、敏感日志 |
| 家长端小程序 | `miniprogram-3` | 登录、订单、投诉、请求封装、页面流程与接口契约 |
| 数据库文档 | `ViaKid-DataBase` | 表结构是否支撑订单闭环、异常处理、状态历史和监管能力 |

### 1.2 验证结果

| 项目 | 命令 | 结果 |
| --- | --- | --- |
| 后端 | `./gradlew.bat compileKotlin` | 通过 |
| Android | `./gradlew.bat compileDebugKotlin` | 通过 |
| 小程序 | 微信开发者工具运行 | 可正常运行和预览 |
| 小程序 | `corepack pnpm@10.30.1 exec tsc --noEmit` | TypeScript 已可执行；发现未使用变量、`showToast` 类型、`Promise.finally` lib 配置和小程序 typings 兼容问题 |

说明：小程序能在微信开发者工具中正常运行。`tsc --noEmit` 属于额外的命令行静态质量检查，它暴露的是类型规范、依赖类型定义和 `tsconfig` 兼容性问题，不等同于小程序运行失败。

### 1.3 总体结论

**总体评估**：`REQUEST_CHANGES`

当前系统具备多端页面、后端模块和数据库基础，已能支撑基本演示和本机复现。但从真实儿童接送平台角度看，项目仍偏向“页面和模型较完整，真实业务闭环不足”。后续优化应优先围绕真实下单到接单、状态同步、权限隔离、异常处理和敏感配置治理展开。代码层面存在多处会影响真实上线或长期维护的问题，包括凭据硬编码、Release 签名信息入库、宽松 CORS、mock 登录和 mock 订单与主流程耦合、接口契约不一致、请求校验不足、未限制未认证司机接单等。

## 2. Findings

### P0 - Critical

1. **`ViaKid-Server/src/main/resources/application.yaml:13` 数据库密码直接写入源码**

   当前配置把 SQL Server 用户 `sa` 的明文密码写入源码配置。该问题一旦随项目传播，会导致本机、测试环境或演示环境数据库凭据泄露，也会强化开发者继续把真实环境配置写入源码的习惯。

   **建议修复**：将数据库 URL、用户名、密码迁移到环境变量或本地私有配置；项目只保留 `application.example.yaml`。如果该密码已在真实环境使用，应立即轮换。

2. **`ViaKidDriver/app/build.gradle.kts:44-62` Release 签名文件路径和密码硬编码**

   Android Release keystore 路径、`storePassword`、`keyAlias`、`keyPassword` 均直接写在 Gradle 文件中。该问题属于高危发布凭据泄露，攻击者拿到 keystore 后可能伪造同包名应用或影响后续发布可信度。

   **建议修复**：将签名配置迁移到 `local.properties`、环境变量或 CI secret；仓库保留占位读取逻辑，不保留真实路径和密码。若该 keystore 已用于发布或对外分发，应更换 keystore 或按平台规则执行密钥轮换。

### P1 - High

3. **`ViaKidDriver/app/src/main/java/com/viakid/driver/data/remote/ApiClient.kt:26` 与 `miniprogram-3/miniprogram/utils/request.ts:4` API 地址写死为本机调试地址**

   Android 当前使用 `http://10.0.2.2:8900/api/v1/`，小程序使用 `http://127.0.0.1:8900/api/v1`。这类配置适合本机复现，但不适合团队协作、真机联调、测试环境或生产部署。小程序在微信开发者工具中可以运行，但运行可行不代表环境配置具备可迁移性。

   **建议修复**：按环境拆分 `dev/test/prod` 配置。Android 使用 `BuildConfig` 或 product flavor 注入 `BASE_URL`；小程序通过环境配置文件或构建变量注入。README 中给出不同运行端的地址说明。

4. **`ViaKidDriver/app/src/main/java/com/viakid/driver/data/remote/ApiClient.kt:68` Release 网络日志会输出完整请求与响应**

   `LogLevel.BODY` 会记录请求体、响应体和可能的 `Authorization`、手机号、订单、儿童信息等敏感数据。儿童接送场景中，订单地址、孩子姓名、学校和联系方式均属于敏感信息。

   **建议修复**：按构建类型设置日志级别，Debug 可保留精简日志，Release 禁用 BODY 并对 token、手机号、地址、儿童信息脱敏。

5. **`ViaKid-Server/src/main/kotlin/com/viakid/server/plugins/HTTP.kt:16-24` CORS 使用 `anyHost()`**

   后端允许任意来源跨域访问，且允许 `Authorization` 请求头。若未来部署到公网或接入 Web 管理端，任意站点都可以向 API 发起带授权头的请求，扩大攻击面。

   **建议修复**：生产环境改为白名单域名；开发环境可通过配置显式启用 `anyHost()`。同时增加安全响应头和环境区分。

6. **`miniprogram-3/miniprogram/pages/login/login.ts:248-258` 与 `306` 家长端登录仍使用固定验证码/密码和 mock token**

   登录逻辑直接接受 `123456`，并写入 `mock_token_*`。这能帮助页面演示和开发者工具预览，但如果作为主流程保留，会导致家长端所有需要后端鉴权的接口无法形成真实登录态，也会掩盖真实注册、短信登录、token 过期、刷新和权限校验问题。

   **建议修复**：登录页必须调用后端认证接口，mock 登录只允许在显式 dev 开关下使用，并在 UI 和代码路径上与真实登录分离。

7. **`miniprogram-3/miniprogram/utils/request.ts:39-41` 响应码判断与后端契约不一致**

   后端成功响应为 `ApiResponse(code = 0, message = "success")`，小程序请求封装却只把 `res.data.code === 200` 视为成功。这会导致小程序即使调用真实后端成功，也会进入失败分支。

   **建议修复**：统一 API 契约，至少将小程序成功码改为后端当前的 `0`，并建立 OpenAPI/Apifox 文档作为三端唯一接口来源。

8. **`ViaKid-Server/src/main/kotlin/com/viakid/server/service/OrderService.kt:132-145` 未校验司机认证/培训状态即可接单**

   迭代建议书明确要求“未完成认证或培训的司机不能接单”。当前 `acceptOrder` 只校验订单是否为 `pending`，没有检查 `Drivers.status`、资质、培训、考试或证书状态。未认证司机可以被分配儿童接送任务。

   **建议修复**：在 `acceptOrder`、`grabOrder` 入口统一校验司机准入状态；建议封装 `DriverEligibilityService`，检查身份认证、证件审核、培训考试、证书有效期和在线状态。

9. **`ViaKid-Server/src/main/kotlin/com/viakid/server/route/OrderRoutes.kt:49-53` 订单详情缺少司机归属校验**

   `/orders/{orderId}` 处于 JWT 保护下，但服务层 `getOrderDetail(orderId)` 只按订单 ID 查询，不校验当前司机是否拥有该订单。任何已登录司机只要枚举或获得订单 UUID，就可能查看其他订单的儿童、家长和路线信息。

   **建议修复**：订单详情接口传入 `driverId`，服务层限定 `(Orders.id eq orderId) and (Orders.driverId eq driverId)`；对于可抢单详情需另设权限规则，只返回最小必要信息。

10. **`ViaKid-Server/src/main/kotlin/com/viakid/server/plugins/RequestValidation.kt:6-9` 请求校验为空**

    后端安装了 RequestValidation，但没有任何 DTO 校验规则。手机号、密码、页码、size、订单状态、证件类型、日期字符串、金额等输入都可能以非法值进入服务层，导致异常、脏数据或绕过业务规则。

    **建议修复**：为认证、订单、司机资料、上传、状态更新等核心 DTO 增加校验；限制 `page >= 1`、`size` 上限、手机号格式、密码长度、状态枚举、日期格式和证件类型枚举。

11. **`ViaKid-Server/src/main/kotlin/com/viakid/server/service/FileService.kt:12-16` 文件上传缺少大小、类型和扩展名控制**

    当前直接 `readBytes()` 并按原文件扩展名保存。攻击者可上传超大文件导致内存压力，也可能上传非图片或危险扩展名。证件照和头像属于高频入口，应有明确上传策略。

    **建议修复**：限制上传大小、MIME 类型和扩展名；使用流式写入；对图片进行格式识别和重编码；上传目录禁止执行；失败时返回统一业务错误。

### P2 - Medium

12. **`ViaKid-Server/src/main/kotlin/com/viakid/server/service/OrderService.kt:235-253` 订单列表存在 N+1 查询**

    `getOrders` 查询订单列表后，每个订单在 `rowToOrderDto` 内再次查询 `OrderChildren`，并调用 `loadParentInfo`。当订单数量增大时会产生 N+1 查询，接口延迟随列表长度线性放大。

    **建议修复**：批量查询当前页所有订单的 children 和 parent，再按 `orderId` 分组组装 DTO；列表接口可默认只返回摘要字段，详情接口再返回完整儿童信息。

13. **`ViaKid-Server/src/main/kotlin/com/viakid/server/service/AuthService.kt:41-45` 密码登录会自动创建司机账号**

    `login` 在手机号不存在时直接 `createDriver`，绕过短信验证码和注册流程。该行为容易产生垃圾账号，也不符合“手机号所有权确认”的认证逻辑。

    **建议修复**：密码登录仅允许已注册用户登录；新用户必须走短信验证码注册。若保留演示自动注册，应放在 seed 或 dev-only 接口中。

14. **`ViaKid-Server/src/main/kotlin/com/viakid/server/plugins/StatusPages.kt:21-28` 未处理异常会把内部错误信息返回给客户端**

    `message = cause.message` 可能暴露数据库错误、SQL 约束、文件路径、内部类名或堆栈相关信息。对外接口应返回通用错误，同时服务端日志保留细节。

    **建议修复**：生产响应统一返回“服务器内部错误”或错误码；仅在开发环境返回详细信息。

15. **`miniprogram-3/miniprogram/pages/order/order.ts:10-105` 订单页主流程仍初始化 mock 订单**

    页面加载即写入本地 `orderList`。该做法对演示友好，也解释了小程序可以在微信开发者工具中正常运行；但从业务闭环角度看，它会让家长端订单展示与后端真实订单脱节，导致“真实下单 -> 真实接单 -> 状态同步”无法在该页面被验证。

    **建议修复**：把 mock 数据迁移到独立 dev/mock 模块；正式订单页调用真实 API；提供数据库 seed 或一键演示数据替代本地假数据。

16. **`miniprogram-3/miniprogram/utils/api.ts:37-58` 小程序订单接口路径与后端实际路径不一致**

    小程序使用 `/order/create`、`/order/list`、`/order/detail/{id}`，后端当前暴露的是司机侧 `/orders`、`/orders/{id}` 等接口。接口命名、资源复数、成功码、角色模型均未统一。

    **建议修复**：先补齐家长端 API 设计，再统一 OpenAPI 文档；客户端按契约生成或校验接口路径、枚举和响应结构。

17. **`ViaKidDriver/app/src/main/java/com/viakid/driver/data/local/TokenManager.kt:113-120` `getAccessTokenSync` 会无限收集 Flow**

    `DataStore.data.collect` 是持续流，当前函数没有 `first()` 或取消条件，因此一旦被调用会挂起不返回。当前检索未发现调用点，但保留该方法会带来后续误用风险。

    **建议修复**：删除该方法，或改为 `return dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()`。

18. **`ViaKidDriver/app/src/main/java/com/viakid/driver/ui/screen/auth/AuthViewModel.kt:469-483` 开发者快捷登录始终可用**

    该入口会写入 mock access/refresh token，并标记已登录。即使目前真实后端会拒绝该 token，该代码仍会制造“客户端已登录但接口 401”的不一致状态。

    **建议修复**：仅 Debug 构建暴露开发者登录；Release 构建移除入口；mock token 不应写入与真实 token 相同的持久化位置。

19. **`ViaKidDriver/app/src/main/java/com/viakid/driver/ui/screen/taskboard/TaskBoardScreen.kt:183-186` 代码格式与大括号结构可读性差**

    当前任务看板虽然通过编译，但 `LazyColumn`、`Scaffold` 和函数闭包缩进错乱，代码维护者难以快速判断层级。类似格式问题容易在后续 UI 改动中引入 Compose 布局 bug。

    **建议修复**：使用 Android Studio/KtLint 统一格式化；删除无意义的 `@param` 块注释，保留必要业务注释。

20. **`ViaKidDriver/app/build.gradle.kts:29-37` 版本号计算改变了原 `versionCode` 量级**

    原 `versionCode` 为 `30000`，新公式将 `0.3.1` 计算为 `301`。如果历史包已经以 `30000` 发布，后续 `301` 不能覆盖升级。

    **建议修复**：保持 `versionCode` 单调递增，或将公式调整为兼容旧版本号体系，例如 `major * 1000000 + minor * 10000 + patch * 100 + build`。

### P3 - Low

21. **项目缺少统一代码格式与换行规范**

    多端项目涉及 Kotlin、TypeScript、JSON、Markdown、Gradle 等多种文件类型，如果没有统一格式化和换行规范，团队协作时容易产生大量无业务意义的格式差异，增加代码审查成本。

    **建议修复**：增加 `.editorconfig` 和 `.gitattributes`，统一缩进、换行、文件编码；后端和 Android 可引入 ktlint 或 detekt，小程序可引入 prettier/eslint。

22. **小程序项目配置缺少共享配置与本地配置边界**

    小程序项目中同时存在团队共享配置和开发者工具本地配置。若 `appid`、基础库版本、域名校验、编译选项等配置没有明确归属，后续换机器复现或多人协作时容易出现“本机能运行，别人环境不一致”的问题。

    **建议修复**：明确 `project.config.json` 只保存团队共享配置；个人环境放入 `project.private.config.json` 或本地说明；README 中记录推荐基础库版本、是否关闭合法域名校验、后端联调地址配置方式。

## 3. 需求不足与改进方向

### 3.1 核心业务闭环不足

迭代建议书已经明确下一轮主线应围绕：

`真实下单 -> 真实接单 -> 状态流转 -> 状态同步 -> 接送确认 -> 异常处理 -> 平台介入 -> 权限控制`

当前代码中，家长端订单和登录仍以 mock 为主，司机端依赖本机地址和 token 状态，后端缺少家长侧下单接口、异常处理入口和平台介入流程。因此当前系统还不能证明儿童接送业务闭环。

**改进方向**：

| 优先级 | 方向 | 验收标准 |
| --- | --- | --- |
| P0 | 打通家长真实下单与司机真实接单 | 小程序创建真实订单，后端落库，司机端可接单，家长端看到状态变化 |
| P0 | 建立后端订单状态机与状态历史 | 非法状态流转被拒绝；每次变更记录操作人、时间、前后状态 |
| P0 | 接送确认与二次确认 | “已接到孩子”“已送达”必须二次确认并留痕 |
| P0 | 异常订单入口 | 家长和司机均可提交迟到、孩子未出现、联系不上等异常 |
| P0 | 儿童信息权限控制 | 家长只看自己的孩子和订单；司机只看自己接单或可抢单的最小必要信息 |
| P1 | 平台后台介入 | 管理员可查看订单、状态历史、异常、投诉并处理 |
| P1 | 认证培训准入 | 未认证、未培训、资质过期司机不能接单 |

### 3.2 接口契约不足

三端接口契约不一致：后端成功码是 `0`，小程序判断 `200`；后端司机端接口是 `/orders`，小程序订单接口是 `/order/list` 等虚拟路径；订单状态在不同端以字符串、数字、中文标签混用。

**改进方向**：

1. 建立 OpenAPI/Apifox 作为唯一接口契约。
2. 统一成功码、错误码、分页格式、状态枚举和角色权限。
3. 客户端模型从契约生成或至少通过类型测试校验。
4. mock 数据只能存在于显式 mock 层，不进入生产页面主流程。

### 3.3 儿童安全与隐私需求不足

订单 DTO 会返回儿童姓名、年龄、年级、过敏史、特殊备注、家长信息和完整地址。当前权限控制不够细，订单详情缺少司机归属校验，列表接口也返回较多敏感信息。

**改进方向**：

1. 列表接口只返回展示所需摘要，详情接口按角色返回不同字段。
2. 可抢单阶段隐藏儿童姓名、家长电话、详细地址等敏感信息，只展示时间、区域和任务类型。
3. 已接单司机才可查看完整执行信息。
4. 后台日志和客户端日志不得输出儿童、家长、地址和 token。

## 4. 代码规范性评价

### 4.1 后端

优点：模块分层基本清晰，`route/service/model/table` 目录结构明确，Ktor、Koin、Exposed、Flyway 组合符合项目规模。

主要问题：

1. 配置与源码耦合，敏感配置没有外置。
2. DTO 校验缺失，错误多依赖服务层抛异常。
3. 业务状态使用裸字符串，缺少类型化枚举和集中状态机。
4. 服务层部分方法承担查询、组装、权限判断、状态变更多重职责，后续应拆分状态机、权限策略和 DTO assembler。

### 4.2 Android 端

优点：Hilt、Repository、ViewModel、Compose 分层已经具备基础形态；任务看板嵌套滚动问题已通过横向 Row 思路修复。

主要问题：

1. 构建配置泄露签名凭据。
2. 网络地址和日志级别缺少构建类型隔离。
3. 开发者登录与真实登录共享持久化 token 状态。
4. 大量注释解释语法参数而非业务意图，降低可读性。
5. 版本号公式可能破坏升级路径。

### 4.3 小程序端

优点：页面覆盖面较广，登录、订单、投诉、儿童管理等入口较完整。

主要问题：

1. 主流程仍依赖 mock 登录和本地订单数据。
2. 请求封装与后端成功码不一致。
3. 接口路径和后端真实路由不一致。
4. 微信开发者工具可正常运行，但命令行 `tsc --noEmit` 仍存在静态类型检查问题，需要作为工程质量改进项处理。
5. 项目配置和 appid 需要区分团队共享配置与个人本地配置，避免环境迁移时产生歧义。

## 5. 效率与性能评价

| 位置 | 问题 | 影响 | 改进 |
| --- | --- | --- | --- |
| `OrderService.rowToOrderDto` | 每个订单单独查 children | 订单列表增长时出现 N+1 查询 | 批量查询并按 orderId 分组 |
| `DriverRoutes` 文件上传 | `readBytes()` 一次性读入内存 | 大文件可能造成内存压力 | 限制大小并流式写入 |
| 小程序订单页 | 每次页面显示都从 storage 加载 mock 数据 | 可支撑开发者工具演示，但无法体现真实接口性能，也可能造成状态不一致 | 统一走 API，使用分页和下拉刷新 |
| Android 网络日志 | BODY 日志输出完整响应 | Release 性能和隐私风险 | Release 关闭，Debug 精简 |

## 6. 安全性评价

### 6.1 高风险安全问题

1. 数据库密码、JWT secret、Android 签名密码硬编码。
2. CORS 任意来源开放。
3. 家长端 mock token、司机端 dev token 混入真实登录态。
4. 订单详情存在潜在 IDOR 风险。
5. 文件上传缺少大小、类型和内容校验。
6. 后端错误响应可能暴露内部异常消息。

### 6.2 儿童隐私专项风险

儿童接送平台处理未成年人姓名、学校、住址、路线、过敏史、监护人联系方式等高敏感数据。当前系统在日志、DTO、接口权限、mock 数据和详情展示上没有形成系统性的最小权限策略。

**建议建立安全基线**：

1. 所有敏感配置不得进入 Git。
2. 所有儿童与订单数据访问必须经过角色和归属校验。
3. 所有日志默认脱敏 token、手机号、地址、儿童姓名。
4. 可抢单列表只展示最小必要字段。
5. Release 构建禁止开发者登录、BODY 日志和 mock token。

## 7. Removal / Iteration Plan

### 7.1 Safe to Remove Now

| 项目 | 位置 | 理由 | 验证 |
| --- | --- | --- | --- |
| 明文数据库密码 | `ViaKid-Server/src/main/resources/application.yaml` | 安全风险，必须外置 | 后端通过环境变量启动 |
| Android Release 签名密码 | `ViaKidDriver/app/build.gradle.kts` | 发布凭据泄露 | Release 构建从本地/CI secret 读取 |
| 小程序 mock 登录主路径 | `miniprogram/pages/login/login.ts` | 适合演示，不适合作为真实认证主流程 | 登录后拿到后端真实 token |
| 小程序订单页自动 mock 初始化 | `miniprogram/pages/order/order.ts` | 适合演示，不适合作为真实订单主流程 | 订单列表来自后端 |
| `getAccessTokenSync` | `ViaKidDriver/.../TokenManager.kt` | 当前未使用且实现会挂起 | 删除后 Android 编译通过 |

### 7.2 Defer Removal With Plan

| 项目 | 为什么暂缓 | 前置条件 |
| --- | --- | --- |
| 客户端 mock 数据体系 | 需要保留演示和离线开发能力 | 建立 `dev/mock` 开关，真实流程默认关闭 |
| README 中本地运行地址 | 对复现有帮助 | 区分“本地调试地址”和“正式配置方式” |
| Android 开发者快捷登录 | 对调试有价值 | 仅 Debug 编译可见，Release 编译剔除 |

## 8. 建议迭代顺序

1. **安全配置治理**：移除所有明文密码、secret、签名凭据，建立 example 配置和环境变量读取。
2. **统一接口契约**：确定成功码、错误码、订单状态、认证状态、分页结构和三端路由。
3. **真实登录闭环**：小程序、Android 均接入真实认证，token 失效和刷新逻辑一致。
4. **订单闭环**：家长下单、司机接单、状态流转、状态同步。
5. **准入与权限**：司机认证培训限制接单，订单详情按角色和归属授权。
6. **异常与确认**：二次确认、异常上报、状态历史、平台介入。
7. **测试与复现**：补充接口测试、状态机测试、越权访问测试、构建脚本和演示 seed。

## 9. 测试缺口

当前未发现系统性测试文件。建议优先补齐：

| 测试类型 | 覆盖目标 |
| --- | --- |
| 后端单元测试 | 订单状态机、司机准入、AuthService 注册/登录/刷新 |
| 后端集成测试 | Flyway 迁移、订单接口权限、越权访问拦截 |
| 小程序类型检查 | 请求封装、接口返回类型、订单状态映射；不影响微信开发者工具运行，但建议纳入 CI |
| Android ViewModel 测试 | 登录态、token 刷新、任务看板筛选 |
| 安全回归测试 | 未认证司机接单、他人订单详情、文件上传限制 |

## 10. Next Steps

本次 Review 共发现 **22 个问题**：P0 2 个、P1 9 个、P2 9 个、P3 2 个。

建议下一步先处理 P0/P1，尤其是凭据外置、签名配置外置、真实登录、接口契约统一、订单权限和司机准入。完成后再补 N+1 查询、测试体系和 mock 隔离。
