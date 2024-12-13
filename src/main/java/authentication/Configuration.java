package authentication;

public class Configuration {
    private static Configuration instance;
    private Authenticator.BROKER broker;

    private Configuration() {
        // Private constructor to prevent instantiation
    }
    
    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public Authenticator.BROKER getBroker() {
        return broker;
    }

    public void setBroker(Authenticator.BROKER broker) {
        this.broker = broker;
    }
}
