package com.example.chessfsm.chessfsm.kafka;


import com.example.chessfsm.chessfsm.service.GameOrchestrator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

/*
* This class listens for messages on the Kafka topic "chess-moves" and sends it to GameOrchestrator.
 */
@Service
public class ChessKafkaConsumer {

    private final GameOrchestrator orchestrator;

    @Autowired
    public ChessKafkaConsumer(GameOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = "chess-moves", groupId = "chess-fsm-group")
    public void consume(String event) {
        orchestrator.processMove(event);
    }
}