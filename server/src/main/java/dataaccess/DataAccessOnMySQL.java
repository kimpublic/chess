package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class DataAccessOnMySQL implements DataAccess {
    private final Gson gson = new Gson();

    public DataAccessOnMySQL() throws DataAccessException {

    }

    @Override
    public void clearAll() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {
            // 1) 외래 키 제약 해제
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 2) DELETE (TRUNCATE 대신)
            statement.executeUpdate("DELETE FROM tokens");
            statement.executeUpdate("DELETE FROM games");
            statement.executeUpdate("DELETE FROM users");

            // 3) AUTO_INCREMENT(자동 증가) 리셋
            statement.executeUpdate("ALTER TABLE tokens AUTO_INCREMENT = 1");
            statement.executeUpdate("ALTER TABLE games AUTO_INCREMENT = 1");
            statement.executeUpdate("ALTER TABLE users AUTO_INCREMENT = 1");

            // 4) 외래 키 제약 복원
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
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
                if (!response.next()) {return null;}
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
            INSERT INTO tokens(token, user_id, expires_at) VALUES (?, ?, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 HOUR))
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
        String statementFormat = """
            INSERT INTO games(
                game_name,
                state_json,
                white_id,
                black_id,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(statementFormat, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, game.gameName());
            statement.setString(2, gson.toJson(game.game()));
            if (game.whiteUsername() != null) {statement.setInt(3, lookupUserId(connection, game.whiteUsername()));}
            else {statement.setNull(3, Types.INTEGER);}
            if (game.blackUsername() != null) {statement.setInt(4, lookupUserId(connection, game.blackUsername()));}
            else {statement.setNull(4, Types.INTEGER);}
            statement.executeUpdate();
            try (var response = statement.getGeneratedKeys()) {
                if (response.next()) {return response.getInt(1);}
                else {throw new DataAccessException("Failed to create game");}
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String statementFormat = """
            SELECT
                game.game_id,
                game.game_name,
                game.state_json,
                white.username AS whiteUsername,
                black.username AS blackUsername,
                game.is_over
            FROM games game
            LEFT JOIN users white ON game.white_id = white.id
            LEFT JOIN users black ON game.black_id = black.id
            WHERE game.game_id = ?
        """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(statementFormat)) {
            statement.setInt(1, gameID);
            try (var response = statement.executeQuery()) {
                if (!response.next()) {return null;}
                ChessGame game = gson.fromJson(response.getString("state_json"), ChessGame.class);
                return new GameData(
                        response.getInt("game_id"),
                        response.getString("whiteUsername"),
                        response.getString("blackUsername"),
                        response.getString("game_name"),
                        game,
                        response.getBoolean("is_over")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String statementFormat = """
            SELECT
                game.game_id,
                game.game_name,
                game.state_json,
                white.username AS whiteUsername,
                black.username AS blackUsername,
            FROM games game
            LEFT JOIN users white ON game.white_id = white.id
            LEFT JOIN users black ON game.black_id = black.id
            WHERE game.is_over = FALSE
        """;
        Collection<GameData> games = new ArrayList<>();
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(statementFormat);
             var response = statement.executeQuery()) {
            while (response.next()) {
                ChessGame game = gson.fromJson(response.getString("state_json"), ChessGame.class);
                games.add(new GameData(
                        response.getInt("game_id"),
                        response.getString("whiteUsername"),
                        response.getString("blackUsername"),
                        response.getString("game_name"),
                        game,
                        response.getBoolean("is_over")
                ));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String statementFormat = "UPDATE games SET state_json = ?, white_id = ?, black_id = ?, is_over = ? WHERE game_id = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(statementFormat)) {
            statement.setString(1, gson.toJson(game.game()));
            if (game.whiteUsername() != null) {statement.setInt(2, lookupUserId(connection, game.whiteUsername()));}
            else {statement.setNull(2, Types.INTEGER);}
            if (game.blackUsername() != null) {statement.setInt(3, lookupUserId(connection, game.blackUsername()));}
            else {statement.setNull(3, Types.INTEGER);}
            statement.setBoolean(4, game.isOver());
            statement.setInt(5, game.gameID());
            int updatedNumbers = statement.executeUpdate();
            if (updatedNumbers == 0) {throw new DataAccessException("game does not exist");}
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }

    private int lookupUserId(Connection connection, String username) throws DataAccessException {
        String statementFormat = "SELECT id FROM users WHERE username = ?";
        try (var statement = connection.prepareStatement(statementFormat)) {
            statement.setString(1, username);
            try (var response = statement.executeQuery()) {
                if (response.next()) {return response.getInt("id");}
                else {throw new DataAccessException("User not found: " + username);}
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to lookup user ID", e);
        }
    }

    public String lookupUsernameWithAuth(String authToken) throws DataAccessException {
        AuthData auth = getAuth(authToken);
        return auth != null ? auth.username() : null;
    }


}
