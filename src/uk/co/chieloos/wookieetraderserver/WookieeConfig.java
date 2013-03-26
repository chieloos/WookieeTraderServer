package uk.co.chieloos.wookieetraderserver;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

public class WookieeConfig {

    private WookieeTrader plugin;
    protected FileConfiguration wcfg;
    private String version;


    public WookieeConfig(WookieeTrader plugin) {

        this.plugin = plugin;
    }

    protected void loadConfig() {

        File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (!file.exists()) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveConfig();
        }
        version = plugin.getConfig().getString("confver");
        if (version == null || !version.equals(WookieeTrader.CONFIG_VERSION)) {
            plugin.getLogger().warning("config.yml out of date. Delete it then use '/wt-admin reload'");
            plugin.getLogger().warning("Once you've edited config.yml use '/wt-admin reload' again.");
        }
        wcfg = plugin.getConfig();
        
        boolean error = false;
        if (error) {
            plugin.getLogger().warning("config.yml is missing information. Delete it then use '/wt-admin reload'");
            plugin.getLogger().warning("Once you've edited config.yml use '/wt-admin reload' again.");
        }
    }

    public void saveConfig() {
        plugin.saveConfig();
    }
}
