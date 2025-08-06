package server;

public class Main {
    public static void main(String[] args) {
        int port;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port, server will start with default port number 5000");
            }
        }
        port = 5000;
        Server server = new Server();
        server.run(port);
        System.out.println("Server started on port " + port);
    }
}