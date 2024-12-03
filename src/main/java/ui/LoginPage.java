package ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import authentication.*;
import authentication.Authenticator.Status;
import database.DatabaseManager;

public class LoginPage extends JPanel {

    private static final String LOGIN_MESSAGE = """
            
            """;

    public LoginPage() {
        super(new BorderLayout());

        // Create a panel for centered content with vertical BoxLayout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        
        // Add welcome message
        JLabel welcomeLabel = new JLabel("Welcome to Algo Trader");
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(welcomeLabel);
        
        // Add some vertical spacing
        centerPanel.add(Box.createVerticalStrut(20));
        
        // Create login button
        JButton loginButton = new JButton("Login to Upstox");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(loginButton);
        
        // Add status labels (initially invisible)
        JLabel successLabel = new JLabel("Login Successful!");
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successLabel.setVisible(false);
        centerPanel.add(successLabel);

        JLabel failureLabel = new JLabel("Authentication failed. Please try logging in again.");
        failureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        failureLabel.setVisible(false);
        centerPanel.add(failureLabel);
        
        JLabel fetchingLabel = new JLabel("Fetching Stocks...");
        fetchingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fetchingLabel.setVisible(false);
        centerPanel.add(fetchingLabel);

        // Create a wrapper panel to center vertically
        JPanel verticalWrapper = new JPanel();
        verticalWrapper.setLayout(new BoxLayout(verticalWrapper, BoxLayout.Y_AXIS));
        verticalWrapper.add(Box.createVerticalGlue());
        verticalWrapper.add(centerPanel);
        verticalWrapper.add(Box.createVerticalGlue());

        // Create a wrapper panel to center horizontally
        JPanel horizontalWrapper = new JPanel();
        horizontalWrapper.setLayout(new BoxLayout(horizontalWrapper, BoxLayout.X_AXIS));
        horizontalWrapper.add(Box.createHorizontalGlue());
        horizontalWrapper.add(verticalWrapper);
        horizontalWrapper.add(Box.createHorizontalGlue());

        add(horizontalWrapper, BorderLayout.CENTER);
        
        // Add login button action
        loginButton.addActionListener(e -> {
            loginButton.setEnabled(false);
            
            DatabaseManager dbManager = DatabaseManager.getInstance();
            UpstoxAuthImpl authenticator = new UpstoxAuthImpl();

            authenticator.addListener(status -> {
                if(status == Status.SUCCESS) {
                    successLabel.setVisible(true);
                    fetchingLabel.setVisible(true);

                    dbManager.loadStocks();
                    dbManager.loadNiftyList();
                } else {
                    failureLabel.setVisible(true);
                    loginButton.setEnabled(true);
                }
            });
            
            dbManager.initDatabase();
            authenticator.authenticate();
        });
    }
}
