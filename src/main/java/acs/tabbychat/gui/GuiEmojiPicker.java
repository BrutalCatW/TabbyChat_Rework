package acs.tabbychat.gui;

import acs.tabbychat.emoji.Emoji;
import acs.tabbychat.emoji.EmojiCategory;
import acs.tabbychat.emoji.EmojiCategory.Category;
import acs.tabbychat.emoji.EmojiRegistry;
import acs.tabbychat.emoji.EmojiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

/**
 * Emoji picker GUI with draggable window and category tabs
 */
public class GuiEmojiPicker {
    private static final int EMOJI_SIZE = 16;
    private static final int EMOJI_PADDING = 2;
    private static final int EMOJIS_PER_ROW = 10;
    private static final int EMOJI_ROWS = 8;
    private static final int TAB_HEIGHT = 20;
    private static final int HEADER_HEIGHT = 16;  // Header bar for dragging
    private static final int BORDER_SIZE = 2;
    private static final int SCROLLBAR_WIDTH = 6;

    // Size constraints
    private static final int MIN_WIDTH = 150;
    private static final int MIN_HEIGHT = 150;
    private static final int MAX_WIDTH = 400;
    private static final int MAX_HEIGHT = 400;

    // ── Стиль brutal-cat.ru ───────────────────────────────────────────────────
    private static final int  C_BG          = 0xEE1C1C24;
    private static final int  C_HEADER_BG   = 0x501C1C24;
    private static final int  C_BORDER      = 0x5555B9EA;
    private static final int  C_DIVIDER     = 0x3A55B9EA;
    private static final int  C_TITLE       = 0xFFB3D4FC;
    private static final int  C_TAB_ACTIVE  = 0x3055B2FD;
    private static final int  C_TAB_HOVER   = 0x2055B9EA;
    private static final int  C_TAB_NORM    = 0x101C1C24;
    private static final int  C_TAB_IND     = 0xFF55B2FD;  // active tab indicator line
    private static final int  C_EMOJI_HOVER = 0x2055B2FD;
    private static final long C_BTN_BG      = 0x2055B9EAL;
    private static final long C_BTN_HV      = 0x3855B9EAL;
    private static final long C_BTN_BD      = 0x5055B9EAL;
    private static final long C_BTN_HB      = 0xAA55B9EAL;
    private static final int  C_SB_TRACK    = 0x201C1C24;
    private static final int  C_SB_THUMB    = 0x5055B9EA;
    private static final int  C_SB_THUMB_HV = 0x8055B9EA;

    // Position and size (static to persist between chat opens)
    private static Rectangle savedBounds = null;  // Saved position/size
    private Rectangle bounds;
    private int width;
    private int height;
    private int emojisPerRow = EMOJIS_PER_ROW;
    private int emojiRows = EMOJI_ROWS;

    // State
    private boolean visible = false;
    private boolean dragging = false;
    private boolean resizing = false;
    private boolean scrollbarDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private int resizeStartWidth = 0;
    private int resizeStartHeight = 0;
    private Category currentCategory = Category.SMILEYS;
    private int scrollOffset = 0;
    private int scrollbarDragOffset = 0;

    // Reference to input field for emoji insertion
    private GuiTextField targetTextField;

    // Emoji data
    private List<Emoji> currentEmojis;

    // Minecraft instance
    private Minecraft mc;

    public GuiEmojiPicker() {
        this.mc = Minecraft.getMinecraft();

        // Restore saved bounds if exists, otherwise calculate initial position
        if (savedBounds != null) {
            this.bounds = new Rectangle(savedBounds);
            this.width = savedBounds.width;
            this.height = savedBounds.height;

            // Recalculate emoji grid based on restored size
            int availableWidth = width - BORDER_SIZE * 2 - 10 - SCROLLBAR_WIDTH - 2;
            emojisPerRow = Math.max(5, availableWidth / (EMOJI_SIZE + EMOJI_PADDING));

            int availableHeight = height - HEADER_HEIGHT - TAB_HEIGHT - BORDER_SIZE * 2 - 10;
            emojiRows = Math.max(3, availableHeight / (EMOJI_SIZE + EMOJI_PADDING));
        } else {
            // Calculate initial dimensions
            updateDimensions();

            // Position to the right of ChatBox with small gap
            ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int chatX = ChatBox.current.x;
            int chatY = ChatBox.current.y;
            int chatWidth = ChatBox.getChatWidth();
            int chatScreenY = sr.getScaledHeight() + chatY;

            // Position 10 pixels to the right of chat
            int pickerX = chatX + chatWidth + 10;
            int pickerY = chatScreenY - height;

            this.bounds = new Rectangle(pickerX, pickerY, width, height);
        }

        // Load initial category
        loadCategory(currentCategory);
    }

