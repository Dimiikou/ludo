package dev.aissa.game;

import dev.aissa.board.Board;
import dev.aissa.board.Figure;
import dev.aissa.board.Tile;
import dev.aissa.exceptions.BoardPositionOccupiedException;
import dev.aissa.exceptions.InvalidMoveException;
import dev.aissa.exceptions.PlayerNotFoundException;
import dev.aissa.player.Player;
import dev.aissa.enums.Color;
import dev.aissa.enums.TileType;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class LupoGame {

    private List<Player> players;
    private Board board;

    public void initializeGame() {
        this.players = new ArrayList<>();

        // Wir lesen aus wie viele Mitspieler es geben soll und mit welchen Namen
        this.players.add(new Player(this.players.size(), "Leon", Color.BLUE, 0));
        this.players.add(new Player(this.players.size(), "Luis", Color.YELLOW, 0));
        this.players.add(new Player(this.players.size(), "Azra", Color.RED, 0));
        this.players.add(new Player(this.players.size(), "Spiessi", Color.GREEN, 0));

        this.board = new Board(this.players);
    }

    public void startGame() {
        determinePlayerSequence();

        boolean gameOver = false;
        while (!gameOver) {
            for (int currentPlayer = 0; currentPlayer < this.players.size(); currentPlayer++) {
                performPlayersTurn(getPlayerByRollPosition(currentPlayer));
            }

            Player getWinner = getWinner();
            if (getWinner == null) {
                continue;
            }

            // TODO: Winning Logic
            System.out.println(getWinner.getName() + " wins!");
            gameOver = true;
        }

        System.out.println("HEHEHE");
    }

    private Player getWinner() {
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

    private void performPlayersTurn(Player player) {
        // Wir prüfen ob der Spieler bereits eine Figur im Spielfeld hat.
        // wenn nein darf er dreimal würfeln. Bei einem Pasch setzen wir eine Figur
        // auf das Startfeld und er darf dann Würfeln um zu ziehen.
        if (!playerHasFigureInField(player.getTeamColor())) {
            for (int rollNumber = 0; rollNumber < 3; rollNumber++) {
                if (player.rollDoubleDice()[0] != player.rollDoubleDice()[1]) {
                    continue;
                }

                Figure figure = this.board.getFiguresByColor(player.getTeamColor()).stream()
                        .filter(currentFigure -> currentFigure.getLocationTileId() == -1)
                        .findFirst()
                        .orElse(null);

                // Figure kann nicht null sein da wir zuvor geprüft haben das wir Figuren noch nicht im Spiel haben müssen
                // Check ist nur für nervige Compilerwarnungen da
                if (figure == null) {
                    break;
                }

                figure.setLocationTileId(this.board.getTeamStartingTile(player.getTeamColor()).getTileId());
                break;
            }
        }

        // Hier ist das ganz normale Würfelszenario
        int diceAmountOne = player.rollDoubleDice()[0];
        int diceAmountTwo = player.rollDoubleDice()[1];
        int totalDiceAmount = diceAmountOne + diceAmountTwo;

        // TODO: Figur im UI auswählbar machen
        Figure figure = this.board.getFiguresByColor(player.getTeamColor()).getFirst();
        try {
            this.board.moveFigure(figure, totalDiceAmount);
        } catch (BoardPositionOccupiedException | InvalidMoveException ex) {
            System.out.println(ex.getMessage());
        }

        // Bei Pasch darf man nochmal
        if (diceAmountOne == diceAmountTwo) {
            performPlayersTurn(player);
        }
    }

    private boolean playerHasFigureInField(Color color) {
        List<Figure> figuresInGame = this.board.getFiguresByColor(color).stream()
                .filter(figure -> {
                    Optional<Tile> tile = this.board.getTileById(figure.getLocationTileId());
                    return tile.isPresent() && tile.get().getTileType() != TileType.FINISH_TILE;
                })
                .toList();

        return !figuresInGame.isEmpty();
    }

    private void determinePlayerSequence() {
        HashMap<Integer, Integer> playerThrows = new HashMap<>();
        for (Player player : this.players) {
            playerThrows.put(player.getPlayerId(), player.rollSingleDice());
        }

        List<Integer> sortedPlayerIds = playerThrows.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        for (int i = 0; i < this.players.size(); i++) {
            getPlayerById(sortedPlayerIds.get(i)).setRollPosition(i);
        }
    }

    private Player getPlayerById(int playerId) throws PlayerNotFoundException {
        return this.players.stream()
                .filter(player -> player.getPlayerId() == playerId)
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player with ID:" + playerId + " not found"));
    }

    private Player getPlayerByRollPosition(int rollPosition) throws PlayerNotFoundException {
        return this.players.stream()
                .filter(player -> player.getRollPosition() == rollPosition)
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player with RollPosition:" + rollPosition + " not found"));
    }
}