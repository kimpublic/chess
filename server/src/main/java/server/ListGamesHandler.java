package server;

import com.google.gson.Gson;

import dataaccess.DataAccessException;

import service.ListGamesRequest;
import service.ListGamesResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class ListGamesHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public ListGamesHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            ListGamesRequest request = new ListGamesRequest(authToken);

            ListGamesResult result = userService.listGames(request);

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
