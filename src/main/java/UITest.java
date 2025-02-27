import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.LoginFrame;

import java.sql.SQLException;

import database.DatabaseManager;

public class UITest extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Start H2 web server
            org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();

            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.initDatabase();
            
            // Start with the login frame
            new LoginFrame(primaryStage, dbManager);
            primaryStage.show();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        // Cleanup when application closes
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
