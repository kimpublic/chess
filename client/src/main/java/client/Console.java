package client;

import ui.EscapeSequences;

import java.util.Scanner;

public class Console {

    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);

    private boolean loggedIn = false;
    private boolean gameMode = false;
    private String cmd = "";

    public Console(ServerFacade facade) {
        this.facade = facade;
    }

    public void help() {
        if (!loggedIn) {
            System.out.println("Options:");
            System.out.println("Login as an existing user: \"login\" <USERNAME> <PASSWORD>");
            System.out.println("Register as a new user: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
            System.out.println("Exit the program: \"quit\"");
            System.out.println("Print this action options page: \"help\"");
        } else if (!gameMode) {
            System.out.println("Options:");
            System.out.println("List current games: \"list\"");
            System.out.println("Create a new game: \"create\" <GAME NAME>");
            System.out.println("Join a game: \"join\" <GAME ID> <COLOR TO PLAY: \"black\" or \"white\">");
            System.out.println("Watch a game: \"watch\" <GAME ID>");
            System.out.println("Logout: \"logout\"");
            System.out.println("Exit the program: \"quit\"");
            System.out.println("Print this action options page: \"help\"");
        } else {

        }
    }

    public void run() {
        System.out.print(EscapeSequences.ERASE_SCREEN);
        System.out.println("Welcome to the Chess game! Sign in or log in with your account to start");
        help();

        while (true) {
            String line = scanner.nextLine().trim();
            String[] parsed = line.split(" ", 2);
            cmd = parsed[0];

            switch (cmd) {
                case "quit": {
                    System.out.println("Hope you had a fun time with us. Good bye and see you next time!");
                    return;
                }
                case "help": {
                    help();
                    break;
                }
                case "register": {
                    try {
                        String arguments = parsed[1];
                        String[] argumentsParsed = arguments.split(" ");
                        if (argumentsParsed.length < 3) {
                            System.out.println("Usage: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
                            break;
                        }
                        facade.register(argumentsParsed[0], argumentsParsed[1], argumentsParsed[2]);
                        loggedIn = true;
                        System.out.println(">> Registered! You are now logged in.");
                        break;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "login": {
                    try {
                        String arguments = parsed[1];
                        String[] argumentsParsed = arguments.split(" ");
                        if (argumentsParsed.length < 2) {
                            System.out.println("Usage: \"login\" <USERNAME> <PASSWORD>");
                            break;
                        }
                        facade.login(argumentsParsed[0], argumentsParsed[1]);
                        loggedIn = true;
                        System.out.println(">> You are now logged in.");
                        help();
                        break;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "logout": {
                    if (!loggedIn) {
                        System.out.println(">> You are not logged in. Check the options");
                        help();
                        break;
                    }
                    try {
                        facade.logout();
                        System.out.println(">> You are now logged out. If you need help, type \"help\"");
                        break;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                case "list": {

                }
                case "create": {

                }
                case "join": {

                }
                case "watch": {

                }
                // 잘못된 커맨드인 경우에도 처리할 수 있도록
            }


        }

    }

}
