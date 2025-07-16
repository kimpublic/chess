package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccessObject;

    public UserService(DataAccess dataAccessObject) {
        this.dataAccessObject = dataAccessObject;
    }

    public RegisterResult register(RegisterRequest request) throws IllegalArgumentException, DataAccessException {


        if (request.username() == null || request.username().isBlank() || request.password() == null || request.password().isBlank() || request.email() == null || request.email().isBlank()) {
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

        RegisterResult requestResult = new RegisterResult(request.username(), authToken);

        return requestResult;

    }
}
