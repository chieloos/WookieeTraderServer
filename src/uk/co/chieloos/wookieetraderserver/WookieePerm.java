package uk.co.chieloos.wookieetraderserver;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class WookieePerm {

    private Plugin plugin;
    public static Permission perms = null;

    public WookieePerm(Plugin plugin) {
        this.plugin = plugin;
    }

    protected boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            plugin.getLogger().info("rsp == null");
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }

    public boolean playerHasPermission(Player player, String permission) {
        String world = player.getWorld().getName();
        if (perms.getPlayerGroups(player) != null) {
            String[] permgroups = perms.getPlayerGroups(player);
            int grouplength = permgroups.length;
            int i = 0;
            while (grouplength > i) {
                if (perms.groupHas(world, permgroups[i], permission)) {
                    return true;
                }
                i++;
            }
        }
        if(perms.playerHas(player, permission)){
            return true;
        }
        if(perms.has(world, player.getName(), permission)){
            return true;
        }
        return false;
    }
}
