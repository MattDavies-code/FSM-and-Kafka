package com.example.chessfsm.chessfsm.fsm;

public class ChessPiece {
    private final String type; // e.g., "pawn"
    private final String color; // "white" or "black"

    public ChessPiece(String type, String color) {
        this.type = type;
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return color + " " + type;
    }
}
