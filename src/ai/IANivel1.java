// ========================= src/ai/IANivel1.java =========================
package ai;

import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IANivel1 implements IA {

    private final Random random = new Random();

    @Override
    public Move makeMove(Game game) {
        List<Move> allLegalMoves = new ArrayList<>();
        // 1. Coleta todos os movimentos legais para o jogador atual
        // Note que o game.whiteToMove() já indica de quem é a vez
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = game.board().get(from);
                if (piece != null && piece.isWhite() == game.whiteToMove()) {
                    for (Position to : game.legalMovesFrom(from)) {
                        allLegalMoves.add(new Move(from, to));
                    }
                }
            }
        }
        // 2. Escolhe um movimento aleatório
        if (!allLegalMoves.isEmpty()) {
            return allLegalMoves.get(random.nextInt(allLegalMoves.size()));
        }
        return null; // Nenhum movimento possível
    }
}