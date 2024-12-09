package ui.watchlist;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.LiveStock;
import java.util.List;

public class StockTable extends TableView<LiveStock> {
    public StockTable() {
        this.getStyleClass().add("stocks-table");
        setupColumns();
        setupPlaceholder();
    }

    private void setupColumns() {
        TableColumn<LiveStock, String> nameCol = createNameColumn();
        TableColumn<LiveStock, Double> ltpCol = createLTPColumn();        
        this.getColumns().add(nameCol);
        this.getColumns().add(ltpCol);
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

    private void setupPlaceholder() {
        Label placeholder = new Label("Search and add stocks from the search bar above");
        placeholder.getStyleClass().add("table-placeholder");
        this.setPlaceholder(placeholder);
    }

    public void loadStocks(List<String> instrumentKeys) {
        ObservableList<LiveStock> stocksData = FXCollections.observableArrayList();
        for (String instrumentKey : instrumentKeys) {
            LiveStock stock = LiveStock.getInstance(instrumentKey);
            stocksData.add(stock);
        }
        this.setItems(stocksData);
    }
} 