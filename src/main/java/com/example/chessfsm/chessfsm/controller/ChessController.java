package com.example.chessfsm.chessfsm.controller;

import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.kafka.ChessKafkaProducer;
import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.model.GameState;
import com.example.chessfsm.chessfsm.model.Position;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
* This class defines the REST API endpoints for the chess game.
* The makeMove method is called when a POST request is sent to /chess/move.
* The method sends the move to the Kafka topic called chess-moves.
*/
@RestController
@RequestMapping("/chess")
public class ChessController {

    private final ChessKafkaProducer kafkaProducer;
    private final ChessBoard chessBoard;
    private final ChessFSM fsm;

    public ChessController(ChessKafkaProducer kafkaProducer, ChessBoard chessBoard, ChessFSM fsm) {
        this.kafkaProducer = kafkaProducer;
        this.chessBoard = chessBoard;
        this.fsm = fsm;
    }

    // POST: Send a move to Kafka
    @PostMapping("/move")
    public String makeMove(@RequestBody String move) {
        kafkaProducer.sendMessage("chess-moves", move);
        return "Move sent to Kafka: " + move;
    }

    // GET: Fetch the current state of the board
    @GetMapping("/board")
    public Map<String, String> getBoardState() {
        return chessBoard.getAllPositions();
    }

    // GET: Fetch information about a specific position
    @GetMapping("/position/{pos}")
    public String getPieceAtPosition(@PathVariable String pos) {
        try {
            Position position = new Position(pos); // Convert String to Position
            String piece = chessBoard.getPieceAt(position);
            return (piece != null) ? piece : "No piece at this position";
        } catch (IllegalArgumentException e) {
            return "Invalid position format: " + pos;
        }
    }

    // GET: Fetch the current game state (state, turn, last move)
    @GetMapping("/game-state")
    public GameState getGameState() {
        return chessBoard.getCurrentGameState(fsm);
    }
}


