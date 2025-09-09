package ai;

import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;

public class IANivel3 extends IANivel2 {

    private static final int PROFUNDIDADE_MAXIMA = 3; // Nível de profundidade para a busca

    @Override
    public Move makeMove(Game game) {
        // A lógica principal do Minimax irá aqui
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
            return null;
        }

        Move bestMove = null;
    double bestScore = game.whiteToMove() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    
    for (Move move : allLegalMoves) {
        Game gameCopy = game.snapshotShallow();
        gameCopy.move(move.from(), move.to(), null);
        
        // Ajuste a chamada para passar alfa e beta.
        // O jogador atual é o que acabou de fazer o movimento na simulação, ou seja, o oponente.
        double score = minimax(gameCopy, PROFUNDIDADE_MAXIMA - 1, game.whiteToMove(),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        if (game.whiteToMove()) {
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        } else {
            if (score < bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
    }
    return bestMove;
    }

    private double minimax(Game game, int depth, boolean isMaximizingPlayer, double alpha, double beta) {
    // 1. Condição de parada (igual à implementação anterior)
    if (depth == 0 || game.isGameOver()) {
        return evaluateBoard(game);
    }
    
    // 2. Coletar todos os movimentos possíveis
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
        // Verifica se é xeque-mate ou empate
        if (game.inCheck(game.whiteToMove())) {
            return isMaximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        } else {
            return 0; // Empate
        }
    }

    if (isMaximizingPlayer) {
        double maxEval = Double.NEGATIVE_INFINITY;
        for (Move move : allLegalMoves) {
            Game gameCopy = game.snapshotShallow();
            gameCopy.move(move.from(), move.to(), null);
            double eval = minimax(gameCopy, depth - 1, false, alpha, beta);
            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval); // Atualiza alpha
            if (beta <= alpha) { // Poda Beta
                break;
            }
        }
        return maxEval;
    } else { // Jogador que tenta minimizar
        double minEval = Double.POSITIVE_INFINITY;
        for (Move move : allLegalMoves) {
            Game gameCopy = game.snapshotShallow();
            gameCopy.move(move.from(), move.to(), null);
            double eval = minimax(gameCopy, depth - 1, true, alpha, beta);
            minEval = Math.min(minEval, eval);
            beta = Math.min(beta, eval); // Atualiza beta
            if (beta <= alpha) { // Poda Alpha
                break;
            }
        }
        return minEval;
    }
    }
}