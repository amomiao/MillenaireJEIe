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
                    Component.literal("§a🔍 点击打开全量 JSON Dump 深度诊断窗口"),
                    Component.literal("§7支持大屏滚动查看与一键复制到剪贴板")
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
        if (isMouseOver(mouseX, mouseY, btnX, btnY, 26, 12)) {
            openJsonDumpScreen(recordObj);
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
        if (targetObj == null) return;
        String jsonText;
        try {
            jsonText = GSON.toJson(targetObj);
        } catch (Exception e) {
            jsonText = "// JSON 序列化失败: " + e.getMessage();
        }
        String titleName = targetObj.getClass().getSimpleName();
        Minecraft.getInstance().setScreen(new JsonDumpScreen(Component.literal("JSON Dump 视图 - " + titleName), jsonText));
    }
// endregion 标题行快捷 Debug 按钮渲染与交互逻辑

// region 全屏大窗口 JSON Dump 预览 Screen 控件
    /**
     * 全屏/大窗口 JSON Dump 视图控件 {@link JsonDumpScreen}，继承自原版 {@link Screen}。
     * [醒目新注释] 重新接回底部操作按钮，并手动执行控件渲染，彻底规避 super.render() 导致的二次背景遮罩。
     * [醒目新注释] 动态自适应屏幕分辨率，最大化利用屏幕空间（固定占据 88% 屏幕宽高）。
     */
    public static class JsonDumpScreen extends Screen {

        private final String rawJson;
        private List<String> jsonLines = new ArrayList<>();
        private double scrollAmount = 0;
        private boolean copySuccessNotice = false;
        private long noticeTimestamp = 0;

        // [醒目新注释] 显式保存按钮引用，便于在不调用 super.render() 的情况下手动渲染，彻底切断二次背景覆盖
        private Button copyButton;
        private Button closeButton;

        /**
         * 构造全屏 JSON 预览 Screen。
         *
         * @param title 窗口标题 {@link Component}
         * @param rawJson 格式化后的 JSON 字符串
         */
        public JsonDumpScreen(Component title, String rawJson) {
            super(title);
            this.rawJson = rawJson;
        }

        @Override
        protected void init() {
            super.init();
            this.jsonLines = Arrays.asList(this.rawJson.split("\n"));

            // [醒目新注释] 屏幕自适应尺寸计算：优先占用 88% 的屏幕空间，保证大屏下有极其充裕的展示面积
            int modalWidth = Math.max(280, (int) (this.width * 0.88));
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int modalX = (this.width - modalWidth) / 2;
            int modalY = (this.height - modalHeight) / 2;

            int btnWidth = 150;
            int btnHeight = 20;
            int btnY = modalY + modalHeight - 28;

            // [醒目新注释] 绑定“复制 JSON 到剪贴板”按钮
            this.copyButton = Button.builder(Component.literal("📋 复制 JSON 到剪贴板"), btn -> {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.rawJson);
                this.copySuccessNotice = true;
                this.noticeTimestamp = System.currentTimeMillis();
            }).bounds(modalX + (modalWidth / 2) - btnWidth - 10, btnY, btnWidth, btnHeight).build();

            // [醒目新注释] 绑定“关闭”按钮
            this.closeButton = Button.builder(Component.literal("关闭 (ESC)"), btn -> {
                this.onClose();
            }).bounds(modalX + (modalWidth / 2) + 10, btnY, 110, btnHeight).build();

            // 注册到 Screen 内部监听链（用于处理鼠标点击响应）
            this.addRenderableWidget(this.copyButton);
            this.addRenderableWidget(this.closeButton);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // [醒目新注释] 1. 仅在此处显式调用一次 renderBackground 绘制背景高斯模糊
            this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

            // [醒目新注释] 2. 强行截断并提交背景 Pass，确保模糊 Shader 执行完毕，绝不污染后续面板与文字
            guiGraphics.flush();

            // [醒目新注释] 3. 动态计算 88% 分辨率尺寸
            int modalWidth = Math.max(280, (int) (this.width * 0.88));
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int modalX = (this.width - modalWidth) / 2;
            int modalY = (this.height - modalHeight) / 2;

            // 绘制主窗口暗色背景与边缘 1px 细线条
            guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xFA141414);
            guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + 1, 0xFF444444);
            guiGraphics.fill(modalX, modalY, modalX + 1, modalY + modalHeight, 0xFF444444);
            guiGraphics.fill(modalX + modalWidth - 1, modalY, modalX + modalWidth, modalY + modalHeight, 0xFF444444);
            guiGraphics.fill(modalX, modalY + modalHeight - 1, modalX + modalWidth, modalY + modalHeight, 0xFF444444);

            // 绘制顶部标题栏背景与文本
            guiGraphics.fill(modalX + 1, modalY + 1, modalX + modalWidth - 1, modalY + 22, 0xFF222222);
            guiGraphics.drawString(this.font, this.title, modalX + 10, modalY + 7, 0x00FF00, true);

            // [醒目新注释] 4. 动态剪裁区计算（底部预留 36px 避免文本穿透到按钮区域）
            int textTop = modalY + 28;
            int textBottom = modalY + modalHeight - 36;
            int lineHeight = 12;
            int visibleLines = (textBottom - textTop) / lineHeight;

            guiGraphics.enableScissor(modalX + 5, textTop, modalX + modalWidth - 5, textBottom);

            int startLine = (int) Math.floor(Math.max(0, this.scrollAmount));
            for (int i = 0; i < visibleLines + 1 && (startLine + i) < this.jsonLines.size(); i++) {
                String line = this.jsonLines.get(startLine + i);
                int lineY = textTop + (i * lineHeight);
                String lineNum = String.format("%3d | ", startLine + i + 1);

                guiGraphics.drawString(this.font, Component.literal("§6" + lineNum + "§f" + line), modalX + 10, lineY, 0xFFFFFF, true);
            }

            guiGraphics.disableScissor();

            // 复制成功提示文本渲染
            if (this.copySuccessNotice) {
                if (System.currentTimeMillis() - this.noticeTimestamp < 2000) {
                    guiGraphics.drawString(this.font, Component.literal("§a✔ 已成功复制到剪贴板！"), modalX + modalWidth - 160, modalY + 7, 0x00FF00, true);
                } else {
                    this.copySuccessNotice = false;
                }
            }

            // [醒目新注释] 5. 手动渲染按钮！跳过 super.render()，彻底解决二次 renderBackground 导致的混浊覆膜
            if (this.copyButton != null) {
                this.copyButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            if (this.closeButton != null) {
                this.closeButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int visibleLines = (modalHeight - 64) / 12;
            int maxScroll = Math.max(0, this.jsonLines.size() - visibleLines);

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