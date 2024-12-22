package ui.watchlist;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Stock;
import model.Watchlist;
import database.DatabaseManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class WatchlistContainer extends VBox {
    private final DatabaseManager dbManager;
    private final StockTable stocksTable;

    @FXML
    private ListView<Watchlist> watchlistsView;
    @FXML
    private Button addWatchlistButton;
    
    public WatchlistContainer(DatabaseManager dbManager, StockTable stocksTable) {
        this.dbManager = dbManager;
        this.stocksTable = stocksTable;
        
        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/WatchlistContainer.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Initialize watchlistsView
        watchlistsView.setItems(FXCollections.observableArrayList());
        
        // Setup components
        setupWatchlistCellFactory();
        setupAddWatchlistButton();
        setupWatchlistSelection();
    }

    private void setupWatchlistSelection() {
        watchlistsView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> loadWatchlistData(newValue)
        );
    }

    private void setupWatchlistCellFactory() {
        watchlistsView.setCellFactory(lv -> new javafx.scene.control.ListCell<Watchlist>() {
            private final Button deleteButton = new Button("x");
            private final HBox cell = new HBox();
            private final Label label = new Label();
            
            {
                setupCellComponents();
            }

            private void setupCellComponents() {
                deleteButton.getStyleClass().add("delete-button");
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
                        watchlistsView.getItems().remove(item);
                    });
                    
                    setOnMouseEntered(event -> deleteButton.setVisible(true));
                    setOnMouseExited(event -> deleteButton.setVisible(false));
                    
                    setGraphic(cell);
                }
            }
        });
    }

    private void setupAddWatchlistButton() {
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
        List<Stock> stocks = dbManager.getWatchlistInstruments(watchlist.getId());
        stocksTable.setWatchlistId(watchlist.getId());
        stocksTable.loadStocks(stocks);
    }

    public Watchlist getSelectedWatchlist() {
        return watchlistsView.getSelectionModel().getSelectedItem();
    }

    public void selectFirstWatchlist() {
        watchlistsView.getSelectionModel().selectFirst();
    }

    public void loadWatchlists() {
        List<Watchlist> watchlists = dbManager.getWatchlists();
        watchlistsView.setItems(FXCollections.observableArrayList(watchlists));
    }
} 