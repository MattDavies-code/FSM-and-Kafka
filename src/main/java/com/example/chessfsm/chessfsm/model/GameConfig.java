package com.example.chessfsm.chessfsm.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/*
GameConfig class is responsible for loading the rules from a JSON rules file.
 */
public class GameConfig {
    public List<String> states;
    public List<Transition> transitions;
    public Map<String, PieceRules> rules;
    public GeneralRules generalRules; // E.g., starting player, board size

    public static class Transition {
        public String from;
        public String to;
        public String on;
    }

    public static class GeneralRules {
        public String startingPlayer; // "white" or "black"
    }

    public static class PieceRules {
        public List<Move> moves;
        public int points;
    }

    public static class Move {
        public String direction; // E.g., "vertical", "diagonal"
        public String steps;     // Can be "any" or a number as a string
        public boolean initial;  // True if move is allowed only at initial position (for pawns)
        public String pattern;   // For special patterns like "L" for knights
        public String capture;   // is diagonal for pawns
    }

    public static GameConfig loadConfig(String filePath) throws IOException {
        System.out.println("Loading GameConfig from: " + filePath);
        ObjectMapper mapper = new ObjectMapper();
        GameConfig config = mapper.readValue(new File(filePath), GameConfig.class);
        System.out.println("Loaded GameConfig: " + config.rules);
        return config;
    }
}

