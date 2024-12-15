package api.upstox;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import api.LiveFeedManager;

public class UpstoxFeedManager implements LiveFeedManager {

    private static final int MAX_INSTRUMENTS_PER_CONNECTION = 100;

    private String accessToken;
    private final List<UpstoxLiveFeeder> liveFeeders;
    private final Map<String, UpstoxLiveFeeder> instrumentToFeederMap;
    private final List<String> pendingInstruments;
    private final Object lock = new Object();

    public UpstoxFeedManager() {
        this.liveFeeders = new ArrayList<>();
        this.instrumentToFeederMap = new HashMap<>();
        this.pendingInstruments = new ArrayList<>();
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
                    int requiredFeeders = (int) Math.ceil((double)remainingInstruments / MAX_INSTRUMENTS_PER_CONNECTION);
                    for(int i = 0; i < requiredFeeders; i++) {
                        new UpstoxLiveFeeder(accessToken, this);
                    }

                    pendingInstruments.addAll(instrumentKeys.subList(instrumentIndex, instrumentKeys.size()));
                    return;
                }

                UpstoxLiveFeeder feeder = liveFeeders.get(feederIndex);
                if(feeder.getInstrumentCount() < MAX_INSTRUMENTS_PER_CONNECTION) {
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
            Map<UpstoxLiveFeeder, List<String>> feederToInstrumentsMap = new HashMap<>();
            
            for(String instrumentKey : instrumentKeys) {
                UpstoxLiveFeeder feeder = instrumentToFeederMap.get(instrumentKey);
                if(feeder == null) {
                    continue;
                }
                
                feederToInstrumentsMap.computeIfAbsent(feeder, k -> new ArrayList<>()).add(instrumentKey);
                instrumentToFeederMap.remove(instrumentKey);
            }
            
            for(Map.Entry<UpstoxLiveFeeder, List<String>> entry : feederToInstrumentsMap.entrySet()) {
                entry.getKey().unsubscribe(entry.getValue());
            }
        }
    }

    public void onFeederConnected(UpstoxLiveFeeder feeder) {
        synchronized (lock) {
            liveFeeders.add(feeder);
            if (!pendingInstruments.isEmpty()) {
                List<String> instrumentsToAdd = new ArrayList<>();
                int remainingCapacity = MAX_INSTRUMENTS_PER_CONNECTION - feeder.getInstrumentCount();
                
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

    public void onFeederDisconnected(UpstoxLiveFeeder feeder) {
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
}
