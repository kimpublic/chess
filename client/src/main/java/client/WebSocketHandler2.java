package client;

import com.google.gson.Gson;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.*;


@ClientEndpoint
public class WebSocketHandler2 {

    private Session session;
    private final Gson gson = new Gson();
    private final Console console;

    public WebSocketHandler2(Console console) {
        this.console = console;
    }

    private static final Logger logger = Logger.getLogger(WebSocketHandler2.class.getName());

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket connected");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String msg) {
        // System.out.println("Message received: " + msg);
        ServerMessage message = gson.fromJson(msg, ServerMessage.class);

        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameMessage loadGame = gson.fromJson(msg, LoadGameMessage.class);
                console.onLoadGame(loadGame.getGame());
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

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.printf("WebSocket closed: [%d] %s%n", reason);
    }

    @OnError
    public void onError(Session session, Throwable e) {
        System.out.println("WebSocket error: " + e.getMessage());
        logger.log(Level.SEVERE, "WebSocket error", e);
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void send(Object object) {
        try {
            session.getAsyncRemote().sendText(gson.toJson(object));
        } catch (Exception e) {
            System.out.println("WebSocket error: " + e.getMessage());
            logger.log(Level.SEVERE, "WebSocket error", e);
        }
    }


}
