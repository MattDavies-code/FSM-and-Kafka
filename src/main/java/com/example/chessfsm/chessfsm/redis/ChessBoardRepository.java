package com.example.chessfsm.chessfsm.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChessBoardRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, String> hashOperations;

    @Autowired
    public ChessBoardRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    private static final String BOARD_KEY = "chessBoard";

    public void savePosition(String position, String piece) {
        hashOperations.put(BOARD_KEY, position, piece);
        System.out.println("Saved " + piece + " at " + position + " in Redis"); // For debugging
    }

    public String getPieceAt(String position) {
        return hashOperations.get(BOARD_KEY, position);
    }

    public void deletePosition(String position) {
        hashOperations.delete(BOARD_KEY, position);
    }

    public void clearBoard() {
        redisTemplate.delete(BOARD_KEY);
    }
}
