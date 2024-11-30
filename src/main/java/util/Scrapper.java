package util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import model.Stock;

public class Scrapper {
    public static final String NIFTY_URL = "https://en.wikipedia.org/wiki/NIFTY_50";
    public static final String NSE_URL = "https://assets.upstox.com/market-quote/instruments/exchange/NSE.json.gz";

    // Write a method that scrapes the NIFTY_URL and returns a list of all the companies in the NIFTY_50 index.
    public static List<Stock> fetchNiftyList() {
        List<Stock> companies = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(NIFTY_URL).get();
            Element table = doc.getElementById("constituents");
            Elements rows = table.select("tr");
            
            for (Element row : rows) {
                Elements columns = row.select("td");
                if (!columns.isEmpty()) {
                    String name = columns.get(0).text(); // Company name is in first column
                    String symbol = columns.get(1).text(); // Symbol is in second column
                    companies.add(new Stock(symbol, name));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return companies;        
    }

    public static List<Stock> fetchStocksList() {
        List<Stock> stocks = new ArrayList<>();
        try {
            // Download and decompress the JSON data
            java.net.URL url = new java.net.URL(NSE_URL);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            
            // Read the gzipped content
            java.util.zip.GZIPInputStream gzipStream = new java.util.zip.GZIPInputStream(conn.getInputStream());
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(gzipStream));
            
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            
            // Parse JSON
            org.json.JSONArray jsonArray = new org.json.JSONArray(jsonContent.toString());
            
            // Iterate through JSON array and create Stock objects
            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject instrument = jsonArray.getJSONObject(i);

                if(!instrument.getString("instrument_type").equals("EQ") || (instrument.has("segment") && !instrument.getString("segment").equals("NSE_EQ"))) { 
                    continue;
                }
                
                Stock stock = new Stock(instrument.getString("trading_symbol"), instrument.getString("name"));
                stock.setExchange(instrument.optString("exchange"));
                stock.setIsin(instrument.optString("isin")); 
                stock.setInstrument_type(instrument.optString("instrument_type"));
                stock.setInstrument_key(instrument.optString("instrument_key"));
                stock.setLot_size(instrument.optInt("lot_size", 0));
                stock.setFreeze_quantity(instrument.optInt("freeze_quantity", 0));
                stock.setExchange_token(instrument.optString("exchange_token"));
                stock.setTick_size(instrument.getDouble("tick_size"));
                stock.setTrading_symbol(instrument.optString("trading_symbol"));
                stock.setShort_name(instrument.optString("short_name"));
                stock.setQty_multiplier(instrument.optInt("qty_multiplier", 0));
                stock.setSecurity_type(instrument.optString("security_type"));
                stock.setSegment(instrument.optString("segment"));
                stocks.add(stock);
            }

            reader.close();            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stocks;
    }

}
