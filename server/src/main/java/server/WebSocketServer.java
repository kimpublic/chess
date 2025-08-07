package server;

// 이 클래스 역할
// `Session`을 통해 연결된 사용자 관리 (Map<gameID, List> 등)
// 메시지 수신 시 `UserGameCommand` 파싱 → 처리 → `ServerMessage` 생성 후 다시 브로드캐스트

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
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

    private static final Logger LOGGER = Logger.getLogger(WebSocketServer.class.getName());
    private static final Gson GSON = new Gson();


    private static final Map<Integer, Set<Session>> GAME_SESSIONS = new ConcurrentHashMap<>();

    private static final Map<Session, Integer> SESSION_GAME_MAP = new ConcurrentHashMap<>();
    private static final Map<Session, String> SESSION_TOKEN_MAP = new ConcurrentHashMap<>();

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
            UserGameCommand command = GSON.fromJson(message, UserGameCommand.class);
            this.currentGameID = command.getGameID();

            GAME_SESSIONS.putIfAbsent(currentGameID, ConcurrentHashMap.newKeySet());
            GAME_SESSIONS.get(currentGameID).add(session);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMove(session, command);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
            }
        } catch (Exception e) {
            System.out.println("WebSocket error: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "WebSocket error", e);
        }
    }

    private void handleConnect(Session session, UserGameCommand command) throws DataAccessException {
        SESSION_GAME_MAP.put(session, command.getGameID());
        SESSION_TOKEN_MAP.put(session, command.getAuthToken());

        GameData gameData = gameService.getGame(command.getGameID());
        ChessGame game = gameData.game();
        send(session, new LoadGameMessage(game, null));
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
        int gameID = command.getGameID();
        ChessMove move = command.getMove();

        try {
            gameService.makeMove(gameID, move);
            ChessGame updatedGame = gameService.getGame(gameID).game();
            broadcastToAll(currentGameID, new LoadGameMessage(updatedGame, move));

            String username = userService.getUsername(command.getAuthToken());
            String moveDescription = describeMove(command.getMove());
            String message = String.format("%s moved %s.", username, moveDescription);

            broadcastToOthers(session, currentGameID, new NotificationMessage(message));

            ChessGame.TeamColor nextTurn = updatedGame.getTeamTurn();

            if (updatedGame.isInCheckmate(nextTurn)) {
                broadcastToAll(currentGameID, new NotificationMessage(nextTurn + " is checkmated. Game over."));
            } else if (updatedGame.isInStalemate(nextTurn)) {
                broadcastToAll(currentGameID, new NotificationMessage(nextTurn + "Stalemate. Game drawn."));
            } else if (updatedGame.isInCheck(nextTurn)) {
                broadcastToAll(currentGameID, new NotificationMessage(nextTurn + " is in check."));
            }

        } catch (InvalidMoveException e) {
            send(session, new ErrorMessage("Error: Invalid move"));
        } catch (IllegalStateException e) {
            send(session, new ErrorMessage("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            send(session, new ErrorMessage("Error: server failed updating game." + e.getMessage()));
            LOGGER.log(Level.SEVERE, "Failed to persist move", e);
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws DataAccessException {
        int gameID = command.getGameID();
        gameService.leave(command.getAuthToken(), gameID);
        GAME_SESSIONS.getOrDefault(gameID, Set.of()).remove(session);
        broadcastToOthers(session, gameID, new NotificationMessage(">>> " + userService.getUsername(command.getAuthToken()) + " has left the game."));
    }

    private void handleResign(Session session, UserGameCommand command) throws DataAccessException {
        gameService.resign(command.getAuthToken(), command.getGameID());
        String username = userService.getUsername(command.getAuthToken());
        broadcastToAll(currentGameID, new NotificationMessage(">>> " + username + " has resigned. Game ended."));
        broadcastToAll(currentGameID, new NotificationMessage(">>> You can leave this room by typing \"leave\"."));
    }

    private void send(Session session, Object message) {
        try {
            session.getRemote().sendString(GSON.toJson(message));
        } catch (Exception e) {
            System.out.println("WebSocket error(Failed to send a message): " + e.getMessage());
            LOGGER.log(Level.SEVERE, "WebSocket error(Failed to send a message)", e);
        }
    }

    private void broadcastToAll(int gameID, Object message) {
        for (Session session : GAME_SESSIONS.getOrDefault(gameID, Set.of())) {
            send(session, message);
        }
    }

    private void broadcastToOthers(Session sender, int gameID, Object message) {
        for (Session session : GAME_SESSIONS.getOrDefault(gameID, Set.of())) {
            if (!session.equals(sender)) {
                send(session, message);
            }
        }
    }

    private String describeMove(ChessMove move) {
        char startCol = (char) ('A' + move.getStartPosition().getColumn() - 1);
        int startRow = move.getStartPosition().getRow();
        char endCol = (char) ('A' + move.getEndPosition().getColumn() - 1);
        int endRow = move.getEndPosition().getRow();
        return String.format("%c%d to %c%d", startCol, startRow, endCol, endRow);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Integer gameID = SESSION_GAME_MAP.remove(session);
        String  token  = SESSION_TOKEN_MAP.remove(session);

        if (gameID != null && token != null) {
            try {
                gameService.leave(token, gameID);
                String username = userService.getUsername(token);
                broadcastToOthers(session, gameID,
                        new NotificationMessage(username + " has left the game."));
            } catch (DataAccessException e) {
                LOGGER.log(Level.WARNING, "auto-LEAVE failed on close", e);
            }
            GAME_SESSIONS.getOrDefault(gameID, Set.of()).remove(session);
        }
        System.out.printf("WebSocket closed: [%d] %s%n", statusCode, reason);
    }

    @OnWebSocketError
    public void onError(Throwable e) {
        System.out.println("WebSocket error: " + e.getMessage());
        LOGGER.log(Level.SEVERE, "WebSocket error", e);
    }

}
