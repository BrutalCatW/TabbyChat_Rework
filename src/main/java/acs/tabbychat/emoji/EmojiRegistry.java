package acs.tabbychat.emoji;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all emoji data and mappings
 */
public class EmojiRegistry {
    // Use Unicode Private Use Area (U+E000-U+F8FF) to mark emoji positions
    // Each emoji gets a unique PUA character instead of ~shortcode~
    // This avoids string width calculation issues (1 char vs 11+ chars for shortcode)
    private static final int PUA_START = 0xE000; // Start of Private Use Area
    private static final String SHORTCODES_FILE = "shortcodes.po";

    private static final HashMap<String, Emoji> SHORTCODE_TO_EMOJI = new HashMap<>();
    private static final HashMap<Integer, Emoji> UNICODE_TO_EMOJI = new HashMap<>();
    private static final HashMap<Character, Emoji> PUA_TO_EMOJI = new HashMap<>(); // PUA char → Emoji
    private static final HashMap<Emoji, Character> EMOJI_TO_PUA = new HashMap<>(); // Emoji → PUA char

    // Aliases for Discord/other platforms that use different shortcode names
    private static final HashMap<String, String> SHORTCODE_ALIASES = new HashMap<>();

    private static boolean initialized = false;
    private static int nextPuaCodepoint = PUA_START;

