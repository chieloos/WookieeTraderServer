package uk.co.chieloos.wookieetraderserver;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            } else if (size > start) {
                page = new ArrayList<WDBEntry>(list.subList(start, size));
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
            } else if (size > start) {
                page = new ArrayList<WDBEntry>(list.subList(start, size));
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
        } else if (size > start) {
            page = new ArrayList<WDBEntry>(list.subList(start, size));
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
                    //plugin.getLogger().info("found: " + id);
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

    synchronized boolean addToTrades(String customname, String enchants, int durability, String player, int amount, int itemid, int cost) {
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

    void debug() {
        System.out.println(Arrays.toString(tradesdbByID.entrySet().toArray()));
        System.out.println(Arrays.toString(tradesdbByItemID.entrySet().toArray()));
        System.out.println(Arrays.toString(tradesdbByPlayer.entrySet().toArray()));
        System.out.println(Arrays.toString(tradesdbByTime.entrySet().toArray()));
    }

    void putTradesDatabase() {
        long start = System.currentTimeMillis();
        //plugin.getLogger().info("Starting export");
        FileWriter fw = null;
        try {
            File flatTradesFile = new File(plugin.getDataFolder() + "/trades.txt");
            fw = new FileWriter(flatTradesFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("#MODIFYING THIS FILE MAY CORRUPT YOUR TRADES DATABASE.");
            bw.newLine();
            bw.write("#VER:1");
            bw.newLine();
            bw.write("#ID:ITEMID:DURABILITY:CUSTOMNAME:ENCHANTS:AMOUNT:COST:PLAYER:TIME");
            if (tradesdbByID.size() > 0) {
                for (Map.Entry<Integer, WDBEntry> entry : tradesdbByID.entrySet()) {
                    WDBEntry wdb = entry.getValue();
                    String output = wdb.getID() + ":" + wdb.getItemID() + ":" + wdb.getDurability() + ":" + wdb.getCustomName() + ":" + wdb.getEnchants() + ":" + wdb.getAmount() + ":" + wdb.getCost() + ":" + wdb.getPlayer() + ":" + wdb.getTime();
                    bw.newLine();
                    bw.write(output);
                    //plugin.getLogger().info(output);
                }
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(AccessDataBases.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(AccessDataBases.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //plugin.getLogger().info("Finished export");
        long fin = System.currentTimeMillis();
        long total = fin - start;
        //System.out.println(total);
    }

    void getTradesDatabase() {
        long start = System.currentTimeMillis();
        tradesdbByItemID = new HashMap<Integer, List<WDBEntry>>();
        tradesdbByTime = new TreeMap<Long, List<WDBEntry>>();
        tradesdbByPlayer = new HashMap<String, List<WDBEntry>>();
        tradesdbByID = new HashMap<Integer, WDBEntry>();
        BufferedReader br = null;
        int ver = 0;
        int errorCount = 0;
        try {
            String sCurrentLine;
            String[] entry = {"null"};
            File temp = new File(plugin.getDataFolder() + "/trades.txt");
            if (!temp.exists()) {
                return;
            }
            br = new BufferedReader(new FileReader(plugin.getDataFolder() + "/trades.txt"));

            while ((sCurrentLine = br.readLine()) != null) {
                if (!sCurrentLine.startsWith("#")) {
                    entry = sCurrentLine.split(":");
                    //System.out.println(entry.toString());
                } else {
                    if (sCurrentLine.contains("VER")) {
                        ver = Integer.parseInt(sCurrentLine.replace("#VER:", ""));
                    }
                }
                if (!entry[0].equalsIgnoreCase("null")) {
                    switch (ver) {
                        case 0:
                            break;
                        case 1:
                            String customname,
                             enchants,
                             player;
                            int durability,
                             amount,
                             itemid,
                             cost,
                             id;
                            long time;

                            //plugin.getLogger().info(entry[0] + ":" + entry[1] + ":" + entry[2] + ":" + entry[3] + ":" + entry[4] + ":" + entry[5] + ":" + entry[6] + ":" + entry[7] + ":" + entry[8]);
                            try {
                                customname = entry[3];
                                enchants = entry[4];
                                durability = Integer.parseInt(entry[2]);
                                time = Long.parseLong(entry[8]);
                                player = entry[7];
                                amount = Integer.parseInt(entry[5]);
                                itemid = Integer.parseInt(entry[1]);
                                id = Integer.parseInt(entry[0]);
                                cost = Integer.parseInt(entry[6]);
                                WDBEntry wde = new WDBEntry(customname, enchants, durability, time, player, amount, itemid, id, cost);
                                tradesdbByID.put(id, wde);
                            } catch (Exception e) {
                                errorCount++;
                            }
                    }
                }
            }
            if (errorCount > 0) {
                plugin.getLogger().log(Level.WARNING, "{0} Trades Database entries were corrupt", new Object[]{errorCount});
            }
            if (tradesdbByID.size() > 0) {
                for (Map.Entry<Integer, WDBEntry> ent : tradesdbByID.entrySet()) {

                    updateTrades(ent.getValue(), true);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        long fin = System.currentTimeMillis();
        long total = fin - start;
        //System.out.println(total);
    }

    void putMailboxDatabase() {
        long start = System.currentTimeMillis();
        //plugin.getLogger().info("Starting export");
        FileWriter fw = null;
        try {
            File flatTradesFile = new File(plugin.getDataFolder() + "/mailbox.txt");
            fw = new FileWriter(flatTradesFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("#MODIFYING THIS FILE MAY CORRUPT YOUR MAILBOX DATABASE.");
            bw.newLine();
            bw.write("#VER:1");
            bw.newLine();
            bw.write("#ID:ITEMID:DURABILITY:CUSTOMNAME:ENCHANTS:AMOUNT:PLAYER:TIME");
            if (mailboxdbByID.size() > 0) {
                for (Map.Entry<Integer, WDBEntry> entry : mailboxdbByID.entrySet()) {
                    WDBEntry wdb = entry.getValue();
                    String output = wdb.getID() + ":" + wdb.getItemID() + ":" + wdb.getDurability() + ":" + wdb.getCustomName() + ":" + wdb.getEnchants() + ":" + wdb.getAmount() + ":" + wdb.getPlayer() + ":" + wdb.getTime();
                    bw.newLine();
                    bw.write(output);
                    //plugin.getLogger().info(output);
                }
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(AccessDataBases.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(AccessDataBases.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //plugin.getLogger().info("Finished export");
        long fin = System.currentTimeMillis();
        long total = fin - start;
        //System.out.println(total);
    }

    void getMailboxDatabase() {
        long start = System.currentTimeMillis();
        mailboxdbByPlayer = new HashMap<String, List<WDBEntry>>();
        mailboxdbByID = new HashMap<Integer, WDBEntry>();
        BufferedReader br = null;
        int ver = 0;
        int errorCount = 0;

        String sCurrentLine;
        String[] entry = {"null"};
        File temp = new File(plugin.getDataFolder() + "/mailbox.txt");
        if (!temp.exists()) {
            return;
        }
        try {
            br = new BufferedReader(new FileReader(plugin.getDataFolder() + "/mailbox.txt"));

            while ((sCurrentLine = br.readLine()) != null) {
                if (!sCurrentLine.startsWith("#")) {
                    entry = sCurrentLine.split(":");
                    //System.out.println(entry.toString());
                } else {
                    if (sCurrentLine.contains("VER")) {
                        ver = Integer.parseInt(sCurrentLine.replace("#VER:", ""));
                    }
                }
                if (!entry[0].equalsIgnoreCase("null")) {
                    switch (ver) {
                        case 0:
                            break;
                        case 1:
                            String customname,
                             enchants,
                             player;
                            int durability,
                             amount,
                             itemid,
                             id;
                            long time;

                            //plugin.getLogger().info(entry[0] + ":" + entry[1] + ":" + entry[2] + ":" + entry[3] + ":" + entry[4] + ":" + entry[5] + ":" + entry[6] + ":" + entry[7] + ":" + entry[8]);
                            try {
                                customname = entry[3];
                                enchants = entry[4];
                                durability = Integer.parseInt(entry[2]);
                                time = Long.parseLong(entry[8]);
                                player = entry[7];
                                amount = Integer.parseInt(entry[5]);
                                itemid = Integer.parseInt(entry[1]);
                                id = Integer.parseInt(entry[0]);
                                WDBEntry wde = new WDBEntry(customname, enchants, durability, time, player, amount, itemid, id);
                                mailboxdbByID.put(id, wde);
                            } catch (Exception e) {
                                errorCount++;
                            }
                            break;
                    }
                }
            }
            if (errorCount > 0) {
                plugin.getLogger().log(Level.WARNING, "{0} mailbox database entries were corrupt", new Object[]{errorCount});
            }
            if (mailboxdbByID.size() > 0) {
                for (Map.Entry<Integer, WDBEntry> ent : mailboxdbByID.entrySet()) {

                    updateMailbox(ent.getValue(), true);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        long fin = System.currentTimeMillis();
        long total = fin - start;
        //System.out.println(total);
    }

    void clearTradesDB() {
        tradesdbByItemID = new HashMap<Integer, List<WDBEntry>>();
        tradesdbByTime = new TreeMap<Long, List<WDBEntry>>();
        tradesdbByPlayer = new HashMap<String, List<WDBEntry>>();
        tradesdbByID = new HashMap<Integer, WDBEntry>();
        tradecounter = 0;
    }

    void clearMailboxDB() {
        mailboxdbByID = new HashMap<Integer, WDBEntry>();
        mailboxdbByPlayer = new HashMap<String, List<WDBEntry>>();
        mailboxcounter = 0;
    }
}
