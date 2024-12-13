package ui;

import java.util.function.Consumer;

import authentication.*;
import authentication.Authenticator.Status;
import database.DatabaseManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LoginPage extends VBox {

    private static final String LOGIN_MESSAGE = """
    Login with your preferred broker. 
    You will be redirected to the broker's login page.
    Once the authentication is succesful, return to this application to continue.
    """;
    private Consumer<Status> authenticationListener;

    public LoginPage() {
        super(20); // spacing between elements
        getStyleClass().add("login-container");
        
        // Load CSS file
        getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        // Welcome message
        Label welcomeLabel = new Label(LOGIN_MESSAGE);
        welcomeLabel.getStyleClass().add("welcome-label");
        welcomeLabel.setAlignment(Pos.CENTER);

        // Login button
        Button loginButton = new Button("Login to Upstox");
        loginButton.getStyleClass().add("login-button");

        // Status labels
        Label successLabel = new Label("Login Successful!");
        successLabel.getStyleClass().add("success-label");
        successLabel.setVisible(false);

        Label failureLabel = new Label("Authentication failed. Please try logging in again.");
        failureLabel.getStyleClass().add("failure-label");
        failureLabel.setVisible(false);

        Label fetchingLabel = new Label("Fetching Stocks...");
        fetchingLabel.getStyleClass().add("fetching-label");
        fetchingLabel.setVisible(false);

        // Add elements to the VBox
        getChildren().addAll(welcomeLabel, loginButton, successLabel, failureLabel, fetchingLabel);

        // Login button action
        loginButton.setOnAction(e -> {
            loginButton.setDisable(true);

            DatabaseManager dbManager = DatabaseManager.getInstance();
            UpstoxAuthImpl authenticator = new UpstoxAuthImpl();

            authenticator.addListener(status -> {
                if(status == Status.SUCCESS) {
                    successLabel.setVisible(true);
                    fetchingLabel.setVisible(true);

                    dbManager.loadStocks();
                    dbManager.loadNiftyList();

                    Configuration.getInstance().setBroker(Authenticator.BROKER.UPSTOX);
                    
                    // Notify the authentication listener
                    if (authenticationListener != null) {
                        authenticationListener.accept(status);
                    }
                } else {
                    failureLabel.setVisible(true);
                    loginButton.setDisable(false);
                    
                    // Notify the authentication listener of failure
                    if (authenticationListener != null) {
                        authenticationListener.accept(status);
                    }
                }
            });

            authenticator.authenticate();
        });
    }

    public void setAuthenticationListener(Consumer<Status> listener) {
        this.authenticationListener = listener;
    }
}
