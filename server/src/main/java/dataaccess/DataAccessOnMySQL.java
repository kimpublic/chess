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
        String findUserIdFormat = "SELECT id FROM users WHERE username = ?";
        String insertTokenFormat = """
            INSERT INTO tokens(token, user_id, expires_at) VALUES (?, ?, DATA_ADD(CURRENT_TIMESTAMP, INTERVAL 1 HOUR))
        """;

        try (var connection = DatabaseManager.getConnection();
             var findUserIdStatement = connection.prepareStatement(findUserIdFormat);
             var insertTokenStatement = connection.prepareStatement(insertTokenFormat)) {
            findUserIdStatement.setString(1, auth.username());
            try (var response = findUserIdStatement.executeQuery()) {
                if (!response.next()) {
                    throw new DataAccessException("unauthorized");
                }
                int userID = response.getInt("id");

                insertTokenStatement.setString(1, auth.authToken());
                insertTokenStatement.setInt(2, userID);
                insertTokenStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth", e);
        }

    }


    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String findUserIdFormat = """
            SELECT user_id FROM tokens WHERE token = ?
        """;

        String findUsernameFormat = """
            SELECT username FROM users WHERE id = ?        
        """;

        try (var connection = DatabaseManager.getConnection();
             var findUserIdStatement = connection.prepareStatement(findUserIdFormat);
             var findUsernameStatement = connection.prepareStatement(findUsernameFormat)) {
            findUserIdStatement.setString(1, authToken);
            try (var response = findUserIdStatement.executeQuery()) {
                if (!response.next()) {
                    return null;
                }
                int userID = response.getInt("user_id");

                findUsernameStatement.setInt(1, userID);
                try (var secondResponse = findUsernameStatement.executeQuery()) {
                    if (!secondResponse.next()) {
                        return null;
                    }
                    String username = secondResponse.getString("username");
                    return new AuthData(authToken, username);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String statementFormat = "DELETE FROM tokens WHERE token = ?";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(statementFormat)) {
            statement.setString(1, authToken);
            // 딜리트 명령으로 영향을 받은 행의 숫자를 돌려준다고 함
            int deletedNumbers = statement.executeUpdate();
            if (deletedNumbers == 0) {
                throw new DataAccessException("unauthorized");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth", e);
        }
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
