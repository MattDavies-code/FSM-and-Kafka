package com.example.chessfsm.chessfsm;

import java.util.Map;

public class GameState {
    private String currentState;
    private Map<String, String> board; // Map of positions to pieces

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
