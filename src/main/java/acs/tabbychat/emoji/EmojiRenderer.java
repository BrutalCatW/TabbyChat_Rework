package acs.tabbychat.emoji;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles rendering text with embedded emoji
 */
public class EmojiRenderer {
    public static final int EMOJI_RENDER_SIZE = 9; // Size to render emoji in chat (9x9 pixels)
    private static final int EMOJI_WIDTH = 10; // Width emoji takes in text (9px + 1px spacing)
    private static final int EMOJI_SPACING = 1; // Extra spacing between emoji and text

    private final EmojiLoader loader;
    private final FontRenderer fontRenderer;

    public EmojiRenderer(EmojiLoader loader) {
        this.loader = loader;
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
    }

    /**
     * Render a string with emoji support
     * @return The total width rendered
     */
    public int drawStringWithEmoji(String text, int x, int y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) {
            return x;
        }

        // If emoji not loaded yet, just render as normal text
        // PUA characters will render as empty boxes, which is acceptable during loading
        if (!loader.isLoaded()) {
            if (shadow) {
                return fontRenderer.drawStringWithShadow(text, x, y, color);
            } else {
                return fontRenderer.drawString(text, x, y, color);
            }
        }

        int currentX = x;
        StringBuilder textBuffer = new StringBuilder();
        boolean lastWasEmoji = false;

        // Iterate through characters, rendering text chunks and emoji
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Emoji emoji = EmojiRegistry.getEmojiByPUA(c);

