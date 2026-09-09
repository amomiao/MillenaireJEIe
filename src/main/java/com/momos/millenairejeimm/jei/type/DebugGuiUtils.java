package com.momos.millenairejeimm.jei.type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// region DebugGuiUtils 工具类
/**
 * JEI 开发者 Debug 模式通用 GUI 绘制工具类 {@link DebugGuiUtils}。
 * 提供暗色 Debug 终端面板、属性徽章（Badge）、动态悬浮 Inspector 及全量对象 JSON Dump 大窗口预览功能。
 */
public class DebugGuiUtils {
    public static final int BUTTON_WIDTH = 22;
    public static final int BUTTON_HEIGHT = 9;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .registerTypeAdapter(Optional.class, (JsonSerializer<Optional<?>>) (
                    src, typeOfSrc, context) -> src.isPresent() ? context.serialize(src.get()) : JsonNull.INSTANCE
            ).create();

    public static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void drawDetailsButton(GuiGraphics guiGraphics, Font font, int x, int y, double mouseX, double mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + BUTTON_WIDTH && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;

        int bgColor = hovered ? 0xFF4A68B0 : 0xFF333333;
        int textColor = hovered ? 0xFFFFFF00 : 0xFFE0E0E0;

        guiGraphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, bgColor);
        guiGraphics.fill(x, y, x + BUTTON_WIDTH, y + 1, 0xFF666666);
        guiGraphics.fill(x, y, x + 1, y + BUTTON_HEIGHT, 0xFF666666);

        guiGraphics.drawString(font, Component.literal("细节"), x + 1, y + 1, textColor, false);
    }

    // [醒目新注释] 重载 handleDetailsButtonClick，支持自动生成 Inspector 数据或接收自定义 debugLines
    /**
     * 处理"细节"按钮的鼠标点击响应，自动提取 Inspector 属性并打开 {@link JsonDumpScreen} 界面。
     */
    public static boolean handleDetailsButtonClick(Object targetObject, double mouseX, double mouseY, int btnX, int btnY) {
        return handleDetailsButtonClick(targetObject, List.of(), mouseX, mouseY, btnX, btnY);
    }

    // [醒目新注释] 支持传入自定义 Inspector 行的 handleDetailsButtonClick 重载
    public static boolean handleDetailsButtonClick(Object targetObject, List<Component> customDebugLines, double mouseX, double mouseY, int btnX, int btnY) {
        if (mouseX >= btnX && mouseX <= btnX + BUTTON_WIDTH && mouseY >= btnY && mouseY <= btnY + BUTTON_HEIGHT) {
            openJsonDumpScreen(targetObject, customDebugLines);
            return true;
        }
        return false;
    }

    public static boolean handleJsonButtonClick(Object recordObj, List<Component> debugLines, double mouseX, double mouseY, int btnX, int btnY) {
        if (isMouseOver(mouseX, mouseY, btnX, btnY, 26, 12)) {
            openJsonDumpScreen(recordObj, debugLines);
            return true;
        }
        return false;
    }

    public static void openJsonDumpScreen(Object targetObj) {
        openJsonDumpScreen(targetObj, List.of());
    }

    // [醒目新注释] 若未传入 customDebugLines，自动通过反射/RecordComponent 构建完整的 Inspector 属性行
    public static void openJsonDumpScreen(Object targetObj, List<Component> debugLines) {
        if (targetObj == null) return;
        String jsonText;
        try {
            jsonText = GSON.toJson(targetObj);
        } catch (Exception e) {
            jsonText = "// JSON 序列化失败: " + e.getMessage();
        }

        List<Component> finalDebugLines = (debugLines != null && !debugLines.isEmpty())
                ? debugLines
                : buildDefaultInspectorLines(targetObj);

        String titleName = targetObj.getClass().getSimpleName();
        Minecraft.getInstance().setScreen(new JsonDumpScreen(Component.literal("开发者深度诊断 - " + titleName), jsonText, finalDebugLines));
    }

    // [醒目新注释] 新增通用 Inspector 属性构建方法，自动解析 Java Record 与常规 Class 的所有字段
    /**
     * 利用反射与 {@link RecordComponent} 提取对象的全量属性信息，作为 Inspector 视图的展现行。
     *
     * @param target 目标对象
     * @return 格式化后的 {@link Component} 列表
     */
    public static List<Component> buildDefaultInspectorLines(Object target) {
        if (target == null) return List.of(Component.literal("§c[Null Object]"));

        List<Component> lines = new ArrayList<>();
        Class<?> clazz = target.getClass();

        lines.add(Component.literal("§e[类信息]: §f" + clazz.getName()));
        lines.add(Component.literal("§e[结构类型]: §f" + (clazz.isRecord() ? "Java Record" : "Standard Class")));
        lines.add(Component.literal("§8----------------------------------------"));

        // 1. 如果是 Java Record（如 Millénaire 的各类 Record 结构）
        if (clazz.isRecord()) {
            RecordComponent[] components = clazz.getRecordComponents();
            for (RecordComponent rc : components) {
                try {
                    Object value = rc.getAccessor().invoke(target);
                    lines.add(Component.literal("§b" + rc.getName() + "§7: §f" + String.valueOf(value)));
                } catch (Exception e) {
                    lines.add(Component.literal("§b" + rc.getName() + "§7: §c<读取失败>"));
                }
            }
        }
        // 2. 普通 Class 对象的 Field 反射提取
        else {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                try {
                    Object value = field.get(target);
                    lines.add(Component.literal("§b" + field.getName() + "§7: §f" + String.valueOf(value)));
                } catch (Exception e) {
                    lines.add(Component.literal("§b" + field.getName() + "§7: §c<读取失败>"));
                }
            }
        }
        return lines;
    }
