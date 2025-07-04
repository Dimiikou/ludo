package dev.aissa.board;

import lombok.Data;

@Data
public class Board {

    public static final int TOTAL_FIELDS = 40;
    public static final int FINAL_STRETCH = 4;

    public boolean isFinished(Pawn pawn) {
        int pos = pawn.getPosition();
        return pos == TOTAL_FIELDS + (pawn.getPlayerId() + 1) * FINAL_STRETCH - 1;
    }

    public void move(Pawn pawn, int steps) {
        if (!canMove(pawn, steps)) {
            return;
        }

        int pos = pawn.getPosition();

        // Aus dem Home heraus
        if (pos < 0) {
            pawn.setPosition(pawn.getStartPosition());
            return;
        }

        // Auf der Hauptstrecke
        if (pos < TOTAL_FIELDS) {
            int start = pawn.getStartPosition();
            int distToHome = (pos == start)
                    ? TOTAL_FIELDS
                    : (start - pos + TOTAL_FIELDS) % TOTAL_FIELDS;

            // Eintritt in Finalstrecke
            if (steps > distToHome) {
                int indexInFinalRow = steps - distToHome - 1;
                pawn.setPosition(TOTAL_FIELDS + pawn.getPlayerId() * FINAL_STRETCH + indexInFinalRow);
                return;
            }

            pawn.setPosition((pos + steps) % TOTAL_FIELDS);
            return;
        }


        pawn.setPosition(pos + steps);
        return;
    }

    public boolean canMove(Pawn pawn, int steps) {
        int pos = pawn.getPosition();

        // Aus dem Home nur mit 6
        if (pos < 0) {
            return steps == 6;
        }

        // Schon im Zielbereich?
        if (pos >= TOTAL_FIELDS + pawn.getPlayerId() * FINAL_STRETCH) {
            return false;
        }

        // Auf der Hauptstrecke
        if (pos < TOTAL_FIELDS) {
            int start = pawn.getStartPosition();
            // Distanz bis zum Eintritt in die Finalstrecke erst nach voller Umrundung
            int distToHome = (pos == start)
                    ? TOTAL_FIELDS
                    : (start - pos + TOTAL_FIELDS) % TOTAL_FIELDS;
            return steps <= distToHome + FINAL_STRETCH;
        }

        // In der Finalstrecke
        int indexInFinalRow = pos - (TOTAL_FIELDS + pawn.getPlayerId() * FINAL_STRETCH);
        return indexInFinalRow + steps <= FINAL_STRETCH;
    }
}
