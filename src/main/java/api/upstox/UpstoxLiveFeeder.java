package api.upstox;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.upstox.ApiClient;
import com.upstox.ApiException;
import com.upstox.Configuration;
import com.upstox.api.WebsocketAuthRedirectResponse;
import com.upstox.auth.OAuth;
import com.upstox.marketdatafeeder.rpc.proto.MarketDataFeed;

import api.exceptions.FeederNotStartedException;
import io.swagger.client.api.WebsocketApi;

public class UpstoxLiveFeeder {
    private List<String> instrumentKeys;
    private List<String> pendingInstrumentKeys;
    private boolean isConnected;
    private WebSocketClient client;

    public UpstoxLiveFeeder(String accessToken) {
        try {
            ApiClient authenticatedClient = authenticateApiClient(accessToken);

            // Get authorized WebSocket URI for market data feed
            URI serverUri = getAuthorizedWebSocketUri(authenticatedClient);

            // Create and connect the WebSocket client
            this.client = createWebSocketClient(serverUri);
            this.client.connect();
        }
        catch(Exception e) {
            throw new FeederNotStartedException("Failed to start feeder");
        }
    }

    public void subscribe(String instrumentKey) {
        if(isConnected) {
            sendSubscriptionRequest(Arrays.asList(instrumentKey));
            this.instrumentKeys.add(instrumentKey);
        }
        else {
            this.pendingInstrumentKeys.add(instrumentKey);
        }
    }

    public void unsubscribe(String instrumentKey) {
        sendUnsubscriptionRequest(Arrays.asList(instrumentKey));
        this.instrumentKeys.remove(instrumentKey);
        this.pendingInstrumentKeys.remove(instrumentKey);        
    }

    public void subscribe(List<String> instrumentKeys) {
        sendSubscriptionRequest(instrumentKeys);
        this.instrumentKeys.addAll(instrumentKeys);
    }

    public List<String> getInstrumentKeys() {
        if(this.pendingInstrumentKeys.isEmpty()) {
            return this.instrumentKeys;
        }
        else {
            List<String> instrumentKeys = new ArrayList<>(this.instrumentKeys);
            instrumentKeys.addAll(this.pendingInstrumentKeys);
            return instrumentKeys;
        }        
    }

    public int getInstrumentCount() {
        return this.instrumentKeys.size() + this.pendingInstrumentKeys.size();
    }

    private static ApiClient authenticateApiClient(String accessToken) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        OAuth oAuth = (OAuth) defaultClient.getAuthentication("OAUTH2");
        oAuth.setAccessToken(accessToken);

        return defaultClient;
    }

    private static URI getAuthorizedWebSocketUri(ApiClient authenticatedClient) throws ApiException {
        WebsocketApi websocketApi = new WebsocketApi(authenticatedClient);
        WebsocketAuthRedirectResponse response = websocketApi.getMarketDataFeedAuthorize("2.0");

        return URI.create(response.getData()
                .getAuthorizedRedirectUri());
    }

    private WebSocketClient createWebSocketClient(URI serverUri) {
        return new WebSocketClient(serverUri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Opened connection");                
                isConnected = true;
                if(!pendingInstrumentKeys.isEmpty()) {
                    sendSubscriptionRequest(pendingInstrumentKeys);
                    pendingInstrumentKeys.clear();
                }
            }

            @Override
            public void onMessage(String message) {
                System.out.println("Received: " + message);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                handleBinaryMessage(bytes);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + ". Info: " + reason);
                isConnected = false;
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
    }

    private void sendSubscriptionRequest(List<String> instrumentKeys) {
        JsonObject requestObject = constructSubscriptionRequest(instrumentKeys);
        byte[] binaryData = requestObject.toString()
                .getBytes(StandardCharsets.UTF_8);

        System.out.println("Sending: " + requestObject);
        this.client.send(binaryData);
    }

    private void sendUnsubscriptionRequest(List<String> instrumentKeys) {
        JsonObject requestObject = constructUnsubscriptionRequest(instrumentKeys);
        byte[] binaryData = requestObject.toString()
                .getBytes(StandardCharsets.UTF_8);

        System.out.println("Sending: " + requestObject);
        this.client.send(binaryData);
    }

    private static JsonObject constructSubscriptionRequest(List<String> instrumentKeys) {
        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("mode", "full");

        JsonArray instrumentKeysjson = new Gson().toJsonTree(instrumentKeys)
                .getAsJsonArray();
        dataObject.add("instrumentKeys", instrumentKeysjson);

        JsonObject mainObject = new JsonObject();
        mainObject.addProperty("guid", "someguid");
        mainObject.addProperty("method", "sub");
        mainObject.add("data", dataObject);

        return mainObject;
    }

    private static JsonObject constructUnsubscriptionRequest(List<String> instrumentKeys) {
        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("mode", "full");

        JsonArray instrumentKeysjson = new Gson().toJsonTree(instrumentKeys)
                .getAsJsonArray();
        dataObject.add("instrumentKeys", instrumentKeysjson);

        JsonObject mainObject = new JsonObject();
        mainObject.addProperty("guid", "someguid");
        mainObject.addProperty("method", "unsub");
        mainObject.add("data", dataObject);

        return mainObject;
    }

    private static <FeedResponse> void handleBinaryMessage(ByteBuffer bytes) {
        System.out.println("Received: " + bytes);

        try {
            MarketDataFeed.FeedResponse feedResponse = MarketDataFeed.FeedResponse.parseFrom(bytes.array());

            // Convert the protobuf object to a JSON string
            String jsonFormat = feedResponse.toString();

            // Print the JSON string
            System.out.println(jsonFormat);

        } catch (InvalidProtocolBufferException e) {
            System.out.println("Received unparseable message");
            e.printStackTrace();
        }
    }
}

