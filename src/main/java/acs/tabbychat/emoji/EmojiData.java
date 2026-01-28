package acs.tabbychat.emoji;

/**
 * Container for emoji data parsed from shortcodes.po
 * Holds both the base codepoint (for Twemoji URLs) and full symbol sequence (for Discord)
 */
public class EmojiData {
    private final int baseCodepoint;     // Base codepoint for Twemoji URL (e.g. 0x26CF)
    private final String fullSymbol;     // Full symbol with variation selectors (e.g. "⛏️")

    public EmojiData(int baseCodepoint, String fullSymbol) {
        this.baseCodepoint = baseCodepoint;
        this.fullSymbol = fullSymbol;
    }

    public int getBaseCodepoint() {
        return baseCodepoint;
    }

    public String getFullSymbol() {
        return fullSymbol;
    }

    @Override
    public String toString() {
        return "EmojiData{baseCodepoint=0x" + Integer.toHexString(baseCodepoint).toUpperCase() +
               ", fullSymbol='" + fullSymbol + "'}";
    }
}
