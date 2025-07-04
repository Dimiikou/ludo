package dev.aissa.player;

import dev.aissa.interfaces.IDiceRoller;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Player implements IDiceRoller {
    private int playerId;
    private String name;
    private Color teamColor;
    private int rollPosition;

    public int rollSingleDice() {
        return (int) (Math.random() * 6) + 1;
    }
}