    /**
     * Update dimensions based on current settings
     */
    private void updateDimensions() {
        this.width = emojisPerRow * (EMOJI_SIZE + EMOJI_PADDING) + BORDER_SIZE * 2 + 10 + SCROLLBAR_WIDTH + 2;
        this.height = HEADER_HEIGHT + TAB_HEIGHT + emojiRows * (EMOJI_SIZE + EMOJI_PADDING) + BORDER_SIZE * 2 + 10;
    }

    /**
     * Toggle visibility
     */
    public void toggleVisibility() {
        visible = !visible;
        if (visible) {
            scrollOffset = 0;
        } else {
            // Save position/size when closing
            savedBounds = new Rectangle(bounds);
        }
    }

    /**
     * Set visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            scrollOffset = 0;
        } else {
            // Save position/size when closing
            savedBounds = new Rectangle(bounds);
        }
    }

    /**
     * Reposition picker to current screen size
     */
    private void repositionToScreen() {
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        // Keep centered if possible, otherwise move to visible area
        if (bounds.x + width > screenWidth || bounds.y + height > screenHeight ||
            bounds.x < 0 || bounds.y < 0) {
            // Reset to default position on right side
            bounds.x = screenWidth - width - 20;
            bounds.y = 50;
        }
    }

    /**
     * Check if visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set target text field for emoji insertion
     */
    public void setTargetTextField(GuiTextField field) {
        this.targetTextField = field;
    }

    /**
     * Load emoji for a category
     */
    private void loadCategory(Category category) {
        this.currentCategory = category;
        this.currentEmojis = EmojiCategory.getEmojiForCategory(category);
        this.scrollOffset = 0;
    }

    /**
     * Draw the emoji picker
     */
    public void draw(int mouseX, int mouseY) {
        if (!visible) {
            return;
        }

        int x = bounds.x;
        int y = bounds.y;

        // Draw background
        Gui.drawRect(x, y, x + width, y + height, C_BG);

        // Draw border
        drawBorder(x, y, width, height, C_BORDER);

        // Draw header bar (for dragging)
        drawHeader(x, y, mouseX, mouseY);

        // Draw tabs
        drawTabs(x, y + HEADER_HEIGHT, mouseX, mouseY);

        // Draw emoji grid
        drawEmojiGrid(x, y + HEADER_HEIGHT + TAB_HEIGHT, mouseX, mouseY);

        // Draw scrollbar if needed
        if (needsScrollbar()) {
            drawScrollbar(x + width - SCROLLBAR_WIDTH - BORDER_SIZE - 2,
                         y + HEADER_HEIGHT + TAB_HEIGHT + BORDER_SIZE,
                         height - HEADER_HEIGHT - TAB_HEIGHT - BORDER_SIZE * 2 - 2,
                         mouseX, mouseY);
        }

        // Draw resize handle in top-right corner (like ChatBox)
        drawResizeHandle(x + width - 12, y + 2, mouseX, mouseY);
    }

    /**
     * Draw header bar for dragging
     */
    private void drawHeader(int x, int y, int mouseX, int mouseY) {
        // Header background + bottom divider
        Gui.drawRect(x, y, x + width, y + HEADER_HEIGHT, C_HEADER_BG);
        Gui.drawRect(x, y + HEADER_HEIGHT - 1, x + width, y + HEADER_HEIGHT, C_DIVIDER);

        // Title
        FontRenderer fr = mc.fontRenderer;
        String title = "Эмоджи";
        int titleX = x + (width - fr.getStringWidth(title)) / 2;
        int titleY = y + (HEADER_HEIGHT - 8) / 2;
        fr.drawStringWithShadow(title, titleX, titleY, C_TITLE);
    }

