package acs.tabbychat.util;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * Rendering utilities for rounded rectangles and gradients
 */
public class RenderUtils {

    /**
     * Set GL color from integer RGBA
     */
    public static void glColor(long color) {
        float a = (float)(color >> 24 & 255L) / 255.0f;
        float r = (float)(color >> 16 & 255L) / 255.0f;
        float g = (float)(color >> 8 & 255L) / 255.0f;
        float b = (float)(color & 255L) / 255.0f;
        GL11.glColor4f(r, g, b, a);
    }

    /**
     * Draw filled rectangle
     */
    public static void drawRectF(float left, float top, float right, float bottom, long color) {
        float l = left;
        float r = right;
        float t = top;
        float b = bottom;

        if (left < right) {
            l = right;
            r = left;
        }

        if (top < bottom) {
            t = bottom;
            b = top;
        }

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        glColor(color);
        tessellator.startDrawingQuads();
        tessellator.addVertex(l, b, 0.0);
        tessellator.addVertex(r, b, 0.0);
        tessellator.addVertex(r, t, 0.0);
        tessellator.addVertex(l, t, 0.0);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Draw filled rectangle with gradient
     */
    public static void drawRectFGradient(float left, float top, float right, float bottom, long color1, long color2) {
        float l = left;
        float r = right;
        float t = top;
        float b = bottom;

        if (left < right) {
            l = right;
            r = left;
        }

        if (top < bottom) {
            t = bottom;
            b = top;
        }

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        tessellator.startDrawingQuads();

        Color firstColor = new Color((int)color1, true);
        Color secondColor = new Color((int)color2, true);
        float fR = (float)firstColor.getRed() / 255.0f;
        float fG = (float)firstColor.getGreen() / 255.0f;
        float fB = (float)firstColor.getBlue() / 255.0f;
        float fA = (float)firstColor.getAlpha() / 255.0f;
        float sR = (float)secondColor.getRed() / 255.0f;
        float sG = (float)secondColor.getGreen() / 255.0f;
        float sB = (float)secondColor.getBlue() / 255.0f;
        float sA = (float)secondColor.getAlpha() / 255.0f;

        tessellator.setColorRGBA_F(fR, fG, fB, fA);
        tessellator.addVertex(l, b, 0.0);
        tessellator.addVertex(r, b, 0.0);
        tessellator.setColorRGBA_F(sR, sG, sB, sA);
        tessellator.addVertex(r, t, 0.0);
        tessellator.addVertex(l, t, 0.0);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Draw filled sector (pie slice)
     */
    public static void drawSector(float cX, float cY, float radius, long color, float startA, float endA) {
        glColor(color);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator instance = Tessellator.instance;
        instance.startDrawing(GL11.GL_TRIANGLE_STRIP);

        for (double angle = startA; angle <= endA; angle += 6.0) {
            double radians = Math.toRadians(angle);
            instance.addVertex(cX, cY, 0.0);
            instance.addVertex(cX - Math.sin(radians) * radius, cY - Math.cos(radians) * radius, 0.0);
        }

        instance.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Draw rounded rectangle
     */
    public static void drawRectRounded(float x, float y, float w, float h, long color, float radius) {
        if (radius >= h) {
            drawSector(x + radius, y + radius, radius, color, 0, 180);
            drawRectF(x + radius, y, x + w - radius, y + h, color);
            drawSector(x + w - radius, y + radius, radius, color, 180, 360);
        } else {
            drawSector(x + radius, y + radius, radius, color, 0, 90);
            drawSector(x + radius, y + h - radius, radius, color, 90, 180);
            drawSector(x + w - radius, y + h - radius, radius, color, 180, 270);
            drawSector(x + w - radius, y + radius, radius, color, 270, 360);
            drawRectF(x + radius, y, x + w - radius, y + h, color);
            drawRectF(x, y + radius, x + radius, y + h - radius, color);
            drawRectF(x + w - radius, y + radius, x + w, y + h - radius, color);
        }
    }

    /**
     * Draw rounded rectangle with fill and 1px border.
     * border == 0 → no border drawn.
     */
    public static void drawRectRoundedBorder(float x, float y, float w, float h, long fill, long border, float radius) {
        drawRectRounded(x, y, w, h, fill, radius);
        if (border == 0) return;
        int r = (int) radius;
        // straight edges
        drawRectF(x + r,         y,             x + w - r,     y + 1,             border); // top
        drawRectF(x + r,         y + h - 1,     x + w - r,     y + h,             border); // bottom
        drawRectF(x,             y + r,         x + 1,         y + h - r,         border); // left
        drawRectF(x + w - 1,     y + r,         x + w,         y + h - r,         border); // right
        // corner pixels
        for (int i = 1; i < r; i++) {
            drawRectF(x + r - i,         y + i,         x + r - i + 1,         y + i + 1,         border); // TL
            drawRectF(x + w - r + i - 1, y + i,         x + w - r + i,         y + i + 1,         border); // TR
            drawRectF(x + r - i,         y + h - 1 - i, x + r - i + 1,         y + h - i,         border); // BL
            drawRectF(x + w - r + i - 1, y + h - 1 - i, x + w - r + i,         y + h - i,         border); // BR
        }
    }

    /**
     * Draw rounded rectangle with gradient
     */
    public static void drawRectRoundedGradient(float x, float y, float w, float h, long color1, long color2, float radius) {
        if (radius >= h) {
            drawSector(x + radius, y + radius, radius, color2, 0, 180);
            drawRectFGradient(x + radius, y, x + w - radius, y + h, color1, color2);
            drawSector(x + w - radius, y + radius, radius, color1, 180, 360);
        } else {
            drawSector(x + radius, y + radius, radius, color2, 0, 90);
            drawSector(x + radius, y + h - radius, radius, color2, 90, 180);
            drawSector(x + w - radius, y + h - radius, radius, color1, 180, 270);
            drawSector(x + w - radius, y + radius, radius, color1, 270, 360);
            drawRectFGradient(x + radius, y, x + w - radius, y + h, color1, color2);
            drawRectF(x, y + radius, x + radius, y + h - radius, color2);
            drawRectF(x + w - radius, y + radius, x + w, y + h - radius, color1);
        }
    }

    /**
     * Bind texture from ResourceLocation path
     */
    public static void bindTexture(String modid, String path) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager()
            .bindTexture(new net.minecraft.util.ResourceLocation(modid, path));
    }

    /**
     * Draw textured rectangle
     */
    public static void drawTexture(float x, float y, float u, float v, float uW, float vH, float w, float h, float tW, float tH) {
        float f = 1.0f / tW;
        float f1 = 1.0f / tH;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + h, 0.0, u * f, (v + vH) * f1);
        tessellator.addVertexWithUV(x + w, y + h, 0.0, (u + uW) * f, (v + vH) * f1);
        tessellator.addVertexWithUV(x + w, y, 0.0, (u + uW) * f, v * f1);
        tessellator.addVertexWithUV(x, y, 0.0, u * f, v * f1);
        tessellator.draw();
    }
}
