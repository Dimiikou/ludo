package dev.aissa.entity;

import dev.aissa.enums.Color;
import dev.aissa.enums.TileType;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
public class Board {

    private List<Tile> tiles;
    private List<Figure> figures;

    public Board(List<Player> players) {
        this.figures = new ArrayList<>();

        initializeTiles();
        for (Player player : players) {
            for (int i = 0; i < 4; i++) {
                this.figures.add(new Figure(-1, player.getTeamColor()));
            }
        }
    }

    public void moveFigure(Figure figure, int amount) {
        int currentTileId = figure.getLocationTileId();
        int targetTileId = currentTileId + amount;
        Color figureColor = figure.getColor();

        // Alle regulären Felder durchlaufen, um EntryTile zu erkennen
        List<Tile> passedTiles = this.tiles.stream()
                .filter(tile -> tile.getTileId() > currentTileId && tile.getTileId() <= targetTileId)
                .toList();

        Optional<Tile> passedEntryTile = passedTiles.stream()
                .filter(tile -> tile.getTileType() == TileType.ENTRY_TILE && tile.getTeamTileColor() == figureColor)
                .findFirst();

        // Wenn wir durch das EntryTile müssen
        if (passedEntryTile.isPresent()) {
            Tile entryTile = passedEntryTile.get();
            int tilesToReach = entryTile.getTileId() - currentTileId;
            int tilesToMove = amount - tilesToReach;

            if (tilesToMove > 4) {
                return;
            }

            Tile finishTarget = getFinishTiles(figureColor).get(tilesToMove);
            if (isTileOccupiedByOwnTeam(finishTarget.getTileId(), figureColor)) {
                return;
            }

            figure.setLocationTileId(finishTarget.getTileId());
            return;
        }

        // Normales Feld ohne EntryTile-Passage
        if (targetTileId < 40) {
            if (isTileOccupiedByOwnTeam(targetTileId, figureColor)) {
                return;
            }

            // Gegner schlagen
            figures.stream()
                    .filter(other -> other.getLocationTileId() == targetTileId && other.getColor() != figureColor)
                    .forEach(other -> other.setLocationTileId(-1));

            figure.setLocationTileId(targetTileId);
            return;
        }

        // Letzter Notfall-Fall: Direktes Ziehen auf Zielfeld (wenn erlaubt)
        List<Tile> finishTiles = getFinishTiles(figureColor);
        if (finishTiles.stream().anyMatch(tile -> tile.getTileId() == targetTileId)) {
            if (isTileOccupiedByOwnTeam(targetTileId, figureColor)) {
                return;
            }

            figure.setLocationTileId(targetTileId);
        }
    }

    public List<Tile> getFinishTiles(Color color) {
        return this.tiles.stream()
                .filter(tile -> tile.getTileType() == TileType.FINISH_TILE && tile.getTeamTileColor() == color)
                .sorted(Comparator.comparing(Tile::getTileId))
                .toList();
    }

    public List<Figure> getFiguresByColor(Color color) {
        return this.figures.stream()
                .filter(figure -> figure.getColor() == color)
                .toList();
    }

    public Tile getTeamStartingTile(Color color) {
        return this.tiles.stream()
                .filter(tile -> tile.getTileType() == TileType.START_TILE && tile.getTeamTileColor() == color)
                .findFirst()
                .orElse(null);
    }

    public Optional<Tile> getTileById(int id) {
        return this.tiles.stream()
                .filter(tile -> tile.getTileId() == id)
                .findFirst();
    }

    private boolean isTileOccupiedByOwnTeam(int tileId, Color color) {
        return this.figures.stream().anyMatch(figure -> figure.getLocationTileId() == tileId && figure.getColor() == color);
    }

    private void initializeTiles() {
        this.tiles = new ArrayList<>();
        int totalTiles = 40 + 4 * 4;
        for (int i = 0; i < totalTiles; i++) {
            this.tiles.add(new Tile(this.tiles.size(), TileType.NORMAL_TILE, Color.NONE));
        }

        // Setzt Start, Zielfelder und Entryfelder für die jeweiligen Teams.
        int startingTile = 0;
        int entryTile = 9;
        int finishTiles = 40;
        for (Color color : List.of(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW)) {
            getTileById(startingTile).ifPresent(tile -> {
                tile.setTileType(TileType.START_TILE);
                tile.setTeamTileColor(color);
            });
            startingTile += 10;

            getTileById(entryTile).ifPresent(tile -> {
                tile.setTileType(TileType.ENTRY_TILE);
                tile.setTeamTileColor(color);
            });
            entryTile += 10;

            for (int finishTileIndex = 0; finishTileIndex < 4; finishTileIndex++) {
                if (finishTiles > this.tiles.size()) {
                    break;
                }

                getTileById(finishTiles++).ifPresent(tile -> {
                    tile.setTileType(TileType.FINISH_TILE);
                    tile.setTeamTileColor(color);
                });
            }
        }
    }
}
