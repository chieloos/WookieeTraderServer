package uk.co.chieloos.wookieetraderserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WookieeChestListener implements Listener {

    private WookieeTrader plugin;
    private WookieeDatabase wdb;

    public WookieeChestListener(WookieeTrader plugin, WookieeDatabase wdb) {
        this.plugin = plugin;
        this.wdb = wdb;
    }

    private ItemStack[] splitIntoStacks(ItemStack item, int amount) {
        final int maxSize = item.getMaxStackSize();
        final int remainder = amount % maxSize;
        final int fullStacks = (int) Math.floor(amount / item.getMaxStackSize());

        ItemStack fullStack = item.clone();
        ItemStack finalStack = item.clone();
        fullStack.setAmount(maxSize);
        finalStack.setAmount(remainder);
        ItemStack[] items;
        if(remainder != 0){
            items = new ItemStack[fullStacks + 1];
        } else {
            items = new ItemStack[fullStacks];
        }
        for (int i = 0; i < fullStacks; i++) {
            items[i] = fullStack;
        }
        if(remainder != 0){
            items[items.length - 1] = finalStack;
        }
        return items;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChestOpen(PlayerInteractEvent interact) {
        if (interact.getClickedBlock() != null) {
            Block block = interact.getClickedBlock();
            if ((block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) && interact.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Sign sign = (Sign) (block.getState());
                String[] text = sign.getLines();
                if ("[WTrader]".equals(text[0]) && "Mailbox".equals(text[2])) {
                    Player player = interact.getPlayer();
                    String name = player.getName();
                    ArrayList<List<String>> chestcontents = wdb.sqlChest(name);
                    Inventory chest = Bukkit.createInventory(null, 9, name + " - Mailbox");
                    if (!chestcontents.isEmpty()) {
                        int i = 0;
                        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                        while (chestcontents.size() > i) {
                            ItemStack item = new ItemStack(Integer.parseInt(chestcontents.get(i).get(1)), 1);
                            if (!chestcontents.get(i).get(4).equals("false")) {
                                String enchstring = chestcontents.get(i).get(4);
                                String[] encharr = enchstring.split(" ");
                                String[] eachench;
                                enchantments.clear();
                                int count = encharr.length;
                                //plugin.getLogger().info("" + count);
                                int l = 0;
                                while (count > l) {
                                    eachench = encharr[l].split("-");
                                    enchantments.put(Enchantment.getByName(eachench[0]), Integer.parseInt(eachench[1]));
                                    l++;
                                }
                                item.addEnchantments(enchantments);
                            }
                            item.setDurability(Short.parseShort(chestcontents.get(i).get(5)));
                            ItemStack[] items = splitIntoStacks(item, Integer.parseInt(chestcontents.get(i).get(3)));
                            chest.addItem(items);
                            i++;
                        }
                    } else {
                        player.sendMessage("Mailbox empty.");
                        return;
                    }
                    player.openInventory(chest);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChestRemoval(InventoryClickEvent event) {
        if (event.getInventory().getName().contains("Mailbox")) {
            if (event.getSlot() != -999 && event.getCurrentItem().getType().toString() != null) {
                if (event.getSlot() < 9 && "CONTAINER".equals(event.getSlotType().toString())) {
                    ItemStack current = event.getCurrentItem();
                    ItemStack cursor = event.getCursor();
                    //plugin.getLogger().info(cursor.getType().toString() + " " + current.getType().toString());
                    if (cursor.getType() != Material.AIR && (current.getType() == Material.AIR || current.getType() == cursor.getType())) {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        p.sendMessage("You can only take items from the mailbox.");
                        //plugin.getLogger().info(event.getCursor().getType().toString());
                        //plugin.getLogger().info(event.getCurrentItem().getType().toString() + ":" + event.getSlot());
                    } else if (current.getType() != Material.AIR) {
                        //plugin.getLogger().info("Called.");
                        int howmany = -1;
                        if (event.isRightClick()) {
                            howmany = current.getAmount();
                            if (howmany != 1) {
                                howmany = (howmany + 1) / 2;
                            }
                        } else {
                            howmany = current.getAmount();
                        }
                        int itemid = -1;
                        itemid = current.getTypeId();
                        String user = "";
                        int durability = current.getDurability();
                        user = event.getInventory().getName().replace(" - Mailbox", "");
                        Map<Enchantment, Integer> enchantments = current.getEnchantments();
                        String enchants = "";
                        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                            Enchantment key = entry.getKey();
                            Integer value = entry.getValue();
                            enchants += key.getName() + "-" + value + " ";
                        }
                        if (enchants.equals("")) {
                            enchants = "false";
                        }

                        //plugin.getLogger().log(Level.INFO, "You took out {0} {1} From {2}''s Mailbox", new Object[]{howmany, itemid, user});
                        //plugin.getLogger().log(Level.INFO, "SELECT * FROM playerchest WHERE player = ''{0}'' AND itemid = ''{1}''", new Object[]{user, itemid});

                        String found = wdb.sqlChestRemove(itemid, howmany, user, enchants, durability);
                        if ("true".equals(found)) {
                            //plugin.getLogger().info("Found in database");
                        } else {
                            //plugin.getLogger().info("Not found in database");
                        }
                    }
                } else if (event.isShiftClick()) {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.sendMessage("You can only take items from the mailbox.");
                }
            }
        }
    }
}
