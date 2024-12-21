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
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpstoxPeriodicFeeder implements UpstoxFeeder {
    private static final String LTP_API_URL = "https://api.upstox.com/v2/market-quote/ltp";
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds refresh interval
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    private final CopyOnWriteArrayList<String> instrumentKeys;
    private final UpstoxFeedManager upstoxFeedManager;
    private final String accessToken;
    private final AtomicBoolean running;
    private final HttpClient httpClient;
    
    public UpstoxPeriodicFeeder(String accessToken, UpstoxFeedManager upstoxFeedManager) {
        this.accessToken = accessToken;
        this.upstoxFeedManager = upstoxFeedManager;
        this.instrumentKeys = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(false);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            upstoxFeedManager.onFeederConnected(this);
            
            while (running.get()) {
                try {
                    if (!instrumentKeys.isEmpty()) {
                        fetchAndProcessLTPData();
                    }
                    Thread.sleep(REFRESH_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            upstoxFeedManager.onFeederDisconnected(this);
        }
    }

    /**
     * Fetches LTP data for subscribed instruments and processes the response
     */
    private void fetchAndProcessLTPData() {
        try {
            String response = fetchLTPData(new ArrayList<>(instrumentKeys));
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
    private String fetchLTPData(List<String> instruments) throws Exception {
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
     * Processes the JSON response from LTP API and updates the feed manager
     * Uses Java's built-in JSON processing API
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

    @Override
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void subscribe(String instrumentKey) {
        if (!instrumentKeys.contains(instrumentKey)) {
            instrumentKeys.add(instrumentKey);
        }
    }

    @Override
    public void unsubscribe(String instrumentKey) {
        instrumentKeys.remove(instrumentKey);
    }

    @Override
    public void unsubscribe(List<String> keys) {
        instrumentKeys.removeAll(keys);
    }

    @Override
    public void subscribe(List<String> keys) {
        for (String key : keys) {
            if (!instrumentKeys.contains(key)) {
                instrumentKeys.add(key);
            }
        }
    }

    @Override
    public List<String> getInstrumentKeys() {
        return new ArrayList<>(instrumentKeys);
    }

    @Override
    public int getInstrumentCount() {
        return instrumentKeys.size();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
