package server;

import com.google.gson.Gson;
import service.ListGamesRequest;
import service.ListGamesResult;
import service.GameService;
import spark.Request;
import spark.Response;

public class ListGamesHandler extends BaseHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    protected Object process(Request req, Response res) throws Exception {
        // 인증 토큰 검증
        String authToken = requireAuth(req);

        // 게임 목록 조회
        ListGamesResult result = gameService.listGames(
                new ListGamesRequest(authToken)
        );

        // JSON 직렬화하여 반환
        return gson.toJson(result);
    }
}