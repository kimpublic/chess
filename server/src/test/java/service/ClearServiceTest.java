//clear test
package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccessOnMySQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearServiceTest {
    private UserService service;
    private ClearService service1;
    private DataAccessOnMySQL dataAccessObject;

    @BeforeEach
    void testSetUp() throws DataAccessException {
        dataAccessObject = new DataAccessOnMySQL();
        service1 = new ClearService(dataAccessObject);
        service = new UserService(dataAccessObject);
        service1.clear();
    }

    @Test
    void clearShouldNotThrow() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("minjoong", "password", "minjoongkim98@naver.com");
        service.register(registerRequest);

        assertDoesNotThrow(() -> service1.clear());
        assertNull(dataAccessObject.getUser("minjoong"), "users not cleared");

    }

}