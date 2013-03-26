package uk.co.chieloos.wookieetraderserver;

import java.util.*;
import java.util.logging.Level;
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
import org.bukkit.inventory.meta.*;
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
    private WookieeWorldGuard wwg;
    public boolean enabled = false;
    Map<String, ArrayList> confirmmap = Collections.synchronizedMap(new HashMap<String, ArrayList>());
    Map<String, ArrayList> pagemap = Collections.synchronizedMap(new HashMap<String, ArrayList>());
    public ArrayList<String> cmdlist = new ArrayList();
    HashMap<String, String> permlist = new HashMap<String, String>();
    boolean cmdreturn;
    int switcher;

    public WookieeCommandExecutor(WookieeTrader plugin, WookieeDatabase wdb, WookieeEcon wecon, WookieeConfig wcfg, WookieePerm wperm, WookieeWorldGuard _wwg) {
        this.plugin = plugin;
        this.wdb = wdb;
        this.wecon = wecon;
        this.wcfg = wcfg;
        this.wperm = wperm;
        wwg = _wwg;
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
        cmdlist.add("test");
        permlist.put("search", "WookieeTraderServer.wt.basic.search");
        permlist.put("buy", "WookieeTraderServer.wt.basic.buy");
        permlist.put("order", "WookieeTraderServer.wt.basic.order");
        permlist.put("confirm", "WookieeTraderServer.wt.basic.confirm");
        permlist.put("mailbox.other", "WookieeTraderServer.wt.mailbox.other");
        permlist.put("mailbox.self", "WookieeTraderServer.wt.mailbox.self");
        permlist.put("mailbox.list", "WookieeTraderServer.wt.mailbox.list");
        permlist.put("sell", "WookieeTraderServer.wt.basic.sell");
        permlist.put("cancel.self", "WookieeTraderServer.wt.cancel.self");
        permlist.put("cancel.other", "WookieeTraderServer.wt.cancel.other");
        permlist.put("version", "WookieeTraderServer.wt.basic.version");
        permlist.put("page", "WookieeTraderServer.wt.basic.page");
        permlist.put("help", "WookieeTraderServer.wt.basic.help");
    }

    private String canTrade(Player player, String cmd) {
        //plugin.getLogger().info("Player " + player.getName() + " tried to use command:" + cmd);
        String errormsg;
        if (!wperm.playerHasPermission(player, permlist.get(cmd))) {
            //plugin.getLogger().info("Denied because user didn't have correct permissions.");
            errormsg = "You don't have permission to do that.";
            return errormsg;
        }
        if (wwg.wgenabled && wcfg.wcfg.getString("use-wg-region").equals("true")) {
            //plugin.getLogger().info("WorldGuard was found and WookieeTrader is configured to use WorldGuard Regions.");
            if (!wwg.inWTRegion(player)) {
                if (!wperm.playerHasPermission(player, "WookieeTraderServer.region-override")) {
                    //plugin.getLogger().info("Denied because user wasn't in correct region.");
                    errormsg = "You can't do that here.";
                    return errormsg;
                }
            }
        }
        //plugin.getLogger().info("Player was allowed to use command.");
        return null;
    }

