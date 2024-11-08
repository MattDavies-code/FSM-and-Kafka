package com.example.chessfsm.chessfsm.controller;

import com.example.chessfsm.chessfsm.kafka.ChessKafkaProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chess")
public class ChessController {

    private final ChessKafkaProducer kafkaProducer;

    public ChessController(ChessKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping("/move")
    public String makeMove(@RequestBody String move) {
        kafkaProducer.sendMessage("chess-moves", move);
        return "Move sent to Kafka: " + move;
    }
}
