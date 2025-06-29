package dev.aissa.entity;

import dev.aissa.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Figure {
    private int locationTileId;
    private Color color;
}
