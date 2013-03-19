package uk.co.chieloos.wookieetraderserver;

import java.util.logging.Level;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.chieloos.wookieetraderserver.economy.WookieeEcon;

public final class WookieeTrader extends JavaPlugin {

    public final WookieeTrader plugin = this;
    public final WookieeConfig wcfg = new WookieeConfig(this);
    public final WookieeEcon wecon = new WookieeEcon(this);
    public final WookieePerm wperm = new WookieePerm(this);
    public final WookieeDatabase wdb = new WookieeDatabase(this, wecon);
    public final WookieeWorldGuard wwg = new WookieeWorldGuard(this, wcfg);
    protected static final String SETTINGS_VERSION = "0.1a";
    protected PluginManager manager;
    protected PluginDescriptionFile pdf;

    @Override
    public void onEnable() {
        if (getConfig().get("disabled").equals("true")) {
            getLogger().log(Level.SEVERE, "WookieeTraderServer has been disabled in the WookieTraderServer/config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("WookieeItemNames") == null || getServer().getPluginManager().getPlugin("SQLibrary") == null) {
            getLogger().log(Level.SEVERE, "Required dependency not found. Disabling {0}", pdf.getName());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        pdf = this.getDescription();
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
        manager.registerEvents(new WookieeChestListener(this, wdb, wperm), this);
        getCommand("wt").setExecutor(new WookieeCommandExecutor(this, wdb, wecon, wcfg, wperm, wwg));
        getCommand("wt-admin").setExecutor(new WookieeAdminCE(this, wdb, wecon, wcfg, wperm));
        wdb.sqlConnection();
        getLogger().log(Level.INFO, "Enabled {0} v{1}", new Object[]{pdf.getName(), pdf.getVersion()});
    }

    @Override
    public void onDisable() {
    }
}
