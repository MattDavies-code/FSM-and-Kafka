package com.example.chessfsm.chessfsm.fsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.chessfsm.chessfsm.redis.ChessBoardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.plaf.nimbus.State;

@Component
public class ChessBoard {

    private final ChessBoardRepository boardRepository;

    @Autowired
    public ChessBoard(ChessBoardRepository boardRepository) {
        this.boardRepository = boardRepository;
        initializeBoard();
    }

    private void initializeBoard() {
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

    public void printBoard() {
        for (int rank = 8; rank >= 1; rank--) {
            System.out.print(rank + " ");
            for (char file = 'A'; file <= 'H'; file++) {
                Position position = new Position(file + String.valueOf(rank));
                String piece = boardRepository.getPieceAt(position.toString());
                if (piece != null) {
                    System.out.print(getPieceSymbol(piece) + " ");
                } else {
                    System.out.print(". ");
                }
            }
        }
        System.out.println("  A B C D E F G H");
    }

    private String getPieceSymbol(String piece) {
        switch (piece) {
            case "white pawn": return "P";
            case "white rook": return "R";
            case "white knight": return "N";
            case "white bishop": return "B";
            case "white queen": return "Q";
            case "white king": return "K";
            case "black pawn": return "p";
            case "black rook": return "r";
            case "black knight": return "n";
            case "black bishop": return "b";
            case "black queen": return "q";
            case "black king": return "k";
            default: return "?"; // Unknown piece
        }
    }

    public String getPieceAt(Position position) {
        return boardRepository.getPieceAt(position.toString());
    }

    public void movePiece(Position from, Position to) {
        String piece = boardRepository.getPieceAt(from.toString());
        if (piece != null) {
            boardRepository.deletePosition(from.toString());
            boardRepository.savePosition(to.toString(), piece);
        }
    }

    public Map<String, String> getAllPositions() {
        // Retrieve the board state from Redis as a Map
        return boardRepository.getAllPositions();
    }

    public List<Position> getAllValidMoves(Position from, String playerColor) {
        List<Position> validMoves = new ArrayList<>();
        MoveValidation moveValidator = new MoveValidation(this); // Assuming MoveValidation uses ChessBoard

        for (char file = 'A'; file <= 'H'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                Position to = new Position(file + String.valueOf(rank));
                if (moveValidator.isMoveValid(from, to, playerColor)) {
                    validMoves.add(to);
                }
            }
        }
        return validMoves;
    }

    public boolean simulateMoveAndCheck(Position from, Position to, String playerColor) {
        // Get current pieces at `from` and `to`
        String pieceAtFrom = boardRepository.getPieceAt(from.toString());
        String pieceAtTo = boardRepository.getPieceAt(to.toString());

        // Simulate the move in Redis
        boardRepository.savePosition(to.toString(), pieceAtFrom);
        boardRepository.deletePosition(from.toString());

        // Check if the king is in check after the move
        boolean isKingSafe = !new StateChecker(this).isKingInCheck(playerColor);

        // Undo the simulated move in Redis
        boardRepository.savePosition(from.toString(), pieceAtFrom);
        if (pieceAtTo != null) {
            boardRepository.savePosition(to.toString(), pieceAtTo);
        } else {
            boardRepository.deletePosition(to.toString());
        }

        return isKingSafe;
    }
}
