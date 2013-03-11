package uk.co.chieloos.wookieetraderserver;

import java.util.*;
import java.util.logging.Level;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import uk.co.chieloos.wookieeitemnames.ItemNames;
import uk.co.chieloos.wookieetraderserver.economy.WookieeEcon;

public class WookieeCommandExecutor implements CommandExecutor {

    private ItemNames win;
    private WookieeTrader plugin;
    private WookieeDatabase wdb;
    private WookieeEcon wecon;
    private WookieeConfig wcfg;
    private WookieePerm wperm;
    public boolean enabled = false;
    HashMap<String, ArrayList> confirmmap = new HashMap<String, ArrayList>();
    HashMap<String, ArrayList> pagemap = new HashMap<String, ArrayList>();
    //ArrayList<String> cmdmap = new ArrayList<String>();
    public ArrayList<String> cmdlist = new ArrayList();
    boolean cmdreturn;
    int switcher;

    public WookieeCommandExecutor(WookieeTrader plugin, WookieeDatabase wdb, WookieeEcon wecon, WookieeConfig wcfg, WookieePerm wperm) {
        this.plugin = plugin;
        this.wdb = wdb;
        this.wecon = wecon;
        this.wcfg = wcfg;
        this.wperm = wperm;
        win = new ItemNames(this.plugin.getLogger());

        cmdlist.add("search");      //0
        cmdlist.add("buy");         //1
        cmdlist.add("order");       //2
        cmdlist.add("confirm");     //3
        cmdlist.add("mailbox");     //4
        cmdlist.add("sell");        //5
        cmdlist.add("cancel");      //6
        cmdlist.add("version");     //7
        cmdlist.add("page");        //8
        cmdlist.add("help");        //9
    }

    public String getItemName(String itemname) {
        if (isInteger(itemname)) {
            String name = getItemName(Integer.parseInt(itemname));
            if (name != null) {
                return name;
            }
        } else {
            Material itemmat = Material.getMaterial(itemname);
            if (itemmat != null) {
                String name = getItemName(itemmat.getId());
                if (name != null) {
//                    plugin.getLogger().info("Found");
                    return name;
                }
            }
        }
        return "";
    }

    public String getItemName(int itemid) {
        String name = null;
        ItemInfo iteminfo;
        iteminfo = Items.itemById(itemid);
        if (iteminfo != null) {
            name = iteminfo.getName();
        }
        if (name == null) {
            Material itemmat = Material.getMaterial(itemid);
            if (itemmat == null) {
                return "";
            } else {
//                plugin.getLogger().info("Found");
                return itemmat.name();
            }
        } else {
//            plugin.getLogger().info("Found");
            return name;
        }
    }

    public String getEnchantName(String enchant) {
        String name;
        name = plugin.getConfig().getString("enchants." + enchant + ".name");
        if (name == null) {
            return "";
        } else {
//            plugin.getLogger().info("Found");
            return name;
        }
    }

    private String formatEnchants(String enchants) {
        String formatted = "";
        if (!enchants.equals("false")) {
            String[] encharr = enchants.split(" ");
            String[] eachench;
            String enchantname = "";
            int count = encharr.length;
            int i = 0;
            while (count > i) {
                if (!encharr[i].equals("false")) {
                    eachench = encharr[i].split("-");
                    if (getEnchantName(eachench[0]) != null) {
                        enchantname = getEnchantName(eachench[0]);
                        if (enchantname.equals("")) {
                            enchantname = eachench[0];
                        }
                    }
                    formatted += enchantname + " " + eachench[1] + ", ";
                }
                i++;

            }
            formatted += "--";
            formatted = formatted.replace(", --", "");

        }
        return formatted;
    }

