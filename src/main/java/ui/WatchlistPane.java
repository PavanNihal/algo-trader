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
    private DatabaseManager dbManager;

    public WatchlistPane(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        
        // Load CSS file
        initializeStyles();

        // Create and setup components
        TableView<LiveStock> stocksTable = createStocksTable();
        ListView<String> watchlistsView = createWatchlistView(stocksTable);
        VBox watchlistContainer = createWatchlistContainer(watchlistsView);        
        
        // Add components to SplitPane
        this.getItems().addAll(watchlistContainer, stocksTable);
        this.setDividerPositions(0.3);
        this.setPrefWidth(Double.MAX_VALUE);

        // Select first watchlist by default        
        watchlistsView.getSelectionModel().selectFirst();
    }

    private void initializeStyles() {
        this.getStylesheets().add(getClass().getResource("/css/watchlist.css").toExternalForm());
        this.getStyleClass().add("split-pane");
    }

    private VBox createWatchlistContainer(ListView<String> watchlistsView) {
        // Create header components
        Label watchlistsLabel = new Label("Watchlists");
        Button addWatchlistButton = new Button("+");
        addWatchlistButton.getStyleClass().add("add-watchlist-button");
        
        HBox header = createWatchlistHeader(watchlistsLabel, addWatchlistButton);
        
        setupAddWatchlistButton(addWatchlistButton, watchlistsView);

        VBox watchlistContainer = new VBox();
        watchlistContainer.getStyleClass().add("watchlist-container");
        watchlistContainer.getChildren().addAll(header, watchlistsView);
        
        return watchlistContainer;
    }

    private HBox createWatchlistHeader(Label watchlistsLabel, Button addWatchlistButton) {
        HBox header = new HBox();
        header.getStyleClass().add("watchlist-header");
        HBox.setHgrow(watchlistsLabel, Priority.ALWAYS);
        header.getChildren().addAll(watchlistsLabel, addWatchlistButton);
        return header;
    }

    private ListView<String> createWatchlistView(TableView<LiveStock> stocksTable) {
        ListView<String> listView = new ListView<>();
        listView.getStyleClass().add("watchlist-view");
        listView.setItems(FXCollections.observableArrayList(dbManager.getWatchlists()));
        listView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> loadWatchlistData(newValue, stocksTable)
        );
        VBox.setVgrow(listView, Priority.ALWAYS);
        
        setupWatchlistCellFactory(listView);
        
        return listView;
    }

    private void setupWatchlistCellFactory(ListView<String> listView) {
        listView.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
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
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
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

    private void setupAddWatchlistButton(Button addWatchlistButton, ListView<String> watchlistsView) {
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
    }

    private TableView<LiveStock> createStocksTable() {
        TableView<LiveStock> table = new TableView<>();
        table.getStyleClass().add("stocks-table");
        ObservableList<LiveStock> stocksData = FXCollections.observableArrayList();
        
        TableColumn<LiveStock, String> nameCol = createNameColumn();
        TableColumn<LiveStock, Double> ltpCol = createLTPColumn();

        table.getColumns().add(nameCol);
        table.getColumns().add(ltpCol);
        table.setItems(stocksData);
        
        return table;
    }

    private TableColumn<LiveStock, String> createNameColumn() {
        TableColumn<LiveStock, String> nameCol = new TableColumn<>("Stock Name");
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInstrument_key()));
        nameCol.setPrefWidth(200);
        return nameCol;
    }

    private TableColumn<LiveStock, Double> createLTPColumn() {
        TableColumn<LiveStock, Double> ltpCol = new TableColumn<>("LTP");
        ltpCol.setCellValueFactory(cellData -> 
            cellData.getValue().ltpProperty().asObject());
        ltpCol.setPrefWidth(100);
        return ltpCol;
    }

    private void loadWatchlistData(String watchlistName, TableView<LiveStock> stocksTable) {
        ObservableList<LiveStock> stocksData = stocksTable.getItems();
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