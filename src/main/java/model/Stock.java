package model;

public class Stock {
    private String symbol;
    private String name;
    private String isin;
    private String exchange;
    private String instrument_type;
    private String instrument_key;
    private int lot_size;
    private int freeze_quantity;
    private String exchange_token;
    private double tick_size;
    private String trading_symbol;
    private String short_name;
    private int qty_multiplier;
    private String security_type;
    private String segment;

    public Stock(String symbol) {
        this.symbol = symbol;
    }

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getSegment() {
        return segment;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getInstrument_type() {
        return instrument_type;
    }

    public void setInstrument_type(String instrument_type) {
        this.instrument_type = instrument_type;
    }

    public String getInstrument_key() {
        return instrument_key;
    }

    public void setInstrument_key(String instrument_key) {
        this.instrument_key = instrument_key;
    }

    public int getLot_size() {
        return lot_size;
    }

    public void setLot_size(int lot_size) {
        this.lot_size = lot_size;
    }

    public int getFreeze_quantity() {
        return freeze_quantity;
    }

    public void setFreeze_quantity(int freeze_quantity) {
        this.freeze_quantity = freeze_quantity;
    }

    public String getExchange_token() {
        return exchange_token;
    }

    public void setExchange_token(String exchange_token) {
        this.exchange_token = exchange_token;
    }

    public double getTick_size() {
        return tick_size;
    }

    public void setTick_size(double tick_size) {
        this.tick_size = tick_size;
    }

    public String getTrading_symbol() {
        return trading_symbol;
    }

    public void setTrading_symbol(String trading_symbol) {
        this.trading_symbol = trading_symbol;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public int getQty_multiplier() {
        return qty_multiplier;
    }

    public void setQty_multiplier(int qty_multiplier) {
        this.qty_multiplier = qty_multiplier;
    }

    public String getSecurity_type() {
        return security_type;
    }

    public void setSecurity_type(String security_type) {
        this.security_type = security_type;
    }

    
}
