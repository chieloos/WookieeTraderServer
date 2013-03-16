package uk.co.chieloos.wookieetraderserver.economy;

public interface Economy {

    public String currencyName();

    public boolean hasAccount(String string);

    public double getBalance(String string);

    public boolean has(String string, double d);
    
    public boolean withdrawPlayer(String string, double d);

    public boolean depositPlayer(String string, double d);
}
