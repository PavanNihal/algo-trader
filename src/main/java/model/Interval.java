package model;

public enum Interval {
    ONE_DAY("1d"),
    ONE_MINUTE("I1"),
    THIRTY_MINUTE("I30");

    private String value;

    Interval(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
