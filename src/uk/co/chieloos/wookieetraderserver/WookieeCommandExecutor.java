package uk.co.chieloos.wookieetraderserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;

public class WookieeCommandExecutor implements CommandExecutor {

    private WookieeTrader plugin;
    private WookieeDatabase wdb;
    private Economy econ = WookieeTrader.econ;
    
    public boolean enabled = false;
    HashMap<String, ArrayList> confirmmap = new HashMap<String, ArrayList>();
    //ArrayList<String> cmdmap = new ArrayList<String>();
    public ArrayList<String> cmdlist = new ArrayList();
    boolean cmdreturn;
    int switcher;

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

    public WookieeCommandExecutor(WookieeTrader plugin, WookieeDatabase wdb) {
        cmdlist.add("search");      //0
        cmdlist.add("buy");         //1
        cmdlist.add("order");       //2
        cmdlist.add("confirm");     //3
        cmdlist.add("mailbox");     //4
        cmdlist.add("sell");        //5
        cmdlist.add("cancel");      //6
        cmdlist.add("version");     //7
        cmdlist.add("more");        //8
        cmdlist.add("help");        //9
        cmdlist.add("test");        //10
        this.plugin = plugin;
        this.wdb = wdb;
    }

    boolean cmdSearch(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        ArrayList<List<String>> searcharr = new ArrayList();
        boolean all = false;
        if (arrargs.size() == 2 && arrargs.get(1).equalsIgnoreCase("all") || arrargs.size() == 1) {
            searcharr = wdb.sqlTradeSearch(-1, "");
            all = true;
        }
        if (arrargs.size() == 3 || all) {

            if (arrargs.size() > 1 && arrargs.get(1).equalsIgnoreCase("player")) {
                //plugin.getLogger().info("search player");
                searcharr = wdb.sqlTradeSearch(0, arrargs.get(2));
            } else if (arrargs.size() > 1 && arrargs.get(1).equalsIgnoreCase("item")) {
                //plugin.getLogger().info("search item");
                String item = arrargs.get(2);

                if (isInteger(item)) {
                    searcharr = wdb.sqlTradeSearch(Integer.parseInt(arrargs.get(2)), "");
                } else {
                    String itemname = arrargs.get(2).toUpperCase();
                    if (Material.getMaterial(itemname) instanceof Material) {
                        int itemid = Material.getMaterial(itemname).getId();
                        searcharr = wdb.sqlTradeSearch(itemid, "");
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
                while (searchsize > i) {
                    String itemname = Material.getMaterial(Integer.parseInt(searcharr.get(i).get(1))).toString();
                    sender.sendMessage("id: " + searcharr.get(i).get(0) + ", item: " + itemname + ", amount: " + searcharr.get(i).get(2) + ", cost: " + searcharr.get(i).get(3) + ", player: " + searcharr.get(i).get(4));
                    i++;
                }
                return true;
            } else {
                sender.sendMessage("No trades found.");
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
                Player player = (Player) sender;
                int cost = Integer.parseInt(itemdb[3]) * amount;
                if (econ.has(sender.getName(), cost)) {
                    econ.withdrawPlayer(sender.getName(), cost);
                    sender.sendMessage("Cost you: " + cost + " (" + itemdb[3] + "x" + amount + ")");
                    wdb.sqlChestAdd(itemdb, Integer.parseInt(arrargs.get(2)), sender.getName());
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
        return false;
    }

    boolean cmdConfirm(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        if (confirmmap.size() > 0 && confirmmap.containsKey(sender.getName())) {
            ArrayList<String> cmdarr;
            cmdarr = confirmmap.get(sender.getName());
            int n = cmdarr.size();
            int i = 1;
            arrargs.clear();
            while (n > i) {
                arrargs.add(cmdarr.get(i));
                i++;
            }
            switcher = cmdNum(cmdarr.get(0));
            switch (switcher) {
                case 0:     //search
                    cmdreturn = cmdSearch(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 1:     //buy
                    cmdreturn = cmdBuy(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 2:     //order
                    cmdreturn = cmdOrder(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 3:     //confirm
                    cmdreturn = cmdConfirm(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 4:     //mailbox
                    cmdreturn = cmdMailbox(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 5:     //sell
                    cmdreturn = cmdSell(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 6:     //cancel
                    cmdreturn = cmdCancel(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 7:     //version
                    cmdreturn = cmdVersion(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 8:     //more
                    cmdreturn = cmdMore(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 9:     //help
                    cmdreturn = cmdHelp(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 10:
                    cmdreturn = cmdTest(sender, cmd, label, arrargs, true);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                default:
                    return true;
            }
        } else {
            plugin.getLogger().info("Nothing to confirm");
            return true;
        }
    }

    boolean cmdMailbox(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        if (arrargs.size() == 2 && arrargs.get(1).equalsIgnoreCase("list")) {
            sender.sendMessage("List called");
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
            }
            return true;
        } else if (arrargs.size() == 2) {
            Player player = (Player) sender;
            ArrayList<List<String>> chestcontents = wdb.sqlChest(arrargs.get(1));
            Inventory chest;
            if (!chestcontents.isEmpty()) {
                chest = Bukkit.createInventory(null, 9, arrargs.get(1) + " - Mailbox");
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
                sender.sendMessage("/wt <price> <price per unit>");
                return true;
            }
            if (!isInteger(arrargs.get(1)) || !isInteger(arrargs.get(2))) {
                sender.sendMessage("/wt <price> <price per unit>");
                return true;
            }
            if (Integer.parseInt(arrargs.get(2)) < 1) {
                sender.sendMessage("/wt <price> <price per unit>");
                return true;
            }
            Player player = (Player) sender;
            Inventory invent = player.getInventory();
            if (!player.getItemInHand().getType().equals(Material.AIR)) {
                ItemStack item = player.getItemInHand();
                Map<Enchantment, Integer> enchantments = item.getEnchantments();
                String enchants = "";
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment key = entry.getKey();
                    Integer value = entry.getValue();
                    enchants += key.getName() + "-" + value + " ";
                }
                if (enchants.equals("")) {
                    enchants = "false";
                }
                int durability = item.getDurability();
                //sender.sendMessage("You have itemid: " + player.getItemInHand().getTypeId() + ", name: " + player.getItemInHand().getType().toString() + ", data: " + player.getItemInHand().getData().toString());
                //sender.sendMessage("You have stack: " + player.getItemInHand().getAmount());
                int itemid = player.getItemInHand().getTypeId();
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
        if (arrargs != null && arrargs.size() > 1) {
            int n = arrargs.size();
            if (confirmed == true) {
                sender.sendMessage("Cancelled Trade.");
                return true;
            } else {
                if (isInteger(arrargs.get(1))) {
                    ArrayList<List<String>> searcharr;
                    searcharr = wdb.sqlTradeSearch(Integer.parseInt(arrargs.get(1)), "-cancel");
                    if (!searcharr.isEmpty()) {
                        if (searcharr.get(0).get(4).equals(sender.getName())) {
                            confMap(sender, arrargs);
                            String itemname = Material.getMaterial(Integer.parseInt(searcharr.get(0).get(1))).toString();
                            sender.sendMessage("Cancel trade? Item: " + itemname + ", amount: " + searcharr.get(0).get(2) + ", cost: " + searcharr.get(0).get(3));
                            sender.sendMessage("To confirm, type: /wt confirm");
                            return true;
                        } else {
                            sender.sendMessage("You can only cancel your own trades.");
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
            plugin.getLogger().info(name + " " + ver);
        } else {
            sender.sendMessage(name + " " + ver);
        }
        return true;
    }

    boolean cmdMore(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        return false;
    }

    boolean cmdHelp(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        if (arrargs != null && arrargs.size() > 1) {
            int helpswitch = cmdNum(arrargs.get(1));
            String[] msg = new String[2];
            //plugin.getLogger().info("True: " + helpswitch);
            switch (helpswitch) {
                case 0:
                    msg[0] = "/wt search player <name>, /wt search item <item>";
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
                    msg[0] = "/wt mailbox list, /wt mailbox <player>";
                    msg[1] = "Lists current mailboxes or opens a player's mailbox.";
                    helpMsg(msg, sender);
                    return true;
                case 5:
                    msg[0] = "/wt sell <amount> <item> <price per item>";
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
                    msg[0] = "/wt more";
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
                plugin.getLogger().info("Commands: " + cmdstring);
            }
            return true;
        }
        return false;
    }

    boolean cmdTest(CommandSender sender, Command cmd, String label, ArrayList<String> arrargs, boolean confirmed) {
        Player player = (Player) sender;
        int chars;
        int i = 0;
        String str = "";
        if (isInteger(arrargs.get(1))) {
            chars = Integer.parseInt(arrargs.get(1));
            while (chars > i) {
                str += "M";
                i++;
            }
        }

        player.sendMessage(str);
//        ItemStack item = player.getItemInHand();
//        if (item.getItemMeta().hasDisplayName()) {
//            player.sendMessage(item.getItemMeta().getDisplayName());
//        } else {
//            player.sendMessage("Has default name.");
//        }

//        short precurrent = item.getDurability();
//        short max = item.getType().getMaxDurability();
//        int current = max - precurrent;
//        player.sendMessage(precurrent + " " + max);
//        if (max != 0) {
//            float prepercent = 100 * current / max;
//            int percent = (int) Math.floor(prepercent);
//            player.sendMessage("Durability: " + current + " / " + max + "(" + percent + "%)");
//        } else {
//            player.sendMessage("Item has no durability");
//        }

//        if (arrargs.size() > 1){
//        wdb.sqlDelete(Integer.parseInt(arrargs.get(1)));
//        }
        //wdb.sqlInsert(364, 70);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wt") && args.length > 0) {
//            if (!enabled) {
//                if (!args[0].equalsIgnoreCase("enable")) {
//                    sender.sendMessage("WookieeTrader isn't enabled. Type /wt enable");
//                    return true;
//                } else {
//                    if (sender.isOp()) {
//                        enabled = true;
//                        sender.sendMessage("WookieeTrader enabled.");
//                        return true;
//                    }
//                }
//                return true;
//            }
            switcher = cmdNum(args[0]);
            //plugin.getLogger().info("cmd:" + cmd.getName() + " & " + args.length);
            //plugin.getLogger().info("True: " + switcher);
            int n = args.length;
            int i = 0;
            ArrayList<String> arrargs = new ArrayList<String>();
            while (n > i) {
                arrargs.add(args[i]);
                //plugin.getLogger().info("arrarg "+i+": "+arrargs.get(i));
                i++;
            }
            switch (switcher) {
                case 0:     //search
                    cmdreturn = cmdSearch(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 1:     //buy
                    cmdreturn = cmdBuy(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 2:     //order
                    cmdreturn = cmdOrder(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 3:     //confirm
                    cmdreturn = cmdConfirm(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 4:     //mailbox
                    cmdreturn = cmdMailbox(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 5:     //sell
                    cmdreturn = cmdSell(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 6:     //cancel
                    cmdreturn = cmdCancel(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 7:     //version
                    cmdreturn = cmdVersion(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 8:     //more
                    cmdreturn = cmdMore(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 9:     //help
                    cmdreturn = cmdHelp(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
                case 10:    //test
                    cmdreturn = cmdTest(sender, cmd, label, arrargs, false);
                    if (cmdreturn == true) {
                        return true;
                    }
                    return false;
//                case 11:    //enable
//                    cmdreturn = cmdEnable(sender, cmd, label, arrargs, false);
//                    if (cmdreturn == true) {
//                        return true;
//                    }
//                    return false;
            }
        }
        return false;
    }
}