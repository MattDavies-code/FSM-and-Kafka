package com.example.chessfsm.chessfsm.fsm;

import com.example.chessfsm.chessfsm.model.Position;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/*
StateChecker class is responsible for checking if a move changes the state of the game, such as check, checkmate, and stalemate has been reached.
 */
@Configuration
public class StateChecker {
    private final MoveValidation moveValidator;


    public StateChecker(MoveValidation moveValidator) {
        this.moveValidator = moveValidator;
    }

    /**
     * Checks if the given player's king is in check.
     *
     * @param playerColour The colour of the player whose king is being checked.
     * @return true if the king is in check, false otherwise.
     */
    public boolean isKingInCheck(String playerColour, Map<String, String> boardState) {
        // Find the king's position
        Position kingPosition = findKingPosition(playerColour, boardState);
        if (kingPosition == null) {
            throw new IllegalStateException("King not found for player: " + playerColour);
        }

        // Check if any opponent piece can attack the king's position
        String opponentColor = opponentColour(playerColour);
        for (Map.Entry<String, String> entry : boardState.entrySet()) {
            Position attackerPosition = new Position(entry.getKey());
            String attackerPiece = entry.getValue();

            // Only look at opponent pieces
            if (attackerPiece.startsWith(opponentColor)) {
                if (moveValidator.isMoveValid(attackerPosition, kingPosition, opponentColor, boardState)) {
                    return true; // King is in check
                }
            }
        }
        return false; // King is not in check
    }


    /**
     * Finds the position of the king for the specified player.
     *
     * @param playerColour The colour of the player whose king is being located.
     * @return The position of the king, or null if not found.
     */
    private Position findKingPosition(String playerColour, Map<String, String> boardState) {
        for (Map.Entry<String, String> entry : boardState.entrySet()) {
            if (entry.getValue().equals(playerColour + " king")) {
                return new Position(entry.getKey());
            }
        }
        return null; // King not found
    }

    public String opponentColour(String color) {
        return color.equals("white") ? "black" : "white";
    }
}

