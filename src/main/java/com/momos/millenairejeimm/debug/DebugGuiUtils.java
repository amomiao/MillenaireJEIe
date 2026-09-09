package com.momos.millenairejeimm.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
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

}