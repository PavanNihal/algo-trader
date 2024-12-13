package api;

import authentication.Authenticator;
import authentication.Configuration;

public class LiveFeederFactory {
    private static LiveFeeder instance;
    
    private LiveFeederFactory() {
        // Private constructor to prevent instantiation
    }
    
    public static LiveFeeder getInstance() {
        if (instance == null) {
            Authenticator.BROKER broker = Configuration.getInstance().getBroker();
            if (broker == Authenticator.BROKER.UPSTOX) {
                instance = new UpstoxLiveFeeder();
            }
        }
        return instance;
    }
}
