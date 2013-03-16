package uk.co.chieloos.wookieetraderserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lib.PatPeter.SQLibrary.SQLite;
import uk.co.chieloos.wookieetraderserver.economy.WookieeEcon;


/*
 * wookietrader: enchants durability timedate player amount cost id itemid
 *
 * playerchest: durability enchants timedate id itemid player amount
 */
public class WookieeDatabase {

    public SQLite sqLite;
    private WookieeTrader plugin;
    private WookieeEcon wecon;

    public WookieeDatabase(WookieeTrader plugin, WookieeEcon wecon) {
        this.plugin = plugin;
        this.wecon = wecon;
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
        } catch (SQLException e) {
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
                String[] var2 = {var1.getString("id"), var1.getString("itemid"), var1.getString("amount"), var1.getString("cost"), var1.getString("durability"), var1.getString("enchants"), var1.getString("player"), var1.getString("customname")};
                //plugin.getLogger().log(Level.INFO, "Index: {0}", var2[0]);
                var1.close();
                return var2;
            } else {
                //plugin.getLogger().info("Index not found.");
            }
            var1.close();
        } catch (SQLException e) {
            plugin.getLogger().info(e.getMessage());
        }

        String[] blank = {"false"};
        return blank;
    }

    public boolean sqlSell(String player, int itemid, int cost, int stack, String enchants, int durability, String customname) {
        String query = "";
        PreparedStatement ps = null;
        String td = timeDate();
        try {
            //check chest for item
            ResultSet var1 = sqLite.query("SELECT * FROM wookieetrader WHERE player='" + player + "' AND itemid='" + itemid + "' AND durability='" + durability + "'  AND cost='" + cost + "' AND enchants='" + enchants + "' AND customname='" + customname + "'");
            //update or insert item
            if (var1.next()) {
                int count = var1.getInt("amount");
                int newamount = count + stack;
                query = "UPDATE wookieetrader SET amount='" + newamount + "', timedate='" + td + "' WHERE id='" + var1.getString("id") + "'";
//                ps = sqLite.prepare("UPDATE wookieetrader SET amount='?', timedate='?' WHERE id='?'");
//                ps.setInt(1, newamount);
//                ps.setString(2, td);
//                ps.setString(3, var1.getString("id"));
            } else {
                query = "INSERT INTO wookieetrader VALUES('" + customname + "', '" + enchants + "', " + durability + ", '" + td + "', '" + player + "', " + stack + ", " + cost + ", NULL, " + itemid + ")";
//                ps.clearParameters();
//                ps = sqLite.prepare("INSERT INTO wookieetrader VALUES('?', '?', ?, '?', '?', ?, ?, NULL, ?)");
//                ps.setString(1, customname);
//                ps.setString(2, enchants);
//                ps.setInt(3, durability);
//                ps.setString(4, td);
//                ps.setString(5, player);
//                ps.setInt(6, stack);
//                ps.setInt(7, cost);
//                ps.setInt(8, itemid);
            }

            var1.close();
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            plugin.getLogger().info(e.getMessage());
            return false;
        }
    }

    public void sqlInsert(int itemid, int stack) {
    }

    public void sqlDelete(int id) {
        try {
            String[] itemdb = new String[0];
            itemdb[0] = String.valueOf(id);
            sqlChestAdd(itemdb, -1, null, true);
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
                cols.add(var1.getString("customname"));
                myarr.add(cols);
                i++;
                //plugin.getLogger().info(var1.getString("id")+ ", " + var1.getString("itemid")+ ", " + var1.getString("player")+ ", " + var1.getString("amount"));
            }

            //plugin.getLogger().log(Level.INFO, "Index: {0}", myarr.get(1).get(1));
            var1.close();
            return myarr;
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            plugin.getLogger().info(e.getMessage());
        }
        ArrayList<String> temp = new ArrayList();
        return temp;
    }

    public String sqlChestRemove(int itemid, int amount, String player, String enchants, int durability, String customname) {
        //plugin.getLogger().info(enchants);
        String returned = "";
        String query;
        ArrayList<List<Integer>> querylist = new ArrayList<List<Integer>>();
        int i = 0;
        //plugin.getLogger().info("player='" + player + "' AND itemid='" + itemid + "' AND enchants='" + enchants + "' AND durability='" + durability + "' AND customname='" + customname + "'");
        try {
            ResultSet var1 = sqLite.query("SELECT * FROM playerchest WHERE player='" + player + "' AND itemid='" + itemid + "' AND enchants='" + enchants + "' AND durability='" + durability + "' AND customname='" + customname + "'");
            returned = "true";
            while (var1.next()) {
                //plugin.getLogger().info("Found");
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
        } catch (SQLException e) {
            plugin.getLogger().info(e.getMessage());
        }
        return returned;
    }

    public boolean sqlChestAdd(String[] itemdb, int amount, String player, boolean cancel) {
        //String[] var2 = {var1.getString("id"), var1.getString("itemid"), var1.getString("amount"), var1.getString("cost"), var1.getString("durability"), var1.getString("enchants"), var1.getString("player")};
        String start = timeDate();
        String td = timeDate();
        String query;
        PreparedStatement ps;
        try {
            //check chest for item            
            ResultSet var1 = sqLite.query("SELECT * FROM playerchest WHERE player='" + player + "' AND itemid='" + itemdb[1] + "' AND durability='" + itemdb[4] + "' AND enchants='" + itemdb[5] + "'");
            //update or insert item
            if (var1.next()) {
                int count = var1.getInt("amount");
                int newamount = count + amount;
                query = "UPDATE playerchest SET amount='" + newamount + "', timedate='" + td + "' WHERE id='" + var1.getString("id") + "'";
                //plugin.getLogger().info("update item");
            } else {
                //customname durability enchants timedate id itemid player amount
                query = "INSERT INTO playerchest VALUES('" + itemdb[7] + "', '" + itemdb[4] + "', '" + itemdb[5] + "', '" + td + "', NULL , '" + itemdb[1] + "', '" + player + "', '" + amount + "')";
//                ps = sqLite.prepare("INSERT INTO playerchest VALUES('?', ?, '?', '?', NULL , ?, '?', ?)");
//                ps.setString(1, "false");
//                ps.setInt(2, Integer.parseInt(itemdb[4]));
//                ps.setString(3, itemdb[5]);
//                ps.setString(4, td);
//                ps.setInt(5, Integer.parseInt(itemdb[1]));
//                ps.setString(6, player);
//                ps.setInt(7, amount);
//                ps.executeUpdate();
                
                //plugin.getLogger().info("insert item");
            }
            var1.close();
            sqLite.query(query);
        } catch (SQLException e) {
            plugin.getLogger().info(e.getMessage());
        }
        

        int count = Integer.parseInt(itemdb[2]);
        if (count > amount) {
            int newamount = count - amount;
            //plugin.getLogger().info("update");
            query = "UPDATE wookieetrader SET amount='" + newamount + "' WHERE id='" + itemdb[0] + "'";
        } else {
            //plugin.getLogger().info("delete");
            query = "DELETE FROM wookieetrader WHERE id='" + itemdb[0] + "'";
        }
        sqLite.query(query);
        if (!cancel) {
            int cost = Integer.parseInt(itemdb[3]);
            int fundsdue = cost * amount;
            if (wecon.hasAccount(itemdb[6])) {
                wecon.giveMoney(itemdb[6], fundsdue);
            } else {
                plugin.getLogger().warning("Seller was missing from Economy, failed to give money.");
            }
        }
        //delete row from trader
        String finish = timeDate();
        //plugin.getLogger().info(start + " - " + finish);
        return true;
    }

    public int sqlTradeCount(int itemid, String seller) {
        int count = -1;
        String query;
        if (seller.equals("")) {
            query = "SELECT COUNT(*) FROM wookieetrader WHERE itemid='" + itemid + "'";
        } else {
            query = "SELECT COUNT(*) FROM wookieetrader WHERE player='" + seller + "'";
        }
        if (itemid == -1) {
            query = "SELECT COUNT(*) FROM wookieetrader";
        }
        try {
            ResultSet rs = sqLite.query(query);
            while (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            return count;
        } catch (SQLException e) {
            plugin.getLogger().info(e.getMessage());
        }
        return -1;
    }

    public ArrayList<List<String>> sqlTradeSearch(int itemid, String seller, int page) {
        ArrayList<List<String>> myarr = new ArrayList();
        String query;
        String countquery;
        int offset;
        int limit;
        offset = (page - 1) * 5;
        limit = 5;
        int i = 0;
        if (seller.equals("")) {
            query = "SELECT * FROM wookieetrader WHERE itemid='" + itemid + "' ORDER BY cost ASC LIMIT " + limit + " OFFSET " + offset;
            countquery = "SELECT COUNT(*) FROM wookieetrader WHERE itemid='" + itemid + "'";
        } else {
            if (!seller.equalsIgnoreCase("-cancel")) {
                query = "SELECT * FROM wookieetrader WHERE player='" + seller + "' ORDER BY itemid ASC, cost ASC LIMIT " + limit + " OFFSET " + offset;
                countquery = "SELECT COUNT(*) FROM wookieetrader WHERE player='" + seller + "'";
            } else {
                query = "SELECT * FROM wookieetrader WHERE id='" + itemid + "'";
            }
        }
        if (itemid == -1) {
            query = "SELECT * FROM wookieetrader ORDER BY timedate DESC LIMIT " + limit + " OFFSET " + offset;
            countquery = "SELECT COUNT(*) FROM wookieetrader";
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
                results.add(rs.getString("customname"));
                myarr.add(results);
                i++;
            }
            //plugin.getLogger().log(Level.INFO, "{0} trades found.", i);
            rs.close();

        } catch (SQLException e) {
            plugin.getLogger().info(e.getMessage());
        }
        return myarr;
    }
}