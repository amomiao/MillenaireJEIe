package com.momos.millenairejeimm.jei;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// region 工具类声明与成员变量
/**
 * JEI 开发者 Debug 模式通用 GUI 绘制工具类 {@link DebugGuiUtils}。
 * 提供暗色 Debug 终端面板、属性徽章（Badge）、动态悬浮 Inspector 及全量对象 JSON Dump 大窗口预览功能。
 */
public class DebugGuiUtils {

    /** Gson 格式化序列化器，用于生成带缩进的 Pretty Print JSON 文本 */
    /**
     * 注册 {@link Optional} 序列化适配器，解决 Java 17/21 强封装下反射 JDK 内部字段导致的 InaccessibleObjectException 异常。
     */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .registerTypeAdapter(Optional.class, (JsonSerializer<Optional<?>>) (src, typeOfSrc, context) ->
                    src.isPresent() ? context.serialize(src.get()) : JsonNull.INSTANCE
            )
            .create();
// endregion 工具类声明与成员变量

// region 原有工具绘制方法
    /**
     * 绘制暗色开发者 Debug 控制台面板底图。
     *
     * @param guiGraphics GUI 渲染上下文 {@link GuiGraphics}
     * @param x 相对 X 坐标
     * @param y 相对 Y 坐标
     * @param width 面板宽度
     * @param height 面板高度
     */
    public static void drawDebugConsoleBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // 绘制终端暗色背景 (0xFF1E1E1E) 与灰色边框 (0xFF3C3C3C)
        guiGraphics.fill(x, y, x + width, y + height, 0xFF1E1E1E);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF3C3C3C);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF3C3C3C);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF3C3C3C);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF3C3C3C);
    }

    /**
     * 判断鼠标是否处于指定坐标区域内。
     *
     * @param mouseX 鼠标当前 X 坐标
     * @param mouseY 鼠标当前 Y 坐标
     * @param x 矩形起点 X
     * @param y 矩形起点 Y
     * @param width 矩形宽度
     * @param height 矩形高度
     * @return 若鼠标在矩形范围内则返回 true
     */
    public static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /**
     * 当鼠标悬停在 Debug 控制台面板上时，弹出全量字段结构的悬浮 Tooltip。
     *
     * @param font 字体渲染器 {@link Font}
     * @param guiGraphics GUI 上下文 {@link GuiGraphics}
     * @param debugLines 扩展字段文本列表
     * @param mouseX 鼠标当前 X 坐标
     * @param mouseY 鼠标当前 Y 坐标
     * @param panelX 控制台 X
     * @param panelY 控制台 Y
     * @param panelW 控制台宽度
     * @param panelH 控制台高度
     */
    public static void renderHoverInspector(Font font, GuiGraphics guiGraphics, List<Component> debugLines, double mouseX, double mouseY, int panelX, int panelY, int panelW, int panelH) {
        if (isMouseOver(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
            guiGraphics.renderComponentTooltip(font, debugLines, (int) mouseX, (int) mouseY);
        }
    }
// endregion 原有工具绘制方法

// region 标题行快捷 Debug 按钮渲染与交互逻辑
    /**
     * 在 Category 标题行右侧绘制极简紧凑的 JSON Dump 调试按钮徽章。
     *
     * @param guiGraphics GUI 渲染上下文 {@link GuiGraphics}
     * @param font 字体渲染器 {@link Font}
     * @param btnX 按钮起始 X 坐标
     * @param btnY 按钮起始 Y 坐标
     * @param mouseX 当前鼠标 X 坐标
     * @param mouseY 当前鼠标 Y 坐标
     */
    public static void drawJsonDevButton(GuiGraphics guiGraphics, Font font, int btnX, int btnY, double mouseX, double mouseY) {
        boolean hovered = isMouseOver(mouseX, mouseY, btnX, btnY, 26, 12);
        int bgColor = hovered ? 0xFF3D85C6 : 0xFF2A2A2A;
        int borderColor = hovered ? 0xFF9FC5E8 : 0xFF555555;
        int textColor = hovered ? 0xFFFFFFFF : 0xFFCCCCCC;

        // 绘制按压/悬停态按钮框
        guiGraphics.fill(btnX, btnY, btnX + 26, btnY + 12, bgColor);
        guiGraphics.fill(btnX, btnY, btnX + 26, btnY + 1, borderColor);
        guiGraphics.fill(btnX, btnY, btnX + 1, btnY + 12, borderColor);
        guiGraphics.fill(btnX + 25, btnY, btnX + 26, btnY + 12, borderColor);
        guiGraphics.fill(btnX, btnY + 11, btnX + 26, btnY + 12, borderColor);

        // 按钮文字使用 dropShadow=true 保持清晰
        guiGraphics.drawString(font, Component.literal("JSON"), btnX + 3, btnY + 2, textColor, true);

        // 鼠标悬停显示 Tooltip 提示
        if (hovered) {
            List<Component> tooltip = List.of(
                    Component.literal("§a🔍 点击打开 JSON / Inspector 深度诊断大窗口"),
                    Component.literal("§7内置双页签展示，支持搜索预览与一键复制")
            );
            guiGraphics.renderComponentTooltip(font, tooltip, (int) mouseX, (int) mouseY);
        }
    }

    /**
     * 判定并处理 JSON 按钮点击事件，成功触发时弹出全屏 JSON Dump Screen 窗口。
     *
     * @param recordObj 需要被转为 JSON 的 Record/Class 数据对象
     * @param mouseX 鼠标点击 X 坐标
     * @param mouseY 鼠标点击 Y 坐标
     * @param btnX 按钮 X 坐标
     * @param btnY 按钮 Y 坐标
     * @return 若成功触发点击则返回 true
     */
    public static boolean handleJsonButtonClick(Object recordObj, double mouseX, double mouseY, int btnX, int btnY) {
        return handleJsonButtonClick(recordObj, List.of(), mouseX, mouseY, btnX, btnY);
    }

    // [醒目新注释] 重载 handleJsonButtonClick，支持将悬浮 Inspector 内容一同作为页签数据传入
    /**
     * 判定并处理 JSON 按钮点击事件，将对象与其悬浮 Inspector 属性内容一并送入大窗口。
     *
     * @param recordObj 数据对象
     * @param debugLines 悬浮 Inspector 的 {@link Component} 列表
     * @param mouseX 鼠标 X
     * @param mouseY 鼠标 Y
     * @param btnX 按钮 X
     * @param btnY 按钮 Y
     * @return 触发点击返回 true
     */
    public static boolean handleJsonButtonClick(Object recordObj, List<Component> debugLines, double mouseX, double mouseY, int btnX, int btnY) {
        if (isMouseOver(mouseX, mouseY, btnX, btnY, 26, 12)) {
            openJsonDumpScreen(recordObj, debugLines);
            return true;
        }
        return false;
    }

    /**
     * 将对象序列化为 JSON 并直接切入大型预览窗口 {@link JsonDumpScreen}。
     *
     * @param targetObj 待 Dump 的数据对象
     */
    public static void openJsonDumpScreen(Object targetObj) {
        openJsonDumpScreen(targetObj, List.of());
    }

    // [醒目新注释] 重载 openJsonDumpScreen，接收并传递 Inspector 悬浮文本列表
    /**
     * 序列化对象并携带悬浮 Inspector 数据打开双页签预览窗口 {@link JsonDumpScreen}。
     *
     * @param targetObj 目标数据对象
     * @param debugLines 悬浮属性行的 {@link Component} 列表
     */
    public static void openJsonDumpScreen(Object targetObj, List<Component> debugLines) {
        if (targetObj == null) return;
        String jsonText;
        try {
            jsonText = GSON.toJson(targetObj);
        } catch (Exception e) {
            jsonText = "// JSON 序列化失败: " + e.getMessage();
        }
        String titleName = targetObj.getClass().getSimpleName();
        Minecraft.getInstance().setScreen(new JsonDumpScreen(Component.literal("开发者深度诊断 - " + titleName), jsonText, debugLines));
    }
// endregion 标题行快捷 Debug 按钮渲染与交互逻辑

// region 全屏大窗口 JSON Dump 预览 Screen 控件
    /**
     * 全屏/大窗口 JSON Dump 视图控件 {@link JsonDumpScreen}，继承自原版 {@link Screen}。
     * [醒目新注释] 升级为双页签（JSON Dump 视图 / Inspector 属性视图），支持流式滚动与独立页签内容复制。
     */
    public static class JsonDumpScreen extends Screen {

        private final String rawJson;
        // [醒目新注释] 新增 Inspector 原始文本列表
        private final List<Component> debugLines;

        private List<String> jsonLines = new ArrayList<>();
        private List<String> inspectorLines = new ArrayList<>();

        /** [醒目新注释] 当前选中的页签：0 -> JSON 数据，1 -> Inspector 属性 */
        private int activeTab = 0;

        private double scrollAmount = 0;
        private boolean copySuccessNotice = false;
        private long noticeTimestamp = 0;

        // 按钮控件引用
        private Button copyButton;
        private Button closeButton;

        // [醒目新注释] 新增页签切换控件引用
        private Button tabJsonButton;
        private Button tabInspectorButton;

        // [醒目新注释] 新增滚动条拖拽状态标记
        private boolean isDraggingScrollbar = false;

        /**
         * 构造单页签 JSON 预览 Screen（兼容原有构造器）。
         *
         * @param title 窗口标题 {@link Component}
         * @param rawJson 格式化后的 JSON 字符串
         */
        public JsonDumpScreen(Component title, String rawJson) {
            this(title, rawJson, List.of());
        }

        // [醒目新注释] 支持传入 debugLines 的全新构造器
        /**
         * 构造包含 JSON 结构与 Inspector 属性的双页签 Screen。
         *
         * @param title 窗口标题 {@link Component}
         * @param rawJson 格式化后的 JSON 字符串
         * @param debugLines Inspector 属性文本行列表
         */
        public JsonDumpScreen(Component title, String rawJson, List<Component> debugLines) {
            super(title);
            this.rawJson = rawJson;
            this.debugLines = debugLines != null ? debugLines : List.of();
        }

        @Override
        protected void init() {
            super.init();
            this.jsonLines = Arrays.asList(this.rawJson.split("\n"));

            // [醒目新注释] 解析 Component 文本转为字符串展示行
            this.inspectorLines = new ArrayList<>();
            for (Component line : this.debugLines) {
                this.inspectorLines.add(line.getString());
            }

            int modalWidth = Math.max(280, (int) (this.width * 0.88));
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int modalX = (this.width - modalWidth) / 2;
            int modalY = (this.height - modalHeight) / 2;

            // [醒目新注释] 顶端右侧添加页签切换按钮
            int tabY = modalY + 3;
            this.tabJsonButton = Button.builder(Component.literal("JSON 结构"), btn -> {
                this.activeTab = 0;
                this.scrollAmount = 0;
            }).bounds(modalX + modalWidth - 195, tabY, 75, 16).build();

            this.tabInspectorButton = Button.builder(Component.literal("Inspector 属性"), btn -> {
                this.activeTab = 1;
                this.scrollAmount = 0;
            }).bounds(modalX + modalWidth - 115, tabY, 85, 16).build();

            int btnWidth = 150;
            int btnHeight = 20;
            int btnY = modalY + modalHeight - 28;

            // [醒目新注释] 点击复制时依据当前 Tab 复制对应的完整文本
            this.copyButton = Button.builder(Component.literal("📋 复制当前视图到剪贴板"), btn -> {
                String copyText = (this.activeTab == 0) ? this.rawJson : String.join("\n", this.inspectorLines);
                Minecraft.getInstance().keyboardHandler.setClipboard(copyText);
                this.copySuccessNotice = true;
                this.noticeTimestamp = System.currentTimeMillis();
            }).bounds(modalX + (modalWidth / 2) - btnWidth - 10, btnY, btnWidth, btnHeight).build();

            this.closeButton = Button.builder(Component.literal("关闭 (ESC)"), btn -> {
                this.onClose();
            }).bounds(modalX + (modalWidth / 2) + 10, btnY, 110, btnHeight).build();

            this.addRenderableWidget(this.tabJsonButton);
            this.addRenderableWidget(this.tabInspectorButton);
            this.addRenderableWidget(this.copyButton);
            this.addRenderableWidget(this.closeButton);
        }

        // [醒目新注释] 根据当前选中的页签动态获取对应的文本行列表
        private List<String> getCurrentLines() {
            return (this.activeTab == 0) ? this.jsonLines : this.inspectorLines;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // [醒目新注释] 动态禁用/高亮当前选中的 Tab 按钮外观
            if (this.tabJsonButton != null) this.tabJsonButton.active = (this.activeTab != 0);
            if (this.tabInspectorButton != null) this.tabInspectorButton.active = (this.activeTab != 1);

            // 1. 背景高斯模糊 Pass
            this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

            // 2. 提交 RenderPass 避开背景污染
            guiGraphics.flush();

            // 3. 计算自适应尺寸
            int modalWidth = Math.max(280, (int) (this.width * 0.88));
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int modalX = (this.width - modalWidth) / 2;
            int modalY = (this.height - modalHeight) / 2;

            // 绘制主窗口背景与细边框
            guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xFA141414);
            guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + 1, 0xFF444444);
            guiGraphics.fill(modalX, modalY, modalX + 1, modalY + modalHeight, 0xFF444444);
            guiGraphics.fill(modalX + modalWidth - 1, modalY, modalX + modalWidth, modalY + modalHeight, 0xFF444444);
            guiGraphics.fill(modalX, modalY + modalHeight - 1, modalX + modalWidth, modalY + modalHeight, 0xFF444444);

            // 绘制顶部标题栏背景与文本
            guiGraphics.fill(modalX + 1, modalY + 1, modalX + modalWidth - 1, modalY + 22, 0xFF222222);
            guiGraphics.drawString(this.font, this.title, modalX + 10, modalY + 7, 0x00FF00, true);

            // 4. 计算文本区域边界
            List<String> currentLines = getCurrentLines();
            int textTop = modalY + 28;
            int textBottom = modalY + modalHeight - 36;
            int lineHeight = 12;
            int visibleLines = (textBottom - textTop) / lineHeight;
            int maxScroll = Math.max(0, currentLines.size() - visibleLines);

            // [醒目新注释] 剪裁区右侧留出 16px 给滚动条，避免文本挤占滚动条视觉区域
            guiGraphics.enableScissor(modalX + 5, textTop, modalX + modalWidth - 16, textBottom);

            int startLine = (int) Math.floor(Math.max(0, this.scrollAmount));
            for (int i = 0; i < visibleLines + 1 && (startLine + i) < currentLines.size(); i++) {
                String line = currentLines.get(startLine + i);
                int lineY = textTop + (i * lineHeight);
                String lineNum = String.format("%3d | ", startLine + i + 1);

                guiGraphics.drawString(this.font, Component.literal("§6" + lineNum + "§f" + line), modalX + 10, lineY, 0xFFFFFF, true);
            }

            guiGraphics.disableScissor();

            // [醒目新注释] 5. 绘制右侧自适应交互滚动条
            if (maxScroll > 0) {
                int trackX = modalX + modalWidth - 10;
                int trackY = textTop;
                int trackWidth = 4;
                int trackHeight = textBottom - textTop;

                // 绘制深色轨道槽
                guiGraphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF222222);

                // 根据可见行比例计算滑块动态高度
                int thumbHeight = Math.max(16, (int) ((double) visibleLines / currentLines.size() * trackHeight));
                int thumbY = trackY + (int) ((this.scrollAmount / maxScroll) * (trackHeight - thumbHeight));

                // 判定鼠标悬停或拖拽时的亮色反馈
                boolean hovered = mouseX >= trackX - 2 && mouseX <= trackX + trackWidth + 2 && mouseY >= trackY && mouseY <= trackY + trackHeight;
                int thumbColor = (hovered || this.isDraggingScrollbar) ? 0xFFAAAAAA : 0xFF666666;

                // 绘制滑块
                guiGraphics.fill(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, thumbColor);
            }

            // 复制成功提示
            if (this.copySuccessNotice) {
                if (System.currentTimeMillis() - this.noticeTimestamp < 2000) {
                    guiGraphics.drawString(this.font, Component.literal("§a✔ 已成功复制当前视图！"), modalX + modalWidth - 170, modalY + 7, 0x00FF00, true);
                } else {
                    this.copySuccessNotice = false;
                }
            }

            // 6. 渲染包含页签在内的子控件
            //super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // [醒目新注释] 处理鼠标按下的拖拽起点判定
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int modalWidth = Math.max(280, (int) (this.width * 0.88));
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int modalX = (this.width - modalWidth) / 2;
            int modalY = (this.height - modalHeight) / 2;

            int textTop = modalY + 28;
            int textBottom = modalY + modalHeight - 36;
            int trackX = modalX + modalWidth - 10;

            if (button == 0 && mouseX >= trackX - 4 && mouseX <= trackX + 8 && mouseY >= textTop && mouseY <= textBottom) {
                this.isDraggingScrollbar = true;
                this.updateScrollFromMouseY(mouseY, modalY, modalHeight);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // [醒目新注释] 处理鼠标按住移动（拖拽滑块）
        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (this.isDraggingScrollbar) {
                int modalHeight = Math.max(180, (int) (this.height * 0.88));
                int modalY = (this.height - modalHeight) / 2;
                this.updateScrollFromMouseY(mouseY, modalY, modalHeight);
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        // [醒目新注释] 释放鼠标时重置拖拽状态
        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.isDraggingScrollbar = false;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        // [醒目新注释] 计算鼠标 Y 坐标到逻辑滚动行数（scrollAmount）的映射关系
        private void updateScrollFromMouseY(double mouseY, int modalY, int modalHeight) {
            List<String> currentLines = getCurrentLines();
            int textTop = modalY + 28;
            int textBottom = modalY + modalHeight - 36;
            int trackHeight = textBottom - textTop;
            int visibleLines = trackHeight / 12;
            int maxScroll = Math.max(0, currentLines.size() - visibleLines);

            if (maxScroll <= 0) return;

            int thumbHeight = Math.max(16, (int) ((double) visibleLines / currentLines.size() * trackHeight));
            double relativeY = Math.min(Math.max(0, mouseY - textTop - (thumbHeight / 2.0)), trackHeight - thumbHeight);
            this.scrollAmount = (relativeY / (trackHeight - thumbHeight)) * maxScroll;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            List<String> currentLines = getCurrentLines();
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int visibleLines = (modalHeight - 64) / 12;
            int maxScroll = Math.max(0, currentLines.size() - visibleLines);

            if (scrollY < 0) {
                this.scrollAmount = Math.min(maxScroll, this.scrollAmount + 3);
            } else if (scrollY > 0) {
                this.scrollAmount = Math.max(0, this.scrollAmount - 3);
            }
            return true;
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
// endregion 全屏大窗口 JSON Dump 预览 Screen 控件
}