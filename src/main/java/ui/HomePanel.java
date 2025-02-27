package ui;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;

import api.LiveFeedManager;
import database.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.SplitPane;
import ui.watchlist.WatchlistPane;

public class HomePanel extends BorderPane {
    private WatchlistPane watchlistPane;
    private PortfolioPane portfolioPane;
    private StrategyBuilderPane strategyBuilderPane;
    private SplitPane splitPane;
    
    public HomePanel(DatabaseManager dbManager, LiveFeedManager liveFeedManager) {
        // Create the left side panel with options
        VBox leftPanel = new VBox(10); // 10 pixels spacing
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #f0f0f0;");
        leftPanel.setPrefWidth(200);
        leftPanel.setAlignment(Pos.CENTER); // Center align vertically

        // Create spacer for top
        VBox topSpacer = new VBox();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        
        // Create buttons for each option
        Button watchlistBtn = new Button("Watchlist");
        Button portfolioBtn = new Button("Portfolio"); 
        Button strategyBtn = new Button("Strategy Builder");
        
        // Add spacing between buttons
        VBox buttonGroup = new VBox(15); // 15px spacing between buttons
        buttonGroup.setAlignment(Pos.CENTER);
        buttonGroup.getChildren().addAll(watchlistBtn, portfolioBtn, strategyBtn);

        // Create spacer for bottom
        VBox bottomSpacer = new VBox();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        // Style the buttons
        watchlistBtn.setMaxWidth(Double.MAX_VALUE);
        portfolioBtn.setMaxWidth(Double.MAX_VALUE);
        strategyBtn.setMaxWidth(Double.MAX_VALUE);

        leftPanel.getChildren().addAll(topSpacer, buttonGroup, bottomSpacer);

        // Initialize the content panes with token already available
        watchlistPane = new WatchlistPane(dbManager);
        watchlistPane.init(liveFeedManager); // Initialize with the feed manager right away
        
        portfolioPane = new PortfolioPane();
        strategyBuilderPane = new StrategyBuilderPane();

        // Create split pane for resizable boundary
        splitPane = new SplitPane();
        splitPane.getItems().addAll(leftPanel, watchlistPane);
        splitPane.setDividerPositions(0.2); // Initial divider position at 20%

        // Add click handlers to update only the right pane
        watchlistBtn.setOnAction(e -> {
            splitPane.getItems().set(1, watchlistPane);
        });
        portfolioBtn.setOnAction(e -> {
            splitPane.getItems().set(1, portfolioPane);
        });
        strategyBtn.setOnAction(e -> {
            splitPane.getItems().set(1, strategyBuilderPane);
        });

        setCenter(splitPane);
    }
}
