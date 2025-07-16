package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccessOnMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService service;
    private DataAccessOnMemory dataAccessObject;

    @BeforeEach
    void testSetUp() {
        dataAccessObject = new DataAccessOnMemory();
        service = new UserService(dataAccessObject);
    }

    @Test
    void registerPositive() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("minjoong", "password", "minjoongKim98@naver.com");
        RegisterResult response = service.register(request);
        assertEquals("minjoong", response.username());
        assertNotNull(response.authToken());
    }

    // 잘못된 요청
    @Test
    void registerBadRequest() throws IllegalArgumentException {
        RegisterRequest bad = new RegisterRequest("","","");
        assertThrows(IllegalArgumentException.class, () -> {
            service.register(bad);
        });
    }

    // already taken 케이스
    @Test
    void registerAlreadyTaken() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("minjoong", "password", "minjoongKim98@naver.com");
        service.register(request);
        try {
            service.register(request);
            fail("test failed");
        } catch (DataAccessException e) {
            assertEquals("already taken", e.getMessage());
        }

    }

}