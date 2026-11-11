package com.momos.millenairejeimm.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializer;
import com.momos.millenairejeim.util.MMLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 调试界面工具类：提供 JEI 配方内 Raw 调试按钮绘制、点击判定、JSON 导出弹窗与安全反射字段检查。
 */
public class DebugGuiUtils {

    public static final int BUTTON_WIDTH = 22;
    public static final int BUTTON_HEIGHT = 9;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .registerTypeAdapter(Optional.class, (JsonSerializer<Optional<?>>) (src, typeOfSrc, context) ->
                    src.isPresent() ? context.serialize(src.get()) : JsonNull.INSTANCE)
            .create();

    public static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void drawDetailsButton(GuiGraphics guiGraphics, Font font, int x, int y, double mouseX, double mouseY) {
        boolean hovered = isMouseOver(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        int bgColor = hovered ? 0xFF4A68B0 : 0xFF333333;
        int textColor = hovered ? 0xFFFFFF00 : 0xFFE0E0E0;

        guiGraphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, bgColor);
        guiGraphics.fill(x, y, x + BUTTON_WIDTH, y + 1, 0xFF666666);
        guiGraphics.fill(x, y, x + 1, y + BUTTON_HEIGHT, 0xFF666666);
        guiGraphics.drawString(font, Component.literal("Raw"), x + 1, y + 1, textColor, false);
    }

    public static boolean handleDetailsButtonClick(Object target, List<Component> customInspectorLines,
                                                   double mouseX, double mouseY, int btnX, int btnY) {
        if (isMouseOver(mouseX, mouseY, btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            openJsonDumpScreen(target, customInspectorLines);
            return true;
        }
        return false;
    }

    public static void openJsonDumpScreen(Object target) {
        openJsonDumpScreen(target, List.of());
    }

    public static void openJsonDumpScreen(Object target, List<Component> customInspectorLines) {
        openJsonDumpScreen(target, customInspectorLines, Minecraft.getInstance().screen);
    }

    public static void openJsonDumpScreen(Object target, List<Component> customInspectorLines, Screen returnToScreen) {
        if (target == null) return;
        String jsonDump;
        try {
            jsonDump = GSON.toJson(target);
        } catch (Throwable t) {
            MMLog.warn("[DebugGuiUtils] JSON 序列化调试对象 {} 失败: {}",
                    "[DebugGuiUtils] Failed to serialize debug object {} to JSON: {}",
                    "[DebugGuiUtils] Échec de sérialisation JSON de {} : {}",
                    target.getClass().getSimpleName(), t.getMessage());
            jsonDump = "// JSON Serialization Failed: " + t.getMessage();
        }
        List<Component> inspectorLines = (customInspectorLines != null && !customInspectorLines.isEmpty())
                ? customInspectorLines
                : buildDefaultInspectorLines(target);
        String className = target.getClass().getSimpleName();
        String finalJsonDump = jsonDump;
        Minecraft.getInstance().execute(() -> {
            Screen parent = returnToScreen != null ? returnToScreen : Minecraft.getInstance().screen;
            Minecraft.getInstance().setScreen(new JsonDumpScreen(
                    Component.literal("Diag - " + className),
                    finalJsonDump,
                    inspectorLines,
                    parent));
        });
    }

    public static List<Component> buildDefaultInspectorLines(Object target) {
        if (target == null) {
            return List.of(Component.literal("§c[Null Object]"));
        }
        List<Component> lines = new ArrayList<>();
        Class<?> clazz = target.getClass();
        if (clazz.isRecord()) {
            try {
                RecordComponent[] components = clazz.getRecordComponents();
                for (RecordComponent rc : components) {
                    try {
                        Object value = rc.getAccessor().invoke(target);
                        lines.add(Component.literal("§b" + rc.getName() + "§7: §f" + String.valueOf(value)));
                    } catch (Throwable t) {
                        MMLog.warn("[DebugGuiUtils] 反射读取调试字段 ({}): {}",
                                "[DebugGuiUtils] Failed reflective debug field read ({}): {}",
                                "[DebugGuiUtils] Échec de lecture réflexive ({}) : {}",
                                clazz.getSimpleName(), t.getMessage());
                        lines.add(Component.literal("§b" + rc.getName() + "§7: §c<Failed to read>"));
                    }
                }
            } catch (Throwable t) {
                MMLog.warn("[DebugGuiUtils] 获取 {} Record 组件列表失败: {}",
                        "[DebugGuiUtils] Failed listing record components for {}: {}",
                        "[DebugGuiUtils] Échec du listage des composants de {} : {}",
                        clazz.getSimpleName(), t.getMessage());
            }
        } else {
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    try {
                        field.setAccessible(true);
                        Object value = field.get(target);
                        lines.add(Component.literal("§b" + field.getName() + "§7: §f" + String.valueOf(value)));
                    } catch (Throwable t) {
                        MMLog.warn("[DebugGuiUtils] 反射读取调试字段 ({}): {}",
                                "[DebugGuiUtils] Failed reflective debug field read ({}): {}",
                                "[DebugGuiUtils] Échec de lecture réflexive ({}) : {}",
                                clazz.getSimpleName(), t.getMessage());
                        lines.add(Component.literal("§b" + field.getName() + "§7: §c<Failed to read>"));
                    }
                }
            } catch (Throwable t) {
                MMLog.warn("[DebugGuiUtils] 获取 {} 字段列表失败: {}",
                        "[DebugGuiUtils] Failed listing fields for {}: {}",
                        "[DebugGuiUtils] Échec du listage des champs de {} : {}",
                        clazz.getSimpleName(), t.getMessage());
            }
        }
        return lines;
    }
}
