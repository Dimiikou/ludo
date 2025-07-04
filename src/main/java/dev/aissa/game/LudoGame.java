package dev.aissa.game;

import dev.aissa.board.Board;
import dev.aissa.board.Figure;
import dev.aissa.board.Tile;
import dev.aissa.enums.GameState;
import dev.aissa.exceptions.BoardPositionOccupiedException;
import dev.aissa.exceptions.InvalidMoveException;
import dev.aissa.exceptions.PlayerNotFoundException;
import dev.aissa.player.Player;
import dev.aissa.enums.TileType;
import dev.aissa.services.GameService;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LudoGame extends Application {
    private Canvas canvas;
    private Button rollBtn;
    private Label playerLabel;
    private Label rollLabel;
    int grid = 15;
    double cellSize;

    private GameService gameService;

    /**
     * Startet die Spiellogik und damit einhergehend auch den Spielprozess
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.gameService = new GameService();
        canvas = new Canvas(600, 600);

        rollBtn = new Button("Würfeln");
        rollBtn.setOnAction(e -> handleRoll());

        playerLabel = new Label(getNextTurnText());
        rollLabel = new Label();

        // Control box
        HBox controls = new HBox(10, rollBtn);
        controls.setAlignment(Pos.CENTER);

        VBox bottomBox = new VBox(5, playerLabel, rollLabel, controls);
        bottomBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(bottomBox);

        drawBoard();

        Scene scene = new Scene(root);
        stage.setTitle("Mensch ärgere Dich nicht");
        stage.setScene(scene);
        stage.show();
    }

    private void handleRoll() {
        if (gameService.getGameState() == GameState.GAME_OVER) {
            return;
        }
        int currentPlayerId = gameService.getPlayerByRollPosition(gameService.getCurrentPlayerIndex()).getPlayerId();

        int roll = gameService.performPlayersTurn();
        rollLabel.setText("Spieler " + currentPlayerId + " hat Gewürfelt: " + roll);

        drawBoard();
        Player getWinner = gameService.getWinner();
        if (getWinner != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Spieler " + (getWinner.getPlayerId()) + " hat gewonnen!");
            alert.show();
            gameService.setGameState(GameState.GAME_OVER);
            rollBtn.setDisable(true);
            return;
        }

        playerLabel.setText(getNextTurnText());
    }

    private String getNextTurnText() {
        int nextPlayerId = gameService.getPlayerByRollPosition(gameService.getCurrentPlayerIndex()).getPlayerId();
        return gameService.getGameState() == GameState.DETERMINE_TURN_ORDER
                ? "Spieler " + nextPlayerId + " ist dran. (Reihenfolge Würfeln)"
                : "Spieler " + nextPlayerId + " ist dran";
    }

    private void drawBoard() {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        double width = canvas.getWidth(), height = canvas.getHeight();
        cellSize = Math.min(width, height) / grid;

        graphicsContext.setFill(Color.BEIGE);
        graphicsContext.fillRect(0, 0, width, height);

        graphicsContext.setStroke(Color.BLACK);
        for (int row = 0; row < grid; row++) {
            for (int col = 0; col < grid; col++) {
                double x = col * cellSize;
                double y = row * cellSize;
                graphicsContext.strokeRect(x, y, cellSize, cellSize);

                // Index-Zahl in die Mitte der Zelle setzen
                String text = String.valueOf(row * grid + col);
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.fillText(text, x + cellSize * 0.3, y + cellSize * 0.6);
            }
        }

    }

    private void fillCellById(int id, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int row = id / grid;
        int col = id % grid;

        double x = col * cellSize;
        double y = row * cellSize;

        gc.setFill(color);
        gc.fillRect(x, y, cellSize, cellSize);

        // Optional: Zelle neu umranden und Nummer neu schreiben
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, cellSize, cellSize);

        String text = String.valueOf(id);
        gc.setFill(Color.BLACK);
        gc.fillText(text, x + cellSize * 0.3, y + cellSize * 0.6);


    }
}