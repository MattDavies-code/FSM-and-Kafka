package com.example.chessfsm.chessfsm;

import com.example.chessfsm.chessfsm.fsm.ChessBoard;
import com.example.chessfsm.chessfsm.fsm.ChessFSM;
import com.example.chessfsm.chessfsm.redis.ChessBoardRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChessConfig {

    @Bean
    public ChessBoard chessBoard(ChessBoardRepository chessBoardRepository) {
        return new ChessBoard(chessBoardRepository);
    }

    @Bean
    public ChessFSM chessFSM(ChessBoard chessBoard) {
        return new ChessFSM(chessBoard);
    }
}
