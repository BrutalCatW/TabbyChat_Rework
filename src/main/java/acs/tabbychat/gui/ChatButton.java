package acs.tabbychat.gui;

import acs.tabbychat.core.ChatChannel;
import acs.tabbychat.core.GuiNewChatTC;
import acs.tabbychat.core.TabbyChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;

public class ChatButton extends GuiButton {

    public ChatChannel channel;

    public ChatButton() {
        super(9999, 0, 0, 1, 1, "");
    }

    public ChatButton(int _id, int _x, int _y, int _w, int _h, String _title) {
        super(_id, _x, _y, _w, _h, _title);
    }

    private static Rectangle translateButtonDims(Rectangle unscaled) {
        float scaleSetting = GuiNewChatTC.getInstance().getScaleSetting();
        int adjX = Math.round((unscaled.x - ChatBox.current.x) * scaleSetting + ChatBox.current.x);

        int adjY = Math.round((TabbyChat.mc.currentScreen.height - unscaled.y + ChatBox.current.y)
                                      * (1.0f - scaleSetting))
                + unscaled.y;

        int adjW = Math.round(unscaled.width * scaleSetting);
        int adjH = Math.round(unscaled.height * scaleSetting);
        return new Rectangle(adjX, adjY, adjW, adjH);
    }

    /**
     * Returns button width
     */
    public int width() {
        return this.width;
    }

    /**
     * Sets button width
     */
    public void width(int _w) {
        this.width = _w;
    }

    /**
     * Returns button height
     */
    public int height() {
        return this.height;
    }

    /**
     * Sets button height
     */
    public void height(int _h) {
        this.height = _h;
    }

    /**
     * Returns X-position of button
     */
    public int x() {
        return xPosition;
    }

    /**
     * Sets X-position of button
     */
    public void x(int _x) {
        xPosition = _x;
    }

    /**
     * Returns Y-position of button
     */
    public int y() {
        return yPosition;
    }

    /**
     * Sets Y-position of button
     */
    public void y(int _y) {
        yPosition = _y;
    }

    public void clear() {
        this.channel = null;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int par2, int par3) {
        Rectangle cursor = translateButtonDims(new Rectangle(this.x(), this.y(), this.width(),
                                                             this.height()));

        // Check if click is within button bounds
        boolean withinBounds = this.enabled && this.visible && par2 >= cursor.x && par3 >= cursor.y
                && par2 < cursor.x + cursor.width && par3 < cursor.y + cursor.height;

        if (!withinBounds) {
            return false;
        }

        // Don't handle clicks in reserved area (for resize/pin buttons)
        // Reserved area is last 30px on the right of chatbox
        int reservedSpace = 30;
        int chatboxRightEdge = ChatBox.current.x + ChatBox.current.width;
        int reservedAreaStart = chatboxRightEdge - reservedSpace;

        // If click is in reserved area, ignore it for tab buttons
        if (par2 >= reservedAreaStart && par2 <= chatboxRightEdge) {
            return false;
        }

        // Don't handle clicks outside visible tab tray area (for scrolled tabs)
        // Tabs that are scrolled out of view should not be clickable
        int visibleAreaLeft = ChatBox.current.x;
        int visibleAreaRight = chatboxRightEdge - reservedSpace;

        // Check if the button (or click) is outside the visible area
        // Use click position to determine if click is in visible area
        if (par2 < visibleAreaLeft || par2 > visibleAreaRight) {
            return false;
        }

        return true;
    }

    @Override
    public void drawButton(Minecraft mc, int cursorX, int cursorY) {
        if (this.visible) {
            FontRenderer fr = mc.fontRenderer;
            float _mult = mc.gameSettings.chatOpacity * 0.9F + 0.1F;
            int _opacity = (int) (255 * _mult);
            int textOpacity = (TabbyChat.advancedSettings.textIgnoreOpacity.getValue() ? 255
                                                                                       : _opacity);

            Rectangle cursor = translateButtonDims(new Rectangle(this.x(), this.y(), this.width(),
                                                                 this.height()));

            boolean hovered = cursorX >= cursor.x && cursorY >= cursor.y
                    && cursorX < cursor.x + cursor.width && cursorY < cursor.y + cursor.height;

            // Цвета: стиль brutal-cat.ru
            final long fillColor;
            final long borderColor;
            final int textColor;
            if (!this.enabled) {
                fillColor   = 0x1A1C1C24L;
                borderColor = 0x3055B9EAL;
                textColor   = 0x7096AFC0;
            } else if (hovered) {
                fillColor   = 0x4A55B2FDL;
                borderColor = 0xCC55B2FDL;
                textColor   = 0xFFB3D4FC;
            } else if (this.channel.active) {
                fillColor   = 0x3055B2FDL;
                borderColor = 0x7855B2FDL;
                textColor   = 0xFF55B2FD;
            } else if (this.channel.unread) {
                fillColor   = 0x2AFFFFFFL;
                borderColor = 0x70FFFF55L;
                textColor   = 0xFFFFFF55;
            } else {
                fillColor   = 0x1A1C1C24L;
                borderColor = 0x3055B9EAL;
                textColor   = 0xFF96AFC0;
            }
            GL11.glEnable(GL11.GL_BLEND);
            acs.tabbychat.util.RenderUtils.drawRectRoundedBorder(
                this.x(), this.y(), this.width(), this.height(),
                fillColor, borderColor, 3
            );
            if (hovered && Keyboard.isKeyDown(42)) {
                String special = (this.channel.getTitle().equalsIgnoreCase("*") ? "\u2398"
                                                                                : "\u26A0");
                this.drawCenteredString(fr, special, this.x() + this.width() / 2,
                                        this.y() + (this.height() - 8) / 2, textColor);
            }
            else {
                // Remove emoji markers for button titles (keep it simple)
                String displayTitle = this.displayString.replace("¿", "");
                this.drawCenteredString(fr, displayTitle, this.x() + this.width() / 2,
                                        this.y() + (this.height() - 8) / 2, textColor);
            }
        }
    }
}
