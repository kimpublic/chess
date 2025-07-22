package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collection;

public class DataAccessOnMySQL implements DataAccess {

    public DataAccessOnMySQL() throws DataAccessException {

    }

    @Override
    public void clearAll() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE users");
            statement.executeUpdate("TRUNCATE TABLE tokens");
            statement.executeUpdate("TRUNCATE TABLE games");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear data on MySQL");
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String statementFormat = "INSERT INTO users(username, hashed_password, email) VALUES (?, ?, ?)";
        var hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(statementFormat)) {
            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());
            statement.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("already taken", e);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statementFormat = "SELECT username, hashed_password, email FROM users WHERE username = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(statementFormat)) {
            statement.setString(1, username);
            try (var response = statement.executeQuery()) {
                if (!response.next()) return null;
                return new UserData(response.getString("username"), response.getString("hashed_password"), response.getString("email"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }


    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public int createGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {

    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {

    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }


}
