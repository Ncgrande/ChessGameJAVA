// ========================= src/ai/IANivel2.java =========================
package ai;

import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.Bishop;
import model.pieces.King;
import model.pieces.Knight;
import model.pieces.Pawn;
import model.pieces.Queen;
import model.pieces.Rook;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IANivel2 implements IA {

    // Valores das peças conforme o PDF
    protected static final int VALOR_PEAO = 100;
    protected static final int VALOR_CAVALO = 320;
    protected static final int VALOR_BISPO = 330;
    protected static final int VALOR_TORRE = 500;
    protected static final int VALOR_DAMA = 900;
    protected static final int VALOR_REI = 20000;

    @Override
    public Move makeMove(Game game) {
        List<Move> allLegalMoves = new ArrayList<>();
        // 1. Coleta todos os movimentos legais para o jogador atual
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

        // 2. Avalia cada movimento e escolhe o melhor
        //Move bestMove = null;
        double bestScore = game.whiteToMove() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        
        // Use uma lista para lidar com movimentos de mesma pontuação
        List<Move> bestMoves = new ArrayList<>();

        for (Move move : allLegalMoves) {
            // Cria uma cópia do jogo para simular
            Game gameCopy = game.snapshotShallow();
            
            // Assume promoção para Rainha, caso ocorra, para simplificar neste nível básico
            Character promo = null;
            Piece movingPiece = game.board().get(move.from());
            if (movingPiece instanceof Pawn && game.isPromotion(move.from(), move.to())) {
                promo = 'Q';
            }
            gameCopy.move(move.from(), move.to(), promo);

            // Avalia a nova posição
            double score = evaluateBoard(gameCopy);

            if (game.whiteToMove()) { // Jogador atual é BRANCO, busca MAIOR pontuação
                if (score > bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            } else { // Jogador atual é PRETO, busca MENOR pontuação
                if (score < bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                } else if (score == bestScore) {
                    bestMoves.add(move);
                }
            }
        }
        
        // Escolhe um movimento aleatório da lista dos melhores para evitar repetição
        if (!bestMoves.isEmpty()) {
            Random random = new Random();
            return bestMoves.get(random.nextInt(bestMoves.size()));
        }

        return null;
    }

    protected double evaluateBoard(Game game) {
        double score = 0;
        
        // Itera sobre TODAS as 64 casas para avaliar a posição
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = game.board().get(pos);
                if (p != null) {
                    int pieceValue = getPieceValue(p);
                    int positionBonus = getPositionBonus(p, pos);

                    if (p.isWhite()) {
                        score += pieceValue + positionBonus;
                    } else {
                        score -= pieceValue + positionBonus;
                    }
                }
            }
        }
        
        // Bônus/Penalidades de estado do jogo
        if (game.inCheck(game.whiteToMove())) {
            score += game.whiteToMove() ? -100 : 100;
        }
        if (game.isCheckmate(game.whiteToMove())) {
             score += game.whiteToMove() ? -VALOR_REI : VALOR_REI;
        }
        
        return score;
    }

    protected int getPieceValue(Piece p) {
        if (p instanceof Pawn) return VALOR_PEAO;
        if (p instanceof Knight) return VALOR_CAVALO;
        if (p instanceof Bishop) return VALOR_BISPO;
        if (p instanceof Rook) return VALOR_TORRE;
        if (p instanceof Queen) return VALOR_DAMA;
        if (p instanceof King) return VALOR_REI;
        return 0;
    }

    private int getPositionBonus(Piece p, Position pos) {
        int bonus = 0;
        int r = pos.getRow();
        int c = pos.getColumn();
        
        // Bônus para controle do centro
        if ((r == 3 || r == 4) && (c == 3 || c == 4)) {
            bonus += 10;
        } else if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5)) {
            bonus += 4;
        }
        
        // Bônus para peões avançados
        if (p instanceof Pawn) {
            if (p.isWhite()) {
                bonus += (7 - r) * 5; // Mais pontos quanto mais perto do final
            } else {
                bonus += r * 5; // Mais pontos quanto mais perto do final
            }
        }
        
        return bonus;
    }
}