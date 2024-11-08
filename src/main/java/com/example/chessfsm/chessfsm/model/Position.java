package com.example.chessfsm.chessfsm.model;

//Converts a chess board position in rank and file notation to a Position object
public class Position {
    private final char file; // A to H
    private final int rank;  // 1 to 8

    public Position(String notation) {
        if (notation.length() != 2) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }
        this.file = notation.toUpperCase().charAt(0);
        this.rank = Character.getNumericValue(notation.charAt(1));

        if (file < 'A' || file > 'H' || rank < 1 || rank > 8) {
            throw new IllegalArgumentException("Invalid board position: " + notation);
        }
    }

    public char getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return "" + file + rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return file == position.file && rank == position.rank;
    }

    @Override
    public int hashCode() {
        return 31 * file + rank;
    }
}
