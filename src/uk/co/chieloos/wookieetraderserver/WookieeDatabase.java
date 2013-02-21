package uk.co.chieloos.wookieetraderserver;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lib.PatPeter.SQLibrary.SQLite;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/*
 * wookietrader: enchants durability timedate player amount cost id itemid
 *
 * playerchest: durability enchants timedate id itemid player amount
 */
public class WookieeDatabase {

    public SQLite sqLite;
    private WookieeTrader plugin;
    private Economy econ = WookieeTrader.econ;
    
    public WookieeDatabase(WookieeTrader plugin) {
        this.plugin = plugin;
    }

    public String timeDate() {
        Date d = new Date();
        SimpleDateFormat td = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ftd = td.format(d);
        return ftd;
    }

    public void sqlConnection() {
        sqLite = new SQLite(plugin.getLogger(), "wookieeauction", "WT", plugin.getDataFolder().getAbsolutePath());
        try {
            sqLite.open();
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
            plugin.getPluginLoader().disablePlugin(plugin);
        }
        
    }

    public void sqlTableCheck() {
        if (sqLite.checkTable("wookieetrader")) {
            //plugin.getLogger().info("Table checked.");
        }
    }

    public String[] sqlBuy(int id) {
        try {
            ResultSet var1 = sqLite.query("SELECT * FROM wookieetrader WHERE id='" + id + "' LIMIT 1");
            if (var1.next()) {
                String[] var2 = {var1.getString("id"), var1.getString("itemid"), var1.getString("amount"), var1.getString("cost"), var1.getString("durability"), var1.getString("enchants"), var1.getString("player")};
                //plugin.getLogger().log(Level.INFO, "Index: {0}", var2[0]);
                var1.close();
                return var2;
            } else {
                //plugin.getLogger().info("Index not found.");
            }
            var1.close();
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }

        String[] blank = {"false"};
        return blank;
    }

    public boolean sqlSell(String player, int itemid, int cost, int stack, String enchants, int durability) {
        String query= "";
        String td = timeDate();
        try {
            //check chest for item            
            ResultSet var1 = sqLite.query("SELECT * FROM wookieetrader WHERE player='" + player + "' AND itemid='" + itemid + "' AND durability='" + durability + "'  AND cost='" + cost + "'");
            //update or insert item
            if(var1.next()){
                int count = var1.getInt("amount");
                int newamount = count + stack;
                query = "UPDATE wookieetrader SET amount='" + newamount + "', timedate='" + td + "' WHERE id='" + var1.getString("id") + "'";
            } else {
                query = "INSERT INTO wookieetrader VALUES('" + enchants + "', " + durability +", '" + td + "', '" + player + "', " + stack + ", " + cost + ", NULL, " + itemid + ")";
            }
            
            var1.close();
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
            sqLite.close();
        }
        try {
            ResultSet success = sqLite.query(query);
            if (!success.rowInserted() && !success.rowUpdated()) {
                success.close();
                return true;
            }
            success.close();
            return false;
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
            return false;
        }
    }

    public void sqlInsert(int itemid, int stack) {
        try {
            String name = "Grey_Paws";
            String date = "Bollocks";
            sqLite.query("INSERT INTO playerchest VALUES('" + date + "', NULL, " + itemid + ", '" + name + "', " + stack + ")");
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }
    }

    public void sqlDelete(int id) {
        try {
            sqLite.query("DELETE FROM wookieetrader WHERE id=" + id);
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }
    }

    public ArrayList<List<String>> sqlChest(String name) {
        try {
            //plugin.getLogger().log(Level.INFO, "Name: {0}", name);
            ResultSet var1 = sqLite.query("SELECT * FROM playerchest WHERE player='" + name + "' ORDER BY timedate DESC");
            ArrayList<List<String>> myarr = new ArrayList<List<String>>();

            int i = 0;
            while (var1.next()) {
                ArrayList<String> cols = new ArrayList();
                cols.add(var1.getString("id"));
                cols.add(var1.getString("itemid"));
                cols.add(var1.getString("player"));
                cols.add(var1.getString("amount"));
                cols.add(var1.getString("enchants"));
                cols.add(var1.getString("durability"));
                myarr.add(cols);
                i++; //while count
            }
            //String[] var2 = {var1.getString("id"), var1.getString("itemid"), var1.getString("player"), var1.getString("count")};

            //plugin.getLogger().log(Level.INFO, "Index: {0}", myarr.get(1).get(1));
            var1.close();
            return myarr;
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }
        ArrayList<List<String>> myarr = new ArrayList<List<String>>();
        return myarr;
    }

    public ArrayList<String> sqlChestList() {
        try {
            ResultSet var1 = sqLite.query("SELECT * FROM playerchest ORDER BY player ASC");
            ArrayList<String> myarr = new ArrayList();
            String last = "";
            while (var1.next()) {
                if (!last.equalsIgnoreCase(var1.getString("player"))) {
                    myarr.add(var1.getString("player"));
                    //plugin.getLogger().info("Player Added.");
                }
                last = var1.getString("player");
            }
            var1.close();
            return myarr;
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }
        ArrayList<String> temp = new ArrayList();
        return temp;
    }

