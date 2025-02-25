/*
 *
 *     Copyright (C) 2023 Calum (mineblock11), enjarai
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package com.mineblock11.skinshuffle.client.gui.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TexturedIconButtonWidget extends ButtonWidget {
    protected final Identifier iconTexture;
    protected final int iconU;
    protected final int iconV;
    protected final int iconDisabledVOffset;
    protected final int iconTextureWidth;
    protected final int iconTextureHeight;
    private final int iconXOffset;
    private final int iconYOffset;
    private final int iconWidth;
    private final int iconHeight;

    public TexturedIconButtonWidget(int iconU, int iconV, int iconXOffset, int iconYOffset, int iconDisabledVOffset, int iconWidth, int iconHeight, int iconTextureWidth, int iconTextureHeight, Identifier iconTexture, ButtonWidget.PressAction onPress) {
        super(0, 0, 20, 20, Text.of(""), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.iconTextureWidth = iconTextureWidth;
        this.iconTextureHeight = iconTextureHeight;
        this.iconU = iconU;
        this.iconV = iconV;
        this.iconDisabledVOffset = iconDisabledVOffset;
        this.iconTexture = iconTexture;
        this.iconXOffset = iconXOffset;
        this.iconYOffset = iconYOffset;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context, mouseX, mouseY, delta);
        this.drawTexture(context, this.iconTexture, this.getIconX(), this.getIconY(), this.iconU, this.iconV + (active ? 0 : this.iconDisabledVOffset), 0, this.iconWidth, this.iconHeight, this.iconTextureWidth, this.iconTextureHeight);
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        int i = this.getX() + 2;
        int j = this.getX() + this.getWidth() - this.iconWidth - 6;
        drawScrollableText(context, textRenderer, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), color);
    }

    private int getIconX() {
        return this.getX() + (this.width / 2 - this.iconWidth / 2) + this.iconXOffset;
    }

    private int getIconY() {
        return this.getY() + this.iconYOffset;
    }
}
