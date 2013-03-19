package uk.co.chieloos.wookieetraderserver;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WookieeWorldGuard {

    private Plugin plugin;
    private WGBukkit wgb;
    private WookieeConfig wcfg;
    protected boolean wgenabled;

    public WookieeWorldGuard(Plugin _plugin, WookieeConfig _wcfg) {
        plugin = _plugin;
        wcfg = _wcfg;
    }

    protected WorldGuardPlugin getWorldGuard() {
        Plugin wgp = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (wgp == null || !(wgp instanceof WorldGuardPlugin)) {
            wgenabled = false;
            return null; // Maybe you want throw an exception instead
        }
        wgenabled = true;
        return (WorldGuardPlugin) wgp;
    }

    protected boolean inWTRegion(Player player) {
        String regionid = wcfg.wcfg.getString("wg-region-id");
        World world = player.getWorld();
        Location loc = player.getLocation();
        ApplicableRegionSet set = WGBukkit.getRegionManager(world).getApplicableRegions(loc);
        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(regionid)) {
                return true;
            }
        }
        return false;
    }
}
