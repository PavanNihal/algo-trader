import javax.swing.*;

import com.upstox.api.MarketQuoteOHLC;

import api.APIUtil;
import database.DatabaseManager;
import model.Interval;
import model.Stock;
import ui.DetailsPage;
import ui.MainFrame;
import ui.MainPanel;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class UITest extends JFrame {

    private static final String MAIN_PAGE = "MAIN";
    private static final String DETAIL_PAGE = "DETAIL";
    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private List<Stock> companies;
    private Map<String, MarketQuoteOHLC> map;


    public UITest() {
        // Set up the frame
        setTitle("Nifty 50 Companies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        
        // Set up CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel);

        // Initialize companies and map
        DatabaseManager dbManager = DatabaseManager.getInstance();
        companies = dbManager.getNifty50FromDB();
        map = APIUtil.getOHLCQuotes(companies, Interval.ONE_DAY);

        // Create panels
        DetailsPage detailsPage = new DetailsPage(() -> cardLayout.show(contentPanel, MAIN_PAGE));
        MainPanel mainPanel = new MainPanel(companies, map);
        
        // Set up the stock selection listener
        mainPanel.setStockSelectionListener((stock, marketKey) -> {
            MarketQuoteOHLC quote = map.get(marketKey);
            detailsPage.updateDetails(stock, quote);
            cardLayout.show(contentPanel, DETAIL_PAGE);
        });

        // Create OK button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());
        buttonPanel.add(okButton);
        
        // Create a wrapper panel for main content and button
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.add(mainPanel, BorderLayout.CENTER);
        mainWrapper.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add panels to content panel
        contentPanel.add(mainWrapper, MAIN_PAGE);
        contentPanel.add(detailsPage, DETAIL_PAGE);
        
        // Show main page initially
        cardLayout.show(contentPanel, MAIN_PAGE);
    }

    public static void main(String[] args) {
        try {
            org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create and show the UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
