# Turboism 插件模板

[English](README.md) / [简体中文](README.zh-CN.md)

用于构建 Turboism 插件的独立 Gradle 工程模板，**无需克隆或编译 Turboism 主仓库**。只需运行一次初始化脚本即可自动下载并校验 SDK 与 `@SubscribeEvent` 注解处理器、配置插件元数据，并打包出可直接放入 Turboism 安装目录的插件 JAR。

插件 SDK API 范围为 `[0.67.0,1.0.0)`，下限表示所需的最低框架基础版本，忽略 beta/nightly/SNAPSHOT 后缀。运行时版本来源与兼容策略见[插件 API 兼容性](https://github.com/turboism/Turboism/blob/main/compatibility/plugin-api.md)。

插件生命周期、线程、设置、存储、权限和测试规范见[插件作者指南（英文）](https://github.com/turboism/Turboism/blob/main/sdk/plugin-author-guide.md)。

## 环境要求

- **JDK 17**（或更高版本 —— 编译目标为 Java 17 字节码）
- `curl` 与 `python3`（Linux/macOS）或 PowerShell 5.1+（Windows），用于运行初始化脚本
- 仓库已内置 Gradle Wrapper（`./gradlew` / `gradlew.bat`，Gradle 8.10.2）

## 快速开始

1. **创建工程** —— 在 [`turboism/turboism-plugin-template`](https://github.com/turboism/turboism-plugin-template) 页面点击 **Use this template**，或直接克隆：

   ```bash
   git clone https://github.com/turboism/turboism-plugin-template.git my-turboism-plugin
   cd my-turboism-plugin
   ```

2. **运行初始化脚本** —— 自动下载 `turboism-sdk-<version>.jar` 与 `turboism-event-processor-<version>.jar`，校验 `.sha256` 摘要，并配置插件 ID、显示名称、Java 包名、作者及 Cubism Editor 版本支持声明：

   ```bash
   # 交互式配置（Linux / macOS / Git Bash）
   ./scripts/setup.sh

   # 非交互式（CI 或命令行参数）
   ./scripts/setup.sh --channel stable \
     --id com.example.myplugin \
     --name "My Plugin" \
     --package com.example.myplugin \
     --author "Ada Lovelace" \
     --yes
   ```

   在 Windows PowerShell 下运行：

   ```powershell
   .\scripts\setup.ps1
   # 或非交互式：
   .\scripts\setup.ps1 -Channel stable -Id com.example.myplugin -Name "My Plugin" -Package com.example.myplugin -Author "Ada Lovelace" -Yes
   ```

3. **运行测试并构建插件 JAR**：

   ```bash
   ./gradlew build        # Windows 下使用 .\gradlew.bat build
   ```

   `build` 会执行 JUnit 5 单元测试（`./gradlew test`），并将构建产物输出到 `build/libs/<slug>-turboism-plugin-0.1.0.jar`（默认为 `build/libs/hello-turboism-plugin-0.1.0.jar`）。运行 `setup.sh` / `setup.ps1` 时会同步重命名 `src/test/java` 下对应的 `*Test.java`。

4. **安装插件** —— 将生成的 JAR 复制到 Turboism 安装目录下的 `plugins/` 文件夹，在“插件管理”中启用并重启即可。

## 渠道与版本选择

`setup.sh` / `setup.ps1` 优先通过 `https://api.turboism.dev/v1/releases/<channel>.json` 解析发布版本，不可用时回退至 GitHub Releases API：

- **稳定版渠道（默认）**：`./scripts/setup.sh --channel stable`
- **测试版渠道（Beta）**：`./scripts/setup.sh --channel beta`
- **指定版本**：`./scripts/setup.sh --version 0.67.4`

选定的版本号会写入 `gradle.properties` 的 `turboismSdkVersion`，校验通过的 JAR 放入 `libs/`。若所选版本早于独立 SDK 分发阶段（不含 `turboism-sdk-<version>.jar`），脚本会给出明确提示（改用 `--channel beta` 或 `--version <含 SDK 的版本>`）；若所选版本仅含 SDK 而不含 `turboism-event-processor-<version>.jar`，脚本会打印警告并仅配置 SDK 编译（此时请使用 `EventBus#subscribe`）。脚本具有幂等性，重复运行会安全替换旧版本 JAR 并就地更新包名、类名和描述文件。

## 离线与手动配置方式

1. **手动下载 Release 附件**：从 <https://github.com/turboism/Turboism/releases> 下载 `turboism-sdk-<version>.jar` 与 `turboism-event-processor-<version>.jar`（及对应 `.sha256` 文件），校验通过后放入 `libs/` 目录。
2. **本地 Maven 仓库（`mavenLocal()`）**：在 Turboism 主仓库执行 `./gradlew :sdk:publishToMavenLocal :event-processor:publishToMavenLocal`，当 `libs/` 为空时构建会自动从 `mavenLocal()` 解析依赖。

## Cubism Editor 版本适配

- 所有面向 Cubism 的 SDK API 均通过 `@CubismEditor` 标注支持的宿主版本。调用前请优先使用 `isAvailable()` 检查可用性，并在功能边界捕获 `CubismEditorApiUnavailableException` 进行优雅降级。
- 插件通过 `src/main/resources/META-INF/turboism/cubism-versions.json` 声明支持的 Cubism Editor 版本集合。初始化脚本默认读取发布 API `compatibility` 字段中的全部已审核版本（也可通过 `--cubism-versions` 指定）。若插件依赖 Cubism 宿主环境，请传入 `--requires-cubism true` 或在 `plugin.json` 的 `environment.requiresCubism` 中设为 `true`。

## 模板维护说明

`turboism/turboism-plugin-template` 是插件模板的独立权威源，同时服务于社区插件与独立仓库形态的第一方插件。

默认 SDK 版本与已审核 Cubism Editor 版本列表通过 `.github/workflows/sync-sdk-version.yml`（每日定时及手动 `workflow_dispatch`）跟踪 [`turboism/Turboism`](https://github.com/turboism/Turboism) 发布：发现新版本时仅更新 `gradle.properties` 中的 `turboismSdkVersion` 与 `src/main/resources/META-INF/turboism/cubism-versions.json`，并自动发起 Pull Request，不会覆盖模板源码。
