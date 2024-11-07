package com.example.chessfsm.chessfsm.fsm;

import org.springframework.stereotype.Component;

@Component
public class ChessFSM {
    private GameState currentState;

    public ChessFSM() {
        this.currentState = GameState.START; // Start in the initial state
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void startGame() {
        if (currentState == GameState.START) {
            transitionTo(GameState.IN_PROGRESS);
        }
    }

    public void transitionTo(GameState newState) {
        System.out.println("Transitioning from " + currentState + " to " + newState);
        this.currentState = newState;
    }

    public void checkForSpecialConditions(boolean isCheck, boolean isCheckmate, boolean isStalemate) {
        if (isCheckmate) {
            transitionTo(GameState.CHECKMATE);
        } else if (isStalemate) {
            transitionTo(GameState.STALEMATE);
        } else if (isCheck) {
            transitionTo(GameState.CHECK);
        } else {
            transitionTo(GameState.IN_PROGRESS);
        }
    }
}
