package client;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.Session;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.logging.Level;
import java.util.logging.Logger;


@WebSocket
public class WebSocketHandler {

    private Session session;
    private final Gson gson = new Gson();

    private static final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    @OnWebSocketConnect
    public void connectionConfirmed(Session session) {
        System.out.println("WebSocket connected");
        this.session = session;
    }

    @OnWebSocketMessage
    public void handleReceivedMessage(String msg) {
        System.out.println("Message received: " + msg);
        ServerMessage message = gson.fromJson(msg, ServerMessage.class);

        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameMessage loadGame = gson.fromJson(msg, LoadGameMessage.class);
                System.out.println("Received updated game information. Redrawing the board...");
                // TODO
                break;
            case ERROR:
                ErrorMessage error = gson.fromJson(msg, ErrorMessage.class);
                System.out.println("Error: " + error.getErrorMessage());
                break;
            case NOTIFICATION:
                NotificationMessage notification = gson.fromJson(msg, NotificationMessage.class);
                System.out.println("Notification: " + notification.getMessage());
                break;
        }
    }

    @OnWebSocketClose
    public void connectionClosed(int statusCode, String reason) {
        System.out.printf("WebSocket closed: [%d] %s%n", statusCode, reason);
    }

    @OnWebSocketError
    public void websocketError(Throwable e) {
        System.out.println("WebSocket error: " + e.getMessage());
        logger.log(Level.SEVERE, "WebSocket error", e);
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void send(Object object) {
        try {
            if (isOpen()) {
                session.getRemote().sendString(gson.toJson(object));
            } else {
                System.out.println("WebSocket is not opened.");
            }
        } catch (Exception e) {
            System.out.println("WebSocket error: " + e.getMessage());
            logger.log(Level.SEVERE, "WebSocket error", e);
        }
    }


}
