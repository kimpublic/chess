package service;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;

import java.util.ArrayList;
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
            throw new IllegalArgumentException("bad request");
        }

        AuthData authToken = dataAccessObject.getAuth(request.authToken());
        if (authToken == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData gameStored = new GameData(0, null, null, request.gameName(), new ChessGame(), false);

        // DataAccessOnMemory에서 게임 생성 후 아이디 리턴한거 받아오기
        int createdGameID = dataAccessObject.createGame(gameStored);

        return new CreateGameResult(createdGameID);
    }

    public void joinGame(JoinGameRequest request) throws IllegalArgumentException, DataAccessException {
        if (request.authToken() == null || request.authToken().isBlank() || request.playerColor() == null || request.playerColor().isBlank()) {
            throw new IllegalArgumentException("bad request");
        }

        AuthData auth = dataAccessObject.getAuth(request.authToken());
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        GameData searchedGame = dataAccessObject.getGame(request.gameID());
        if (searchedGame == null) {
            throw new IllegalArgumentException("bad request");
        }

        String white = searchedGame.whiteUsername();
        String black = searchedGame.blackUsername();
        String player = auth.username();

        GameData updatedGameData;
        if (request.playerColor().equals("WHITE")) {
            if (white != null) {
                throw new DataAccessException("already taken");
            }
            updatedGameData = new GameData(
                    searchedGame.gameID(), player, black, searchedGame.gameName(), searchedGame.game(), searchedGame.isOver()
            );
        } else if (request.playerColor().equals("BLACK")) {
            if (black != null) {
                throw new DataAccessException("already taken");
            }
            updatedGameData = new GameData(
                    searchedGame.gameID(), white, player, searchedGame.gameName(), searchedGame.game(), searchedGame.isOver()
            );
        } else {
            throw new IllegalArgumentException("bad request");
        }
        dataAccessObject.updateGame(updatedGameData);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return dataAccessObject.getGame(gameID);
    }

    public void makeMove(int gameID, ChessMove move) throws DataAccessException {
        try {
            GameData searchedGame = getGame(gameID);
            if (searchedGame.isOver()) {
                throw new DataAccessException("Game is already over.");
            }
            ChessGame game = searchedGame.game();


            ChessBoard board = game.getBoard();
            if (game.getTeamTurn() != board.getPiece(move.getStartPosition()).getTeamColor()) {
                throw new DataAccessException("Invalid request");
            }
            game.makeMove(move);

            boolean isOverAfterMove = false;

            ChessGame.TeamColor nextTurn = game.getTeamTurn();
            if (game.isInCheckmate(nextTurn) || game.isInStalemate(nextTurn)) {
                isOverAfterMove = true;
            }

            GameData updatedGameData = new GameData(
                    searchedGame.gameID(),
                    searchedGame.whiteUsername(),
                    searchedGame.blackUsername(),
                    searchedGame.gameName(),
                    game,
                    isOverAfterMove);
            dataAccessObject.updateGame(updatedGameData);

        } catch (Exception e) {
            throw new DataAccessException("Failed making a move");
        }
    }

    public void resign(String authToken, int gameID) throws DataAccessException {
        try {
            GameData searchedGame = getGame(gameID);
            if (searchedGame.isOver()) {
                throw new DataAccessException("Game is already over.");
            }

            String username = dataAccessObject.lookupUsernameWithAuth(authToken);

            if (searchedGame.whiteUsername().equals(username) || searchedGame.blackUsername().equals(username)) {
                GameData updatedGameData = new GameData(
                        searchedGame.gameID(),
                        searchedGame.whiteUsername(),
                        searchedGame.blackUsername(),
                        searchedGame.gameName(),
                        searchedGame.game(),
                        true);
                dataAccessObject.updateGame(updatedGameData);
            } else {
                throw new DataAccessException("Unauthorized");
            }

        } catch (Exception e) {
            throw new DataAccessException("Failed resigning");
        }
    }

    public void leave(String authToken, int gameID) throws DataAccessException {
        try {
            GameData searchedGame = getGame(gameID);

            String username = dataAccessObject.lookupUsernameWithAuth(authToken);

            if (searchedGame.whiteUsername().equals(username) || searchedGame.blackUsername().equals(username)) {
                String whiteUsername = searchedGame.whiteUsername().equals(username) ? null : (searchedGame.whiteUsername());
                String blackUsername = searchedGame.blackUsername().equals(username) ? null : (searchedGame.blackUsername());
                GameData updatedGameData = new GameData(
                        searchedGame.gameID(),
                        whiteUsername,
                        blackUsername,
                        searchedGame.gameName(),
                        searchedGame.game(),
                        searchedGame.isOver());
                dataAccessObject.updateGame(updatedGameData);
            }

        } catch (Exception e) {
            throw new DataAccessException("Failed resigning");
        }
    }

}
