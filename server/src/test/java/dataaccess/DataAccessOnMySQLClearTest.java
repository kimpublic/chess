package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import chess.ChessGame;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessOnMySQLClearTest {

    private DataAccessOnMySQL dataAccessObject;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccessObject = new DataAccessOnMySQL();
        // 매 테스트마다 완전 초기화
        dataAccessObject.clearAll();
    }

    @Test
    void clearAllShouldNotThrow() {
        assertDoesNotThrow(() -> dataAccessObject.clearAll());
    }

    @Test
    void clearAllPositive() throws DataAccessException {
        // 1) 임시 데이터 삽입
        dataAccessObject.createUser(new UserData("minjoong", "pw", "minjoong@example.com"));
        String token = "token";
        dataAccessObject.createAuth(new AuthData(token, "minjoong"));
        ChessGame game = new ChessGame();
        dataAccessObject.createGame(new GameData(0, null, null, "Game1", game));

        // 삽입 확인
        assertNotNull(dataAccessObject.getUser("minjoong"),    "test user not found");
        assertNotNull(dataAccessObject.getAuth(token),      "test token not found");
        assertFalse(dataAccessObject.listGames().isEmpty(), "test game not found");

        // 2) clearAll() 호출
        dataAccessObject.clearAll();

        // 3) 실제로 모두 삭제되었는지 검증
        assertNull(dataAccessObject.getUser("minjoong"),       "clearAll() did not work for users");
        assertNull(dataAccessObject.getAuth(token),         "clearAll() did not work for tokens");
        Collection<GameData> games = dataAccessObject.listGames();
        assertTrue(games.isEmpty(),            "clearAll() did not work for games");
    }
}
