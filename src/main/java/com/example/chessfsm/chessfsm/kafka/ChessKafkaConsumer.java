package com.example.chessfsm.chessfsm.kafka;

import com.example.chessfsm.chessfsm.fsm.StateChecker;
import com.example.chessfsm.chessfsm.model.GameState;
import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.MoveValidation;
import com.example.chessfsm.chessfsm.model.Position;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ChessKafkaConsumer {

    private final ChessFSM fsm;
    private final ChessBoard board;
    private final ObjectMapper objectMapper;
    private final MoveValidation moveValidation;
    private final StateChecker stateChecker;

    @Autowired
    public ChessKafkaConsumer(ChessFSM fsm, ChessBoard board, ObjectMapper objectMapper, MoveValidation moveValidation, StateChecker stateChecker) {
        this.fsm = fsm;
        this.board = board;
        this.objectMapper = objectMapper;
        this.moveValidation = moveValidation;
        this.stateChecker = stateChecker;
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
                board.movePiece(from, to);

                // Check for game conditions: check, checkmate, stalemate
                String opponentColour = stateChecker.opponentColour(playerColor);
                if (stateChecker.isKingInCheck(opponentColour)) {
                    if (stateChecker.isCheckmate(opponentColour)) {
                        fsm.transition("CHECKMATE");
                        System.out.println("Checkmate. " + playerColor + " wins");
                    } else {
                        fsm.transition("CHECK");
                        System.out.println("Check on " + opponentColour + "'s king");
                    }
                } else if (stateChecker.isStalemate(opponentColour)) {
                    fsm.transition("STALEMATE");
                    System.out.println("Stalemate. The game is a draw.");
                } else {
                    fsm.transition("IN_PLAY"); // No special condition, continue game
                }

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