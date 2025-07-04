package dev.aissa.board;

import java.util.Random;

public class Dice {
    private final Random random = new Random();

    public int roll() {
        return random.nextInt(6) + 1;
    }
}