            if (emoji != null) {
                // Found emoji - first render any buffered text
                if (textBuffer.length() > 0) {
                    String textPart = textBuffer.toString();
                    if (shadow) {
                        currentX = fontRenderer.drawStringWithShadow(textPart, currentX, y, color);
                    } else {
                        currentX = fontRenderer.drawString(textPart, currentX, y, color);
                    }
                    textBuffer.setLength(0); // Clear buffer
                    // Add small spacing between text and emoji
                    currentX += EMOJI_SPACING;
                } else if (lastWasEmoji) {
                    // Add spacing between consecutive emojis
                    currentX += EMOJI_SPACING;
                }

                // Render emoji
                renderEmoji(emoji, currentX, y, EMOJI_RENDER_SIZE);
                currentX += EMOJI_RENDER_SIZE;
                lastWasEmoji = true;
            } else {
                // Regular character
                if (lastWasEmoji) {
                    // Add spacing between emoji and text
                    currentX += EMOJI_SPACING;
                    lastWasEmoji = false;
                }
                textBuffer.append(c);
            }
        }

        // Render any remaining text
        if (textBuffer.length() > 0) {
            String textPart = textBuffer.toString();
            if (shadow) {
                currentX = fontRenderer.drawStringWithShadow(textPart, currentX, y, color);
            } else {
                currentX = fontRenderer.drawString(textPart, currentX, y, color);
            }
        }

        return currentX;
    }

    /**
     * Render a single emoji at specified position
     */
    private void renderEmoji(Emoji emoji, int x, int y, int size) {
        SimpleTextureAtlas atlas = loader.getAtlas();
        if (atlas == null) return;

        SimpleTextureAtlas.AtlasRegion region = atlas.getRegion(emoji);
        if (region != null) {
            region.render(x, y, size, size);
        }
    }

    /**
     * Static method to draw emoji (for GUI usage)
     */
    public static void drawEmoji(Emoji emoji, int x, int y, int size) {
        // Get the emoji loader from EmojiManager
        EmojiManager manager = EmojiManager.getInstance();
        if (!manager.isReady()) {
            return;
        }

        // Access loader through reflection or add getter
        // For now, create a simple path through atlas
        try {
            java.lang.reflect.Field loaderField = EmojiManager.class.getDeclaredField("loader");
            loaderField.setAccessible(true);
            EmojiLoader loader = (EmojiLoader) loaderField.get(manager);

            if (loader == null || !loader.isLoaded()) {
                return;
            }

            SimpleTextureAtlas atlas = loader.getAtlas();
            if (atlas == null) return;

            SimpleTextureAtlas.AtlasRegion region = atlas.getRegion(emoji);
            if (region != null) {
                region.render(x, y, size, size);
            }
        } catch (Exception e) {
            System.err.println("[TabbyChat/Emoji] Failed to draw emoji: " + e.getMessage());
        }
    }

    /**
     * Calculate width of string with emoji
     */
    public int getStringWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int width = 0;
        StringBuilder textBuffer = new StringBuilder();
        boolean lastWasEmoji = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Emoji emoji = EmojiRegistry.getEmojiByPUA(c);

            if (emoji != null) {
                // Emoji - first count buffered text, then add emoji width
                if (textBuffer.length() > 0) {
                    width += fontRenderer.getStringWidth(textBuffer.toString());
                    textBuffer.setLength(0);
                    // Add spacing between text and emoji
                    width += EMOJI_SPACING;
                } else if (lastWasEmoji) {
                    // Add spacing between consecutive emojis
                    width += EMOJI_SPACING;
                }
                width += EMOJI_RENDER_SIZE;
                lastWasEmoji = true;
            } else {
                // Regular character
                if (lastWasEmoji) {
                    // Add spacing between emoji and text
                    width += EMOJI_SPACING;
                    lastWasEmoji = false;
                }
                textBuffer.append(c);
            }
        }

        // Count any remaining text
        if (textBuffer.length() > 0) {
            width += fontRenderer.getStringWidth(textBuffer.toString());
        }

        return width;
    }

    /**
     * Split text into multiple lines based on max width, accounting for emoji
     */
    public List<String> splitByWidth(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        int currentWidth = 0;

        // Split by words (spaces)
        String[] words = text.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int wordWidth = getStringWidth(word);
            int spaceWidth = fontRenderer.getStringWidth(" ");

            // Check if adding this word would exceed max width
            if (currentWidth > 0 && currentWidth + spaceWidth + wordWidth > maxWidth) {
                // Start new line
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
                currentWidth = wordWidth;
            } else {
                // Add to current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                    currentWidth += spaceWidth;
                }
                currentLine.append(word);
                currentWidth += wordWidth;
            }
        }

        // Add last line
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Trim string to fit within maxWidth, accounting for emoji
     */
    public String trimToWidth(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        int width = getStringWidth(text);
        if (width <= maxWidth) {
            return text;
        }

        // Need to trim - process character by character with PUA emoji detection
        StringBuilder result = new StringBuilder();
        int currentWidth = 0;
        boolean lastWasEmoji = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Emoji emoji = EmojiRegistry.getEmojiByPUA(c);

            if (emoji != null) {
                // Calculate spacing
                int spacing = 0;
                if (result.length() > 0) {
                    spacing = EMOJI_SPACING;
                }

                // Emoji - check if it fits
                if (currentWidth + spacing + EMOJI_RENDER_SIZE > maxWidth) {
                    return result.toString();
                }
                currentWidth += spacing;
                result.append(c); // Keep PUA character
                currentWidth += EMOJI_RENDER_SIZE;
                lastWasEmoji = true;
            } else {
                // Regular character
                int spacing = lastWasEmoji ? EMOJI_SPACING : 0;
                int charWidth = fontRenderer.getCharWidth(c);

                if (currentWidth + spacing + charWidth > maxWidth) {
                    return result.toString();
                }
                currentWidth += spacing;
                result.append(c);
                currentWidth += charWidth;
                lastWasEmoji = false;
            }
        }

        return result.toString();
    }

    /**
     * Check if emoji system is ready
     */
    public boolean isReady() {
        return loader.isLoaded();
    }

    /**
     * Get loading progress
     */
    public float getProgress() {
        return loader.getProgress();
    }
}
