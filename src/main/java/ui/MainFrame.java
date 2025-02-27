package ui;

import api.LiveFeedManager;
import api.LiveFeederFactory;
import database.DatabaseManager;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class MainFrame extends StackPane {

    private HomePanel homePanel;
    
    public MainFrame(String authToken, DatabaseManager dbManager) {
        // Initialize LiveFeedManager with token
        LiveFeedManager liveFeedManager = LiveFeederFactory.getInstance();
        liveFeedManager.setAccessToken(authToken);
        
        // Initialize main application components with the token
        homePanel = new HomePanel(dbManager, liveFeedManager);
        
        // Add panel to the StackPane
        getChildren().add(homePanel);
        
        // Create the scene - 40% smaller than original 1200x800
        Scene scene = new Scene(this, 720, 480); // 60% of original size
        String cssPath = getClass().getResource("/css/main.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        setSceneRef(scene);
    }
    
    private Scene mainScene;
    
    private void setSceneRef(Scene scene) {
        this.mainScene = scene;
    }
    
    public Scene getMainScene() {
        return mainScene;
    }
}
