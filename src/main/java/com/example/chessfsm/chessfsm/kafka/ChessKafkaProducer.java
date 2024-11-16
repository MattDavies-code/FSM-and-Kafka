package com.example.chessfsm.chessfsm.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * ChessKafkaProducer class is responsible for sending game state updates to a Kafka topic
 */
@Service
public class ChessKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ChessKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendGameState(String message) {
        String topic = "game-state-updates"; // Kafka topic to send game state updates
        kafkaTemplate.send(topic, message);
        System.out.println("Sent game state: " + message + " to topic: " + topic);
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Sent message: " + message + " to topic: " + topic);
    }
}
