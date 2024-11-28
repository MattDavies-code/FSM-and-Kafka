package com.example.chessfsm.chessfsm;

import java.io.IOException;
import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.model.GameConfig;

/*
ChessFsmApplication class is the entry point of the application.
 */
@SpringBootApplication
public class ChessFsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessFsmApplication.class, args);
    }

    @Bean
    public ChessFSM chessFSM() throws IOException {
        String configPath = Objects.requireNonNull(getClass().getClassLoader().getResource("rules.json")).getPath();
        return new ChessFSM(configPath);
    }

    @Bean
    public GameConfig gameConfig() throws IOException {
        String configPath = Objects.requireNonNull(getClass().getClassLoader().getResource("rules.json")).getPath();
        return GameConfig.loadConfig(configPath);
    }
}
