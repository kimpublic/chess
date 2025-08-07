package websocket.messages;

import chess.ChessGame;
import chess.ChessMove;

public class LoadGameMessage extends ServerMessage {
    private final ChessGame game;
    private final ChessMove moveMade;

    public LoadGameMessage(ChessGame game, ChessMove moveMade) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.moveMade = moveMade;
    }

    public ChessGame getGame() {
        return game;
    }

    public ChessMove getMoveMade() {
        return moveMade;
    }

}
