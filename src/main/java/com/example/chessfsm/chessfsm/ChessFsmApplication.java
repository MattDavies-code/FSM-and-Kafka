package com.example.chessfsm.chessfsm;

import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.fsm.MoveValidation;
import com.example.chessfsm.chessfsm.model.GameConfig;
import com.example.chessfsm.chessfsm.service.ChessBoardRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class ChessFsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessFsmApplication.class, args);
    }
    @Bean
    public ChessBoard chessBoard(ChessBoardRepository chessBoardRepository, GameConfig gameConfig, MoveValidation moveValidation) {
        return new ChessBoard(chessBoardRepository, gameConfig, moveValidation);
    }

    @Bean
    public ChessFSM chessFSM() throws IOException {
        String configPath = "src/main/resources/rules.json";
        return new ChessFSM(configPath);
    }

}
