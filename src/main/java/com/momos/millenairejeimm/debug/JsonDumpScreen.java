// region JSON Debug Screen Implementation
package com.momos.millenairejeimm.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.momos.millenairejeimm.debug.DebugGuiUtils.buildDefaultInspectorLines;
import static org.millenaire.culture.JsonLoaderUtils.GSON;

/**
 * Debug GUI Screen for inspecting raw JSON and Object inspector properties {@link JsonDumpScreen}.
 */
public class JsonDumpScreen extends Screen {

    private final String rawJson;
    private final List<Component> debugLines;
    // [醒目新注释] 保存唤起 Debug 界面之前打开的父级屏幕 (如 JEI 视图 {@link Screen})
    private final Screen parentScreen;

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
        this(targetObject, null);
    }
    // [醒目新注释] 重载构造函数支持传入父级屏幕 {@link Screen}
    public JsonDumpScreen(Object targetObject, Screen parentScreen) {
        this(Component.literal("Debug - " + (targetObject != null ? targetObject.getClass().getSimpleName() : "Null")),
                targetObject != null ? GSON.toJson(targetObject) : "{}",
                buildDefaultInspectorLines(targetObject),
                parentScreen);
    }
    public JsonDumpScreen(Component title, String rawJson) {
        this(title, rawJson, List.of(), null);
    }
    public JsonDumpScreen(Component title, String rawJson, List<Component> debugLines) {
        this(title, rawJson, debugLines, null);
    }
    // [醒目新注释] 包含 parentScreen 的主构造函数，用于传递上级 GUI 状态
    /**
     * Constructs {@link JsonDumpScreen} with full configuration including parent screen reference {@link Screen}.
     *
     * @param title Component title for the screen
     * @param rawJson Serialized JSON content string
     * @param debugLines Custom debug components
     * @param parentScreen Parent active screen reference to preserve navigation
     */
    public JsonDumpScreen(Component title, String rawJson, List<Component> debugLines, Screen parentScreen) {
        super(title);
        this.rawJson = rawJson;
        this.debugLines = debugLines != null ? debugLines : List.of();
        this.parentScreen = parentScreen;
    }

    // [醒目新注释] 初始化界面控件并将原本硬编码的中文按钮文本替换为全英文文本
    /**
     * Initializes elements including buttons, tabs, and layout bounds for {@link JsonDumpScreen}.
     */
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
        this.tabJsonButton = Button.builder(Component.literal("JSON Structure"), btn -> {
            this.activeTab = 0;
            this.scrollAmount = 0;
        }).bounds(modalX + modalWidth - 195, tabY, 75, 16).build();

        this.tabInspectorButton = Button.builder(Component.literal("Inspector Props"), btn -> {
            this.activeTab = 1;
            this.scrollAmount = 0;
        }).bounds(modalX + modalWidth - 115, tabY, 85, 16).build();

        int btnWidth = 150;
        int btnHeight = 20;
        int btnY = modalY + modalHeight - 28;

        this.copyButton = Button.builder(Component.literal("📋 Copy View to Clipboard"), btn -> {
            String copyText = (this.activeTab == 0) ? this.rawJson : String.join("\n", this.inspectorLines);
            Minecraft.getInstance().keyboardHandler.setClipboard(copyText);
            this.copySuccessNotice = true;
            this.noticeTimestamp = System.currentTimeMillis();
        }).bounds(modalX + (modalWidth / 2) - btnWidth - 10, btnY, btnWidth, btnHeight).build();

        this.closeButton = Button.builder(Component.literal("Close (ESC)"), btn -> {
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

    // [醒目新注释] 渲染界面元素，包含将中文成功提示文本“已成功复制当前视图！”修改为英文“Copied view to clipboard!”
    /**
     * Renders background, text content, scrollbar, copy notification, and child widgets for {@link JsonDumpScreen}.
     *
     * @param guiGraphics The graphics object used for rendering {@link GuiGraphics}
     * @param mouseX Current mouse cursor X coordinate
     * @param mouseY Current mouse cursor Y coordinate
     * @param partialTick Partial tick value for rendering interpolation
     */
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
                Component noticeComponent = Component.literal("§a✔ Copied view to clipboard!");
                int textWidth = this.font.width(noticeComponent);
                // 动态计算右下角坐标，靠右边距 15px，底部边距 22px
                int noticeX = modalX + modalWidth - textWidth - 15;
                int noticeY = modalY + modalHeight - 22;
                guiGraphics.drawString(this.font, noticeComponent, noticeX, noticeY, 0x00FF00, true);
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

    // [醒目新注释] 重写 onClose 方法，关闭当前调试界面时将屏幕恢复为先前的 parentScreen (如 JEI 视图 {@link Screen})，避免 JEI 界面关闭丢失
    /**
     * Overrides {@link Screen#onClose()} to return back to the parent screen (such as JEI GUI) when closing.
     */
    @Override
    public void onClose() {
        if (this.minecraft != null && this.parentScreen != null) {
            this.minecraft.setScreen(this.parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
// endregion JSON Debug Screen Implementation