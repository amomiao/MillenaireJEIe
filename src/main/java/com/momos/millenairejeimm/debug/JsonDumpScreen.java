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

public class JsonDumpScreen extends Screen {

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