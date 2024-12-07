package ui.watchlist;

import database.DatabaseManager;
import javafx.scene.control.SplitPane;

public class WatchlistPane extends SplitPane {
    private final StockTable stocksTable;
    private final WatchlistContainer watchlistContainer;
    private final StockSearchContainer searchContainer;

    public WatchlistPane(DatabaseManager dbManager) {
        // Load CSS file
        initializeStyles();

        // Create components
        stocksTable = new StockTable();
        watchlistContainer = new WatchlistContainer(dbManager, stocksTable);
        searchContainer = new StockSearchContainer(dbManager, stocksTable, watchlistContainer);
        
        // Add components to SplitPane
        this.getItems().addAll(watchlistContainer, searchContainer);
        this.setDividerPositions(0.3);
        this.setPrefWidth(Double.MAX_VALUE);

        // Select first watchlist by default
        watchlistContainer.selectFirstWatchlist();
    }

    private void initializeStyles() {
        this.getStylesheets().add(getClass().getResource("/css/watchlist.css").toExternalForm());
        this.getStyleClass().add("split-pane");
    }
} 