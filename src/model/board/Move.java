// ========================= src/model/board/Move.java =========================
package model.board;

public record Move(Position from, Position to) {
    // A classe record é uma forma concisa de criar classes para dados imutáveis.
    // Ela gera automaticamente construtor, getters, equals(), hashCode() e toString().
    // Se preferir, pode usar uma classe normal com atributos finais e construtor.
}