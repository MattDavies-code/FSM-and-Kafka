package com.example.chessfsm.chessfsm.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
public class GameConfig {
    public List<String> states;
    public List<Transition> transitions;
    public Map<String, PieceRules> rules;

    public static class Transition {
        public String from;
        public String to;
        public String on;
    }

    public static class PieceRules {
        public List<Move> moves;
    }

    public static class Move {
        public String direction;
        public int steps;
        public boolean initial; // For moves allowed only at the initial position
        public String pattern; // For knight's L-shaped move
    }

    public static GameConfig loadConfig(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), GameConfig.class);
    }
}

