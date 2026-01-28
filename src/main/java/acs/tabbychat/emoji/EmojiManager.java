package acs.tabbychat.emoji;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.concurrent.CompletableFuture;

/**
 * Central manager for emoji system
 * Coordinates registry, loader, and renderer
 */
public class EmojiManager {
    private static EmojiManager instance;

    private final EmojiLoader loader;
    private final EmojiRenderer renderer;
    private boolean initialized = false;
    private boolean enabled = true;

    private EmojiManager() {
        this.loader = new EmojiLoader();
        this.renderer = new EmojiRenderer(loader);
    }

    /**
     * Get singleton instance
     */
    public static EmojiManager getInstance() {
        if (instance == null) {
            instance = new EmojiManager();
        }
        return instance;
    }

    /**
     * Initialize emoji system
     * Should be called during mod initialization
     */
    public CompletableFuture<Void> init() {
        if (initialized) {
            System.out.println("[TabbyChat/Emoji] Already initialized");
            return CompletableFuture.completedFuture(null);
        }

        System.out.println("[TabbyChat/Emoji] Initializing emoji system...");

        // Initialize registry
        EmojiRegistry.init();

        if (!EmojiRegistry.isInitialized()) {
            System.err.println("[TabbyChat/Emoji] Failed to initialize registry");
            return CompletableFuture.completedFuture(null);
        }

        // Start loading emoji images
        initialized = true;
        return loader.loadAllEmoji().thenRun(() -> {
            System.out.println("[TabbyChat/Emoji] Emoji system initialized successfully");
        });
    }

    /**
     * Process incoming chat message - convert emoji to markers
     * Supports: ASCII aliases (:), <3), Discord shortcodes (:fire:), Unicode emoji (🔥)
     */
    public String processIncomingMessage(String text) {
        if (!enabled || text == null) {
            return text;
        }

        // Apply conversions in order:
        // 1. ASCII aliases first (to avoid :) being parsed as shortcode start)
        text = EmojiRegistry.convertAsciiToPUA(text);

        // 2. Discord shortcodes (:grinning:, :fire:, etc.)
        text = EmojiRegistry.convertShortcodesToPUA(text);

        // 3. Unicode emoji (😀, 🔥, ❤️)
        text = EmojiRegistry.convertUnicodeToMarkers(text);

        return text;
    }

    /**
     * Render text with emoji support
     */
    public int renderText(String text, int x, int y, int color, boolean shadow) {
        if (!enabled || !initialized || !isReady()) {
            // Fallback to normal rendering without emoji (PUA chars will show as empty boxes)
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            if (shadow) {
                return fr.drawStringWithShadow(text, x, y, color);
            } else {
                return fr.drawString(text, x, y, color);
            }
        }

        return renderer.drawStringWithEmoji(text, x, y, color, shadow);
    }

    /**
     * Get width of text with emoji
     */
    public int getTextWidth(String text) {
        if (!enabled || !initialized) {
            return 0;
        }

        return renderer.getStringWidth(text);
    }

    /**
     * Split text by width with emoji support
     */
    public java.util.List<String> splitTextByWidth(String text, int maxWidth) {
        if (!enabled || !initialized) {
            return new java.util.ArrayList<>();
        }

        return renderer.splitByWidth(text, maxWidth);
    }

    /**
     * Trim text to max width with emoji support
     */
    public String trimTextToWidth(String text, int maxWidth) {
        if (!enabled || !initialized) {
            return text;
        }

        return renderer.trimToWidth(text, maxWidth);
    }

    /**
     * Check if emoji system is ready for rendering
     */
    public boolean isReady() {
        return enabled && initialized && renderer.isReady();
    }

    /**
     * Get loading progress (0.0 to 1.0)
     */
    public float getProgress() {
        if (!initialized) return 0.0f;
        return renderer.getProgress();
    }

    /**
     * Enable/disable emoji rendering
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Check if emoji rendering is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Shutdown emoji system
     */
    public void shutdown() {
        if (loader != null) {
            loader.shutdown();
        }
        initialized = false;
    }
}
