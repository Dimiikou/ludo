package dev.aissa.board;

import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Figure {
    private int locationTileId;
    private Color color;
}
