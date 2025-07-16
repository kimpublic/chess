package server;

import com.google.gson.Gson;

import dataaccess.DataAccessException;

import service.UserService;
import service.RegisterRequest;
import service.RegisterResult;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public class RegisterHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);
            RegisterResult result = userService.register(request);
            res.status(200);
            return gson.toJson(result);
        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            if ("already taken".equals(e.getMessage())) {
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
