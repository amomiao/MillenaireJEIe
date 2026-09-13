package com.momos.millenairejeimm.jei.event.linkageslot;

import mezz.jei.api.ingredients.IIngredientType;
import java.util.List;

/**
 * [醒目新注释] 槽位配置小元组 {@link SlotBinding}。
 * 将坐标 (X, Y)、JEI 成分类型 {@link IIngredientType} 与数据列表原子化封装。
 *
 * @param x 槽位 X 坐标
 * @param y 槽位 Y 坐标
 * @param ingredientType JEI 成分类型
 * @param ingredients 成分数据列表
 * @param <T> 成分数据类型
 */
public record SlotBinding<T>(int x, int y, IIngredientType<T> ingredientType, List<T> ingredients) {

    /**
     * [醒目新注释] 校验当前元组是否具备渲染条件
     *
     * @return 列表非空且包含数据时返回 true
     */
    public boolean isValid() {
        return ingredients != null && !ingredients.isEmpty();
    }
}