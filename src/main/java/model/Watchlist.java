package model;

public class Watchlist {
    private int id;
    private String name;

    public Watchlist(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Watchlist watchlist = (Watchlist) o;
        return id == watchlist.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
} 