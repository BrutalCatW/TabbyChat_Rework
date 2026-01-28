package acs.tabbychat.emoji;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Emoji categories for organizing emoji picker
 */
public class EmojiCategory {
    public enum Category {
        SMILEYS("Люди", "grinning"),
        ANIMALS("Животные", "dog"),
        FOOD("Еда", "apple"),
        ACTIVITIES("Активности", "soccer"),
        TRAVEL("Путешествия", "rocket"),
        OBJECTS("Объекты", "bulb"),
        SYMBOLS("Символы", "heart"),
        ALL("Все", "star");

        private final String displayName;
        private final String iconShortcode;

        Category(String displayName, String iconShortcode) {
            this.displayName = displayName;
            this.iconShortcode = iconShortcode;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIconShortcode() {
            return iconShortcode;
        }
    }

    /**
     * Get all emoji for a specific category
     */
    public static List<Emoji> getEmojiForCategory(Category category) {
        List<Emoji> result = new ArrayList<>();

        if (category == Category.ALL) {
            result.addAll(EmojiRegistry.getAllEmoji());
            return result;
        }

        // Get category-specific emoji by checking shortcodes
        Set<String> categoryShortcodes = getCategoryShortcodes(category);

        for (Emoji emoji : EmojiRegistry.getAllEmoji()) {
            if (categoryShortcodes.contains(emoji.getShortcode())) {
                result.add(emoji);
            }
        }

        return result;
    }

    /**
     * Get emoji icon for category tab
     */
    public static Emoji getCategoryIcon(Category category) {
        String iconShortcode;

        switch (category) {
            case SMILEYS: iconShortcode = "grinning"; break;
            case ANIMALS: iconShortcode = "dog"; break;
            case FOOD: iconShortcode = "apple"; break;
            case ACTIVITIES: iconShortcode = "soccer"; break;
            case TRAVEL: iconShortcode = "rocket"; break;
            case OBJECTS: iconShortcode = "bulb"; break;
            case SYMBOLS: iconShortcode = "red_heart"; break;
            case ALL: iconShortcode = "star"; break;
            default: return null;
        }

        return EmojiRegistry.getEmojiByShortcode(iconShortcode);
    }