//    public String getEnchantName(String enchant) {
//        String name;
//        name = plugin.getConfig().getString("enchants." + enchant + ".name");
//        if (name == null) {
//            return "";
//        } else {
////            plugin.getLogger().info("Found");
//            return name;
//        }
//    }
    private String formatEnchants(String enchants) {
        String formatted = "";
        if (!enchants.equals("false")) {
            String[] encharr = enchants.split(" ");
            String[] eachench;
            String enchantname;
            int count = encharr.length;
            int i = 0;
            while (count > i) {
                if (!encharr[i].equals("false")) {
                    eachench = encharr[i].split("-");
                    if (win.getEnchantName(eachench[0]) != null) {
                        enchantname = win.getEnchantName(eachench[0]);
                        if (enchantname.equals("")) {
                            enchantname = eachench[0];
                        }
                        formatted += enchantname + " " + eachench[1] + ", ";
                    } else {
                        formatted += eachench[0] + " " + eachench[1] + ", ";
                    }

                }
                i++;

            }
            formatted += "--";
            formatted = formatted.replace(", --", "");

        }
        return formatted;
    }

    private int getPercent(int currentdurability, int itemid) {
        Material item = Material.getMaterial(itemid);
        short precurrent = (short) currentdurability;
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

    public String enchToStr(Map<Enchantment, Integer> ench) {
        Map<String, Integer> enchantments = new HashMap();
        String enchants = "";
        for (Map.Entry<Enchantment, Integer> entry : ench.entrySet()) {
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        List<WDBEntry> searcharr = new ArrayList();
        int count = 0;
        boolean all = false;
        if (arrargs.size() == 2 && arrargs.get(1).equalsIgnoreCase("all") || arrargs.size() == 1) {
            searcharr = wdb.searchTrades(-1, "", page);
            count = wdb.tradeCount(-1, "");
            all = true;
        }
        if (arrargs.size() == 3 || all) {
            if (page > 1) {
            }
            if (arrargs.size() > 1 && arrargs.get(1).equalsIgnoreCase("player")) {
                //plugin.getLogger().info("search player");
                searcharr = wdb.searchTrades(-1, arrargs.get(2), page);
                count = wdb.tradeCount(-1, arrargs.get(2));
            } else if (arrargs.size() > 1 && arrargs.get(1).equalsIgnoreCase("item")) {
                //plugin.getLogger().info("search item");
                String item = arrargs.get(2);

                if (isInteger(item)) {
                    searcharr = wdb.searchTrades(Integer.parseInt(arrargs.get(2)), "", page);
                    count = wdb.tradeCount(Integer.parseInt(arrargs.get(2)), "");
                } else {
                    String itemname = arrargs.get(2).toUpperCase();
                    if (Material.getMaterial(itemname) instanceof Material) {
                        int itemid = Material.getMaterial(itemname).getId();
                        searcharr = wdb.searchTrades(itemid, "", page);
                        count = wdb.tradeCount(itemid, "");
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
            if (searcharr != null && !searcharr.isEmpty()) {
                plugin.getLogger().info("searcharr isn't empty");
                int searchsize = searcharr.size();
                int i = 0;
                String itemname;
                int durability;
                String durabilitypercent;
                String enchants;

                while (searchsize > i) {
                    enchants = formatEnchants(searcharr.get(i).getEnchants());
                    durability = getPercent(searcharr.get(i).getDurability(), searcharr.get(i).getItemID());
                    if (durability != -1) {
                        itemname = win.getItemName(searcharr.get(i).getItemID(), 0);
                        if (durability != 100) {
                            durabilitypercent = "Dur: " + ChatColor.RED + durability + ChatColor.RESET + "%" + ", ";
                        } else {
                            durabilitypercent = "Dur: " + ChatColor.GREEN + durability + ChatColor.RESET + "%" + ", ";
                        }
                    } else {
                        itemname = win.getItemName(searcharr.get(i).getItemID(), searcharr.get(i).getDurability());
                        durabilitypercent = "";
                    }
                    sender.sendMessage("id " + ChatColor.YELLOW + searcharr.get(i).getID() + ChatColor.RESET + ": " + searcharr.get(i).getAmount() + " " + ChatColor.DARK_GREEN + itemname + ChatColor.RESET + " for " + ChatColor.BLUE + searcharr.get(i).getCost() + ChatColor.RESET + " ea, " + durabilitypercent + "Seller: " + ChatColor.GOLD + searcharr.get(i).getPlayer());
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
            WDBEntry wde = wdb.getTrade(id);
            if (wde != null) {
                int count = wde.getAmount();
                int amount = Integer.parseInt(arrargs.get(2));
                if (amount < 1) {
                    return false;
                }
                if (amount > count) {
                    sender.sendMessage("There aren't that many to buy.");
                    return true;
                }
                double cost = wde.getCost() * amount;
                double amountdue = Math.abs(cost) * -1;
                if (wecon.giveMoney(sender.getName(), amountdue)) {
                    sender.sendMessage("Cost you: " + cost + " (" + wde.getCost() + "x" + amount + ")");
                    wdb.addToMailbox(wde, Integer.parseInt(arrargs.get(2)), sender.getName());
                    wecon.giveMoney(wde.getPlayer(), cost);
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
        sender.sendMessage(ChatColor.RED + "Not yet implemented.");
//        TODO: Order command
        return true;
    }

    boolean cmdMailbox(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        String errormsg;
        Player player = (Player) sender;
        if (arrargs.size() == 2 && arrargs.get(1).equalsIgnoreCase("list")) {
            errormsg = canTrade(player, "mailbox.list");
            if (errormsg == null) {
                ArrayList<String> playerlist = wdb.getMailboxList();
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
                sender.sendMessage(ChatColor.RED + errormsg);
                return true;
            }
        } else if (arrargs.size() > 0) {
            String playerchest;
            if (arrargs.size() == 1) {
                errormsg = canTrade(player, "mailbox.self");
                if (errormsg != null) {
                    sender.sendMessage(ChatColor.RED + errormsg);
                    return true;
                }
                playerchest = sender.getName();
            } else {
                errormsg = canTrade(player, "mailbox.other");
                if (errormsg != null) {
                    sender.sendMessage(ChatColor.RED + errormsg);
                    return true;
                }
                playerchest = arrargs.get(1);
            }
            ArrayList<WDBEntry> mailboxcontents = wdb.searchMailbox(playerchest);
            Inventory chest;
            if (mailboxcontents != null) {
                chest = Bukkit.createInventory(null, 27, playerchest + " - Mailbox");
                int i = 0;
                Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                while (mailboxcontents.size() > i) {
                    ItemStack item = new ItemStack(mailboxcontents.get(i).getItemID(), mailboxcontents.get(i).getAmount());
                    if (!mailboxcontents.get(i).getEnchants().equals("false")) {
                        String enchstring = mailboxcontents.get(i).getEnchants();
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
                    item.setDurability((short) mailboxcontents.get(i).getDurability());
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
            if (player.getItemInHand().getType().equals(Material.AIR)) {
                sender.sendMessage("No item in hand.");
                return true;
            }
            ItemStack item = player.getItemInHand();

            int durability = item.getDurability();
            //sender.sendMessage("You have itemid: " + player.getItemInHand().getTypeId() + ", name: " + player.getItemInHand().getType().toString() + ", data: " + player.getItemInHand().getData().toString());
            //sender.sendMessage("You have stack: " + player.getItemInHand().getAmount());
            int itemid = player.getItemInHand().getTypeId();
            ItemMeta itemmeta = player.getItemInHand().getItemMeta();
            if (itemmeta instanceof BookMeta
                    || itemmeta instanceof FireworkMeta
                    || itemmeta instanceof MapMeta
                    || itemmeta instanceof FireworkEffectMeta
                    || itemmeta instanceof SkullMeta
                    || itemmeta instanceof LeatherArmorMeta) {
                sender.sendMessage("Unsupported item. (ItemMeta type not supported)");
                return true;
            }
            String itemname = player.getItemInHand().getType().toString();
            ItemStack itemtype = player.getItemInHand();
            String dataval = itemtype.getData().toString();
            //plugin.getLogger().info(dataval);
            int itemcount = Integer.parseInt(arrargs.get(1));
            if (!invent.containsAtLeast(itemtype, itemcount)) {
                sender.sendMessage("You don't have " + itemcount + " of the exact same items.");
                return true;
            }
            String enchants;
            if (itemtype.getType().equals(Material.ENCHANTED_BOOK)) {
                EnchantmentStorageMeta inhandenchantmeta = (EnchantmentStorageMeta) itemtype.getItemMeta();
                enchants = enchToStr(inhandenchantmeta.getStoredEnchants());
            } else {
                enchants = enchToStr(item.getEnchantments());
            }
            ItemStack toberemoved = new ItemStack(itemtype.getTypeId(), itemcount);
            toberemoved.addEnchantments(itemtype.getEnchantments());
            toberemoved.setDurability(itemtype.getDurability());
            toberemoved.setItemMeta(itemmeta);
            String customname = "false";
            if (itemmeta.hasDisplayName()) {
                customname = itemmeta.getDisplayName();
                if (customname.contains("'")
                        || customname.contains("\"")
                        || customname.contains("\\")
                        || customname.contains("--")
                        || customname.contains(";")
                        || customname.contains("`")) {
                    sender.sendMessage("Custom name contains illegal characters.");
                    return true;
                }
            }
            Map<Integer, ItemStack> removedleft = invent.removeItem(toberemoved);
            if (removedleft.isEmpty()) {
                double ppu = Double.parseDouble(arrargs.get(2));
                boolean wdbsuccess = wdb.sell(player.getName(), itemid, ppu, itemcount, enchants, durability, customname);
                if (!wdbsuccess) {
                    sender.sendMessage("Error selling item.");
                    invent.addItem(toberemoved);
                }
            } else {
                int removedleftsize = removedleft.size();
                int i = 0;
                int taken;
                int toreturn;
                ItemStack is;
                while (removedleftsize > i) {
                    is = removedleft.get(i);
                    taken = is.getAmount();
                    toreturn = toberemoved.getAmount() - taken;
                    if (toreturn > 0) {
                        is.setAmount(toreturn);
                        player.getInventory().addItem(is);
                    }
                    sender.sendMessage(is.toString());
                    i++;
                }
            }
            return true;
        }
    }

    boolean cmdCancel(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        String errormsg;
        Player player = (Player) sender;
        errormsg = canTrade(player, "cancel.self");
        if (errormsg != null) {
            sender.sendMessage(ChatColor.RED + errormsg);
            return true;
        }
        if (arrargs != null && arrargs.size() > 1) {
            if (confirmed == true) {
                WDBEntry wde;
                wde = wdb.getTrade(Integer.parseInt(arrargs.get(1)));
                if (wde != null) {
                    wdb.addToMailbox(wde, wde.getAmount(), sender.getName());
                } else {
                    sender.sendMessage("Nothing to cancel.");
                }
                return true;
            } else {
                if (isInteger(arrargs.get(1))) {
                    WDBEntry wde;
                    wde = wdb.getTrade(Integer.parseInt(arrargs.get(1)));
                    if (wde != null) {
                        errormsg = canTrade(player, "cancel.other");
                        if (wde.getPlayer().equals(sender.getName()) || errormsg == null) {
                            confMap(sender, arrargs);
                            String itemname = Material.getMaterial(wde.getItemID()).toString();
                            sender.sendMessage("Cancel trade? Item: " + itemname + ", amount: " + wde.getAmount() + ", cost: " + wde.getCost());
                            sender.sendMessage("To confirm, type: /wt confirm");
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + errormsg);
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
                    msg[1] = "Sells an amount of an item for the price per item.";
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
    void wdeDebug(WDBEntry wde){
        plugin.getLogger().info(wde.toString());
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wt") && args.length > 0) {
            int n = args.length;
            int i = 0;
            boolean confirm;
            Player player = null;
            if ((sender instanceof Player)) {
                player = (Player) sender;
            }
            ArrayList<String> arrargs = new ArrayList<String>();
            String errormsg;
            int page = 1;
            while (n > i) {
                arrargs.add(args[i]);
                i++;
            }
            if (arrargs.get(0).equalsIgnoreCase("confirm") && confirmmap.size() > 0 && confirmmap.containsKey(sender.getName())) {
                errormsg = canTrade(player, "confirm");
                if (errormsg == null) {
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
                    sender.sendMessage(ChatColor.RED + errormsg);
                    return true;
                }
            } else {
                confirm = false;
            }
            if (arrargs.get(0).equalsIgnoreCase("page") && arrargs.size() > 1 && pagemap.size() > 0 && pagemap.containsKey(sender.getName())) {
                errormsg = canTrade(player, "page");
                if (errormsg == null) {
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
                    sender.sendMessage(ChatColor.RED + errormsg);
                    return true;
                }
            }
            switcher = cmdNum(arrargs.get(0));
            switch (switcher) {
                case 0:     //search
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    errormsg = canTrade(player, "search");
                    if (errormsg == null) {
                        return cmdSearch(sender, cmd, label, arrargs, confirm, page);
                    } else {
                        sender.sendMessage(ChatColor.RED + errormsg);
                        return true;
                    }
                case 1:     //buy
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    errormsg = canTrade(player, "buy");
                    if (errormsg == null) {
                        return cmdBuy(sender, cmd, label, arrargs, confirm);
                    } else {
                        sender.sendMessage(ChatColor.RED + errormsg);
                        return true;
                    }
                case 2:     //order
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    errormsg = canTrade(player, "order");
                    if (errormsg == null) {
                        return cmdOrder(sender, cmd, label, arrargs, confirm);
                    } else {
                        sender.sendMessage(ChatColor.RED + errormsg);
                        return true;
                    }
                case 3:     //confirm
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    errormsg = canTrade(player, "confirm");
                    if (errormsg == null) {
                        sender.sendMessage("Nothing to confirm");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + errormsg);
                        return true;
                    }
                case 4:     //mailbox
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    return cmdMailbox(sender, cmd, label, arrargs, confirm);
                case 5:     //sell
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    errormsg = canTrade(player, "sell");
                    if (errormsg == null) {
                        return cmdSell(sender, cmd, label, arrargs, confirm);

                    } else {
                        sender.sendMessage(ChatColor.RED + errormsg);
                        return true;
                    }
                case 6:     //cancel
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    return cmdCancel(sender, cmd, label, arrargs, confirm);
                case 7:     //version
                    errormsg = canTrade(player, "version");
                    if (errormsg == null) {
                        return cmdVersion(sender, cmd, label, arrargs, confirm);
                    } else {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(errormsg);
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + errormsg);
                            return true;
                        }
                    }
                case 8:     //page
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return true;
                    }
                    errormsg = canTrade(player, "page");
                    if (errormsg == null) {
                        sender.sendMessage("No pages to change.");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + errormsg);
                        return true;
                    }
                case 9:     //help
                    return cmdHelp(sender, cmd, label, arrargs, confirm);
                case 10:
                    if (arrargs.size() == 2) {
                        if (arrargs.get(1).equals("debug")){
                            wdb.getDebugInfo();
                        }
                        if (arrargs.get(1).equals("populate")) {
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                                @Override
                                public void run() {
                                    wdb.sqlPopulate();
                                }
                            });
                        }
                        if (arrargs.get(1).equals("save")) {
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

                                @Override
                                public void run() {
                                    wdb.loadDatabases();
                                }
                            });
                        }
                    }
            }
        }
        return false;
    }
}