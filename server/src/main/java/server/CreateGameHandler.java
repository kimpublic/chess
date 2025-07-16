package server;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import dataaccess.DataAccessException;

import service.LoginRequest;
import service.UserService;
import service.GameService;
import service.CreateGameRequest;
import service.CreateGameResult;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class CreateGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            JsonObject json = gson.fromJson(req.body(), JsonObject.class);
            String gameName = json.get("gameName").getAsString();

            CreateGameRequest request = new CreateGameRequest(authToken, gameName);
            CreateGameResult result = gameService.createGame(request);

            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            if ("unauthorized".equals(e.getMessage())) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
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
