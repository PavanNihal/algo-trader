package api.upstox;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpstoxPeriodicFeeder implements UpstoxFeeder, Runnable {
    private final CopyOnWriteArrayList<String> instrumentKeys;
    private final UpstoxFeedManager upstoxFeedManager;
    private final String accessToken;
    private final Thread thread;
    private final AtomicBoolean running;
    private static final long REFRESH_INTERVAL = 30000; // 30 seconds refresh interval
    
    public UpstoxPeriodicFeeder(String accessToken, UpstoxFeedManager upstoxFeedManager) {
        this.accessToken = accessToken;
        this.upstoxFeedManager = upstoxFeedManager;
        this.instrumentKeys = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(true);
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run() {
        try {
            // Register with feed manager when thread starts
            upstoxFeedManager.onFeederConnected(this);
            
            while (running.get()) {
                try {
                    if (!instrumentKeys.isEmpty()) {
                        // TODO: Implement periodic data fetching for subscribed instruments
                        // Use accessToken to make API calls and update data
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
            // Deregister from feed manager when thread ends
            upstoxFeedManager.onFeederDisconnected(this);
        }
    }

    public void shutdown() {
        running.set(false);
        thread.interrupt();
        try {
            thread.join(REFRESH_INTERVAL * 2); // Wait for thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Ensure deregistration happens even if join fails or times out
            upstoxFeedManager.onFeederDisconnected(this);
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
}
