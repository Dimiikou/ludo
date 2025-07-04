package dev.aissa.board;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pawn {
    private int pawnId;
    private int position;
    private int playerId;

    public int getStartPosition() {
        return ((playerId - 1) * 10) % Board.TOTAL_FIELDS;
    }
}
