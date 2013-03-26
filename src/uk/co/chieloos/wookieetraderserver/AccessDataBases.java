package uk.co.chieloos.wookieetraderserver;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class AccessDataBases {

    private HashMap<String, List<WDBEntry>> mailboxdbByPlayer;
    private HashMap<Integer, WDBEntry> mailboxdbByID;
    private HashMap<Integer, List<WDBEntry>> tradesdbByItemID;
    private TreeMap<Long, List<WDBEntry>> tradesdbByTime;
    private HashMap<String, List<WDBEntry>> tradesdbByPlayer;
    private HashMap<Integer, WDBEntry> tradesdbByID;
    private File tradesFile = null;
    private File mailboxFile = null;
    private WookieeTrader plugin;
    private WookieeConfig wcfg;
    private int tradecounter;
    private int mailboxcounter;

    protected AccessDataBases(WookieeTrader plugin, WookieeConfig wcfg) {
        this.plugin = plugin;
        this.wcfg = wcfg;
    }

    void getCounters() {
        if (!wcfg.wcfg.getKeys(false).contains("trade-start-id")) {
            tradecounter = 0;
        } else {
            tradecounter = wcfg.wcfg.getInt("trade-start-id");
        }
        if (!plugin.getConfig().contains("mailbox-start-id")) {
            mailboxcounter = 0;
        } else {
            mailboxcounter = wcfg.wcfg.getInt("mailbox-start-id");
        }
    }

    synchronized ArrayList<WDBEntry> searchMailbox(String player) {
        if (!mailboxdbByPlayer.containsKey(player)) {
            return null;
        } else {
            ArrayList list = new ArrayList(mailboxdbByPlayer.get(player));
            return list;
        }
    }

    synchronized ArrayList<String> getMailboxList() {
        ArrayList<String> list = new ArrayList<String>(mailboxdbByPlayer.keySet());
        Collections.sort(list);
        return list;
    }

    synchronized List<WDBEntry> searchTrades(String player, int offset) {
        if (!tradesdbByPlayer.containsKey(player)) {
            return null;
        } else {
            ArrayList<WDBEntry> list = new ArrayList<WDBEntry>();
            List page = new ArrayList<WDBEntry>();
            int size;
            int start = (offset * 5) - 5;
            int end = (offset * 5) - 1;
            int i;
            List<WDBEntry> tradeslist = tradesdbByPlayer.get(player);
            size = tradeslist.size();
            i = 0;
            while (size > i) {
                list.add(tradeslist.get(i));
                i++;
            }
            Collections.sort(list, WDBEntry.compareByTime());
            size = list.size();
            if (size - 1 >= end) {
                page = new ArrayList<WDBEntry>(list.subList(start, end + 1));
            } else if (size < start) {
                page = new ArrayList<WDBEntry>(list.subList(start, size + 1));
            }
            return page;
        }
    }

    synchronized List<WDBEntry> searchTrades(int itemid, int offset) {
        if (!tradesdbByItemID.containsKey(itemid)) {
            return null;
        } else {
            ArrayList<WDBEntry> list = new ArrayList<WDBEntry>();
            List page = new ArrayList<WDBEntry>();
            int size;
            int start = (offset * 5) - 5;
            int end = (offset * 5) - 1;
            int i;
            List<WDBEntry> tradeslist = tradesdbByItemID.get(itemid);
            size = tradeslist.size();
            i = 0;
            while (size > i) {
                list.add(tradeslist.get(i));
                i++;
            }
            Collections.sort(list, WDBEntry.compareByTime());
            size = list.size();
            if (size - 1 >= end) {
                page = new ArrayList<WDBEntry>(list.subList(start, end + 1));
            } else if (size < start) {
                page = new ArrayList<WDBEntry>(list.subList(start, size + 1));
            }
            return page;
        }
    }

    synchronized List<WDBEntry> getAllTrades(int offset) {
        ArrayList<WDBEntry> list = new ArrayList<WDBEntry>();
        List page = new ArrayList<WDBEntry>();
        int size;
        int start = (offset * 5) - 5;
        int end = (offset * 5) - 1;
        int i;
        for (Map.Entry<Long, List<WDBEntry>> entry : tradesdbByTime.entrySet()) {
            size = entry.getValue().size();
            i = 0;
            while (size > i) {
                list.add(entry.getValue().get(i));
                i++;
            }
        }
        Collections.sort(list, WDBEntry.compareByTime());
        size = list.size();
        if (size - 1 >= end) {
            page = new ArrayList<WDBEntry>(list.subList(start, end + 1));
        } else if (size < start) {
            page = new ArrayList<WDBEntry>(list.subList(start, size + 1));
        }
        return page;
    }

    synchronized WDBEntry getTrade(int id) {
        if (!tradesdbByID.containsKey(id)) {
            return null;
        } else {
            return tradesdbByID.get(id);
        }
    }

    synchronized int getTradeCount() {
        int size = tradesdbByID.size();
        return size;
    }

    synchronized int getTradeCount(int itemid) {
        int size;
        if (tradesdbByItemID.containsKey(itemid)) {
            size = tradesdbByItemID.get(itemid).size();
        } else {
            size = 0;
        }
        return size;
    }

    synchronized int getTradeCount(String player) {
        int size;
        if (tradesdbByPlayer.containsKey(player)) {
            size = tradesdbByPlayer.get(player).size();
        } else {
            size = 0;
        }
        return size;
    }

    synchronized boolean removeFromMailbox(int id) {
        WDBEntry wde = mailboxdbByID.get(id);
        int i = 0;
        boolean found = false;
        boolean success = true;
        if (mailboxdbByPlayer.containsKey(wde.getPlayer())) {
            //plugin.getLogger().info("contains player");
            List mailboxlist = mailboxdbByPlayer.get(wde.getPlayer());
            int size = mailboxlist.size();
            while (size > i) {
                if (wde.equals(mailboxlist.get(i))) {
                    found = true;
                    break;
                }
                i++;
            }
            if (found) {
                mailboxlist.remove(i);
                if (mailboxlist.isEmpty()) {
                    if (mailboxdbByPlayer.remove(wde.getPlayer()) == null) {
                        success = false;
                    }
                } else {
                    mailboxdbByPlayer.put(wde.getPlayer(), mailboxlist);
                }
            }
        } else {
            success = false;
        }
        if (mailboxdbByID.remove(id) == null) {
            success = false;
        }
        return success;
    }

    synchronized boolean removeFromTrades(int id) {
        WDBEntry wde = tradesdbByID.get(id);
        List tradeslist = tradesdbByPlayer.get(wde.getPlayer());
        int size = tradeslist.size();
        int i = 0;
        boolean found = false;
        boolean success = true;
        while (size > i) {
            if (wde.equals(tradeslist.get(i))) {
                //plugin.getLogger().info("found in player: " + i);
                found = true;
                break;
            }
            i++;
        }
        if (found) {
            tradeslist.remove(i);
            if (tradeslist.isEmpty()) {
                tradesdbByPlayer.remove(wde.getPlayer());
            } else {
                tradesdbByPlayer.put(wde.getPlayer(), tradeslist);
            }
        } else {
            success = false;
        }
        tradeslist = tradesdbByItemID.get(wde.getItemID());
        size = tradeslist.size();
        i = 0;
        found = false;
        while (size > i) {
            if (wde.equals(tradeslist.get(i))) {
                //plugin.getLogger().info("found in itemid: " + i);
                found = true;
                break;
            }
            i++;
        }
        if (found) {
            tradeslist.remove(i);
            if (tradeslist.isEmpty()) {
                tradesdbByItemID.remove(wde.getItemID());
            } else {
                tradesdbByItemID.put(wde.getItemID(), tradeslist);
            }
        } else {
            success = false;
        }
        tradeslist = tradesdbByTime.get(wde.getTime());
        size = tradeslist.size();
        i = 0;
        found = false;
        while (size > i) {
            if (wde.equals(tradeslist.get(i))) {
                //plugin.getLogger().info("found in time:" + i);
                found = true;
                break;
            }
            i++;
        }
        if (found) {
            tradeslist.remove(i);
            if (tradeslist.isEmpty()) {
                tradesdbByTime.remove(wde.getTime());
            } else {
                tradesdbByTime.put(wde.getTime(), tradeslist);
            }
        } else {
            success = false;
        }

        if (tradesdbByID.remove(id) == null) {
            success = false;
        }
        return success;
    }

    synchronized boolean addToMailbox(WDBEntry wde, int amount, String player) {
        WDBEntry listentry;
        WDBEntry newentry = null;
        int i = 0;
        boolean found = false;
        boolean delete = false;
        boolean error = false;
        int newamount;
        int id = -1;
        if (mailboxdbByPlayer.containsKey(player)) {
            //plugin.getLogger().info("contains player");
            List<WDBEntry> list = mailboxdbByPlayer.get(player);
            int listsize = list.size();
            while (listsize > i) {
                listentry = list.get(i);
                if (listentry.getCustomName().equals(wde.getCustomName()) && listentry.getEnchants().equals(wde.getEnchants()) && listentry.getDurability() == wde.getDurability() && listentry.getItemID() == wde.getItemID()) {
                    id = listentry.getID();
                    plugin.getLogger().info("found: " + id);
                    found = true;
                    break;
                }
                i++;
            }
            if (found) {
                newentry = new WDBEntry(list.get(i));
                newentry.updateTime();
                //plugin.getLogger().log(Level.INFO, "amount: {0}", amount);
                newamount = list.get(i).getAmount() + amount;
                if (newamount > 0) {
                    newentry.setAmount(newamount);
                } else {
                    if (newamount <= 0) {
                        removeFromMailbox(list.get(i).getID());
                        delete = true;
                    } else {
                        error = true;
                    }
                }
            }
        }
        if (!found) {
            if (wde.getAmount() > 0) {
                newentry = new WDBEntry(wde.getCustomName(), wde.getEnchants(), wde.getDurability(), 0, player, amount, wde.getItemID(), mailboxcounter);
                newentry.updateTime();
                mailboxcounter++;
                wcfg.wcfg.set("mailbox-start-id", mailboxcounter);
            } else {
                error = true;
            }
        }
        if (!error && !delete) {
            if (id != -1 && found) {
                //plugin.getLogger().info("try to remove from mailboxdb");
                removeFromMailbox(id);
            }
            updateMailbox(newentry, false);
        }
        return !error;
    }

    synchronized boolean addToTrades(String customname, String enchants, int durability, String player, int amount, int itemid, double cost) {
        WDBEntry wde;
        WDBEntry newentry = null;
        int i = 0;
        boolean found = false;
        boolean delete = false;
        boolean error = false;
        int newamount;
        int id = -1;
        if (tradesdbByPlayer.containsKey(player)) {
            List<WDBEntry> list = tradesdbByPlayer.get(player);
            int listsize = list.size();
            while (listsize > i) {
                wde = list.get(i);
                if (wde.getCustomName().equals(customname) && wde.getEnchants().equals(enchants) && wde.getDurability() == durability && wde.getItemID() == itemid && wde.getCost() == cost) {
                    found = true;
                    id = wde.getID();
                    break;
                }
                i++;
            }
            if (found) {
                newentry = new WDBEntry(list.get(i));
                //plugin.getLogger().info("amount: " + amount);
                newamount = list.get(i).getAmount() + amount;
                newentry.updateTime();
                if (newamount > 0) {
                    newentry.setAmount(newamount);
                } else {
                    if (newamount == 0) {
                        removeFromTrades(list.get(i).getID());
                        delete = true;
                    } else {
                        error = true;
                    }
                }
            }

        }
        if (!found) {
            if (amount > 0) {
                newentry = new WDBEntry(customname, enchants, durability, 0, player, amount, itemid, tradecounter, cost);
                newentry.updateTime();
                tradecounter++;
                wcfg.wcfg.set("trade-start-id", tradecounter);
            }
        }
        if (!error && !delete) {
            if (id != -1 && found) {
                removeFromTrades(id);
            }
            updateTrades(newentry, false);
        }
        return !error;
    }

    synchronized void updateMailbox(WDBEntry wde, boolean fromFile) {
        List<WDBEntry> dbList;
        int listSize;
        int i;
        boolean update = false;
        if (!fromFile) {
            mailboxdbByID.put(wde.getID(), wde);
        }
        if (mailboxdbByPlayer.containsKey(wde.getPlayer())) {
            dbList = mailboxdbByPlayer.get(wde.getPlayer());
            listSize = dbList.size();
            i = 0;
            while (listSize > i) {
                if (wde.getID() == dbList.get(i).getID()) {
                    dbList.add(i, wde);
                    update = true;
                    break;
                }
                i++;
            }
            if (!update) {
                dbList.add(wde);
            }
        } else {
            dbList = new ArrayList<WDBEntry>();
            dbList.add(wde);
        }
        Collections.sort(dbList, WDBEntry.compareByPlayer());
        mailboxdbByPlayer.put(wde.getPlayer(), dbList);
    }

    synchronized void updateTrades(WDBEntry wde, boolean fromFile) {
        List<WDBEntry> dbList;
        int listSize;
        int i;
        boolean update = false;
        //plugin.getLogger().info(Arrays.toString(tradesdbByTime.keySet().toArray()) + "-" + wde.getTime());
        if (tradesdbByTime.containsKey(wde.getTime())) {
            dbList = tradesdbByTime.get(wde.getTime());
            listSize = dbList.size();
            i = 0;
            while (listSize > i) {
                if (wde.getID() == dbList.get(i).getID()) {
                    dbList.add(i, wde);
                    //plugin.getLogger().info("found in time");
                    update = true;
                    break;
                }
                i++;
            }
            if (!update) {
                dbList.add(wde);
            }
        } else {
            dbList = new ArrayList<WDBEntry>();
            dbList.add(wde);
        }
        Collections.sort(dbList, WDBEntry.compareByTime());
        tradesdbByTime.put(wde.getTime(), dbList);

        if (tradesdbByItemID.containsKey(wde.getItemID())) {
            dbList = tradesdbByItemID.get(wde.getItemID());
            listSize = dbList.size();
            i = 0;
            while (listSize > i) {
                if (wde.getID() == dbList.get(i).getID()) {
                    dbList.add(i, wde);
                    //plugin.getLogger().info("found in itemid");
                    update = true;
                    break;
                }
                i++;
            }
            if (!update) {
                dbList.add(wde);
            }
        } else {
            dbList = new ArrayList<WDBEntry>();
            dbList.add(wde);
        }
        Collections.sort(dbList, WDBEntry.compareByItemID());
        tradesdbByItemID.put(wde.getItemID(), dbList);
        if (tradesdbByPlayer.containsKey(wde.getPlayer())) {
            dbList = tradesdbByPlayer.get(wde.getPlayer());
            listSize = dbList.size();
            i = 0;
            while (listSize > i) {
                if (wde.getID() == dbList.get(i).getID()) {
                    dbList.add(i, wde);
                    //plugin.getLogger().info("found in player");
                    update = true;
                    break;
                }
                i++;
            }
            if (!update) {
                dbList.add(wde);
            }
        } else {
            dbList = new ArrayList<WDBEntry>();
            dbList.add(wde);
        }
        Collections.sort(dbList, WDBEntry.compareByPlayer());
        tradesdbByPlayer.put(wde.getPlayer(), dbList);
        if (!fromFile) {
            tradesdbByID.put(wde.getID(), wde);
        }
    }

    synchronized void getMailboxDatabase() {
        mailboxdbByID = new HashMap<Integer, WDBEntry>();
        mailboxdbByPlayer = new HashMap<String, List<WDBEntry>>();
        mailboxFile = new File(plugin.getDataFolder() + "/mailbox.wdb");
        if (mailboxFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(mailboxFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    mailboxdbByID = (HashMap<Integer, WDBEntry>) ois.readObject();
                } catch (ClassNotFoundException e) {
                    mailboxdbByID = new HashMap<Integer, WDBEntry>();
                    plugin.getLogger().log(Level.SEVERE, "Mailbox database file was corrupt.");
                }
                ois.close();
            } catch (IOException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
            }
        }
        if (mailboxdbByID.size() > 0) {
            for (Map.Entry<Integer, WDBEntry> entry : mailboxdbByID.entrySet()) {

                updateMailbox(entry.getValue(), true);
            }
        }
    }

    synchronized void getTradesDatabase() {
        tradesdbByItemID = new HashMap<Integer, List<WDBEntry>>();
        tradesdbByTime = new TreeMap<Long, List<WDBEntry>>();
        tradesdbByPlayer = new HashMap<String, List<WDBEntry>>();
        tradesdbByID = new HashMap<Integer, WDBEntry>();
        tradesFile = new File(plugin.getDataFolder() + "/trades.wdb");
        if (tradesFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(tradesFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    tradesdbByID = (HashMap<Integer, WDBEntry>) ois.readObject();
                } catch (ClassNotFoundException e) {
                    tradesdbByID = new HashMap<Integer, WDBEntry>();
                    plugin.getLogger().log(Level.SEVERE, "Trades database file was corrupt.");
                }
                ois.close();
            } catch (IOException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
            }
        }
        if (tradesdbByID.size() > 0) {
            for (Map.Entry<Integer, WDBEntry> entry : tradesdbByID.entrySet()) {

                updateTrades(entry.getValue(), true);
            }
        }
    }

    void putMailboxDatabase() {
        long start = System.currentTimeMillis();
        try {
            FileOutputStream fos = new FileOutputStream(plugin.getDataFolder() + "/mailbox.wdb");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mailboxdbByID);
            oos.close();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, null, ex);
        }
        long fin = System.currentTimeMillis();
        long total = fin - start;
        //plugin.getLogger().log(Level.INFO, "Mailbox save time: {0}", new Object[]{total});
    }

    void putTradesDatabase() {
        long start = System.currentTimeMillis();
        try {
            FileOutputStream fos = new FileOutputStream(plugin.getDataFolder() + "/trades.wdb");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(tradesdbByID);
            oos.close();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, null, ex);
        }
        long fin = System.currentTimeMillis();
        long total = fin - start;
        //plugin.getLogger().log(Level.INFO, "Trades save time: {0}", new Object[]{total});
    }

    void debug() {
        System.out.println(Arrays.toString(tradesdbByID.entrySet().toArray()));
        System.out.println(Arrays.toString(tradesdbByItemID.entrySet().toArray()));
        System.out.println(Arrays.toString(tradesdbByPlayer.entrySet().toArray()));
        System.out.println(Arrays.toString(tradesdbByTime.entrySet().toArray()));
    }
}
