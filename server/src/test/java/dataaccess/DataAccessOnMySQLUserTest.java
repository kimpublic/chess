package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessOnMySQLUserTest {

    private DataAccessOnMySQL dataAccessObject;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccessObject = new DataAccessOnMySQL();
        dataAccessObject.clearAll();
    }

    @Test
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("minjoong", "password", "minjoong@example.com");
        dataAccessObject.createUser(user);

        UserData fetched = dataAccessObject.getUser("minjoong");
        assertNotNull(fetched, "created user not found");
        assertEquals("minjoong", fetched.username(), "username mismatch");
        assertEquals("minjoong@example.com", fetched.email(), "email mismatch");
        assertNotEquals("password", fetched.password(), "password not hashed");
    }

    @Test
    void createUserNegative() throws DataAccessException {
        UserData user = new UserData("minjoong", "pw", "minjoong@example.com");
        dataAccessObject.createUser(user);

        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> dataAccessObject.createUser(user),
                "duplicate username should throw exception"
        );
        assertEquals("already taken", ex.getMessage(), "exception message mismatch");
    }

    @Test
    void getUserPositive() throws DataAccessException {
        UserData user = new UserData("minjoong", "password", "minjoong@example.com");
        dataAccessObject.createUser(user);

        UserData fetched = dataAccessObject.getUser("minjoong");
        assertNotNull(fetched, "existing user should be returned");
        assertEquals("minjoong", fetched.username(), "username mismatch");
        assertEquals("minjoong@example.com", fetched.email(), "email mismatch");
    }

    @Test
    void getUserNegative() throws DataAccessException {
        assertNull(dataAccessObject.getUser("minjoong"), "this user should return null");
    }
}
