package ai;

import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IANivel5 extends IANivel4 {

    // Herdamos a lógica minimax com poda alfa-beta da classe pai IANivel4.
    // O que muda é a ordenação dos movimentos antes da busca.

    private static final int PROFUNDIDADE_MAXIMA = 3; // Mantém a profundidade aqui para visibilidade

    @Override
    public Move makeMove(Game game) {
        // A lógica principal do makeMove continua a mesma da classe pai,
        // mas a lista de movimentos coletada já virá ordenada.
        
        List<Move> allLegalMoves = this.getOrderedMoves(game);

        if (allLegalMoves.isEmpty()) {
            return null;
        }

        Move bestMove = null;
        double bestScore = game.whiteToMove() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        
        for (Move move : allLegalMoves) {
            Game gameCopy = game.snapshotShallow();
            // Assumimos que a promoção é para Rainha para simplificar
            gameCopy.move(move.from(), move.to(), null);
            
            // Chama a função minimax, que se beneficiará da ordenação
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

    // Método para obter e ordenar os movimentos
    protected List<Move> getOrderedMoves(Game game) {
        List<Move> moves = new ArrayList<>();
        Map<Move, Integer> scores = new HashMap<>();
        
        // 1. Coleta todos os movimentos legais e atribui uma pontuação
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = game.board().get(from);
                if (piece != null && piece.isWhite() == game.whiteToMove()) {
                    for (Position to : game.legalMovesFrom(from)) {
                        Move move = new Move(from, to);
                        int score = 0;

                        Piece captured = game.board().get(to);
                        if (captured != null) {
                            // Heurística MVV-LVA: Most Valuable Victim - Least Valuable Aggressor
                            score += getPieceValue(captured) * 10 - getPieceValue(piece);
                        }
                        
                        if (game.isPromotion(from, to)) {
                            score += VALOR_DAMA; // Grande bônus por promoção
                        }
                        
                        // Bônus para o centro (pode ser ajustado)
                        int toRow = move.to().getRow();
                        int toCol = move.to().getColumn();
                        if ((toRow >= 3 && toRow <= 4) && (toCol >= 3 && toCol <= 4)) {
                            score += 10;
                        } else if ((toRow >= 2 && toRow <= 5) && (toCol >= 2 && toCol <= 5)) {
                            score += 4;
                        }

                        moves.add(move);
                        scores.put(move, score);
                    }
                }
            }
        }
        
        // 2. Ordena os movimentos pela pontuação (do maior para o menor)
        moves.sort(Comparator.comparingInt(scores::get).reversed());
        
        return moves;
    }
    
    // O método minimax com poda Alpha-Beta foi herdado de IANivel4
}