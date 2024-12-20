package ui;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.sql.SQLException;

import api.LiveFeedManager;
import api.LiveFeederFactory;
import authentication.AccessTokenExpiredException;
import database.DatabaseManager;
import javafx.geometry.Insets;
import javafx.scene.control.SplitPane;
import ui.watchlist.WatchlistPane;
public class HomePanel extends BorderPane {
    private WatchlistPane watchlistPane;
    private PortfolioPane portfolioPane;
    private StrategyBuilderPane strategyBuilderPane;
    private SplitPane splitPane;
    private DatabaseManager dbManager;

    public HomePanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
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
        watchlistPane = new WatchlistPane(dbManager);
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

    public void initializeAfterLogin() {
        LiveFeedManager liveFeedManager = LiveFeederFactory.getInstance();
        try {
            liveFeedManager.setAccessToken(dbManager.getToken());
        } catch (AccessTokenExpiredException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        watchlistPane.init(liveFeedManager);
    }
}
