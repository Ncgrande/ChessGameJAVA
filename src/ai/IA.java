// ========================= src/ai/IA.java =========================
package ai;

import controller.Game;
import model.board.Move;

public interface IA {
    Move makeMove(Game game);
}