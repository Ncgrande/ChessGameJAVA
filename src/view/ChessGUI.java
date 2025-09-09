// ========================= src/view/ChessGUI.java =========================
package view;

import ai.IA; // Importe a interface IA
import ai.IANivel1; // Importe a IA Nível 1
import ai.IANivel10;
import ai.IANivel2; // Importe a IA Nível 2
import ai.IANivel3;
import ai.IANivel4;
import ai.IANivel5;
import ai.IANivel6;
import ai.IANivel7;
import ai.IANivel8;
import ai.IANivel9;
import controller.Game;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import model.board.Move; // Importe a nova classe Move
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L; // evita warning de serialização

    // --- Config de cores/styles ---
    private static final Color LIGHT_SQ = new Color(173, 216, 230);
    private static final Color DARK_SQ  = new Color(70, 130, 180);
    private static final Color HILITE_SELECTED = new Color(50, 120, 220);
    private static final Color HILITE_LEGAL    = new Color(20, 140, 60);
    private static final Color HILITE_LASTMOVE = new Color(220, 170, 30);

    private static final Border BORDER_SELECTED = new MatteBorder(3,3,3,3, HILITE_SELECTED);
    private static final Border BORDER_LEGAL    = new MatteBorder(3,3,3,3, HILITE_LEGAL);
    private static final Border BORDER_LASTMOVE = new MatteBorder(3,3,3,3, HILITE_LASTMOVE);

    private final Game game;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;

    // Menu / controles
    private JCheckBoxMenuItem pcAsBlack;
    private JSpinner depthSpinner;
    private JMenuItem newGameItem, quitItem;

    // Seleção atual e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Realce do último lance
    private Position lastFrom = null, lastTo = null;

    // IA
    private boolean aiThinking = false;
    // private final Random rnd = new Random(); // Não é mais necessário aqui, a IA cuidará disso

    public ChessGUI() {
        super("ChessGame");

        // Look&Feel Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {}

        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Menu
        setJMenuBar(buildMenuBar());

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(Color.DARK_GRAY);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        // Cria botões das casas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f)); // fallback com Unicode
                b.addActionListener(e -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Vez: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Histórico
        history = new JTextArea(14, 22);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyScroll = new JScrollPane(history);

        // Layout principal: tabuleiro à esquerda, histórico à direita
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        JLabel histLabel = new JLabel("Histórico de lances:");
        histLabel.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
        rightPanel.add(histLabel, BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);
        rightPanel.add(buildSideControls(), BorderLayout.SOUTH);

        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Atualiza ícones conforme a janela/painel muda de tamanho
        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh(); // recarrega ícones ajustando o tamanho
            }
        });

        setMinimumSize(new Dimension(920, 680));
        setLocationRelativeTo(null);

        // Atalhos: Ctrl+N, Ctrl+Q
        setupAccelerators();

        setVisible(true);
        refresh();
        maybeTriggerAI(); // caso o PC jogue primeiro
    }

    // ----------------- Menus e controles -----------------

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu gameMenu = new JMenu("Jogo");

        newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());

        pcAsBlack = new JCheckBoxMenuItem("PC joga com as Pretas");
        pcAsBlack.setSelected(false);

        JMenu depthMenu = new JMenu("Profundidade IA");
        // O valor máximo 4 para o spinner é da sua implementação anterior,
        // mas para os níveis iniciais 1 e 2, isso não fará diferença direta
        // na "profundidade" real de busca, apenas na escolha da IA.
        depthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1)); // Alterado para 9 níveis
        depthSpinner.setToolTipText("Nível da IA (1: Aleatório, 2: Avaliação Básica)");
        depthMenu.add(depthSpinner);

        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(pcAsBlack);
        gameMenu.add(depthMenu);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        mb.add(gameMenu);
        return mb;
    }

    private JPanel buildSideControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnNew = new JButton("Novo Jogo");
        btnNew.addActionListener(e -> doNewGame());
        panel.add(btnNew);

        JCheckBox cb = new JCheckBox("PC (Pretas)");
        cb.setSelected(pcAsBlack.isSelected());
        cb.addActionListener(e -> {
            pcAsBlack.setSelected(cb.isSelected());
            maybeTriggerAI(); // Aciona a IA se for a vez dela após mudar a configuração
        });
        panel.add(cb);

        panel.add(new JLabel("Nivel IA:"));
        int curDepth = ((Integer) depthSpinner.getValue()).intValue();
        JSpinner sp = new JSpinner(new SpinnerNumberModel(curDepth, 1, 10, 1)); // Alterado para 9 níveis
        sp.addChangeListener(e -> {
            depthSpinner.setValue(sp.getValue());
            // Reiniciar o jogo ou acionar a IA pode ser uma boa ideia aqui,
            // caso o nível mude no meio de uma partida.
        });
        panel.add(sp);

        return panel;
    }

    private void setupAccelerators() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "newGame");
        getRootPane().getActionMap().put("newGame", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { doNewGame(); }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "quit");
        getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(ChessGUI.this, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    private void doNewGame() {
        selected = null;
        legalForSelected.clear();
        lastFrom = lastTo = null;
        aiThinking = false;
        game.newGame();
        refresh();
        maybeTriggerAI();
    }

    // ----------------- Interação de tabuleiro -----------------

    private void handleClick(Position clicked) {
        if (game.isGameOver() || aiThinking) return;

        // Se for vez do PC (pretas) e modo PC ativado, ignore cliques
        if (pcAsBlack.isSelected() && !game.whiteToMove()) return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            // Nada selecionado ainda: só seleciona se for peça da vez
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            // Já havia uma seleção
            List<Position> legals = game.legalMovesFrom(selected); // recalc por segurança
            if (legals.contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                lastFrom = selected;
                lastTo   = clicked;

                game.move(selected, clicked, promo);

                selected = null;
                legalForSelected.clear();

                refresh();
                maybeAnnounceEnd();
                maybeTriggerAI();
                return;
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                // Troca a seleção para outra peça da vez
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            } else {
                // Clique inválido: limpa seleção
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    private Character askPromotion() {
        String[] opts = {"Rainha", "Torre", "Bispo", "Cavalo"};
        int ch = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para promoção:",
                "Promoção",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opts,
                opts[0]
        );
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    // ----------------- IA (não bloqueante) -----------------

    private void maybeTriggerAI() {
        if (game.isGameOver()) return;
        if (!pcAsBlack.isSelected()) return;
        if (game.whiteToMove()) return; // PC joga de pretas

        aiThinking = true;
        status.setText("Vez: Pretas — PC pensando...");

        // Seleciona a IA com base na profundidade/nível escolhido pelo usuário
        final int depth = (Integer) depthSpinner.getValue();
        final IA ai;

        // Adicione mais ifs/else-ifs aqui conforme for implementando mais níveis
        if (depth == 1) {
            ai = new IANivel1();
        } else if (depth >= 2) { // Para profundidade 2 ou mais, usa o Nível 2 por enquanto
            ai = new IANivel2();
        } else if (depth >= 3) { // Agora a IA de Nível 3 é usada para profundidade 3 ou mais
            ai = new IANivel3();
        } else if (depth >= 4) { // E a de Nível 4 para profundidade 4 ou mais
            ai = new IANivel4();
        }  else if (depth >= 5) { // Para profundidade 5 ou mais
            ai = new IANivel5();
        } else if (depth == 6) {
            ai = new IANivel6();
        } else if (depth >= 7) { // Usa a IA de Nível 7 para profundidade 7 ou mais
            ai = new IANivel7();
        } else if (depth >= 8) { // Usa a IA de Nível 8 para profundidade 8 ou mais
            ai = new IANivel8();
        }  else if (depth == 9) {
            ai = new IANivel9();
        } else if (depth >= 10) { // Nível 10 para IA com Rede Neural
            ai = new IANivel10();
        } else {
            ai = new IANivel1();
        }
        new SwingWorker<Void, Void>() {
            Move chosenMove;

            @Override
            protected Void doInBackground() {
                // A IA faz a jogada
                chosenMove = ai.makeMove(game);
                return null;
            }

            @Override
            protected void done() {
                try { get(); } catch (Exception ignored) {}

                if (chosenMove != null && !game.isGameOver() && !game.whiteToMove()) {
                    lastFrom = chosenMove.from();
                    lastTo   = chosenMove.to();
                    
                    // Lógica de promoção para a IA: assume Rainha
                    Character promo = null;
                    Piece moving = game.board().get(lastFrom);
                    if (moving instanceof Pawn && game.isPromotion(lastFrom, lastTo)) {
                        promo = 'Q';
                    }
                    game.move(lastFrom, lastTo, promo);
                }
                aiThinking = false;
                refresh();
                maybeAnnounceEnd();
                // Não chame maybeTriggerAI() novamente aqui, pois game.move() já alternou a vez
                // e o ciclo de jogo (clique humano -> maybeTriggerAI) cuidará do próximo turno.
            }
        }.execute();
    }

    // A classe Move interna de ChessGUI foi removida, use model.board.Move

    // Os métodos collectAllLegalMovesForSide, pieceValue, centerBonus
    // foram movidos ou a lógica equivalente está nas classes IANivel1 e IANivel2
    // Portanto, eles devem ser removidos desta classe ChessGUI.
    // Se eles são usados em algum outro lugar do ChessGUI, por favor me avise.
    // Pelo que vejo, eles só eram usados no maybeTriggerAI().
    /*
    private List<Move> collectAllLegalMovesForSide(boolean whiteSide) { ... }
    private int pieceValue(Piece p) { ... }
    private int centerBonus(Position pos) { ... }
    */

    // ----------------- Atualização de UI -----------------

    private void refresh() {
        // 1) Cores base e limpa bordas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? LIGHT_SQ : DARK_SQ;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
                b.setToolTipText(null);
            }
        }

        // 2) Realce último lance
        if (lastFrom != null) squares[lastFrom.getRow()][lastFrom.getColumn()].setBorder(BORDER_LASTMOVE);
        if (lastTo   != null) squares[lastTo.getRow()][lastTo.getColumn()].setBorder(BORDER_LASTMOVE);

        // 3) Realce seleção e movimentos legais
        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        // 4) Ícones das peças (ou Unicode como fallback)
        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];

                if (p == null) {
                    b.setIcon(null);
                    b.setText("");
                    continue;
                }

                char sym = p.getSymbol().charAt(0);
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon);
                    b.setText("");
                } else {
                    b.setIcon(null);
                    b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }

        // 5) Status e histórico
        String side = game.whiteToMove() ? "Brancas" : "Pretas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        if (aiThinking) chk = " — PC pensando...";
        status.setText("Vez: " + side + chk);

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0) sb.append((i / 2) + 1).append('.').append(' ');
            sb.append(hist.get(i)).append(' ');
            if (i % 2 == 1) sb.append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    private void maybeAnnounceEnd() {
        if (!game.isGameOver()) return;
        String msg;
        if (game.inCheck(game.whiteToMove())) {
            msg = "Xeque-mate! " + (game.whiteToMove() ? "Brancas" : "Pretas") + " estão em mate.";
        } else {
            msg = "Fim de Jogo! .";
        }
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1) return 64;
        return Math.max(24, side - 8);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}