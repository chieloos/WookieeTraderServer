package uk.co.chieloos.wookieetraderserver;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class WookieeChestListener implements Listener {

    private WookieeTrader plugin;
    private WookieeDatabase wdb;
    private WookieePerm wperm;

    public WookieeChestListener(WookieeTrader plugin, WookieeDatabase wdb, WookieePerm wperm) {
        this.plugin = plugin;
        this.wdb = wdb;
        this.wperm = wperm;
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
        if (remainder != 0) {
            items = new ItemStack[fullStacks + 1];
        } else {
            items = new ItemStack[fullStacks];
        }
        for (int i = 0; i < fullStacks; i++) {
            items[i] = fullStack;
        }
        if (remainder != 0) {
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
                    if (!wperm.playerHasPermission(player, "WookieeTraderServer.sign.use")) {
                        player.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                        return;
                    }
                    String name = player.getName();
                    ArrayList<WDBEntry> mailboxcontents = wdb.getMailbox(name);
                    Inventory chest = Bukkit.createInventory(null, 27, name + " - Mailbox");
                    if (!mailboxcontents.isEmpty()) {
                        int i = 0;
                        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                        while (mailboxcontents.size() > i) {
                            ItemStack item = new ItemStack(mailboxcontents.get(i).getItemID(), 1);
                            boolean enchbook = false;
                            EnchantmentStorageMeta enchmeta = null;
                            ItemMeta itemmeta = null;
                            if (item.getType().equals(Material.ENCHANTED_BOOK)) {
                                enchbook = true;
                                enchmeta = (EnchantmentStorageMeta) item.getItemMeta();
                            }
                            if (!mailboxcontents.get(i).getEnchants().equals("false")) {
                                String enchstring = mailboxcontents.get(i).getEnchants();
                                String[] encharr = enchstring.split(" ");
                                String[] eachench;
                                enchantments.clear();
                                int count = encharr.length;
                                //plugin.getLogger().info("" + count);
                                int l = 0;
                                while (count > l) {
                                    eachench = encharr[l].split("-");
                                    if (enchbook) {
                                        enchmeta.addStoredEnchant(Enchantment.getByName(eachench[0]), Integer.parseInt(eachench[1]), true);
                                    } else {
                                        enchantments.put(Enchantment.getByName(eachench[0]), Integer.parseInt(eachench[1]));
                                    }
                                    l++;
                                }
                                if(!enchbook){
                                    item.addEnchantments(enchantments);
                                } else {
                                    item.setItemMeta(enchmeta);
                                }
                            }
                            if(!mailboxcontents.get(i).getCustomName().equals("false")){
                                itemmeta = item.getItemMeta();
                                itemmeta.setDisplayName(mailboxcontents.get(i).getCustomName());
                                item.setItemMeta(itemmeta);
                            }
                            item.setDurability((short) mailboxcontents.get(i).getDurability());
                            ItemStack[] items = splitIntoStacks(item, mailboxcontents.get(i).getAmount());
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
                        String customname;
                        int durability = current.getDurability();
                        user = event.getInventory().getName().replace(" - Mailbox", "");
                        Map<Enchantment, Integer> enchantmap;
                        if (current.getType().equals(Material.ENCHANTED_BOOK)) {
                            EnchantmentStorageMeta enchmeta = (EnchantmentStorageMeta) current.getItemMeta();
                            enchantmap = enchmeta.getStoredEnchants();
                        } else {
                            enchantmap = current.getEnchantments();
                        }
                        Map<String, Integer> enchantments = new HashMap();
                        String enchants = "";
                        for (Map.Entry<Enchantment, Integer> entry : enchantmap.entrySet()) {
                            Enchantment key = entry.getKey();
                            Integer value = entry.getValue();
                            enchantments.put(key.getName(), value);
                        }
                        SortedSet<String> keys = new TreeSet<String>(enchantments.keySet());
                        for (String key : keys) {
                            int value = enchantments.get(key);
                            enchants += key + "-" + value + " ";
                        }
                        if (enchants.equals("")) {
                            enchants = "false";
                        }
                        if (current.getItemMeta().hasDisplayName()) {
                            customname = current.getItemMeta().getDisplayName();
                        } else {
                            customname = "false";
                        }
                        //plugin.getLogger().log(Level.INFO, "You took out {0} {1} From {2}''s Mailbox", new Object[]{howmany, itemid, user});
                        //plugin.getLogger().log(Level.INFO, "SELECT * FROM playerchest WHERE player = ''{0}'' AND itemid = ''{1}''", new Object[]{user, itemid});
                        WDBEntry wde = new WDBEntry(customname, enchants, durability, 0, "", 0, itemid, 0);
                        boolean found;
                        found = wdb.removeFromMailbox(itemid, howmany, user, enchants, durability, customname);
                        if (found) {
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