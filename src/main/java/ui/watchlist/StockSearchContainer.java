package ui.watchlist;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import model.LiveStock;
import model.Stock;

import java.io.IOException;
import java.util.Arrays;

import api.LiveFeedManager;
import database.DatabaseManager;

public class StockSearchContainer extends VBox {
    private final DatabaseManager dbManager;
    private final StockTable stocksTable;
    private final WatchlistContainer watchlistContainer;
    private final ObservableList<Stock> allStocks;
    private LiveFeedManager liveFeedManager;

    @FXML
    private TextField searchField;
    @FXML
    private ListView<Stock> searchResultsView;
    @FXML
    private Button clearButton;
    @FXML
    private VBox tableContainer;
    
    public StockSearchContainer(DatabaseManager dbManager, StockTable stocksTable, WatchlistContainer watchlistContainer) {
        this.dbManager = dbManager;
        this.stocksTable = stocksTable;
        this.watchlistContainer = watchlistContainer;
        
        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StockSearchContainer.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Load all stocks
        allStocks = FXCollections.observableArrayList(dbManager.getStocksFromDB());
        
        // Add stock table to container
        tableContainer.getChildren().add(stocksTable);
        
        // Setup custom cell factory for search results
        setupSearchResultsCell();
        
        // Setup event handlers
        setupSearchFieldListener();
        setupKeyboardNavigation();
        setupMouseSelection();
        setupClearButton();
    }

    private void setupSearchResultsCell() {
        searchResultsView.setCellFactory(lv -> {
            ListCell<Stock> cell = new ListCell<Stock>() {
                private final BorderPane content = new BorderPane();
                private final Label nameLabel = new Label();
                private final Label symbolLabel = new Label();
                
                {
                    symbolLabel.getStyleClass().add("trading-symbol");
                    symbolLabel.setStyle("-fx-text-fill: #666666;");
                    BorderPane.setMargin(symbolLabel, new Insets(0, 5, 0, 0));
                    
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
    }

    private void setupClearButton() {
        clearButton.setOnAction(e -> clearSearch());
    }

    @FXML
    private void initialize() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearButton.setVisible(newValue != null && !newValue.isEmpty());
        });
    }

    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                hideSearchResults();
            } else {
                ObservableList<Stock> results = allStocks.filtered(
                    stock -> stock.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            stock.getTrading_symbol().toLowerCase().contains(newValue.toLowerCase())
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
        searchResultsView.setMouseTransparent(false);
    }

    private void hideSearchResults() {
        searchResultsView.setVisible(false);
        searchResultsView.setManaged(false);
        searchResultsView.setMouseTransparent(true);
    }

    public void setLiveFeedManager(LiveFeedManager liveFeedManager) {
        this.liveFeedManager = liveFeedManager;
    }
} 