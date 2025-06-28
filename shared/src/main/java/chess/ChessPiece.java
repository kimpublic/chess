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


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
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

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        if (type == PieceType.KING) {

            int[][] directions = {
                    {1, -1},
                    {1, 1},
                    {-1, 1},
                    {-1, -1},
                    {-1, 0},
                    {1, 0},
                    {0, -1},
                    {0, 1}
            };

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

        } else if (type == PieceType.QUEEN) {

            int[][] directions = {
                    {1, -1},
                    {1, 1},
                    {-1, 1},
                    {-1, -1},
                    {-1, 0},
                    {1, 0},
                    {0, -1},
                    {0, 1}
            };

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

        } else if (type == PieceType.BISHOP) {
            int[][] directions = {
                    {1, -1},
                    {1, 1},
                    {-1, 1},
                    {-1, -1}
            };

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

        } else if (type == PieceType.KNIGHT) {
            int[][] directions = {
                    {1, -2},
                    {2, -1},
                    {2, 1},
                    {1, 2},
                    {-1, 2},
                    {-2, 1},
                    {-2, -1},
                    {-1, -2}
            };

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

        } else if (type == PieceType.ROOK) {

            int[][] directions = {
                    {-1, 0},
                    {1, 0},
                    {0, -1},
                    {0, 1}
            };

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


        } else if (type == PieceType.PAWN) {
            int direction;
            int startRow;
            /* 여기서 ChessGame.TeamColor.WHITE는 enum임 비교하기 위해 끌고오는 것일 뿐. */
            if (pieceColor == ChessGame.TeamColor.WHITE) {
                direction = 1;
            } else {
                direction = -1;
            }
            if (pieceColor == ChessGame.TeamColor.WHITE) {
                startRow = 2;
            } else {
                startRow = 7;
            }

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            /* checking 한 칸 앞 */
            ChessPosition oneStep = new ChessPosition(row + direction, col);

            if ( board.getPiece(oneStep) == null) {
                validMoves.add(new ChessMove(myPosition, oneStep, null));

                /* Checking 두 칸 앞 */
                ChessPosition twoStep = new ChessPosition(row + 2 * direction, col);

                if (row == startRow && board.getPiece(twoStep) == null) {
                    validMoves.add(new ChessMove(myPosition, twoStep, null));
                }

            }

            /* Checking 대각선에 적 있는지 */

            ChessPosition upLeft = new ChessPosition(row + direction, col - 1);

            if (board.isValidPosition(upLeft)) {
                ChessPiece opponent = board.getPiece(upLeft);
                if (opponent != null && opponent.getTeamColor() != pieceColor) {
                    validMoves.add(new ChessMove(myPosition, upLeft, null));
                }
            }

            ChessPosition upRight = new ChessPosition(row + direction, col + 1);

            if (board.isValidPosition(upRight)) {
                ChessPiece opponent = board.getPiece(upRight);
                if (opponent != null && opponent.getTeamColor() != pieceColor) {
                    validMoves.add(new ChessMove(myPosition, upRight, null));
                }
            }
        }

        return validMoves;

    }

    @Override
    public boolean equals(Object o) {
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
