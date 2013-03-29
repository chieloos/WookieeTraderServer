package uk.co.chieloos.wookieetraderserver;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

public class WookieeCommandSign implements Listener {

    private Plugin plugin;
    private WookieePerm wperm;

    public WookieeCommandSign(Plugin plugin, WookieePerm wperm) {
        this.plugin = plugin;
        this.wperm = wperm;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
            Sign sign = (Sign) event.getBlock().getState();
            if ("[WTrader]".equals(sign.getLine(0)) && !wperm.playerHasPermission(player, "WookieeTraderServer.sign.modify")) {
                if (!event.isCancelled()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You don't have permission to break that sign.");
                }
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if ("[WTrader]".equals(event.getLine(0)) && !wperm.playerHasPermission(player, "WookieeTraderServer.sign.modify")) {
            if (!event.isCancelled()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to make that sign.");
            }
        }
    }
}