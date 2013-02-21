package uk.co.chieloos.wookieetraderserver;

import java.util.logging.Level;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class WookieeTrader extends JavaPlugin {

    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    public final WookieeTrader plugin = this;
    public final WookieeDatabase wdb = new WookieeDatabase(this);
    public final WookieeConfig wconf = new WookieeConfig(this);

    @Override
    public void onEnable() {
        
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        //setupChat();
        
        PluginDescriptionFile pdf = this.getDescription();
        getLogger().log(Level.INFO, "Enabled {0} v{1}", new Object[]{pdf.getName(), pdf.getVersion()});
        PluginManager manager = this.getServer().getPluginManager();
        manager.registerEvents(new WookieeCommandSign(), this);
        manager.registerEvents(new WookieeChestListener(this, wdb), this);
        getCommand("wt").setExecutor(new WookieeCommandExecutor(this, wdb));
        wdb.sqlConnection();
        getLogger().info(plugin.getDataFolder().getAbsolutePath());
        getConfig().options().copyDefaults(true);
        getConfig().options().configuration().getString("test");
        saveConfig();
        //getLogger().log(Level.INFO, "test: {0}", getConfig().options().configuration().getString("test"));
    }

    @Override
    public void onDisable() {
        //wdb.sqLite.close();
        getLogger().info("WookieeTrader has been disabled.");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("vault == null");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().info("rsp == null");
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
