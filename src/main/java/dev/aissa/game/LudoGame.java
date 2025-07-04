package dev.aissa.game;

import dev.aissa.board.Board;
import dev.aissa.board.Dice;
import dev.aissa.board.Pawn;
import dev.aissa.enums.Gamestate;
import dev.aissa.exceptions.PlayerNotFoundException;
import dev.aissa.player.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Data
public class LudoGame {
    private List<Player> players;
    private HashMap<Integer, Integer> rolledAmount;

    private Board board;
    private Dice dice;
    private int currentPlayerIndex;
    private Gamestate gameState;
    private boolean gameOver = false;

    public LudoGame() {
        this.board = new Board();
        this.rolledAmount = new HashMap<>();
        this.players = IntStream.range(1, 5)
                .mapToObj(i -> new Player(i, "Player " + i))
                .toList();

        this.currentPlayerIndex = 0;
        this.dice = new Dice();

        gameState = Gamestate.DETERMINE_TURN_ORDER;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int rollDice() {
        return dice.roll();
    }

    public String processTurn(int rolledAmount) {
        if (gameState == Gamestate.DETERMINE_TURN_ORDER) {
            this.rolledAmount.put(getCurrentPlayer().getPlayerId(), rolledAmount);
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

            if (this.rolledAmount.size() == this.players.size()) {
                gameState = Gamestate.INGAME;
                applyOrder();
                return "Spieler " + getPlayerByRollPosition(currentPlayerIndex).getPlayerId() + " ist dran.";
            }
            return "Spieler " + players.get(currentPlayerIndex).getPlayerId() + " ist dran (Um reihenfolge Würfeln).";
        }

        Player currentPlayer = getPlayerByRollPosition(currentPlayerIndex);
        if (rolledAmount != 6) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        // Hat der Spieler noch Figuren im Home?
        for (Pawn pawn : currentPlayer.getPawns()) {
            if (pawn.getPosition() < 0) {
                board.move(pawn, rolledAmount);
                return "Spieler " + getPlayerByRollPosition(currentPlayerIndex).getPlayerId() + " ist dran.";
            }
        }

        for (Pawn pawn : currentPlayer.getPawns()) {
            if (pawn.getPosition() >= 0 && canMoveAny(currentPlayer, rolledAmount)) {
                board.move(pawn, rolledAmount);
                break;
            }
        }

        return "Spieler " + getPlayerByRollPosition(currentPlayerIndex).getPlayerId() + " ist dran.";
    }

    private boolean canMoveAny(Player player, int rolledAmount) {
       for(Pawn pawn : player.getPawns()) {
           if (board.canMove(pawn, rolledAmount)) {
               return true;
           }
       }

       return false;
    }

    public boolean checkWin(Player p) {
        return p.getPawns().stream()
                .allMatch(board::isFinished);
    }

    private void checkKickout(Pawn pawn) {
        List<Pawn> pawns = new ArrayList<>();

        for (Player player : players) {
            if (player.getPlayerId() != pawn.getPlayerId()) {
                pawns.addAll(player.getPawns());
            }
        }

        pawns.stream()
                .filter(currentPawn -> currentPawn.getPosition() == pawn.getPosition())
                .findFirst()
                .ifPresent(value -> value.setPosition(-1));
    }

    private void applyOrder() {
        List<Integer> sortedPlayerIds = this.rolledAmount.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        for (int i = 0; i < this.players.size(); i++) {
            getPlayerById(sortedPlayerIds.get(i)).setRollOrder(i);
        }
    }

    /**
     * Ermittelt den Spieler anhand seiner Id
     * @param playerId Id des gesuchten Spielers
     * @return den gefundenen Spieler passend zur Id
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
     * @param rollPosition Index der Würfelposition
     * @return den gefundenen Spieler passend zur Id
     * @throws PlayerNotFoundException konnte kein Spieler mit der Würfelposition gefunden werden
     */
    private Player getPlayerByRollPosition(int rollPosition) throws PlayerNotFoundException {
        return this.players.stream()
                .filter(player -> player.getRollOrder() == rollPosition)
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player with RollPosition:" + rollPosition + " not found"));
    }
}