    /**
     * Draw resize handle with icon
     */
    private void drawResizeHandle(int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + 12 &&
                         mouseY >= y && mouseY < y + 12;

        // Draw rounded button background
        long bgColor = (hovered || resizing) ? C_BTN_HV : C_BTN_BG;
        long bdColor = (hovered || resizing) ? C_BTN_HB : C_BTN_BD;
        acs.tabbychat.util.RenderUtils.drawRectRoundedBorder(x, y, 12, 12, bgColor, bdColor, 2);

        // Draw resize icon
        acs.tabbychat.util.RenderUtils.bindTexture("tabbychat", "textures/gui/resize_ico.png");
        org.lwjgl.opengl.GL11.glColor4f(1.0f, 1.0f, 1.0f, (hovered || resizing) ? 1.0f : 0.7f);
        acs.tabbychat.util.RenderUtils.drawTexture(x + 1, y + 1, 0, 0, 16, 16, 10, 10, 16, 16);
        org.lwjgl.opengl.GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Draw border around window
     */
    private void drawBorder(int x, int y, int w, int h, int color) {
        Gui.drawRect(x, y, x + w, y + 1, color); // Top
        Gui.drawRect(x, y + h - 1, x + w, y + h, color); // Bottom
        Gui.drawRect(x, y, x + 1, y + h, color); // Left
        Gui.drawRect(x + w - 1, y, x + w, y + h, color); // Right
    }

    /**
     * Draw category tabs
     */
    private void drawTabs(int x, int y, int mouseX, int mouseY) {
        Category[] categories = Category.values();
        int tabWidth = (width - BORDER_SIZE * 2) / categories.length;
        int tabH = TAB_HEIGHT - BORDER_SIZE;

        for (int i = 0; i < categories.length; i++) {
            Category cat = categories[i];
            int tabX = x + BORDER_SIZE + i * tabWidth;
            int tabY = y + BORDER_SIZE;

            boolean active  = cat == currentCategory;
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabWidth &&
                              mouseY >= tabY && mouseY < tabY + tabH;

            int bgColor = active ? C_TAB_ACTIVE : (hovered ? C_TAB_HOVER : C_TAB_NORM);
            Gui.drawRect(tabX, tabY, tabX + tabWidth, tabY + tabH, bgColor);

            // Active indicator line at bottom of tab
            if (active) {
                Gui.drawRect(tabX + 1, tabY + tabH - 1, tabX + tabWidth - 1, tabY + tabH, C_TAB_IND);
            }

            // Icon
            Emoji icon = EmojiCategory.getCategoryIcon(cat);
            if (icon != null) {
                int iconX = tabX + (tabWidth - 12) / 2;
                int iconY = tabY + (tabH - 12) / 2;
                EmojiRenderer.drawEmoji(icon, iconX, iconY, 12);
            } else {
                FontRenderer fr = mc.fontRenderer;
                String name = cat.name().substring(0, 1);
                int textX = tabX + (tabWidth - fr.getStringWidth(name)) / 2;
                int textY = tabY + (tabH - 8) / 2;
                int textColor = active ? C_TAB_IND : 0xFF96AFC0;
                fr.drawString(name, textX, textY, textColor);
            }
        }
        // Bottom divider under tabs
        Gui.drawRect(x, y + BORDER_SIZE + tabH, x + width, y + BORDER_SIZE + tabH + 1, C_DIVIDER);
    }

    /**
     * Draw emoji grid
     */
    private void drawEmojiGrid(int x, int y, int mouseX, int mouseY) {
        if (currentEmojis == null || currentEmojis.isEmpty()) {
            // Draw "No emoji" message
            FontRenderer fr = mc.fontRenderer;
            String msg = "No emoji in this category";
            int msgX = x + (width - fr.getStringWidth(msg)) / 2;
            int msgY = y + height / 2;
            fr.drawString(msg, msgX, msgY, 0xAAAAAA);
            return;
        }

        int startX = x + BORDER_SIZE + 5;
        int startY = y + BORDER_SIZE + 5;

        int col = 0;
        int row = 0;

        int startIndex = scrollOffset * emojisPerRow;
        int endIndex = Math.min(startIndex + emojiRows * emojisPerRow, currentEmojis.size());

        for (int i = startIndex; i < endIndex; i++) {
            Emoji emoji = currentEmojis.get(i);

            int emojiX = startX + col * (EMOJI_SIZE + EMOJI_PADDING);
            int emojiY = startY + row * (EMOJI_SIZE + EMOJI_PADDING);

            // Check if hovered
            boolean hovered = mouseX >= emojiX && mouseX < emojiX + EMOJI_SIZE &&
                            mouseY >= emojiY && mouseY < emojiY + EMOJI_SIZE;

            // Draw hover background
            if (hovered) {
                Gui.drawRect(emojiX - 1, emojiY - 1, emojiX + EMOJI_SIZE + 1,
                           emojiY + EMOJI_SIZE + 1, C_EMOJI_HOVER);
            }

            // Draw emoji
            EmojiRenderer.drawEmoji(emoji, emojiX, emojiY, EMOJI_SIZE);

            col++;
            if (col >= emojisPerRow) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Check if scrollbar is needed
     */
    private boolean needsScrollbar() {
        if (currentEmojis == null) return false;
        int totalRows = (int) Math.ceil((double) currentEmojis.size() / emojisPerRow);
        return totalRows > emojiRows;
    }

    /**
     * Draw scrollbar
     */
    private void drawScrollbar(int x, int y, int h, int mouseX, int mouseY) {
        // Draw scrollbar track
        Gui.drawRect(x, y, x + SCROLLBAR_WIDTH, y + h, C_SB_TRACK);

        // Calculate scrollbar thumb
        if (currentEmojis == null || currentEmojis.isEmpty()) return;

        int totalRows = (int) Math.ceil((double) currentEmojis.size() / emojisPerRow);
        int maxScroll = Math.max(0, totalRows - emojiRows);

        if (maxScroll > 0) {
            int thumbHeight = Math.max(20, h * emojiRows / totalRows);
            int thumbY = y + (h - thumbHeight) * scrollOffset / maxScroll;

            // Check if hovered
            boolean hovered = mouseX >= x && mouseX < x + SCROLLBAR_WIDTH &&
                            mouseY >= thumbY && mouseY < thumbY + thumbHeight;

            int thumbColor = hovered || scrollbarDragging ? C_SB_THUMB_HV : C_SB_THUMB;

            // Draw scrollbar thumb
            Gui.drawRect(x + 1, thumbY, x + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight, thumbColor);
        }
    }

    /**
     * Check if mouse is over header
     */
    private boolean isMouseOverHeader(int mouseX, int mouseY) {
        return mouseX >= bounds.x && mouseX < bounds.x + width &&
               mouseY >= bounds.y && mouseY < bounds.y + HEADER_HEIGHT;
    }

    /**
     * Check if mouse is over resize handle (top-right corner)
     */
    private boolean isMouseOverResizeHandle(int mouseX, int mouseY) {
        int handleX = bounds.x + width - 12;
        int handleY = bounds.y + 2;
        return mouseX >= handleX && mouseX < handleX + 12 &&
               mouseY >= handleY && mouseY < handleY + 12;
    }

    /**
     * Check if mouse is over window
     */
    private boolean isMouseOverWindow(int mouseX, int mouseY) {
        return mouseX >= bounds.x && mouseX < bounds.x + width &&
               mouseY >= bounds.y && mouseY < bounds.y + height;
    }

    /**
     * Public method to check if mouse is over emoji picker (for external checks)
     */
    public boolean isMouseOver(int mouseX, int mouseY) {
        if (!visible) {
            return false;
        }
        return isMouseOverWindow(mouseX, mouseY);
    }

    /**
     * Handle mouse click - coordinates are already in correct scale
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!visible) {
            return false;
        }

        System.out.println("[EmojiPicker] mouseClicked: coords=" + mouseX + "," + mouseY +
                          " bounds=" + bounds.x + "," + bounds.y + " size=" + width + "x" + height);

        // Check if clicking inside window
        if (!isMouseOverWindow(mouseX, mouseY)) {
            System.out.println("[EmojiPicker] Click outside window");
            return false;
        }

        System.out.println("[EmojiPicker] Click INSIDE window - processing!");

        if (button == 0) {
            // Check resize handle
            if (isMouseOverResizeHandle(mouseX, mouseY)) {
                resizing = true;
                resizeStartWidth = width;
                resizeStartHeight = height;
                dragOffsetX = mouseX;
                dragOffsetY = mouseY;
                return true;
            }

            // Check scrollbar
            if (needsScrollbar()) {
                int scrollbarX = bounds.x + width - SCROLLBAR_WIDTH - BORDER_SIZE - 2;
                int scrollbarY = bounds.y + HEADER_HEIGHT + TAB_HEIGHT + BORDER_SIZE;
                int scrollbarH = height - HEADER_HEIGHT - TAB_HEIGHT - BORDER_SIZE * 2 - 2;

                if (mouseX >= scrollbarX && mouseX < scrollbarX + SCROLLBAR_WIDTH &&
                    mouseY >= scrollbarY && mouseY < scrollbarY + scrollbarH) {

                    // Calculate thumb position
                    int totalRows = (int) Math.ceil((double) currentEmojis.size() / emojisPerRow);
                    int maxScroll = Math.max(0, totalRows - emojiRows);
                    int thumbHeight = Math.max(20, scrollbarH * emojiRows / totalRows);
                    int thumbY = scrollbarY + (scrollbarH - thumbHeight) * scrollOffset / maxScroll;

                    if (mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
                        // Clicked on thumb
                        scrollbarDragging = true;
                        scrollbarDragOffset = mouseY - thumbY;
                        return true;
                    }
                }
            }

            // Check header (for dragging)
            if (isMouseOverHeader(mouseX, mouseY)) {
                dragging = true;
                dragOffsetX = mouseX - bounds.x;
                dragOffsetY = mouseY - bounds.y;
                return true;
            }

            // Check tab clicks
            int tabY = bounds.y + HEADER_HEIGHT + BORDER_SIZE;
            if (mouseY >= tabY && mouseY < tabY + TAB_HEIGHT - BORDER_SIZE) {
                int tabWidth = (width - BORDER_SIZE * 2) / Category.values().length;
                int tabIndex = (mouseX - bounds.x - BORDER_SIZE) / tabWidth;

                if (tabIndex >= 0 && tabIndex < Category.values().length) {
                    loadCategory(Category.values()[tabIndex]);
                    return true;
                }
            }

            // Check emoji clicks
            int gridY = bounds.y + HEADER_HEIGHT + TAB_HEIGHT + BORDER_SIZE + 5;
            int relX = mouseX - bounds.x - BORDER_SIZE - 5;
            int relY = mouseY - gridY;

            int col = relX / (EMOJI_SIZE + EMOJI_PADDING);
            int row = relY / (EMOJI_SIZE + EMOJI_PADDING);

            if (col >= 0 && col < emojisPerRow && row >= 0 && row < emojiRows) {
                int index = (scrollOffset + row) * emojisPerRow + col;

                if (currentEmojis != null && index >= 0 && index < currentEmojis.size()) {
                    insertEmoji(currentEmojis.get(index));
                    return true;
                }
            }
        }

        return true; // Consume all clicks on window
    }

    /**
     * Handle mouse release
     */
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) {
            // Save position/size after dragging or resizing
            if (dragging || resizing) {
                savedBounds = new Rectangle(bounds);
            }
            dragging = false;
            resizing = false;
            scrollbarDragging = false;
        }
    }

    /**
     * Handle mouse drag
     */
    public void mouseDragged(int mouseX, int mouseY) {
        if (dragging) {
            bounds.x = mouseX - dragOffsetX;
            bounds.y = mouseY - dragOffsetY;

            // Constrain to screen
            ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int screenWidth = sr.getScaledWidth();
            int screenHeight = sr.getScaledHeight();

            bounds.x = Math.max(0, Math.min(bounds.x, screenWidth - width));
            bounds.y = Math.max(0, Math.min(bounds.y, screenHeight - height));
        } else if (resizing) {
            int deltaX = mouseX - dragOffsetX;
            int deltaY = mouseY - dragOffsetY;

            // Calculate new width and height
            int newWidth = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, resizeStartWidth + deltaX));
            // Invert deltaY: drag down (positive) = shrink, drag up (negative) = grow
            int newHeight = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, resizeStartHeight - deltaY));

            // When resizing from top-right corner, keep bottom edge fixed
            // Calculate how much height changed
            int heightDelta = newHeight - height;

            width = newWidth;
            height = newHeight;

            // Adjust Y to keep bottom edge in place (increase Y when shrinking, decrease when growing)
            bounds.y -= heightDelta;

            // Recalculate emojis per row and rows based on new size
            int availableWidth = width - BORDER_SIZE * 2 - 10 - SCROLLBAR_WIDTH - 2;
            emojisPerRow = Math.max(5, availableWidth / (EMOJI_SIZE + EMOJI_PADDING));

            int availableHeight = height - HEADER_HEIGHT - TAB_HEIGHT - BORDER_SIZE * 2 - 10;
            emojiRows = Math.max(3, availableHeight / (EMOJI_SIZE + EMOJI_PADDING));

            // Update bounds - bottom stays in place, window grows up
            bounds.width = width;
            bounds.height = height;
        } else if (scrollbarDragging) {
            int scrollbarY = bounds.y + HEADER_HEIGHT + TAB_HEIGHT + BORDER_SIZE;
            int scrollbarH = height - HEADER_HEIGHT - TAB_HEIGHT - BORDER_SIZE * 2 - 2;

            int totalRows = (int) Math.ceil((double) currentEmojis.size() / emojisPerRow);
            int maxScroll = Math.max(0, totalRows - emojiRows);
            int thumbHeight = Math.max(20, scrollbarH * emojiRows / totalRows);

            int thumbY = mouseY - scrollbarDragOffset;
            int relThumbY = thumbY - scrollbarY;
            scrollOffset = Math.max(0, Math.min(maxScroll, relThumbY * maxScroll / (scrollbarH - thumbHeight)));
        }
    }

    /**
     * Handle mouse scroll and input
     */
    public void handleMouseInput() throws IOException {
        if (!visible) {
            return;
        }

        // Calculate mouse coordinates (same as in original implementation)
        int mouseX = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
        int mouseY = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;

        // Check if mouse is over window
        if (isMouseOverWindow(mouseX, mouseY)) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                int direction = scroll > 0 ? -1 : 1;
                scrollEmoji(direction);
            }
        }
    }

    /**
     * Update dragging state (called from updateScreen)
     */
    public void updateDragging(int mouseX, int mouseY) {
        if (dragging || resizing || scrollbarDragging) {
            mouseDragged(mouseX, mouseY);
        }
    }

    /**
     * Scroll emoji list
     */
    private void scrollEmoji(int direction) {
        if (currentEmojis == null) return;

        int totalRows = (int) Math.ceil((double) currentEmojis.size() / emojisPerRow);
        int maxScroll = Math.max(0, totalRows - emojiRows);

        scrollOffset = Math.max(0, Math.min(scrollOffset + direction, maxScroll));
    }

    /**
     * Insert emoji at cursor position in target text field
     */
    private void insertEmoji(Emoji emoji) {
        if (targetTextField == null) {
            return;
        }

        String currentText = targetTextField.getText();
        int cursorPos = targetTextField.getCursorPosition();

        // Insert Unicode emoji character (not PUA) so it works with Discord/server
        String emojiChar = emoji.getSymbol(); // Gets the actual Unicode emoji

        // Insert emoji at cursor position
        String before = currentText.substring(0, cursorPos);
        String after = currentText.substring(cursorPos);
        String newText = before + emojiChar + after;

        targetTextField.setText(newText);
        targetTextField.setCursorPosition(cursorPos + emojiChar.length());
    }

    /**
     * Update picker (for animations, etc)
     */
    public void updateScreen() {
        // Reserved for future animations
    }
}
