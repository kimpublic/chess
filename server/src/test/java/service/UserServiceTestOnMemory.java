package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccessOnMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTestOnMemory {
    private UserService service;
    private DataAccessOnMemory dataAccessObject;

    @BeforeEach
    void testSetUp() {
        dataAccessObject = new DataAccessOnMemory();
        service = new UserService(dataAccessObject);
    }

    // register 테스트
    @Test
    void registerPositive() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("minjoong", "password", "minjoongKim98@naver.com");
        RegisterResult response = service.register(request);
        assertEquals("minjoong", response.username());
        assertNotNull(response.authToken());
    }

    // register-잘못된 요청
    @Test
    void registerBadRequest() {
        RegisterRequest badRegister = new RegisterRequest("","","");
        assertThrows(IllegalArgumentException.class, () -> {
            service.register(badRegister);
        });
    }

    // register-already taken 케이스
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

    // login 테스트
    @Test
    void loginPositive() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("minjoong", "password", "minjoongkim98@naver.com");
        service.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("minjoong","password");
        LoginResult loginResult = service.login(loginRequest);
        assertEquals("minjoong", loginResult.username());
        assertNotNull(loginResult.authToken());
    }

    // login-잘못된 요청
    @Test
    void loginBadRequest() throws IllegalArgumentException {
        LoginRequest badLogin = new LoginRequest("","");
        assertThrows(IllegalArgumentException.class, () -> {
            service.login(badLogin);
        });
    }

    // login-없는 아이디
    @Test
    void loginWrongUsername() throws DataAccessException {
        LoginRequest wrongUsername = new LoginRequest("not_valid_username", "hehe");
        try {
            service.login(wrongUsername);
            fail("test failed");
        } catch (DataAccessException e) {
            assertEquals("unauthorized", e.getMessage());
        }
    }

    // login-잘못된 패스워드
    @Test
    void loginWrongPassword() throws DataAccessException {
        RegisterRequest registerWrongPassword = new RegisterRequest("minjoong", "valid", "minjoongkim98@naver.com");
        service.register(registerWrongPassword);
        LoginRequest loginWrongPassword = new LoginRequest("minjoong", "not-valid");
        try {
            LoginResult loginResult = service.login(loginWrongPassword);
            fail("test failed");
        } catch (DataAccessException e) {
            assertEquals("unauthorized", e.getMessage());
        }
    }

    // logout 테스트
    @Test
    void logoutPositive() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("minjoongforlogout", "password", "test@gmail.com");
        service.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("minjoongforlogout", "password");
        LoginResult loginResult = service.login(loginRequest);
        String authToken = loginResult.authToken();

        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        assertDoesNotThrow(() -> service.logout(logoutRequest));
    }

    // logout-잘못된 요청
    @Test
    void logoutBadRequest() throws IllegalArgumentException {
        LogoutRequest badLogout = new LogoutRequest("");
        assertThrows(IllegalArgumentException.class, () -> {
            service.logout(badLogout);
        });
    }

    // logout-Unauthorized
    @Test
    void logoutNotValidToken() throws DataAccessException {
        LogoutRequest logoutNotValidToken = new LogoutRequest("not-valid-token");
        try {
            service.logout(logoutNotValidToken);
            fail("test failed");
        } catch (DataAccessException e) {
            assertEquals("unauthorized", e.getMessage());
        }
    }



}