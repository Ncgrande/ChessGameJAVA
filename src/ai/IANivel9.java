package ai;

import controller.Game;
import model.board.Move;
import java.util.HashMap;
import java.util.Map;
//import java.util.List;
import model.board.Position;

public class IANivel9 extends IANivel8 {

    // O livro de aberturas como um Map de histórico de movimentos para o próximo movimento
    private final Map<String, Move> openingBook = new HashMap<>();

    public IANivel9() {
        // Inicializa o livro de aberturas
        loadOpeningBook();
    }

    @Override
    public Move makeMove(Game game) {
        // 1. Verifica se a posição atual está no livro de aberturas
        String currentHistory = game.getHistoryAsString(); // Você precisa criar este método em Game.java
        
        if (openingBook.containsKey(currentHistory)) {
            System.out.println("Usando movimento do livro de aberturas: " + openingBook.get(currentHistory));
            return openingBook.get(currentHistory);
        }
        
        // 2. Se não estiver no livro, usa o algoritmo normal (herda do IANivel8)
        return super.makeMove(game);
    }
    
    private void loadOpeningBook() {
        // Adiciona algumas aberturas comuns para a IA jogar
        addOpening("e2-e4", "e7-e5"); // Abertura do Peão do Rei
        addOpening("d2-d4", "d7-d5"); // Abertura do Peão da Dama
        addOpening("c2-c4", "e7-e5"); // Abertura Inglesa
    }
    
    private void addOpening(String... moves) {
        StringBuilder history = new StringBuilder();
        for (int i = 0; i < moves.length - 1; i++) {
            history.append(moves[i]).append(",");
            String key = history.toString();
            String nextMoveString = moves[i+1];
            
            // Supondo que o formato da string de movimento é "a1-a2"
            // Você precisará de um método para converter a string para um objeto Move
            Move nextMove = convertStringToMove(nextMoveString);
            openingBook.put(key, nextMove);
        }
    }
    
    // Método auxiliar para converter a string de movimento (ex: "e2-e4") em um objeto Move
    private Move convertStringToMove(String moveStr) {
        // Lógica para converter "e2-e4" em um objeto Move
        // Exemplo: 'e' -> 4, '2' -> 6, '4' -> 4, 'e' -> 4
        // from: (6, 4), to: (4, 4)
        int fromCol = moveStr.charAt(0) - 'a';
        int fromRow = 8 - (moveStr.charAt(1) - '0');
        int toCol = moveStr.charAt(3) - 'a';
        int toRow = 8 - (moveStr.charAt(4) - '0');
        return new Move(new Position(fromRow, fromCol), new Position(toRow, toCol));
    }
}