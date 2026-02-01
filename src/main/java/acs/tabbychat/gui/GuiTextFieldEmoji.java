package acs.tabbychat.gui;

import acs.tabbychat.emoji.EmojiManager;
import acs.tabbychat.emoji.EmojiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * Extended GuiTextField with emoji rendering support
 * Renders emoji as PNG icons instead of Unicode characters
 */
public class GuiTextFieldEmoji extends GuiTextField {

    public GuiTextFieldEmoji(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height);
    }

    /**
     * Override drawTextBox to use emoji rendering
     */
    @Override
    public void drawTextBox() {
        if (!this.getVisible()) {
            return;
        }

        // Draw background box (same as vanilla)
        if (this.getEnableBackgroundDrawing()) {
            Gui.drawRect(this.xPosition - 1, this.yPosition - 1,
                    this.xPosition + this.width + 1, this.yPosition + this.height + 1,
                    -6250336);
            Gui.drawRect(this.xPosition, this.yPosition,
                    this.xPosition + this.width, this.yPosition + this.height,
                    -16777216);
        }

        // Get text color (white)
        int textColor = 0xFFFFFF;

        // Get displayed text (with cursor consideration)
        String text = this.getText();

        // Convert Unicode emoji to PUA markers for rendering
        text = EmojiRegistry.convertUnicodeToMarkers(text);

        int cursorPos = this.getCursorPosition();
        int selectionEnd = this.getSelectionEnd();

        // Calculate scroll offset for long text
        String displayText = text;
        int drawX = this.xPosition + 2;  // Left padding
        int drawY = this.yPosition;  // No top padding - align to top of field
        int maxWidth = this.width - 4;  // Available width for text (minus padding)

        // Handle text scrolling when it's longer than field width
        if (displayText.length() > 0) {
            String visibleText = displayText;

            // Calculate scroll offset to keep cursor visible
            int fullTextWidth = EmojiManager.getInstance().getTextWidth(displayText);
            int scrollOffset = 0;

            if (fullTextWidth > maxWidth && this.isFocused()) {
                // Text is too long - need to scroll
                String beforeCursor = displayText.substring(0, cursorPos);
                int beforeCursorWidth = EmojiManager.getInstance().getTextWidth(beforeCursor);

                // Scroll to keep cursor visible
                if (beforeCursorWidth > maxWidth) {
                    scrollOffset = beforeCursorWidth - maxWidth;
                }
            }

            drawX -= scrollOffset;

            // Enable scissor test to clip text outside field bounds
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int scale = sr.getScaleFactor();

            // Calculate scissor box in screen pixels (from bottom-left)
            int scissorX = this.xPosition * scale;
            int scissorY = mc.displayHeight - (this.yPosition + this.height) * scale;
            int scissorWidth = this.width * scale;
            int scissorHeight = this.height * scale;

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

            // Render text with emoji support
            // Cursor blinks every 500ms (similar to vanilla)
            boolean showCursor = this.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0;
            if (showCursor) {
                // Draw text with cursor
                if (cursorPos < visibleText.length()) {
                    String before = visibleText.substring(0, cursorPos);
                    String after = visibleText.substring(cursorPos);

                    int beforeWidth = EmojiManager.getInstance().getTextWidth(before);
                    EmojiManager.getInstance().renderText(before, drawX, drawY, textColor, false);

                    // Draw cursor
                    Gui.drawRect(drawX + beforeWidth, drawY - 1, drawX + beforeWidth + 1, drawY + 9, -3092272);

                    // Draw text after cursor
                    EmojiManager.getInstance().renderText(after, drawX + beforeWidth + 1, drawY, textColor, false);
                } else {
                    // Cursor at end
                    int textWidth = EmojiManager.getInstance().getTextWidth(visibleText);
                    EmojiManager.getInstance().renderText(visibleText, drawX, drawY, textColor, false);
                    Gui.drawRect(drawX + textWidth, drawY - 1, drawX + textWidth + 1, drawY + 9, -3092272);
                }
            } else {
                // No cursor - just draw text
                EmojiManager.getInstance().renderText(visibleText, drawX, drawY, textColor, false);
            }

            // Draw selection highlight if any
            if (selectionEnd != cursorPos) {
                int selStart = Math.min(cursorPos, selectionEnd);
                int selEnd = Math.max(cursorPos, selectionEnd);

                String beforeSel = visibleText.substring(0, selStart);
                String selected = visibleText.substring(selStart, Math.min(selEnd, visibleText.length()));

                int beforeWidth = EmojiManager.getInstance().getTextWidth(beforeSel);
                int selectedWidth = EmojiManager.getInstance().getTextWidth(selected);

                // Draw selection box
                Gui.drawRect(drawX + beforeWidth, drawY - 1,
                        drawX + beforeWidth + selectedWidth, drawY + 9,
                        -16776961); // Blue selection color
            }

            // Disable scissor test
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            // Empty text - just draw cursor if focused
            boolean showCursor = this.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0;
            if (showCursor) {
                Gui.drawRect(drawX, drawY - 1, drawX + 1, drawY + 9, -3092272);
            }
        }
    }

    /**
     * Override setText to handle emoji conversion and filter newlines
     */
    @Override
    public void setText(String text) {
        // Filter out newline characters to prevent multi-line text
        if (text != null) {
            text = text.replace("\n", "").replace("\r", "");
        }
        // Text is stored as-is (with Unicode emoji)
        // Conversion to PUA happens during rendering
        super.setText(text);
    }

    /**
     * Override writeText to filter newlines
     */
    @Override
    public void writeText(String text) {
        // Filter out newline characters
        if (text != null) {
            text = text.replace("\n", "").replace("\r", "");
        }
        super.writeText(text);
    }
}
