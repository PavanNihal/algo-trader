package ui;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.control.SplitPane;

public class HomePanel extends BorderPane {
    private WatchlistPane watchlistPane;
    private PortfolioPane portfolioPane;
    private StrategyBuilderPane strategyBuilderPane;
    private SplitPane splitPane;

    public HomePanel() {
        // Create the left side panel with options
        VBox leftPanel = new VBox(10); // 10 pixels spacing
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #f0f0f0;");
        leftPanel.setPrefWidth(200);

        // Create buttons for each option
        Button watchlistBtn = new Button("Watchlist");
        Button portfolioBtn = new Button("Portfolio"); 
        Button strategyBtn = new Button("Strategy Builder");

        // Style the buttons
        watchlistBtn.setMaxWidth(Double.MAX_VALUE);
        portfolioBtn.setMaxWidth(Double.MAX_VALUE);
        strategyBtn.setMaxWidth(Double.MAX_VALUE);

        leftPanel.getChildren().addAll(watchlistBtn, portfolioBtn, strategyBtn);

        // Initialize the content panes
        watchlistPane = new WatchlistPane();
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
