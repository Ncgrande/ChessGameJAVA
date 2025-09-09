package ai;

import controller.Game;
import model.board.Move;
import java.util.List;
//import java.util.ArrayList;

public class IANivel6 extends IANivel5 {

    // A profundidade máxima agora é um limite para o aprofundamento
    protected static final int PROFUNDIDADE_MAXIMA = 4;

    @Override
    public Move makeMove(Game game) {
        Move bestMoveOverall = null;

        // Itera sobre as profundidades, de 1 até o limite
        for (int profundidade = 1; profundidade <= PROFUNDIDADE_MAXIMA; profundidade++) {
            
            // Reutiliza a lógica de makeMove da classe-mãe para cada profundidade
            // O aprofundamento iterativo se beneficia do fato de que o makeMove da
            // IANivel5 já ordena os movimentos antes de chamar o minimax.
            
            List<Move> allLegalMoves = this.getOrderedMoves(game);

            if (allLegalMoves.isEmpty()) {
                return null;
            }

            Move bestMoveAtCurrentDepth = null;
            double bestScore = game.whiteToMove() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            
            for (Move move : allLegalMoves) {
                Game gameCopy = game.snapshotShallow();
                gameCopy.move(move.from(), move.to(), null);
                
                // Chamada do minimax com a profundidade atual
                double score = minimax(gameCopy, profundidade - 1, !game.whiteToMove(),
                                       Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

                if (game.whiteToMove()) {
                    if (score > bestScore) {
                        bestScore = score;
                        bestMoveAtCurrentDepth = move;
                    }
                } else {
                    if (score < bestScore) {
                        bestScore = score;
                        bestMoveAtCurrentDepth = move;
                    }
                }
            }
            
            // O melhor movimento da busca atual se torna o "melhor até agora"
            if (bestMoveAtCurrentDepth != null) {
                bestMoveOverall = bestMoveAtCurrentDepth;
            }
        }
        
        return bestMoveOverall;
    }

    // Os métodos minimax, evaluateBoard, getOrderedMoves, etc.
    // são herdados das classes-mãe.
}