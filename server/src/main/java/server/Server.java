package server;

import dataaccess.DataAccessOnMemory;
import service.ClearService;

import static spark.Spark.*;


public class Server {

    public int run(int portNumber) {
        port(portNumber);

        staticFiles.location("web");

        var dataAccessObject = new DataAccessOnMemory();
        var clearService = new ClearService(dataAccessObject);

        // Register your endpoints and handle exceptions here.

        delete("/db", new ClearHandler(clearService));

        //This line initializes the server and can be removed once you have a functioning endpoint 

        awaitInitialization();
        return port();
    }

    public void stop() {
        stop();
        awaitStop();
    }
}
