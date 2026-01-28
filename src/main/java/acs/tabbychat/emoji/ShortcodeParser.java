package acs.tabbychat.emoji;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses shortcodes.po file to extract emoji mappings
 */
public class ShortcodeParser {
    private static final Pattern UNICODE_PATTERN = Pattern.compile("^#\\s+([0-9A-F]+(?:\\s+[0-9A-F]+)?)\\s*$");
    private static final Pattern MSGID_PATTERN = Pattern.compile("^msgid\\s+\"([^\"]+)\"\\s*$");
    private static final Pattern MSGCTXT_PATTERN = Pattern.compile("^msgctxt\\s+\"EMOJI:\\s+(.+?)\\s+(.+?)\"\\s*$");

    /**
     * Parse shortcodes.po file
     * @return Map of shortcode → EmojiData (base codepoint + full symbol)
     */
    public static Map<String, EmojiData> parseShortcodes(String filePath) {
        Map<String, EmojiData> shortcodeToEmoji = new HashMap<>();

        try {
            BufferedReader reader;
            File file = new File(filePath);

            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
            } else {
                // Try to load from resources
                InputStream is = ShortcodeParser.class.getResourceAsStream("/" + filePath);
                if (is == null) {
                    System.err.println("[TabbyChat/Emoji] Could not find shortcodes.po: " + filePath);
                    return shortcodeToEmoji;
                }
                reader = new BufferedReader(new InputStreamReader(is));
            }

            String line;
            String[] currentCodepoints = null;  // All codepoints in sequence
            String currentShortcode = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Parse unicode comment (# 1F44E or # 26CF FE0F or # 1F1E6 1F1F7 for flags)
                Matcher unicodeMatcher = UNICODE_PATTERN.matcher(line);
                if (unicodeMatcher.matches()) {
                    // Store ALL codepoints in the sequence
                    currentCodepoints = unicodeMatcher.group(1).split("\\s+");
                    continue;
                }

                // Parse msgid ("shortcode")
                Matcher msgidMatcher = MSGID_PATTERN.matcher(line);
                if (msgidMatcher.matches()) {
                    currentShortcode = msgidMatcher.group(1);

                    // Register mapping if we have both codepoints and shortcode
                    if (currentCodepoints != null && currentCodepoints.length > 0 &&
                        currentShortcode != null && !currentShortcode.isEmpty()) {
                        try {
                            // First codepoint is base (for Twemoji URL)
                            int baseCodepoint = Integer.parseInt(currentCodepoints[0], 16);

                            // Build full symbol from ALL codepoints
                            StringBuilder fullSymbol = new StringBuilder();
                            for (String hexCode : currentCodepoints) {
                                int cp = Integer.parseInt(hexCode, 16);
                                fullSymbol.append(Character.toChars(cp));
                            }

                            EmojiData emojiData = new EmojiData(baseCodepoint, fullSymbol.toString());
                            shortcodeToEmoji.put(currentShortcode.toLowerCase(), emojiData);

                        } catch (NumberFormatException e) {
                            // Skip invalid unicode
                        }
                    }

                    currentShortcode = null;
                    currentCodepoints = null;
                    continue;
                }
            }

            reader.close();
            System.out.println("[TabbyChat/Emoji] Parsed " + shortcodeToEmoji.size() + " emoji shortcodes");

        } catch (Exception e) {
            System.err.println("[TabbyChat/Emoji] Failed to parse shortcodes.po: " + e.getMessage());
            e.printStackTrace();
        }

        return shortcodeToEmoji;
    }

    /**
     * Convert Unicode codepoint to Twemoji filename
     * Example: 0x1F600 → "1f600.png"
     */
    public static String codepointToTwemojiFilename(int codepoint) {
        return Integer.toHexString(codepoint).toLowerCase() + ".png";
    }

    /**
     * Get Twemoji CDN URL for a codepoint
     */
    public static String getTwemojiUrl(int codepoint) {
        String filename = codepointToTwemojiFilename(codepoint);
        return "https://cdn.jsdelivr.net/gh/twitter/twemoji@latest/assets/72x72/" + filename;
    }
}
