package com.example.chessfsm.chessfsm.fsm;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.example.chessfsm.chessfsm.model.Position;

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
        //Find the king's position
        Position kingPosition = findKingPosition(playerColour, boardState);
        if (kingPosition == null) {
            throw new IllegalStateException("King not found for player: " + playerColour);
        }

        // Check if any opponent piece can attack the king's position
        String opponentColour = opponentColour(playerColour);
        for (Map.Entry<String, String> entry : boardState.entrySet()) {
            Position attackerPosition = new Position(entry.getKey());
            String attackerPiece = entry.getValue();

            // Only look at opponent pieces
            if (attackerPiece.startsWith(opponentColour)) {
                if (moveValidator.isMoveValid(attackerPosition, kingPosition, opponentColour, boardState)) {
                    return true; // King is in check
                }
            }
        }
        return false; // King is not in check
    }

        /**
     * Checks if the given player's king is in checkmate.
     *
     * @param playerColour The colour of the player whose king is being checked.
     * @param boardState   The current state of the board.
     * @return true if the player is in checkmate, false otherwise.
     */
    public boolean isCheckmate(String playerColour, Map<String, String> boardState) {
        // First, check if the king is in check
        if (!isKingInCheck(playerColour, boardState)) {
            return false; // Not in check, so not checkmate
        }

        // Iterate over all pieces belonging to the player
        for (Map.Entry<String, String> entry : boardState.entrySet()) {
            String piece = entry.getValue();
            if (piece.startsWith(playerColour)) {
                Position from = new Position(entry.getKey());

                // Test all possible positions on the board
                for (char file = 'A'; file <= 'H'; file++) {
                    for (int rank = 1; rank <= 8; rank++) {
                        Position to = new Position(file + String.valueOf(rank));

                        // Validate the move and check if it resolves the check
                        if (moveValidator.isMoveValid(from, to, playerColour, boardState)) {
                            // Simulate the move on a copy of the board state
                            Map<String, String> simulatedBoard = simulateMove(boardState, from, to);

                            // Check if the king is still in check after this move
                            if (!isKingInCheck(playerColour, simulatedBoard)) {
                                return false; // Found a move that resolves the check
                            }
                        }
                    }
                }
            }
        }

        return true; // No valid moves found to resolve the check
    }

    /**
     * Checks if the given player is in stalemate.
     *
     * @param playerColour The colour of the player being checked for stalemate.
     * @param boardState   The current state of the board.
     * @return true if the player is in stalemate, false otherwise.
     */
    public boolean isStalemate(String playerColour, Map<String, String> boardState) {
        // The king must not be in check for a stalemate to occur
        if (isKingInCheck(playerColour, boardState)) {
            return false; // Not a stalemate because the player is in check
        }

        // Iterate over all pieces belonging to the player
        for (Map.Entry<String, String> entry : boardState.entrySet()) {
            String piece = entry.getValue();
            if (piece.startsWith(playerColour)) {
                Position from = new Position(entry.getKey());

                // Test all possible moves for this piece
                for (char file = 'A'; file <= 'H'; file++) {
                    for (int rank = 1; rank <= 8; rank++) {
                        Position to = new Position(file + String.valueOf(rank));

                        // Check if the move is valid
                        if (moveValidator.isMoveValid(from, to, playerColour, boardState)) {
                            // Simulate the move to ensure it doesn't leave the king in check
                            Map<String, String> simulatedBoard = simulateMove(boardState, from, to);
                            if (!isKingInCheck(playerColour, simulatedBoard)) {
                                return false; // Found at least one valid move, so it's not a stalemate
                            }
                        }
                    }
                }
            }
        }

        return true; // No valid moves and not in check; stalemate
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

    private Map<String, String> simulateMove(Map<String, String> boardState, Position from, Position to) {
        // Create a new HashMap to represent the simulated board state
        Map<String, String> simulatedBoard = new HashMap<>(boardState);

        // Simulate the move by removing the piece from the starting position
        String piece = simulatedBoard.remove(from.toString()); 

        // Add the piece to the target position
        simulatedBoard.put(to.toString(), piece);

        // Return the simulated board state
        return simulatedBoard;
    }

    public String opponentColour(String color) {
        return color.equals("white") ? "black" : "white";
    }
}