    /**
     * Initialize emoji registry from shortcodes.po file
     */
    public static void init() {
        if (initialized) {
            return;
        }

        System.out.println("[TabbyChat/Emoji] Initializing emoji system from shortcodes.po...");

        try {
            // Parse shortcodes.po
            Map<String, EmojiData> emojiDataMap = ShortcodeParser.parseShortcodes(SHORTCODES_FILE);

            if (emojiDataMap.isEmpty()) {
                System.err.println("[TabbyChat/Emoji] No emoji loaded from shortcodes.po!");
                return;
            }

            // Create Emoji objects
            int count = 0;
            for (Map.Entry<String, EmojiData> entry : emojiDataMap.entrySet()) {
                String shortcode = entry.getKey();
                EmojiData data = entry.getValue();

                Emoji emoji = new Emoji(shortcode, data.getBaseCodepoint(), data.getFullSymbol());
                register(emoji);

                // Print first 10 emoji as examples
                if (count < 10) {
                    System.out.println("[TabbyChat/Emoji] Registered: " + emoji.getSymbol() +
                                      " (U+" + Integer.toHexString(data.getBaseCodepoint()).toUpperCase() +
                                      ") -> " + shortcode);
                }
                count++;
            }

            // Initialize aliases for Discord and other platforms
            initAliases();

            initialized = true;
            System.out.println("[TabbyChat/Emoji] Loaded " + SHORTCODE_TO_EMOJI.size() + " emoji from shortcodes.po");
            System.out.println("[TabbyChat/Emoji] Registered " + SHORTCODE_ALIASES.size() + " shortcode aliases");

            // Check if common emoji are registered
            System.out.println("[TabbyChat/Emoji] Checking common emoji:");
            System.out.println("  grinning (U+1F600): " + (UNICODE_TO_EMOJI.containsKey(0x1F600) ? "YES" : "NO"));
            System.out.println("  fire (U+1F525): " + (UNICODE_TO_EMOJI.containsKey(0x1F525) ? "YES" : "NO"));
            System.out.println("  heart (U+2764): " + (UNICODE_TO_EMOJI.containsKey(0x2764) ? "YES" : "NO"));
            System.out.println("  red_heart (U+2764): " + (SHORTCODE_TO_EMOJI.containsKey("red_heart") ? "YES" : "NO"));

        } catch (Exception e) {
            System.err.println("[TabbyChat/Emoji] Failed to load emoji: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register an emoji and assign it a PUA character
     */
    private static void register(Emoji emoji) {
        SHORTCODE_TO_EMOJI.put(emoji.getShortcode(), emoji);
        UNICODE_TO_EMOJI.put(emoji.getUnicode(), emoji);

        // Assign PUA character (Private Use Area U+E000-U+F8FF supports 6400 emoji)
        char puaChar = (char) nextPuaCodepoint++;
        PUA_TO_EMOJI.put(puaChar, emoji);
        EMOJI_TO_PUA.put(emoji, puaChar);

        if (nextPuaCodepoint > 0xF8FF) {
            System.err.println("[TabbyChat/Emoji] WARNING: Exceeded PUA capacity! Some emoji won't work.");
        }
    }

    /**
     * Initialize shortcode aliases for Discord and other platforms
     * Maps alternative shortcode names to canonical names from shortcodes.po
     */
    private static void initAliases() {
        // Discord uses different names for some emoji
        // Format: alias -> canonical_name_in_shortcodes.po

        // Smileys & Emotion
        addAlias("slight_smile", "slightly_smiling_face");  // 🙂
        addAlias("smile", "grinning");  // 😀 (sometimes)
        addAlias("smiley", "smiley");  // ☺️
        addAlias("grin", "grinning");  // 😀
        addAlias("laughing", "laughing");  // 😆
        addAlias("satisfied", "laughing");  // 😆
        addAlias("sweat_smile", "sweat_smile");  // 😅
        addAlias("joy", "joy");  // 😂
        addAlias("rofl", "rofl");  // 🤣
        addAlias("relaxed", "relaxed");  // ☺️
        addAlias("blush", "blush");  // 😊
        addAlias("innocent", "innocent");  // 😇
        addAlias("wink", "wink");  // 😉
        addAlias("relieved", "relieved");  // 😌
        addAlias("heart_eyes", "heart_eyes");  // 😍
        addAlias("kissing_heart", "kissing_heart");  // 😘
        addAlias("yum", "yum");  // 😋
        addAlias("stuck_out_tongue", "stuck_out_tongue");  // 😛
        addAlias("stuck_out_tongue_winking_eye", "stuck_out_tongue_winking_eye");  // 😜
        addAlias("stuck_out_tongue_closed_eyes", "stuck_out_tongue_closed_eyes");  // 😝
        addAlias("neutral_face", "neutral_face");  // 😐
        addAlias("expressionless", "expressionless");  // 😑
        addAlias("no_mouth", "no_mouth");  // 😶
        addAlias("smirk", "smirk");  // 😏
        addAlias("unamused", "unamused");  // 😒
        addAlias("grimacing", "grimacing");  // 😬
        addAlias("lying_face", "lying_face");  // 🤥
        addAlias("pensive", "pensive");  // 😔
        addAlias("sleepy", "sleepy");  // 😪
        addAlias("drooling_face", "drooling_face");  // 🤤
        addAlias("sleeping", "sleeping");  // 😴
        addAlias("mask", "mask");  // 😷
        addAlias("face_with_thermometer", "face_with_thermometer");  // 🤒
        addAlias("dizzy_face", "dizzy_face");  // 😵
        addAlias("astonished", "astonished");  // 😲
        addAlias("flushed", "flushed");  // 😳
        addAlias("scream", "scream");  // 😱
        addAlias("fearful", "fearful");  // 😨
        addAlias("cold_sweat", "cold_sweat");  // 😰
        addAlias("disappointed_relieved", "disappointed_relieved");  // 😥
        addAlias("cry", "cry");  // 😢
        addAlias("sob", "sob");  // 😭
        addAlias("confused", "confused");  // 😕
        addAlias("slightly_frowning_face", "slightly_frowning_face");  // 🙁
        addAlias("frowning", "frowning");  // ☹️
        addAlias("persevere", "persevere");  // 😣
        addAlias("confounded", "confounded");  // 😖
        addAlias("tired_face", "tired_face");  // 😫
        addAlias("weary", "weary");  // 😩
        addAlias("triumph", "triumph");  // 😤
        addAlias("angry", "angry");  // 😠
        addAlias("rage", "rage");  // 😡
        addAlias("symbols_over_mouth", "symbols_over_mouth");  // 🤬
        addAlias("smiling_imp", "smiling_imp");  // 😈
        addAlias("imp", "imp");  // 👿
        addAlias("skull", "skull");  // 💀
        addAlias("skull_crossbones", "skull_crossbones");  // ☠️
        addAlias("hankey", "hankey");  // 💩
        addAlias("poop", "hankey");  // 💩
        addAlias("shit", "hankey");  // 💩
        addAlias("clown", "clown");  // 🤡
        addAlias("ghost", "ghost");  // 👻
        addAlias("alien", "alien");  // 👽
        addAlias("robot", "robot");  // 🤖
        addAlias("jack_o_lantern", "jack_o_lantern");  // 🎃
        addAlias("smiley_cat", "smiley_cat");  // 😺
        addAlias("smile_cat", "smile_cat");  // 😸
        addAlias("joy_cat", "joy_cat");  // 😹
        addAlias("heart_eyes_cat", "heart_eyes_cat");  // 😻
        addAlias("smirk_cat", "smirk_cat");  // 😼
        addAlias("kissing_cat", "kissing_cat");  // 😽
        addAlias("scream_cat", "scream_cat");  // 🙀
        addAlias("crying_cat_face", "crying_cat_face");  // 😿
        addAlias("pouting_cat", "pouting_cat");  // 😾

        // Hearts
        addAlias("heart", "red_heart");  // ❤️
        addAlias("orange_heart", "orange_heart");  // 🧡
        addAlias("yellow_heart", "yellow_heart");  // 💛
        addAlias("green_heart", "green_heart");  // 💚
        addAlias("blue_heart", "blue_heart");  // 💙
        addAlias("purple_heart", "purple_heart");  // 💜
        addAlias("black_heart", "black_heart");  // 🖤
        addAlias("broken_heart", "broken_heart");  // 💔
        addAlias("heart_exclamation", "heart_exclamation");  // ❣️
        addAlias("two_hearts", "two_hearts");  // 💕
        addAlias("revolving_hearts", "revolving_hearts");  // 💞
        addAlias("heartbeat", "heartbeat");  // 💓
        addAlias("heartpulse", "heartpulse");  // 💗
        addAlias("sparkling_heart", "sparkling_heart");  // 💖
        addAlias("cupid", "cupid");  // 💘
        addAlias("gift_heart", "gift_heart");  // 💝

        // People & Body
        addAlias("wave", "wave");  // 👋
        addAlias("raised_back_of_hand", "raised_back_of_hand");  // 🤚
        addAlias("hand_splayed", "hand_splayed");  // 🖐️
        addAlias("raised_hand", "raised_hand");  // ✋
        addAlias("vulcan", "vulcan");  // 🖖
        addAlias("ok_hand", "ok_hand");  // 👌
        addAlias("v", "v");  // ✌️
        addAlias("fingers_crossed", "fingers_crossed");  // 🤞
        addAlias("metal", "metal");  // 🤘
        addAlias("call_me", "call_me");  // 🤙
        addAlias("point_left", "point_left");  // 👈
        addAlias("point_right", "point_right");  // 👉
        addAlias("point_up_2", "point_up_2");  // 👆
        addAlias("point_down", "point_down");  // 👇
        addAlias("point_up", "point_up");  // ☝️
        addAlias("+1", "+1");  // 👍
        addAlias("thumbsup", "+1");  // 👍
        addAlias("-1", "-1");  // 👎
        addAlias("thumbsdown", "-1");  // 👎
        addAlias("fist", "fist");  // ✊
        addAlias("punch", "punch");  // 👊
        addAlias("left_facing_fist", "left_facing_fist");  // 🤛
        addAlias("right_facing_fist", "right_facing_fist");  // 🤜
        addAlias("clap", "clap");  // 👏
        addAlias("raised_hands", "raised_hands");  // 🙌
        addAlias("open_hands", "open_hands");  // 👐
        addAlias("palms_up_together", "palms_up_together");  // 🤲
        addAlias("handshake", "handshake");  // 🤝
        addAlias("pray", "pray");  // 🙏

        // Animals & Nature
        addAlias("dog", "dog");  // 🐶
        addAlias("cat", "cat");  // 🐱
        addAlias("mouse", "mouse");  // 🐭
        addAlias("hamster", "hamster");  // 🐹
        addAlias("rabbit", "rabbit");  // 🐰
        addAlias("fox", "fox");  // 🦊
        addAlias("bear", "bear");  // 🐻
        addAlias("panda_face", "panda_face");  // 🐼
        addAlias("koala", "koala");  // 🐨
        addAlias("tiger", "tiger");  // 🐯
        addAlias("lion_face", "lion_face");  // 🦁
        addAlias("cow", "cow");  // 🐮
        addAlias("pig", "pig");  // 🐷
        addAlias("frog", "frog");  // 🐸
        addAlias("monkey_face", "monkey_face");  // 🐵
        addAlias("see_no_evil", "see_no_evil");  // 🙈
        addAlias("hear_no_evil", "hear_no_evil");  // 🙉
        addAlias("speak_no_evil", "speak_no_evil");  // 🙊

        // Food & Drink
        addAlias("coffee", "coffee");  // ☕
        addAlias("tea", "tea");  // 🍵
        addAlias("sake", "sake");  // 🍶
        addAlias("champagne", "champagne");  // 🍾
        addAlias("wine_glass", "wine_glass");  // 🍷
        addAlias("cocktail", "cocktail");  // 🍸
        addAlias("tropical_drink", "tropical_drink");  // 🍹
        addAlias("beer", "beer");  // 🍺
        addAlias("beers", "beers");  // 🍻
        addAlias("pizza", "pizza");  // 🍕
        addAlias("hamburger", "hamburger");  // 🍔
        addAlias("fries", "fries");  // 🍟
        addAlias("popcorn", "popcorn");  // 🍿
        addAlias("doughnut", "doughnut");  // 🍩
        addAlias("cookie", "cookie");  // 🍪
        addAlias("birthday", "birthday");  // 🎂
        addAlias("cake", "cake");  // 🍰
        addAlias("icecream", "icecream");  // 🍦
        addAlias("ice_cream", "icecream");  // 🍦
        addAlias("candy", "candy");  // 🍬
        addAlias("lollipop", "lollipop");  // 🍭
        addAlias("chocolate_bar", "chocolate_bar");  // 🍫
        addAlias("apple", "apple");  // 🍎
        addAlias("banana", "banana");  // 🍌
        addAlias("watermelon", "watermelon");  // 🍉
        addAlias("strawberry", "strawberry");  // 🍓
        addAlias("peach", "peach");  // 🍑
        addAlias("eggplant", "eggplant");  // 🍆

        // Activities & Sports
        addAlias("soccer", "soccer");  // ⚽
        addAlias("basketball", "basketball");  // 🏀
        addAlias("football", "football");  // 🏈
        addAlias("baseball", "baseball");  // ⚾
        addAlias("tennis", "tennis");  // 🎾
        addAlias("volleyball", "volleyball");  // 🏐
        addAlias("8ball", "8ball");  // 🎱
        addAlias("golf", "golf");  // ⛳
        addAlias("medal", "medal");  // 🏅
        addAlias("trophy", "trophy");  // 🏆
        addAlias("dart", "dart");  // 🎯
        addAlias("game_die", "game_die");  // 🎲
        addAlias("bowling", "bowling");  // 🎳

        // Travel & Places
        addAlias("rocket", "rocket");  // 🚀
        addAlias("airplane", "airplane");  // ✈️
        addAlias("car", "car");  // 🚗
        addAlias("taxi", "taxi");  // 🚕
        addAlias("bus", "bus");  // 🚌
        addAlias("train", "train");  // 🚆
        addAlias("bike", "bike");  // 🚲
        addAlias("house", "house");  // 🏠
        addAlias("office", "office");  // 🏢
        addAlias("hospital", "hospital");  // 🏥
        addAlias("school", "school");  // 🏫
        addAlias("hotel", "hotel");  // 🏨
        addAlias("bank", "bank");  // 🏦
        addAlias("earth_africa", "earth_africa");  // 🌍
        addAlias("earth_americas", "earth_americas");  // 🌎
        addAlias("earth_asia", "earth_asia");  // 🌏
        addAlias("moon", "moon");  // 🌙
        addAlias("star", "star");  // ⭐
        addAlias("star2", "star2");  // 🌟
        addAlias("zap", "zap");  // ⚡
        addAlias("fire", "fire");  // 🔥
        addAlias("boom", "boom");  // 💥
        addAlias("collision", "boom");  // 💥
        addAlias("snowflake", "snowflake");  // ❄️
        addAlias("cloud", "cloud");  // ☁️
        addAlias("sunny", "sunny");  // ☀️
        addAlias("rainbow", "rainbow");  // 🌈
        addAlias("umbrella", "umbrella");  // ☂️

        // Objects
        addAlias("watch", "watch");  // ⌚
        addAlias("iphone", "iphone");  // 📱
        addAlias("calling", "calling");  // 📲
        addAlias("computer", "computer");  // 💻
        addAlias("keyboard", "keyboard");  // ⌨️
        addAlias("desktop", "desktop");  // 🖥️
        addAlias("printer", "printer");  // 🖨️
        addAlias("mouse_three_button", "mouse_three_button");  // 🖱️
        addAlias("trackball", "trackball");  // 🖲️
        addAlias("joystick", "joystick");  // 🕹️
        addAlias("camera", "camera");  // 📷
        addAlias("video_camera", "video_camera");  // 📹
        addAlias("tv", "tv");  // 📺
        addAlias("radio", "radio");  // 📻
        addAlias("vhs", "vhs");  // 📼
        addAlias("mag", "mag");  // 🔍
        addAlias("mag_right", "mag_right");  // 🔎
        addAlias("bulb", "bulb");  // 💡
        addAlias("flashlight", "flashlight");  // 🔦
        addAlias("candle", "candle");  // 🕯️
        addAlias("book", "book");  // 📖
        addAlias("notebook", "notebook");  // 📓
        addAlias("closed_book", "closed_book");  // 📕
        addAlias("green_book", "green_book");  // 📗
        addAlias("blue_book", "blue_book");  // 📘
        addAlias("orange_book", "orange_book");  // 📙
        addAlias("books", "books");  // 📚
        addAlias("bookmark", "bookmark");  // 🔖
        addAlias("link", "link");  // 🔗
        addAlias("paperclip", "paperclip");  // 📎
        addAlias("pencil2", "pencil2");  // ✏️
        addAlias("pen_ballpoint", "pen_ballpoint");  // 🖊️
        addAlias("pen_fountain", "pen_fountain");  // 🖋️
        addAlias("paintbrush", "paintbrush");  // 🖌️
        addAlias("crayon", "crayon");  // 🖍️
        addAlias("memo", "memo");  // 📝
        addAlias("pencil", "memo");  // 📝
        addAlias("briefcase", "briefcase");  // 💼
        addAlias("file_folder", "file_folder");  // 📁
        addAlias("open_file_folder", "open_file_folder");  // 📂
        addAlias("card_index_dividers", "card_index_dividers");  // 🗂️
        addAlias("calendar", "calendar");  // 📆
        addAlias("spiral_calendar_pad", "spiral_calendar_pad");  // 🗓️
        addAlias("chart_with_upwards_trend", "chart_with_upwards_trend");  // 📈
        addAlias("chart_with_downwards_trend", "chart_with_downwards_trend");  // 📉
        addAlias("bar_chart", "bar_chart");  // 📊
        addAlias("clipboard", "clipboard");  // 📋
        addAlias("pushpin", "pushpin");  // 📌
        addAlias("round_pushpin", "round_pushpin");  // 📍
        addAlias("scissors", "scissors");  // ✂️
        addAlias("lock", "lock");  // 🔒
        addAlias("unlock", "unlock");  // 🔓
        addAlias("key", "key");  // 🔑
        addAlias("hammer", "hammer");  // 🔨
        addAlias("pick", "pick");  // ⛏️
        addAlias("hammer_pick", "hammer_pick");  // ⚒️
        addAlias("wrench", "wrench");  // 🔧
        addAlias("nut_and_bolt", "nut_and_bolt");  // 🔩
        addAlias("gear", "gear");  // ⚙️
        addAlias("gun", "gun");  // 🔫
        addAlias("bomb", "bomb");  // 💣
        addAlias("knife", "knife");  // 🔪
        addAlias("shield", "shield");  // 🛡️
        addAlias("smoking", "smoking");  // 🚬
        addAlias("pill", "pill");  // 💊
        addAlias("syringe", "syringe");  // 💉

        // Symbols
        addAlias("100", "100");  // 💯
        addAlias("1234", "1234");  // 🔢
        addAlias("hash", "hash");  // #️⃣
        addAlias("keycap_star", "keycap_star");  // *️⃣
        addAlias("zero", "zero");  // 0️⃣
        addAlias("one", "one");  // 1️⃣
        addAlias("two", "two");  // 2️⃣
        addAlias("three", "three");  // 3️⃣
        addAlias("four", "four");  // 4️⃣
        addAlias("five", "five");  // 5️⃣
        addAlias("six", "six");  // 6️⃣
        addAlias("seven", "seven");  // 7️⃣
        addAlias("eight", "eight");  // 8️⃣
        addAlias("nine", "nine");  // 9️⃣
        addAlias("ten", "ten");  // 🔟
        addAlias("arrow_up", "arrow_up");  // ⬆️
        addAlias("arrow_down", "arrow_down");  // ⬇️
        addAlias("arrow_left", "arrow_left");  // ⬅️
        addAlias("arrow_right", "arrow_right");  // ➡️
        addAlias("checkmark", "white_check_mark");  // ✅
        addAlias("white_check_mark", "white_check_mark");  // ✅
        addAlias("x", "x");  // ❌
        addAlias("o", "o");  // ⭕
        addAlias("exclamation", "exclamation");  // ❗
        addAlias("question", "question");  // ❓
        addAlias("grey_exclamation", "grey_exclamation");  // ❕
        addAlias("grey_question", "grey_question");  // ❔
        addAlias("heavy_plus_sign", "heavy_plus_sign");  // ➕
        addAlias("heavy_minus_sign", "heavy_minus_sign");  // ➖
        addAlias("heavy_division_sign", "heavy_division_sign");  // ➗
        addAlias("heavy_multiplication_x", "heavy_multiplication_x");  // ✖️
        addAlias("bangbang", "bangbang");  // ‼️
        addAlias("interrobang", "interrobang");  // ⁉️
        addAlias("copyright", "copyright");  // ©️
        addAlias("registered", "registered");  // ®️
        addAlias("tm", "tm");  // ™️
    }

    /**
     * Add a shortcode alias
     */
    private static void addAlias(String alias, String canonicalShortcode) {
        SHORTCODE_ALIASES.put(alias.toLowerCase(), canonicalShortcode.toLowerCase());
    }

    /**
     * Get emoji by shortcode (supports aliases)
     */
    public static Emoji getEmojiByShortcode(String shortcode) {
        if (shortcode == null) return null;

        String normalized = shortcode.toLowerCase();

        // Try direct lookup first
        Emoji emoji = SHORTCODE_TO_EMOJI.get(normalized);
        if (emoji != null) {
            return emoji;
        }

        // Try alias lookup
        String canonicalShortcode = SHORTCODE_ALIASES.get(normalized);
        if (canonicalShortcode != null) {
            return SHORTCODE_TO_EMOJI.get(canonicalShortcode);
        }

        return null;
    }

    /**
     * Get emoji by Unicode codepoint
     */
    public static Emoji getEmojiByUnicode(int codepoint) {
        return UNICODE_TO_EMOJI.get(codepoint);
    }

    /**
     * Get emoji by PUA (Private Use Area) character
     */
    public static Emoji getEmojiByPUA(char puaChar) {
        return PUA_TO_EMOJI.get(puaChar);
    }

    /**
     * Get all registered emoji
     */
    public static Collection<Emoji> getAllEmoji() {
        return SHORTCODE_TO_EMOJI.values();
    }

    /**
     * Convert Unicode emoji in text to PUA marker format
     * Example: "Hello 😀" → "Hello \uE000" (1 PUA character per emoji)
     */
    public static String convertUnicodeToMarkers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int emojiCount = 0;

        for (int i = 0; i < text.length(); i++) {
            int codePoint = text.codePointAt(i);

            // Check if this codepoint is an emoji
            Emoji emoji = UNICODE_TO_EMOJI.get(codePoint);

            if (emoji != null) {
                // Found emoji - convert to PUA character
                Character puaChar = EMOJI_TO_PUA.get(emoji);
                if (puaChar != null) {
                    result.append(puaChar);
                    emojiCount++;
                }

                // Skip second part of surrogate pair if this is a supplementary codepoint
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    i++;
                }

                // Skip variation selector if present (U+FE0F or U+FE0E)
                if (i + 1 < text.length()) {
                    int nextCodePoint = text.codePointAt(i + 1);
                    if (nextCodePoint == 0xFE0F || nextCodePoint == 0xFE0E) {
                        i++;
                    }
                }
            } else {
                // Not an emoji - add as regular character
                // Skip variation selectors that weren't preceded by emoji
                if (codePoint == 0xFE0F || codePoint == 0xFE0E) {
                    // Skip orphan variation selector
                } else {
                    result.append(Character.toChars(codePoint));
                }

                // Skip second part of surrogate pair if this is a supplementary codepoint
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    i++;
                }
            }
        }

        if (emojiCount > 0) {
            System.out.println("[TabbyChat/Emoji] Converted " + emojiCount + " emoji in message");
        }
        return result.toString();
    }

