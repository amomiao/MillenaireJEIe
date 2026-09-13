package com.momos.millenairejeimm.jei.Runtime;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * 千年 JEI 自定义成分渲染器抽象基类 {@link IIngredientRenderer}。
 *
 * @param <T> 成分类型
 */
public abstract class BaseMillIngredientRenderer<T> implements IIngredientRenderer<T> {

    // [醒目新注释] 重写 JEI 抽象渲染签名 render，统一通过 MillIngredientIconResolver 绘制主体图标、右上角角标与点缀文字
    /**
     * 渲染 JEI 成分图标与其覆盖的点缀徽章。
     *
     * @param guiGraphics GUI 绘制上下文 {@link GuiGraphics}
     * @param ingredient 成分对象实例
     */
    @Override
    public void render(GuiGraphics guiGraphics, T ingredient) {
        if (ingredient == null) return;

        // 1. 通过顶级统筹类解析图标物品 Stack、右上角角标物品与点缀文字
        MillIngredientIconResolver.IngredientIconInfo iconInfo = MillIngredientIconResolver.resolve(ingredient);

        // [醒目新注释] 修正字段方法调用：由 iconStack() 替换为 mainIconStack()，对应主体图标
        // 2. 渲染背景/主体 Item 图标 (16x16 槽位)
        ItemStack mainStack = iconInfo.mainIconStack();
        if (!mainStack.isEmpty()) {
            guiGraphics.renderFakeItem(mainStack, 0, 0);
        }

        // [醒目新注释] 新增：渲染右上角微缩角标物品（文化:钻石, 村庄:金锭, 建筑:铁锭, 村民:绿宝石），通过 PoseStack 进行 0.5 倍 Matrix 缩放并提昇 Z 轴
        // 3. 渲染右上角微缩角标物品
        ItemStack overlayStack = iconInfo.overlayIconStack();
        if (!overlayStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            // 偏移到右上角 (9, -1)，Z 轴抬高 200F 避免被主体物品覆盖，缩放为 0.5 倍
            guiGraphics.pose().translate(9.0F, -1.0F, 200.0F);
            guiGraphics.pose().scale(0.5F, 0.5F, 1.0F);
            guiGraphics.renderFakeItem(overlayStack, 0, 0);
            guiGraphics.pose().popPose();
        }

        // 4. 渲染右下角/左上角点缀文字 (提升 Z 轴偏移，防止被物品模型遮挡)
        String badgeText = iconInfo.badgeText();
        if (badgeText != null && !badgeText.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.pose().pushPose();
            // 沿 Z 轴前移，确保文字渲染在物品与角标上方
            guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
            // 在右下角绘制带有阴影的点缀文字 (坐标 9, 9 适合 16x16 槽位中的单字/双字徽章)
            guiGraphics.drawString(font, badgeText, 9, 9, iconInfo.badgeColor(), true);
            guiGraphics.pose().popPose();
        }
    }

    /**
     * 保持向下兼容的旧版徽章获取接口。
     * @param ingredient 成分对象
     * @return 徽章文本
     */
    protected String getBadgeText(T ingredient) {
        return MillIngredientIconResolver.resolve(ingredient).badgeText();
    }
}