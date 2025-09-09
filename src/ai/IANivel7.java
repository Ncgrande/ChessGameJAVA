package ai;

import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;

public class IANivel7 extends IANivel6 {

    @Override
    public Move makeMove(Game game) {
        Move bestMoveOverall = null;

        // Itera sobre as profundidades, de 1 até o limite
        for (int profundidade = 1; profundidade <= PROFUNDIDADE_MAXIMA; profundidade++) {
            List<Move> allLegalMoves = this.getOrderedMoves(game);

            if (allLegalMoves.isEmpty()) {
                return null;
            }

            Move bestMoveAtCurrentDepth = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            
            for (Move move : allLegalMoves) {
                Game gameCopy = game.snapshotShallow();
                gameCopy.move(move.from(), move.to(), null);
                
                // Chama a função Negamax
                double score = -negamax(gameCopy, profundidade - 1,
                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

                if (score > bestScore) {
                    bestScore = score;
                    bestMoveAtCurrentDepth = move;
                }
            }
            
            if (bestMoveAtCurrentDepth != null) {
                bestMoveOverall = bestMoveAtCurrentDepth;
            }
        }
        return bestMoveOverall;
    }

    // O método negamax com poda Alpha-Beta
    protected double negamax(Game game, int depth, double alpha, double beta) {
        // Condição de parada (profundidade zero ou fim de jogo)
        if (depth == 0 || game.isGameOver()) {
            return evaluateBoard(game);
        }
        
        List<Move> allLegalMoves = new ArrayList<>();
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
        
        if (allLegalMoves.isEmpty()) {
            if (game.inCheck(game.whiteToMove())) {
                return Double.NEGATIVE_INFINITY;
            } else {
                return 0; // Empate
            }
        }

        double maxEval = Double.NEGATIVE_INFINITY;
        for (Move move : allLegalMoves) {
            Game gameCopy = game.snapshotShallow();
            gameCopy.move(move.from(), move.to(), null);

            // A chamada recursiva inverte o sinal da pontuação
            double eval = -negamax(gameCopy, depth - 1, -beta, -alpha);
            
            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval);
            if (alpha >= beta) {
                break;
            }
        }
        return maxEval;
    }
}