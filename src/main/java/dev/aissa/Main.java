package dev.aissa;

import dev.aissa.game.LupoGame;

public class Main {

    public static void main(String[] args) {
        LupoGame game = new LupoGame();
        game.initializeGame();
        game.startGame();
    }
}