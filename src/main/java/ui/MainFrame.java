package ui;

import authentication.Authenticator.Status;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class MainFrame extends StackPane {

    private LoginPage loginPage;
    private HomePanel homePanel;

    public MainFrame() {
        loginPage = new LoginPage();
        homePanel = new HomePanel();

        // Add both pages to the StackPane
        getChildren().addAll(loginPage, homePanel);

        showLoginPage();

        // Register authentication listener
        loginPage.setAuthenticationListener(status -> {
            if (status == Status.SUCCESS) {
                showHomePage();
            }
        });

        // Create the scene
        new Scene(this, 1000, 800);
    }

    public void showLoginPage() {
        loginPage.setVisible(true);
        homePanel.setVisible(false);
    }

    public void showHomePage() {
        loginPage.setVisible(false);
        homePanel.setVisible(true);
    }
}
