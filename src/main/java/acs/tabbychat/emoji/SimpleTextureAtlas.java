package acs.tabbychat.emoji;

import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Simplified texture atlas for emoji rendering
 */
public class SimpleTextureAtlas {
    private final int atlasWidth;
    private final int atlasHeight;
    private final int imageWidth;
    private final int imageHeight;

    private BufferedImage bufferedImage;
    private Graphics2D graphics;
    private int glTextureId = -1;

    private int currentU = 0;
    private int currentV = 0;

    private final HashMap<Emoji, AtlasRegion> emojiRegions = new HashMap<>();

    public SimpleTextureAtlas(int atlasWidth, int atlasHeight, int imageWidth, int imageHeight) {
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        this.bufferedImage = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        this.graphics = bufferedImage.createGraphics();
    }

    /**
     * Add an image to the atlas and return its region
     */
    public AtlasRegion addImage(Emoji emoji, BufferedImage image) {
        // Check if we need to go to next row
        if (currentU + imageWidth > atlasWidth) {
            currentU = 0;
            currentV += imageHeight;
        }

        // Check if we have space
        if (currentV + imageHeight > atlasHeight) {
            System.err.println("[TabbyChat/Emoji] Atlas is full! Cannot add more emoji");
            return null;
        }

        // Draw image to atlas at current position
        graphics.drawImage(image, currentU, currentV, imageWidth, imageHeight, null);

        // Create region
        AtlasRegion region = new AtlasRegion(
            this,
            currentU,
            currentV,
            imageWidth,
            imageHeight
        );

        // Store mapping
        emojiRegions.put(emoji, region);

        // Move to next position
        currentU += imageWidth;

        return region;
    }

    /**
     * Get atlas region for an emoji
     */
    public AtlasRegion getRegion(Emoji emoji) {
        return emojiRegions.get(emoji);
    }

    /**
     * Upload atlas to OpenGL
     */
    public void uploadToGL() {
        if (glTextureId != -1) {
            return; // Already uploaded
        }

        try {
            // Get RGB data from BufferedImage
            int[] pixels = bufferedImage.getRGB(0, 0, atlasWidth, atlasHeight, null, 0, atlasWidth);

            // Create OpenGL texture
            glTextureId = TextureUtil.glGenTextures();
            TextureUtil.allocateTexture(glTextureId, atlasWidth, atlasHeight);
            TextureUtil.uploadTexture(glTextureId, pixels, atlasWidth, atlasHeight);

            // Cleanup
            graphics.dispose();
            bufferedImage.flush();

            System.out.println("[TabbyChat/Emoji] Uploaded atlas to OpenGL: texture ID " + glTextureId);

        } catch (Exception e) {
            System.err.println("[TabbyChat/Emoji] Failed to upload atlas to OpenGL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Bind this atlas texture for rendering
     */
    public void bind() {
        if (glTextureId == -1) {
            uploadToGL();
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureId);
    }

    public int getGlTextureId() {
        return glTextureId;
    }

    public int getAtlasWidth() {
        return atlasWidth;
    }

    public int getAtlasHeight() {
        return atlasHeight;
    }

    /**
     * Represents a region within the texture atlas
     */
    public static class AtlasRegion {
        private final SimpleTextureAtlas atlas;
        private final int u, v, width, height;

        public AtlasRegion(SimpleTextureAtlas atlas, int u, int v, int width, int height) {
            this.atlas = atlas;
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }

        /**
         * Render this region at the specified screen coordinates
         */
        public void render(int x, int y, int renderWidth, int renderHeight) {
            atlas.bind();

            float u0 = (float) u / atlas.atlasWidth;
            float v0 = (float) v / atlas.atlasHeight;
            float u1 = (float) (u + width) / atlas.atlasWidth;
            float v1 = (float) (v + height) / atlas.atlasHeight;

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(x, y + renderHeight);
            GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x + renderWidth, y + renderHeight);
            GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(x + renderWidth, y);
            GL11.glEnd();

            GL11.glDisable(GL11.GL_BLEND);
        }

        public int getU() { return u; }
        public int getV() { return v; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
}
