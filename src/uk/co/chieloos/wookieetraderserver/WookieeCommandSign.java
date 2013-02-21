package uk.co.chieloos.wookieetraderserver;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

class WookieeCommandSign implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        //player.sendMessage("You placed "+event.getBlock().getType());
        if (event.isCancelled()) {
            return;
        }

        if (event.getBlock().getType() == Material.SIGN_POST) {
            //event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        //player.sendMessage("You changed the sign. "+event.getLine(0));
        if ("[WTrader]".equals(event.getLine(0))) {
            //event.setCancelled(true);
            player.sendMessage("You aren't allowed to make that sign.");

        }

    }
}