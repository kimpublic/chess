package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;


public class DataAccessOnMemory implements DataAccess {

    private static final Map<String, UserData> userData = new HashMap<>();
    private static final Map<String, AuthData> authData = new HashMap<>();
    private static final Map<String, GameData> gameData = new HashMap<>();

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

    @Override
    public void clearUserData() throws DataAccessException {
        try {
            userData.clear();
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear data on memory");
        }
    }

    @Override
    public void clearAuthData() throws DataAccessException {
        try {
            authData.clear();
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear data on memory");
        }
    }

    @Override
    public void clearGameData() throws DataAccessException {
        try {
            gameData.clear();
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear data on memory");
        }
    }

    // 여기서부터 추가로 작업해야함
    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (userData.containsKey(user.username())) {
            throw new DataAccessException("username already taken");
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
        throw new DataAccessException("Not implemented");
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        throw new DataAccessException("Not implemented");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        throw new DataAccessException("Not implemented");
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        throw new DataAccessException("Not implemented");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        throw new DataAccessException("Not implemented");
    }

}