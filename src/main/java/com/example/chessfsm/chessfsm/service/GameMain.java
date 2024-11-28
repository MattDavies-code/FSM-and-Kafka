package com.example.chessfsm.chessfsm.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.fsm.MoveValidation;
import com.example.chessfsm.chessfsm.fsm.StateChecker;
import com.example.chessfsm.chessfsm.kafka.ChessKafkaProducer;
import com.example.chessfsm.chessfsm.model.GameConfig;
import com.example.chessfsm.chessfsm.model.GameState;
import com.example.chessfsm.chessfsm.model.Position;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class GameMain {

    private final ChessBoard chessBoard;
    private final MoveValidation moveValidation;
    private final ChessFSM fsm;
    private final StateChecker stateChecker;
    private final ObjectMapper objectMapper;
    private final ChessKafkaProducer kafkaProducer;
    private final GameConfig gameConfig;

    private String currentTurn; // Tracks whose turn it is
    private final Map<String, Integer> capturedPieces; // Track captured pieces for both sides
    private final Map<String, Integer> playerScores;   // Track scores for each player

    public GameMain(ChessBoard chessBoard, MoveValidation moveValidation, ChessFSM fsm, StateChecker stateChecker, ObjectMapper objectMapper, ChessKafkaProducer kafkaProducer, GameConfig gameConfig) {
        this.chessBoard = chessBoard;
        this.moveValidation = moveValidation;
        this.fsm = fsm;
        this.stateChecker = stateChecker;
        this.objectMapper = objectMapper;
        this.kafkaProducer = kafkaProducer;
        this.gameConfig = gameConfig;

        this.currentTurn = gameConfig.generalRules.startingPlayer;

        this.capturedPieces = new HashMap<>();
        this.playerScores = new HashMap<>();
        this.playerScores.put("white", 0);
        this.playerScores.put("black", 0);

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

    @PostConstruct
    public void initKafkaProducer() {
        kafkaProducer.sendCaptureDetail("{}"); // Send a dummy message to initialize the producer (JUST TEMP TO GET RID OF LOGS)
    }

    public void processMove(String message) {
        try {
            // Parse JSON message
            JsonNode moveJson = objectMapper.readTree(message);
            String playerColour = moveJson.get("player").asText();
            Position from = new Position(moveJson.get("from").asText());
            Position to = new Position(moveJson.get("to").asText());

            Map<String, String> boardState = chessBoard.getAllPositions();

            //Validate turn
            if (!playerColour.equals(currentTurn)) {
                System.out.println("Invalid move: It is " + currentTurn + "'s turn, not " + playerColour + "'s.");
                return; 
            }

            // Validate move
            if (!moveValidation.isMoveValid(from, to, playerColour, boardState)) {
                System.out.println("Invalid move.");
                return;
            }

            // Update the board
            chessBoard.movePiece(from, to);
            
            chessBoard.printBoard(); // Display the updated board


            String capturedPiece = boardState.get(to.toString());
            if (capturedPiece != null && !capturedPiece.startsWith(playerColour)) {
                String pieceType = capturedPiece.split(" ")[1]; // E.g., "rook"
                int points = gameConfig.rules.get(pieceType).points;

                // Update captured pieces
                capturedPieces.merge(capturedPiece, 1, Integer::sum);

                // Update current player's score
                playerScores.merge(playerColour, points, Integer::sum);

                System.out.println("Captured " + capturedPiece + " worth " + points + " points.");
            }

            sendCaptureDetails(); 

            switchTurn();

            // Check game conditions for a state change
            handleGameConditions(playerColour, from, to);


        } catch (Exception e) {
            //System.err.println("Error processing move: " + e.getMessage()); (NEEDS FIXING)
        }
    }

    private void switchTurn() {
        currentTurn = currentTurn.equals("white") ? "black" : "white";
        System.out.println("Switching turn. It is now " + currentTurn + "'s turn.");

    }

    private void handleGameConditions(String playerColour, Position from, Position to) throws Exception {
        String opponentColour = stateChecker.opponentColour(playerColour);

        // GAME START
        if (fsm.getCurrentState().equals("START")) {
            fsm.transition("first_move");
        }
        //Checkmate condition
        // if (stateChecker.isKingInCheck(opponentColour, chessBoard.getAllPositions())) {
        //     if (stateChecker.isCheckmate(opponentColour, chessBoard.getAllPositions())) {
        //         fsm.transition("checkmate");
        //         System.out.println("Checkmate! " + playerColour + " wins.");
        //     } else {
        //         fsm.transition("check");
        //         System.out.println("Check on " + opponentColour + "'s king.");
        //     }
        // } else if (stateChecker.isStalemate(opponentColour, chessBoard.getAllPositions())) {
        //     fsm.transition("stalemate");
        //     System.out.println("Stalemate! The game is a draw.");
        // }

        sendGameState(playerColour, from, to);
        
    }

    private void sendGameState(String playerColour, Position from, Position to) throws Exception {
        // Generate and log the game state JSON
        String currentState = fsm.getCurrentState();
        Map<String, String> boardState = chessBoard.getAllPositions();
        String nextTurn = stateChecker.opponentColour(playerColour);

        GameState.LastMove lastMove = new GameState.LastMove(chessBoard.getPieceAt(to), from.toString(), to.toString());
        GameState GameState = new GameState(currentState, boardState, nextTurn, lastMove);

        String gameStateJson = objectMapper.writeValueAsString(GameState);

        // Use the producer to send the message
        kafkaProducer.sendGameState(gameStateJson);
    }

    public void sendCaptureDetails() {
        // Prepare capture details
        Map<String, Object> captureDetails = new HashMap<>();
        captureDetails.put("capturedPieces", capturedPieces);
        captureDetails.put("scores", playerScores);
    
        try {
            // Convert capture details to JSON
            String message = objectMapper.writeValueAsString(captureDetails);
    
            // Use Kafka producer to send the message
            kafkaProducer.sendCaptureDetail(message);
    
        } catch (JsonProcessingException e) {
            System.err.println("Failed to send capture details: " + e.getMessage());
        }
    }
    
    
}

