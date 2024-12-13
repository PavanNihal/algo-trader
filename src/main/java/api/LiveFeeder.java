package api;

import java.util.List;

import api.exceptions.FeederNotStartedException;

public interface LiveFeeder {
    
    void setAuthToken(String authToken);

    void start() throws FeederNotStartedException;

    void stop();

    void subscribe(List<String> instrumentKeys);

    void unsubscribe(List<String> instrumentKeys);
}
