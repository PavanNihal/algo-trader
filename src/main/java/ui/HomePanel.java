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
        leftPanel.getStyleClass().add("nav-panel");
        leftPanel.setPrefWidth(200);
        leftPanel.setAlignment(Pos.CENTER); // Center align vertically

        // Create spacer for top
        VBox topSpacer = new VBox();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        
        // Create buttons for each option
        Button watchlistBtn = new Button("Watchlist");
        Button portfolioBtn = new Button("Portfolio"); 
        Button strategyBtn = new Button("Strategy Builder");
        
        // Apply style classes to buttons
        watchlistBtn.getStyleClass().add("nav-button");
        portfolioBtn.getStyleClass().add("nav-button");
        strategyBtn.getStyleClass().add("nav-button");
        
        // Set the first button as selected by default
        watchlistBtn.getStyleClass().add("nav-button-selected");
        
        // Add spacing between buttons
        VBox buttonGroup = new VBox(5); // 5px spacing between buttons
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

        // Add click handlers to update only the right pane and handle button selection state
        watchlistBtn.setOnAction(e -> {
            // Update the content pane
            splitPane.getItems().set(1, watchlistPane);
            
            // Update button selection states
            watchlistBtn.getStyleClass().add("nav-button-selected");
            portfolioBtn.getStyleClass().remove("nav-button-selected");
            strategyBtn.getStyleClass().remove("nav-button-selected");
        });
        
        portfolioBtn.setOnAction(e -> {
            // Update the content pane
            splitPane.getItems().set(1, portfolioPane);
            
            // Update button selection states
            watchlistBtn.getStyleClass().remove("nav-button-selected");
            portfolioBtn.getStyleClass().add("nav-button-selected");
            strategyBtn.getStyleClass().remove("nav-button-selected");
        });
        
        strategyBtn.setOnAction(e -> {
            // Update the content pane
            splitPane.getItems().set(1, strategyBuilderPane);
            
            // Update button selection states
            watchlistBtn.getStyleClass().remove("nav-button-selected");
            portfolioBtn.getStyleClass().remove("nav-button-selected");
            strategyBtn.getStyleClass().add("nav-button-selected");
        });

        setCenter(splitPane);
    }
}