// endregion 标题行快捷 Debug 按钮渲染与交互逻辑

    // region 全屏大窗口 JSON Dump 预览 Screen 控件
    public static class JsonDumpScreen extends Screen {

        private final String rawJson;
        private final List<Component> debugLines;

        private List<String> jsonLines = new ArrayList<>();
        private List<String> inspectorLines = new ArrayList<>();

        private int activeTab = 0;
        private double scrollAmount = 0;
        private boolean copySuccessNotice = false;
        private long noticeTimestamp = 0;

        private Button copyButton;
        private Button closeButton;
        private Button tabJsonButton;
        private Button tabInspectorButton;

        private boolean isDraggingScrollbar = false;

        public JsonDumpScreen(Object targetObject) {
            this(Component.literal("开发者深度诊断 - " + (targetObject != null ? targetObject.getClass().getSimpleName() : "Null")),
                    targetObject != null ? GSON.toJson(targetObject) : "{}",
                    buildDefaultInspectorLines(targetObject));
        }

        public JsonDumpScreen(Component title, String rawJson) {
            this(title, rawJson, List.of());
        }

        public JsonDumpScreen(Component title, String rawJson, List<Component> debugLines) {
            super(title);
            this.rawJson = rawJson;
            this.debugLines = debugLines != null ? debugLines : List.of();
        }

        @Override
        protected void init() {
            super.init();
            this.jsonLines = Arrays.asList(this.rawJson.split("\n"));

            this.inspectorLines = new ArrayList<>();
            for (Component line : this.debugLines) {
                this.inspectorLines.add(line.getString());
            }

            int modalWidth = Math.max(280, (int) (this.width * 0.88));
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int modalX = (this.width - modalWidth) / 2;
            int modalY = (this.height - modalHeight) / 2;

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

        private List<String> getCurrentLines() {
            return (this.activeTab == 0) ? this.jsonLines : this.inspectorLines;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.tabJsonButton != null) this.tabJsonButton.active = (this.activeTab != 0);
            if (this.tabInspectorButton != null) this.tabInspectorButton.active = (this.activeTab != 1);

            this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.flush();

            int modalWidth = Math.max(280, (int) (this.width * 0.88));
            int modalHeight = Math.max(180, (int) (this.height * 0.88));
            int modalX = (this.width - modalWidth) / 2;
            int modalY = (this.height - modalHeight) / 2;

            guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xFA141414);
            guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + 1, 0xFF444444);
            guiGraphics.fill(modalX, modalY, modalX + 1, modalY + modalHeight, 0xFF444444);
            guiGraphics.fill(modalX + modalWidth - 1, modalY, modalX + modalWidth, modalY + modalHeight, 0xFF444444);
            guiGraphics.fill(modalX, modalY + modalHeight - 1, modalX + modalWidth, modalY + modalHeight, 0xFF444444);

            guiGraphics.fill(modalX + 1, modalY + 1, modalX + modalWidth - 1, modalY + 22, 0xFF222222);
            guiGraphics.drawString(this.font, this.title, modalX + 10, modalY + 7, 0x00FF00, true);

            List<String> currentLines = getCurrentLines();
            int textTop = modalY + 28;
            int textBottom = modalY + modalHeight - 36;
            int lineHeight = 12;
            int visibleLines = (textBottom - textTop) / lineHeight;
            int maxScroll = Math.max(0, currentLines.size() - visibleLines);

            guiGraphics.enableScissor(modalX + 5, textTop, modalX + modalWidth - 16, textBottom);

            int startLine = (int) Math.floor(Math.max(0, this.scrollAmount));
            for (int i = 0; i < visibleLines + 1 && (startLine + i) < currentLines.size(); i++) {
                String line = currentLines.get(startLine + i);
                int lineY = textTop + (i * lineHeight);
                String lineNum = String.format("%3d | ", startLine + i + 1);

                guiGraphics.drawString(this.font, Component.literal("§6" + lineNum + "§f" + line), modalX + 10, lineY, 0xFFFFFF, true);
            }

            guiGraphics.disableScissor();

            if (maxScroll > 0) {
                int trackX = modalX + modalWidth - 10;
                int trackY = textTop;
                int trackWidth = 4;
                int trackHeight = textBottom - textTop;

                guiGraphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF222222);

                int thumbHeight = Math.max(16, (int) ((double) visibleLines / currentLines.size() * trackHeight));
                int thumbY = trackY + (int) ((this.scrollAmount / maxScroll) * (trackHeight - thumbHeight));

                boolean hovered = mouseX >= trackX - 2 && mouseX <= trackX + trackWidth + 2 && mouseY >= trackY && mouseY <= trackY + trackHeight;
                int thumbColor = (hovered || this.isDraggingScrollbar) ? 0xFFAAAAAA : 0xFF666666;

                guiGraphics.fill(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, thumbColor);
            }

            if (this.copySuccessNotice) {
                if (System.currentTimeMillis() - this.noticeTimestamp < 2000) {
                    guiGraphics.drawString(this.font, Component.literal("§a✔ 已成功复制当前视图！"), modalX + modalWidth - 170, modalY + 7, 0x00FF00, true);
                } else {
                    this.copySuccessNotice = false;
                }
            }

            for (GuiEventListener child : this.children()) {
                if (child instanceof Renderable renderable) {
                    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            }
        }

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

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.isDraggingScrollbar = false;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

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