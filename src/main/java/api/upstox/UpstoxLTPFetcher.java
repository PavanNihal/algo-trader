package api.upstox;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.json.JSONObject;

import model.LiveStock;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * Issue: What if the number of instruments is greater than the number of instruments that can be fetched in a single request? (500)
 */

public class UpstoxLTPFetcher implements Runnable {
    private static final String LTP_API_URL = "https://api.upstox.com/v2/market-quote/ltp";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    private final UpstoxFeedManager upstoxFeedManager;
    private final String accessToken;
    private final AtomicBoolean running;
    private final HttpClient httpClient;
    private final BlockingQueue<List<String>> requestQueue;
    
    public UpstoxLTPFetcher(String accessToken, UpstoxFeedManager upstoxFeedManager) {
        this.accessToken = accessToken;
        this.upstoxFeedManager = upstoxFeedManager;
        this.running = new AtomicBoolean(false);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
        this.requestQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            upstoxFeedManager.onLTPFetcherConnected(this);
            
            while (running.get()) {
                try {
                    List<String> instruments = requestQueue.poll(30, TimeUnit.SECONDS);
                    if (instruments != null) {
                        fetchLTPData(instruments);
                    }
                } catch (InterruptedException e) {
                    if (!running.get()) {
                        break;
                    }
                }
            }
        } finally {
            upstoxFeedManager.onLTPFetcherDisconnected(this);
        }
    }

    /**
     * Fetches LTP data for the given instruments on demand
     * @param instruments List of instrument keys to fetch LTP for
     */
    public void fetchLTPData(List<String> instruments) {
        if (!running.get() || instruments.isEmpty()) {
            return;
        }

        try {
            String response = fetchLTPFromAPI(instruments);
            processLTPResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes HTTP request to Upstox LTP API using Java's HttpClient
     * @param instruments List of instrument keys to fetch LTP for
     * @return Raw JSON response from the API
     * @throws Exception If the API request fails
     */
    private String fetchLTPFromAPI(List<String> instruments) throws Exception {
        String instrumentsParam = URLEncoder.encode(
            String.join(",", instruments),
            StandardCharsets.UTF_8
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(LTP_API_URL + "?instrument_key=" + instrumentsParam))
            .header("Accept", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .timeout(TIMEOUT)
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("Unexpected response code: " + response.statusCode());
        }
        
        return response.body();
    }

    /**
     * Processes the JSON response from LTP API and updates the LiveStock instances
     * @param jsonResponse Raw JSON response from the API
     */
    private void processLTPResponse(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            
            if ("success".equals(response.getString("status"))) {
                JSONObject data = response.getJSONObject("data");
                
                for (String key : data.keySet()) {
                    JSONObject instrumentData = data.getJSONObject(key);
                    double lastPrice = instrumentData.getDouble("last_price");
                    String instrumentKey = instrumentData.getString("instrument_token");
                    LiveStock.getInstance(instrumentKey).setLtp(lastPrice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shuts down the LTP fetcher
     */
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks if the LTP fetcher is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    public void queueFetchRequest(List<String> instruments) {
        if (running.get()) {
            requestQueue.offer(instruments);
        }
    }
}
