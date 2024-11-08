package com.example.chessfsm.chessfsm.fsm;

public class StateChecker {
    private final ChessBoard board;

    public StateChecker(ChessBoard board) {
        this.board = board;
    }

    public boolean isKingInCheck(String playerColor) {
        // Locate the king’s position
        Position kingPosition = findKingPosition(playerColor);

        if (kingPosition == null) {
            // King not found (this shouldn't happen in a valid game)
            return false;
        }

        // Check if any opposing piece can attack the king’s position
        return isPositionUnderAttack(kingPosition, opponentColor(playerColor));
    }

    public boolean isCheckmate(String playerColor) {
        // Check if the king is in check and has no valid moves to escape
        if (!isKingInCheck(playerColor)) {
            return false;
        }

        // Check if the player has any valid moves to escape check
        // Implement logic to iterate over all possible moves for this player and see if they can escape
        // This is a simplified placeholder
        for (Position from : board.getAllPositions(playerColor)) {
            for (Position to : board.getAllValidMoves(from, playerColor)) {
                // Simulate move and check if it resolves the check
                if (board.simulateMoveAndCheck(from, to, playerColor)) {
                    return false; // There is at least one valid move to escape check
                }
            }
        }

        return true; // No valid moves to escape check, so it's checkmate
    }

    public boolean isStalemate(String playerColor) {
        // If the king is in check, it’s not stalemate
        if (isKingInCheck(playerColor)) {
            return false;
        }

        // Check if the player has no valid moves but is not in check
        for (Position from : board.getAllPositions(playerColor)) {
            for (Position to : board.getAllValidMoves(from, playerColor)) {
                if (board.simulateMoveAndCheck(from, to, playerColor)) {
                    return false; // There is at least one valid move, so it's not stalemate
                }
            }
        }

        return true; // No valid moves and not in check, so it's stalemate
    }

    private Position findKingPosition(String playerColor) {
        // Find the king’s position on the board based on player color
        for (Position position : board.getAllPositions(playerColor)) {
            String piece = board.getPieceAt(position);
            if (piece != null && piece.equals(playerColor + " king")) {
                return position;
            }
        }
        return null; // King not found (shouldn't happen in a valid game)
    }

    private boolean isPositionUnderAttack(Position position, String opponentColor) {
        // Check if the given position is under attack by any piece of the opponentColor
        for (Position attacker : board.getAllPositions(opponentColor)) {
            if (board.isMoveValid(attacker, position, opponentColor)) {
                return true;
            }
        }
        return false;
    }

    private String opponentColor(String color) {
        return color.equals("white") ? "black" : "white";
    }
}

