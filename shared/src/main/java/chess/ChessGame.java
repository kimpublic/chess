package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurnTeam;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTurnTeam = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurnTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurnTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        } else if (currentTurnTeam == piece.getTeamColor()) {
            Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

            Collection<ChessMove> validMoves = new ArrayList<>();

            for (ChessMove move : possibleMoves) {
                ChessBoard simulationBoard = board.simulationBoard();
                ChessPiece targetPiece = simulationBoard.getPiece(startPosition);

                simulationBoard.addPiece(move.getEndPosition(), targetPiece);
                simulationBoard.addPiece(startPosition, null);



                if (!checkSimulation(simulationBoard, currentTurnTeam)) {

                }

            }

            return validMoves;
        } else { return null;
        }
    }

    private boolean checkSimulation(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col ++) {
                ChessPosition searchPosition = new ChessPosition(row, col);
                ChessPiece searchedPiece = board.getPiece(searchPosition);
                if (searchedPiece != null && searchedPiece.getPieceType() == ChessPiece.PieceType.KING && searchedPiece.getTeamColor() == teamColor) {
                    ChessPosition kingPosition = new ChessPosition(row, col);
                    if (canKingBeAttacked(board, kingPosition, teamColor))
                    break;
                }
            }
        }
        return false;
    }

    private boolean canKingBeAttacked(ChessBoard board, ChessPosition kingPosition, TeamColor currentTeamColor) {
        if (currentTeamColor == TeamColor.WHITE) {
            TeamColor opponentColor = TeamColor.BLACK;
        } else {
            TeamColor opponentColor = TeamColor.WHITE;
        }

        return true;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
