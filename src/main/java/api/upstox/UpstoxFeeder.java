package api.upstox;

import java.util.List;

public interface UpstoxFeeder extends Runnable {
    void subscribe(String instrumentKey);
    void unsubscribe(String instrumentKey);
    void unsubscribe(List<String> instrumentKeys);
    void subscribe(List<String> instrumentKeys);
    List<String> getInstrumentKeys();
    int getInstrumentCount();
    void shutdown();
    boolean isRunning();
}
