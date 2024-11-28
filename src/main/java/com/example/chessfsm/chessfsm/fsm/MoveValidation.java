package com.example.chessfsm.chessfsm.fsm;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.example.chessfsm.chessfsm.model.GameConfig;
import com.example.chessfsm.chessfsm.model.Position;

/*
* This class is responsible for validating if a move from one position to another is allowed for the piece type.
* A stateless service that validates moves based on the current state of the board passed to it as an argument.
 */
@Configuration
public class MoveValidation {

    private final GameConfig gameConfig;

    @Autowired
    public MoveValidation(GameConfig gameConfig) {
        if (gameConfig.rules == null) {
            throw new IllegalStateException("GameConfig rules are null. Ensure rules.json is loaded correctly.");
        }
        this.gameConfig = gameConfig;
    }

    /**
     * Validate if a move is valid based on the piece rules in GameConfig.
     *
     * @param from        Starting position of the move.
     * @param to          Target position of the move.
     * @param playerColor The color of the player making the move.
     * @param boardState  Current state of the board as a Map.
     * @return true if the move is valid, false otherwise.
     */
     public boolean isMoveValid(Position from, Position to, String playerColor, Map<String, String> boardState) {
        String piece = boardState.get(from.toString());
        
        if (piece == null || !piece.startsWith(playerColor)) {
            return false; // No piece at the starting position or piece doesn't belong to the player
        }

        // Extract piece type (e.g., "pawn", "rook")
        String pieceType = piece.split(" ")[1];
        

        // Retrieve piece rules from GameConfig
        GameConfig.PieceRules pieceRules = gameConfig.rules.get(pieceType);
        if (pieceRules == null) {
            return false; // No rules defined for this piece type
        }
        System.out.println("Piece at " + from + ": " + piece);
        System.out.println("Piece type: " + pieceType);
        System.out.println("Player color: " + playerColor);


         for (GameConfig.Move rule : pieceRules.moves) {
             if (isValidBasedOnRule(rule, from, to, playerColor, boardState, pieceType)) {
                 return true;
             }
         }
         return false; // If no rules matched, the move is invalid
     }



    private boolean isValidBasedOnRule(GameConfig.Move rule, Position from, Position to, String playerColor,
                                       Map<String, String> boardState, String pieceType) {

        // Ensure the move does not target a square occupied by the player's own piece
        String targetPiece = boardState.get(to.toString());
        if (targetPiece != null && targetPiece.startsWith(playerColor)) {
            return false; // Cannot capture your own piece
        }

        String direction = rule.direction; // Read direction dynamically from rules.json

        System.out.println("Processing rule: " + rule);
        System.out.println("Rule pattern: " + rule.pattern);
        System.out.println("--------------------");

        // Special handling for knights and other non-directional pieces
        if ("L".equals(rule.pattern)) {
            System.out.println("Processing knight rule: " + rule);
            return validateKnightMove(rule, from, to);
        }

        switch (direction) {
            case "forward" -> {
                return validatePawnMove(rule, from, to, playerColor, boardState);
            }
            case "vertical", "horizontal", "diagonal" -> {
                return validatePath(from, to, boardState); // Directly call validatePath
            }
            default -> {
                System.out.println(rule.pattern);
                if ("L".equals(rule.pattern)) {
                    System.out.println("Processing knight rule: " + rule);
                    return validateKnightMove(rule, from, to);
                }
        
                //Special case for king
                if ("king".equals(pieceType)) {
                    return validateKingMove(rule, from, to, playerColor, boardState, new StateChecker(MoveValidation.this));
                }
        
                return false; // Unsupported direction
            }
        }
    }


    private boolean validatePawnMove(GameConfig.Move rule, Position from, Position to, String playerColor, Map<String, String> boardState) {
        // Determine move direction based on player color
        int direction = playerColor.equals("white") ? 1 : -1;
        int fromRank = from.getRank();
        int toRank = to.getRank();
        char fromFile = from.getFile();
        char toFile = to.getFile();

        // Rule only applies to "forward" movement
        if (!rule.direction.equals("forward") || fromFile != toFile) {
            return false;
        }

        // Parse step count from the rule
        int steps = parseSteps(rule.steps);

        // Validate single-step move
        if (steps == 1 && toRank == fromRank + direction) {
            return true; // Valid single-step forward move
        }

        // Validate double-step move
        if (steps == 2 && rule.initial) {
            // Double-step allowed only if pawn is at its initial position
            if ((playerColor.equals("white") && fromRank == 2 && toRank == 4) ||
                    (playerColor.equals("black") && fromRank == 7 && toRank == 5)) {
                return true; // Valid double-step move from initial position
            }
        }

        // Check diagonal capturing
        if (rule.direction.equals("diagonal") && Math.abs(toFile - fromFile) == 1 && toRank == fromRank + direction) {
            String targetPiece = boardState.get(to.toString());
            if (targetPiece != null && !targetPiece.startsWith(playerColor)) {
                return true; // Capture is valid if an opponent's piece is present
            }
        }

        return false; // Invalid move for this rule
    }





