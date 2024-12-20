package api.upstox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import api.LiveFeedManager;

public class UpstoxFeedManager implements LiveFeedManager {

    private static final int MAX_INSTRUMENTS_PER_LIVE_FEEDER = 100;
    private static final int MAX_INSTRUMENTS_PER_STATIC_FEEDER = 500;

    private String accessToken;
    private final List<UpstoxFeeder> liveFeeders;
    private final Map<String, UpstoxFeeder> instrumentToFeederMap;
    private final List<String> pendingInstruments;
    private boolean isWorkingDay;
    private boolean isMarketOpen;
    private final Object lock = new Object();

    public UpstoxFeedManager() {
        this.liveFeeders = new ArrayList<>();
        this.instrumentToFeederMap = new HashMap<>();
        this.pendingInstruments = new ArrayList<>();
        this.isWorkingDay = isWorkingDay();
        this.isMarketOpen = isMarketOpen();
        System.out.println("isWorkingDay: " + isWorkingDay + ", isMarketOpen: " + isMarketOpen);
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void subscribe(List<String> instrumentKeys) {
        synchronized (lock) {
            int instrumentIndex = 0;
            int feederIndex = 0;
            for(; instrumentIndex < instrumentKeys.size(); ) {
                String instrumentKey = instrumentKeys.get(instrumentIndex);
                /* Instrument already subscribed to */
                if(instrumentToFeederMap.containsKey(instrumentKey)) {
                    continue;
                }

                if(feederIndex >= liveFeeders.size()) {
                    int remainingInstruments = instrumentKeys.size() - instrumentIndex;
                    int requiredFeeders = (int) Math.ceil((double)remainingInstruments / getMaxInstrumentsPerFeeder());
                    for(int i = 0; i < requiredFeeders; i++) {
                        if(isMarketOpen && isWorkingDay) {
                            new UpstoxLiveFeeder(accessToken, this);
                        }
                        else {
                            new UpstoxPeriodicFeeder(accessToken, this).run();
                        }                        
                    }

                    pendingInstruments.addAll(instrumentKeys.subList(instrumentIndex, instrumentKeys.size()));
                    return;
                }

                UpstoxFeeder feeder = liveFeeders.get(feederIndex);
                if(feeder.getInstrumentCount() < getMaxInstrumentsPerFeeder()) {
                    feeder.subscribe(instrumentKey);
                    instrumentToFeederMap.put(instrumentKey, feeder);
                    instrumentIndex++;
                }
                else {
                    feederIndex++;
                }
            }
        }
    }

    @Override
    public void unsubscribe(List<String> instrumentKeys) {
        synchronized (lock) {
            Map<UpstoxFeeder, List<String>> feederToInstrumentsMap = new HashMap<>();
            
            for(String instrumentKey : instrumentKeys) {
                UpstoxFeeder feeder = instrumentToFeederMap.get(instrumentKey);
                if(feeder == null) {
                    continue;
                }
                
                feederToInstrumentsMap.computeIfAbsent(feeder, k -> new ArrayList<>()).add(instrumentKey);
                instrumentToFeederMap.remove(instrumentKey);
            }
            
            for(Map.Entry<UpstoxFeeder, List<String>> entry : feederToInstrumentsMap.entrySet()) {
                UpstoxFeeder feeder = entry.getKey();
                feeder.unsubscribe(entry.getValue());
                
                // Shutdown periodic feeder if it has no instruments
                if (feeder instanceof UpstoxPeriodicFeeder && feeder.getInstrumentCount() == 0) {
                    ((UpstoxPeriodicFeeder) feeder).shutdown();
                }
            }
        }
    }

    public void onFeederConnected(UpstoxFeeder feeder) {
        synchronized (lock) {
            liveFeeders.add(feeder);
            if (!pendingInstruments.isEmpty()) {
                List<String> instrumentsToAdd = new ArrayList<>();
                int remainingCapacity = getMaxInstrumentsPerFeeder() - feeder.getInstrumentCount();
                
                int instrumentsToTake = Math.min(remainingCapacity, pendingInstruments.size());
                for (int i = 0; i < instrumentsToTake; i++) {
                    String instrumentKey = pendingInstruments.remove(0);
                    instrumentsToAdd.add(instrumentKey);
                    instrumentToFeederMap.put(instrumentKey, feeder);
                }
                
                if (!instrumentsToAdd.isEmpty()) {
                    feeder.subscribe(instrumentsToAdd);
                }
            }
        }
    }

    public void onFeederDisconnected(UpstoxFeeder feeder) {
        synchronized (lock) {
            liveFeeders.remove(feeder);
            List<String> disconnectedInstruments = new ArrayList<>(feeder.getInstrumentKeys());
            pendingInstruments.addAll(disconnectedInstruments);
            
            // Remove disconnected instruments from the map
            for (String instrument : disconnectedInstruments) {
                instrumentToFeederMap.remove(instrument);
            }
        }
    }

    private int getMaxInstrumentsPerFeeder() {
        return isMarketOpen && isWorkingDay ? MAX_INSTRUMENTS_PER_LIVE_FEEDER : MAX_INSTRUMENTS_PER_STATIC_FEEDER;
    }

    public boolean isWorkingDay() {
        try {
            // Get today's date in YYYY-MM-DD format
            LocalDate today = LocalDate.now();
            String todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Construct URL with date parameter
            URL url = new URL("https://api.upstox.com/v2/market/holidays?date=" + todayStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray holidays = jsonResponse.getJSONArray("data");

            // If there's no holiday data for today, it's a working day
            if (holidays.length() == 0) {
                return true;
            }

            // Check if all exchanges are closed
            JSONObject holiday = holidays.getJSONObject(0);
            JSONArray closedExchanges = holiday.getJSONArray("closed_exchanges");
            JSONArray openExchanges = holiday.getJSONArray("open_exchanges");
            
            // If there are open exchanges, it's a working day
            return openExchanges.length() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isMarketOpen() {
        try {
            // Get today's date in YYYY-MM-DD format
            LocalDate today = LocalDate.now();
            String todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Construct URL with date parameter
            URL url = new URL("https://api.upstox.com/v2/market/timings/" + todayStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray marketTimings = jsonResponse.getJSONArray("data");
            
            // Get current time in milliseconds
            long currentTimeMillis = System.currentTimeMillis();
            
            // Check if current time falls within any exchange's trading hours
            for (int i = 0; i < marketTimings.length(); i++) {
                JSONObject timing = marketTimings.getJSONObject(i);
                String exchange = timing.getString("exchange");
                
                // We're primarily interested in NSE/BSE for equity markets
                if (exchange.equals("NSE") || exchange.equals("BSE")) {
                    long startTime = timing.getLong("start_time");
                    long endTime = timing.getLong("end_time");
                    
                    if (currentTimeMillis >= startTime && currentTimeMillis <= endTime) {
                        return true;
                    }
                }
            }
            
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
