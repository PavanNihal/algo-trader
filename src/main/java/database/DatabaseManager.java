package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import authentication.AccessTokenExpiredException;

import model.Stock;
import util.Scrapper;

public class DatabaseManager {
    private static final String DB_FILE = "trading_app";  // Will create trading_app.mv.db
    private static final String CONNECTION_URL = "jdbc:h2:file:./" + DB_FILE;

    private static DatabaseManager instance;

    private DatabaseManager() {
        // Private constructor to prevent direct instantiation
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public void initDatabase() {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL)) {
            // Example table creation
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS auth_tokens (" +
                    "token VARCHAR(500) PRIMARY KEY, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS Nifty50 (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "symbol VARCHAR(50)" +
                    ")"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS instruments (" +
                    "name VARCHAR(255), " +
                    "exchange VARCHAR(50), " +
                    "isin VARCHAR(50), " +
                    "instrument_type VARCHAR(50), " +
                    "instrument_key VARCHAR(100), " +
                    "lot_size INT, " +
                    "freeze_quantity INT, " +
                    "exchange_token VARCHAR(50), " +
                    "tick_size DECIMAL(10,2), " +
                    "trading_symbol VARCHAR(50), " +
                    "short_name VARCHAR(50), " +
                    "qty_multiplier INT, " +
                    "security_type VARCHAR(50)" +
                    ")"
                );
            }                
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO auth_tokens (token) VALUES (?)")) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getToken() throws AccessTokenExpiredException, SQLException {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT token, created_at FROM auth_tokens");
            if (rs.next()) {
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp twentyFourHoursAgo = new Timestamp(System.currentTimeMillis() - (8 * 60 * 60 * 1000));
                
                if (createdAt.after(twentyFourHoursAgo)) {
                    return rs.getString("token");
                }
                // Delete expired token
                stmt.executeUpdate("DELETE FROM auth_tokens");
            }
            throw new AccessTokenExpiredException("Token expired");
        } catch (SQLException e) {
            throw e;
        }
    }

    public void saveStock(model.Stock stock) {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO Nifty50 (symbol, name) VALUES (?, ?)"
             )) {
            pstmt.setString(1, stock.getSymbol());
            pstmt.setString(2, stock.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveNiftyStocks(java.util.List<model.Stock> stocks) {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO Nifty50 (symbol) VALUES (?)"
             )) {
            for (model.Stock stock : stocks) {
                pstmt.setString(1, stock.getSymbol());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadStocks() {
        if(getStocksFromDB().isEmpty()) {
            List<Stock> stocks = Scrapper.fetchStocksList();
            saveStocks(stocks);
        }
    }

    public void saveStocks(java.util.List<model.Stock> stocks) {
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "MERGE INTO instruments KEY(trading_symbol) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
             )) {
            for (model.Stock stock : stocks) {
                pstmt.setString(1, stock.getName());
                pstmt.setString(2, stock.getExchange());
                pstmt.setString(3, stock.getIsin());
                pstmt.setString(4, stock.getInstrument_type());
                pstmt.setString(5, stock.getInstrument_key());
                pstmt.setInt(6, stock.getLot_size());
                pstmt.setInt(7, stock.getFreeze_quantity());
                pstmt.setString(8, stock.getExchange_token());
                pstmt.setDouble(9, stock.getTick_size());
                pstmt.setString(10, stock.getTrading_symbol());
                pstmt.setString(11, stock.getShort_name());
                pstmt.setInt(12, stock.getQty_multiplier());
                pstmt.setString(13, stock.getSecurity_type());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadNiftyList() {
        if(getNifty50FromDB().isEmpty()) {
            List<Stock> stocks = Scrapper.fetchNiftyList();
            saveNiftyStocks(stocks);
        }
    }

    public List<Stock> getNifty50FromDB() {
        List<Stock> stocks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT n.symbol, i.* FROM Nifty50 n " +
                "JOIN instruments i ON n.symbol = i.trading_symbol"
            );
            while (rs.next()) {
                String symbol = rs.getString("trading_symbol");
                String name = rs.getString("name");
                Stock stock = new Stock(symbol, name);
                stock.setIsin(rs.getString("isin"));
                stock.setExchange(rs.getString("exchange")); 
                stock.setInstrument_type(rs.getString("instrument_type"));
                stock.setInstrument_key(rs.getString("instrument_key"));
                stock.setLot_size(rs.getInt("lot_size"));
                stock.setFreeze_quantity(rs.getInt("freeze_quantity"));
                stock.setExchange_token(rs.getString("exchange_token"));
                stock.setTick_size(rs.getDouble("tick_size"));
                stock.setTrading_symbol(rs.getString("trading_symbol"));
                stock.setShort_name(rs.getString("short_name"));
                stock.setQty_multiplier(rs.getInt("qty_multiplier"));
                stock.setSecurity_type(rs.getString("security_type"));
                stocks.add(stock);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Nifty50 stocks loaded: " + stocks.size());
        return stocks;
    }

    public List<Stock> getStocksFromDB() {
        List<Stock> stocks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM instruments");
            while (rs.next()) {
                String symbol = rs.getString("trading_symbol");
                String name = rs.getString("name");
                Stock stock = new model.Stock(symbol, name);
                stock.setIsin(rs.getString("isin"));
                stock.setExchange(rs.getString("exchange"));
                stock.setInstrument_type(rs.getString("instrument_type"));
                stock.setInstrument_key(rs.getString("instrument_key"));
                stock.setLot_size(rs.getInt("lot_size"));
                stock.setFreeze_quantity(rs.getInt("freeze_quantity"));
                stock.setExchange_token(rs.getString("exchange_token"));
                stock.setTick_size(rs.getDouble("tick_size"));
                stock.setTrading_symbol(rs.getString("trading_symbol"));
                stock.setShort_name(rs.getString("short_name"));
                stock.setQty_multiplier(rs.getInt("qty_multiplier"));
                stock.setSecurity_type(rs.getString("security_type"));
                stocks.add(stock);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }
} 
