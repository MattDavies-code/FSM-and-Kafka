package com.example.chessfsm.chessfsm;

import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.MoveValidation;
import com.example.chessfsm.chessfsm.fsm.Position;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChessKafkaConsumer {

    private final ChessFSM fsm;
    private final ChessBoard board;
    private final ObjectMapper objectMapper;
    private final MoveValidation moveValidation;

    @Autowired
    public ChessKafkaConsumer(ChessFSM fsm, ChessBoard board, ObjectMapper objectMapper, MoveValidation moveValidation) {
        this.fsm = fsm;
        this.board = board;
        this.objectMapper = objectMapper;
        this.moveValidation = moveValidation;
    }

    @KafkaListener(topics = "chess-moves", groupId = "chess-fsm-group")
    public void consume(String message) {
        try {
            // Parse the JSON message
            JsonNode moveJson = objectMapper.readTree(message);
            String playerColor = moveJson.get("player").asText();
            Position from = new Position(moveJson.get("from").asText());
            Position to = new Position(moveJson.get("to").asText());

            // Validate the move
            if (moveValidation.isMoveValid(from, to, playerColor)) {
                // Move is valid, perform the move and update state
                chessBoard.movePiece(from, to);
                fsm.transition("move");

                // Output the current game state as JSON
                sendGameState();
            } else {
                System.out.println("Invalid move or move rejected.");
            }
        } catch (Exception e) {
            System.err.println("Failed to process move message: " + e.getMessage());
        }
        board.printBoard();
    }

    public void sendGameState() throws Exception {
        // Create the JSON representation of the game state
        GameState gameState = new GameState(fsm.getCurrentState(), board.getAllPositions());
        String gameStateJson = objectMapper.writeValueAsString(gameState);

        // Output the game state JSON or send to another Kafka topic
        System.out.println("Game State JSON: " + gameStateJson);
    }
}