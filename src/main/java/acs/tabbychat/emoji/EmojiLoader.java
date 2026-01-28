package acs.tabbychat.emoji;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles loading emoji PNG images from Twemoji CDN and creating texture atlas
 */
public class EmojiLoader {
    private static final String EMOJI_CACHE_DIR = "tabbychat/emoji_cache/";
    private static final int EMOJI_SIZE = 72; // Size of each emoji in atlas

    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final HashMap<Emoji, BufferedImage> loadedImages = new HashMap<>();
    private SimpleTextureAtlas atlas;
    private boolean isLoading = false;
    private int loadedCount = 0;
    private int totalCount = 0;

    public EmojiLoader() {
        // Create cache directory
        File cacheDir = new File(EMOJI_CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    /**
     * Start loading all emoji images asynchronously
     */
    public CompletableFuture<Void> loadAllEmoji() {
        if (isLoading) {
            System.out.println("[TabbyChat/Emoji] Already loading emoji...");
            return CompletableFuture.completedFuture(null);
        }

        isLoading = true;
        loadedCount = 0;
        totalCount = EmojiRegistry.getAllEmoji().size();

        System.out.println("[TabbyChat/Emoji] Starting to load " + totalCount + " emoji images from Twemoji CDN...");

        // Calculate atlas size
        int gridSize = (int) Math.ceil(Math.sqrt(totalCount));
        int atlasSize = gridSize * EMOJI_SIZE;
        atlas = new SimpleTextureAtlas(atlasSize, atlasSize, EMOJI_SIZE, EMOJI_SIZE);

        // Load all emoji in parallel
        CompletableFuture<?>[] futures = new CompletableFuture[totalCount];
        int index = 0;

        for (Emoji emoji : EmojiRegistry.getAllEmoji()) {
            futures[index++] = CompletableFuture.runAsync(() -> loadEmoji(emoji), executor);
        }

        // Wait for all to complete, then build atlas
        return CompletableFuture.allOf(futures).thenRun(() -> {
            buildAtlas();
            isLoading = false;
            System.out.println("[TabbyChat/Emoji] Finished loading " + loadedCount + "/" + totalCount + " emoji");
        });
    }

    /**
     * Load a single emoji image
     */
    private void loadEmoji(Emoji emoji) {
        try {
            BufferedImage image = loadEmojiImage(emoji);
            if (image != null) {
                synchronized (loadedImages) {
                    loadedImages.put(emoji, image);
                    loadedCount++;

                    if (loadedCount % 100 == 0) {
                        System.out.println("[TabbyChat/Emoji] Loaded " + loadedCount + "/" + totalCount + " emoji...");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[TabbyChat/Emoji] Failed to load emoji " + emoji.getShortcode() + ": " + e.getMessage());
        }
    }

    /**
     * Load emoji image from cache or download from Twemoji CDN
     */
    private BufferedImage loadEmojiImage(Emoji emoji) {
        String cacheFilename = Integer.toHexString(emoji.getUnicode()).toLowerCase() + ".png";
        File cacheFile = new File(EMOJI_CACHE_DIR + cacheFilename);

        // Try cache first
        if (cacheFile.exists()) {
            try {
                return ImageIO.read(cacheFile);
            } catch (Exception e) {
                System.err.println("[TabbyChat/Emoji] Failed to read cached emoji " + emoji.getShortcode() + ": " + e.getMessage());
                // Delete corrupted cache file
                cacheFile.delete();
            }
        }

        // Download from Twemoji CDN
        try {
            String urlString = emoji.getTwemojiUrl();
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "TabbyChat-EmojiLoader/1.0");

            if (connection.getResponseCode() == 200) {
                InputStream input = connection.getInputStream();
                BufferedImage image = ImageIO.read(input);
                input.close();

                // Save to cache
                if (image != null) {
                    try {
                        ImageIO.write(image, "png", cacheFile);
                    } catch (Exception e) {
                        System.err.println("[TabbyChat/Emoji] Failed to cache emoji " + emoji.getShortcode() + ": " + e.getMessage());
                    }
                }

                return image;
            } else {
                System.err.println("[TabbyChat/Emoji] Failed to download " + emoji.getShortcode() + ": HTTP " + connection.getResponseCode());
            }
        } catch (Exception e) {
            System.err.println("[TabbyChat/Emoji] Failed to download " + emoji.getShortcode() + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Build texture atlas from loaded images
     */
    private void buildAtlas() {
        System.out.println("[TabbyChat/Emoji] Building texture atlas from " + loadedImages.size() + " images...");

        synchronized (loadedImages) {
            for (Emoji emoji : loadedImages.keySet()) {
                BufferedImage image = loadedImages.get(emoji);
                if (image != null) {
                    atlas.addImage(emoji, image);
                }
            }
        }

        System.out.println("[TabbyChat/Emoji] Texture atlas built successfully");
    }

    /**
     * Get the texture atlas
     */
    public SimpleTextureAtlas getAtlas() {
        return atlas;
    }

    /**
     * Check if loading is complete
     * Note: Texture will be uploaded to OpenGL on first render (lazy loading)
     */
    public boolean isLoaded() {
        return !isLoading && atlas != null;
    }

    /**
     * Get loading progress (0.0 to 1.0)
     */
    public float getProgress() {
        if (totalCount == 0) return 0.0f;
        return (float) loadedCount / totalCount;
    }

    /**
     * Shutdown executor
     */
    public void shutdown() {
        executor.shutdown();
    }
}
