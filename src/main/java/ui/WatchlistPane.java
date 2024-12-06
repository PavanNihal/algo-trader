package ui;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.ObservableList;
import model.LiveStock;

    
    public class WatchlistPane extends SplitPane {
        private ListView<String> watchlistsView;
        private ObservableList<LiveStock> stocksData;
    
        public WatchlistPane() {
            // Initialize left pane with watchlists
            watchlistsView = new ListView<>();
            watchlistsView.getItems().addAll("Default Watchlist", "My Watchlist 1", "My Watchlist 2");
            watchlistsView.setPrefWidth(200);
            watchlistsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> loadWatchlistData(newValue)
            );
    
            // Initialize right pane with stocks table
            TableView<LiveStock> stocksTable = new TableView<>();
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
    
            // Add watchlistsView and stocksTable to SplitPane
            this.getItems().addAll(watchlistsView, stocksTable);
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