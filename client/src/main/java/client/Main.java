package client;

public class Main {
    public static void main(String[] args) {

        int port = 5000;

        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number : " + args[0] + ", using default port 5000");
            }
        }

        String serverUrl = "http://localhost:" + port;

        var facade = new ServerFacade(serverUrl);
        var repl = new Repl(facade);
        repl.run();
    }
}