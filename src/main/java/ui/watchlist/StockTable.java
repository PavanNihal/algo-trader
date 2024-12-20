package ui.watchlist;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import model.LiveStock;
import model.Stock;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import api.LiveFeedManager;

public class StockTable extends TableView<LiveStockWrapper> {
    private LiveFeedManager liveFeedManager;
    private List<String> currentInstruments;

    @FXML
    private TableColumn<LiveStockWrapper, String> nameColumn;

    @FXML
    private TableColumn<LiveStockWrapper, String> symbolColumn;

    @FXML
    private TableColumn<LiveStockWrapper, Double> ltpColumn;
    
    public StockTable() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StockTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.currentInstruments = new ArrayList<>();
        setupPlaceholder();
        setupColumnValueFactories();
    }

    @FXML
    private void initialize() {
        // FXML initialization code if needed
    }

    private void setupColumnValueFactories() {
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStock().getName()));
            
        symbolColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStock().getTrading_symbol()));
            
        ltpColumn.setCellValueFactory(cellData -> 
            cellData.getValue().getLiveStock().ltpProperty().asObject());
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