    /**
     * Get shortcodes for each category based on common emoji
     */
    private static Set<String> getCategoryShortcodes(Category category) {
        Set<String> shortcodes = new HashSet<>();

        switch (category) {
            case SMILEYS:
                // Smileys & Emotion
                addShortcodes(shortcodes,
                    "grinning", "grimacing", "grin", "joy", "rofl", "smiley", "smile", "sweat_smile",
                    "laughing", "innocent", "wink", "blush", "slightly_smiling_face", "upside_down_face",
                    "relaxed", "yum", "relieved", "heart_eyes", "kissing_heart", "kissing",
                    "kissing_smiling_eyes", "kissing_closed_eyes", "stuck_out_tongue_winking_eye",
                    "stuck_out_tongue_closed_eyes", "stuck_out_tongue", "money_mouth_face", "nerd_face",
                    "sunglasses", "hugging_face", "smirk", "no_mouth", "neutral_face", "expressionless",
                    "unamused", "face_with_rolling_eyes", "thinking_face", "lying_face", "flushed",
                    "disappointed", "worried", "angry", "rage", "pensive", "confused", "slightly_frowning_face",
                    "frowning", "persevere", "confounded", "tired_face", "weary", "triumph", "open_mouth",
                    "scream", "fearful", "cold_sweat", "hushed", "frowning2", "anguished", "cry",
                    "disappointed_relieved", "drooling_face", "sleepy", "sweat", "sob", "dizzy_face",
                    "astonished", "zipper_mouth_face", "face_with_thermometer", "face_with_head_bandage",
                    "sleeping", "zzz", "hankey", "smiling_imp", "imp", "japanese_ogre", "japanese_goblin",
                    "skull", "ghost", "alien", "robot", "smiley_cat", "smile_cat", "joy_cat",
                    "heart_eyes_cat", "smirk_cat", "kissing_cat", "scream_cat", "crying_cat_face",
                    "pouting_cat", "see_no_evil", "hear_no_evil", "speak_no_evil"
                );
                break;

            case ANIMALS:
                // Animals & Nature
                addShortcodes(shortcodes,
                    "dog", "cat", "mouse", "hamster", "rabbit", "fox", "bear", "panda_face",
                    "koala", "tiger", "lion_face", "cow", "pig", "pig_nose", "frog", "monkey_face",
                    "monkey", "gorilla", "dog2", "poodle", "wolf", "cat2", "tiger2", "leopard",
                    "horse", "racehorse", "unicorn_face", "zebra_face", "deer", "ox", "water_buffalo",
                    "cow2", "pig2", "boar", "ram", "sheep", "goat", "dromedary_camel", "camel",
                    "elephant", "rhino", "mouse2", "rat", "hamster2", "rabbit2", "chipmunk",
                    "bat", "bear2", "koala2", "panda", "feet", "turkey", "chicken", "rooster",
                    "hatching_chick", "baby_chick", "hatched_chick", "bird", "penguin", "dove_of_peace",
                    "eagle", "duck", "owl", "frog2", "crocodile", "turtle", "lizard", "snake",
                    "dragon_face", "dragon", "sauropod", "t_rex", "whale", "whale2", "dolphin",
                    "fish", "tropical_fish", "blowfish", "shark", "octopus", "shell", "snail",
                    "butterfly", "bug", "ant", "bee", "beetle", "lady_beetle", "cricket", "spider",
                    "spider_web", "scorpion", "bouquet", "cherry_blossom", "white_flower", "rosette",
                    "rose", "wilted_flower", "hibiscus", "sunflower", "blossom", "tulip", "seedling",
                    "evergreen_tree", "deciduous_tree", "palm_tree", "cactus", "ear_of_rice", "herb",
                    "shamrock", "four_leaf_clover", "maple_leaf", "fallen_leaf", "leaves", "mushroom"
                );
                break;

            case FOOD:
                // Food & Drink
                addShortcodes(shortcodes,
                    "grapes", "melon", "watermelon", "tangerine", "lemon", "banana", "pineapple",
                    "apple", "green_apple", "pear", "peach", "cherries", "strawberry", "kiwi_fruit",
                    "tomato", "avocado", "eggplant", "potato", "carrot", "corn", "hot_pepper",
                    "cucumber", "broccoli", "mushroom", "peanuts", "chestnut", "bread", "croissant",
                    "baguette_bread", "pretzel", "pancakes", "cheese_wedge", "meat_on_bone",
                    "poultry_leg", "bacon", "hamburger", "fries", "pizza", "hotdog", "sandwich",
                    "taco", "burrito", "stuffed_flatbread", "egg", "fried_egg", "shallow_pan_of_food",
                    "stew", "bowl_with_spoon", "green_salad", "popcorn", "canned_food", "bento",
                    "rice_cracker", "rice_ball", "rice", "curry", "ramen", "spaghetti", "sweet_potato",
                    "oden", "sushi", "fried_shrimp", "fish_cake", "dango", "icecream", "shaved_ice",
                    "ice_cream", "doughnut", "cookie", "birthday", "cake", "pie", "chocolate_bar",
                    "candy", "lollipop", "custard", "honey_pot", "baby_bottle", "milk_glass",
                    "coffee", "tea", "sake", "champagne", "wine_glass", "cocktail", "tropical_drink",
                    "beer", "beers", "clinking_glasses", "tumbler_glass"
                );
                break;

            case ACTIVITIES:
                // Activities & Sports
                addShortcodes(shortcodes,
                    "soccer", "basketball", "football", "baseball", "tennis", "volleyball",
                    "rugby_football", "8ball", "table_tennis_paddle_and_ball", "badminton_racquet_and_shuttlecock",
                    "goal_net", "ice_hockey_stick_and_puck", "field_hockey_stick_and_ball",
                    "cricket_bat_and_ball", "golf", "bow_and_arrow", "fishing_pole_and_fish",
                    "boxing_glove", "martial_arts_uniform", "running_shirt_with_sash", "ski",
                    "skier", "ice_skate", "snowboarder", "person_fencing", "wrestlers",
                    "person_cartwheeling", "basketball_player", "weight_lifter", "bicyclist",
                    "mountain_bicyclist", "person_doing_cartwheel", "racing_car", "racing_motorcycle",
                    "trophy", "medal", "first_place_medal", "second_place_medal", "third_place_medal",
                    "dart", "yo_yo", "kite", "gun", "8ball", "video_game", "slot_machine",
                    "game_die", "jigsaw", "bowling", "musical_score", "musical_keyboard",
                    "drum_with_drumsticks", "saxophone", "trumpet", "guitar", "violin", "clapper",
                    "movie_camera", "headphones", "art", "performing_arts", "microphone", "microphone2"
                );
                break;

            case TRAVEL:
                // Travel & Places
                addShortcodes(shortcodes,
                    "earth_africa", "earth_americas", "earth_asia", "globe_with_meridians",
                    "world_map", "japan", "snow_capped_mountain", "mountain", "volcano",
                    "mount_fuji", "camping", "beach_with_umbrella", "desert", "desert_island",
                    "national_park", "stadium", "classical_building", "building_construction",
                    "house_buildings", "cityscape", "derelict_house_building", "house",
                    "house_with_garden", "office", "post_office", "european_post_office",
                    "hospital", "bank", "hotel", "love_hotel", "convenience_store", "school",
                    "department_store", "factory", "japanese_castle", "european_castle",
                    "wedding", "tokyo_tower", "statue_of_liberty", "church", "mosque",
                    "synagogue", "shinto_shrine", "kaaba", "fountain", "tent", "foggy",
                    "night_with_stars", "sunrise_over_mountains", "sunrise", "city_sunset",
                    "city_sunrise", "bridge_at_night", "hotsprings", "milky_way", "carousel_horse",
                    "ferris_wheel", "roller_coaster", "steam_locomotive", "railway_car",
                    "bullettrain_side", "bullettrain_front", "train2", "metro", "light_rail",
                    "station", "tram", "monorail", "mountain_railway", "train", "bus",
                    "oncoming_bus", "trolleybus", "minibus", "ambulance", "fire_engine",
                    "police_car", "oncoming_police_car", "taxi", "oncoming_taxi", "car",
                    "oncoming_automobile", "blue_car", "truck", "articulated_lorry", "tractor",
                    "bike", "scooter", "motor_scooter", "busstop", "motorway", "railway_track",
                    "fuelpump", "rotating_light", "traffic_light", "vertical_traffic_light",
                    "construction", "anchor", "boat", "canoe", "speedboat", "passenger_ship",
                    "ferry", "motor_boat", "ship", "airplane", "small_airplane", "airplane_departure",
                    "airplane_arriving", "seat", "helicopter", "suspension_railway", "mountain_cableway",
                    "aerial_tramway", "satellite", "rocket", "flying_saucer"
                );
                break;

            case OBJECTS:
                // Objects
                addShortcodes(shortcodes,
                    "watch", "iphone", "calling", "computer", "keyboard", "desktop_computer",
                    "printer", "mouse_three_button", "trackball", "joystick", "compression",
                    "minidisc", "floppy_disk", "cd", "dvd", "vhs", "camera", "camera_with_flash",
                    "video_camera", "movie_camera", "film_projector", "film_frames", "telephone_receiver",
                    "phone", "pager", "fax", "tv", "radio", "microphone2", "level_slider",
                    "control_knobs", "stopwatch", "timer_clock", "alarm_clock", "mantelpiece_clock",
                    "hourglass_flowing_sand", "hourglass", "satellite_antenna", "battery",
                    "electric_plug", "bulb", "flashlight", "candle", "wastebasket", "oil_drum",
                    "money_with_wings", "dollar", "yen", "euro", "pound", "moneybag",
                    "credit_card", "gem", "scales", "wrench", "hammer", "hammer_and_pick",
                    "hammer_and_wrench", "pick", "nut_and_bolt", "gear", "chains", "gun",
                    "bomb", "knife", "dagger_knife", "crossed_swords", "shield", "smoking",
                    "coffin", "funeral_urn", "amphora", "crystal_ball", "prayer_beads", "barber",
                    "alembic", "telescope", "microscope", "hole", "pill", "syringe", "thermometer",
                    "label", "bookmark", "toilet", "shower", "bathtub", "key", "old_key",
                    "couch_and_lamp", "sleeping_accommodation", "bed", "door", "bellhop_bell",
                    "frame_with_picture", "world_map", "umbrella_on_ground", "moyai", "shopping_bags",
                    "balloon", "flags", "ribbon", "gift", "confetti_ball", "tada", "dolls",
                    "izakaya_lantern", "wind_chime", "email", "envelope_with_arrow", "incoming_envelope",
                    "e_mail", "love_letter", "postbox", "mailbox_closed", "mailbox", "mailbox_with_mail",
                    "mailbox_with_no_mail", "package", "postal_horn", "inbox_tray", "outbox_tray",
                    "scroll", "page_with_curl", "bookmark_tabs", "bar_chart", "chart_with_upwards_trend",
                    "chart_with_downwards_trend", "page_facing_up", "date", "calendar", "spiral_calendar_pad",
                    "card_index", "card_file_box", "ballot_box_with_ballot", "file_cabinet",
                    "clipboard", "spiral_note_pad", "file_folder", "open_file_folder", "card_index_dividers",
                    "rolled_up_newspaper", "newspaper", "notebook", "closed_book", "green_book",
                    "blue_book", "orange_book", "notebook_with_decorative_cover", "ledger", "books",
                    "book", "link", "paperclip", "linked_paperclips", "scissors", "triangular_ruler",
                    "straight_ruler", "pushpin", "round_pushpin", "triangular_flag_on_post",
                    "flag_white", "flag_black", "closed_lock_with_key", "lock", "unlock",
                    "lock_with_ink_pen", "lower_left_ballpoint_pen", "lower_left_fountain_pen",
                    "black_nib", "memo", "pencil2", "lower_left_crayon", "lower_left_paintbrush",
                    "mag", "mag_right"
                );
                break;

            case SYMBOLS:
                // Symbols & Hearts
                addShortcodes(shortcodes,
                    "red_heart", "orange_heart", "yellow_heart", "green_heart", "blue_heart",
                    "purple_heart", "black_heart", "broken_heart", "heart_exclamation", "two_hearts",
                    "revolving_hearts", "heartbeat", "heartpulse", "sparkling_heart", "cupid",
                    "gift_heart", "heart_decoration", "100", "1234", "arrow_forward", "arrow_backward",
                    "arrow_up", "arrow_down", "arrow_left", "arrow_right", "arrow_upper_right",
                    "arrow_lower_right", "arrow_lower_left", "arrow_upper_left", "arrow_up_down",
                    "left_right_arrow", "arrows_counterclockwise", "arrow_right_hook", "leftwards_arrow_with_hook",
                    "arrow_heading_up", "arrow_heading_down", "twisted_rightwards_arrows", "repeat",
                    "repeat_one", "zero", "one", "two", "three", "four", "five", "six",
                    "seven", "eight", "nine", "ten", "capital_abcd", "abcd", "symbols",
                    "abc", "a", "ab", "b", "cl", "cool", "free", "information_source",
                    "id", "m", "new", "ng", "o2", "ok", "parking", "sos", "up", "vs",
                    "koko", "sa", "u6708", "u6709", "u6307", "ideograph_advantage", "u5272",
                    "u7121", "u7981", "accept", "u7533", "u5408", "u7a7a", "congratulations",
                    "secret", "u55b6", "u6e80", "red_circle", "large_orange_circle",
                    "large_yellow_circle", "large_green_circle", "large_blue_circle",
                    "large_purple_circle", "large_brown_circle", "black_circle", "white_circle",
                    "red_square", "orange_square", "yellow_square", "green_square", "blue_square",
                    "purple_square", "brown_square", "black_large_square", "white_large_square",
                    "black_medium_square", "white_medium_square", "black_medium_small_square",
                    "white_medium_small_square", "black_small_square", "white_small_square",
                    "large_orange_diamond", "large_blue_diamond", "small_orange_diamond",
                    "small_blue_diamond", "small_red_triangle", "small_red_triangle_down",
                    "diamond_shape_with_a_dot_inside", "radio_button", "white_square_button",
                    "black_square_button", "checkered_flag", "triangular_flag_on_post",
                    "crossed_flags", "flag_white", "flag_black", "rainbow_flag", "male_sign",
                    "female_sign", "heavy_multiplication_x", "heavy_plus_sign", "heavy_minus_sign",
                    "heavy_division_sign", "infinity", "bangbang", "interrobang", "question",
                    "grey_question", "grey_exclamation", "exclamation", "wavy_dash", "currency_exchange",
                    "heavy_dollar_sign", "recycle", "fleur_de_lis", "trident", "name_badge",
                    "beginner", "o", "white_check_mark", "ballot_box_with_check", "heavy_check_mark",
                    "x", "negative_squared_cross_mark", "curly_loop", "loop", "part_alternation_mark",
                    "eight_spoked_asterisk", "eight_pointed_black_star", "sparkle", "copyright",
                    "registered", "tm", "hash", "keycap_star", "asterisk", "eject", "arrow_forward",
                    "pause_button", "play_or_pause_button", "stop_button", "record_button",
                    "track_next", "track_previous", "fast_forward", "rewind", "twisted_rightwards_arrows",
                    "repeat", "repeat_one", "arrow_backward", "arrow_up_small", "arrow_down_small",
                    "arrow_double_up", "arrow_double_down", "arrow_right", "arrow_left",
                    "arrow_up", "arrow_down", "arrow_upper_right", "arrow_lower_right",
                    "arrow_lower_left", "arrow_upper_left", "arrow_up_down", "left_right_arrow",
                    "arrows_counterclockwise", "arrow_right_hook", "leftwards_arrow_with_hook",
                    "arrow_heading_up", "arrow_heading_down", "arrows_clockwise", "back",
                    "end", "on", "soon", "top", "place_of_worship", "atom_symbol", "om_symbol",
                    "star_of_david", "wheel_of_dharma", "yin_yang", "latin_cross", "orthodox_cross",
                    "star_and_crescent", "peace_symbol", "menorah_with_nine_branches", "six_pointed_star",
                    "aries", "taurus", "gemini", "cancer", "leo", "virgo", "libra", "scorpius",
                    "sagittarius", "capricorn", "aquarius", "pisces", "ophiuchus", "star",
                    "star2", "dizzy", "sparkles", "comet", "sunny", "white_sun_small_cloud",
                    "partly_sunny", "white_sun_cloud", "white_sun_rain_cloud", "cloud",
                    "cloud_rain", "thunder_cloud_rain", "cloud_lightning", "zap", "fire",
                    "boom", "snowflake", "cloud_snow", "snowman2", "snowman", "wind_blowing_face",
                    "dash", "cloud_tornado", "fog", "umbrella2", "umbrella", "droplet",
                    "sweat_drops", "ocean"
                );
                break;
        }

        return shortcodes;
    }

    /**
     * Helper to add shortcodes to set
     */
    private static void addShortcodes(Set<String> set, String... shortcodes) {
        for (String shortcode : shortcodes) {
            set.add(shortcode.toLowerCase());
        }
    }
}
