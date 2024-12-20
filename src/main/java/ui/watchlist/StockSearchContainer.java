package ui.watchlist;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import model.LiveStock;
import model.Stock;

import java.util.Arrays;

import api.LiveFeedManager;
import database.DatabaseManager;

public class StockSearchContainer extends VBox {
    private final DatabaseManager dbManager;
    private final StockTable stocksTable;
    private final WatchlistContainer watchlistContainer;
    private final TextField searchField;
    private final ListView<Stock> searchResultsView;
    private final ObservableList<Stock> allStocks;
    private  LiveFeedManager liveFeedManager;
    
    public StockSearchContainer(DatabaseManager dbManager, StockTable stocksTable, WatchlistContainer watchlistContainer) {
        this.dbManager = dbManager;
        this.stocksTable = stocksTable;
        this.watchlistContainer = watchlistContainer;
        this.setPadding(new Insets(10));

        // Create search components
        this.searchField = createSearchField();
        HBox searchBox = createSearchBox(this.searchField);
        searchResultsView = createSearchResultsView();
        
        // Load all stocks
        allStocks = FXCollections.observableArrayList(dbManager.getStocksFromDB());

        // Create separate containers for search results and stock table
        VBox searchContainer = new VBox();
        searchContainer.getChildren().addAll(searchBox, searchResultsView);
        
        VBox tableContainer = new VBox();
        tableContainer.getChildren().add(stocksTable);
        VBox.setMargin(stocksTable, new Insets(30, 0, 0, 0));
        
        // Create stack pane to overlay search results
        StackPane overlayPane = new StackPane();
        overlayPane.getChildren().addAll(tableContainer, searchContainer);
        
        // Setup alignment for search container
        StackPane.setAlignment(searchContainer, Pos.TOP_CENTER);
        
        // Setup event handlers
        setupSearchFieldListener();
        setupKeyboardNavigation();
        setupMouseSelection();
        
        // Add the overlay pane to main container
        this.getChildren().add(overlayPane);
        VBox.setVgrow(overlayPane, Priority.ALWAYS);
    }

    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("Search stocks to add to watchlist...");
        field.getStyleClass().add("search-field");        

        return field;
    }

    private HBox createSearchBox(TextField field) {
        javafx.scene.control.Button clearButton = new javafx.scene.control.Button("✕");
        clearButton.getStyleClass().add("clear-button");
        clearButton.setOnAction(e -> clearSearch());
        clearButton.setVisible(false);
        
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            clearButton.setVisible(newValue != null && !newValue.isEmpty());
        });
        
        HBox searchBox = new HBox(field, clearButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return searchBox;
    }

    private ListView<Stock> createSearchResultsView() {
        ListView<Stock> listView = new ListView<>();
        listView.setVisible(false);
        listView.setManaged(false);
        listView.getStyleClass().add("search-results");
        
        listView.setCellFactory(lv -> {
            ListCell<Stock> cell = new ListCell<Stock>() {
                private final BorderPane content = new BorderPane();
                private final Label nameLabel = new Label();
                private final Label symbolLabel = new Label();
                
                {
                    symbolLabel.getStyleClass().add("trading-symbol");
                    symbolLabel.setStyle("-fx-text-fill: #666666;"); // Lighter font color
                    BorderPane.setMargin(symbolLabel, new Insets(0, 5, 0, 0)); // Right margin
                    
                    content.setLeft(nameLabel);
                    content.setRight(symbolLabel);
                }
                
                @Override
                protected void updateItem(Stock item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        nameLabel.setText(item.getName());
                        symbolLabel.setText(item.getTrading_symbol());
                        setGraphic(content);
                    }
                }
            };
            cell.getStyleClass().add("search-cell");
            return cell;
        });

        return listView;
    }

    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                hideSearchResults();
            } else {
                ObservableList<Stock> results = allStocks.filtered(
                    stock -> stock.getName().toLowerCase().contains(newValue.toLowerCase())
                );
                
                searchResultsView.setItems(results);
                showSearchResults();
                
                if (searchField.isFocused()) {
                    searchResultsView.getStyleClass().add("search-results-active");
                }
            }
        });

        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && searchResultsView.isVisible()) {
                searchResultsView.getStyleClass().add("search-results-focused");
            } else {
                searchResultsView.getStyleClass().remove("search-results-focused");
            }
        });
    }

    private void setupKeyboardNavigation() {
        searchField.setOnKeyPressed(event -> {
            

            switch (event.getCode()) {
                case UP:
                    if (!searchResultsView.isVisible()) return;
                    navigateResults(-1);
                    event.consume();
                    break;
                case DOWN:
                    if (!searchResultsView.isVisible()) return;
                    navigateResults(1);
                    event.consume();
                    break;
                case ENTER:
                    if (!searchResultsView.isVisible()) return;
                    addSelectedStock();
                    event.consume();
                    break;
                case ESCAPE:
                    clearSearch();
                    event.consume();
                    break;
                default:
                    break;
            }
        });

        searchResultsView.setOnKeyTyped(event -> {
            searchField.requestFocus();
            searchField.appendText(event.getCharacter());
            event.consume();
        });
    }

    private void setupMouseSelection() {
        searchResultsView.setOnMouseClicked(event -> addSelectedStock());
    }

    private void navigateResults(int direction) {
        searchResultsView.requestFocus();
        int currentIndex = searchResultsView.getSelectionModel().getSelectedIndex();
        int newIndex;
        
        if (currentIndex == -1) {
            newIndex = direction > 0 ? 0 : searchResultsView.getItems().size() - 1;
        } else {
            newIndex = currentIndex + direction;
            if (newIndex < 0) newIndex = 0;
            if (newIndex >= searchResultsView.getItems().size()) {
                newIndex = searchResultsView.getItems().size() - 1;
            }
        }
        
        searchResultsView.getSelectionModel().select(newIndex);
    }

    private void addSelectedStock() {
        Stock selectedStock = searchResultsView.getSelectionModel().getSelectedItem();
        if (selectedStock == null) return;

        LiveStock stock = LiveStock.getInstance(selectedStock.getInstrument_key());
        liveFeedManager.subscribe(Arrays.asList(stock.getInstrument_key()));
        stocksTable.getItems().add(new LiveStockWrapper(stock, selectedStock));
        
        // Save to watchlist_instruments table using instrument_key
        var selectedWatchlist = watchlistContainer.getSelectedWatchlist();
        if (selectedWatchlist != null) {
            dbManager.saveWatchlistInstrument(selectedStock.getInstrument_key(), selectedWatchlist.getId());
        }
        
        clearSearch();
    }

    private void clearSearch() {
        searchField.clear();
        searchField.getParent().requestFocus();
        hideSearchResults();
    }

    private void showSearchResults() {
        searchResultsView.setVisible(true);
        searchResultsView.setManaged(true);
    }

    private void hideSearchResults() {
        searchResultsView.setVisible(false);
        searchResultsView.setManaged(false);
    }

    public void setLiveFeedManager(LiveFeedManager liveFeedManager) {
        this.liveFeedManager = liveFeedManager;
    }
} 