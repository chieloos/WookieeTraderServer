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
        boolean console = false;
        Player player = null;
        if (cmd.getName().equalsIgnoreCase("wt-admin") && args.length > 0) {
            if (!(sender instanceof Player)) {
                console = true;
            } else {
                 player = (Player) sender;
            }
            if (args[0].equalsIgnoreCase("stop") && args.length > 1) {
                if (args[1].equalsIgnoreCase("once")) {
                    if (console || wperm.playerHasPermission(player, "WookieeTraderServer.wt-admin.stop.once")) {
                        sender.sendMessage(ChatColor.RED + "Stopping WookieeTraderServer");
                        plugin.getLogger().log(Level.SEVERE, "{0} has stopped WookieeTraderServer", sender.getName());
                        plugin.getPluginLoader().disablePlugin(plugin);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                        return true;
                    }
                }
                if (args[1].equalsIgnoreCase("permanent")) {
                    if (console || wperm.playerHasPermission(player, "WookieeTraderServer.wt-admin.stop.permanent")) {
                        sender.sendMessage(ChatColor.RED + "Stopping WookieeTraderServer");
                        plugin.getConfig().set("disabled", "true");
                        plugin.saveConfig();
                        plugin.getLogger().log(Level.SEVERE, "{0} has stopped WookieeTraderServer", sender.getName());
                        plugin.getPluginLoader().disablePlugin(plugin);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                        return true;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("clear") && args.length > 1) {
                if (args[1].equalsIgnoreCase("mailbox")) {
                    if (console || wperm.playerHasPermission(player, "WookieeTraderServer.wt-admin.clear-databases")) {
                        wdb.clearMailbox();
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                        return true;
                    }
                }
                if (args[1].equalsIgnoreCase("trades")) {
                    if (console || wperm.playerHasPermission(player, "WookieeTraderServer.wt-admin.clear-databases")) {
                        wdb.clearTrades();
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                        return true;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("help")) {
                if (console || wperm.playerHasPermission(player, "WookieeTraderServer.wt-admin.help")) {
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("stop")) {
                            sender.sendMessage("/wt-admin stop <once|permanent>");
                            sender.sendMessage("Stops the plugin until the next restart or permanently.");
                            return true;
                        }
                        if (args[1].equalsIgnoreCase("clear")) {
                            sender.sendMessage("/wt-admin clear <trades|mailbox>");
                            sender.sendMessage("Clears either the trades or mailbox databases.");
                            return true;
                        }
                    } else {
                        String msg = "Type /wt help <command> for more information.";
                        sender.sendMessage(msg);
                        sender.sendMessage("stop, clear");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
            }
        }
        return false;
    }
}
