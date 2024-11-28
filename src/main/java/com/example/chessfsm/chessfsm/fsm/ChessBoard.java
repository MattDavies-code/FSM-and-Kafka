package com.example.chessfsm.chessfsm.fsm;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.chessfsm.chessfsm.model.GameState;
import com.example.chessfsm.chessfsm.model.Position;
import com.example.chessfsm.chessfsm.service.ChessBoardRepository;

/**
ChessBoard class is responsible for managing the state of the chess board.
**/
@Component
public class ChessBoard {

    private final ChessBoardRepository boardRepository;
    private int moveCount; // Keeps track of the total number of moves made
    private GameState.LastMove lastMove; // Stores details of the last move made

    public ChessBoard(ChessBoardRepository boardRepository, ChessFSM fsm) {
        this.boardRepository = boardRepository;

    }
    public void initializeBoard() {
        // Initialize white pieces
        boardRepository.savePosition("A1", "white rook");
        boardRepository.savePosition("B1", "white knight");
        boardRepository.savePosition("C1", "white bishop");
        boardRepository.savePosition("D1", "white queen");
        boardRepository.savePosition("E1", "white king");
        boardRepository.savePosition("F1", "white bishop");
        boardRepository.savePosition("G1", "white knight");
        boardRepository.savePosition("H1", "white rook");

        // Initialize white pawns
        for (char file = 'A'; file <= 'H'; file++) {
            boardRepository.savePosition(file + "2", "white pawn");
        }

        // Initialize black pieces
        boardRepository.savePosition("A8", "black rook");
        boardRepository.savePosition("B8", "black knight");
        boardRepository.savePosition("C8", "black bishop");
        boardRepository.savePosition("D8", "black queen");
        boardRepository.savePosition("E8", "black king");
        boardRepository.savePosition("F8", "black bishop");
        boardRepository.savePosition("G8", "black knight");
        boardRepository.savePosition("H8", "black rook");

        // Initialize black pawns
        for (char file = 'A'; file <= 'H'; file++) {
            boardRepository.savePosition(file + "7", "black pawn");
        }
    }

    /**
     * Retrieve the current state of the board as a Map.
     * Key: Position (e.g., "A2"), Value: Piece (e.g., "white pawn").
     */
    public Map<String, String> getAllPositions() {
        return boardRepository.getAllPositions();
    }

    /**
     * Move a piece from one position to another.
     */
    public void movePiece(Position from, Position to) {
        String movingPiece = boardRepository.getPieceAt(from.toString());
        if (movingPiece != null) {
            System.out.println("Moving piece: " + movingPiece + " from " + from + " to " + to);
    
            // Check if the destination has an opponent's piece
            String capturedPiece = boardRepository.getPieceAt(to.toString());
            if (capturedPiece != null) {
                System.out.println("Captured piece: " + capturedPiece + " at " + to);
                // Caputered pieces kafka here or points tally stuff
            }
    
            // Update the board state
            boardRepository.deletePosition(from.toString()); // Clear the old position
            boardRepository.savePosition(to.toString(), movingPiece); // Place the piece in the new position
        } else {
            System.err.println("No piece found at " + from);
        }
    }

    /**
     * Get the piece at a specific position.
     */
    public String getPieceAt(Position position) {
        return boardRepository.getPieceAt(position.toString());
    }


    public void recordLastMove(String piece, String from, String to) {
        this.lastMove = new GameState.LastMove(piece, from, to);
    }

    // Get the current game state dynamically
    public GameState getCurrentGameState(ChessFSM fsm) {
        // Fetch the current FSM state
        String currentState = fsm.getCurrentState();

        // Get the current turn dynamically based on move count
        String currentTurn = (moveCount % 2 == 0) ? "white" : "black";

        // Return the GameState object
        return new GameState(currentState, getAllPositions(), currentTurn, lastMove);
    }



    public void clearBoard() {
        boardRepository.clearBoard();
    }

    public void printBoard() {
        System.out.println("   A  B  C  D  E  F  G  H");
        for (int rank = 8; rank >= 1; rank--) {
            System.out.print(rank + " ");
            for (char file = 'A'; file <= 'H'; file++) {
                Position position = new Position("" + file + rank);
                String piece = getPieceAt(position); // Use your getPieceAt() method here
                if (piece != null) {
                    System.out.print(" " + getPieceSymbol(piece) + " ");
                } else {
                    System.out.print(" . "); // Empty square
                }
            }
            System.out.println(" " + rank);
        }
        System.out.println("   A  B  C  D  E  F  G  H");
    }

    private String getPieceSymbol(String piece) {
        return switch (piece) {
            case "white pawn" -> "P";
            case "black pawn" -> "p";
            case "white rook" -> "R";
            case "black rook" -> "r";
            case "white knight" -> "N";
            case "black knight" -> "n";
            case "white bishop" -> "B";
            case "black bishop" -> "b";
            case "white queen" -> "Q";
            case "black queen" -> "q";
            case "white king" -> "K";
            case "black king" -> "k";
            default -> "?"; // Unknown piece
        }; 
    }


}
