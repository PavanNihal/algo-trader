package ui;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {

    private static final String LOGIN_PAGE = "LOGIN";
    private static final String MAIN_PAGE = "MAIN";

    
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    public MainFrame() {

        setTitle("Algo Trader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel);

        contentPanel.add(new LoginPage(), LOGIN_PAGE);
        contentPanel.add(new HomePanel(), MAIN_PAGE);
        
        cardLayout.show(contentPanel, LOGIN_PAGE);
    }
}
