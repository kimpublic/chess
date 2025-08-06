package server;

// 이 클래스 역할
// `Session`을 통해 연결된 사용자 관리 (Map<gameID, List> 등)
// 메시지 수신 시 `UserGameCommand` 파싱 → 처리 → `ServerMessage` 생성 후 다시 브로드캐스트

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class WebSocketServer {

    private static final Logger logger = Logger.getLogger(WebSocketServer.class.getName());
    private static final Gson gson = new Gson();


    private static final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    private static final Map<Session, Integer> sessionGameMap = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionTokenMap= new ConcurrentHashMap<>();

    private final GameService gameService;
    private final UserService userService;
    private Session session;
    private int currentGameID;

    public WebSocketServer(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketConnect
    public void connectionConfirmed(Session session) {
        this.session = session;
        System.out.println("WebSocket connected");
    }

    @OnWebSocketMessage
    public void receiveMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            this.currentGameID = command.getGameID();

            gameSessions.putIfAbsent(currentGameID, ConcurrentHashMap.newKeySet());
            gameSessions.get(currentGameID).add(session);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMove(session, command);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
            }
        } catch (Exception e) {
            System.out.println("WebSocket error: " + e.getMessage());
            logger.log(Level.SEVERE, "WebSocket error", e);
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws DataAccessException {
        sessionGameMap.put(session, command.getGameID());
        sessionTokenMap.put(session, command.getAuthToken());

        GameData gameData = gameService.getGame(command.getGameID());
        ChessGame game = gameData.game();
        send(session, new LoadGameMessage(game));
        String username = userService.getUsername(command.getAuthToken());
        String role;
        if (username.equals(gameData.whiteUsername())) {
            role = "a white player";   // 흰색 플레이어
        } else if (username.equals(gameData.blackUsername())) {
            role = "a black player";   // 검은색 플레이어
        } else {
            role = "an observer"; // 관전자
        }
        String msg = String.format("%s has joined as %s.", username, role);
        broadcastToOthers(session, currentGameID, new NotificationMessage(msg));
    }

    private void handleMove(Session session, UserGameCommand command) throws DataAccessException {
        try {
            gameService.makeMove(command.getGameID(), command.getMove());
            ChessGame updatedGame = gameService.getGame(command.getGameID()).game();
            broadcastToAll(currentGameID, new LoadGameMessage(updatedGame));
            broadcastToOthers(session, currentGameID, new NotificationMessage(command.getMove().getStartPosition() + "moved to " + command.getMove().getEndPosition()));
        } catch (IllegalStateException e) {
            send(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws DataAccessException {
        gameService.leave(command.getAuthToken(), command.getGameID());
        gameSessions.getOrDefault(currentGameID, Set.of()).remove(session);
        broadcastToOthers(session, currentGameID, new NotificationMessage(userService.getUsername(command.getAuthToken()) + " has left the game."));
    }

    private void handleResign(Session session, UserGameCommand command) throws DataAccessException {
        gameService.resign(command.getAuthToken(), command.getGameID());
        gameSessions.getOrDefault(currentGameID, Set.of()).remove(session);
        broadcastToAll(currentGameID, new NotificationMessage(userService.getUsername(command.getAuthToken()) + " has resigned. Game ended."));
    }

    private void send(Session session, Object message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (Exception e) {
            System.out.println("WebSocket error(Failed to send a message): " + e.getMessage());
            logger.log(Level.SEVERE, "WebSocket error(Failed to send a message)", e);
        }
    }

    private void broadcastToAll(int gameID, Object message) {
        for (Session session : gameSessions.getOrDefault(gameID, Set.of())) {
            send(session, message);
        }
    }

    private void broadcastToOthers(Session sender, int gameID, Object message) {
        for (Session session : gameSessions.getOrDefault(gameID, Set.of())) {
            if (!session.equals(sender)) {
                send(session, message);
            }
        }
    }

    @OnWebSocketClose
    public void connectionClosed(Session session, int statusCode, String reason) {
        Integer gameID = sessionGameMap.remove(session);
        String  token  = sessionTokenMap.remove(session);

        if (gameID != null && token != null) {
            try {
                gameService.leave(token, gameID);
                String username = userService.getUsername(token);
                broadcastToOthers(session, gameID,
                        new NotificationMessage(username + " has left the game."));
            } catch (DataAccessException e) {
                logger.log(Level.WARNING, "auto-LEAVE failed on close", e);
            }
            gameSessions.getOrDefault(gameID, Set.of()).remove(session);
        }
        System.out.printf("WebSocket closed: [%d] %s%n", statusCode, reason);
    }

    @OnWebSocketError
    public void websocketError(Throwable e) {
        System.out.println("WebSocket error: " + e.getMessage());
        logger.log(Level.SEVERE, "WebSocket error", e);
    }

}
