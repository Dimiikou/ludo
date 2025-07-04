package dev.aissa.ui;

import dev.aissa.board.Pawn;
import dev.aissa.enums.Gamestate;
import dev.aissa.game.LudoGame;
import dev.aissa.player.Player;
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

public class LudoUI extends Application {
    private LudoGame game;
    private Canvas canvas;
    private Button rollBtn;
    private Label playerLabel;
    private Label rollLabel;

    @Override
    public void start(Stage primaryStage) {
        game = new LudoGame();
        canvas = new Canvas(600, 600);

        rollBtn = new Button("Würfeln");
        rollBtn.setOnAction(event -> handleRoll()); //TODO

        playerLabel = new Label("Spieler 1 ist dran (Um reihenfolge Würfeln).");
        rollLabel = new Label();

        HBox controls = new HBox(10, rollBtn);
        controls.setAlignment(Pos.CENTER);

        VBox bottomBox = new VBox(5, playerLabel, rollLabel, controls);
        bottomBox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(bottomBox);

        drawBoard();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Mensch ärgere Dich nicht");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleRoll() {
        if (game.getGameState() == Gamestate.GAME_OVER) {
            return;
        }

        int roll = game.rollDice();
        rollLabel.setText("Spieler " + (game.getCurrentPlayer().getPlayerId()) + " hat Gewürfelt: " + roll);

        String labelText = game.processTurn(roll);
        playerLabel.setText(labelText);

        if (game.checkWin(game.getCurrentPlayer())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Spieler " + (game.getCurrentPlayer().getPlayerId()) + " hat gewonnen!");
            alert.show();
            game.setGameOver(true);
            rollBtn.setDisable(true);
            return;
        }

        drawBoard();
    }

    private void drawBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        int grid = 15;
        double cell = Math.min(w, h) / grid;

        // Hintergrund
        gc.setFill(Color.BEIGE);
        gc.fillRect(0, 0, w, h);

