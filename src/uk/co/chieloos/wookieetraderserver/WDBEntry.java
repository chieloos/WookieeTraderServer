package uk.co.chieloos.wookieetraderserver;

import java.util.Comparator;

public class WDBEntry implements java.io.Serializable {

    private String customname, enchants, player;
    private int durability, amount, itemid, id, cost;
    private long time;

    WDBEntry(String customname, String enchants, int durability, long time, String player, int amount, int itemid, int id, int cost) {
        this.customname = customname;
        this.enchants = enchants;
        this.durability = durability;
        this.time = time;
        this.player = player;
        this.amount = amount;
        this.itemid = itemid;
        this.id = id;
        this.cost = cost;
    }

    WDBEntry(String customname, String enchants, int durability, long time, String player, int amount, int itemid, int id) {
        this.customname = customname;
        this.enchants = enchants;
        this.durability = durability;
        this.time = time;
        this.player = player;
        this.amount = amount;
        this.itemid = itemid;
        this.id = id;
    }

    WDBEntry(WDBEntry o) {
        this.customname = o.customname;
        this.enchants = o.enchants;
        this.durability = o.durability;
        this.time = o.time;
        this.player = o.player;
        this.amount = o.amount;
        this.itemid = o.itemid;
        this.id = o.id;
        this.cost = o.cost;
    }

    String getCustomName() {
        return customname;
    }

    String getEnchants() {
        return enchants;
    }

    int getDurability() {
        return durability;
    }

    long getTime() {
        return time;
    }

    int getItemID() {
        return itemid;
    }

    String getPlayer() {
        return player;
    }

    int getAmount() {
        return amount;
    }

    int getID() {
        return id;
    }

    int getCost() {
        return cost;
    }

    void updateTime() {
        this.time = System.currentTimeMillis();
    }

    void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof WDBEntry)) {
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.customname != null ? this.customname.hashCode() : 0);
        hash = 17 * hash + (this.enchants != null ? this.enchants.hashCode() : 0);
        hash = 17 * hash + (this.player != null ? this.player.hashCode() : 0);
        hash = 17 * hash + this.durability;
        hash = 17 * hash + this.amount;
        hash = 17 * hash + this.itemid;
        hash = 17 * hash + this.id;
        hash = 17 * hash + this.cost;
        hash = 17 * hash + (int) (this.time ^ (this.time >>> 32));
        return hash;
    }

    static Comparator<WDBEntry> compareByTime() {
        return new Comparator<WDBEntry>() {

            @Override
            public int compare(WDBEntry o1, WDBEntry o2) {
                Long time1 = o1.time;
                Long time2 = o2.time;
                int timeCompare = time2.compareTo(time1);
                if (timeCompare != 0) {
                    return timeCompare;
                } else {
                    return o1.id - o2.id;
                }
            }
        };
    }

    static Comparator<WDBEntry> compareByCost() {
        return new Comparator<WDBEntry>() {

            @Override
            public int compare(WDBEntry o1, WDBEntry o2) {
                Integer cost1 = o1.cost;
                Integer cost2 = o2.cost;
                int costCompare = cost1.compareTo(cost2);
                if (costCompare != 0) {
                    return costCompare;
                } else {
                    Long time1 = o1.time;
                    Long time2 = o2.time;
                    int timeCompare = time1.compareTo(time2);
                    if (timeCompare != 0) {
                        return timeCompare;
                    } else {
                        return o1.id - o2.id;
                    }
                }
            }
        };
    }

    static Comparator<WDBEntry> compareByItemID() {
        return new Comparator<WDBEntry>() {

            @Override
            public int compare(WDBEntry o1, WDBEntry o2) {
                Integer itemid1 = o1.itemid;
                Integer itemid2 = o2.itemid;
                int iidCompare = itemid1.compareTo(itemid2);
                if (iidCompare != 0) {
                    return iidCompare;
                } else {
                    Integer cost1 = o1.cost;
                    Integer cost2 = o2.cost;
                    int costCompare = cost1.compareTo(cost2);
                    if (costCompare != 0) {
                        return costCompare;
                    } else {
                        Long time1 = o1.time;
                        Long time2 = o2.time;
                        int timeCompare = time1.compareTo(time2);
                        if (timeCompare != 0) {
                            return timeCompare;
                        } else {
                            return o1.id - o2.id;
                        }
                    }
                }
            }
        };
    }

    static Comparator<WDBEntry> compareByAmount() {
        return new Comparator<WDBEntry>() {

            @Override
            public int compare(WDBEntry o1, WDBEntry o2) {
                Integer amount1 = o1.amount;
                Integer amount2 = o2.amount;
                int amountCompare = amount1.compareTo(amount2);
                if (amountCompare != 0) {
                    return amountCompare;
                } else {
                    return o1.id - o2.id;
                }
            }
        };
    }

    static Comparator<WDBEntry> compareByID() {
        return new Comparator<WDBEntry>() {

            @Override
            public int compare(WDBEntry o1, WDBEntry o2) {
                return o1.id - o2.id;
            }
        };
    }

    static Comparator<WDBEntry> compareByPlayer() {
        return new Comparator<WDBEntry>() {

            @Override
            public int compare(WDBEntry o1, WDBEntry o2) {
                int playerCompare = o1.player.compareTo(o1.player);
                if (playerCompare != 0) {
                    return playerCompare;
                } else {
                    Long time1 = o1.time;
                    Long time2 = o2.time;
                    int timeCompare = time1.compareTo(time2);
                    if (timeCompare != 0) {
                        return timeCompare;
                    } else {
                        return o1.id - o2.id;
                    }
                }
            }
        };
    }
}