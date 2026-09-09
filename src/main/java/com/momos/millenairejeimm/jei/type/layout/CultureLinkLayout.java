package com.momos.millenairejeimm.jei.type.layout;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.millenaire.culture.VillageType;

import java.util.List;

// region 文化分类链接框布局
/**
 * 封装文化关联内容的布局容器 {@link CultureLinkLayout}。
 */
public class CultureLinkLayout {
    // [原注释保留] 关联村庄列表类型占位，后续补全 List<VillageType>
    public final JeiLinkBox<VillageType> villageLinks;

    public CultureLinkLayout(String title) {
        villageLinks = new JeiLinkBox<>(title);
    }

    public CultureLinkLayout bindJei(IJeiRuntime jeiRuntime, IIngredientType<VillageType> villageType) {
        this.villageLinks.useJeiMode(jeiRuntime, villageType);
        return this;
    }

    public CultureLinkLayout bindJei(IIngredientManager ingredientManager, IIngredientType<VillageType> villageType) {
        this.villageLinks.useJeiMode(ingredientManager, villageType);
        return this;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, List<VillageType> villageTypes, int x, int y) {
        this.villageLinks.setRecipe(builder, villageTypes, x, y);
    }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width) {
        villageLinks.render(guiGraphics, font, x, y, width);
    }
}