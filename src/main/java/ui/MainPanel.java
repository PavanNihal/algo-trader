package ui;

import java.awt.*;
import java.util.Map;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.upstox.api.MarketQuoteOHLC;

import model.Stock;

public class MainPanel extends JPanel {
    private final List<Stock> companies;
    private final Map<String, MarketQuoteOHLC> map;
    private StockSelectionListener stockSelectionListener;

    public interface StockSelectionListener {
        void onStockSelected(Stock stock, String marketKey);
    }

    public MainPanel(List<Stock> companies, Map<String, MarketQuoteOHLC> map) {
        super(new BorderLayout());
        this.companies = companies;
        this.map = map;
        
        initializeUI();
    }

    public void setStockSelectionListener(StockSelectionListener listener) {
        this.stockSelectionListener = listener;
    }

    private void initializeUI() {
        // Create table with column headers
        String[] columnNames = {"Company Name", "Stock Symbol", "Open", "High", "Low", "Close", "LTP"};
        Object[][] data = new Object[companies.size()][7];
        
        // Populate table data
        for (int i = 0; i < companies.size(); i++) {
            Stock company = companies.get(i);
            data[i][0] = company.getName();
            data[i][1] = company.getSymbol();
            String key = company.getExchange() + "_" + company.getInstrument_type() + ":" + company.getSymbol();
            data[i][2] = map.get(key).getOhlc().getOpen();
            data[i][3] = map.get(key).getOhlc().getHigh();
            data[i][4] = map.get(key).getOhlc().getLow();
            data[i][5] = map.get(key).getOhlc().getClose();
            data[i][6] = map.get(key).getLastPrice();
        }
        
        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add click listener
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0 && stockSelectionListener != null) {
                    Stock company = companies.get(row);
                    String key = company.getExchange() + "_" + company.getInstrument_type() + ":" + company.getSymbol();
                    stockSelectionListener.onStockSelected(company, key);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // Update panel contents
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}
