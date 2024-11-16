package com.example.chessfsm.chessfsm.model;

import java.util.Map;
/*
GameState class represents the current state of the chess game.
 */
public class GameState {
    private final String currentState;           // Game state like "IN_PLAY", "CHECK", etc.
    private final Map<String, String> board;     // Map of positions to pieces
    private final String currentTurn;            // Current player's turn
    private final LastMove lastMove;             // Details of the last move

    public GameState(String currentState, Map<String, String> board, String currentTurn, LastMove lastMove) {
        this.currentState = currentState;
        this.board = board;
        this.currentTurn = currentTurn;
        this.lastMove = lastMove;
    }

    public String getCurrentState() {
        return currentState;
    }

    public Map<String, String> getBoard() {
        return board;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public LastMove getLastMove() {
        return lastMove;
    }

    // Nested class for the details of the last move
        public record LastMove(String piece, String from, String to) {
    }
}

