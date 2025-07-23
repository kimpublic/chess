package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessOnMySQLGameTest {

    private DataAccessOnMySQL dataAccessObject;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccessObject = new DataAccessOnMySQL();
        dataAccessObject.clearAll();

        dataAccessObject.createUser(new UserData("minjoong", "password", "minjoong@example.com"));
        dataAccessObject.createUser(new UserData("sangjun", "password", "sangjun@example.com"));
    }

    @Test
    void createGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, null, null, "TestGame", game);
        int gameId = dataAccessObject.createGame(gameData);

        assertTrue(gameId > 0, "game creation failed");

        GameData searched = dataAccessObject.getGame(gameId);
        assertNotNull(searched, "created game not found");
        assertEquals(gameId, searched.gameID(), "gameId mismatched");
        assertEquals("TestGame", searched.gameName(), "gameName mismatched");
        assertNull(searched.whiteUsername(), "whiteUsername should be null on creation");
        assertNull(searched.blackUsername(), "blackUsername should be null on creation");
    }

    @Test
    void createGameNegative() {
        ChessGame game = new ChessGame();
        GameData bad = new GameData(0, "unknown", null, "X", game);
        DataAccessException e = assertThrows(
                DataAccessException.class,
                () -> dataAccessObject.createGame(bad),
                "creating game with invalid user"
        );
        assertTrue(e.getMessage().toLowerCase().contains("user not found"),
                "user not found");
    }

    @Test
    void getGamePositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, null, null, "G1", game);
        int idReturned = dataAccessObject.createGame(gameData);

        GameData searched = dataAccessObject.getGame(idReturned);
        assertNotNull(searched, "existing game did not return");
        assertEquals(idReturned, searched.gameID(), "gameId mismatched");
    }

    @Test
    void getGameNegative() throws DataAccessException {
        assertNull(dataAccessObject.getGame(9999), "wrong gameID is not supposed to return gameData");
    }

    @Test
    void listGamesEmptyList() throws DataAccessException {
        Collection<GameData> list = dataAccessObject.listGames();
        assertTrue(list.isEmpty(), "listGamesEmptyList test failed");
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        dataAccessObject.createGame(new GameData(0, null, null, "listGameTest1", new ChessGame()));
        dataAccessObject.createGame(new GameData(0, null, null, "listGameTest2", new ChessGame()));

        Collection<GameData> list = dataAccessObject.listGames();
        assertEquals(2, list.size(), "listGamesPositive test failed");
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        int id = dataAccessObject.createGame(
                new GameData(0, null, null, "updateGameTest", new ChessGame())
        );

        GameData updated = new GameData(id, "minjoong", "sangjun", "updateGameTest", new ChessGame());
        assertDoesNotThrow(() ->
                        dataAccessObject.updateGame(updated),
                "updateGamePositive test failed"
        );

        GameData fetched = dataAccessObject.getGame(id);
        assertEquals("minjoong", fetched.whiteUsername(),
                "whiteUsername mismatch after update");
        assertEquals("sangjun", fetched.blackUsername(),
                "blackUsername mismatch after update");
    }

    @Test
    void updateGameNegative() {
        ChessGame game = new ChessGame();
        GameData nonexist = new GameData(9999, null, null, "X", game);
        DataAccessException e = assertThrows(
                DataAccessException.class,
                () -> dataAccessObject.updateGame(nonexist),
                "updateGameNegative test failed (DataAccessException)"
        );
        assertEquals("game does not exist", e.getMessage(), "updateGameNegative test failed (got the wrong message)");
    }
}
