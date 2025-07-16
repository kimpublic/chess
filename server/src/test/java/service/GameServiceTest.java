package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccessOnMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private UserService userService;
    private DataAccessOnMemory dataAccessObject;

    @BeforeEach
    void testSetUp() {
        dataAccessObject = new DataAccessOnMemory();
        gameService = new GameService(dataAccessObject);
        userService = new UserService(dataAccessObject);
    }

    // createGame 테스트
    @Test
    void createGamePositive() throws DataAccessException {
        userService.register(new RegisterRequest("minjoong", "password", "test@gmail.com"));
        String authToken = userService.login(new LoginRequest("minjoong", "password")).authToken();

        CreateGameResult createGameResult = gameService.createGame(new CreateGameRequest(authToken, "Test Game"));
        assertEquals(1, createGameResult.gameID());
    }

    // createGame-잘못된 요청
    @Test
    void createGameBadRequest() {
        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(new CreateGameRequest("", "gameName")));
        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(new CreateGameRequest("authToken", "")));
    }

    // createGame-Unauthorized
    @Test
    void createGameUnauthorized() {
        try {
            gameService.createGame(new CreateGameRequest("not-valid-authToken", "gameName"));
            fail("test failed");
        } catch (DataAccessException e) {
            assertEquals("unauthorized", e.getMessage());
        }
    }

    // listGames 테스트
    @Test
    void listGamesPositive() throws DataAccessException {
        userService.register(new RegisterRequest("minjoong", "password", "test@gmail.com"));
        String authToken = userService.login(new LoginRequest("minjoong", "password")).authToken();

        CreateGameResult createGameResult1 = gameService.createGame(new CreateGameRequest(authToken, "Test Game1"));
        CreateGameResult createGameResult2 = gameService.createGame(new CreateGameRequest(authToken, "Test Game2"));
        assertEquals(1, createGameResult1.gameID());
        assertEquals(2, createGameResult2.gameID());

        ListGamesResult gameList = gameService.listGames(new ListGamesRequest(authToken));
        List<GameInfo> gameInfoList = gameList.gameInfoList();

        assertEquals(2, gameInfoList.size());
    }

    // listGames-empty list
    @Test
    void listGamesEmptyList() throws DataAccessException {
        userService.register((new RegisterRequest("minjoong", "password", "test@gmail.com")));
        String authToken = userService.login(new LoginRequest("minjoong", "password")).authToken();

        ListGamesResult gameList = gameService.listGames((new ListGamesRequest(authToken)));
        assertTrue(gameList.gameInfoList().isEmpty());
    }

    // listGames-잘못된 요청
    @Test
    void listGamesBadRequest() {
        assertThrows(IllegalArgumentException.class, () -> gameService.listGames(new ListGamesRequest("")));
    }

    // listGame-Unauthorized
    @Test
    void listGameUnauthorized() {
        try {
            gameService.listGames(new ListGamesRequest("not-valid-authToken"));
            fail("test failed");
        } catch (DataAccessException e) {
            assertEquals("unauthorized", e.getMessage());
        }
    }


}