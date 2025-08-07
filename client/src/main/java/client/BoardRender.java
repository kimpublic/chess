package client;

import chess.*;

import java.util.Collection;

import static ui.EscapeSequences.*;

public class BoardRender {

    public static String returnPieceString(ChessPiece.PieceType type, ChessGame.TeamColor color) {
        switch (type) {
            case KING:
                return color == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN:
                return color == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP:
                return color == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT:
                return color == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK:
                return color == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN:
                return color == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
            default:
                return EMPTY;
        }
    }

    public static void drawBoard(ChessGame game, String perspective, ChessPosition selected, Collection<ChessPosition> validPositions, ChessMove moveMade) {
        System.out.println(">>> Chessboard loaded");
        boolean ifWhite = "WHITE".equals(perspective);
        ChessBoard board = game.getBoard();

        String[] alphabet = {
                "   ", "\u2003A ", "\u2003B ", "\u2003C ", "\u2003D ", "\u2003E ", "\u2003F ", "\u2003G ", "\u2003H ", "   "
        };



        for (int col = 0; col < 10; col++) {
            String item = (ifWhite) ? alphabet[col] : alphabet[9 - col];
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + item + RESET_TEXT_COLOR + RESET_BG_COLOR);
        }
        System.out.println();

        String bgColor = SET_BG_COLOR_DARK_BEIGE;

        for (int row = 0; row < 8; row++) {
            int rowIndex = (ifWhite) ? 8 - row : row + 1;
            String rowString = " " + rowIndex + " ";
            // 왼쪽 행 숫자 출력
            System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + rowString + RESET_TEXT_COLOR + RESET_BG_COLOR);

            for (int col = 0; col < 8; col++) {
                // 배경 색 바둑판처럼 번갈아가며 설정
                bgColor = (bgColor.equals(SET_BG_COLOR_DARK_BEIGE)) ? SET_BG_COLOR_BEIGE : SET_BG_COLOR_DARK_BEIGE;
                int colIndex = (ifWhite) ? col + 1 : 8 - col;

                ChessPosition position = new ChessPosition(rowIndex, colIndex);
                ChessPiece piece = board.getPiece(position);

                String squareBgColor = bgColor;

                if (moveMade != null) {
                    if (position.equals(moveMade.getStartPosition())) {
                        squareBgColor = SET_BG_COLOR_YELLOW; // 출발 지점: 노란색
                    } else if (position.equals(moveMade.getEndPosition())) {
                        squareBgColor = SET_BG_COLOR_GREEN; // 도착 지점: 연두색
                    }
                }

                if (position.equals(selected)) {
                    squareBgColor = SET_BG_COLOR_YELLOW;
                } else if (validPositions != null && validPositions.contains(position)) {
                    squareBgColor = SET_BG_COLOR_GREEN;
                }

                String pieceColor = SET_TEXT_COLOR_WHITE;
                String text = EMPTY;
                if (piece != null) {
                    ChessGame.TeamColor pieceColorData = piece.getTeamColor();
                    pieceColor = (pieceColorData.equals(ChessGame.TeamColor.WHITE)) ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
                    ChessPiece.PieceType pieceType = piece.getPieceType();
                    text = returnPieceString(pieceType, pieceColorData);
                }
                System.out.print(squareBgColor + pieceColor + text + RESET_TEXT_COLOR + RESET_BG_COLOR);

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
}
