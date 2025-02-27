package ui.watchlist;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import model.LiveStock;
import model.Stock;
import database.DatabaseManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import api.LiveFeedManager;

public class StockTable extends TableView<LiveStockWrapper> {
    private LiveFeedManager liveFeedManager;
    private List<String> currentInstruments;
    private int watchlistId;
    private Map<String, Double> previousLtpValues = new HashMap<>();

    @FXML
    private TableColumn<LiveStockWrapper, String> nameColumn;

    @FXML
    private TableColumn<LiveStockWrapper, String> symbolColumn;

    @FXML
    private TableColumn<LiveStockWrapper, Double> ltpColumn;

    @FXML
    private TableColumn<LiveStockWrapper, Node> deleteColumn;
    
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
            
        ltpColumn.setCellFactory(column -> new TableCell<LiveStockWrapper, Double>() {
            private final Timeline flashTimeline = new Timeline();
            
            {
                flashTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(800), event -> getStyleClass().removeAll("price-up-flash", "price-down-flash"))
                );
                flashTimeline.setCycleCount(1);
            }

            @Override
            protected void updateItem(Double ltp, boolean empty) {
                super.updateItem(ltp, empty);
                
                if (empty || ltp == null) {
                    setText(null);
                    return;
                }
                
                setText(String.format("%.2f", ltp));
                
                LiveStockWrapper wrapper = getTableView().getItems().get(getIndex());
                if (wrapper != null) {
                    String instrumentKey = wrapper.getStock().getInstrument_key();
                    Double previousLtp = previousLtpValues.get(instrumentKey);
                    
                    if (previousLtp != null && !previousLtp.equals(ltp)) {
                        getStyleClass().removeAll("price-up-flash", "price-down-flash");
                        
                        if (ltp > previousLtp) {
                            getStyleClass().add("price-up-flash");
                        } else if (ltp < previousLtp) {
                            getStyleClass().add("price-down-flash");
                        }
                        
                        flashTimeline.playFromStart();
                    }
                    
                    previousLtpValues.put(instrumentKey, ltp);
                }
            }
        });

        deleteColumn.setCellFactory(col -> {
            return new javafx.scene.control.TableCell<LiveStockWrapper, Node>() {
                private final Button deleteButton = new Button("×");
                private final HBox container = new HBox(deleteButton);
                {
                    deleteButton.getStyleClass().add("delete-button");
                    deleteButton.setVisible(false);
                    container.setAlignment(javafx.geometry.Pos.CENTER);
                    
                    // Show/hide button based on row hover
                    tableRowProperty().addListener((obs, oldRow, newRow) -> {
                        if (newRow != null) {
                            newRow.setOnMouseEntered(e -> {
                                if (getTableRow() != null && !isEmpty()) {
                                    deleteButton.setVisible(true);
                                }
                            });
                            newRow.setOnMouseExited(e -> deleteButton.setVisible(false));
                        }
                    });
                    
                    deleteButton.setOnAction(event -> {
                        LiveStockWrapper stockWrapper = getTableView().getItems().get(getIndex());
                        handleDeleteStock(stockWrapper);
                    });
                }

                @Override
                protected void updateItem(Node item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(container);
                        // Ensure button visibility matches row hover state
                        if (getTableRow() != null && getTableRow().isHover()) {
                            deleteButton.setVisible(true);
                        } else {
                            deleteButton.setVisible(false);
                        }
                    }
                }
            };
        });
    }

    private void handleDeleteStock(LiveStockWrapper stockWrapper) {
        Stock stock = stockWrapper.getStock();
        String instrumentKey = stock.getInstrument_key();
        
        // Remove LTP listener if it exists
        previousLtpValues.remove(instrumentKey);
        
        DatabaseManager.getInstance().deleteWatchlistInstrument(instrumentKey, watchlistId);
        getItems().remove(stockWrapper);
        currentInstruments.remove(instrumentKey);
        liveFeedManager.unsubscribe(List.of(instrumentKey));
    }

    private void setupPlaceholder() {
        Label placeholder = new Label("Search and add stocks from the search bar above");
        placeholder.getStyleClass().add("table-placeholder");
        this.setPlaceholder(placeholder);
    }

    public void loadStocks(List<Stock> stocks) {
        // Clear previous values
        previousLtpValues.clear();
        
        if (!currentInstruments.isEmpty()) {
            liveFeedManager.unsubscribe(currentInstruments);
        }
        
        this.currentInstruments = new ArrayList<>(stocks.stream()
            .map(Stock::getInstrument_key)
            .collect(Collectors.toList()));
            
        this.liveFeedManager.subscribe(this.currentInstruments);
        ObservableList<LiveStockWrapper> stocksData = FXCollections.observableArrayList();
        
        for (Stock stock : stocks) {
            String instrumentKey = stock.getInstrument_key();
            LiveStock liveStock = LiveStock.getInstance(instrumentKey);
            
            // Initialize previous LTP value
            previousLtpValues.put(instrumentKey, liveStock.getLtp());
            
            stocksData.add(new LiveStockWrapper(liveStock, stock));
        }
        
        this.setItems(stocksData);
    }

    public void setLiveFeedManager(LiveFeedManager liveFeedManager) {
        this.liveFeedManager = liveFeedManager;
    }

    public void setWatchlistId(int watchlistId) {
        this.watchlistId = watchlistId;
    }
} 