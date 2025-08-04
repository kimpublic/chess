package server;

// 이 클래스 역할
// `Session`을 통해 연결된 사용자 관리 (Map<gameID, List> 등)
// 메시지 수신 시 `UserGameCommand` 파싱 → 처리 → `ServerMessage` 생성 후 다시 브로드캐스트

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@WebSocket
public class WebSocketServer {

    private static final Logger logger = Logger.getLogger(WebSocketServer.class.getName());
    private static final Gson gson = new Gson();


    private static final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    private final GameService gameService;
    private Session session;
    private int currentGameID;

    public WebSocketServer(GameService gameService) {
        this.gameService = gameService;
    }

    @OnWebSocketConnect
    public void connectionConfirmed(Session session) {
        this.session = session;
        System.out.println("WebSocket connected");
    }

    @OnWebSocketMessage
    public void receiveMessage(Session session, String message) {

    }
}
