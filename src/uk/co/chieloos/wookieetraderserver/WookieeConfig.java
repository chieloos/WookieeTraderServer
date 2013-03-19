package uk.co.chieloos.wookieetraderserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;

public class WookieeConfig {

    public WookieeTrader plugin;
    public Map<String, String> COMMANDS;
    protected FileConfiguration wcfg;
    private String version;
    private List<String> cmdlist = new ArrayList();

    public WookieeConfig(WookieeTrader plugin) {
        cmdlist.add("search");
        cmdlist.add("buy");
        cmdlist.add("order");
        cmdlist.add("confirm");
        cmdlist.add("mailbox");
        cmdlist.add("sell");
        cmdlist.add("cancel");
        cmdlist.add("page");
        cmdlist.add("help");
        this.plugin = plugin;
    }

    protected void loadConfig() {
        String cmdhelp;
        String cmdname;
        COMMANDS = new HashMap();

        File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (!file.exists()) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveConfig();
        }
        version = plugin.getConfig().getString("ver");
        if (version == null || !version.equals(WookieeTrader.SETTINGS_VERSION)) {
            plugin.getLogger().warning("config.yml out of date. Delete it then use '/wtadmin reload' to fix.");
        }
        wcfg = plugin.getConfig();
        boolean error = false;

        for (int i = 0; i < cmdlist.size(); i++) {
            cmdhelp = wcfg.getString("commands." + cmdlist.get(i) + ".help-message");
            cmdname = wcfg.getString("commands." + cmdlist.get(i) + ".command-name");
            if (cmdname != null && cmdhelp != null) {
                COMMANDS.put(cmdname, cmdhelp);
            } else {
                error = true;
                plugin.getLogger().info("Couldn't find:" + cmdlist.get(i));
            }
        }

        if (error) {
            plugin.getLogger().warning("config.yml is missing information. Delete it then use '/wtadmin reload' to fix.");
        }
    }

    public void getConfig() {
    }
}
