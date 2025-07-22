package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;


import java.util.UUID;

public class UserService {
    private final DataAccess dataAccessObject;

    public UserService(DataAccess dataAccessObject) {
        this.dataAccessObject = dataAccessObject;
    }

    public RegisterResult register(RegisterRequest request) throws IllegalArgumentException, DataAccessException {

        String username = request.username();
        String password = request.password();
        String email    = request.email();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("bad request");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("bad request");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("bad request");
        }

        if (dataAccessObject.getUser(request.username()) != null) {
            throw new DataAccessException("already taken");
        }

        UserData newUser = new UserData(request.username(), request.password(), request.email());
        dataAccessObject.createUser(newUser);
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, request.username());
        dataAccessObject.createAuth(auth); // 사실 create보단 save하는거지

        RegisterResult registerRequestResult = new RegisterResult(request.username(), authToken);

        return registerRequestResult;

    }

    public LoginResult login(LoginRequest request) throws IllegalArgumentException, DataAccessException {
        if (request.username() == null || request.username().isBlank() || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("bad request");
        }

        // 근데 내가 짠 Diagram에서는 먼저 아이디 체크하고 그다음에 패스워드 체크하는데.. 이렇게 해도 될려나? 이러면 달라지는거 아닌가?
        UserData searchedUserData = dataAccessObject.getUser(request.username());
        // if (searchedUserData == null || !searchedUserData.password().equals(request.password())) {
        if (searchedUserData == null || !BCrypt.checkpw(request.password(), searchedUserData.password())) {
            throw new DataAccessException("unauthorized");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, request.username());
        dataAccessObject.createAuth(auth);

        LoginResult loginRequestResult = new LoginResult(request.username(), authToken);

        return loginRequestResult;
    }

    public void logout(LogoutRequest request) throws IllegalArgumentException, DataAccessException {
        if (request.authToken() == null || request.authToken().isBlank()) {
            throw new IllegalArgumentException("bad request");
        }
        dataAccessObject.deleteAuth(request.authToken());
    }


}
