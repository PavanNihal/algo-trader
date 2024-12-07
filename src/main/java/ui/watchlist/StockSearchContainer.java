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
import model.LiveStock;
import model.Stock;
import database.DatabaseManager;
import java.util.stream.Collectors;

public class StockSearchContainer extends VBox {
    private final DatabaseManager dbManager;
    private final StockTable stocksTable;
    private final WatchlistContainer watchlistContainer;
    private final TextField searchField;
    private final ListView<String> searchResultsView;
    private final ObservableList<String> allStocks;

    public StockSearchContainer(DatabaseManager dbManager, StockTable stocksTable, WatchlistContainer watchlistContainer) {
        this.dbManager = dbManager;
        this.stocksTable = stocksTable;
        this.watchlistContainer = watchlistContainer;
        this.setPadding(new Insets(10));

        // Create search components
        searchField = createSearchField();
        searchResultsView = createSearchResultsView();
        
        // Load all stocks
        allStocks = FXCollections.observableArrayList(
            dbManager.getStocksFromDB().stream()
                .map(Stock::getTrading_symbol)
                .collect(Collectors.toList())
        );

        // Create stack pane for search results overlay
        StackPane stackPane = createStackPane();
        
        // Setup event handlers
        setupSearchFieldListener();
        setupKeyboardNavigation();
        setupMouseSelection();
        
        // Add components
        this.getChildren().addAll(searchField, stackPane);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
    }

    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("Search stocks to add to watchlist...");
        field.getStyleClass().add("search-field");
        return field;
    }

    private ListView<String> createSearchResultsView() {
        ListView<String> listView = new ListView<>();
        listView.setVisible(false);
        listView.setManaged(false);
        listView.getStyleClass().add("search-results");
        
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.getStyleClass().add("search-cell");
            return cell;
        });

        return listView;
    }

    private StackPane createStackPane() {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(stocksTable, searchResultsView);
        StackPane.setAlignment(searchResultsView, Pos.TOP_CENTER);
        StackPane.setMargin(searchResultsView, new Insets(0, 10, 0, 10));
        return stackPane;
    }

    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                hideSearchResults();
            } else {
                ObservableList<String> results = allStocks.filtered(
                    stock -> stock.toLowerCase().contains(newValue.toLowerCase())
                );
                
                searchResultsView.setItems(results);
                showSearchResults();
                
                if (searchField.isFocused()) {
                    searchResultsView.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
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
            if (!searchResultsView.isVisible()) return;

            switch (event.getCode()) {
                case UP:
                    navigateResults(-1);
                    event.consume();
                    break;
                case DOWN:
                    navigateResults(1);
                    event.consume();
                    break;
                case ENTER:
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
        String selected = searchResultsView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        LiveStock stock = LiveStock.getInstance(selected);
        stocksTable.getItems().add(stock);
        
        // Save to watchlist_instruments table
        var selectedWatchlist = watchlistContainer.getSelectedWatchlist();
        if (selectedWatchlist != null) {
            dbManager.saveWatchlistInstrument(selected, selectedWatchlist.getId());
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
} 