package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessOnMySQLAuthTest {

    private DataAccessOnMySQL dataAccessObject;
    private final String TOKEN = "token";
    private final String USERNAME = "minjoong";

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccessObject = new DataAccessOnMySQL();
        dataAccessObject.clearAll();
        // 기본 사용자 생성
        dataAccessObject.createUser(new UserData(USERNAME, "password", "minjoong@example.com"));
    }

    @Test
    void createAndGetAuthPositive() throws DataAccessException {
        // 토큰 생성
        dataAccessObject.createAuth(new AuthData(TOKEN, USERNAME));

        AuthData searched = dataAccessObject.getAuth(TOKEN);
        assertNotNull(searched, "created auth not found");
        assertEquals(TOKEN, searched.authToken(), "wrong token");
        assertEquals(USERNAME, searched.username(), "wrong username");
    }

    @Test
    void createAuthNegative() {
        // 존재하지 않는 사용자로 토큰 생성 시 예외
        DataAccessException e = assertThrows(
                DataAccessException.class,
                () -> dataAccessObject.createAuth(new AuthData("token2", "unknownUser")),
                "auth creation for not-valid user"
        );
        assertEquals("unauthorized", e.getMessage(), "something went wrong, we were supposed to get unauthorized but we did not.");
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        assertNull(dataAccessObject.getAuth("wrong-token"), "wrong token should return null");
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        dataAccessObject.createAuth(new AuthData(TOKEN, USERNAME));
        // 삭제
        assertDoesNotThrow(() -> dataAccessObject.deleteAuth(TOKEN), "deleteAuth throws something");
        assertNull(dataAccessObject.getAuth(TOKEN), "failed to delete auth");
    }

    @Test
    void deleteAuthNegative() {
        DataAccessException e = assertThrows(
                DataAccessException.class,
                () -> dataAccessObject.deleteAuth("invalid-token"),
                "deleteAuthNegative throws exception"
        );
        assertEquals("unauthorized", e.getMessage(), "exception message error");
    }
}
