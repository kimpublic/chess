package server;

import com.google.gson.Gson;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.GameService;
import spark.Request;
import spark.Response;

public class CreateGameHandler extends BaseHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    protected Object process(Request req, Response res) throws Exception {
        String authToken = requireAuth(req);
        String gameName  = requireField(req, "gameName");

        CreateGameResult result = gameService.createGame(
                new CreateGameRequest(authToken, gameName)
        );
        return gson.toJson(result);
    }
}
