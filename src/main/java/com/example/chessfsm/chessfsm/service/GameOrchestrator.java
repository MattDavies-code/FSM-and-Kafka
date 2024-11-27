package com.example.chessfsm.chessfsm.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.fsm.MoveValidation;
import com.example.chessfsm.chessfsm.fsm.StateChecker;
import com.example.chessfsm.chessfsm.kafka.ChessKafkaProducer;
import com.example.chessfsm.chessfsm.model.GameState;
import com.example.chessfsm.chessfsm.model.Position;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class GameOrchestrator {

    private final ChessBoard chessBoard;
    private final MoveValidation moveValidation;
    private final ChessFSM fsm;
    private final StateChecker stateChecker;
    private final ObjectMapper objectMapper;
    private final ChessKafkaProducer kafkaProducer;

    public GameOrchestrator(ChessBoard chessBoard, MoveValidation moveValidation, ChessFSM fsm, StateChecker stateChecker, ObjectMapper objectMapper, ChessKafkaProducer kafkaProducer) {
        this.chessBoard = chessBoard;
        this.moveValidation = moveValidation;
        this.fsm = fsm;
        this.stateChecker = stateChecker;
        this.objectMapper = objectMapper;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Initialize the chessboard with default positions.
     * PostConstruct is a lifecycle callback that gets executed after the bean has been initialised.
     */
    @PostConstruct
    public void initializeGame() {
        chessBoard.clearBoard();
        chessBoard.initializeBoard();
        System.out.println("Chessboard initialized with default positions.");
    }

    public void processMove(String message) {
        try {
            // Parse JSON message
            JsonNode moveJson = objectMapper.readTree(message);
            String playerColor = moveJson.get("player").asText();
            Position from = new Position(moveJson.get("from").asText());
            Position to = new Position(moveJson.get("to").asText());

            Map<String, String> boardState = chessBoard.getAllPositions();
            // Validate move
            if (!moveValidation.isMoveValid(from, to, playerColor, boardState)) {
                System.out.println("Invalid move.");
                return;
            }

            // Update the board
            chessBoard.movePiece(from, to);

            chessBoard.printBoard(); // Display the updated board

            // Check game conditions for a state change
            handleGameConditions(playerColor, from, to);

        } catch (Exception e) {
            System.err.println("Error processing move: " + e.getMessage());
        }
    }

    private void handleGameConditions(String playerColour, Position from, Position to) throws Exception {
        String opponentColour = stateChecker.opponentColour(playerColour);

        // GAME START
        if (fsm.getCurrentState().equals("START")) {
            fsm.transition("first_move");
        }
        // CHECK (NOT WORKING)
//        if (stateChecker.isKingInCheck(opponentColour, chessBoard.getAllPositions())) {
//            fsm.transition("CHECK");
//            System.out.println("Check on " + opponentColour + "'s king.");
//        }

        sendGameState(playerColour, from, to);
    }

    private void sendGameState(String playerColour, Position from, Position to) throws Exception {
        // Generate and log the game state JSON
        String currentState = fsm.getCurrentState();
        Map<String, String> boardState = chessBoard.getAllPositions();
        String nextTurn = stateChecker.opponentColour(playerColour);

        GameState.LastMove lastMove = new GameState.LastMove(chessBoard.getPieceAt(to), from.toString(), to.toString());
        GameState gameState = new GameState(currentState, boardState, nextTurn, lastMove);

        String gameStateJson = objectMapper.writeValueAsString(gameState);

        // Use the producer to send the message
        kafkaProducer.sendGameState(gameStateJson);
    }
}

