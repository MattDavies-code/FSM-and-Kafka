package com.example.chessfsm.chessfsm.fsm;

import com.example.chessfsm.chessfsm.model.GameConfig;
import com.example.chessfsm.chessfsm.model.Position;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MoveValidation {
    private final ChessBoard chessBoard;
    private final GameConfig gameConfig;

    public MoveValidation(ChessBoard chessBoard, GameConfig gameConfig) {
        this.chessBoard = chessBoard;
        this.gameConfig = gameConfig;
    }

    // Validates if a move from one position to another is allowed for the piece type
    public boolean isMoveValid(Position from, Position to, String playerColor) {
        String piece = chessBoard.getPieceAt(from);
        if (piece == null || !piece.startsWith(playerColor)) {
            return false; // No piece or opponent's piece at the 'from' position
        }

        String pieceType = piece.split(" ")[1]; // Extract piece type (e.g., "pawn", "rook")
        GameConfig.PieceRules pieceRules = gameConfig.rules.get(pieceType);
        if (pieceRules == null) {
            return false; // Unknown piece type
        }

        // Check each valid move pattern for the piece
        for (GameConfig.Move move : pieceRules.moves) {
            if (isValidMovePattern(from, to, move)) {
                return true;
            }
        }
        return false;
    }

    // Helper method to check if a move matches a given pattern defined in GameConfig
    private boolean isValidMovePattern(Position from, Position to, GameConfig.Move move) {
        // Implement basic move validation based on `direction`, `steps`, and `initial`
        if ("forward".equals(move.direction)) {
            int stepCount = Math.abs(to.getRank() - from.getRank());
            return stepCount == move.steps;
        }
        // Additional logic for other directions and patterns goes here (e.g for rook, bishop, knight)
        return false;
    }
}

