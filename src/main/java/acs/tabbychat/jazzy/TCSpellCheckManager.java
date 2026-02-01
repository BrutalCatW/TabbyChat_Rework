package acs.tabbychat.jazzy;

import acs.tabbychat.core.TabbyChat;
import acs.tabbychat.emoji.EmojiManager;
import acs.tabbychat.emoji.EmojiRegistry;
import acs.tabbychat.gui.ITCSettingsGUI;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TCSpellCheckManager {
    private static TCSpellCheckManager instance = null;
    private final ReentrantReadWriteLock errorLock = new ReentrantReadWriteLock();
    private final Lock errorReadLock = errorLock.readLock();
    private final Lock errorWriteLock = errorLock.writeLock();
    private final HashMap<Integer, String> errorCache = new HashMap<>();
    private TCSpellCheckListener listener;
    private String lastAttemptedLocale;

    private TCSpellCheckManager() {
        this.reloadDictionaries();
    }

    public static TCSpellCheckManager getInstance() {
        if (instance == null) {
            instance = new TCSpellCheckManager();
        }
        return instance;
    }

    /**
     * Add word to ignore list
     */
    public void addToIgnoredWords(String word) {
        if (!listener.spellCheck.isIgnored(word))
            listener.spellCheck.ignoreAll(word);
    }

    public void drawErrors(GuiScreen screen, List<GuiTextField> inputFields) {
        List<String> inputCache = new ArrayList<>();
        List<GuiTextField> visibleFields = new ArrayList<>();
        int activeFields = 0;
        for (GuiTextField field : inputFields) {
            if (field.getVisible()) {
                activeFields++;
                visibleFields.add(field);
            }
            inputCache.add(field.getText());
        }
        if (activeFields == 0)
            return;

        errorReadLock.lock();
        try {
            Iterator<Map.Entry<Integer, String>> errors = errorCache.entrySet().iterator();
            ListIterator<String> inputs;
            Map.Entry<Integer, String> error;

            while (errors.hasNext()) {
                error = errors.next();
                inputs = inputCache.listIterator(activeFields);
                if (!inputs.hasPrevious())
                    break;
                String input = inputs.previous();
                if (input.length() == 0)
                    break;

                // Use actual field position instead of calculating from screen bottom
                int currentFieldIndex = activeFields - 1;
                GuiTextField currentField = visibleFields.get(currentFieldIndex);
                // Position underline below the text (field height is 16px, text starts at +0, text height is 9)
                int y = currentField.yPosition + 9;  // 0 + 9 = 9 (text start + text height)
                int x = currentField.xPosition + 2;  // Text has 2px left padding
                int width;
                int wordIndex = error.getKey();
                int errLength = error.getValue().length();

                while (wordIndex >= input.length()) {
                    wordIndex -= input.length();
                    currentFieldIndex++;
                    if (!inputs.hasPrevious() || currentFieldIndex >= visibleFields.size()) {
                        return;
                    }
                    input = inputs.previous();
                    currentField = visibleFields.get(currentFieldIndex);
                    y = currentField.yPosition + 9;
                    x = currentField.xPosition + 2;
                }

                if (wordIndex + errLength > input.length()) {
                    // Misspelled word spans line break
                    String beforeWord = EmojiRegistry.convertUnicodeToMarkers(input.substring(0, wordIndex));
                    String fromWord = EmojiRegistry.convertUnicodeToMarkers(input.substring(wordIndex));
                    x += EmojiManager.getInstance().getTextWidth(beforeWord);
                    width = EmojiManager.getInstance().getTextWidth(fromWord);
                    this.drawUnderline(screen, currentField, x, y, width);

                    if (inputs.hasPrevious()) {
                        int remainder = errLength - input.length() + wordIndex;
                        input = inputs.previous();
                        if (input.length() == 0)
                            continue;
                        else if (remainder > input.length())
                            return;
                        currentFieldIndex++;
                        if (currentFieldIndex >= visibleFields.size())
                            return;
                        currentField = visibleFields.get(currentFieldIndex);
                        y = currentField.yPosition + 9;
                        x = currentField.xPosition + 2;
                        String remainderText = EmojiRegistry.convertUnicodeToMarkers(input.substring(0, remainder));
                        width = EmojiManager.getInstance().getTextWidth(remainderText);
                    }
                }
                else {
                    String beforeWord = EmojiRegistry.convertUnicodeToMarkers(input.substring(0, wordIndex));
                    String errorWord = EmojiRegistry.convertUnicodeToMarkers(error.getValue());
                    x += EmojiManager.getInstance().getTextWidth(beforeWord);
                    width = EmojiManager.getInstance().getTextWidth(errorWord);
                }

                this.drawUnderline(screen, currentField, x, y, width);
            }
        }
        finally {
            errorReadLock.unlock();
        }
    }

    /**
     * Marks word as misspelled
     */
    private void drawUnderline(GuiScreen screen, GuiTextField field, int x, int y, int width) {
        // Enable scissor test to clip underline within field bounds
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        // Calculate scissor box in screen pixels (from bottom-left)
        int scissorX = field.xPosition * scale;
        int scissorY = mc.displayHeight - (field.yPosition + field.height) * scale;
        int scissorWidth = field.width * scale;
        int scissorHeight = field.height * scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

        // Draw underline dots
        int next = x + 1;
        while (next - x < width) {
            Gui.drawRect(next - 1, y, next, y + 1, 0xaaff0000);
            next += 2;
        }

        // Disable scissor test
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void handleListenerEvent(SpellCheckEvent event) {
        errorWriteLock.lock();
        try {
            errorCache.put(event.getWordContextPosition(), event.getInvalidWord());
        }
        finally {
            errorWriteLock.unlock();
        }
    }

    /**
     * Loads dictionary
     */
    public boolean loadLocaleDictionary() {
        File localeDict = new File(ITCSettingsGUI.tabbyChatDir,
                                   Minecraft.getMinecraft().gameSettings.language + ".dic");
        if (localeDict.canRead()) {
            listener = new TCSpellCheckListener(localeDict);
            return true;
        }
        else
            return false;
    }

    /**
     * Load user dictionary
     */
    public void loadUserDictionary() {
        File userDict = new File(ITCSettingsGUI.tabbyChatDir, "user.dic");
        BufferedReader in = null;
        if (userDict.canRead()) {
            try {
                in = new BufferedReader(new FileReader(userDict));
                String word;
                while ((word = in.readLine()) != null) {
                    listener.spellCheck.ignoreAll(word);
                }
            }
            catch (Exception e) {
                TabbyChat.printException("Unable to load user dictionary for spell checking", e);
            }
            finally {
                try {
                    if (in != null)
                        in.close();
                }
                catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Reloads dictionaries
     */
    public void reloadDictionaries() {
        if (!this.loadLocaleDictionary())
            listener = new TCSpellCheckListener();
        lastAttemptedLocale = Minecraft.getMinecraft().gameSettings.language;
        this.loadUserDictionary();
    }

    public void update(List<GuiTextField> inputFields) {
        if (!Objects.equals(lastAttemptedLocale, Minecraft.getMinecraft().gameSettings.language))
            this.reloadDictionaries();
        // Clear stored error words and locations
        errorWriteLock.lock();
        try {
            errorCache.clear();
        }
        finally {
            errorWriteLock.unlock();
        }
        // Clear and re-populate contents of input fields, initiate spell
        // checker
        StringBuilder inputCache = new StringBuilder();
        for (GuiTextField inputField : inputFields) {
            if (inputField.getVisible()) {
                inputCache.insert(0, inputField.getText());
            }
        }
        listener.checkSpelling(inputCache.toString());
    }

    @SuppressWarnings("unchecked")
    public List<String> getSuggestions(String word, int threshold) {
        return this.listener.spellCheck.getSuggestions(word, threshold);
    }

    public boolean isSpelledCorrectly(String word) {
        return this.listener.spellCheck.checkSpelling(new StringWordTokenizer(word)) == SpellChecker.SPELLCHECK_OK;
    }
}