        // Felder in einem klassischen 15×15-Layout
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};

        // Pfad-Koordinaten
        java.util.List<int[]> path = new java.util.ArrayList<>();
        for (int x = 0; x <= 4; x++) path.add(new int[]{6, x + 2});
        for (int y = 5; y >= 2; y--) path.add(new int[]{y, 6});
        path.add(new int[]{2, 7});
        for (int y = 2; y <= 5; y++) path.add(new int[]{y, 8});
        for (int x = 6; x <= 10; x++) path.add(new int[]{6, x + 2});
        path.add(new int[]{7, 12});
        for (int x = 10; x >= 6; x--) path.add(new int[]{8, x + 2});
        for (int y = 9; y <= 12; y++) path.add(new int[]{y, 8});
        path.add(new int[]{12, 7});
        for (int y = 12; y >= 9; y--) path.add(new int[]{y, 6});
        for (int x = 4; x >= 0; x--) path.add(new int[]{8, x + 2});
        path.add(new int[]{7, 2});

        // Zeichnen Hauptstrecke
        for (int i = 0; i < path.size(); i++) {
            int r = path.get(i)[0], c = path.get(i)[1];
            double cx = c * cell + cell / 2, cy = r * cell + cell / 2;
            gc.setFill(Color.LIGHTGRAY);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }

        // Startfelder in path
        // Gelb
        {
            double cx = 6 * cell + cell / 2, cy = 12 * cell + cell / 2;
            gc.setFill(colors[3]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }
        // Grün
        {
            double cx = 12 * cell + cell / 2, cy = 8 * cell + cell / 2;
            gc.setFill(colors[2]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }
        // Blau
        {
            double cx = 8 * cell + cell / 2, cy = 2 * cell + cell / 2;
            gc.setFill(colors[1]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }
        // Rot
        {
            double cx = 2 * cell + cell / 2, cy = 6 * cell + cell / 2;
            gc.setFill(colors[0]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }

        // Home-Bereiche
        // Rot (links unten)
        for (int r = 0; r <= 1; r++)
            for (int c = 0; c <= 1; c++) {
                double cx = c * cell + cell / 2, cy = r * cell + cell / 2;
                gc.setFill(colors[0]);
                gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            }
        // Grün (rechts unten)
        for (int r = 13; r <= 14; r++)
            for (int c = 13; c <= 14; c++) {
                double cx = c * cell + cell / 2, cy = r * cell + cell / 2;
                gc.setFill(colors[2]);
                gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            }
        // Blau (rechts oben)
        for (int r = 0; r <= 1; r++)
            for (int c = 13; c <= 14; c++) {
                double cx = c * cell + cell / 2, cy = r * cell + cell / 2;
                gc.setFill(colors[1]);
                gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            }
        // Gelb (links oben)
        for (int r = 13; r <= 14; r++)
            for (int c = 0; c <= 1; c++) {
                double cx = c * cell + cell / 2, cy = r * cell + cell / 2;
                gc.setFill(colors[3]);
                gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            }

        // Finalstrecken
        // Gelb endet unten
        for (int i = 1; i <= 4; i++) {
            double cx = 7 * cell + cell / 2, cy = (7 + i) * cell + cell / 2;
            gc.setFill(colors[3]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }
        // Grün endet rechts
        for (int i = 1; i <= 4; i++) {
            double cx = (7 + i) * cell + cell / 2, cy = 7 * cell + cell / 2;
            gc.setFill(colors[2]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }
        // Blau endet oben
        for (int i = 1; i <= 4; i++) {
            double cx = 7 * cell + cell / 2, cy = (7 - i) * cell + cell / 2;
            gc.setFill(colors[1]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }
        // Rot endet links
        for (int i = 1; i <= 4; i++) {
            double cx = (7 - i) * cell + cell / 2, cy = 7 * cell + cell / 2;
            gc.setFill(colors[0]);
            gc.fillOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(cx - cell * 0.4, cy - cell * 0.4, cell * 0.8, cell * 0.8);
        }

        // Figuren zeichnen
        for (Player player : game.getPlayers()) {
            for (Pawn pawn : player.getPawns()) {
                int pos = pawn.getPosition();
                double cx = 0, cy = 0;
                if (pos < 0) {
                    int idx = player.getPawns().indexOf(pawn);
                    // Home entsprechend oben
                    switch (player.getPlayerId() - 1) {
                        case 0 -> {
                            int r = 0 + idx / 2, c = 0 + idx % 2;
                            cx = c * cell + cell / 2;
                            cy = r * cell + cell / 2;
                        }
                        case 2 -> {
                            int r = 13 + idx / 2, c = 13 + idx % 2;
                            cx = c * cell + cell / 2;
                            cy = r * cell + cell / 2;
                        }
                        case 1 -> {
                            int r = 0 + idx / 2, c = 13 + idx % 2;
                            cx = c * cell + cell / 2;
                            cy = r * cell + cell / 2;
                        }
                        case 3 -> {
                            int r = 13 + idx / 2, c = 0 + idx % 2;
                            cx = c * cell + cell / 2;
                            cy = r * cell + cell / 2;
                        }
                    }
                } else if (pos < path.size()) {
                    int[] rc = path.get(pos);
                    cx = rc[1] * cell + cell / 2;
                    cy = rc[0] * cell + cell / 2;
                } else {
                    // f = 0..3, also +1 ergibt 1..4
                    int f = pos - path.size();
                    int step = f + 1;

                    switch (player.getPlayerId()) {
                        case 0 -> {
                            // Rot endet links
                            cx = (7 - step) * cell + cell/2;
                            cy =  7        * cell + cell/2;
                        }
                        case 1 -> {
                            // Blau endet oben
                            cx =  7        * cell + cell/2;
                            cy = (7 - step) * cell + cell/2;
                        }
                        case 2 -> {
                            // Grün endet rechts
                            cx = (7 + step) * cell + cell/2;
                            cy =  7        * cell + cell/2;
                        }
                        case 3 -> {
                            // Gelb endet unten
                            cx =  7        * cell + cell/2;
                            cy = (7 + step) * cell + cell/2;
                        }
                    }
                }


                gc.setFill(colors[player.getPlayerId() - 1]);
                gc.fillOval(cx - cell * 0.3, cy - cell * 0.3, cell * 0.6, cell * 0.6);
                gc.setStroke(Color.BLACK);
                gc.strokeOval(cx - cell * 0.3, cy - cell * 0.3, cell * 0.6, cell * 0.6);
            }
        }
    }
}
