package client;

import chess.*;
import chess.ChessGame.*;
import chess.ChessPiece.*;
import ui.EscapeSequences;
import websocket.commands.UserGameCommand;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Console {

    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);
    private boolean loggedIn = false;
    private boolean gameMode = false;
    private boolean observeMode = false;
    private String cmd = "";
    private List<Map<String,Object>> listOfGames = null;
    private Integer currentGameID;
    private String perspective;
    private CountDownLatch joinLatch;
    private Map<Integer,Integer> indexToGameID = new HashMap<>();
    private static final Map<String, ChessPiece.PieceType> PROMOTION_COMMANDS = Map.of(
            "Q", PieceType.QUEEN, "B", PieceType.BISHOP, "K", PieceType.KNIGHT, "R", PieceType.ROOK
    );
    public ChessGame.TeamColor myColor;
    public ChessGame.TeamColor currentTurnTeam;
    public ChessGame currentGame;

    public Console(ServerFacade facade) {
        this.facade = facade;
    }

    public void help() {
        if (!loggedIn) {
            System.out.println("#Options:");
            System.out.println(" - Login as an existing user: \"login\" <USERNAME> <PASSWORD>");
            System.out.println("Register as a new user: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
            System.out.println("Exit the program: \"quit\"");
            System.out.println("Print this option page: \"help\"");
        } else if (gameMode) {
            System.out.println("#Options:");
            System.out.println(" - Redraw the chess board: \"redraw\"");
            System.out.println(" - Leave the current game you are in: \"leave\"");
            System.out.println(" - Make a move of a peace: \"move\" <START POSITION> <DESTINATION> <OPTIONAL PROMOTION TYPE> | Ex. move a7 a8 q");
            System.out.println(" - Possible Promotion Types: q(QUEEN) / b(BISHOP) / k(KNIGHT) / r(ROOK)");
            System.out.println(" - Resign: \"resign\"");
            System.out.println(" - Highlight legal moves of a piece: \"hi\" <PIECE LOCATION>");
            System.out.println(" - Print this option page: \"help\"");
        } else if (observeMode) {
            System.out.println("#Options:");
            System.out.println(" - Redraw the chess board: \"redraw\"");
            System.out.println(" - Highlight legal moves of a piece: \"hi\" <PIECE LOCATION>");
            System.out.println(" - Leave the current game you are in: \"leave\"");
            System.out.println(" - Print this option page: \"help\"");
        }
        else {
            System.out.println("#Options:");
            System.out.println(" - List current games: \"list\"");
            System.out.println(" - Create a new game: \"create\" <GAME NAME>");
            System.out.println(" - Join a game: \"join\" <GAME INDEX> <COLOR TO PLAY: \"black\" or \"white\">");
            System.out.println(" - Observe a game: \"observe\" <GAME CODE>");
            System.out.println(" - Logout: \"logout\"");
            System.out.println(" - Exit the program: \"quit\"");
            System.out.println(" - Print this option page: \"help\"");
        }
    }

    public void drawBoard(ChessGame game, String color, ChessPosition selected, Collection<ChessPosition> validMoves, ChessMove moveMade) {
        BoardRender.drawBoard(game, color, selected, validMoves, moveMade);
    }

    public void drawBoard(String color) {
        drawBoard(new ChessGame(), color, null, null, null);
    }

    public String getPerspective() {
        return perspective;
    }

    private void handleRegister(String[] parsed) {
        try {
            if (loggedIn) {
                System.out.println(">>> You are already logged in. Logout first to register a new account.");
                return;
            }
            if (parsed.length != 2) {
                System.out.println(">>> Usage: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
                return;
            }
            String arguments = parsed[1];
            String[] argumentsParsed = arguments.split(" ");
            if (argumentsParsed.length != 3) {
                System.out.println(">>> Usage: \"register\" <USERNAME> <PASSWORD> <EMAIL>");
                return;
            }
            facade.register(argumentsParsed[0], argumentsParsed[1], argumentsParsed[2]);
            loggedIn = true;
            System.out.println(">>> Registered! You are now logged in.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleLogin(String[] parsed) {
        try {
            if (loggedIn) {
                System.out.println(">>> You are already logged in. Logout first to login as another user.");
                return;
            }
            if (parsed.length != 2) {
                System.out.println(">>> Usage: \"login\" <USERNAME> <PASSWORD>");
                return;
            }
            String arguments = parsed[1];
            String[] argumentsParsed = arguments.split(" ");
            if (argumentsParsed.length != 2) {
                System.out.println(">>> Usage: \"login\" <USERNAME> <PASSWORD>");
                return;
            }
            facade.login(argumentsParsed[0], argumentsParsed[1]);
            loggedIn = true;
            System.out.println(">>> You are now logged in.");
            help();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleList() {
        if (!loggedIn) {
            System.out.println(">>> You are not logged in. Check the options");
            help();
            return;
        }
        if (gameMode || observeMode) {
            System.out.println(">>> You cannot list games while inside a game room. Please leave the game first.");
            return;
        }
        try {
            indexToGameID.clear();
            System.out.println(">>> Current Game List: [INDEX]) [GAME NAME] [WHITE PLAYER NAME] [BLACK PLAYER NAME]");
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
                        ">>> %d) %s [White: %s, Black: %s]%n", indexNumber, game.get("gameName"), white, black
                );
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleLogout() {
        if (!loggedIn) {
            System.out.println(">>> You are not logged in. Check the options");
            help();
            return;
        }
        try {
            facade.logout();
            System.out.println(">>> You are now logged out. If you need help, type \"help\"");
            loggedIn = false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleObserve(String[] parsed) {
        if (!loggedIn) {
            System.out.println(">>> You are not logged in. Check the options");
            help();
            return;
        }
        if (gameMode || observeMode) {
            System.out.println(">>> You cannot observe a game while inside a game room. Please leave the game first.");
            return;
        }
        if (parsed.length != 2) {
            System.out.println(">>> Usage: \"observe\" <GAME INDEX>");
            return;
        }
        int gameIndex;
        try {
            gameIndex = Integer.parseInt(parsed[1]);
        } catch (NumberFormatException e) {
            System.out.println(">>> Game index must be in number.");
            return;
        }
        if (!indexToGameID.containsKey(gameIndex)) {
            System.out.println(">>> Game index is not valid.");
            return;
        }

        try {
            facade.connectWebSocket(this);
            this.perspective = "WHITE";
            int gameID = indexToGameID.get(gameIndex);
            this.currentGameID = gameID;
            facade.sendGameCommand(new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT,
                    facade.getAuthToken(),
                    currentGameID
            ));
            observeMode = true;
            System.out.printf(">>> You are now observing the game with index [ %d ]. Game loading ... %n", gameIndex);
            joinLatch = new CountDownLatch(1);
            if (!joinLatch.await(5, TimeUnit.SECONDS)) {
                System.out.println(">>> Waiting for server to draw the chess board…");
            }
        } catch (Exception e) {
            this.currentGameID = null;
            System.out.println(">>> Observing failed: " + e.getMessage());
        }
    }

    private void handleCreate(String[] parsed) {
        if (!loggedIn) {
            System.out.println(">>> You are not logged in. Check the options"); return;
        }
        if (gameMode || observeMode) {
            System.out.println(">>> You cannot create a new game while inside a game room. Please leave the game first."); return;
        }

        try {
            if (parsed.length != 2) {
                System.out.println(">>> Usage: \"create\" <GAME NAME>"); return;
            }
            String gameName = parsed[1];
            facade.createGame(gameName);
            System.out.println(">>> Game created.");
        } catch (Exception e) {
            System.out.println("Creating game failed: " + e.getMessage());
        }
    }

    private void handleJoin(String[] parsed) {
        if (!loggedIn) {
            System.out.println(">>> You are not logged in. Check the options");
            return;
        }
        if (gameMode || observeMode) {
            System.out.println(">>> You cannot join a game while inside a game room. Please leave the game first.");
            return;
        }

        String arguments = parsed[1];
        String[] argumentsParsed = arguments.split(" ");
        if (argumentsParsed.length != 2) {
            System.out.println(">>> Usage: \"join\" <GAME INDEX> <COLOR TO PLAY: \"black\" or \"white\">");
            return;
        }
        int gameIndex;
        try {
            gameIndex = Integer.parseInt(argumentsParsed[0]);
        } catch (NumberFormatException e) {
            System.out.println(">>> Game index must be in number.");
            return;
        }
        if (!indexToGameID.containsKey(gameIndex)) {
            System.out.println(">>> Game index is not valid.");
            return;
        }
        String chosenColor = argumentsParsed[1].toUpperCase();
        myColor = ChessGame.TeamColor.valueOf(chosenColor);
        if (!chosenColor.equals("WHITE") && !chosenColor.equals("BLACK")) {
            System.out.println(">>> Color should be either white or black.");
            return;
        }
        try {
            int gameID = indexToGameID.get(gameIndex);
            this.perspective = chosenColor;
            facade.joinGame(gameID, chosenColor);
            this.currentGameID = gameID;
            System.out.printf(">>> You joined the game with index [ %d ]. Game loading ... %n", gameIndex);
            facade.connectWebSocket(this);
            facade.sendGameCommand(new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT,
                    facade.getAuthToken(),
                    gameID
            ));
            joinLatch = new CountDownLatch(1);
            if (!joinLatch.await(5, TimeUnit.SECONDS)) {
                System.out.println(">>> Waiting for server to draw the chess board…");
            }
            gameMode = true;
        } catch (Exception e) {
            this.currentGameID = null;
            System.out.println("Joining game failed: " + e.getMessage());
        }
    }

    public void handleLeave() {
        if ((gameMode || observeMode) && currentGameID != null) {
            // 1) 먼저 서버에 LEAVE 커맨드 전송
            try {
                facade.sendGameCommand(new UserGameCommand(
                        UserGameCommand.CommandType.LEAVE,
                        facade.getAuthToken(),
                        currentGameID
                ));
            } catch (Exception e) {
                System.out.println(">>> Failed to send leave command: " + e.getMessage());
                return; // 서버에 못 보냈으면 여기서 그만.
            }
            if (gameMode) {gameMode = false;}
            if (observeMode) {observeMode = false;}
            currentGameID = null;
            System.out.println(">>> You left the game.");
            help();
        } else {
            System.out.println("You are not in a game room.");
        }
    }

    public void handleResign() {
        if (!gameMode || currentGameID == null) {
            System.out.println(">>> You are not in a game room.");
            return;
        }
        facade.sendGameCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, facade.getAuthToken(), currentGameID));
    }

    public void handleMove(String[] parsed) {
        if (!gameMode || currentGameID == null) {
            System.out.println(">>> You are not in a game room.");
            return;
        }
        if (currentTurnTeam != myColor) {
            System.out.println(">>> You cannot make a move right now.");
            return;
        }
        String arguments = parsed[1];
        String[] argumentsParsed = arguments.split(" ");
        if (argumentsParsed.length < 2 || argumentsParsed.length > 3) {
            System.out.println(">>> Usage: \"move\" <START POSITION> <DESTINATION> <OPTIONAL PROMOTION TYPE>");
            System.out.println(">>> Example: \"move\" a2 a3 Q(if applied)");
            return;
        }
        String start = argumentsParsed[0].toUpperCase();
        String end = argumentsParsed[1].toUpperCase();
        if (!start.matches("^[A-H][1-8]$") || !end.matches("^[A-H][1-8]$")) {
            System.out.println(">>> Invalid positions. Columns: a–h, Rows: 1–8");
            return;
        }
        int startRow = Character.getNumericValue(start.charAt(1));
        int startCol = start.charAt(0) - 'A' + 1;
        ChessPosition startPosition = new ChessPosition(startRow, startCol);
        int endRow = Character.getNumericValue(end.charAt(1));
        int endCol = end.charAt(0) - 'A' + 1;
        ChessPosition endPosition = new ChessPosition(endRow, endCol);
        PieceType promotionType = null;
        if (argumentsParsed.length == 3) {
            ChessPiece piece = currentGame.getBoard().getPiece(startPosition);
            System.out.println(">>> Piece at " + start + " = " + (piece == null ? "null" : piece.getPieceType() + " (" + piece.getTeamColor() + ")"));

            boolean onLastRowCheck = piece.getTeamColor() == TeamColor.WHITE ? endRow == 8 : endRow == 1;
            if (!onLastRowCheck || piece.getPieceType() != PieceType.PAWN) {
                System.out.println(">>> The piece is not eligible for promotion.");
                return;
            } else if (!PROMOTION_COMMANDS.containsKey(argumentsParsed[2].toUpperCase())) {
                System.out.println(">>> Check your promotion type. ");
                System.out.println(">>> Possible Promotion Types: q(QUEEN) / b(BISHOP) / k(KNIGHT) / r(ROOK)");
            } else {
                promotionType = PROMOTION_COMMANDS.get(argumentsParsed[2].toUpperCase());
            }
        }
        ChessMove move = new ChessMove(startPosition, endPosition, promotionType);
        try {
            facade.sendGameCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, facade.getAuthToken(), currentGameID, move));
        } catch (IllegalStateException e) {
            System.out.println(">>> " + e.getMessage());
        } catch (Exception e) {
            System.out.println(">>> Failed to make move: " + e.getMessage());
        }
    }

    public void handleHighlight(String[] parsed) {
        if (!gameMode && !observeMode) {
            System.out.println(">>> You are not in a game room.");
            return;
        }
        if (currentGame == null) {
            System.out.println(">>> No game loaded.");
            return;
        }
        if (parsed.length != 2) {
            System.out.println(">>> Usage: \"highlight\" <PIECE POSITION> | Ex. highlight a2");
            return;
        }
        String positionString = parsed[1].toUpperCase();
        if (!positionString.matches("^[A-H][1-8]$")) {
            System.out.println(">>> Invalid position. Columns: a–h, Rows: 1–8");
            return;
        }
        int row = Character.getNumericValue(positionString.charAt(1));
        int col = positionString.charAt(0) - 'A' + 1;
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = currentGame.getBoard().getPiece(position);
        if (piece == null) {
            System.out.println(">>> No piece at the given position.");
            return;
        }
        Collection<ChessMove> validMoves = currentGame.validMoves(position);
        if (validMoves == null || validMoves.isEmpty()) {
            System.out.println(">>> No valid moves for the selected piece.");
        }
        Collection<ChessPosition> validPositions = new HashSet<>();
        for (ChessMove move : validMoves) {
            validPositions.add(move.getEndPosition());
        }
        drawBoard(currentGame, perspective, position, validPositions, null);
    }

    public void onLoadGame(ChessGame game, ChessMove moveMade) {
        this.currentGame = game;
        drawBoard(game, perspective, null, null, moveMade);
        if (joinLatch != null) {
            joinLatch.countDown();
        }
    }

    public void handleClear() {
        try {
            facade.clearDatabase();
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
                case "quit": {System.out.println("Hope you had a fun time with us. Good bye and see you next time!");return;}
                case "help": {help();break;}
                case "register": {handleRegister(parsed);break;}
                case "login": {handleLogin(parsed);break;}
                // post-login
                case "logout": {handleLogout();break;}
                case "list": {handleList();break;} case "create": {handleCreate(parsed);break;}
                case "join": {handleJoin(parsed);break;}
                case "observe": {handleObserve(parsed);break;}
                // gameMode & observeMode
                case "leave": {handleLeave();break;}
                case "redraw": {
                    if (currentGame != null) {
                        drawBoard(currentGame, perspective, null, null, null);
                    } else {
                        System.out.println(">>> No game found to redraw.");
                    }
                    break;
                }
                case "move": {handleMove(parsed);break;}
                case "resign": {handleResign();break;}
                case "hi": {handleHighlight(parsed);break;}
                case "clear": {handleClear(); break;}
                default: System.out.println(">>> Unknown command");
            }
        }
    }
}
