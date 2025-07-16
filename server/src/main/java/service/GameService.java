package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import model.AuthData;

import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;
import java.util.List;

public class GameService {
    private final DataAccess dataAccessObject;

    public GameService(DataAccess dataAccessObject) {
        this.dataAccessObject = dataAccessObject;
    }

    public ListGamesResult listGames(ListGamesRequest request) throws IllegalArgumentException, DataAccessException {
        if (request.authToken() == null || request.authToken().isBlank()) {
            throw new IllegalArgumentException("bad request");
        }

        AuthData returnedAuth = dataAccessObject.getAuth((request.authToken()));
        if (returnedAuth == null) {
            throw new DataAccessException("unauthorized");
        }

        List<GameInfo> gameInfoList = new ArrayList<>();

        for (GameData game : dataAccessObject.listGames()) {
            GameInfo gameinfo = new GameInfo(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()
            );
            gameInfoList.add(gameinfo);
        }

        return new ListGamesResult(gameInfoList);
    }
    public CreateGameResult createGame(CreateGameRequest request) throws IllegalArgumentException, DataAccessException {
        if (request.authToken() == null || request.authToken().isBlank() || request.gameName() == null || request.gameName().isBlank()) {
            throw new DuplicateFormatFlagsException("bad request");
        }

        AuthData authToken = dataAccessObject.getAuth(request.authToken());
        if (authToken == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData gameStored = new GameData(0, authToken.username(), null, request.gameName(), new ChessGame());

        // DataAccessOnMemory에서 게임 생성 후 아이디 리턴한거 받아오기
        int createdGameID = dataAccessObject.createGame(gameStored);

        return new CreateGameResult(createdGameID);
    }
}
