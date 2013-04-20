package uk.co.chieloos.wookieetraderserver.economy;

import java.text.DecimalFormat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import uk.co.chieloos.wookieetraderserver.WookieeTrader;

public class WookieeEcon {

    protected static Economy econ = null;
    private WookieeTrader plugin;
    private uk.co.chieloos.wookieetraderserver.economy.Economy wookecon;

    public WookieeEcon(WookieeTrader plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
//            plugin.getLogger().info("vault == null");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
//            plugin.getLogger().info("rsp == null");
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void setupWookonomy() {
        //wookecon = new Economy_Wookonomy(plugin);
    }

    public boolean giveMoney(String name, int amount) {
        if (amount != 0) {
            //double value = (double) amount / 100;
            double d = (double) amount / 100;
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            double value = Double.valueOf(twoDForm.format(d));
            if (amount < 0) {
                if (econ != null) {
                    if (econ.has(name, Math.abs(value))) {
                        econ.withdrawPlayer(name, Math.abs(value));
                        return true;
                    }
                } else {
                    if (wookecon.has(name, Math.abs(value))) {
                        wookecon.withdrawPlayer(name, Math.abs(value));
                        return true;
                    }
                }
            } else {
                if (econ != null) {
                    econ.depositPlayer(name, value);
                    econ.getBalance(name);
                    return true;
                } else {
                    wookecon.depositPlayer(name, value);
                    wookecon.getBalance(name);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAccount(String name) {
        if (econ.hasAccount(name)) {
            return true;
        }
        return false;
    }
}
