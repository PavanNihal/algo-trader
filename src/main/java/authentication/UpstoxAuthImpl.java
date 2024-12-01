package authentication;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import java.awt.Desktop;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpsServer;

import database.DatabaseManager;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

public class UpstoxAuthImpl implements Authenticator {

    public static final String UPSTOX_LOGIN_LINK = "https://api.upstox.com/v2/login/authorization/dialog";
    public static final String UPSTOX_ACCESS_TOKEN_LINK = "https://api.upstox.com/v2/login/authorization/token";
    public static final String API_KEY = "97a70ebf-7347-4222-9de1-8efc4ea3a318";
    public static final String REDIRECT_URI = "https://localhost:8100/login";
    public static String SECRET_KEY;
    public static String KEYSTORE_PASSWORD;


    private AuthListener listener;
    
    @Override
    public void authenticate() {
        try {
            loadSecretKey();
            loadKeystorePassword();
        }
        catch(IOException e) {
            System.err.println("Error reading secret.txt file: " + e.getMessage());
            SECRET_KEY = null;
            KEYSTORE_PASSWORD = null;
            listener.onComplete(Status.FAILURE);
            return;
        }

        try {
            startLocalServer();
            openLoginPage();
        } catch(IOException | GeneralSecurityException | URISyntaxException e) {
            e.printStackTrace();
            listener.onComplete(Status.FAILURE);
        }

    }

    @Override
    public void addListener(AuthListener listener) {
        this.listener = listener;
    }

    public static void loadSecretKey() throws IOException {
        if(SECRET_KEY != null) {
            return;
        }
        SECRET_KEY = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("secret.txt"))).trim();        
    }

    public static void loadKeystorePassword() throws IOException {
        if(KEYSTORE_PASSWORD != null) {
            return;
        }
        KEYSTORE_PASSWORD = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("keystore_password.txt"))).trim();
    }

    public void openLoginPage() throws IOException, URISyntaxException {
        String authUrl = UPSTOX_LOGIN_LINK + 
            "?client_id=" + API_KEY +
            "&redirect_uri=" + REDIRECT_URI + 
            "&response_type=code";

        
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            throw new IOException("Desktop browsing is not supported");
        }
    }

    private void startLocalServer() throws GeneralSecurityException, IOException  {

        // Load the keystore
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream("keystore.jks");
        ks.load(fis, KEYSTORE_PASSWORD.toCharArray());

        // Setup the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, KEYSTORE_PASSWORD.toCharArray());

        // Setup the trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // Setup the HTTPS context and parameters
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        
        HttpsServer server = HttpsServer.create(new java.net.InetSocketAddress(8100), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                params.setSSLParameters(sslContext.getDefaultSSLParameters());
            }
        });

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
        System.out.println("Local HTTPS server started on port 8100");     
        
    }

    public void makeAccessTokenRequest(String code) throws IOException {

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
        }

        conn.disconnect();
        listener.onComplete(Status.SUCCESS);
    }
}
