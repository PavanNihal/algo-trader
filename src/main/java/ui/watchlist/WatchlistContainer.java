package ui.watchlist;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Watchlist;
import database.DatabaseManager;
import java.util.List;
import java.util.Optional;

public class WatchlistContainer extends VBox {
    private final DatabaseManager dbManager;
    private final ListView<Watchlist> watchlistsView;
    private final StockTable stocksTable;

    public WatchlistContainer(DatabaseManager dbManager, StockTable stocksTable) {
        this.dbManager = dbManager;
        this.stocksTable = stocksTable;
        this.getStyleClass().add("watchlist-container");

        // Create components
        Label watchlistsLabel = new Label("Watchlists");
        Button addWatchlistButton = new Button("+");
        addWatchlistButton.getStyleClass().add("add-watchlist-button");
        
        // Create header
        HBox header = createHeader(watchlistsLabel, addWatchlistButton);
        
        // Initialize watchlistsView without data
        watchlistsView = createWatchlistView(FXCollections.observableArrayList());
        
        // Setup add button functionality
        setupAddWatchlistButton(addWatchlistButton);
        
        // Add components to container
        this.getChildren().addAll(header, watchlistsView);
    }

    private HBox createHeader(Label watchlistsLabel, Button addWatchlistButton) {
        HBox header = new HBox();
        header.getStyleClass().add("watchlist-header");
        HBox.setHgrow(watchlistsLabel, Priority.ALWAYS);
        header.getChildren().addAll(watchlistsLabel, addWatchlistButton);
        return header;
    }

    private ListView<Watchlist> createWatchlistView(List<Watchlist> watchlists) {
        ListView<Watchlist> listView = new ListView<>();
        listView.getStyleClass().add("watchlist-view");
        listView.setItems(FXCollections.observableArrayList(watchlists));
        listView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> loadWatchlistData(newValue)
        );
        VBox.setVgrow(listView, Priority.ALWAYS);
        
        setupWatchlistCellFactory(listView);
        
        return listView;
    }

    private void setupWatchlistCellFactory(ListView<Watchlist> listView) {
        listView.setCellFactory(lv -> new javafx.scene.control.ListCell<Watchlist>() {
            private final Button deleteButton = new Button("-");
            private final HBox cell = new HBox();
            private final Label label = new Label();
            
            {
                setupCellComponents();
            }

            private void setupCellComponents() {
                deleteButton.getStyleClass().add("delete-watchlist-button");
                deleteButton.setVisible(false);
                cell.getChildren().addAll(label, deleteButton);
                cell.setSpacing(10);
                cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                
                cell.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(cell, Priority.ALWAYS);
                
                label.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(label, Priority.ALWAYS);
                
                deleteButton.setMaxWidth(USE_PREF_SIZE);
                HBox.setMargin(deleteButton, new javafx.geometry.Insets(0, 5, 0, 0));
            }

            @Override
            protected void updateItem(Watchlist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item.getName());
                    deleteButton.setOnAction(event -> {
                        dbManager.deleteWatchlist(item);
                        listView.getItems().remove(item);
                    });
                    
                    setOnMouseEntered(event -> deleteButton.setVisible(true));
                    setOnMouseExited(event -> deleteButton.setVisible(false));
                    
                    setGraphic(cell);
                }
            }
        });
    }

    private void setupAddWatchlistButton(Button addWatchlistButton) {
        addWatchlistButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Watchlist");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new watchlist name:");
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                String trimmedName = name.trim();
                if (!trimmedName.isEmpty() && watchlistsView.getItems().stream()
                        .noneMatch(w -> w.getName().equals(trimmedName))) {
                    dbManager.saveWatchlist(trimmedName);
                    watchlistsView.setItems(FXCollections.observableArrayList(dbManager.getWatchlists()));
                    watchlistsView.getSelectionModel().selectLast();
                }
            });
        });
    }

    private void loadWatchlistData(Watchlist watchlist) {
        if (watchlist == null) return;
        List<String> instrumentKeys = dbManager.getWatchlistInstruments(watchlist.getId());
        stocksTable.loadStocks(instrumentKeys);
    }

    public Watchlist getSelectedWatchlist() {
        return watchlistsView.getSelectionModel().getSelectedItem();
    }

    public void selectFirstWatchlist() {
        watchlistsView.getSelectionModel().selectFirst();
    }

    // Added a method to load watchlists
    public void loadWatchlists() {
        List<Watchlist> watchlists = dbManager.getWatchlists();
        watchlistsView.setItems(FXCollections.observableArrayList(watchlists));
    }
} 