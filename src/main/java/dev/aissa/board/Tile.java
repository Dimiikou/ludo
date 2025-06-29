package dev.aissa.board;

import dev.aissa.enums.Color;
import dev.aissa.enums.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tile {
    private int tileId;
    private TileType tileType;
    private Color teamTileColor;
}
