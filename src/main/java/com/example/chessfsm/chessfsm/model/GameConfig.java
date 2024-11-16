package com.example.chessfsm.chessfsm.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
GameConfig class is responsible for loading the rules from a JSON rules file.
 */
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
        public String direction; // E.g., "vertical", "diagonal"
        public String steps;     // Can be "any" or a number as a string
        public boolean initial;  // True if move is allowed only at initial position (for pawns)
        public String pattern;   // For special patterns like "L" for knights
    }

    public static GameConfig loadConfig(String filePath) throws IOException {
        System.out.println("Loading GameConfig from: " + filePath);
        ObjectMapper mapper = new ObjectMapper();
        GameConfig config = mapper.readValue(new File(filePath), GameConfig.class);
        System.out.println("Loaded GameConfig: " + config.rules);
        return config;
    }
}

