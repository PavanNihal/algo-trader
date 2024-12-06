package model;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class LiveStock {
    private String instrument_key;
    private DoubleProperty ltp;

    private LiveStock(String instrument_key) {
        this.instrument_key = instrument_key;
    }

    public String getInstrument_key() {
        return instrument_key;
    }

    public double getLtp() {
        return ltp.get();
    }

    public void setLtp(double value) {
        if(this.ltp == null) {
            this.ltp = new SimpleDoubleProperty(value);
        } else {
            this.ltp.set(value);
        }
    }

    public DoubleProperty ltpProperty() {
        return ltp;
    }

    private static Map<String, LiveStock> instances = new HashMap<>();

    public static LiveStock getInstance(String instrument_key) {
        return instances.computeIfAbsent(instrument_key, key -> new LiveStock(key));
    }
}