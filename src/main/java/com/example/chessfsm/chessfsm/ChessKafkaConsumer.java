package com.example.chessfsm.chessfsm;

import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.Position;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChessKafkaConsumer {

    private final ChessFSM fsm;
    private final ChessBoard board;

    @Autowired
    public ChessKafkaConsumer(ChessFSM fsm, ChessBoard board) {
        this.fsm = fsm;
        this.board = board;
    }

    @KafkaListener(topics = "chess-moves", groupId = "chess-fsm-group")
    public void consume(String message) {
        // Parse the move message (implementation for parsing JSON depends on JSON library)
        String playerColor = "white"; // Placeholder, should parse from message
        Position from = new Position("A2"); // Placeholder, should parse from message
        Position to = new Position("A3"); // Placeholder, should parse from message

        // Validate the move
        boolean isValid = board.isMoveValid(from, to, playerColor);
        if (isValid) {
            System.out.println("Valid move for " + playerColor + ": " + from + " to " + to);
            board.movePiece(from, to); // Update board state in Redis
            fsm.checkForSpecialConditions(false, false, false); // Placeholder for actual conditions
        } else {
            System.out.println("Invalid move. Move rejected.");
        }

        System.out.println("Current game state: " + fsm.getCurrentState());
    }
}
