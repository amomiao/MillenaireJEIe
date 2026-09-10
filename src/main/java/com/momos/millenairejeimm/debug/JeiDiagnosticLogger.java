package com.momos.millenairejeimm.debug;

import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JEI 全链路诊断调试监听器 {@link JeiDiagnosticLogger}。
 */
public class JeiDiagnosticLogger {
    private static final Logger LOGGER = LogManager.getLogger("MillenaireJEI-Diag");

    /**
     * 1. 监听：点击发送 Focus 时
     */
    public static void logFocusTrigger(String source, IFocus<?> focus) {
        if (focus == null) {
            log("[JEI诊断-点击] " + source + " 发送的 Focus 为 null！");
            return;
        }
        var typed = focus.getTypedValue();
        log(String.format("[JEI诊断-点击-%s] Role: %s | Type: %s | ValueClass: %s | ObjectHash: %d | Content: %s",
                source,
                focus.getRole(),
                typed.getType().getIngredientClass().getSimpleName(),
                typed.getIngredient().getClass().getName(),
                System.identityHashCode(typed.getIngredient()),
                typed.getIngredient()
        ));
    }

    /**
     * 2. 监听：Category 构建配方隐形成分绑定时
     */
    public static void logCategoryBinding(String categoryName, String role, Object ingredientType, Object ingredientValue) {
        log(String.format("[JEI诊断-配方构建-%s] Role: %s | Type: %s | ObjectHash: %d | Content: %s",
                categoryName,
                role,
                ingredientType,
                System.identityHashCode(ingredientValue),
                ingredientValue
        ));
    }

    /**
     * 3. 监听：IIngredientHelper 获取 UID 时
     */
    public static void logHelperGetUid(String helperName, Object ingredient, Object uid) {
        log(String.format("[JEI诊断-HelperUID-%s] IngredientHash: %d | CalculatedUID: %s",
                helperName,
                System.identityHashCode(ingredient),
                uid
        ));
    }

    /**
     * 4. 监听：IIngredientHelper 匹配比对时
     */
    public static void logHelperMatches(String helperName, Object a, Object b, boolean result) {
        log(String.format("[JEI诊断-HelperMatch-%s] AHash: %d | BHash: %d | MatchResult: %b",
                helperName,
                System.identityHashCode(a),
                System.identityHashCode(b),
                result
        ));
    }

    private static void log(String msg) {
        LOGGER.info(msg);
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("§b" + msg));
        }
    }
}