package com.example.chessfsm.chessfsm.model;

import java.util.Map;

public class GameState {
    private final String currentState;
    private final Map<String, String> board; // Map of positions to pieces

    public GameState(String currentState, Map<String, String> board) {
        this.currentState = currentState;
        this.board = board;
    }

    public String getCurrentState() {
        return currentState;
    }

    public Map<String, String> getBoard() {
        return board;
    }
}
