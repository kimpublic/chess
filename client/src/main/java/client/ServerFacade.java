package client;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String baseUrl;
    private final Gson gson = new Gson();
    private String authToken;
    private String currentUsername;

    public ServerFacade(String url) {
        this.baseUrl = url;
    }

    public String getCurrentUsername() {
        return this.currentUsername;
    }

    public void register(String username, String password, String email) throws Exception {
        URI uri = new URI(baseUrl + "/user");
        HttpURLConnection http = (HttpURLConnection) new URL(uri.toString()).openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.addRequestProperty("Content-Type", "application/json");

        String body = gson.toJson(Map.of(
                "username", username,
                "password", password,
                "email", email
        ));

        // 요청 보내기
        try (OutputStream os = http.getOutputStream()) {
            os.write(body.getBytes());
        }

        int status = http.getResponseCode();
        InputStream is;
        if (status == 200) {
            is = http.getInputStream();
        } else {
            is = http.getErrorStream();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = gson.fromJson(new InputStreamReader(is), Map.class);

        if (status == 200) {
            this.authToken = (String) response.get("authToken");
            this.currentUsername = (String) response.get("username");
        } else {
            throw new RuntimeException("Register failed (" + response.get("message") + ")");
        }
    }

    public void login(String username, String password) throws Exception {
        URI uri = new URI(baseUrl + "/session");
        HttpURLConnection http = (HttpURLConnection) new URL(uri.toString()).openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.addRequestProperty("Content-Type", "application/json");

        String body = gson.toJson(Map.of(
                "username", username,
                "password", password
        ));

        try (OutputStream os = http.getOutputStream()) {
            os.write(body.getBytes());
        }

        int status = http.getResponseCode();
        InputStream is;
        if (status == 200) {
            is = http.getInputStream();
        } else {
            is = http.getErrorStream();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = gson.fromJson(new InputStreamReader(is), Map.class);

        if (status == 200) {
            this.authToken = (String) response.get("authToken");
            this.currentUsername = (String) response.get("username");
        } else {
            throw new RuntimeException("Login failed (" + response.get("message") + ")");
        }
    }

    public void logout() throws Exception {
        URI uri = new URI((baseUrl + "/session"));
        HttpURLConnection http = (HttpURLConnection) new URL(uri.toString()).openConnection();
        http.setRequestMethod("DELETE");
        http.addRequestProperty("Authorization", authToken);
        http.connect();

        int status = http.getResponseCode();
        if (status != 200) {
            InputStream is = http.getErrorStream();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = gson.fromJson(new InputStreamReader(is), Map.class);
            throw new RuntimeException("Logout failed (" + response.get("message") + ")");
        }

        this.authToken = null;
        this.currentUsername = null;
    }

    public void clearDatabase() throws Exception {
        URI uri = new URI((baseUrl + "/db"));
        HttpURLConnection http = (HttpURLConnection) new URL(uri.toString()).openConnection();
        http.setRequestMethod("DELETE");
        http.connect();

        int status = http.getResponseCode();
        if (status != 200) {
            InputStream is = http.getErrorStream();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = gson.fromJson(new InputStreamReader(is), Map.class);
            throw new RuntimeException("Database clearing failed (" + response.get("message") + ")");
        }
    }

    public int createGame(String gameName) throws Exception {
        URI uri = new URI((baseUrl + "/game"));
        HttpURLConnection http = (HttpURLConnection) new URL(uri.toString()).openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.addRequestProperty("Content-Type", "application/json");
        http.addRequestProperty("Authorization", authToken);

        String body = gson.toJson(Map.of("gameName", gameName));

        try (OutputStream os = http.getOutputStream()) {
            os.write(body.getBytes());
        }

        int status = http.getResponseCode();
        InputStream is;
        if (status == 200) {
            is = http.getInputStream();
        } else {
            is = http.getErrorStream();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = gson.fromJson(new InputStreamReader(is), Map.class);

        if (status == 200) {
            Double id = (double) response.get("gameID");
            return id.intValue();
        } else {
            throw new RuntimeException("Game creation failed (" + response.get("message") + ")");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listGames() throws Exception {
        URI uri = new URI((baseUrl + "/game"));
        HttpURLConnection http = (HttpURLConnection) new URL(uri.toString()).openConnection();
        http.setRequestMethod("GET");
        http.addRequestProperty("Authorization", authToken);

        http.connect();

        int status = http.getResponseCode();
        InputStream is;
        if (status == 200) {
            is = http.getInputStream();
        } else {
            is = http.getErrorStream();
        }

        Map<String, Object> response = gson.fromJson(new InputStreamReader(is), Map.class);

        if (status == 200) {
            return (List<Map<String,Object>>) response.get("games");
        } else {
            throw new RuntimeException("Game listing failed (" + response.get("message") + ")");
        }
    }

    public void joinGame(int gameID, String playerColor) throws Exception {
        URI uri = new URI((baseUrl + "/game"));
        HttpURLConnection http = (HttpURLConnection) new URL(uri.toString()).openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);

        http.addRequestProperty("Authorization", authToken);
        http.addRequestProperty("Content-Type", "application/json");

        String body = gson.toJson(Map.of(
                "gameID", gameID,
                "playerColor", playerColor
        ));

        try (OutputStream os = http.getOutputStream()) {
            os.write(body.getBytes());
        }

        int status = http.getResponseCode();
        InputStream is;
        if (status == 200) {
            is = http.getInputStream();
        } else {
            is = http.getErrorStream();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = gson.fromJson(new InputStreamReader(is), Map.class);

        if (status == 200) {
            return;
        } else {
            throw new RuntimeException("Joining game failed (" + response.get("message") + ")");
        }
    }


}
