package dev.aissa.player;

import dev.aissa.board.Pawn;
import lombok.Data;

import java.util.List;
import java.util.stream.IntStream;

@Data
public class Player {
    private int playerId;
    private String name;
    private int rollOrder;

    private List<Pawn> pawns;

    public Player(int playerId, String name) {
        this.playerId = playerId;
        this.name = name;

        this.pawns = IntStream.range(0, 4)
                .mapToObj(index -> new Pawn(index, -1, this.playerId))
                .toList();
    }



}