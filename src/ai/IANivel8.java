package ai;

import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

public class IANivel8 extends IANivel7 {

    // Herdamos a lógica de makeMove e getOrderedMoves da classe pai.
    // A única alteração será na condição de parada da busca negamax.

    @Override
    protected double negamax(Game game, int depth, double alpha, double beta) {
        // Condição de parada (profundidade zero ou fim de jogo)
        if (depth == 0 || game.isGameOver()) {
            return quiescence(game, alpha, beta); // Chama a busca de quiescência
        }
        
        List<Move> allLegalMoves = new ArrayList<>();
        // Lógica de coleta de movimentos (pode ser um método auxiliar herdado ou não)
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
                return 0;
            }
        }

        double maxEval = Double.NEGATIVE_INFINITY;
        for (Move move : allLegalMoves) {
            Game gameCopy = game.snapshotShallow();
            gameCopy.move(move.from(), move.to(), null); // Simplificado
            double eval = -negamax(gameCopy, depth - 1, -beta, -alpha);
            
            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval);
            if (alpha >= beta) {
                break;
            }
        }
        return maxEval;
    }
    
    // Novo método para a busca de quiescência
    private double quiescence(Game game, double alpha, double beta) {
        double standPat = evaluateBoard(game); // Avaliação estática da posição atual
        
        if (standPat >= beta) {
            return beta;
        }
        if (standPat > alpha) {
            alpha = standPat;
        }
        
        // Coleta apenas movimentos de captura
        List<Move> allCaptures = this.getCapturesOrdered(game);
        
        for (Move capture : allCaptures) {
            Game gameCopy = game.snapshotShallow();
            gameCopy.move(capture.from(), capture.to(), null); // Simplificado
            
            // Chamada recursiva para a busca de quiescência
            double score = -quiescence(gameCopy, -beta, -alpha);
            
            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;
    }

    // Método auxiliar para obter apenas capturas, ordenadas por MVV-LVA
    private List<Move> getCapturesOrdered(Game game) {
        List<Move> captures = new ArrayList<>();
        Map<Move, Integer> scores = new HashMap<>();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = game.board().get(from);
                if (piece != null && piece.isWhite() == game.whiteToMove()) {
                    for (Position to : game.legalMovesFrom(from)) {
                        Piece captured = game.board().get(to);
                        if (captured != null) {
                            Move move = new Move(from, to);
                            int score = getPieceValue(captured) * 10 - getPieceValue(piece);
                            captures.add(move);
                            scores.put(move, score);
                        }
                    }
                }
            }
        }
        captures.sort(Comparator.comparingInt(scores::get).reversed());
        return captures;
    }
}