import javax.swing.*;

import ui.MainFrame;

import java.sql.SQLException;


public class UITest extends JFrame {

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
