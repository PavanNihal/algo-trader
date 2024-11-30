package api;

import java.util.List;
import java.util.Map;

import com.upstox.ApiClient;
import com.upstox.ApiException;
import com.upstox.api.GetMarketQuoteOHLCResponse;
import com.upstox.api.MarketQuoteOHLC;
import com.upstox.auth.OAuth;

import database.DatabaseManager;

import com.upstox.Configuration;
import io.swagger.client.api.MarketQuoteApi;

import model.Stock;
import model.Interval;
public class APIUtil {
    
    public static Map<String, MarketQuoteOHLC> getOHLCQuotes(List<Stock> stocks, Interval interval) {

        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure OAuth2 access token for authorization: OAUTH2
        OAuth OAUTH2 = (OAuth) defaultClient.getAuthentication("OAUTH2");
        String token = DatabaseManager.getInstance().getToken();
        OAUTH2.setAccessToken(token);

        MarketQuoteApi apiInstance = new MarketQuoteApi();
        
        String symbol = stocks.stream()
            .map(Stock::getInstrument_key)
            .reduce((a, b) -> a + "," + b)
            .orElse(""); // String | Comma separated list of symbols

        System.out.println(symbol);
            
 // String | Interval to get ohlc data
        String apiVersion = "v2"; // String | API Version Header
        try {
            GetMarketQuoteOHLCResponse result = apiInstance.getMarketQuoteOHLC(symbol, interval.getValue(), apiVersion);
            return result.getData();
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketQuoteApi#getMarketQuoteOHLC");
            e.printStackTrace();
        }

        return null;
    }
}
