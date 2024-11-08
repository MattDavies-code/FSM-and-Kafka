package com.example.chessfsm.chessfsm.fsm;

import java.io.IOException;

import com.example.chessfsm.chessfsm.model.GameConfig;

public class ChessFSM {
    private final GameConfig gameConfig;
    private String currentState;

    // Constructor to load configuration from a JSON file
    public ChessFSM(String configPath) throws IOException {
        this.gameConfig = GameConfig.loadConfig(configPath);
        this.currentState = "START"; // Initial state as defined in the JSON
    }

    // Method to get the current state
    public String getCurrentState() {
        return currentState;
    }

    // Method to transition states based on an event
    public void transition(String event) {
        // Find a transition from the current state that matches the event
        for (GameConfig.Transition transition : gameConfig.transitions) {
            if (transition.from.equals(currentState) && transition.on.equals(event)) {
                currentState = transition.to; // Update current state
                System.out.println("Transitioned to state: " + currentState);
                break;
            }
        }
    }
}
