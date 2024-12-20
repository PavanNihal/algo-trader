package ui.watchlist;

import model.LiveStock;
import model.Stock;

public class LiveStockWrapper {
    private LiveStock liveStock;
    private Stock stock;

    public LiveStockWrapper(LiveStock liveStock, Stock stock) {
        this.liveStock = liveStock;
        this.stock = stock;
    }

    public LiveStock getLiveStock() {
        return liveStock;
    }

    public Stock getStock() {
        return stock;
    }
}
