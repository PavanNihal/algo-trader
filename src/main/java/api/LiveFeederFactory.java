package api;

import api.upstox.UpstoxFeedManager;
import authentication.Authenticator;
import authentication.Configuration;

public class LiveFeederFactory {
    private static LiveFeedManager instance;
    
    private LiveFeederFactory() {
        // Private constructor to prevent instantiation
    }
    
    public static LiveFeedManager getInstance() {
        if (instance == null) {
            Authenticator.BROKER broker = Configuration.getInstance().getBroker();
            if (broker == Authenticator.BROKER.UPSTOX) {
                instance = new UpstoxFeedManager();
            }
        }
        return instance;
    }
}
