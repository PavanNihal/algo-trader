package authorisation;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import database.DatabaseManager;
import org.json.JSONObject;

public class AuthoriseUI {
    public static final String UPSTOX_LOGIN_LINK = "https://api.upstox.com/v2/login/authorization/dialog";
    public static final String UPSTOX_ACCESS_TOKEN_LINK = "https://api.upstox.com/v2/login/authorization/token";
    public static final String API_KEY = "97a70ebf-7347-4222-9de1-8efc4ea3a318";
    public static final String REDIRECT_URI = "http://localhost:8100/login";
    public static String SECRET_KEY;

    private Consumer<String> tokenCallback;


    public static void loadSecretKey() {
        try {
            SECRET_KEY = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("secret.txt"))).trim();
        } catch (IOException e) {
            System.err.println("Error reading secret.txt file: " + e.getMessage());
            SECRET_KEY = null;
        }
    }

    public void openLoginPage() {
        if(SECRET_KEY == null) {
            loadSecretKey();
        }
        startLocalServer();
        try {
            // Build the authorization URL with query parameters
            String authUrl = UPSTOX_LOGIN_LINK + 
                "?client_id=" + API_KEY +
                "&redirect_uri=" + REDIRECT_URI + 
                "&response_type=code";

            // Open the default browser with the authorization URL
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                System.out.println("Desktop browsing is not supported");
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void makeAccessTokenRequest(String code) {
        try {
            String authUrl = UPSTOX_ACCESS_TOKEN_LINK;

            URL url = new URL(authUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String postData = "code=" + code +
                "&client_id=" + API_KEY +
                "&client_secret=" + SECRET_KEY +
                "&redirect_uri=" + REDIRECT_URI +
                "&grant_type=authorization_code";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                String tokenString = jsonResponse.getString("access_token");
                System.out.println("Access token response: " + tokenString);
                DatabaseManager dbManager = DatabaseManager.getInstance();
                dbManager.saveToken(tokenString);
                if (tokenCallback != null) {
                    tokenCallback.accept(tokenString);
                }
            }

            conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startLocalServer() {
        try {
            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8100), 0);
            server.createContext("/login", (exchange -> {
                String query = exchange.getRequestURI().getQuery();
                
                String code = null;
                if (query != null && query.contains("code=")) {
                    code = query.substring(query.indexOf("code=") + 5);
                    System.out.println("Received authorization code: " + code);
                    makeAccessTokenRequest(code);                                              
                }

                String response = "Authorization successful! You can close this window.";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();                
                server.stop(0);
            }));
            
            server.setExecutor(null);
            server.start();
            System.out.println("Local server started on port 8100");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTokenCallback(Consumer<String> callback) {
        this.tokenCallback = callback;
    }


}
