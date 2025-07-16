package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

public abstract class BaseHandler implements Route {
    protected final Gson gson = new Gson();

    @Override
    public final Object handle(Request req, Response res) {
        try {
            Object result = process(req, res);
            res.status(200);
            return result;
        } catch (IllegalArgumentException e) {
            res.status(400);
            return errorJson("bad request");
        } catch (DataAccessException e) {
            if ("unauthorized".equals(e.getMessage())) {
                res.status(401);
                return errorJson("unauthorized");
            }
            res.status(500);
            return errorJson(e.getMessage());
        } catch (Exception e) {
            res.status(500);
            return errorJson(e.getMessage());
        }
    }

    // 실제 핸들링 로직만 여기에 구현
    protected abstract Object process(Request req, Response res) throws Exception;

    // 공통 에러 응답
    private String errorJson(String msg) {
        return gson.toJson(Map.of("message", "Error: " + msg));
    }

    // 인증 토큰 꺼내기 + 검증
    protected String requireAuth(Request req) {
        String token = req.headers("authorization");
        if (token == null || token.isBlank()) {throw new IllegalArgumentException();}
        return token;
    }

    // body에서 필드 꺼내기 + 검증
    @SuppressWarnings("unchecked")
    protected String requireField(Request req, String field) {
        Map<String,?> body = gson.fromJson(req.body(), Map.class);
        Object v = body.get(field);
        if (!(v instanceof String) || ((String)v).isBlank()) {
            throw new IllegalArgumentException();
        }
        return (String)v;
    }
}
