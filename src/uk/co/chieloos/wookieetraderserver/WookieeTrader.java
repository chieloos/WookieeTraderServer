package uk.co.chieloos.wookieetraderserver;

import java.util.logging.Level;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.chieloos.wookieetraderserver.economy.WookieeEcon;

/* 
 * This, being my first self taught java project attempt,
 * may include slow or badly written code, or bad programming practices.
 * These will be updated and/or corrected as I continue to learn.
 * It does however work as intended, aside from the odd bugs that
 * may be introduced during this plugin's lifespan.
 */

public final class WookieeTrader extends JavaPlugin {

    public final WookieeTrader plugin = this;
    public final WookieeConfig wcfg = new WookieeConfig(this);
    public final WookieeEcon wecon = new WookieeEcon(this);
    public final WookieePerm wperm = new WookieePerm(this);
    public final AccessDataBases accessdb = new AccessDataBases(plugin, wcfg);
    public final WookieeDatabase wdb = new WookieeDatabase(this, wecon, accessdb);
    public final WookieeWorldGuard wwg = new WookieeWorldGuard(this, wcfg);
    public final WookieeMailbox wmb = new WookieeMailbox(this, wdb);
    
    protected static final String CONFIG_VERSION = "1.0";
    protected PluginManager manager;
    protected PluginDescriptionFile pdf;

    @Override
    public void onEnable() {
        if (getConfig().get("disabled").equals("true")) {
            getLogger().log(Level.SEVERE, "WookieeTraderServer has been disabled in the WookieTraderServer/config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        pdf = this.getDescription();
        if (getServer().getPluginManager().getPlugin("WookieeItemNames") == null) {
            getLogger().log(Level.SEVERE, "Required dependency not found. Disabling {0}", pdf.getName());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!wecon.setupEconomy()) {
            getLogger().log(Level.SEVERE, "No Vault dependency found. Disabling {0}", pdf.getName());
            getServer().getPluginManager().disablePlugin(this);
            return;
            //wecon.setupWookonomy();
        }
        if (!wperm.setupPermissions()) {
        }
        wwg.getWorldGuard();
        manager = this.getServer().getPluginManager();
        wcfg.loadConfig();
        manager.registerEvents(new WookieeCommandSign(this, wperm), this);
        manager.registerEvents(new WookieeChestListener(this, wdb, wperm, wmb), this);
        getCommand("wt").setExecutor(new WookieeCommandExecutor(this, wdb, wecon, wcfg, wperm, wwg, wmb));
        getCommand("wt-admin").setExecutor(new WookieeAdminCE(this, wdb, wecon, wcfg, wperm));
        loadDatabases();
        accessdb.getCounters();
        startDatabaseSaves();
        getLogger().log(Level.INFO, "Enabled {0} v{1}", new Object[]{pdf.getName(), pdf.getVersion()});
    }

    @Override
    public void onDisable() {
        wdb.saveDatabases();
        saveConfig();
    }
    
    void startDatabaseSaves() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {

            @Override
            public void run() {
                wdb.saveDatabases();
                wcfg.saveConfig();
            }
        }, 6000, 6000);
    }

    void loadDatabases() {
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {

            @Override
            public void run() {
                wdb.loadDatabases();
            }
        });
    }
}
