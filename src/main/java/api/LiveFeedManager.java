package api;

import java.util.List;

public interface LiveFeedManager {
    void setAccessToken(String accessToken);
    void subscribe(List<String> instrumentKeys);
    void unsubscribe(List<String> instrumentKeys);
}
