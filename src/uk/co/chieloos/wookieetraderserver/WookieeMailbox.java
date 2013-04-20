package uk.co.chieloos.wookieetraderserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class WookieeMailbox {

    private WookieeTrader plugin;
    private WookieeDatabase wdb;

    public WookieeMailbox(WookieeTrader plugin, WookieeDatabase wdb) {
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

    public void openMailbox(Player player, String name) {
        ArrayList<WDBEntry> mailboxcontents = wdb.searchMailbox(name);
        Inventory chest = Bukkit.createInventory(null, 27, name + " - Mailbox");
        if (mailboxcontents != null) {
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
                    if (!enchbook) {
                        item.addEnchantments(enchantments);
                    } else {
                        item.setItemMeta(enchmeta);
                    }
                }
                if (!mailboxcontents.get(i).getCustomName().equals("false")) {
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
