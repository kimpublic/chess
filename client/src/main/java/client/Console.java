package client;

import ui.EscapeSequences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Console {

    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);

    private boolean loggedIn = false;
    private boolean gameMode = false;
    private String cmd = "";
    private List<Map<String,Object>> listOfGames = null;

    private Map<Integer,Integer> indexToGameID = new HashMap<>();

    public Console(ServerFacade facade) {
        this.facade = facade;
    }

    public void help() {
        if (!loggedIn) {
            System.out.println("Options:");
            System.out.println("Login as an existing user: \"login\" <USERNAME> <PASSWORD>");
            System.out.println("Register as a new user: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
            System.out.println("Exit the program: \"quit\"");
            System.out.println("Print this option page: \"help\"");
        } else if (!gameMode) {
            System.out.println("Options:");
            System.out.println("List current games: \"list\"");
            System.out.println("Create a new game: \"create\" <GAME NAME>");
            System.out.println("Join a game: \"join\" <GAME INDEX> <COLOR TO PLAY: \"black\" or \"white\">");
            System.out.println("Observe a game: \"observe\" <GAME CODE>");
            System.out.println("Logout: \"logout\"");
            System.out.println("Exit the program: \"quit\"");
            System.out.println("Print this option page: \"help\"");
        } else {
            System.out.println("gameMode to be implemented");
        }
    }

    public void drawBoard(String color) {

    }

    public void run() {
        System.out.print(EscapeSequences.ERASE_SCREEN);
        System.out.println("Welcome to the Chess game! Sign in or log in with your account to start.");
        help();

        while (true) {
            System.out.print(">>> ");
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
                        if (parsed.length != 2) {
                            System.out.println("Usage: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
                            break;
                        }
                        String arguments = parsed[1];
                        String[] argumentsParsed = arguments.split(" ");
                        if (argumentsParsed.length != 3) {
                            System.out.println("Usage: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
                            break;
                        }
                        facade.register(argumentsParsed[0], argumentsParsed[1], argumentsParsed[2]);
                        loggedIn = true;
                        System.out.println(">> Registered! You are now logged in.");
                        break;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                case "login": {
                    try {
                        if (parsed.length != 2) {
                            System.out.println("Usage: \"login\" <USERNAME> <PASSWORD>");
                            break;
                        }
                        String arguments = parsed[1];
                        String[] argumentsParsed = arguments.split(" ");
                        if (argumentsParsed.length != 2) {
                            System.out.println("Usage: \"login\" <USERNAME> <PASSWORD>");
                            break;
                        }
                        facade.login(argumentsParsed[0], argumentsParsed[1]);
                        loggedIn = true;
                        System.out.println(">> You are now logged in.");
                        help();
                        break;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
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
                        loggedIn = false;
                        break;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

                case "list": {
                    if (!loggedIn) {
                        System.out.println(">> You are not logged in. Check the options");
                        help();
                        break;
                    }
                    try {
                        indexToGameID.clear();
                        System.out.println(">> Current Game List: [INDEX]) [GAME NAME] [WHITE PLAYER NAME] [BLACK PLAYER NAME]");
                        listOfGames = facade.listGames();
                        if (listOfGames.isEmpty()) {
                            System.out.println("Currently, there is no game on the list. Create your own with \"create\" [GAME NAME]");
                        }
                        for (int i = 0; i < listOfGames.size(); i++) {
                            Map<String,Object> game = listOfGames.get(i);

                            String white = game.get("whiteUsername") != null
                                    && !((String)game.get("whiteUsername")).isBlank()
                                    ? (String)game.get("whiteUsername")
                                    : "empty";

                            String black = game.get("blackUsername") != null
                                    && !((String)game.get("blackUsername")).isBlank()
                                    ? (String)game.get("blackUsername")
                                    : "empty";

                            int indexNumber = i + 1;
                            int gameID = ((Number) game.get("gameID")).intValue();

                            indexToGameID.put(indexNumber, gameID);

                            System.out.printf(
                                    ">> %d) %s [White: %s, Black: %s]%n",
                                    indexNumber,
                                    game.get("gameName"),
                                    white,
                                    black
                            );
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }
                case "create": {
                    if (!loggedIn) {
                        System.out.println(">> You are not logged in. Check the options");
                        help();
                        break;
                    }

                    try {
                        if (parsed.length != 2) {
                            System.out.println("Usage: \"create\" <GAME NAME>");
                            break;
                        }
                        String gameName = parsed[1];
                        facade.createGame(gameName);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }
                case "join": {
                    if (!loggedIn) {
                        System.out.println(">> You are not logged in. Check the options");
                        help();
                        break;
                    }
                    String arguments = parsed[1];
                    String[] argumentsParsed = arguments.split(" ");
                    if (argumentsParsed.length != 2) {
                        System.out.println("Usage: \"join\" <GAME INDEX> <COLOR TO PLAY: \"black\" or \"white\">");
                        break;
                    }

                    int gameIndex;
                    try {
                        gameIndex = Integer.parseInt(argumentsParsed[0]);
                    } catch (NumberFormatException e) {
                        System.out.println(">> Game index must be in number.");
                        break;
                    }

                    if (!indexToGameID.containsKey(gameIndex)) {
                        System.out.println(">> Game index is not valid.");
                        break;
                    }

                    String chosenColor = argumentsParsed[1].toUpperCase();
                    if (!chosenColor.equals("WHITE") && !chosenColor.equals("BLACK")) {
                        System.out.println(">> Color should be either white or black.");
                        break;
                    }
                    try {
                        int gameID = indexToGameID.get(gameIndex);
                        facade.joinGame(gameID, chosenColor);
                        System.out.printf(">> You joined the game with index [ %d ]. Game loading ... %n", gameIndex);
                        gameMode = true;
                        // drawBoard
                        drawBoard(chosenColor);
                        break;
                    }  catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }
                case "observe": {
                    if (!loggedIn) {
                        System.out.println(">> You are not logged in. Check the options");
                        help();
                        break;
                    }

                    if (parsed.length != 2) {
                        System.out.println("Usage: \"observe\" <GAME INDEX>");
                        break;
                    }

                    int gameIndex;
                    try {
                        gameIndex = Integer.parseInt(parsed[1]);
                    } catch (NumberFormatException e) {
                        System.out.println(">> Game index must be in number.");
                        break;
                    }
                    if (!indexToGameID.containsKey(gameIndex)) {
                        System.out.println(">> Game index is not valid.");
                        break;
                    }
                    System.out.printf(">> You joined the game with index [ %d ] as an observer. Game loading ... %n", gameIndex);
                    // drawBoard
                    drawBoard("WHITE");
                    break;
                }

                default: System.out.println(">> Unknown command");
                // 잘못된 커맨드인 경우에도 처리할 수 있도록
            }


        }

    }

}
