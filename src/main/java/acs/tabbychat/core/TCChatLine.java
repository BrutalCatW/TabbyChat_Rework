package acs.tabbychat.core;

import acs.tabbychat.util.TCChatLineFake;
import com.google.gson.annotations.Expose;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.Date;

public class TCChatLine extends TCChatLineFake {
    @Expose
    public Date timeStamp;
    @Expose
    protected boolean statusMsg = false;

    // Cached processed text with emoji markers for rendering
    private String processedTextCache = null;

    public TCChatLine(int _counter, IChatComponent _string, int _id) {
        super(_counter, _string, _id);
    }

    public TCChatLine(ChatLine _cl) {
        super(_cl.getUpdatedCounter(), _cl.func_151461_a(), _cl.getChatLineID());
        if (_cl instanceof TCChatLine line) {
            timeStamp = line.timeStamp;
            statusMsg = line.statusMsg;
            processedTextCache = line.processedTextCache; // CRITICAL: Copy emoji processed text cache
        }
    }

    public TCChatLine(int _counter, IChatComponent _string, int _id, boolean _stat) {
        this(_counter, _string, _id);
        this.statusMsg = _stat;
    }

    protected void setChatLineString(IChatComponent newLine) {
        this.chatComponent = newLine;
    }

    public IChatComponent getTimeStamp() {
        String format = TabbyChat.generalSettings.timeStamp.format(timeStamp);
        return new ChatComponentText(format + " ");
    }

    public IChatComponent getChatComponentWithTimestamp() {
        IChatComponent result = getChatComponent();
        if (TabbyChat.generalSettings.timeStampEnable.getValue() && timeStamp != null) {
            result = getTimeStamp().appendSibling(result);
        }
        return result;
    }

    /**
     * Set cached processed text for emoji rendering
     */
    public void setProcessedText(String text) {
        this.processedTextCache = text;
    }

    /**
     * Get processed text for rendering (with PUA emoji markers and timestamp)
     * Falls back to getFormattedText() if not set
     */
    public String getProcessedText() {
        String text = processedTextCache;

        // Fallback to formatted text if no cached version (preserves color codes)
        if (text == null) {
            text = getChatComponent().getFormattedText();
        }

        // Add timestamp if enabled
        if (TabbyChat.generalSettings.timeStampEnable.getValue() && timeStamp != null) {
            String timestamp = TabbyChat.generalSettings.timeStamp.format(timeStamp);
            text = timestamp + " " + text;
        }

        return text;
    }
}
