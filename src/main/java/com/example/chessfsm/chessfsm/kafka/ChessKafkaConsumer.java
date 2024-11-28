package com.example.chessfsm.chessfsm.kafka;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.chessfsm.chessfsm.service.GameMain;

/*
* This class listens for messages on the Kafka topic "chess-moves" and sends it to GameOrchestrator.
 */
@Service
public class ChessKafkaConsumer {

    private final GameMain orchestrator;

    @Autowired
    public ChessKafkaConsumer(GameMain orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = "chess-moves", groupId = "chess-fsm-group")
    public void consume(String event) {
        orchestrator.processMove(event);
    }
}