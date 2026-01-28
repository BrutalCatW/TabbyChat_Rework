package acs.tabbychat.emoji;

/**
 * Represents a single emoji with its metadata
 */
public class Emoji {
    private final String shortcode;  // e.g. "smile", "heart", "fire"
    private final int unicode;       // Base Unicode codepoint for Twemoji URL (e.g. 0x26CF)
    private final String symbol;     // Full emoji symbol with variation selectors (e.g. "⛏️")

    /**
     * Create emoji with full symbol sequence (includes variation selectors)
     * @param shortcode Emoji shortcode (e.g. "pick")
     * @param baseCodepoint Base codepoint for Twemoji URL (e.g. 0x26CF)
     * @param fullSymbol Complete symbol with all codepoints (e.g. "⛏️" = U+26CF U+FE0F)
     */
    public Emoji(String shortcode, int baseCodepoint, String fullSymbol) {
        this.shortcode = shortcode.toLowerCase();
        this.unicode = baseCodepoint;
        this.symbol = fullSymbol;
    }

    public String getShortcode() {
        return shortcode;
    }

    public int getUnicode() {
        return unicode;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Get Twemoji CDN URL for this emoji
     */
    public String getTwemojiUrl() {
        return "https://cdn.jsdelivr.net/gh/twitter/twemoji@latest/assets/72x72/" +
               Integer.toHexString(unicode).toLowerCase() + ".png";
    }

    @Override
    public String toString() {
        return "Emoji{shortcode='" + shortcode + "', unicode=" + String.format("U+%04X", unicode) +
               ", symbol='" + symbol + "'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Emoji)) return false;
        Emoji other = (Emoji) obj;
        return unicode == other.unicode;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(unicode);
    }
}
