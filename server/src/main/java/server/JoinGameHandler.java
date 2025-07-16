package server;

import com.google.gson.Gson;

import dataaccess.DataAccessException;

import service.GameService;
import service.JoinGameRequest;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");

            if (authToken == null || authToken.isBlank()) {
                throw new IllegalArgumentException("bad request");
            }

            Map<?,?> body = gson.fromJson(req.body(), Map.class);

            Object colorObj = body.get("playerColor");
            if (!(colorObj instanceof String) || ((String)colorObj).isBlank()) {
                throw new IllegalArgumentException("bad request");
            }
            String playerColor = (String) colorObj;

            Object idObj = body.get("gameID");
            if (!(idObj instanceof Number)) {
                throw new IllegalArgumentException("bad request");
            }
            int gameID = ((Number) idObj).intValue();


            JoinGameRequest request = new JoinGameRequest(authToken, gameID, playerColor);

            gameService.joinGame(request);
            res.status(200);
            return "{}";
        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            if ("unauthorized".equals(e.getMessage())) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            } else if ("already taken".equals(e.getMessage())) {
                res.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            } else {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}