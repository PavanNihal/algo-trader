package ui.watchlist;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.LiveStock;
import model.Stock;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import api.LiveFeedManager;

public class StockTable extends TableView<LiveStockWrapper> {
    private LiveFeedManager liveFeedManager;
    private List<String> currentInstruments;
    
    public StockTable() {
        this.getStyleClass().add("stocks-table");
        setupColumns();
        setupPlaceholder();
        this.currentInstruments = new ArrayList<>();
    }

    private void setupColumns() {
        @SuppressWarnings("unchecked")
        TableColumn<LiveStockWrapper, ?>[] columns = new TableColumn[] {
            createNameColumn(),
            createSymbolColumn(),
            createLTPColumn()
        };
        this.getColumns().addAll(columns);
    }

    private TableColumn<LiveStockWrapper, String> createNameColumn() {
        TableColumn<LiveStockWrapper, String> nameCol = new TableColumn<>("Stock Name");
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStock().getName()));
        nameCol.setPrefWidth(200);
        return nameCol;
    }

    private TableColumn<LiveStockWrapper, String> createSymbolColumn() {
        TableColumn<LiveStockWrapper, String> symbolCol = new TableColumn<>("Trading Symbol");
        symbolCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStock().getTrading_symbol()));
        symbolCol.setPrefWidth(150);
        return symbolCol;
    }

    private TableColumn<LiveStockWrapper, Double> createLTPColumn() {
        TableColumn<LiveStockWrapper, Double> ltpCol = new TableColumn<>("LTP");
        ltpCol.setCellValueFactory(cellData -> 
            cellData.getValue().getLiveStock().ltpProperty().asObject());
        ltpCol.setPrefWidth(100);
        return ltpCol;
    }

    private void setupPlaceholder() {
        Label placeholder = new Label("Search and add stocks from the search bar above");
        placeholder.getStyleClass().add("table-placeholder");
        this.setPlaceholder(placeholder);
    }

    public void loadStocks(List<Stock> stocks) {
        if (!currentInstruments.isEmpty()) {
            liveFeedManager.unsubscribe(currentInstruments);
        }
        
        this.currentInstruments = new ArrayList<>(stocks.stream()
            .map(Stock::getInstrument_key)
            .collect(Collectors.toList()));
            
        this.liveFeedManager.subscribe(this.currentInstruments);
        ObservableList<LiveStockWrapper> stocksData = FXCollections.observableArrayList();
        for (Stock stock : stocks) {
            LiveStock liveStock = LiveStock.getInstance(stock.getInstrument_key());
            stocksData.add(new LiveStockWrapper(liveStock, stock));
        }
        this.setItems(stocksData);
    }

    public void setLiveFeedManager(LiveFeedManager liveFeedManager) {
        this.liveFeedManager = liveFeedManager;
    }

} 