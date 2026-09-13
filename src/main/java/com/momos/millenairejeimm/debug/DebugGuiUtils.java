package com.momos.millenairejeimm.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializer;
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
 * JEI Developer Debug Mode GUI Utility Class {@link DebugGuiUtils}.
 * Provides dark terminal panels, property badges, inspector tooltips, and full object JSON dump windows.
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

    // [醒目新注释] 将“细节”按钮硬编码文本替换为英文“Details”
    /**
     * Renders the small "Details" debug button on the screen.
     *
     * @param guiGraphics Graphics context {@link GuiGraphics}
     * @param font Font renderer {@link Font}
     * @param x Button X coordinate
     * @param y Button Y coordinate
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     */
    public static void drawDetailsButton(GuiGraphics guiGraphics, Font font, int x, int y, double mouseX, double mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + BUTTON_WIDTH && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;

        int bgColor = hovered ? 0xFF4A68B0 : 0xFF333333;
        int textColor = hovered ? 0xFFFFFF00 : 0xFFE0E0E0;

        guiGraphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, bgColor);
        guiGraphics.fill(x, y, x + BUTTON_WIDTH, y + 1, 0xFF666666);
        guiGraphics.fill(x, y, x + 1, y + BUTTON_HEIGHT, 0xFF666666);

        guiGraphics.drawString(font, Component.literal("Raw"), x + 1, y + 1, textColor, false);
    }

    public static boolean handleDetailsButtonClick(Object targetObject, List<Component> customDebugLines, double mouseX, double mouseY, int btnX, int btnY) {
        if (isMouseOver(mouseX, mouseY, btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            openJsonDumpScreen(targetObject, customDebugLines);
            return true;
        }
        return false;
    }

    public static void openJsonDumpScreen(Object targetObj) {
        openJsonDumpScreen(targetObj, List.of());
    }

    // [醒目新注释] 替换序列化失败信息与 Screen 标题文本为全英文
    /**
     * Opens the {@link JsonDumpScreen} screen safely on the client thread.
     *
     * @param targetObj Target object to serialize and inspect
     * @param debugLines Custom debug lines to pass to inspector tab
     */
    public static void openJsonDumpScreen(Object targetObj, List<Component> debugLines) {
        openJsonDumpScreen(targetObj, debugLines, Minecraft.getInstance().screen);
    }

    // [醒目新注释] 新增重载方法以在打开 JsonDumpScreen 时显式捕获并保存当前的父级屏幕 {@link Screen}
    /**
     * Opens the {@link JsonDumpScreen} screen safely on the client thread while preserving the parent screen reference.
     *
     * @param targetObj Target object to serialize and inspect
     * @param debugLines Custom debug lines to pass to inspector tab
     * @param parentScreen Active parent screen {@link Screen} to restore upon closing
     */
    public static void openJsonDumpScreen(Object targetObj, List<Component> debugLines, Screen parentScreen) {
        if (targetObj == null) return;
        String jsonText;
        try {
            jsonText = GSON.toJson(targetObj);
        } catch (Throwable e) {
            jsonText = "// JSON Serialization Failed: " + e.getMessage();
        }

        List<Component> finalDebugLines = (debugLines != null && !debugLines.isEmpty())
                ? debugLines
                : buildDefaultInspectorLines(targetObj);

        String titleName = targetObj.getClass().getSimpleName();
        String finalJsonText = jsonText;
        Minecraft.getInstance().execute(() -> {
            Screen activeParent = (parentScreen != null) ? parentScreen : Minecraft.getInstance().screen;
            Minecraft.getInstance().setScreen(new JsonDumpScreen(Component.literal("Diag - " + titleName), finalJsonText, finalDebugLines, activeParent));
        });
    }

    // [醒目新注释] 移除指定的 3 行中文 Class 结构头信息，并将异常捕获提示修改为英文 <Failed to read>
    /**
     * Extracts all fields and record components from a target object for the Inspector view.
     *
     * @param target Target object to analyze via reflection
     * @return List of formatted text components {@link Component}
     */
    public static List<Component> buildDefaultInspectorLines(Object target) {
        if (target == null) return List.of(Component.literal("§c[Null Object]"));

        List<Component> lines = new ArrayList<>();
        Class<?> clazz = target.getClass();

        // 1. Java Record structures
        if (clazz.isRecord()) {
            RecordComponent[] components = clazz.getRecordComponents();
            for (RecordComponent rc : components) {
                try {
                    Object value = rc.getAccessor().invoke(target);
                    lines.add(Component.literal("§b" + rc.getName() + "§7: §f" + String.valueOf(value)));
                } catch (Exception e) {
                    lines.add(Component.literal("§b" + rc.getName() + "§7: §c<Failed to read>"));
                }
            }
        }
        // 2. Standard Class field extraction
        else {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                try {
                    Object value = field.get(target);
                    lines.add(Component.literal("§b" + field.getName() + "§7: §f" + String.valueOf(value)));
                } catch (Exception e) {
                    lines.add(Component.literal("§b" + field.getName() + "§7: §c<Failed to read>"));
                }
            }
        }
        return lines;
    }
}
// endregion DebugGuiUtils Utility Implementation