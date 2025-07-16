package server;

import com.google.gson.Gson;
import service.LoginRequest;
import service.LoginResult;
import service.UserService;
import spark.Request;
import spark.Response;

public class LoginHandler extends BaseHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected Object process(Request req, Response res) throws Exception {
        LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);
        LoginResult result = userService.login(request);
        return gson.toJson(result);
    }
}