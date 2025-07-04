package dev.aissa.board;

import dev.aissa.enums.TileType;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tile {
    private int tileId;
    private TileType tileType;
    private Color teamTileColor;
    private int cellId;
}
