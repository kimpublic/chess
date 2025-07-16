package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurnTeam;

    /* 앙파상 */
    private ChessPosition lastDoublePawn = null;


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
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return validMoves;
        } else {
            Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

            for (ChessMove move : possibleMoves) {
                ChessBoard simulationBoard = board.simulationBoard();
                ChessPiece targetPiece = simulationBoard.getPiece(startPosition);

                simulationBoard.addPiece(move.getEndPosition(), targetPiece);
                simulationBoard.addPiece(startPosition, null);



                if (!checkSimulation(simulationBoard, piece.getTeamColor())) {
                    validMoves.add(move);
                }

            }

            /* 캐슬링 */
            if (piece.getPieceType() == ChessPiece.PieceType.KING && !piece.hasMoved() && !checkSimulation(board, piece.getTeamColor())) {
                int row = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : 8;

                ChessPosition rookRight = new ChessPosition(row, 8);
                ChessPiece rookPieceRight = board.getPiece(rookRight);
                if (rookPieceRight != null && rookPieceRight.getPieceType() == ChessPiece.PieceType.ROOK && !rookPieceRight.hasMoved() && board.getPiece(new ChessPosition(row, 6)) == null && board.getPiece(new ChessPosition(row, 7)) == null) {
                    ChessBoard simulationForCol6 = board.simulationBoard();
                    ChessPiece kingForRightSimulation = simulationForCol6.getPiece(startPosition);
                    simulationForCol6.addPiece(new ChessPosition(row, 6), kingForRightSimulation);
                    simulationForCol6.addPiece(startPosition, null);
                    if(!checkSimulation(simulationForCol6, piece.getTeamColor())) {
                        ChessBoard simulationForCol7 = simulationForCol6.simulationBoard();
                        simulationForCol7.addPiece(new ChessPosition(row, 7), kingForRightSimulation);
                        simulationForCol7.addPiece(new ChessPosition(row, 6), null);
                        if (!checkSimulation(simulationForCol7, piece.getTeamColor())) {
                            validMoves.add(new ChessMove(startPosition, new ChessPosition(row, 7), null));
                        }
                    }
                }

                ChessPosition rookLeft = new ChessPosition(row, 1);
                ChessPiece rookPieceLeft = board.getPiece(rookLeft);

                if (rookPieceLeft != null && rookPieceLeft.getPieceType() == ChessPiece.PieceType.ROOK && !rookPieceLeft.hasMoved() && board.getPiece(new ChessPosition(row, 2)) == null && board.getPiece(new ChessPosition(row, 3)) == null && board.getPiece(new ChessPosition(row, 4)) == null) {
                    ChessBoard simulationForCol4 = board.simulationBoard();
                    ChessPiece kingForLeftSimulation = simulationForCol4.getPiece(startPosition);
                    simulationForCol4.addPiece(new ChessPosition(row, 4), kingForLeftSimulation);
                    simulationForCol4.addPiece(startPosition, null);
                    if (!checkSimulation(simulationForCol4, piece.getTeamColor())) {
                        ChessBoard simulationForCol3 = simulationForCol4.simulationBoard();
                        simulationForCol3.addPiece(new ChessPosition(row, 3), kingForLeftSimulation);
                        simulationForCol3.addPiece(new ChessPosition(row, 4), null);
                        if (!checkSimulation(simulationForCol3, piece.getTeamColor())) {
                            validMoves.add(new ChessMove(startPosition, new ChessPosition(row, 3), null));
                        }
                    }
                }
            }

            if (piece.getPieceType() == ChessPiece.PieceType.PAWN && lastDoublePawn != null) {
                int direction = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
                int startRow = startPosition.getRow();
                int thePawn = (piece.getTeamColor() == TeamColor.WHITE) ? 5 : 4;

                if ( startRow == thePawn && Math.abs(lastDoublePawn.getColumn() - startPosition.getColumn()) == 1) {

                    ChessPosition capturePosition = new ChessPosition(startRow + direction, lastDoublePawn.getColumn());

                    if (board.isValidPosition(capturePosition) && board.getPiece(capturePosition) == null) {
                        ChessBoard simulation = board.simulationBoard();

                        ChessPiece pawnForSimulation = simulation.getPiece(startPosition);
                        simulation.addPiece(capturePosition, pawnForSimulation);
                        simulation.addPiece(startPosition, null);
                        simulation.addPiece(lastDoublePawn, null);

                        if (!checkSimulation(simulation, piece.getTeamColor())) {
                            validMoves.add(new ChessMove(startPosition, capturePosition, null));
                        }
                    }
                }
            }

            return validMoves;
        }
    }

    private boolean checkSimulation(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col ++) {
                ChessPosition searchPosition = new ChessPosition(row, col);
                ChessPiece searchedPiece = board.getPiece(searchPosition);
                if (searchedPiece != null && searchedPiece.getPieceType() == ChessPiece.PieceType.KING && searchedPiece.getTeamColor() == teamColor) {
                    return canKingBeAttacked(board, searchPosition, teamColor);
                }
            }
        }
        return false;
    }

    private boolean canKingBeAttacked(ChessBoard board, ChessPosition kingPosition, TeamColor currentTeamColor) {
        TeamColor opponentColor;

        opponentColor = (currentTeamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col ++) {
                ChessPosition opponentPosition = new ChessPosition(row, col);
                ChessPiece opponentPiece = board.getPiece(opponentPosition);

                if (opponentPiece != null && opponentPiece.getTeamColor() == opponentColor) {
                    Collection<ChessMove> opponentMoves = opponentPiece.pieceMoves(board, opponentPosition);

                    for (ChessMove move : opponentMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        ChessPiece piece = board.getPiece(start);

        if (piece == null || piece.getTeamColor() != currentTurnTeam) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> validMoves = validMoves(start);
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && Math.abs(end.getColumn() - start.getColumn()) == 1 && board.getPiece(end) == null && lastDoublePawn != null && lastDoublePawn.getRow() == start.getRow() && lastDoublePawn.getColumn() == end.getColumn()) {
            board.addPiece(lastDoublePawn, null);
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING && Math.abs(end.getColumn() - start.getColumn()) == 2) {
            int row = start.getRow();

            if (end.getColumn() == start.getColumn() + 2) {
                ChessPosition rookStart = new ChessPosition(row, 8);
                ChessPosition rookEnd = new ChessPosition(row, 6);
                ChessPiece rookPiece = board.getPiece(rookStart);

                board.addPiece(rookEnd, rookPiece);
                board.addPiece(rookStart, null);
                if (!rookPiece.hasMoved()) {rookPiece.markMoved();}
            } else if (end.getColumn() == start.getColumn() - 2) {
                ChessPosition rookStart = new ChessPosition(row, 1);
                ChessPosition rookEnd = new ChessPosition(row, 4);
                ChessPiece rookPiece = board.getPiece(rookStart);
                board.addPiece(rookEnd, rookPiece);
                board.addPiece(rookStart, null);
                if (!rookPiece.hasMoved()) {rookPiece.markMoved();}
            }
        }


        if (move.getPromotionPiece() != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            board.addPiece(end, new ChessPiece(currentTurnTeam, move.getPromotionPiece()));
        } else {
            board.addPiece(end, piece);
        }

        board.addPiece(start, null);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && Math.abs(end.getRow() - start.getRow()) == 2) {
            lastDoublePawn = end;
        } else {
            lastDoublePawn = null;
        }

        if (!piece.hasMoved()) {
            piece.markMoved();
        }

        if (currentTurnTeam == TeamColor.WHITE) {
            currentTurnTeam = TeamColor.BLACK;
        } else {
            currentTurnTeam = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for (int row = 1; row <= 8; row ++) {
            for (int col = 1; col <= 8; col ++) {
                ChessPosition searchPosition = new ChessPosition(row, col);
                ChessPiece searchedPiece = board.getPiece(searchPosition);
                if (searchedPiece != null && searchedPiece.getPieceType() == ChessPiece.PieceType.KING && searchedPiece.getTeamColor() == teamColor) {
                    if (canKingBeAttacked(board, searchPosition, teamColor)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }


        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return Objects.equals(board, chessGame.board) && currentTurnTeam == chessGame.currentTurnTeam;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurnTeam);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", currentTurnTeam=" + currentTurnTeam +
                '}';
    }
}
