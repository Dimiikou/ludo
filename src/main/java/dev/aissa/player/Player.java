package dev.aissa.player;

import dev.aissa.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Player {
    private int playerId;
    private String name;
    private Color teamColor;
    private int rollPosition;

    public int rollSingleDice() {
        return (int) (Math.random() * 6) + 1;
    }

    public int[] rollDoubleDice() {
        return new int[] { rollSingleDice(), rollSingleDice() };
    }
}
