package ui;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.upstox.api.MarketQuoteOHLC;

import model.Stock;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

public class DetailsPage extends JPanel {
    private JLabel nameLabel;
    private JLabel symbolLabel;
    private JLabel priceLabel;

    public interface BackButtonListener {
        void onBackButtonClicked();
    }


    public DetailsPage(BackButtonListener listener) {
        super(new BorderLayout());
        
        // Create main panel with padding and box layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        
        // Add components to main panel vertically
        mainPanel.add(createButtonPanel(listener));
        mainPanel.add(createDetailsPanel());
        
        // Add main panel to the DetailsPage
        add(mainPanel, BorderLayout.CENTER);
    }

    public JPanel createButtonPanel(BackButtonListener listener) {
        // Add back button with left arrow
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> listener.onBackButtonClicked());
        
        // Create panel for back button and align it to the left
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.add(backButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, backButton.getPreferredSize().height));

        return buttonPanel;
    }

    public JPanel createDetailsPanel() {
        // Create a panel for stock details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Create inner panel for the details content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Initialize labels with custom fonts
        nameLabel = new JLabel("");
        nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 24));
        
        // Create a panel for symbol and price to be on same line
        JPanel symbolPricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        symbolLabel = new JLabel("");
        symbolLabel.setFont(new Font(symbolLabel.getFont().getName(), Font.BOLD, 18));
        priceLabel = new JLabel("");
        priceLabel.setFont(new Font(priceLabel.getFont().getName(), Font.PLAIN, 18));
        symbolPricePanel.add(symbolLabel);
        symbolPricePanel.add(priceLabel);
        symbolPricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components to content panel
        contentPanel.add(nameLabel);
        contentPanel.add(symbolPricePanel);
        
        // Add content panel to details panel with full width
        detailsPanel.add(contentPanel);

        return detailsPanel;
    }

    public void updateDetails(Stock stock, MarketQuoteOHLC quote) {
        nameLabel.setText(stock.getName());
        symbolLabel.setText(stock.getSymbol());
        priceLabel.setText("LTP: ₹" + quote.getLastPrice());
        
        revalidate();
        repaint();
    }
}
