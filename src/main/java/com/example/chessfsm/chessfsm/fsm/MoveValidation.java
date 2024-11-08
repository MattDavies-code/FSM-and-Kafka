package com.example.chessfsm.chessfsm.fsm;

import com.example.chessfsm.chessfsm.fsm.Position;
import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.GameConfig;

public class MoveValidation {

    private final ChessBoard chessBoard;
    private final GameConfig gameConfig;

    public MoveValidation(ChessBoard chessBoard, GameConfig gameConfig) {
        this.chessBoard = chessBoard;
        this.gameConfig = gameConfig;
    }

    public boolean isMoveValid(Position from, Position to, String playerColor) {
        String piece = chessBoard.getPieceAt(from);
        if (piece == null || !piece.startsWith(playerColor)) {
            return false; // No piece or opponent's piece
        }

        String pieceType = piece.split(" ")[1];
        GameConfig.PieceRules pieceRules = gameConfig.rules.get(pieceType);
        if (pieceRules == null) {
            return false; // Unknown piece type
        }

        for (GameConfig.Move move : pieceRules.moves) {
            if (isValidMovePattern(from, to, move)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidMovePattern(Position from, Position to, GameConfig.Move move) {
        // Implement movement logic based on `direction`, `steps`, and other JSON-configured rules
        if ("forward".equals(move.direction)) {
            int stepCount = Math.abs(to.getRank() - from.getRank());
            return stepCount == move.steps;
        }
        // Additional checks for directions, steps, and other patterns
        return false;
    }
}
