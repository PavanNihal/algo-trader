package ui;

import java.util.Optional;

import database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableColumn;
import javafx.collections.ObservableList;
import model.LiveStock;

public class WatchlistPane extends SplitPane {
    private ListView<String> watchlistsView;
    private ObservableList<LiveStock> stocksData;
    private DatabaseManager dbManager;

    public WatchlistPane(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        
        // Load CSS file
        this.getStylesheets().add(getClass().getResource("/css/watchlist.css").toExternalForm());
        this.getStyleClass().add("split-pane");

        // Initialize left pane with watchlists
        Label watchlistsLabel = new Label("Watchlists");
        Button addWatchlistButton = new Button("+");
        addWatchlistButton.getStyleClass().add("add-watchlist-button");
        
        HBox header = new HBox();
        header.getStyleClass().add("watchlist-header");
        HBox.setHgrow(watchlistsLabel, Priority.ALWAYS);
        header.getChildren().addAll(watchlistsLabel, addWatchlistButton);
        
        watchlistsView = new ListView<>();
        watchlistsView.getStyleClass().add("watchlist-view");
        watchlistsView.setItems(FXCollections.observableArrayList(dbManager.getWatchlists()));
        watchlistsView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> loadWatchlistData(newValue)
        );
        VBox.setVgrow(watchlistsView, Priority.ALWAYS);

        // Custom cell factory to add delete button
        watchlistsView.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            private final Button deleteButton = new Button("-");
            private final HBox cell = new HBox();
            private final Label label = new Label();
            
            {
                deleteButton.getStyleClass().add("delete-watchlist-button");
                deleteButton.setVisible(false);
                cell.getChildren().addAll(label, deleteButton);
                cell.setSpacing(10);
                cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                
                // Set HBox to fill width
                cell.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(cell, Priority.ALWAYS);
                
                // Make label fill available space
                label.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(label, Priority.ALWAYS);
                
                // Right align delete button
                deleteButton.setMaxWidth(USE_PREF_SIZE);
                HBox.setMargin(deleteButton, new javafx.geometry.Insets(0, 5, 0, 0));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    deleteButton.setOnAction(event -> {
                        dbManager.deleteWatchlist(item);
                        watchlistsView.getItems().remove(item);
                    });
                    
                    // Show delete button on hover
                    setOnMouseEntered(event -> deleteButton.setVisible(true));
                    setOnMouseExited(event -> deleteButton.setVisible(false));
                    
                    setGraphic(cell);
                }
            }
        });

        addWatchlistButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Watchlist");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new watchlist name:");
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                String trimmedName = name.trim();
                if (!trimmedName.isEmpty() && !watchlistsView.getItems().contains(trimmedName)) {
                    dbManager.saveWatchlist(trimmedName);  // Save to DB
                    watchlistsView.getItems().add(trimmedName);
                    watchlistsView.getSelectionModel().select(trimmedName);
                }
            });
        });

        VBox watchlistContainer = new VBox();
        watchlistContainer.getStyleClass().add("watchlist-container");
        watchlistContainer.getChildren().addAll(header, watchlistsView);

        // Initialize right pane with stocks table
        TableView<LiveStock> stocksTable = new TableView<>();
        stocksTable.getStyleClass().add("stocks-table");
        stocksData = FXCollections.observableArrayList();
        
        TableColumn<LiveStock, String> nameCol = new TableColumn<>("Stock Name");
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInstrument_key()));
        nameCol.setPrefWidth(200);

        TableColumn<LiveStock, Double> ltpCol = new TableColumn<>("LTP");
        ltpCol.setCellValueFactory(cellData -> 
            cellData.getValue().ltpProperty().asObject());
        ltpCol.setPrefWidth(100);

        stocksTable.getColumns().add(nameCol);
        stocksTable.getColumns().add(ltpCol);
        stocksTable.setItems(stocksData);

        
        // Add watchlistContainer and stocksTable to SplitPane
        this.getItems().addAll(watchlistContainer, stocksTable);
        this.setDividerPositions(0.3);
        this.setPrefWidth(Double.MAX_VALUE);

        // Select first watchlist by default
        watchlistsView.getSelectionModel().selectFirst();
    }

    private void loadWatchlistData(String watchlistName) {
        stocksData.clear();        
        
        // Example data
        if (watchlistName.equals("Default Watchlist")) {
            LiveStock reliance = LiveStock.getInstance("RELIANCE");
            LiveStock tcs = LiveStock.getInstance("TCS");
            LiveStock hdfc = LiveStock.getInstance("HDFC");

            reliance.setLtp(1000.0);
            tcs.setLtp(1500.0);
            hdfc.setLtp(2000.0);

            stocksData.addAll(reliance, tcs, hdfc); 
        }
    }
}