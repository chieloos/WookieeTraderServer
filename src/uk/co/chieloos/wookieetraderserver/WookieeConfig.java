package uk.co.chieloos.wookieetraderserver;

import java.util.ArrayList;

public class WookieeConfig {

    public WookieeTrader plugin;

    public WookieeConfig(WookieeTrader plugin) {
        this.plugin = plugin;
    }

    public ArrayList getConfig() {
        ArrayList arr = new ArrayList();
        return arr;
    }
}
