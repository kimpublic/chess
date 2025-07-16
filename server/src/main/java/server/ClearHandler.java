package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;


public class ClearHandler implements Route {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            clearService.clear();
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: failed to clear data"));
        }
    }

    private record ErrorMessage(String message) {}
}
