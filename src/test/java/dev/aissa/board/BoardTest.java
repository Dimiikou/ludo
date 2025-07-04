package dev.aissa.board;

import dev.aissa.exceptions.BoardPositionOccupiedException;
import dev.aissa.exceptions.InvalidMoveException;
import dev.aissa.player.Player;
import dev.aissa.enums.TileType;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board Tests")
class BoardTest {

    private Board board;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        // Echte Player-Objekte erstellen
        players = Arrays.asList(
                createPlayer(Color.BLUE, 1, "Player 1"),
                createPlayer(Color.RED, 2, "Player 2"),
                createPlayer(Color.GREEN, 3, "Player 3"),
                createPlayer(Color.YELLOW, 4, "Player 4")
        );
        board = new Board(players);
    }

    private Player createPlayer(Color color, int playerId, String name) {
        return new Player(playerId, name, color, 0);
    }

    @Nested
    @DisplayName("Board Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize board with correct number of tiles")
        void shouldInitializeBoardWithCorrectTileCount() {
            // 40 normale Felder + 4*4 Zielfelder = 56 Felder
            assertEquals(56, board.getTiles().size());
        }

        @Test
        @DisplayName("Should initialize correct number of figures")
        void shouldInitializeCorrectNumberOfFigures() {
            // 4 Spieler * 4 Figuren = 16 Figuren
            assertEquals(16, board.getFigures().size());
        }

        @Test
        @DisplayName("Should initialize figures at home position")
        void shouldInitializeFiguresAtHomePosition() {
            List<Figure> figures = board.getFigures();
            // Alle Figuren sollten zu Beginn bei -1 (Zuhause) stehen
            assertTrue(figures.stream().allMatch(figure -> figure.getLocationTileId() == -1));
        }

        @Test
        @DisplayName("Should create start tiles for each team")
        void shouldCreateStartTilesForEachTeam() {
            for (Color color : Arrays.asList(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW)) {
                Tile startTile = board.getTeamStartingTile(color);
                assertNotNull(startTile);
                assertEquals(TileType.START_TILE, startTile.getTileType());
                assertEquals(color, startTile.getTeamTileColor());
            }
        }

        @Test
        @DisplayName("Should create entry tiles for each team")
        void shouldCreateEntryTilesForEachTeam() {
            int[] expectedEntryTiles = {9, 19, 29, 39};
            Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};

            for (int i = 0; i < expectedEntryTiles.length; i++) {
                Optional<Tile> entryTile = board.getTileById(expectedEntryTiles[i]);
                assertTrue(entryTile.isPresent());
                assertEquals(TileType.ENTRY_TILE, entryTile.get().getTileType());
                assertEquals(colors[i], entryTile.get().getTeamTileColor());
            }
        }

        @Test
        @DisplayName("Should create finish tiles for each team")
        void shouldCreateFinishTilesForEachTeam() {
            for (Color color : Arrays.asList(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW)) {
                List<Tile> finishTiles = board.getFinishTiles(color);
                assertEquals(4, finishTiles.size());
                assertTrue(finishTiles.stream().allMatch(tile ->
                        tile.getTileType() == TileType.FINISH_TILE &&
                                tile.getTeamTileColor() == color
                ));
            }
        }
    }

    @Nested
    @DisplayName("Figure Movement Tests")
    class MovementTests {

        @Test
        @DisplayName("Should move figure forward successfully")
        void shouldMoveFigureForwardSuccessfully() throws InvalidMoveException, BoardPositionOccupiedException {
            Figure figure = new Figure(0, Color.BLUE);
            board.getFigures().add(figure);

            board.moveFigure(figure, 3);

            assertEquals(3, figure.getLocationTileId());
        }

        @Test
        @DisplayName("Should throw exception when target tile is occupied by own team")
        void shouldThrowExceptionWhenTargetTileOccupiedByOwnTeam() {
            Figure figure1 = new Figure(0, Color.BLUE);
            Figure figure2 = new Figure(3, Color.BLUE);
            board.getFigures().addAll(Arrays.asList(figure1, figure2));

            assertThrows(BoardPositionOccupiedException.class, () -> {
                board.moveFigure(figure1, 3);
            });
        }

        @Test
        @DisplayName("Should capture opponent figure")
        void shouldCaptureOpponentFigure() throws InvalidMoveException, BoardPositionOccupiedException {
            Figure blueFigure = new Figure(0, Color.BLUE);
            Figure redFigure = new Figure(3, Color.RED);
            board.getFigures().addAll(Arrays.asList(blueFigure, redFigure));

            board.moveFigure(blueFigure, 3);

            assertEquals(3, blueFigure.getLocationTileId());
            assertEquals(-1, redFigure.getLocationTileId()); // Zurück nach Hause
        }

        @Test
        @DisplayName("Should move figure into finish tiles")
        void shouldMoveFigureIntoFinishTiles() throws InvalidMoveException, BoardPositionOccupiedException {
            Figure figure = new Figure(7, Color.BLUE); // Kurz vor Entry-Tile (9)
            board.getFigures().add(figure);

            board.moveFigure(figure, 4); // Sollte ins erste Finish-Tile

            List<Tile> finishTiles = board.getFinishTiles(Color.BLUE);
            assertEquals(finishTiles.get(1).getTileId(), figure.getLocationTileId());
        }

        @Test
        @DisplayName("Should throw exception when exceeding finish tiles")
        void shouldThrowExceptionWhenExceedingFinishTiles() {
            Figure figure = new Figure(6, Color.BLUE); // Entry-Tile ist bei 9
            board.getFigures().add(figure);

            assertThrows(InvalidMoveException.class, () -> {
                board.moveFigure(figure, 8); // Würde über die 4 Finish-Tiles hinausgehen
            });
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityTests {

        @Test
        @DisplayName("Should get figures by color")
        void shouldGetFiguresByColor() {
            List<Figure> blueFigures = board.getFiguresByColor(Color.BLUE);
            assertEquals(4, blueFigures.size());
            assertTrue(blueFigures.stream().allMatch(figure -> figure.getColor() == Color.BLUE));
        }

        @Test
        @DisplayName("Should get tile by ID")
        void shouldGetTileById() {
            Optional<Tile> tile = board.getTileById(0);
            assertTrue(tile.isPresent());
            assertEquals(0, tile.get().getTileId());
        }

        @Test
        @DisplayName("Should return empty optional for non-existent tile ID")
        void shouldReturnEmptyOptionalForNonExistentTileId() {
            Optional<Tile> tile = board.getTileById(999);
            assertFalse(tile.isPresent());
        }

        @Test
        @DisplayName("Should get team starting tile")
        void shouldGetTeamStartingTile() {
            Tile blueStartTile = board.getTeamStartingTile(Color.BLUE);
            assertNotNull(blueStartTile);
            assertEquals(0, blueStartTile.getTileId());
            assertEquals(TileType.START_TILE, blueStartTile.getTileType());
            assertEquals(Color.BLUE, blueStartTile.getTeamTileColor());
        }

        @Test
        @DisplayName("Should get finish tiles sorted by ID")
        void shouldGetFinishTilesSortedById() {
            List<Tile> finishTiles = board.getFinishTiles(Color.BLUE);
            assertEquals(4, finishTiles.size());

            // Prüfen ob sortiert
            for (int i = 0; i < finishTiles.size() - 1; i++) {
                assertTrue(finishTiles.get(i).getTileId() < finishTiles.get(i + 1).getTileId());
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle movement at board boundary")
        void shouldHandleMovementAtBoardBoundary() throws InvalidMoveException, BoardPositionOccupiedException {
            Figure figure = new Figure(38, Color.BLUE);
            board.getFigures().add(figure);

            board.moveFigure(figure, 1);

            assertEquals(39, figure.getLocationTileId());
        }

        @Test
        @DisplayName("Should handle movement from home")
        void shouldHandleMovementFromHome() throws InvalidMoveException, BoardPositionOccupiedException {
            Figure figure = new Figure(-1, Color.BLUE);
            board.getFigures().add(figure);

            // Bewegung von -1 auf Startfeld sollte möglich sein
            board.moveFigure(figure, 1);

            assertEquals(0, figure.getLocationTileId()); // -1 + 1 = 0
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete a full game scenario")
        void shouldCompleteFullGameScenario() throws InvalidMoveException, BoardPositionOccupiedException {
            // Komplexeres Szenario: Figur bewegt sich, schlägt Gegner, erreicht Zielfeld
            Figure blueFigure = new Figure(6, Color.BLUE);
            Figure redFigure = new Figure(8, Color.RED);
            board.getFigures().addAll(Arrays.asList(blueFigure, redFigure));

            // Erste Bewegung: Schlägt rote Figur
            board.moveFigure(blueFigure, 2);
            assertEquals(8, blueFigure.getLocationTileId());
            assertEquals(-1, redFigure.getLocationTileId());

            // Zweite Bewegung: Erreicht Entry-Tile und geht in Finish-Bereich
            board.moveFigure(blueFigure, 3); // Von 8 zu Entry-Tile 9 + 2 ins Finish
            List<Tile> finishTiles = board.getFinishTiles(Color.BLUE);
            assertEquals(finishTiles.get(1).getTileId(), blueFigure.getLocationTileId());
        }
    }
}