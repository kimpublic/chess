package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    /* 캐슬링 */
    private boolean hasMoved = false;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /* 캐슬링 */
    public boolean hasMoved() {
        return hasMoved;
    }

    /* 캐슬링 */
    public void markMoved() {
        this.hasMoved = true;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    private static final int[][] KING_DIRECTIONS = {
            {1, -1},
            {1, 1},
            {-1, 1},
            {-1, -1},
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };
    private static final int[][] QUEEN_DIRECTIONS  = KING_DIRECTIONS;
    private static final int[][] BISHOP_DIRECTIONS = {
            { 1, -1 }, { 1,  1 }, { -1,  1 }, { -1, -1 }
    };
    private static final int[][] ROOK_DIRECTIONS   = {
            { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }
    };
    private static final int[][] KNIGHT_DIRECTIONS = {
            { 1, -2 }, { 2, -1 }, { 2,  1 }, { 1,  2 },
            { -1, 2 }, { -2, 1 }, { -2, -1 }, { -1, -2 }
    };

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type) {
            case KING -> calculateOneMove(board, myPosition, KING_DIRECTIONS);
            case QUEEN -> calculateloopedMove(board, myPosition, QUEEN_DIRECTIONS);
            case BISHOP -> calculateloopedMove(board, myPosition, BISHOP_DIRECTIONS);
            case ROOK -> calculateloopedMove(board, myPosition, ROOK_DIRECTIONS);
            case KNIGHT -> calculateOneMove(board, myPosition, KNIGHT_DIRECTIONS);
            case PAWN -> calculatePawnMove(board, myPosition);
        };
    }

    private Collection<ChessMove> calculateOneMove(ChessBoard board, ChessPosition myPosition, int[][] directions) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (int[] direction : directions) {
            int row = myPosition.getRow() + direction[0];
            int col = myPosition.getColumn() + direction[1];

            ChessPosition nextPosition = new ChessPosition(row, col);

            if (board.isValidPosition(nextPosition)) {

                ChessPiece pieceAtNextPosition = board.getPiece(nextPosition);

                if (pieceAtNextPosition == null || pieceAtNextPosition.getTeamColor() != pieceColor) {
                    validMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
            }
        }
        return validMoves;
    }

    private Collection<ChessMove> calculateloopedMove(ChessBoard board, ChessPosition myPosition, int[][] directions) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                ChessPosition nextPosition = new ChessPosition(row, col);
                /* 다음 칸이 보드판 밖이면 멈춤 */
                if (!board.isValidPosition(nextPosition)) break;

                ChessPiece pieceAtNextPosition = board.getPiece(nextPosition);
                /* 다른 말이 없으면 유효, 다른 말 있으면 상대말이면 이동 가능, 다만 거기서 멈춰야 함 */
                if (pieceAtNextPosition == null) {
                    validMoves.add(new ChessMove(myPosition, nextPosition, null));
                } else {
                    if (pieceAtNextPosition.getTeamColor() != pieceColor) {
                        validMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                    break;
                }
            }
        }

        return validMoves;
    }

    private Collection<ChessMove> calculatePawnMove(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        int direction;
        int startRow;
        int finalRow;

        /* 여기서 ChessGame.TeamColor.WHITE는 enum임 비교하기 위해 끌고오는 것일 뿐. */
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            direction = 1;
            startRow = 2;
            finalRow = 8;
        } else {
            direction = -1;
            startRow = 7;
            finalRow = 1;
        }

        // 앞으로 한 칸 / 두 칸 이동
        addPawnForwardMoves(board, myPosition, direction, startRow, finalRow, validMoves);

        // 대각선 캡처 (및 프로모션)
        addPawnCaptureMoves(board, myPosition, direction, finalRow, validMoves);

        return validMoves;
    }

    private void addPawnForwardMoves(
            ChessBoard board,
            ChessPosition from,
            int direction,
            int startRow,
            int finalRow,
            Collection<ChessMove> moves
    ) {
        // 한 칸 앞으로
        ChessPosition oneStep = new ChessPosition(from.getRow() + direction, from.getColumn());
        if (board.isValidPosition(oneStep) && board.getPiece(oneStep) == null) {
            if (oneStep.getRow() == finalRow) {
                // 프로모션
                addPawnPromotionMoves(from, oneStep, moves);
            } else {
                moves.add(new ChessMove(from, oneStep, null));
                // 두 칸 앞으로 (스타트 위치에서만)
                ChessPosition twoStep = new ChessPosition(from.getRow() + 2 * direction, from.getColumn());
                if (from.getRow() == startRow && board.isValidPosition(twoStep) && board.getPiece(twoStep) == null) {
                    moves.add(new ChessMove(from, twoStep, null));
                }
            }
        }
    }

    private void addPawnCaptureMoves(
            ChessBoard board,
            ChessPosition from,
            int direction,
            int finalRow,
            Collection<ChessMove> moves
    ) {
        // 왼쪽 대각선
        ChessPosition upLeft = new ChessPosition(from.getRow() + direction, from.getColumn() - 1);
        if (board.isValidPosition(upLeft)) {
            ChessPiece target = board.getPiece(upLeft);
            if (target != null && target.getTeamColor() != pieceColor) {
                if (upLeft.getRow() == finalRow) {
                    addPawnPromotionMoves(from, upLeft, moves);
                } else {
                    moves.add(new ChessMove(from, upLeft, null));
                }
            }
        }

        // 오른쪽 대각선
        ChessPosition upRight = new ChessPosition(from.getRow() + direction, from.getColumn() + 1);
        if (board.isValidPosition(upRight)) {
            ChessPiece target = board.getPiece(upRight);
            if (target != null && target.getTeamColor() != pieceColor) {
                if (upRight.getRow() == finalRow) {
                    addPawnPromotionMoves(from, upRight, moves);
                } else {
                    moves.add(new ChessMove(from, upRight, null));
                }
            }
        }
    }

    private void addPawnPromotionMoves(
            ChessPosition from,
            ChessPosition to,
            Collection<ChessMove> moves
    ) {
        for (PieceType promotionType : PieceType.values()) {
            if (promotionType != PieceType.KING && promotionType != PieceType.PAWN) {
                moves.add(new ChessMove(from, to, promotionType));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece that)) {
            return false;
        }
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }
}
