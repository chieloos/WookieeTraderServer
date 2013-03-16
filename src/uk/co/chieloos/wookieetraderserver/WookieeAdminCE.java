package uk.co.chieloos.wookieetraderserver;

import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.chieloos.wookieetraderserver.economy.WookieeEcon;

public class WookieeAdminCE implements CommandExecutor {

    private WookieeTrader plugin;
    private WookieeDatabase wdb;
    private WookieeEcon wecon;
    private WookieeConfig wcfg;
    private WookieePerm wperm;

    public WookieeAdminCE(WookieeTrader plugin, WookieeDatabase wdb, WookieeEcon wecon, WookieeConfig wcfg, WookieePerm wperm) {
        this.plugin = plugin;
        this.wdb = wdb;
        this.wecon = wecon;
        this.wcfg = wcfg;
        this.wperm = wperm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wt-admin") && args.length > 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
                return true;
            }
            if (args[0].equalsIgnoreCase("stop") && args.length > 1) {
                Player player = (Player) sender;
                if (args[1].equalsIgnoreCase("once") && wperm.playerHasPermission(player, "WookieeTraderServer.wt-admin.stop.once")) {
                    sender.sendMessage(ChatColor.RED + "Stopping WookieeTraderServer");
                    plugin.getLogger().log(Level.SEVERE, "{0} has stopped WookieeTraderServer", sender.getName());
                    plugin.getPluginLoader().disablePlugin(plugin);
                    return true;
                }
                if (args[1].equalsIgnoreCase("permanent") && wperm.playerHasPermission(player, "WookieeTraderServer.wt-admin.stop.permanent")) {
                    sender.sendMessage(ChatColor.RED + "Stopping WookieeTraderServer");
                    plugin.getConfig().set("disabled", "true");
                    plugin.saveConfig();
                    plugin.getLogger().log(Level.SEVERE, "{0} has stopped WookieeTraderServer", sender.getName());
                    plugin.getPluginLoader().disablePlugin(plugin);
                }

                return true;
            }
        }
        return false;
    }
}
