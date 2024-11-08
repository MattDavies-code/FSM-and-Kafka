package com.example.chessfsm.chessfsm;

import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChessFsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChessFsmApplication.class, args);
    }
    @Bean
    public ChessBoard chessBoard() {
        return new ChessBoard();
    }

    @Bean
    public ChessFSM chessFSM(ChessBoard chessBoard) {
        return new ChessFSM(chessBoard);
    }

}