    public String sqlChestRemove(int itemid, int amount, String player, String enchants, int durability) {
        String returned = "";
        String query;
        ArrayList<List<Integer>> querylist = new ArrayList<List<Integer>>();
        int i = 0;
        try {
            ResultSet var1 = sqLite.query("SELECT * FROM playerchest WHERE player='" + player + "' AND itemid='" + itemid + "' AND enchants='" + enchants + "' AND durability='" + durability + "'");
            returned = "true";
            while (var1.next()) {
                ArrayList<Integer> vars = new ArrayList();
                vars.add(var1.getInt("id"));
                vars.add(var1.getInt("amount"));
                querylist.add(vars);
            }
            int querysize = querylist.size();
            var1.close();
            while (querysize > i) {
                int count = querylist.get(i).get(1);
                int dbid = querylist.get(i).get(0);
                //plugin.getLogger().info(""+count);
                if (amount >= count) {
                    //plugin.getLogger().info("Deleted");
                    query = "DELETE FROM playerchest WHERE id='" + dbid + "'";
                    //sqLite.query(query);
                    ResultSet delete = sqLite.query(query);
                    delete.close();
                    amount = amount - count;
                } else {
                    //plugin.getLogger().info("Updated");
                    count = count - amount;
                    query = "UPDATE playerchest SET amount='" + count + "' WHERE id='" + dbid + "'";
                    //sqLite.query(query);
                    ResultSet update = sqLite.query(query);
                    update.close();
                    amount = 0;
                }
                if (amount == 0) {
                    return returned;
                }
                i++;
            }
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }
        return returned;
    }

    public boolean sqlChestAdd(String[] itemdb, int amount, String player) {
        //String[] var2 = {var1.getString("id"), var1.getString("itemid"), var1.getString("amount"), var1.getString("cost"), var1.getString("durability"), var1.getString("enchants"), var1.getString("player")};
        String start = timeDate();
        String td = timeDate();
        String query = "";
        try {
            //check chest for item            
            ResultSet var1 = sqLite.query("SELECT * FROM playerchest WHERE player='" + player + "' AND itemid='" + itemdb[1] + "' AND durability='" + itemdb[4] + "' AND enchants='" + itemdb[5] + "'");
            //update or insert item
            if(var1.next()){
                int count = var1.getInt("amount");
                int newamount = count + amount;;
                query = "UPDATE playerchest SET amount='" + newamount + "', timedate='" + td + "' WHERE id='" + var1.getString("id") + "'";
                //plugin.getLogger().info("update item");
            } else {
                //durability enchants timedate id itemid player amount
                query = "INSERT INTO playerchest VALUES('" + itemdb[4] + "', '" + itemdb[5] +"', '" + td + "', NULL , '" + itemdb[1] + "', '" + player + "', '" + amount + "')";
                //plugin.getLogger().info("insert item");
            }
            
            var1.close();
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }
        sqLite.query(query);

        int count = Integer.parseInt(itemdb[2]);
        if(count > amount){
            int newamount = count - amount;
            query = "UPDATE wookieetrader SET amount='" + newamount + "' WHERE id='" + itemdb[0] + "'";
        } else {
            query = "DELETE FROM wookieetrader WHERE id='" + itemdb[0] + "'";
        }
        sqLite.query(query);
        int cost = Integer.parseInt(itemdb[3]);
        int fundsdue = cost * amount;
        if (econ.hasAccount(itemdb[6])){
            econ.depositPlayer(itemdb[6], fundsdue);
        } else {
            econ.createPlayerAccount(itemdb[6]);
            econ.depositPlayer(itemdb[6], fundsdue);
        }
        //delete row from trader
        String finish = timeDate();
        //plugin.getLogger().info(start + " - " + finish);
        return true;
    }

    public ArrayList<List<String>> sqlTradeSearch(int itemid, String seller) {
        ArrayList<List<String>> myarr = new ArrayList();
        String query = "";
        int offset = 0;
        int limit = 10;
        int i = 0;
        if (seller == "") {
            query = "SELECT * FROM wookieetrader WHERE itemid='" + itemid + "' ORDER BY cost ASC LIMIT " + limit + " OFFSET " + offset;
        } else {
            if (!seller.equalsIgnoreCase("-cancel")) {
                query = "SELECT * FROM wookieetrader WHERE player='" + seller + "' ORDER BY itemid ASC, cost ASC LIMIT " + limit + " OFFSET " + offset;
            } else {
                query = "SELECT * FROM wookieetrader WHERE id='" + itemid + "'";
            }
        }
        if (itemid == -1) {
            query = "SELECT * FROM wookieetrader ORDER BY timedate DESC LIMIT " + limit + " OFFSET " + offset;
        }
        try {
            ResultSet rs = sqLite.query(query);
            while (rs.next()) {
                ArrayList<String> results = new ArrayList();
                results.add(rs.getString("id"));
                results.add(rs.getString("itemid"));
                results.add(rs.getString("amount"));
                results.add(rs.getString("cost"));
                results.add(rs.getString("player"));
                results.add(rs.getString("durability"));
                results.add(rs.getString("enchants"));
                myarr.add(results);
                i++;
            }
            //plugin.getLogger().log(Level.INFO, "{0} trades found.", i);
            rs.close();
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
        }
        return myarr;
    }
}