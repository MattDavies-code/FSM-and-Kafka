package com.example.chessfsm.chessfsm.fsm;

import java.util.HashMap;
import java.util.Map;
import com.example.chessfsm.chessfsm.redis.ChessBoardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChessBoard {

    private final ChessBoardRepository boardRepository;

    @Autowired
    public ChessBoard(ChessBoardRepository boardRepository) {
        this.boardRepository = boardRepository;
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialise pawns as an example
        for (char file = 'A'; file <= 'H'; file++) {
            boardRepository.savePosition(file + "2", "white pawn");
            boardRepository.savePosition(file + "7", "black pawn");
        }
        // Add other pieces here
    }

    public String getPieceAt(Position position) {
        return boardRepository.getPieceAt(position.toString());
    }

    public boolean isMoveValid(Position from, Position to, String playerColor) {
        String piece = boardRepository.getPieceAt(from.toString());
        if (piece == null || !piece.contains(playerColor) || !piece.contains("pawn")) {
            return false; // Only handle pawns for this vertical cut
        }

        int fromRank = from.getRank();
        int toRank = to.getRank();
        char fromFile = from.getFile();
        char toFile = to.getFile();

        // Basic rules for pawn movement
        if (playerColor.equals("white")) {
            if (fromFile == toFile && (toRank == fromRank + 1 || (fromRank == 2 && toRank == 4))) {
                return true; // Valid pawn move forward
            }
        } else if (playerColor.equals("black")) {
            if (fromFile == toFile && (toRank == fromRank - 1 || (fromRank == 7 && toRank == 5))) {
                return true; // Valid pawn move forward
            }
        }

        return false; // Invalid move
    }

    public void movePiece(Position from, Position to) {
        String piece = boardRepository.getPieceAt(from.toString());
        if (piece != null) {
            boardRepository.deletePosition(from.toString());
            boardRepository.savePosition(to.toString(), piece);
        }
    }
}
