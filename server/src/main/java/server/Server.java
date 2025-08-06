package server;

import dataaccess.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.Spark;
import dataaccess.DatabaseManager;
import server.WebSocketServer;


import static spark.Spark.*;


public class Server {

    public int run(int portNumber) {
        port(portNumber);

        webSocketIdleTimeoutMillis(600_000);

        DataAccess dataAccessObject;

        try {
            DatabaseManager.setupDatabase();
            dataAccessObject = new DataAccessOnMySQL();
        } catch (DataAccessException e) {
            System.err.println("Database setup failed: " +e.getMessage());
            throw new RuntimeException(e);
        }

        staticFiles.location("/web");

        // var dataAccessObject = new DataAccessOnMemory();
        var clearService = new ClearService(dataAccessObject);
        var userService = new UserService(dataAccessObject);
        var gameService = new GameService(dataAccessObject);

        // Register your endpoints and handle exceptions here.

        webSocket("/ws", new WebSocketServer(gameService, userService));

        delete("/db", new ClearHandler(clearService));

        post("/user", new RegisterHandler(userService));
        post("/session", new LoginHandler(userService));
        delete("/session", new LogoutHandler(userService));

        post("/game", new CreateGameHandler(gameService));
        get("/game", new ListGamesHandler(gameService));
        put("/game", new JoinGameHandler(gameService));

        //This line initializes the server and can be removed once you have a functioning endpoint 

        awaitInitialization();
        return port();
    }

    public void stop() {
        Spark.stop();
        awaitStop();
    }
}
