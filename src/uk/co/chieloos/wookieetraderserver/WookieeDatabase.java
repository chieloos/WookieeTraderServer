package uk.co.chieloos.wookieetraderserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import uk.co.chieloos.wookieetraderserver.economy.WookieeEcon;

public class WookieeDatabase {

    private WookieeTrader plugin;
    private WookieeEcon wecon;
    private AccessDataBases accessdb;

    public WookieeDatabase(WookieeTrader plugin, WookieeEcon wecon, AccessDataBases accessdb) {
        this.plugin = plugin;
        this.wecon = wecon;
        this.accessdb = accessdb;
    }

    void loadDatabases() {
        long start = System.currentTimeMillis();
        accessdb.getTradesDatabase();
        accessdb.getMailboxDatabase();
        long finish = System.currentTimeMillis();
        long total = finish - start;
        plugin.getLogger().log(Level.INFO, "Got Databases. Took: {0} milliseconds", new Object[]{total});
    }

    void saveDatabases() {
        long start = System.currentTimeMillis();
        accessdb.putTradesDatabase();
        accessdb.putMailboxDatabase();
        long finish = System.currentTimeMillis();
        long total = finish - start;
        plugin.getLogger().log(Level.INFO, "Saved Databases. Took: {0} milliseconds", total);
    }

    protected String timeDate() {
        Date d = new Date();
        SimpleDateFormat td = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ftd = td.format(d);
        return ftd;
    }

    protected WDBEntry getTrade(int id) {
        return accessdb.getTrade(id);
    }

    protected boolean sell(String player, int itemid, int cost, int stack, String enchants, int durability, String customname) {
        return accessdb.addToTrades(customname, enchants, durability, player, stack, itemid, cost);
    }

    protected void testPopulate() {
        List<WDBEntry> dbList = new ArrayList<WDBEntry>();
        WDBEntry wde;
        Random rand = new Random();
        String customname = "false";
        String enchants = "false";
        int durability = 0;
        String player;
        int stack;
        int cost;
        int itemid;
        int popcount = 5000;
        int i = 0;
        while (popcount > i) {
            stack = rand.nextInt(63) + 1;
            cost = rand.nextInt(1000);
            itemid = rand.nextInt(29) + 1;
            Integer name = rand.nextInt(9) + 1;
            player = name.toString();
            long time = System.currentTimeMillis();
            wde = new WDBEntry(customname, enchants, durability, time, player, stack, itemid, i, cost);
            dbList.add(wde);
            i++;
        }
        plugin.getLogger().info("Finished Generating, populating...");
        int size = dbList.size();
        i = 0;
        long start = System.currentTimeMillis();
        while (size > i) {
            accessdb.updateTrades(dbList.get(i), false);
            i++;
        }
        long fin = System.currentTimeMillis();
        long total = fin - start;
        plugin.getLogger().info("Finished populating in: " + total + " ms");
    }

    protected void removeTrade(int id) {
        accessdb.removeFromTrades(id);
    }

    protected ArrayList<String> getMailboxList() {
        return accessdb.getMailboxList();
    }

    protected boolean removeFromMailbox(int itemid, int amount, String player, String enchants, int durability, String customname) {
        WDBEntry wde = new WDBEntry(customname, enchants, durability, 0, "", 0, itemid, 0);
        amount = amount * -1;
        accessdb.addToMailbox(wde, amount, player);
        return true;
    }

    protected boolean addToMailbox(WDBEntry wde, int amount, String player) {
        boolean success = true;
        boolean error;
        error = accessdb.addToMailbox(wde, amount, player);
        if (error) {
            success = false;
        }
        amount = amount * -1;
        error = accessdb.addToTrades(wde.getCustomName(), wde.getEnchants(), wde.getDurability(), wde.getPlayer(), amount, wde.getItemID(), wde.getCost());
        if (error) {
            success = false;
        }
        return success;
    }

    protected ArrayList<WDBEntry> searchMailbox(String player) {
        return accessdb.searchMailbox(player);
    }

    protected List<WDBEntry> searchTrades(int itemid, String player, int page) {
        if (itemid != -1) {
            //plugin.getLogger().info("Searching by itemid");
            return accessdb.searchTrades(itemid, page);
        }
        if (!player.equals("")) {
            //plugin.getLogger().info("Searching by player");
            return accessdb.searchTrades(player, page);
        }
        //plugin.getLogger().info("Searching by all");
        return accessdb.getAllTrades(page);
    }

    protected int tradeCount(int itemid, String player) {
        if (itemid != -1) {
            return accessdb.getTradeCount(itemid);
        }
        if (!player.equals("")) {
            return accessdb.getTradeCount(player);
        }
        return accessdb.getTradeCount();
    }

    void getDebugInfo() {
        accessdb.debug();
    }
    void clearTrades(){
        accessdb.clearTradesDB();
    }
    void clearMailbox(){
        accessdb.clearMailboxDB();
    }
}