    /**
     * Find all emoji in text by PUA markers
     * Example: "Hello \uE000" → [Emoji(grinning)]
     */
    public static ArrayList<Emoji> getEmojisInText(String text) {
        ArrayList<Emoji> list = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return list;
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Emoji emoji = getEmojiByPUA(c);
            if (emoji != null) {
                list.add(emoji);
            }
        }

        return list;
    }

    /**
     * Convert Discord shortcodes to PUA markers
     * Example: "Hello :grinning: :fire:" → "Hello \uE000 \uE001"
     */
    public static String convertShortcodesToPUA(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        int convertedCount = 0;

        while (i < text.length()) {
            if (text.charAt(i) == ':') {
                // Find closing :
                int end = text.indexOf(':', i + 1);
                if (end > i + 1) {
                    String shortcode = text.substring(i + 1, end);
                    Emoji emoji = getEmojiByShortcode(shortcode);

                    if (emoji != null) {
                        // Convert to PUA
                        Character puaChar = EMOJI_TO_PUA.get(emoji);
                        if (puaChar != null) {
                            result.append(puaChar);
                            convertedCount++;
                            i = end + 1;
                            continue;
                        }
                    }
                }
            }

            // Not a shortcode, add as-is
            result.append(text.charAt(i));
            i++;
        }

        if (convertedCount > 0) {
            System.out.println("[TabbyChat/Emoji] Converted " + convertedCount + " shortcodes to emoji");
        }

        return result.toString();
    }

    /**
     * Convert ASCII emoji aliases to PUA markers
     * Example: "Hello :) <3" → "Hello \uE000 \uE001"
     */
    public static String convertAsciiToPUA(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // ASCII alias mappings (most common ones)
        // Order matters - longer patterns first to avoid conflicts
        String[][] asciiAliases = {
            // Hearts
            {"<3", "heart"},
            {"</3", "broken_heart"},

            // Smiles
            {":D", "grinning"},
            {":-D", "grinning"},
            {":)", "slight_smile"},
            {":-)", "slight_smile"},
            {";)", "wink"},
            {";-)", "wink"},
            {":]", "slight_smile"},
            {":-]", "slight_smile"},
            {":>", "slight_smile"},
            {"=)", "slight_smile"},

            // Sad
            {":(", "slightly_frowning_face"},
            {":-(", "slightly_frowning_face"},
            {":[", "slightly_frowning_face"},
            {":-[", "slightly_frowning_face"},
            {":<", "slightly_frowning_face"},
            {"=(", "slightly_frowning_face"},

            // Other
            {":P", "stuck_out_tongue"},
            {":-P", "stuck_out_tongue"},
            {":p", "stuck_out_tongue"},
            {":-p", "stuck_out_tongue"},
            {":O", "open_mouth"},
            {":-O", "open_mouth"},
            {":o", "open_mouth"},
            {":-o", "open_mouth"},
            {":/", "confused"},
            {":-/", "confused"},
            {":|", "neutral_face"},
            {":-|", "neutral_face"},
            {"^_^", "smile"},
            {">:(", "angry"},
            {">:-(", "angry"},
            {"O:)", "innocent"},
            {"O:-)", "innocent"},
        };

        String result = text;
        int convertedCount = 0;

        // Replace each ASCII pattern with PUA
        for (String[] mapping : asciiAliases) {
            String asciiPattern = mapping[0];
            String shortcode = mapping[1];

            Emoji emoji = getEmojiByShortcode(shortcode);
            if (emoji != null) {
                Character puaChar = EMOJI_TO_PUA.get(emoji);
                if (puaChar != null) {
                    String puaString = String.valueOf(puaChar);

                    // Escape special regex chars
                    String escapedPattern = asciiPattern
                        .replace("\\", "\\\\")
                        .replace(".", "\\.")
                        .replace("(", "\\(")
                        .replace(")", "\\)")
                        .replace("[", "\\[")
                        .replace("]", "\\]")
                        .replace("^", "\\^")
                        .replace("$", "\\$")
                        .replace("|", "\\|")
                        .replace("?", "\\?")
                        .replace("*", "\\*")
                        .replace("+", "\\+")
                        .replace("<", "\\<")
                        .replace(">", "\\>");

                    // Use regex with boundaries to avoid replacing in URLs
                    // Don't replace if preceded by alphanumeric (prevents https:// → https😕/)
                    // Don't replace if followed by / and preceded by : (prevents :// in URLs)
                    String regexPattern;
                    if (asciiPattern.contains(":")) {
                        // For patterns with ":", use negative lookbehind to exclude URLs
                        // (?<![a-zA-Z0-9]) = not preceded by letter/digit
                        regexPattern = "(?<![a-zA-Z0-9])" + escapedPattern;
                    } else {
                        // For patterns without ":", use word boundaries
                        regexPattern = "(?<!\\w)" + escapedPattern + "(?!\\w)";
                    }

                    int oldLength = result.length();
                    result = result.replaceAll(regexPattern, puaString);
                    if (result.length() != oldLength) {
                        convertedCount++;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Convert a position in the original text to a position in the converted text
     * (after Unicode emoji → PUA conversion)
     *
     * This is needed because emoji surrogate pairs (2 chars) become PUA markers (1 char),
     * so cursor positions need to be adjusted.
     *
     * @param originalText The original text with Unicode emoji
     * @param originalPos Position in the original text
     * @return Equivalent position in the converted text
     */
    public static int convertPosition(String originalText, int originalPos) {
        if (originalText == null || originalText.isEmpty() || originalPos <= 0) {
            return 0;
        }

        // Clamp originalPos to text length
        if (originalPos > originalText.length()) {
            originalPos = originalText.length();
        }

        int convertedPos = 0;

        for (int i = 0; i < originalPos && i < originalText.length(); ) {
            int codePoint = originalText.codePointAt(i);
            Emoji emoji = UNICODE_TO_EMOJI.get(codePoint);

            if (emoji != null) {
                // This is an emoji - it will become 1 PUA char in converted text
                convertedPos++;

                // Skip surrogate pair if supplementary codepoint
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    i += 2;  // Skip both high and low surrogate
                } else {
                    i++;
                }

                // Skip variation selector if present
                if (i < originalText.length()) {
                    int nextCodePoint = originalText.codePointAt(i);
                    if (nextCodePoint == 0xFE0F || nextCodePoint == 0xFE0E) {
                        i++;
                    }
                }
            } else {
                // Regular character - stays the same in converted text
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    // Supplementary character (non-emoji) - still 2 chars in both texts
                    convertedPos += 2;
                    i += 2;
                } else if (codePoint == 0xFE0F || codePoint == 0xFE0E) {
                    // Orphan variation selector - skipped in conversion
                    i++;
                    // Don't increment convertedPos (variation selector is removed)
                } else {
                    // Normal char
                    convertedPos++;
                    i++;
                }
            }
        }

        return convertedPos;
    }

    /**
     * Check if initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Get total count of registered emoji
     */
    public static int getEmojiCount() {
        return SHORTCODE_TO_EMOJI.size();
    }
}
