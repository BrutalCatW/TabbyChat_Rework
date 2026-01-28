package acs.tabbychat.threads;

import acs.tabbychat.core.TabbyChat;
import acs.tabbychat.util.TabbyChatUtils;
import net.minecraft.client.resources.I18n;

public class BackgroundUpdateCheck extends Thread {

    @Override
    public void run() {
        // Update check disabled for custom emoji build
        // This prevents "update available" messages for modified TabbyChat versions
    }
}
