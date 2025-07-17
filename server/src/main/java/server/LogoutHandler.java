package server;

import service.LogoutRequest;
import service.UserService;
import spark.Request;
import spark.Response;

public class LogoutHandler extends BaseHandler {
    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected Object process(Request req, Response res) throws Exception {
        // 인증 토큰 검증
        String authToken = requireAuth(req);

        // 로그아웃 요청 생성 및 처리
        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        userService.logout(logoutRequest);

        // 빈 JSON 응답 반환
        return "{}";
    }
}
