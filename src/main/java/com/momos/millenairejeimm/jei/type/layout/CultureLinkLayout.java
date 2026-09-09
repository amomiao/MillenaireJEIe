package com.momos.millenairejeimm.jei.type.layout;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.millenaire.culture.VillageType;

import java.util.List;

// region 文化分类链接框布局
public class CultureLinkLayout {
    // [原注释保留] 关联村庄列表类型占位，后续补全 List<VillageType>[cite: 10]
    public final JeiLinkBox<VillageType> villageLinks;

    // [原注释保留] 保存渲染位置与宽度，便于 handleInput 无缝计算点击判定[cite: 10]
    private int lastX = 2;
    private int lastY = 106;
    private int lastWidth = 172;

    public CultureLinkLayout(String title, int maxRows) {
        villageLinks = new JeiLinkBox<>(title, maxRows);
    }

    /**
     * [醒目新注释]
     * 全功能绑定：推荐传入 {@link IJeiRuntime} 以激活原生 R/U 按键与左右键配方查找。
     */
    public CultureLinkLayout bindJei(IJeiRuntime jeiRuntime, IIngredientType<VillageType> villageType) {
        this.villageLinks.useJeiMode(jeiRuntime, villageType);
        return this;
    }

    /**
     * [原注释保留] 初始化 JEI 对象模式，仅绑定 JEI 的 {@link IIngredientManager}。[cite: 10]
     */
    public CultureLinkLayout bindJei(IIngredientManager ingredientManager, IIngredientType<VillageType> villageType) {
        this.villageLinks.useJeiMode(ingredientManager, villageType);
        return this;
    }

    public void set(List<VillageType> villageTypes) {
        villageLinks.setTargets(villageTypes);
    }

    public void render(GuiGraphics guiGraphics, Font font, int startY) {
        // [原注释保留] y = 45 处开始绘制顺位 1 框[cite: 10]
        villageLinks.render(guiGraphics, font, startY);
    }

    /**
     * [原注释保留] 完整渲染重载：记录绘制参数并委托给 {@link JeiLinkBox} 执行渲染。[cite: 10]
     */
    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, double mouseX, double mouseY) {
        this.lastX = x;
        this.lastY = y;
        this.lastWidth = width;
        villageLinks.render(guiGraphics, font, x, y, width, mouseX, mouseY);
    }

    /**
     * [醒目新注释]
     * 托管输入处理，对接 {@link mezz.jei.api.recipe.category.IRecipeCategory#handleInput} 方法。
     * 全量捕获并转发鼠标点击（左键0/右键1）与键盘按键（R键/U键）。
     */
    public boolean handleInput(double mouseX, double mouseY, InputConstants.Key input) {
        if (input == null) return false;

        // 1. 鼠标按键响应 (0: 左键/配方, 1: 右键/用途)
        if (input.getType() == InputConstants.Type.MOUSE) {
            int button = input.getValue();
            return villageLinks.mouseClicked(mouseX, mouseY, button, lastX, lastY, lastWidth);
        }
        // 2. 键盘按键响应 (R: 配方, U: 用途)
        else if (input.getType() == InputConstants.Type.KEYSYM) {
            int keyCode = input.getValue();
            return villageLinks.keyPressed(mouseX, mouseY, keyCode, 0, 0, lastX, lastY, lastWidth);
        }

        return false;
    }
}
// endregion 文化分类链接框布局