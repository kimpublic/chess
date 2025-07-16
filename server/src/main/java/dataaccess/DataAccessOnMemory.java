package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataAccessOnMemory implements DataAccess {

    private final Map<String, UserData> userData = new HashMap<>();
    private final Map<String, AuthData> authData = new HashMap<>();
    private final Map<Integer, GameData> gameData = new HashMap<>();
    private int gameIDTracker = 0;

    @Override
    public void clearAll() throws DataAccessException {
        try {
            userData.clear();
            authData.clear();
            gameData.clear();
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear data on memory");
        }
    }

    // 여기서부터 추가로 작업해야함
    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (userData.containsKey(user.username())) {
            throw new DataAccessException("already taken");
        }
        userData.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userData.get(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authData.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authData.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (!authData.containsKey(authToken)) {
            throw new DataAccessException("unauthorized");
        }
        authData.remove(authToken);
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        int gameID = ++gameIDTracker;
        GameData gameStored = new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        gameData.put(gameID, gameStored);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameData.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(gameData.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!gameData.containsKey(game.gameID())) {
            throw new DataAccessException("game not found");
        }
        gameData.put(game.gameID(), game);
    }

}