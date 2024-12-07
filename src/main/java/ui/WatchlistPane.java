package ui;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableColumn;
import javafx.collections.ObservableList;
import model.LiveStock;
import model.Stock;
import model.Watchlist;

public class WatchlistPane extends SplitPane {
    private DatabaseManager dbManager;
    private ListView<Watchlist> watchlistsView;
    private TableView<LiveStock> stocksTable;

    public WatchlistPane(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        
        // Load CSS file
        initializeStyles();

        List<Watchlist> watchlists = dbManager.getWatchlists();
        // Create and setup components
        stocksTable = createStocksTable(watchlists);
        watchlistsView = createWatchlistView(stocksTable, watchlists);
        VBox watchlistContainer = createWatchlistContainer(watchlistsView);        
        
        // Create right side container with search and table
        VBox rightContainer = createRightContainer(stocksTable);
        
        // Add components to SplitPane
        this.getItems().addAll(watchlistContainer, rightContainer);
        this.setDividerPositions(0.3);
        this.setPrefWidth(Double.MAX_VALUE);

        // Select first watchlist by default        
        watchlistsView.getSelectionModel().selectFirst();
    }

    private VBox createRightContainer(TableView<LiveStock> stocksTable) {
        // Create search components
        TextField searchField = new TextField();
        searchField.setPromptText("Search stocks to add to watchlist...");
        searchField.getStyleClass().add("search-field");
        
        ListView<String> searchResultsView = new ListView<>();
        searchResultsView.setVisible(false);
        searchResultsView.setManaged(false);
        searchResultsView.getStyleClass().add("search-results");
        
        // Load stock list once
        ObservableList<String> allStocks = FXCollections.observableArrayList(
            dbManager.getStocksFromDB().stream()
            .map(Stock::getTrading_symbol)
            .collect(Collectors.toList())
        );
        
        // Add focus effects
        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && searchResultsView.isVisible()) {
                searchResultsView.getStyleClass().add("search-results-focused");
            } else {
                searchResultsView.getStyleClass().remove("search-results-focused");
            }
        });

        // Style the search results list cells
        searchResultsView.setCellFactory(lv -> {
            javafx.scene.control.ListCell<String> cell = new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            
            cell.getStyleClass().add("search-cell");
            return cell;
        });

        // Create container for search components
        VBox searchContainer = new VBox(0);
        searchContainer.setPadding(new javafx.geometry.Insets(10));
        searchContainer.getChildren().addAll(searchField);
        
        // Create stack pane to overlay search results
        javafx.scene.layout.StackPane stackPane = new javafx.scene.layout.StackPane();
        stackPane.getChildren().addAll(stocksTable, searchResultsView);
        javafx.scene.layout.StackPane.setAlignment(searchResultsView, javafx.geometry.Pos.TOP_CENTER);
        javafx.scene.layout.StackPane.setMargin(searchResultsView, new javafx.geometry.Insets(0, 10, 0, 10));
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                searchResultsView.setVisible(false);
                searchResultsView.setManaged(false);
            } else {
                ObservableList<String> results = allStocks.filtered(
                    stock -> stock.toLowerCase().contains(newValue.toLowerCase())
                );
                
                searchResultsView.setItems(results);
                searchResultsView.setVisible(true);
                searchResultsView.setManaged(true);
                
                // Apply shadow effect to results when visible and search field is focused
                if (searchField.isFocused()) {
                    searchResultsView.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
                }
            }
        });

        // Handle keyboard navigation
        searchField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    if (searchResultsView.isVisible()) {
                        searchResultsView.requestFocus();
                        int currentIndex = searchResultsView.getSelectionModel().getSelectedIndex();
                        if (currentIndex == -1) {
                            searchResultsView.getSelectionModel().select(searchResultsView.getItems().size() - 1);
                        } else if (currentIndex > 0) {
                            searchResultsView.getSelectionModel().select(currentIndex - 1);
                        }
                        event.consume();
                    }
                    break;
                    
                case DOWN:
                    if (searchResultsView.isVisible()) {
                        searchResultsView.requestFocus();
                        int currentIndex = searchResultsView.getSelectionModel().getSelectedIndex();
                        if (currentIndex == -1) {
                            searchResultsView.getSelectionModel().select(0);
                        } else if (currentIndex < searchResultsView.getItems().size() - 1) {
                            searchResultsView.getSelectionModel().select(currentIndex + 1);
                        }
                        event.consume();
                    }
                    break;
                    
                case ENTER:
                    String selected = searchResultsView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        LiveStock stock = LiveStock.getInstance(selected);
                        stocksTable.getItems().add(stock);
                        
                        // Save to watchlist_instruments table
                        Watchlist selectedWatchlist = watchlistsView.getSelectionModel().getSelectedItem();
                        if (selectedWatchlist != null) {
                            dbManager.saveWatchlistInstrument(selected, selectedWatchlist.getId());
                        }
                        
                        searchField.clear();
                        searchField.requestFocus();
                        searchResultsView.setVisible(false);
                        searchResultsView.setManaged(false);
                    }
                    event.consume();
                    break;
                    
                case ESCAPE:
                    searchField.clear();
                    searchField.getParent().requestFocus(); // Remove focus from search field
                    searchResultsView.setVisible(false);
                    searchResultsView.setManaged(false);
                    event.consume();
                    break;
                    
                default:
                    break;
            }
        });
        
        // Handle mouse selection
        searchResultsView.setOnMouseClicked(event -> {
            String selected = searchResultsView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                LiveStock stock = LiveStock.getInstance(selected);
                stocksTable.getItems().add(stock);
                
                // Save to watchlist_instruments table
                Watchlist selectedWatchlist = watchlistsView.getSelectionModel().getSelectedItem();
                if (selectedWatchlist != null) {
                    dbManager.saveWatchlistInstrument(selected, selectedWatchlist.getId());
                }
                
                searchField.clear();
                searchResultsView.setVisible(false);
                searchResultsView.setManaged(false);
            }
        });
        
        // Add this after creating searchResultsView
        searchResultsView.setOnKeyTyped(event -> {
            // If user types while results are focused, redirect input to search field
            searchField.requestFocus();
            searchField.appendText(event.getCharacter());
            event.consume();
        });
        
        // Create right side container
        VBox rightContainer = new VBox();
        rightContainer.getChildren().addAll(searchContainer, stackPane);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        
        return rightContainer;
    }

    private void initializeStyles() {
        this.getStylesheets().add(getClass().getResource("/css/watchlist.css").toExternalForm());
        this.getStyleClass().add("split-pane");
    }

    private VBox createWatchlistContainer(ListView<Watchlist> watchlistsView) {
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

    private ListView<Watchlist> createWatchlistView(TableView<LiveStock> stocksTable, List<Watchlist> watchlists) {
        ListView<Watchlist> listView = new ListView<>();
        listView.getStyleClass().add("watchlist-view");
        listView.setItems(FXCollections.observableArrayList(watchlists));
        listView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> loadWatchlistData(newValue, stocksTable)
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

    private void setupAddWatchlistButton(Button addWatchlistButton, ListView<Watchlist> watchlistsView) {
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

    private TableView<LiveStock> createStocksTable(List<Watchlist> watchlists) {
        TableView<LiveStock> table = new TableView<>();
        table.getStyleClass().add("stocks-table");
        ObservableList<LiveStock> stocksData = FXCollections.observableArrayList();

        // If there are watchlists, load stocks for the first watchlist
        if (!watchlists.isEmpty()) {
            Watchlist firstWatchlist = watchlists.get(0);
            List<String> instrumentKeys = dbManager.getWatchlistInstruments(firstWatchlist.getId());
            System.out.println("Instrument keys: " + instrumentKeys);
            
            // Create LiveStock instances and add to table
            for (String instrumentKey : instrumentKeys) {
                LiveStock stock = LiveStock.getInstance(instrumentKey);
                stocksData.add(stock);
            }
        }
        
        TableColumn<LiveStock, String> nameCol = createNameColumn();
        TableColumn<LiveStock, Double> ltpCol = createLTPColumn();

        table.getColumns().add(nameCol);
        table.getColumns().add(ltpCol);
        table.setItems(stocksData);
        
        // Add placeholder message when table is empty
        Label placeholder = new Label("Search and add stocks from the search bar above");
        placeholder.getStyleClass().add("table-placeholder");
        table.setPlaceholder(placeholder);
        
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

    private void loadWatchlistData(Watchlist watchlist, TableView<LiveStock> stocksTable) {
        if (watchlist == null) return;
        
        ObservableList<LiveStock> stocksData = stocksTable.getItems();
        stocksData.clear();        
        
        
        // Load instruments from database for the selected watchlist
        List<String> instrumentKeys = dbManager.getWatchlistInstruments(watchlist.getId());
        
        // Create LiveStock instances for each instrument
        for (String instrumentKey : instrumentKeys) {
            LiveStock stock = LiveStock.getInstance(instrumentKey);
            stocksData.add(stock);
        }
    }
}