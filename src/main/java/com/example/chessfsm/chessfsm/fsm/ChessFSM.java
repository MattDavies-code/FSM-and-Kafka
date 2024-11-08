package com.example.chessfsm.chessfsm.fsm;

import com.example.chessfsm.chessfsm.GameConfig;
import org.springframework.stereotype.Component;

@Component
public class ChessFSM {


    private GameConfig gameConfig;
    private String currentState;

    public ChessFSM(String configPath) throws IOException {
        this.gameConfig = GameConfig.loadConfig(configPath);
        this.currentState = "START"; // Initial state as defined in the JSON
    }

    public String getCurrentState() {
        return currentState;
    }

    public void transition(String event) {
        // Check the JSON-configured transitions to update the FSM state based on the event
        for (GameConfig.Transition transition : gameConfig.transitions) {
            if (transition.from.equals(currentState) && transition.on.equals(event)) {
                currentState = transition.to;
                break;
            }
        }
    }

    // Starts the game
    public void startGame() {
        if (currentState == GameState.START) {
            transitionTo(GameState.IN_PROGRESS);
        }
    }

    // Process a move and update the FSM state based on game rules
    public boolean processMove(Position from, Position to, String playerColor) {
        if (currentState != GameState.IN_PROGRESS && currentState != GameState.CHECK) {
            System.out.println("Game is not in progress.");
            return false;
        }

        // Validate the move using the chess board
        if (!moveValidation.isMoveValid(from, to, playerColor)) {
            System.out.println("Invalid move.");
            return false;
        }

        // Update the board with the move
        chessBoard.movePiece(from, to);

        // Use StateChecker to determine the game state
        boolean isCheck = stateChecker.isKingInCheck(opponentColor(playerColor));
        boolean isCheckmate = stateChecker.isCheckmate(opponentColor(playerColor));
        boolean isStalemate = stateChecker.isStalemate(playerColor);

        if (isCheckmate) {
            transitionTo(GameState.CHECKMATE);
        } else if (isStalemate) {
            transitionTo(GameState.STALEMATE);
        } else if (isCheck) {
            transitionTo(GameState.CHECK);
        } else {
            transitionTo(GameState.IN_PROGRESS);
        };

        // Transition based on conditions
        if (isCheckmate) {
            transitionTo(GameState.CHECKMATE);
        } else if (isStalemate) {
            transitionTo(GameState.STALEMATE);
        } else if (isCheck) {
            transitionTo(GameState.CHECK);
        } else {
            transitionTo(GameState.IN_PROGRESS);
        }

        return true;
    }

    // Helper method to get the opponent's color
    private String opponentColor(String color) {
        return color.equals("white") ? "black" : "white";
    }
}
