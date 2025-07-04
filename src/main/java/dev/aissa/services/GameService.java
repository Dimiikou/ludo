package dev.aissa.services;

import dev.aissa.board.Board;
import dev.aissa.board.Figure;
import dev.aissa.board.Tile;
import dev.aissa.enums.GameState;
import dev.aissa.enums.TileType;
import dev.aissa.exceptions.BoardPositionOccupiedException;
import dev.aissa.exceptions.InvalidMoveException;
import dev.aissa.exceptions.PlayerNotFoundException;
import dev.aissa.player.Player;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class GameService {
    private HashMap<Integer, Integer> playerThrows = new HashMap<>();
    private List<Player> players;
    private Board board;
    private GameState gameState;

    private int currentPlayerIndex;

    public GameService() {
        this.players = new ArrayList<>();

        // Wir erstellen 4 neue Spieler
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
        for (int i = 1; i < 5; i++) {
            this.players.add(new Player(i, "Spieler " + i, colors[i - 1], i - 1));
        }

        this.board = new Board(this.players);
        this.currentPlayerIndex = 0;
        this.gameState = GameState.DETERMINE_TURN_ORDER;
    }

    /**
     * Ermittelt, ob und wenn wer das Spiel gewonnen hat.
     *
     * @return Objekt des Spielers der gewonnen hat, wenn keiner, dann null
     */
    public Player getWinner() {
        for (Player player : this.players) {
            Color teamColor = player.getTeamColor();
            List<Integer> finishTileIds = this.board.getFinishTiles(teamColor).stream()
                    .map(Tile::getTileId)
                    .toList();

            boolean allInFinish = this.board.getFiguresByColor(teamColor).stream()
                    .allMatch(fig -> finishTileIds.contains(fig.getLocationTileId()));

            if (allInFinish) {
                return player;
            }
        }

        return null;
    }

    /**
     * Wird ausgeführt, wenn ein Spieler an der Reihe mit Würfeln ist
     */
    public int performPlayersTurn() {
        Player player = getPlayerByRollPosition(currentPlayerIndex);
        int rolledAmount = player.rollSingleDice();

        if (gameState == GameState.DETERMINE_TURN_ORDER) {
            handleDeterminePlayerSequenceThrow(rolledAmount);
            return rolledAmount;
        }

        // Bei einer 6 darf man nochmal würfeln.
        if (rolledAmount != 6) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            return rolledAmount;
        }

        // Wir prüfen ob der Spieler bereits eine Figur im Spielfeld hat.
        // wenn nein, setzen wir bei einer 6 eine Figur
        // auf das Startfeld und er darf dann Würfeln um zu ziehen.
        if (!playerHasFigureInField(player.getTeamColor())) {
            Figure figure = this.board.getFiguresByColor(player.getTeamColor()).stream()
                    .filter(currentFigure -> currentFigure.getLocationTileId() == -1)
                    .findFirst()
                    .orElse(null);

            // Figure kann nicht null sein da wir zuvor geprüft haben das wir Figuren noch nicht im Spiel haben müssen
            // Check ist nur für nervige Compilerwarnungen da
            if (figure == null) {
                return rolledAmount;
            }

            figure.setLocationTileId(this.board.getTeamStartingTile(player.getTeamColor()).getTileId());
            return rolledAmount;
        }

        // Hier ist das ganz normale Würfelszenario
        for(Figure figure : this.board.getFiguresByColor(player.getTeamColor())) {
            try {
                this.board.moveFigure(figure, rolledAmount);
            } catch (BoardPositionOccupiedException | InvalidMoveException _) { }
        }

        return rolledAmount;
    }

    /**
     * Ermittelt ob der Spieler eine Figur auf dem Spielfeld hat. Dazu zählen nicht die im "Zuhause" und auch nicht die im Ziel
     *
     * @param color Farbe des Teams welchem die Figur gehören soll
     *
     * @return true / false
     */
    private boolean playerHasFigureInField(Color color) {
        List<Figure> figuresInGame = this.board.getFiguresByColor(color).stream()
                .filter(figure -> {
                    Optional<Tile> tile = this.board.getTileById(figure.getLocationTileId());
                    return tile.isPresent() && tile.get().getTileType() != TileType.FINISH_TILE;
                })
                .toList();

        return !figuresInGame.isEmpty();
    }

    /**
     * Wird zu Beginn des Spiels ausgeführt um zu ermitteln in welcher Reihenfolge gewürfelt wird. Jeder Spieler würfelt einmal - die
     * Reihenfolge ermittelt sich dann durch absteigende Anzahl der geworfenen Augen
     */
    private void handleDeterminePlayerSequenceThrow(int rolledAmount) {
        playerThrows.put(currentPlayerIndex + 1, rolledAmount);

        if (playerThrows.size() < 4) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            return;
        }

        List<Integer> sortedPlayerIds = playerThrows.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        for (int i = 0; i < this.players.size(); i++) {
            getPlayerById(sortedPlayerIds.get(i)).setRollPosition(i);
        }

        gameState = GameState.INGAME;
        currentPlayerIndex = 0;
    }

    /**
     * Ermittelt den Spieler anhand seiner Id
     *
     * @param playerId Id des gesuchten Spielers
     *
     * @return den gefundenen Spieler passend zur Id
     *
     * @throws PlayerNotFoundException Es konnte kein Spieler mit der Id gefunden werden
     */
    private Player getPlayerById(int playerId) throws PlayerNotFoundException {
        return this.players.stream()
                .filter(player -> player.getPlayerId() == playerId)
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player with ID:" + playerId + " not found"));
    }

    /**
     * Ermittelt den Spieler anhand seiner Würfelposition
     *
     * @param rollPosition Index der Würfelposition
     *
     * @return den gefundenen Spieler passend zur Id
     *
     * @throws PlayerNotFoundException konnte kein Spieler mit der Würfelposition gefunden werden
     */
    public Player getPlayerByRollPosition(int rollPosition) throws PlayerNotFoundException {
        return this.players.stream()
                .filter(player -> player.getRollPosition() == rollPosition)
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player with RollPosition:" + rollPosition + " not found"));
    }
}