    private int getPercent(String currentdurability, String itemid) {
        Material item = Material.getMaterial(Integer.parseInt(itemid));
        short precurrent = Short.parseShort(currentdurability);
        short max = item.getMaxDurability();
        int current = max - precurrent;
        if (max != 0) {
            float prepercent = 100 * current / max;
            int percent = (int) Math.floor(prepercent);
            return percent;
        } else {
            return -1;
        }
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int cmdNum(String cmdname) {
        for (int i = 0; i < this.cmdlist.size(); i++) {
            if (cmdname.equalsIgnoreCase(cmdlist.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public void confMap(CommandSender sender, ArrayList<String> arrargs) {
        if (confirmmap.containsKey(sender.getName())) {
            confirmmap.remove(sender.getName());
        }
        ArrayList<String> cmdmap = new ArrayList<String>();
        cmdmap.add("cancel");
        int n = arrargs.size();
        int i = 0;
        while (n > i) {
            cmdmap.add(arrargs.get(i));
            i++;
        }
        confirmmap.put(sender.getName(), cmdmap);
    }

    public void pageMap(CommandSender sender, ArrayList<String> arrargs) {
        if (pagemap.containsKey(sender.getName())) {
            pagemap.remove(sender.getName());
        }
        ArrayList<String> cmdmap = new ArrayList<String>();
        cmdmap.add("search");
        int n = arrargs.size();
        int i = 0;
        while (n > i) {
            cmdmap.add(arrargs.get(i));
            i++;
        }
        pagemap.put(sender.getName(), cmdmap);
    }

    public void helpMsg(String[] msg, CommandSender sender) {
        if (sender instanceof Player) {
            sender.sendMessage(msg[0]);
            sender.sendMessage(msg[1]);
        } else {
            plugin.getLogger().info(msg[0]);
            plugin.getLogger().info(msg[1]);
        }
    }

    public String enchToStr(ItemStack is) {
        Map<Enchantment, Integer> enchantments = is.getEnchantments();
        String enchants = "";
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment key = entry.getKey();
            Integer value = entry.getValue();
            enchants += key.getName() + "-" + value + " ";
        }
        if (enchants.equals("")) {
            enchants = "false";
        }
        return enchants;
    }

    public Map<Enchantment, Integer> strToEnch(String str) {
        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
        String[] encharr = str.split(" ");
        String[] eachench;
        int count = encharr.length;
        int l = 0;
        while (count > l) {
            eachench = encharr[l].split("-");
            enchantments.put(Enchantment.getByName(eachench[0]), Integer.parseInt(eachench[1]));
            l++;
        }

        return enchantments;
    }

    boolean cmdSearch(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed, int page) {
        ArrayList<List<String>> searcharr = new ArrayList();
        int count = 0;
        boolean all = false;
        if (arrargs.size() == 2 && arrargs.get(1).equalsIgnoreCase("all") || arrargs.size() == 1) {
            searcharr = wdb.sqlTradeSearch(-1, "", page);
            count = wdb.sqlTradeCount(-1, "");
            all = true;
        }
        if (arrargs.size() == 3 || all) {
            if (page > 1) {
            }
            if (arrargs.size() > 1 && arrargs.get(1).equalsIgnoreCase("player")) {
                //plugin.getLogger().info("search player");
                searcharr = wdb.sqlTradeSearch(0, arrargs.get(2), page);
                count = wdb.sqlTradeCount(0, arrargs.get(2));
            } else if (arrargs.size() > 1 && arrargs.get(1).equalsIgnoreCase("item")) {
                //plugin.getLogger().info("search item");
                String item = arrargs.get(2);

                if (isInteger(item)) {
                    searcharr = wdb.sqlTradeSearch(Integer.parseInt(arrargs.get(2)), "", page);
                    count = wdb.sqlTradeCount(Integer.parseInt(arrargs.get(2)), "");
                } else {
                    String itemname = arrargs.get(2).toUpperCase();
                    if (Material.getMaterial(itemname) instanceof Material) {
                        int itemid = Material.getMaterial(itemname).getId();
                        searcharr = wdb.sqlTradeSearch(itemid, "", page);
                        count = wdb.sqlTradeCount(itemid, "");
                    } else {
                        sender.sendMessage("Item name not recognised.");
                        return true;
                    }
                }
            } else {
                if (all != true) {
                    sender.sendMessage("Incorrect arguments, type '/wt help search'.");
                    return true;
                }
            }
            if (!searcharr.isEmpty()) {
                //plugin.getLogger().info("searcharr isn't empty");
                int searchsize = searcharr.size();
                int i = 0;
                String itemname;
                int durability;
                String durabilitypercent;
                String enchants;

                while (searchsize > i) {
                    enchants = formatEnchants(searcharr.get(i).get(6));
                    durability = getPercent(searcharr.get(i).get(5), searcharr.get(i).get(1));
                    if (durability != -1) {
                        itemname = win.getItemName(Integer.parseInt(searcharr.get(i).get(1)), 0);
                        if (durability != 100) {
                            durabilitypercent = "Dur: " + ChatColor.RED + durability + ChatColor.RESET + "%" + ", ";
                        } else {
                            durabilitypercent = "Dur: " + ChatColor.GREEN + durability + ChatColor.RESET + "%" + ", ";
                        }
                    } else {
                        itemname = win.getItemName(Integer.parseInt(searcharr.get(i).get(1)), Integer.parseInt(searcharr.get(i).get(5)));
                        durabilitypercent = "";
                    }
                    sender.sendMessage("id " + ChatColor.YELLOW + searcharr.get(i).get(0) + ChatColor.RESET + ": " + searcharr.get(i).get(2) + " " + ChatColor.DARK_GREEN + itemname + ChatColor.RESET + " for " + ChatColor.BLUE + searcharr.get(i).get(3) + ChatColor.RESET + " ea, " + durabilitypercent + "Seller: " + ChatColor.GOLD + searcharr.get(i).get(4));
                    if (!enchants.equals("")) {
                        sender.sendMessage("Enchants: " + enchants);
                    }
                    sender.sendMessage(ChatColor.DARK_GRAY + "----------");
                    //sender.sendMessage();
                    i++;
                }

                double d = (double) count / 5;
                int pages = (int) Math.ceil(d);
                //int pages = (int) Math.ceil(d);
                sender.sendMessage(ChatColor.YELLOW + "" + count + ChatColor.RESET + " trades found. (Page " + page + "/" + pages + ")");
                if (count > (page * 5)) {
//                    plugin.getLogger().info("count was more than 5");
                    pageMap(sender, arrargs);
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.YELLOW + "0" + ChatColor.RESET + " trades found.");
                return true;
            }
        }
        sender.sendMessage("Missing arguements, type '/wt help search'.");
        return true;
    }

    boolean cmdBuy(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        } else {
            if (arrargs.size() != 3) {
                return false;
            }
            if (!isInteger(arrargs.get(2)) || !isInteger(arrargs.get(1))) {
                return false;
            }
            int id = Integer.parseInt(arrargs.get(1));
            String[] itemdb = wdb.sqlBuy(id);
            if (!itemdb[0].equals("false")) {
                int count = Integer.parseInt(itemdb[2]);
                int amount = Integer.parseInt(arrargs.get(2));
                if (amount < 1) {
                    return false;
                }
                if (amount > count) {
                    sender.sendMessage("There aren't that many to buy.");
                    return true;
                }
                int cost = Integer.parseInt(itemdb[3]) * amount;
                int amountdue = Math.abs(cost) * -1;
                if (wecon.giveMoney(sender.getName(), amountdue)) {
                    sender.sendMessage("Cost you: " + cost + " (" + itemdb[3] + "x" + amount + ")");
                    wdb.sqlChestAdd(itemdb, Integer.parseInt(arrargs.get(2)), sender.getName(), false);
                    return true;
                } else {
                    sender.sendMessage("You don't have enough money.");
                    return true;
                }
            } else {
                sender.sendMessage("Item not found.");
                return true;
            }
        }
    }

    boolean cmdOrder(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        sender.sendMessage("Not yet implemented");
//        TODO: Order command
        return false;
    }

    boolean cmdMailbox(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        Player player = (Player) sender;
        if (arrargs.size() == 2 && arrargs.get(1).equalsIgnoreCase("list")) {
            if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.mailbox.list")) {
                ArrayList<String> playerlist = wdb.sqlChestList();
                if (!playerlist.isEmpty()) {
                    int listsize = playerlist.size();
                    int i = 0;
                    String prettylist = "Mailboxes:";
                    while (listsize > i) {
                        prettylist = prettylist + " " + playerlist.get(i);
                        i++;
                    }
                    sender.sendMessage(prettylist);
                    return true;
                }
                sender.sendMessage("No mailboxes found.");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                return true;
            }
        } else if (arrargs.size() > 0) {
            String playerchest;
            if (arrargs.size() == 1) {
                if (!wperm.playerHasPermission(player, "WookieeTraderServer.wt.mailbox.self")) {
                    sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                    return false;
                }
                playerchest = sender.getName();
            } else {
                if (!wperm.playerHasPermission(player, "WookieeTraderServer.wt.mailbox.other")) {
                    sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                    return false;
                }
                playerchest = arrargs.get(1);
            }
            ArrayList<List<String>> chestcontents = wdb.sqlChest(playerchest);
            Inventory chest;
            if (!chestcontents.isEmpty()) {
                chest = Bukkit.createInventory(null, 9, playerchest + " - Mailbox");
                int i = 0;
                Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                while (chestcontents.size() > i) {
                    ItemStack item = new ItemStack(Integer.parseInt(chestcontents.get(i).get(1)), Integer.parseInt(chestcontents.get(i).get(3)));
                    if (!chestcontents.get(i).get(4).equals("false")) {
                        String enchstring = chestcontents.get(i).get(4);
                        String[] encharr = enchstring.split(" ");
                        String[] eachench;
                        enchantments.clear();
                        int count = encharr.length;
                        //plugin.getLogger().info(""+count);
                        int l = 0;
                        while (count > l) {
                            eachench = encharr[l].split("-");
                            enchantments.put(Enchantment.getByName(eachench[0]), Integer.parseInt(eachench[1]));
                            l++;
                        }
                        item.addEnchantments(enchantments);
                    }
                    item.setDurability(Short.parseShort(chestcontents.get(i).get(5)));
                    chest.addItem(item);
                    i++;
                }
            } else {
                player.sendMessage("Mailbox empty.");
                return true;
            }
            player.openInventory(chest);
            return true;
        }
        return false;
    }

    boolean cmdSell(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        } else {
            if (arrargs.size() != 3) {
                sender.sendMessage("/wt sell <amount> <price per unit>");
                return true;
            }
            if (!isInteger(arrargs.get(1)) || !isInteger(arrargs.get(2))) {
                sender.sendMessage("/wt sell <amount> <price per unit>");
                return true;
            }
            if (Integer.parseInt(arrargs.get(2)) < 1) {
                sender.sendMessage("/wt sell <amount> <price per unit>");
                return true;
            }
            if (Integer.parseInt(arrargs.get(1)) < 1) {
                sender.sendMessage("/wt sell <amount> <price per unit>");
                return true;
            }
            Player player = (Player) sender;
            Inventory invent = player.getInventory();
            if (!player.getItemInHand().getType().equals(Material.AIR)) {
                ItemStack item = player.getItemInHand();
                Map<Enchantment, Integer> enchantmap = item.getEnchantments();
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
                int durability = item.getDurability();
                //sender.sendMessage("You have itemid: " + player.getItemInHand().getTypeId() + ", name: " + player.getItemInHand().getType().toString() + ", data: " + player.getItemInHand().getData().toString());
                //sender.sendMessage("You have stack: " + player.getItemInHand().getAmount());
                int itemid = player.getItemInHand().getTypeId();
                if (itemid == 373) {
                    sender.sendMessage("Selling potions isn't supported in this version.");
                    return true;
                }
                String itemname = player.getItemInHand().getType().toString();
                ItemStack itemtype = player.getItemInHand();
                String dataval = itemtype.getData().toString();
                //plugin.getLogger().info(dataval);
                int itemcount = Integer.parseInt(arrargs.get(1));
                ItemStack toberemoved = new ItemStack(itemtype.getTypeId(), itemcount);
                toberemoved.addEnchantments(itemtype.getEnchantments());
                toberemoved.setDurability(itemtype.getDurability());
                if (!invent.containsAtLeast(itemtype, itemcount)) {
                    sender.sendMessage("You don't have " + itemcount + " of the exact same items.");
                    return true;
                }
                if (itemtype.hasItemMeta() == true) {
                    if (!toberemoved.hasItemMeta() || !itemtype.getItemMeta().equals(toberemoved.getItemMeta())) {
                        sender.sendMessage("You can't sell that item.");
                        return true;
                    }
                }
                int ppu = Integer.parseInt(arrargs.get(2));
                boolean wdbsuccess = wdb.sqlSell(player.getName(), itemid, ppu, itemcount, enchants, durability);
                if (wdbsuccess) {
                    invent.removeItem(toberemoved);
                } else {
                    sender.sendMessage("Error selling item.");
                }
                return true;
            } else {
                sender.sendMessage("No item in hand.");
                return true;
            }
        }
    }

    boolean cmdCancel(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        Player player = (Player) sender;
        if (!wperm.playerHasPermission(player, "WookieeTraderServer.wt.cancel.self")) {
            sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
            return true;
        }
        if (arrargs != null && arrargs.size() > 1) {
            int page = 1;
            int n = arrargs.size();
            if (confirmed == true) {
                String[] itemdb;
                itemdb = wdb.sqlBuy(Integer.parseInt(arrargs.get(1)));
                if (itemdb != null) {
                    if (!itemdb[0].equals("false")) {
                        int count = Integer.parseInt(itemdb[2]);
                        int amount = Integer.parseInt(itemdb[2]);
                        if (amount < 1) {
                            return false;
                        }
                        wdb.sqlChestAdd(itemdb, Integer.parseInt(itemdb[2]), sender.getName(), false);
                    }
                } else {
                    sender.sendMessage("Nothing to cancel.");
                }
                return true;
            } else {
                if (isInteger(arrargs.get(1))) {
                    ArrayList<List<String>> searcharr;
                    searcharr = wdb.sqlTradeSearch(Integer.parseInt(arrargs.get(1)), "-cancel", page);
                    if (!searcharr.isEmpty()) {
                        if (searcharr.get(0).get(4).equals(sender.getName()) || wperm.playerHasPermission(player, "WookieeTraderServer.wt.cancel.other")) {
                            confMap(sender, arrargs);
                            String itemname = Material.getMaterial(Integer.parseInt(searcharr.get(0).get(1))).toString();
                            sender.sendMessage("Cancel trade? Item: " + itemname + ", amount: " + searcharr.get(0).get(2) + ", cost: " + searcharr.get(0).get(3));
                            sender.sendMessage("To confirm, type: /wt confirm");
                            return true;
                        } else {
                            sender.sendMessage("You don't have permission to cancel other peoples trades.");
                            return true;
                        }
                    } else {
                        sender.sendMessage("Trade not found.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean cmdVersion(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        PluginDescriptionFile pdf = plugin.getDescription();
        String name = pdf.getName();
        String ver = pdf.getVersion();
        if (!(sender instanceof Player)) {
            plugin.getLogger().log(Level.INFO, "{0} {1}", new Object[]{name, ver});
        } else {
            sender.sendMessage(name + " " + ver);
        }
        return true;
    }

    boolean cmdHelp(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        if (arrargs != null && arrargs.size() > 1) {
            int helpswitch = cmdNum(arrargs.get(1));
            String[] msg = new String[2];
            //plugin.getLogger().info("True: " + helpswitch);
            switch (helpswitch) {
                case 0:
                    msg[0] = "/wt search, /wt search player <name>, /wt search item <item>";
                    msg[1] = "Searches for trade IDs by item or player.";
                    helpMsg(msg, sender);
                    return true;
                case 1:
                    msg[0] = "/wt buy <trade id> <amount>";
                    msg[1] = "Buys the amount of items from a trader.";
                    helpMsg(msg, sender);
                    return true;
                case 2:
                    msg[0] = "/wt order <amount> <item> <price per item>";
                    msg[1] = "Orders an amount of an item for the price per item.";
                    helpMsg(msg, sender);
                    return true;
                case 3:
                    msg[0] = "/wt confirm";
                    msg[1] = "Confirms an action where applicable.";
                    helpMsg(msg, sender);
                    return true;
                case 4:
                    msg[0] = "/wt mailbox, /wt mailbox list, /wt mailbox <player>";
                    msg[1] = "Lists current mailboxes or opens a player's mailbox.";
                    helpMsg(msg, sender);
                    return true;
                case 5:
                    msg[0] = "/wt sell <amount> <price per item>";
                    msg[1] = "Orders an amount of an item for the price per item.";
                    helpMsg(msg, sender);
                    return true;
                case 6:
                    msg[0] = "/wt cancel <trade id>";
                    msg[1] = "Cancels a trade by trade ID.";
                    helpMsg(msg, sender);
                    return true;
                case 7:
                    msg[0] = "/wt version";
                    msg[1] = "Displays Wookiee trader version information.";
                    helpMsg(msg, sender);
                    return true;
                case 8:
                    msg[0] = "/wt page <number>";
                    msg[1] = "Lists the next page where applicable";
                    helpMsg(msg, sender);
                    return true;
                case 9:
                    msg[0] = "/wt help";
                    msg[1] = "Lists the Wookiee trader command list";
                    helpMsg(msg, sender);
                    return true;
                default:
                    msg[0] = "WookieeAuction command not found, type '/wt help' for list of commands.";
                    helpMsg(msg, sender);
                    return true;

            }
        }
        if (arrargs != null && arrargs.size() == 1) {
            String msg = "Type /wt help <command> for more information.";
            int cmds = cmdlist.size();
            int i = 0;
            String cmdstring = "";
            while (cmds > i) {
                cmdstring = cmdstring + cmdlist.get(i) + ", ";
                i++;
            }
            cmdstring = cmdstring + "--";
            cmdstring = cmdstring.replace(", --", "");
            if (sender instanceof Player) {
                sender.sendMessage(msg);
                sender.sendMessage("Commands: " + cmdstring);
            } else {
                plugin.getLogger().info(msg);
                plugin.getLogger().log(Level.INFO, "Commands: {0}", cmdstring);
            }
            return true;
        }
        return false;
    }

    boolean cmdTest(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wt") && args.length > 0) {
            int n = args.length;
            int i = 0;
            boolean confirm;
            Player player = (Player) sender;
            boolean returned;
            ArrayList<String> arrargs = new ArrayList<String>();
            int page = 1;
            while (n > i) {
                arrargs.add(args[i]);
                i++;
            }
            if (arrargs.get(0).equalsIgnoreCase("confirm") && confirmmap.size() > 0 && confirmmap.containsKey(sender.getName())) {
                if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.confirm")) {
                    ArrayList<String> cmdarr;
                    cmdarr = confirmmap.get(sender.getName());
                    n = cmdarr.size();
                    i = 1;
                    arrargs.clear();
                    while (n > i) {
                        arrargs.add(cmdarr.get(i));
                        i++;
                    }
                    confirm = true;
                } else {
                    confirm = false;
                }
            } else {
                confirm = false;
            }
            if (arrargs.get(0).equalsIgnoreCase("page") && arrargs.size() > 1 && pagemap.size() > 0 && pagemap.containsKey(sender.getName())) {
                if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.page")) {
                    ArrayList<String> cmdarr;
                    cmdarr = pagemap.get(sender.getName());
                    n = cmdarr.size();
                    i = 1;
                    if (isInteger(arrargs.get(1)) && Integer.parseInt(arrargs.get(1)) > 0) {
                        page = Integer.parseInt(arrargs.get(1));
                        arrargs.clear();
//                        sender.sendMessage("all seems to work");
                        while (n > i) {
                            arrargs.add(cmdarr.get(i));
                            i++;
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                }
            }
            switcher = cmdNum(arrargs.get(0));

            switch (switcher) {
                case 0:     //search
                    if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.search")) {
                        return cmdSearch(sender, cmd, label, arrargs, confirm, page);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                        return true;
                    }
                case 1:     //buy
                    if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.buy")) {
                        return cmdBuy(sender, cmd, label, arrargs, confirm);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                        return true;
                    }
                case 2:     //order
                    if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.order")) {
                        return cmdOrder(sender, cmd, label, arrargs, confirm);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                    }
                case 3:     //confirm
                    sender.sendMessage("Nothing to confirm");
                    return true;
                case 4:     //mailbox
                    return cmdMailbox(sender, cmd, label, arrargs, confirm);
                case 5:     //sell
                    if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.sell")) {
                        return cmdSell(sender, cmd, label, arrargs, confirm);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                        return true;
                    }
                case 6:     //cancel
                    return cmdCancel(sender, cmd, label, arrargs, confirm);
                case 7:     //version
                    if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.version")) {
                        return cmdVersion(sender, cmd, label, arrargs, confirm);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                        return true;
                    }

                case 8:     //page
                    sender.sendMessage("No pages to change.");
                    return true;
                case 9:     //help
                    if (wperm.playerHasPermission(player, "WookieeTraderServer.wt.basic.help")) {
                        return cmdHelp(sender, cmd, label, arrargs, confirm);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Didn't have permission to do that.");
                        return true;
                    }
            }
        }
        return false;
    }
}