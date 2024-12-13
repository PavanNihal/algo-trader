package api.upstox;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import api.LiveFeedManager;

public class UpstoxFeedManager implements LiveFeedManager {

    private static final int MAX_INSTRUMENTS_PER_CONNECTION = 100;

    private String accessToken;
    private List<UpstoxLiveFeeder> liveFeeders;
    private Map<String, UpstoxLiveFeeder> instrumentToFeederMap;

    public UpstoxFeedManager() {
        this.liveFeeders = new ArrayList<>();
        this.instrumentToFeederMap = new HashMap<>();
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void subscribe(List<String> instrumentKeys) {
        if (liveFeeders.isEmpty()) {
            liveFeeders.add(new UpstoxLiveFeeder(accessToken));
        }

        int feederIndex = 0;
        for(String instrumentKey : instrumentKeys) {
            if(instrumentToFeederMap.containsKey(instrumentKey)) {
                continue;
            }
            if(liveFeeders.get(feederIndex).getInstrumentCount() >= MAX_INSTRUMENTS_PER_CONNECTION) {
                feederIndex++;
            }
            if(feederIndex >= liveFeeders.size()) {
                liveFeeders.add(new UpstoxLiveFeeder(accessToken));
            }
            liveFeeders.get(feederIndex).subscribe(instrumentKey);
            instrumentToFeederMap.put(instrumentKey, liveFeeders.get(feederIndex));
        }
    }

    @Override
    public void unsubscribe(List<String> instrumentKeys) {
        for(String instrumentKey : instrumentKeys) {
            UpstoxLiveFeeder feeder = instrumentToFeederMap.get(instrumentKey);
            if(feeder == null) {
                continue;
            }
            feeder.unsubscribe(instrumentKey);
            instrumentToFeederMap.remove(instrumentKey);
        }
    }
}
