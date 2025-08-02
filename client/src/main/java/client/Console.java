package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessGame.*;
import chess.ChessPiece;
import chess.ChessPiece.*;
import chess.ChessPosition;
import ui.EscapeSequences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Console {

    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);

    private boolean loggedIn = false;
    private boolean gameMode = false;
    private boolean observeMode = false;
    private String cmd = "";
    private List<Map<String,Object>> listOfGames = null;

    private Map<Integer,Integer> indexToGameID = new HashMap<>();

    private String[] alphabet = {
            "   ",
            "\u2003A ",
            "\u2003B ",
            "\u2003C ",
            "\u2003D ",
            "\u2003E ",
            "\u2003F ",
            "\u2003G ",
            "\u2003H ",
            "   "
    };

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
        } else if (gameMode) {
            System.out.println("Options:");
            System.out.println("Redraw the chess board: \"redraw\"");
            System.out.println("Leave the current game you are in: \"leave\"");
            System.out.println("Make a move of a peace: \"move\" <START POSITION> <DESTINATION>");
            System.out.println("Resign: \"resign\"");
            System.out.println("Highlight legal moves of a piece: \"highlight\" <PIECE LOCATION>");
            System.out.println("Print this option page: \"help\"");
        } else if (observeMode) {
            System.out.println("Options:");
            System.out.println("Redraw the chess board: \"redraw\"");
            System.out.println("Leave the current game you are in: \"leave\"");
            System.out.println("Print this option page: \"help\"");
        }
        else {
            System.out.println("Options:");
            System.out.println("List current games: \"list\"");
            System.out.println("Create a new game: \"create\" <GAME NAME>");
            System.out.println("Join a game: \"join\" <GAME INDEX> <COLOR TO PLAY: \"black\" or \"white\">");
            System.out.println("Observe a game: \"observe\" <GAME CODE>");
            System.out.println("Logout: \"logout\"");
            System.out.println("Exit the program: \"quit\"");
            System.out.println("Print this option page: \"help\"");
        }
    }

    public String returnPieceString(PieceType type, TeamColor color) {
        switch (type) {
            case KING:
                return color == TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN:
                return color == TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP:
                return color == TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT:
                return color == TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK:
                return color == TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN:
                return color == TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
            default:
                return EMPTY;
        }
    }

    public void drawBoard(String color) {
        boolean ifWhite = "WHITE".equals(color);
        ChessGame game = new ChessGame();

        ChessBoard board = game.getBoard();

        for (int col = 0; col < 10; col++) {
            String item = (ifWhite) ? alphabet[col] : alphabet[9 - col];
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + item + RESET_TEXT_COLOR + RESET_BG_COLOR);
        }
        System.out.println();

        String bgColor = SET_BG_COLOR_DARK_BEIGE;

        for (int row = 0; row < 8; row++) {
            int rowIndex = (ifWhite) ? 8 - row : row + 1;
            String rowString = " " + rowIndex + " ";
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + rowString + RESET_TEXT_COLOR + RESET_BG_COLOR);
            for (int col = 0; col < 8; col++) {
                bgColor = (bgColor.equals(SET_BG_COLOR_DARK_BEIGE)) ? SET_BG_COLOR_BEIGE : SET_BG_COLOR_DARK_BEIGE;
                int colIndex = (ifWhite) ? col + 1 : 8 - col;
                ChessPosition position = new ChessPosition(rowIndex, colIndex);
                ChessPiece piece = board.getPiece(position);
                String pieceColor = SET_TEXT_COLOR_WHITE;
                String text = EMPTY;
                if (piece != null) {
                    TeamColor pieceColorData = piece.getTeamColor();
                    pieceColor = (pieceColorData.equals(TeamColor.WHITE)) ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
                    PieceType pieceType = piece.getPieceType();
                    text = returnPieceString(pieceType, pieceColorData);
                }
                System.out.print(bgColor + pieceColor + text + RESET_TEXT_COLOR + RESET_BG_COLOR);

            }
            bgColor = (bgColor.equals(SET_BG_COLOR_BEIGE)) ? SET_BG_COLOR_DARK_BEIGE : SET_BG_COLOR_BEIGE;
            System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + rowString + RESET_TEXT_COLOR + RESET_BG_COLOR);
        }

        for (int col = 0; col < 10; col++) {
            String item = (ifWhite) ? alphabet[col] : alphabet[9 - col];
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + item + RESET_TEXT_COLOR + RESET_BG_COLOR);
        }
        System.out.println();

    }

    private void handleRegister(String[] parsed) {
        try {
            if (parsed.length != 2) {
                System.out.println("Usage: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
                return;
            }
            String arguments = parsed[1];
            String[] argumentsParsed = arguments.split(" ");
            if (argumentsParsed.length != 3) {
                System.out.println("Usage: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
                return;
            }
            facade.register(argumentsParsed[0], argumentsParsed[1], argumentsParsed[2]);
            loggedIn = true;
            System.out.println(">> Registered! You are now logged in.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleLogin(String[] parsed) {
        try {
            if (parsed.length != 2) {
                System.out.println("Usage: \"login\" <USERNAME> <PASSWORD>");
                return;
            }
            String arguments = parsed[1];
            String[] argumentsParsed = arguments.split(" ");
            if (argumentsParsed.length != 2) {
                System.out.println("Usage: \"login\" <USERNAME> <PASSWORD>");
                return;
            }
            facade.login(argumentsParsed[0], argumentsParsed[1]);
            loggedIn = true;
            System.out.println(">> You are now logged in.");
            help();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleList() {
        if (!loggedIn) {
            System.out.println(">> You are not logged in. Check the options");
            help();
            return;
        }
        try {
            indexToGameID.clear();
            System.out.println(">> Current Game List: [INDEX]) [GAME NAME] [WHITE PLAYER NAME] [BLACK PLAYER NAME]");
            listOfGames = facade.listGames();
            if (listOfGames.isEmpty()) {
                System.out.println("Currently, there is no game on the list. Create your own with \"create\" [GAME NAME]");
            }
            for (int i = 0; i < listOfGames.size(); i++) {
                Map<String, Object> game = listOfGames.get(i);

                String white = game.get("whiteUsername") != null
                        && !((String) game.get("whiteUsername")).isBlank()
                        ? (String) game.get("whiteUsername")
                        : "empty";

                String black = game.get("blackUsername") != null
                        && !((String) game.get("blackUsername")).isBlank()
                        ? (String) game.get("blackUsername")
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
    }

    private void handleLogout() {
        if (!loggedIn) {
            System.out.println(">> You are not logged in. Check the options");
            help();
            return;
        }
        try {
            facade.logout();
            System.out.println(">> You are now logged out. If you need help, type \"help\"");
            loggedIn = false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleObserve(String[] parsed) {
        if (!loggedIn) {
            System.out.println(">> You are not logged in. Check the options");
            help();
            return;
        }

        if (parsed.length != 2) {
            System.out.println("Usage: \"observe\" <GAME INDEX>");
            return;
        }

        int gameIndex;
        try {
            gameIndex = Integer.parseInt(parsed[1]);
        } catch (NumberFormatException e) {
            System.out.println(">> Game index must be in number.");
            return;
        }
        if (!indexToGameID.containsKey(gameIndex)) {
            System.out.println(">> Game index is not valid.");
            return;
        }
        System.out.printf(">> You joined the game with index [ %d ] as an observer. Game loading ... %n", gameIndex);
        // drawBoard
        drawBoard("WHITE");
        observeMode = true;
    }

    private void handleCreate(String[] parsed) {
        if (!loggedIn) {
            System.out.println(">> You are not logged in. Check the options");
            help();
            return;
        }

        try {
            if (parsed.length != 2) {
                System.out.println("Usage: \"create\" <GAME NAME>");
                return;
            }
            String gameName = parsed[1];
            facade.createGame(gameName);
            System.out.println(">> Game created.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleJoin(String[] parsed) {
        if (!loggedIn) {
            System.out.println(">> You are not logged in. Check the options");
            help();
            return;
        }
        String arguments = parsed[1];
        String[] argumentsParsed = arguments.split(" ");
        if (argumentsParsed.length != 2) {
            System.out.println("Usage: \"join\" <GAME INDEX> <COLOR TO PLAY: \"black\" or \"white\">");
            return;
        }

        int gameIndex;
        try {
            gameIndex = Integer.parseInt(argumentsParsed[0]);
        } catch (NumberFormatException e) {
            System.out.println(">> Game index must be in number.");
            return;
        }

        if (!indexToGameID.containsKey(gameIndex)) {
            System.out.println(">> Game index is not valid.");
            return;
        }

        String chosenColor = argumentsParsed[1].toUpperCase();
        if (!chosenColor.equals("WHITE") && !chosenColor.equals("BLACK")) {
            System.out.println(">> Color should be either white or black.");
            return;
        }
        try {
            int gameID = indexToGameID.get(gameIndex);
            facade.joinGame(gameID, chosenColor);
            System.out.printf(">> You joined the game with index [ %d ]. Game loading ... %n", gameIndex);
            gameMode = true;
            // drawBoard
            drawBoard(chosenColor);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
                    handleRegister(parsed);
                    break;
                }
                case "login": {
                    handleLogin(parsed);
                    break;
                }
                case "logout": {
                    handleLogout();
                    break;
                }
                case "list": {
                    handleList();
                    break;
                }
                case "create": {
                    handleCreate(parsed);
                    break;
                }
                case "join": {
                    handleJoin(parsed);
                    break;
                }
                case "observe": {
                    handleObserve(parsed);
                    break;
                }
                case "draw": {
                    drawBoard("WHITE");
                    System.out.println();
                    drawBoard("BLACK");
                    break;
                }

                // these are for develop
                case "leave": {
                    if (gameMode || observeMode) {
                        if (gameMode) {
                            gameMode = false;
                        }
                        if (observeMode) {
                            observeMode = false;
                        }
                        System.out.println(">> You left the game.");
                        break;
                    }
                    else {
                        System.out.println("You are not in the game room.");
                        break;
                    }
                }
                case "clear": {
                    try {
                        facade.clearDatabase();
                        break;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
                // need to delete these two later

                default: System.out.println(">> Unknown command");
            }
        }

    }

}
