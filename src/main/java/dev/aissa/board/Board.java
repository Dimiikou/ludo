package dev.aissa.board;

import dev.aissa.exceptions.BoardPositionOccupiedException;
import dev.aissa.exceptions.InvalidMoveException;
import dev.aissa.player.Player;
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

    /**
     * Führt einen Spielzug für die übergebene Figur aus.
     * <p>
     * Die Figur wird um die angegebene Anzahl an Feldern bewegt, sofern dies erlaubt ist.
     * Dabei gelten folgende Bedingungen:
     * <ul>
     *   <li>Das Zielfeld darf nicht von einer Figur des eigenen Teams besetzt sein.</li>
     *   <li>Beim Einlaufen in die Zielfelder darf die maximale Feldanzahl nicht überschritten werden.</li>
     *   <li>Gegnerische Figuren auf dem Zielfeld werden ins "Zuhause" zurückgeworfen.</li>
     * </ul>
     *
     * @param figure Die zu bewegende Spielfigur.
     * @param amount Die Anzahl der zu ziehenden Felder.
     * @throws InvalidMoveException Wenn der Zug nicht gültig ist.
     * @throws BoardPositionOccupiedException Wenn das Zielfeld unrechtmäßig besetzt ist.
     */
    public void moveFigure(Figure figure, int amount) throws InvalidMoveException, BoardPositionOccupiedException {
        int currentTileId = figure.getLocationTileId();
        int targetTileId = currentTileId + amount;
        Color figureColor = figure.getColor();

        if (isTileOccupiedByOwnTeam(targetTileId, figureColor)) {
            throw new BoardPositionOccupiedException("Tile with ID: " + targetTileId + " already occupied");
        }

        List<Tile> passedTiles = this.tiles.stream()
                .filter(tile -> tile.getTileId() > currentTileId && tile.getTileId() <= targetTileId)
                .toList();

        Optional<Tile> passedEntryTile = passedTiles.stream()
                .filter(tile -> tile.getTileType() == TileType.ENTRY_TILE)
                .findFirst();

        if (passedEntryTile.isPresent()) {
            Tile entryTile = passedEntryTile.get();
            int tilesLeftAfterReachingEntryTile = entryTile.getTileId() - currentTileId;
            if (tilesLeftAfterReachingEntryTile > 4) {
                throw new InvalidMoveException("Target exceeds Board Size");
            }

            figure.setLocationTileId(getFinishTiles(figureColor).get(tilesLeftAfterReachingEntryTile).getTileId());
            return;
        }

        if (targetTileId < 40) {
            figure.setLocationTileId(targetTileId);
            figures.stream()
                    .filter(other -> other.getLocationTileId() == targetTileId && other.getColor() != figureColor)
                    .forEach(other -> other.setLocationTileId(-1));
            return;
        }

        List<Tile> finishTiles = getFinishTiles(figureColor);
        if (finishTiles.stream().noneMatch(tile -> tile.getTileId() == targetTileId && !isTileOccupiedByOwnTeam(tile.getTileId(), figureColor))) {
            throw new InvalidMoveException("Target is not reachable by Player");
        }

        figure.setLocationTileId(targetTileId);
    }

    /**
     * Findet die Zielfelder des gewünschten Teams
     * @param color Farbe des Teams
     * @return Liste an Tile
     */
    public List<Tile> getFinishTiles(Color color) {
        return this.tiles.stream()
                .filter(tile -> tile.getTileType() == TileType.FINISH_TILE && tile.getTeamTileColor() == color)
                .sorted(Comparator.comparing(Tile::getTileId))
                .toList();
    }

    /**
     * Findet alle Figuren einer Teamfarbe
     * @param color Farbe der gesuchten Figuren
     * @return Liste an Figure
     */
    public List<Figure> getFiguresByColor(Color color) {
        return this.figures.stream()
                .filter(figure -> figure.getColor() == color)
                .toList();
    }

    /**
     * Findet das Startfeld des gewünschten Teams
     * @param color Farbe des Teams
     * @return Tile
     */
    public Tile getTeamStartingTile(Color color) {
        return this.tiles.stream()
                .filter(tile -> tile.getTileType() == TileType.START_TILE && tile.getTeamTileColor() == color)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gibt das gesuchte Feld anhand seiner Id zurück
     * @param id TileId
     * @return Das gefundene Feld als Optional
     */
    public Optional<Tile> getTileById(int id) {
        return this.tiles.stream()
                .filter(tile -> tile.getTileId() == id)
                .findFirst();
    }

    /**
     *
     * Prüft, ob bereits eine eigene Figur auf dem Feld vorhanden ist
     * @param tileId TileId des zu prüfenden Feldes
     * @param color Farbe der Figuren welche dort nicht stehen dürfen
     * @return true / false
     */
    private boolean isTileOccupiedByOwnTeam(int tileId, Color color) {
        return this.figures.stream().anyMatch(figure -> figure.getLocationTileId() == tileId && figure.getColor() == color);
    }

    /**
     * Erstellt alle benötigten Felder für das Spiel samt Kategorisierung der TileTypes
     */
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