    private boolean validateDiagonalPath(Position from, Position to, Map<String, String> boardState) {
        int fromRank = from.getRank();
        int toRank = to.getRank();
        char fromFile = from.getFile();
        char toFile = to.getFile();

        // Determine the direction of movement (up-right, up-left, down-right, down-left)
        int rankStep = (toRank > fromRank) ? 1 : -1;
        int fileStep = (toFile > fromFile) ? 1 : -1;

        // Check each square in the path
        int rank = fromRank + rankStep;
        char file = (char) (fromFile + fileStep);
        while (rank != toRank && file != toFile) {
            String positionKey = file + String.valueOf(rank);
            if (boardState.containsKey(positionKey)) {
                return false; // Path is obstructed
            }
            rank += rankStep;
            file += (char) fileStep;
        }

        // Check the destination square
        String targetPiece = boardState.get(to.toString());
        return targetPiece == null || !targetPiece.startsWith(boardState.get(from.toString()).split(" ")[0]);
    }

    private boolean validateHorizontalPath(Position from, Position to, Map<String, String> boardState) {
        int rank = from.getRank();
        char fromFile = from.getFile();
        char toFile = to.getFile();

        // Determine the direction of movement (left or right)
        int step = (toFile > fromFile) ? 1 : -1;

        // Check each square in the path
        for (char file = (char) (fromFile + step); file != toFile; file += (char) step) {
            String positionKey = file + String.valueOf(rank);
            if (boardState.containsKey(positionKey)) {
                return false; // Path is obstructed
            }
        }

        // Check the destination square
        String targetPiece = boardState.get(to.toString());
        return targetPiece == null || !targetPiece.startsWith(boardState.get(from.toString()).split(" ")[0]);
    }

    private boolean validateVerticalPath(Position from, Position to, Map<String, String> boardState) {
        int fromRank = from.getRank();
        int toRank = to.getRank();
        char file = from.getFile();

        // Determine the direction of movement (up or down)
        int step = (toRank > fromRank) ? 1 : -1;

        // Check each square in the path
        for (int rank = fromRank + step; rank != toRank; rank += step) {
            String positionKey = file + String.valueOf(rank);
            if (boardState.containsKey(positionKey)) {
                return false; // Path is obstructed
            }
        }

        // Check the destination square
        String targetPiece = boardState.get(to.toString());
        return targetPiece == null || !targetPiece.startsWith(boardState.get(from.toString()).split(" ")[0]);
    }

    private boolean validateKnightMove(GameConfig.Move rule, Position from, Position to) {
        int rankDifference = Math.abs(from.getRank() - to.getRank());
        int fileDifference = Math.abs(from.getFile() - to.getFile());
    
        System.out.println("Validating Knight Move:");
        System.out.println("From: " + from + ", To: " + to);
        System.out.println("Rank Difference: " + rankDifference + ", File Difference: " + fileDifference);
    
        boolean isValid = rule.pattern.equals("L") && 
                          ((rankDifference == 2 && fileDifference == 1) || 
                           (rankDifference == 1 && fileDifference == 2));
        
        System.out.println("Is Valid: " + isValid);
        return isValid;
    }

    private boolean validateKingMove(GameConfig.Move rule, Position from, Position to, String playerColor,
                                     Map<String, String> boardState, StateChecker stateChecker) {
        int rankDifference = Math.abs(from.getRank() - to.getRank());
        int fileDifference = Math.abs(from.getFile() - to.getFile());

        // Validate direction and step size
        if ((rule.direction.equals("vertical") && rankDifference == 1 && fileDifference == 0) ||
                (rule.direction.equals("horizontal") && rankDifference == 0 && fileDifference == 1) ||
                (rule.direction.equals("diagonal") && rankDifference == 1 && fileDifference == 1)) {

            // Ensure the target square is not occupied by the player's own piece
            String targetPiece = boardState.get(to.toString());
            if (targetPiece != null && targetPiece.startsWith(playerColor)) {
                return false; // Cannot move to a square occupied by your own piece
            }

            // Check that the king does not move into a position that would place it in check
            Map<String, String> simulatedBoard = simulateMove(boardState, from, to);
            return !stateChecker.isKingInCheck(playerColor, simulatedBoard);
        }

        return false; // Invalid move for the king
    }

    /**
     * Simulate a move on a copy of the board state.
     *
     * @param boardState Current state of the board as a Map.
     * @param from       Starting position of the move.
     * @param to         Target position of the move.
     * @return A copy of the board state with the move applied.
     */
    private Map<String, String> simulateMove(Map<String, String> boardState, Position from, Position to) {
        Map<String, String> simulatedBoard = new HashMap<>(boardState);
        String piece = simulatedBoard.remove(from.toString()); // Remove piece from original position
        simulatedBoard.put(to.toString(), piece); // Place piece in new position
        return simulatedBoard;
    }


    private boolean validatePath(Position from, Position to, Map<String, String> boardState) {
        int fromRank = from.getRank();
        int toRank = to.getRank();
        char fromFile = from.getFile();
        char toFile = to.getFile();

        int rankDifference = Math.abs(fromRank - toRank);
        int fileDifference = Math.abs(fromFile - toFile);

        // Determine direction dynamically
        boolean isVertical = fromFile == toFile;
        boolean isHorizontal = fromRank == toRank;
        boolean isDiagonal = rankDifference == fileDifference;

        if (isVertical) {
            return validateVerticalPath(from, to, boardState);
        } else if (isHorizontal) {
            return validateHorizontalPath(from, to, boardState);
        } else if (isDiagonal) {
            return validateDiagonalPath(from, to, boardState);
        }

            return false; // Invalid path for the given direction
        }


    /**
     * Helper method to parse "steps" value from rules.json.
     * Handles "any" or numeric values (such as 1 or 2).
     */
    private int parseSteps(String steps) {
        if ("any".equals(steps)) {
            return Integer.MAX_VALUE; // Treat "any" as a very large number
        }
        return Integer.parseInt(steps);
    }
}

