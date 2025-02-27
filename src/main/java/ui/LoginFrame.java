package ui;

import authentication.Authenticator.Status;
import database.DatabaseManager;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LoginFrame extends StackPane {
    private LoginPage loginPage;
    private Stage primaryStage;
    private DatabaseManager dbManager;

    public LoginFrame(Stage primaryStage, DatabaseManager dbManager) {
        this.primaryStage = primaryStage;
        this.dbManager = dbManager;
        
        loginPage = new LoginPage();
        getChildren().add(loginPage);

        // Register authentication listener
        loginPage.setAuthenticationListener(status -> {
            if (status == Status.SUCCESS) {
                try {
                    String token = dbManager.getToken();
                    launchMainApplication(token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Create the scene
        Scene scene = new Scene(this, 500, 350);
        String cssPath = getClass().getResource("/css/main.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Algo Trader - Login");
    }
    
    private void launchMainApplication(String token) {
        // Create and show the main application frame
        MainFrame mainFrame = new MainFrame(token, dbManager);
        Scene mainScene = mainFrame.getMainScene();
        
        // Apply the scene to the existing stage
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Algo Trader");
        // Don't maximize the window to keep it 40% smaller
    }
}