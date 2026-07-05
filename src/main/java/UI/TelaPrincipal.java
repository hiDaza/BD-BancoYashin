/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.AgendamentoTransferencia;
import com.mycompany.yashin.model.BoletoEmitido;
import com.mycompany.yashin.model.CartaoCredito;
import com.mycompany.yashin.model.Cliente;
import com.mycompany.yashin.model.CompraFatura;
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.EmprestimoSolicitacao;
import com.mycompany.yashin.model.FaturaCartao;
import com.mycompany.yashin.model.PagamentoLote;
import com.mycompany.yashin.model.PreferenciaAlertas;
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.TipoPessoa;
import controller.BancoController;
import controller.Sessao;
import dao.CompraFaturaDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import util.CatalogoBancos;
import util.FormatadorUtil;
import javax.swing.SwingWorker;

public class TelaPrincipal extends JFrame {
    private BancoController controller = new BancoController();
    private Cliente clienteLogado;
    private List<Conta> contas;

    private JTabbedPane tabbedPane;

    // Componentes principais
    private JComboBox<Conta> cmbContaSaldo = new JComboBox<>(), cmbContaOrigem, cmbContaDestino, cmbContaPagamento;
    private JLabel lblSaldo = new JLabel();
    private JTextArea txtExtrato, txtResultadoEmprestimo;
    private JTable tableCartoes, tableFaturas, tableBoletos, tableLotes, tableAgendamentos;
    private DefaultTableModel modelCartoes, modelFaturas, modelBoletos, modelLotes, modelAgendamentos;
    private JComboBox<String> cmbAgenciaTED;
    
    private boolean saldoVisivel = true;

    public TelaPrincipal() {
        clienteLogado = Sessao.getInstance().getClienteLogado();
        contas = Sessao.getInstance().getContasCliente();
        initComponents();
        carregarDadosIniciais();
    }

    private void initComponents() {
        setTitle("Banco Yashin - Área do Cliente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Saldo/Extrato", criarPainelSaldoExtrato());
        tabbedPane.addTab("Transferências", criarPainelTransferencias());
        tabbedPane.addTab("Pagamentos", criarPainelPagamentos());
        tabbedPane.addTab("Empréstimos", criarPainelEmprestimos());
        tabbedPane.addTab("Cartões", criarPainelCartoes());
        tabbedPane.addTab("Agendamentos", criarPainelAgendamentos());

        if (clienteLogado.getTipoPessoa() == TipoPessoa.PJ) {
            tabbedPane.addTab("Boletos", criarPainelBoletos());
            tabbedPane.addTab("Pagamentos em Lote", criarPainelLote());
        }

        tabbedPane.addTab("Configurações", criarPainelConfiguracoes());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(new JLabel("Cliente: " + clienteLogado.getNomeRazao() + " | " +
                (clienteLogado.getTipoPessoa() == TipoPessoa.PF ? "PF" : "PJ")));
        JButton btnLogout = new JButton("Sair");
        btnLogout.addActionListener(e -> logout());
        top.add(btnLogout);
        add(top, BorderLayout.NORTH);

        setVisible(true);
    }

    private void carregarDadosIniciais() {
        atualizarCombosContas();
        if (!contas.isEmpty()) {
            cmbContaSaldo.setSelectedIndex(0);
            carregarSaldo();
        }
        carregarTabelaCartoes();
        carregarTabelaFaturas();
        carregarTabelaAgendamentos();
        if (clienteLogado.getTipoPessoa() == TipoPessoa.PJ) {
            carregarTabelaBoletos();
            carregarTabelaLotes();
        }
    }

    private void atualizarCombosContas() {
        DefaultComboBoxModel<Conta> modelSaldo = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Conta> modelOrigem = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Conta> modelDestino = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Conta> modelPagamento = new DefaultComboBoxModel<>();

        for (Conta c : contas) {
            modelSaldo.addElement(c);
            modelOrigem.addElement(c);
            modelDestino.addElement(c);
            modelPagamento.addElement(c);
        }

        if (cmbContaSaldo != null) cmbContaSaldo.setModel(modelSaldo);
        if (cmbContaOrigem != null) cmbContaOrigem.setModel(modelOrigem);
        if (cmbContaDestino != null) cmbContaDestino.setModel(modelDestino);
        if (cmbContaPagamento != null) cmbContaPagamento.setModel(modelPagamento);

        // Evitar que origem e destino sejam iguais
        if (cmbContaOrigem != null && cmbContaDestino != null) {
            cmbContaOrigem.addActionListener(e -> {
                Conta origem = (Conta) cmbContaOrigem.getSelectedItem();
                Conta destino = (Conta) cmbContaDestino.getSelectedItem();
                if (origem != null && destino != null && origem.equals(destino)) {
                    for (int i = 0; i < cmbContaDestino.getItemCount(); i++) {
                        Conta item = cmbContaDestino.getItemAt(i);
                        if (!item.equals(origem)) {
                            cmbContaDestino.setSelectedItem(item);
                            break;
                        }
                    }
                }
            });

            cmbContaDestino.addActionListener(e -> {
                Conta origem = (Conta) cmbContaOrigem.getSelectedItem();
                Conta destino = (Conta) cmbContaDestino.getSelectedItem();
                if (origem != null && destino != null && origem.equals(destino)) {
                    for (int i = 0; i < cmbContaOrigem.getItemCount(); i++) {
                        Conta item = cmbContaOrigem.getItemAt(i);
                        if (!item.equals(destino)) {
                            cmbContaOrigem.setSelectedItem(item);
                            break;
                        }
                    }
                }
            });
        }
    }

    private void abrirNovaConta() {
        try {
            String[] tipos = {"CORRENTE", "POUPANCA"};
            String tipo = (String) JOptionPane.showInputDialog(this, "Tipo de conta:", "Nova Conta",
                    JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
            if (tipo == null) return;

            List<String> agencias = CatalogoBancos.getAgenciasYashin();
            String agencia = (String) JOptionPane.showInputDialog(this, "Agência:", "Nova Conta",
                    JOptionPane.QUESTION_MESSAGE, null, agencias.toArray(), agencias.get(0));
            if (agencia == null) return;

            Conta novaConta = controller.abrirNovaConta(clienteLogado.getIdCliente(), tipo, agencia);
            JOptionPane.showMessageDialog(this, "Nova conta criada!\nNúmero: " + novaConta.getNumeroConta() +
                    "\nSaldo inicial: " + FormatadorUtil.formatarMoeda(novaConta.getSaldo()));

            contas = controller.buscarContasPorCliente(clienteLogado.getIdCliente());
            Sessao.getInstance().setContasCliente(contas);
            atualizarCombosContas();
            carregarSaldo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== SALDO/EXTRATO ==================
    private JPanel criarPainelSaldoExtrato() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        java.text.NumberFormat formatadorMoeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));

        BigDecimal saldoDisponivel = BigDecimal.ZERO;
        int idConta = 0;
        if (!Sessao.getInstance().getContasCliente().isEmpty()) {
            com.mycompany.yashin.model.Conta contaAtual = Sessao.getInstance().getContasCliente().get(0);
            saldoDisponivel = contaAtual.getSaldo();
            idConta = contaAtual.getIdConta();
        }

        // ==========================================
        // 1. CONTAINER SUPERIOR: CARDS DE SALDO E RESUMOS
        // ==========================================
        JPanel painelCards = new JPanel(new GridLayout(1, 2, 20, 0));
        painelCards.setBackground(Color.WHITE);

        // --- CARD 1: SALDO DISPONÍVEL ---
        JPanel cardSaldo = new JPanel(new BorderLayout());
        cardSaldo.setBackground(new Color(248, 249, 250)); // Cinza bem claro premium
        cardSaldo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 233, 237), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTituloSaldo = new JLabel("Saldo disponível");
        lblTituloSaldo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTituloSaldo.setForeground(new Color(110, 115, 125));

        final BigDecimal valorSaldoFinal = saldoDisponivel;
        JLabel lblValorSaldo = new JLabel(formatadorMoeda.format(valorSaldoFinal));
        lblValorSaldo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValorSaldo.setForeground(new Color(33, 37, 41));

        JButton btnOlho = new JButton("👁");
        btnOlho.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnOlho.setBorderPainted(false);
        btnOlho.setContentAreaFilled(false);
        btnOlho.setFocusPainted(false);
        btnOlho.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOlho.addActionListener(e -> {
            saldoVisivel = !saldoVisivel;
            if (saldoVisivel) {
                lblValorSaldo.setText(formatadorMoeda.format(valorSaldoFinal));
                btnOlho.setText("👁");
            } else {
                lblValorSaldo.setText("R$ ••••,••");
                btnOlho.setText("👁‍🗨");
            }
        });

        JPanel painelLinhaSaldo = new JPanel(new BorderLayout());
        painelLinhaSaldo.setOpaque(false);
        painelLinhaSaldo.add(lblValorSaldo, BorderLayout.WEST);
        painelLinhaSaldo.add(btnOlho, BorderLayout.EAST);

        cardSaldo.add(lblTituloSaldo, BorderLayout.NORTH);
        cardSaldo.add(painelLinhaSaldo, BorderLayout.CENTER);

        // --- CARD 2: RESUMO DE ENTRADAS / SAÍDAS ---
        JPanel cardResumo = new JPanel(new GridLayout(2, 1, 0, 5));
        cardResumo.setBackground(new Color(248, 249, 250));
        cardResumo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 233, 237), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblEntradas = new JLabel("<html>↑ Entradas: <font color='#27ae60'><b>+ R$ 0,00</b></font></html>");
        lblEntradas.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblSaidas = new JLabel("<html>↓ Saídas: <font color='#c0392b'><b>- R$ 0,00</b></font></html>");
        lblSaidas.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblEntradas.setText("<html>↑ Entradas: <font color='#27ae60'><b>+ R$ 0,00</b></font></html>");
        lblSaidas.setText("<html>↓ Saídas: <font color='#c0392b'><b>- R$ 0,00</b></font></html>");

        cardResumo.add(lblEntradas);
        cardResumo.add(lblSaidas);

        painelCards.add(cardSaldo);
        painelCards.add(cardResumo);
        mainPanel.add(painelCards, BorderLayout.NORTH);

        // ==========================================
        // 2. CONTAINER CENTRAL: HISTÓRICO DO EXTRATO
        // ==========================================
        JPanel painelExtrato = new JPanel(new BorderLayout(0, 10));
        painelExtrato.setBackground(Color.WHITE);

        JLabel lblTituloExtrato = new JLabel("Histórico de Transações");
        lblTituloExtrato.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTituloExtrato.setForeground(new Color(50, 55, 65));
        painelExtrato.add(lblTituloExtrato, BorderLayout.NORTH);

        String[] colunas = {"Data/Hora", "Tipo", "Descrição", "Valor"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Bloqueia edição direta nas células
        };

        JTable tabelaExtrato = new JTable(modeloTabela);
        tabelaExtrato.setRowHeight(38); // Linhas mais altas geram um visual muito mais limpo (respiro)
        tabelaExtrato.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaExtrato.setShowVerticalLines(false); // Remove as linhas verticais feias de planilhas antigas
        tabelaExtrato.setGridColor(new Color(240, 240, 245));
        tabelaExtrato.setSelectionBackground(new Color(235, 243, 255)); // Cor sutil de seleção
        tabelaExtrato.setSelectionForeground(Color.BLACK);

        tabelaExtrato.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaExtrato.getTableHeader().setBackground(new Color(240, 242, 245));
        tabelaExtrato.getTableHeader().setForeground(new Color(70, 75, 85));
        tabelaExtrato.getTableHeader().setReorderingAllowed(false);

        // --- CARREGAMENTO DOS DADOS NO EXTRATO ---
        try {
         if (idConta > 0) {
             // Buscando as transações da conta
             java.time.LocalDateTime fim = java.time.LocalDateTime.now();
             java.time.LocalDateTime inicio = fim.minusDays(30);

             java.util.List<com.mycompany.yashin.model.Transacao> transacoes = controller.visualizarExtrato(idConta, inicio, fim);

             // VARIÁVEIS PARA SOMAR TUDO
             BigDecimal somaEntradas = BigDecimal.ZERO;
             BigDecimal somaSaidas = BigDecimal.ZERO;

             if (transacoes != null && !transacoes.isEmpty()) {
                 java.time.format.DateTimeFormatter formatadorData = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                 for (com.mycompany.yashin.model.Transacao t : transacoes) {
                     BigDecimal valor = t.getValor();
                     String sinal = "";

                     // Lógica para descobrir se o dinheiro ENTROU ou SAIU
                     boolean ehEntrada = t.getIdContaDestino() != null && t.getIdContaDestino() == idConta;
                     if (!ehEntrada) {
                         sinal = "- ";
                         somaSaidas = somaSaidas.add(valor); // <-- SOMA NAS SAÍDAS
                     } else {
                         sinal = "+ ";
                         somaEntradas = somaEntradas.add(valor); // <-- SOMA NAS ENTRADAS
                     }

                     modeloTabela.addRow(new Object[]{
                         t.getDataHora() != null ? t.getDataHora().format(formatadorData) : "-",
                         t.getTipoTransacao() != null ? t.getTipoTransacao().toString() : "OUTROS",
                         t.getDescricao(),
                         sinal + formatadorMoeda.format(valor)
                     });
                 }

                 // DEPOIS DE SOMAR TUDO, ATUALIZAMOS OS CARDS LÁ DE CIMA!
                 lblEntradas.setText("<html>↑ Entradas: <font color='#27ae60'><b>+ " + formatadorMoeda.format(somaEntradas) + "</b></font></html>");
                 lblSaidas.setText("<html>↓ Saídas: <font color='#c0392b'><b>- " + formatadorMoeda.format(somaSaidas) + "</b></font></html>");

             } else {
                 modeloTabela.addRow(new Object[]{"-", "Nenhuma transação encontrada", "Período sem movimentações.", "-"});
             }
         }
     } catch (Exception ex) {
         modeloTabela.addRow(new Object[]{"Erro", "Falha ao carregar extrato", ex.getMessage(), "-"});
     }
        // ==========================================
        // 3. RENDERIZADOR DE CORES CUSTOMIZADAS (O segredo do visual)
        // ==========================================
        tabelaExtrato.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Object valorColuna = table.getValueAt(row, 3);
                if (valorColuna != null) {
                    String strValor = valorColuna.toString();
                    if (strValor.startsWith("+")) {
                        if (column == 3) c.setForeground(new Color(39, 174, 96));
                        else c.setForeground(new Color(33, 37, 41));
                    } else if (strValor.startsWith("-")) {
                        if (column == 3) c.setForeground(new Color(192, 41, 43));
                        else c.setForeground(new Color(33, 37, 41));
                    } else {
                        c.setForeground(Color.GRAY);
                    }
                }

                if (!isSelected) {
                    if (row % 2 == 0) c.setBackground(Color.WHITE);
                    else c.setBackground(new Color(252, 252, 254));
                }

                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tabelaExtrato);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 240)));
        scrollTable.getViewport().setBackground(Color.WHITE);
        painelExtrato.add(scrollTable, BorderLayout.CENTER);

        mainPanel.add(painelExtrato, BorderLayout.CENTER);
        return mainPanel;
    }

    private void carregarSaldo() {
        if (cmbContaSaldo.getSelectedItem() == null) return;
        Conta c = (Conta) cmbContaSaldo.getSelectedItem();
        try {
            BigDecimal saldo = controller.consultarSaldo(c.getIdConta());
            lblSaldo.setText(FormatadorUtil.formatarMoeda(saldo));
        } catch (Exception ex) {
            lblSaldo.setText("Erro: " + ex.getMessage());
        }
    }

    private void carregarExtrato() {
        if (cmbContaSaldo.getSelectedItem() == null) return;
        Conta c = (Conta) cmbContaSaldo.getSelectedItem();
        try {
            LocalDateTime fim = LocalDateTime.now();
            LocalDateTime inicio = fim.minusDays(30);
            List<Transacao> transacoes = controller.visualizarExtrato(c.getIdConta(), inicio, fim);
            StringBuilder sb = new StringBuilder();
            for (Transacao t : transacoes) {
                sb.append(FormatadorUtil.formatarDataHora(t.getDataHora()))
                  .append(" | ").append(t.getTipoTransacao())
                  .append(" | R$ ").append(t.getValor())
                  .append(" | ").append(t.getDescricao())
                  .append("\n");
            }
            txtExtrato.setText(sb.toString());
        } catch (Exception ex) {
            txtExtrato.setText("Erro: " + ex.getMessage());
        }
    }

    private void gerarExtratoPDF() {
        if (cmbContaSaldo.getSelectedItem() == null) return;
        Conta c = (Conta) cmbContaSaldo.getSelectedItem();
        try {
            LocalDateTime fim = LocalDateTime.now();
            LocalDateTime inicio = fim.minusDays(30);
            String extrato = controller.gerarExtratoPDF(c.getIdConta(), inicio, fim);
            JOptionPane.showMessageDialog(this, extrato, "Extrato PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== TRANSFERÊNCIAS ==================
    private JPanel criarPainelTransferencias() {
        JPanel painelPrincipal = new JPanel(new BorderLayout());

        JPanel conteudo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        int linha = 0;

        JLabel titulo = new JLabel("Transferências");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(titulo, gbc);
        linha++;

        // Transferência Interna
        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Transferência Interna"), gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Conta Origem:"), gbc);
        cmbContaOrigem = new JComboBox<>();
        gbc.gridx = 1;
        conteudo.add(cmbContaOrigem, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Conta Destino:"), gbc);
        cmbContaDestino = new JComboBox<>();
        gbc.gridx = 1;
        conteudo.add(cmbContaDestino, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Valor:"), gbc);
        JTextField txtValorInterna = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtValorInterna, gbc);
        linha++;

        JButton btnTransferirInterna = new JButton("Transferir");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnTransferirInterna.addActionListener(e -> {
            try {
                Conta origem = (Conta) cmbContaOrigem.getSelectedItem();
                Conta destino = (Conta) cmbContaDestino.getSelectedItem();
                if (origem == null || destino == null) throw new Exception("Selecione contas válidas.");
                if (origem.getIdConta().equals(destino.getIdConta())) throw new Exception("Origem e destino devem ser diferentes.");
                BigDecimal valor = new BigDecimal(txtValorInterna.getText().replace(",", "."));
                controller.transferenciaInterna(origem.getIdConta(), destino.getIdConta(), valor);
                JOptionPane.showMessageDialog(this, "Transferência interna realizada com sucesso!");
                carregarSaldo();
                txtValorInterna.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnTransferirInterna, gbc);
        linha++;

        // Separador
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // TED
        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        JLabel lblTed = new JLabel("Transferência TED");
        lblTed.setFont(new Font("Arial", Font.BOLD, 14));
        conteudo.add(lblTed, gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Banco Destino:"), gbc);
        JTextField txtBancoTED = new JTextField(5);
        JLabel lblNomeBanco = new JLabel("");
        lblNomeBanco.setForeground(Color.BLUE);

        cmbAgenciaTED = new JComboBox<>();
        txtBancoTED.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { atualizar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { atualizar(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { atualizar(); }
            private void atualizar() {
                String codigo = txtBancoTED.getText().trim();
                if (CatalogoBancos.isBancoValido(codigo)) {
                    lblNomeBanco.setText(CatalogoBancos.getNomeBanco(codigo));
                    List<String> agencias = CatalogoBancos.getAgencias(codigo);
                    cmbAgenciaTED.removeAllItems();
                    for (String ag : agencias) {
                        cmbAgenciaTED.addItem(ag);
                    }
                    if (!agencias.isEmpty()) cmbAgenciaTED.setSelectedIndex(0);
                } else {
                    lblNomeBanco.setText("");
                    cmbAgenciaTED.removeAllItems();
                }
            }
        });

        JPanel panelBanco = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelBanco.add(txtBancoTED);
        panelBanco.add(Box.createHorizontalStrut(5));
        panelBanco.add(lblNomeBanco);
        gbc.gridx = 1;
        conteudo.add(panelBanco, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Agência:"), gbc);
        gbc.gridx = 1;
        conteudo.add(cmbAgenciaTED, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Conta Destino:"), gbc);
        JTextField txtContaTED = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtContaTED, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("CPF/CNPJ Beneficiário:"), gbc);
        JTextField txtCpfTED = new JTextField(14);
        gbc.gridx = 1;
        conteudo.add(txtCpfTED, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Nome Beneficiário:"), gbc);
        JTextField txtNomeTED = new JTextField(15);
        gbc.gridx = 1;
        conteudo.add(txtNomeTED, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Valor:"), gbc);
        JTextField txtValorTED = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtValorTED, gbc);
        linha++;

        JButton btnTED = new JButton("Enviar TED");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnTED.addActionListener(e -> {
            try {
                Conta origem = (Conta) cmbContaOrigem.getSelectedItem();
                if (origem == null) throw new Exception("Selecione uma conta origem.");
                String banco = txtBancoTED.getText().trim();
                if (!CatalogoBancos.isBancoValido(banco)) {
                    throw new Exception("Código de banco inválido.");
                }
                String agencia = (String) cmbAgenciaTED.getSelectedItem();
                if (agencia == null || agencia.isEmpty()) {
                    throw new Exception("Selecione uma agência.");
                }
                String conta = txtContaTED.getText().trim();
                String cpfCnpj = txtCpfTED.getText().trim();
                String nome = txtNomeTED.getText().trim();
                BigDecimal valor = new BigDecimal(txtValorTED.getText().replace(",", "."));
                controller.transferenciaTED(origem.getIdConta(), banco, agencia, conta, cpfCnpj, nome, valor);
                JOptionPane.showMessageDialog(this, "TED enviada com sucesso!");
                carregarSaldo();
                txtValorTED.setText("");
                txtBancoTED.setText("");
                lblNomeBanco.setText("");
                cmbAgenciaTED.removeAllItems();
                txtContaTED.setText("");
                txtCpfTED.setText("");
                txtNomeTED.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnTED, gbc);
        linha++;

        // Separador
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // PIX
        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Transferência PIX"), gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Chave PIX:"), gbc);
        JTextField txtChavePIX = new JTextField(15);
        gbc.gridx = 1;
        conteudo.add(txtChavePIX, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Valor:"), gbc);
        JTextField txtValorPIX = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtValorPIX, gbc);
        linha++;

        JButton btnPIX = new JButton("Enviar PIX");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnPIX.addActionListener(e -> {
            try {
                Conta origem = (Conta) cmbContaOrigem.getSelectedItem();
                BigDecimal valor = new BigDecimal(txtValorPIX.getText().replace(",", "."));
                controller.transferenciaPIX(origem.getIdConta(), txtChavePIX.getText(), valor);
                JOptionPane.showMessageDialog(this, "PIX enviado com sucesso!");
                carregarSaldo();
                txtValorPIX.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnPIX, gbc);
        linha++;

        // Agendamento
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Agendar Transferência"), gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Tipo (TED/PIX):"), gbc);
        JComboBox<String> cmbTipoAgend = new JComboBox<>(new String[]{"TED", "PIX"});
        gbc.gridx = 1;
        conteudo.add(cmbTipoAgend, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Dados Destino (JSON):"), gbc);
        JTextField txtDadosAgend = new JTextField(20);
        gbc.gridx = 1;
        conteudo.add(txtDadosAgend, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Valor:"), gbc);
        JTextField txtValorAgend = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtValorAgend, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Data (dd/mm/aaaa):"), gbc);
        JTextField txtDataAgend = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtDataAgend, gbc);
        linha++;

        JButton btnAgendar = new JButton("Agendar");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnAgendar.addActionListener(e -> {
            try {
                Conta origem = (Conta) cmbContaOrigem.getSelectedItem();
                String tipo = (String) cmbTipoAgend.getSelectedItem();
                BigDecimal valor = new BigDecimal(txtValorAgend.getText().replace(",", "."));
                LocalDate data = FormatadorUtil.parseData(txtDataAgend.getText());
                controller.agendarTransferencia(origem.getIdConta(), tipo, txtDadosAgend.getText(), valor, data);
                JOptionPane.showMessageDialog(this, "Transferência agendada com sucesso!");
                carregarTabelaAgendamentos();
                txtValorAgend.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnAgendar, gbc);

        JScrollPane scroll = new JScrollPane(conteudo);
        painelPrincipal.add(scroll, BorderLayout.CENTER);
        return painelPrincipal;
    }

    // ================== PAGAMENTOS ==================
    private JPanel criarPainelPagamentos() {
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        JPanel conteudo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        int linha = 0;

        JLabel titulo = new JLabel("Pagamentos");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(titulo, gbc);
        linha++;

        // Pagar Conta
        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Pagar Conta com Linha Digitável"), gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Conta Débito:"), gbc);
        cmbContaPagamento = new JComboBox<>();
        gbc.gridx = 1;
        conteudo.add(cmbContaPagamento, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Linha Digitável:"), gbc);
        JTextField txtLinha = new JTextField(30);
        gbc.gridx = 1;
        conteudo.add(txtLinha, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Valor:"), gbc);
        JTextField txtValorConta = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtValorConta, gbc);
        linha++;

        JButton btnPagarConta = new JButton("Pagar Conta");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnPagarConta.addActionListener(e -> {
            try {
                Conta c = (Conta) cmbContaPagamento.getSelectedItem();
                BigDecimal valor = new BigDecimal(txtValorConta.getText().replace(",", "."));
                controller.pagarConta(c.getIdConta(), txtLinha.getText(), valor);
                JOptionPane.showMessageDialog(this, "Conta paga com sucesso!");
                carregarSaldo();
                txtValorConta.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnPagarConta, gbc);
        linha++;

        // Separador
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // Pagar Fatura Cartão
        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Pagar Fatura de Cartão de Crédito"), gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("ID da Fatura:"), gbc);
        JTextField txtIdFatura = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtIdFatura, gbc);
        linha++;

        JButton btnPagarFatura = new JButton("Pagar Fatura");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnPagarFatura.addActionListener(e -> {
            try {
                Conta c = (Conta) cmbContaPagamento.getSelectedItem();
                int idFatura = Integer.parseInt(txtIdFatura.getText());
                controller.pagarFaturaCartao(c.getIdConta(), idFatura);
                JOptionPane.showMessageDialog(this, "Fatura paga com sucesso!");
                carregarSaldo();
                carregarTabelaFaturas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnPagarFatura, gbc);
        linha++;

        // Separador
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // PIX QR Code
        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Pagamento via PIX QR Code"), gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("QR Code (texto):"), gbc);
        JTextField txtQRCode = new JTextField(30);
        gbc.gridx = 1;
        conteudo.add(txtQRCode, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Valor:"), gbc);
        JTextField txtValorQR = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtValorQR, gbc);
        linha++;

        JButton btnPagarQR = new JButton("Pagar com PIX");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnPagarQR.addActionListener(e -> {
            try {
                Conta c = (Conta) cmbContaPagamento.getSelectedItem();
                BigDecimal valor = new BigDecimal(txtValorQR.getText().replace(",", "."));
                controller.pagarPIXQRCode(c.getIdConta(), txtQRCode.getText(), valor);
                JOptionPane.showMessageDialog(this, "Pagamento PIX realizado com sucesso!");
                carregarSaldo();
                txtValorQR.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnPagarQR, gbc);
        linha++;

        // Separador
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // Pagamento com Cartão
        gbc.gridwidth = 2;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Pagamento com Cartão de Crédito"), gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Selecione o Cartão:"), gbc);
        JComboBox<CartaoCredito> cmbCartaoPagamento = new JComboBox<>();
        try {
            List<CartaoCredito> cartoes = controller.listarCartoesCliente(clienteLogado.getIdCliente());
            for (CartaoCredito c : cartoes) {
                if ("ATIVO".equals(c.getStatus())) {
                    cmbCartaoPagamento.addItem(c);
                }
            }
        } catch (Exception ex) {
            // ignora
        }
        gbc.gridx = 1;
        conteudo.add(cmbCartaoPagamento, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Valor da Compra:"), gbc);
        JTextField txtValorCartao = new JTextField(10);
        gbc.gridx = 1;
        conteudo.add(txtValorCartao, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Descrição:"), gbc);
        JTextField txtDescCartao = new JTextField(20);
        gbc.gridx = 1;
        conteudo.add(txtDescCartao, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        conteudo.add(new JLabel("Parcelas (1-12):"), gbc);
        JTextField txtParcelasCartao = new JTextField(3);
        gbc.gridx = 1;
        conteudo.add(txtParcelasCartao, gbc);
        linha++;

        JButton btnPagarCartao = new JButton("Confirmar Compra no Cartão");
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        btnPagarCartao.addActionListener(e -> {
            try {
                CartaoCredito cartao = (CartaoCredito) cmbCartaoPagamento.getSelectedItem();
                if (cartao == null) throw new Exception("Selecione um cartão.");
                BigDecimal valor = new BigDecimal(txtValorCartao.getText().replace(",", "."));
                String descricao = txtDescCartao.getText().trim();
                if (descricao.isEmpty()) descricao = "Compra no cartão";
                int parcelas = Integer.parseInt(txtParcelasCartao.getText());
                if (parcelas < 1 || parcelas > 12) throw new Exception("Parcelas deve ser entre 1 e 12.");
                controller.pagarComCartao(cartao.getIdCartao(), valor, descricao, parcelas);
                JOptionPane.showMessageDialog(this, "Compra realizada com sucesso!");
                txtValorCartao.setText("");
                txtDescCartao.setText("");
                txtParcelasCartao.setText("");
                carregarTabelaCartoes();
                carregarTabelaFaturas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        conteudo.add(btnPagarCartao, gbc);

        JScrollPane scroll = new JScrollPane(conteudo);
        painelPrincipal.add(scroll, BorderLayout.CENTER);
        return painelPrincipal;
    }

    // ================== EMPRÉSTIMOS ==================
    private JPanel criarPainelEmprestimos() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int linha = 0;
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel titulo = new JLabel("Simulação e Solicitação de Empréstimo");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titulo, gbc);
        linha++;

        gbc.gridwidth = 1;
        gbc.gridy = linha; gbc.gridx = 0;
        panel.add(new JLabel("Valor Desejado:"), gbc);
        JTextField txtValorEmprestimo = new JTextField(10);
        gbc.gridx = 1;
        panel.add(txtValorEmprestimo, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        panel.add(new JLabel("Prazo (meses):"), gbc);
        JTextField txtPrazoEmprestimo = new JTextField(5);
        gbc.gridx = 1;
        panel.add(txtPrazoEmprestimo, gbc);
        linha++;

        gbc.gridy = linha; gbc.gridx = 0;
        panel.add(new JLabel("Finalidade:"), gbc);
        JTextField txtFinalidade = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtFinalidade, gbc);
        linha++;

        JButton btnSimular = new JButton("Simular");
        JButton btnSolicitar = new JButton("Solicitar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoes.add(btnSimular);
        botoes.add(btnSolicitar);
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(botoes, gbc);
        linha++;

        // Área de resultado rápido
        txtResultadoEmprestimo = new JTextArea(4, 40);
        txtResultadoEmprestimo.setEditable(false);
        txtResultadoEmprestimo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollResultado = new JScrollPane(txtResultadoEmprestimo);
        scrollResultado.setPreferredSize(new Dimension(400, 70));
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(scrollResultado, gbc);
        linha++;

        // Histórico
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Histórico de Solicitações:"), gbc);
        linha++;

        DefaultTableModel modelHistorico = new DefaultTableModel(
                new Object[]{"ID", "Valor", "Prazo", "Status", "Data"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tableHistorico = new JTable(modelHistorico);
        tableHistorico.setRowHeight(25);
        tableHistorico.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tableHistorico.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        int id = (int) modelHistorico.getValueAt(row, 0);
                        try {
                            EmprestimoSolicitacao solicitacao = controller.buscarSolicitacaoPorId(id);
                            if (solicitacao != null) {
                                exibirDetalhesEmprestimo(solicitacao, "SOLICITAÇÃO");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(panel, "Erro: " + ex.getMessage());
                        }
                    }
                }
            }
        });

        JScrollPane scrollHistorico = new JScrollPane(tableHistorico);
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(scrollHistorico, gbc);

        JPanel bottomHistorico = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnCarregarHistorico = new JButton("Carregar Histórico");
        btnCarregarHistorico.addActionListener(e -> atualizarHistoricoEmprestimos(modelHistorico));
        bottomHistorico.add(btnCarregarHistorico);

        JButton btnVerDetalhes = new JButton("Ver Detalhes (duplo clique)");
        btnVerDetalhes.setEnabled(false);
        bottomHistorico.add(btnVerDetalhes);

        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panel.add(bottomHistorico, gbc);

        // Ação Simular
        btnSimular.addActionListener(e -> {
            try {
                BigDecimal valor = new BigDecimal(txtValorEmprestimo.getText().replace(",", "."));
                int prazo = Integer.parseInt(txtPrazoEmprestimo.getText());
                EmprestimoSolicitacao sim = controller.simularEmprestimo(clienteLogado.getIdCliente(), valor, prazo);
                exibirDetalhesEmprestimo(sim, "SIMULAÇÃO");
                txtResultadoEmprestimo.setText("Simulação: Parcela = " + FormatadorUtil.formatarMoeda(sim.getValorParcela()) +
                        " | Total = " + FormatadorUtil.formatarMoeda(sim.getValorParcela().multiply(BigDecimal.valueOf(sim.getNumeroParcelas()))));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Ação Solicitar (com animação)
        btnSolicitar.addActionListener(e -> {
            try {
                BigDecimal valor = new BigDecimal(txtValorEmprestimo.getText().replace(",", "."));
                int prazo = Integer.parseInt(txtPrazoEmprestimo.getText());
                String finalidade = txtFinalidade.getText().trim();
                processarSolicitacaoComAnimacao(clienteLogado.getIdCliente(), valor, prazo, finalidade, modelHistorico);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Carregar histórico automaticamente
        atualizarHistoricoEmprestimos(modelHistorico);

        return panel;
    }

    private void processarSolicitacaoComAnimacao(int idCliente, BigDecimal valor, int prazo, String finalidade, DefaultTableModel modelHistorico) {
        JDialog dialogLoading = new JDialog(this, "Análise de Crédito", true);
        dialogLoading.setSize(400, 200);
        dialogLoading.setLocationRelativeTo(this);
        dialogLoading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblIcon = new JLabel("🔄");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblIcon, gbc);

        JLabel lblMensagem = new JLabel("Analisando seu perfil de crédito...");
        lblMensagem.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(lblMensagem, gbc);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(progressBar, gbc);

        JLabel lblStatus = new JLabel("Consultando bureaus de crédito...");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStatus.setForeground(Color.GRAY);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(lblStatus, gbc);

        dialogLoading.add(panel);

        SwingWorker<EmprestimoSolicitacao, String> worker = new SwingWorker<EmprestimoSolicitacao, String>() {
            @Override
            protected EmprestimoSolicitacao doInBackground() throws Exception {
                publish("Consultando bureaus de crédito...");
                Thread.sleep(1000);
                publish("Analisando score e comprometimento...");
                Thread.sleep(1000);
                publish("Calculando condições do empréstimo...");
                Thread.sleep(800);
                return controller.solicitarEmprestimo(idCliente, valor, prazo, finalidade);
            }

            @Override
            protected void process(List<String> chunks) {
                lblStatus.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                dialogLoading.dispose();
                try {
                    EmprestimoSolicitacao solicitacao = get();
                    if (solicitacao.getIdSolicitacao() == null) {
                        // Se o ID for nulo, recarregar o histórico para obter o ID mais recente
                        // Na verdade, o histórico já deve mostrar, mas vamos tentar buscar novamente
                        atualizarHistoricoEmprestimos(modelHistorico);
                        // Tenta buscar a última solicitação do cliente
                        List<EmprestimoSolicitacao> lista = controller.listarEmprestimosCliente(idCliente);
                        if (!lista.isEmpty()) {
                            solicitacao = lista.get(lista.size() - 1); // pega a mais recente
                        }
                    }
                    exibirDetalhesEmprestimo(solicitacao, "SOLICITAÇÃO");
                    if (solicitacao.getStatus().toString().equals("APROVADO")) {
                        carregarSaldo();
                    }
                    atualizarHistoricoEmprestimos(modelHistorico);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TelaPrincipal.this,
                        "Erro na solicitação: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        dialogLoading.setVisible(true);
    }

    private void atualizarHistoricoEmprestimos(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            List<EmprestimoSolicitacao> lista = controller.listarEmprestimosCliente(clienteLogado.getIdCliente());
            for (EmprestimoSolicitacao s : lista) {
                model.addRow(new Object[]{
                        s.getIdSolicitacao() != null ? s.getIdSolicitacao() : 0,
                        FormatadorUtil.formatarMoeda(s.getValorSolicitado()),
                        s.getPrazoMeses(),
                        s.getStatus(),
                        FormatadorUtil.formatarDataHora(s.getDataSolicitacao())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exibirDetalhesEmprestimo(EmprestimoSolicitacao obj, String tipo) {
        JDialog dialog = new JDialog(this, "Detalhes do Empréstimo - " + tipo, true);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font tituloFont = new Font("Arial", Font.BOLD, 16);
        Font normalFont = new Font("Arial", Font.PLAIN, 14);
        Font destaqueFont = new Font("Arial", Font.BOLD, 18);

        JLabel lblTitulo = new JLabel(tipo + " de Empréstimo");
        lblTitulo.setFont(tituloFont);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(15));

        if ("SOLICITAÇÃO".equals(tipo)) {
            JLabel lblStatus = new JLabel("Status: " + obj.getStatus());
            lblStatus.setFont(destaqueFont);
            lblStatus.setForeground(obj.getStatus().toString().equals("APROVADO") ? Color.GREEN :
                                   obj.getStatus().toString().equals("NEGADO") ? Color.RED : Color.ORANGE);
            lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblStatus);
            panel.add(Box.createVerticalStrut(15));
        }

        addLinha(panel, "Valor Solicitado:", FormatadorUtil.formatarMoeda(obj.getValorSolicitado()), normalFont);
        addLinha(panel, "Prazo (meses):", String.valueOf(obj.getPrazoMeses()), normalFont);
        addLinha(panel, "Taxa de Juros:", (obj.getTaxaJuros() != null ? obj.getTaxaJuros() : "N/A") + "% ao mês", normalFont);

        BigDecimal valorParcela = obj.getValorParcela();
        Integer parcelas = obj.getNumeroParcelas() != null ? obj.getNumeroParcelas() : obj.getPrazoMeses();

        if (valorParcela != null && parcelas != null) {
            BigDecimal totalPagar = valorParcela.multiply(BigDecimal.valueOf(parcelas));
            BigDecimal totalJuros = totalPagar.subtract(obj.getValorSolicitado());
            addLinha(panel, "Valor da Parcela:", FormatadorUtil.formatarMoeda(valorParcela), normalFont);
            addLinha(panel, "Total a Pagar:", FormatadorUtil.formatarMoeda(totalPagar), normalFont);
            addLinha(panel, "Total de Juros:", FormatadorUtil.formatarMoeda(totalJuros), normalFont);
        } else {
            addLinha(panel, "Valor da Parcela:", "Indisponível", normalFont);
            addLinha(panel, "Total a Pagar:", "Indisponível", normalFont);
            addLinha(panel, "Total de Juros:", "Indisponível", normalFont);
        }

        if ("SOLICITAÇÃO".equals(tipo)) {
            addLinha(panel, "Data da Solicitação:", FormatadorUtil.formatarDataHora(obj.getDataSolicitacao()), normalFont);

            if (obj.getStatus().toString().equals("APROVADO")) {
                panel.add(Box.createVerticalStrut(10));
                JLabel lblCondicoes = new JLabel("Condições Aprovadas:");
                lblCondicoes.setFont(tituloFont);
                lblCondicoes.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(lblCondicoes);
                panel.add(Box.createVerticalStrut(5));
                addLinha(panel, "Valor Aprovado:", FormatadorUtil.formatarMoeda(obj.getValorAprovado()), normalFont);
                addLinha(panel, "Número de Parcelas:", String.valueOf(obj.getNumeroParcelas()), normalFont);
                addLinha(panel, "Data de Aprovação:", FormatadorUtil.formatarDataHora(obj.getDataAprovacao()), normalFont);
                addLinha(panel, "ID da Solicitação:", 
    obj.getIdSolicitacao() != null ? String.valueOf(obj.getIdSolicitacao()) : "N/A", normalFont);
            }

            if (obj.getStatus().toString().equals("NEGADO") && obj.getMotivoNegacao() != null) {
                panel.add(Box.createVerticalStrut(10));
                JLabel lblMotivo = new JLabel("Motivo da Negação:");
                lblMotivo.setFont(tituloFont);
                lblMotivo.setForeground(Color.RED);
                lblMotivo.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(lblMotivo);
                panel.add(Box.createVerticalStrut(5));
                JLabel lblMotivoTexto = new JLabel(obj.getMotivoNegacao());
                lblMotivoTexto.setFont(normalFont);
                lblMotivoTexto.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(lblMotivoTexto);
            }
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        dialog.add(scroll);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(ev -> dialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(btnFechar);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addLinha(JPanel panel, String label, String valor, Font font) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(font);
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(font);
        linha.add(lblLabel);
        linha.add(lblValor);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(linha);
    }

    // ================== CARTÕES ==================
    
    private JPanel criarWidgetCartao(CartaoCredito cartao) {
    JPanel painelCartao = new JPanel();
    painelCartao.setLayout(new BorderLayout());
    
    // Cores modernas (Estilo Dark/Premium)
    Color corFundo = new Color(32, 32, 45); // Cinza azulado escuro
    Color corTextoPrincipal = Color.WHITE;
    Color corTextoSecundario = new Color(170, 170, 185);

    painelCartao.setBackground(corFundo);
    painelCartao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 1, true), // Borda arredondada suave
            BorderFactory.createEmptyBorder(18, 18, 18, 18) // Espaçamento interno
    ));
    painelCartao.setPreferredSize(new Dimension(320, 190));
    painelCartao.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de clique

    // Topo do Cartão (Bandeira e Tipo)
    JPanel painelTopo = new JPanel(new BorderLayout());
    painelTopo.setOpaque(false);
    JLabel lblBandeira = new JLabel(cartao.getBandeira().toUpperCase());
    lblBandeira.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblBandeira.setForeground(corTextoPrincipal);
    
    JLabel lblTipo = new JLabel(cartao.getTipo().toUpperCase());
    lblTipo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    lblTipo.setForeground(corTextoSecundario);
    
    painelTopo.add(lblBandeira, BorderLayout.WEST);
    painelTopo.add(lblTipo, BorderLayout.EAST);

    // Centro do Cartão (Número mascarado)
    String numSub = cartao.getNumeroCartao().substring(Math.max(0, cartao.getNumeroCartao().length() - 4));
    JLabel lblNumero = new JLabel("••••  ••••  ••••  " + numSub);
    lblNumero.setFont(new Font("Consolas", Font.BOLD, 22));
    lblNumero.setForeground(corTextoPrincipal);
    lblNumero.setHorizontalAlignment(SwingConstants.CENTER);

    // Rodapé do Cartão (Limite Disponível rápido)
    JPanel painelRodape = new JPanel(new GridLayout(2, 1));
    painelRodape.setOpaque(false);
    
    JLabel lblTituloLimite = new JLabel("Limite disponível");
    lblTituloLimite.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lblTituloLimite.setForeground(corTextoSecundario);
    
    BigDecimal disponivel = cartao.getLimiteTotal().subtract(cartao.getLimiteUtilizado());
    JLabel lblValorLimite = new JLabel("R$ " + FormatadorUtil.formatarMoeda(disponivel)); // Ajuste se seu formatador for diferente
    lblValorLimite.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblValorLimite.setForeground(new Color(46, 204, 113)); // Verde moderno

    painelRodape.add(lblTituloLimite);
    painelRodape.add(lblValorLimite);

    // Montando o cartão
    painelCartao.add(painelTopo, BorderLayout.NORTH);
    painelCartao.add(lblNumero, BorderLayout.CENTER);
    painelCartao.add(painelRodape, BorderLayout.SOUTH);

    // Efeito Visual de passar o mouse por cima (Hover)
    painelCartao.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            painelCartao.setBackground(new Color(45, 45, 65));
        }
        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            painelCartao.setBackground(corFundo);
        }
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            abrirDetalhesDoCartao(cartao);
        }
    });

    return painelCartao;
}

/**
 * Abre a janela flutuante detalhada com os limites e faturas do cartão clicado
 */
    private void abrirDetalhesDoCartao(CartaoCredito cartao) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gerenciamento do Cartão", true);
        dialog.setSize(650, 550);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new BorderLayout());

        // --- PAINEL SUPERIOR: Resumo de Limites (Estilo Banco) ---
        JPanel painelResumo = new JPanel();
        painelResumo.setLayout(new BoxLayout(painelResumo, BoxLayout.Y_AXIS));
        painelResumo.setBackground(new Color(245, 246, 250));
        painelResumo.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        BigDecimal total = cartao.getLimiteTotal();  /////////////LEMBRAR DESSA PARTE
        BigDecimal utilizado = cartao.getLimiteUtilizado();
        BigDecimal disponivel = total.subtract(utilizado);

        JLabel lblStatusFatura = new JLabel("Fatura Atual");
        lblStatusFatura.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblValorUtilizado = new JLabel("R$ " + utilizado);
        lblValorUtilizado.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValorUtilizado.setForeground(new Color(231, 76, 60)); // Vermelho para gasto

        // Barra de progresso visual do limite
        JProgressBar barraLimite = new JProgressBar(0, total.intValue());
        barraLimite.setValue(utilizado.intValue());
        barraLimite.setForeground(new Color(231, 76, 60));
        barraLimite.setBackground(new Color(220, 220, 230));
        barraLimite.setPreferredSize(new Dimension(500, 8));
        barraLimite.setBorderPainted(false);

        JLabel lblDisponivelTexto = new JLabel("Limite disponível: R$ " + disponivel + " de R$ " + total);
        lblDisponivelTexto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDisponivelTexto.setForeground(Color.GRAY);

        painelResumo.add(lblStatusFatura);
        painelResumo.add(Box.createVerticalStrut(5));
        painelResumo.add(lblValorUtilizado);
        painelResumo.add(Box.createVerticalStrut(10));
        painelResumo.add(barraLimite);
        painelResumo.add(Box.createVerticalStrut(5));
        painelResumo.add(lblDisponivelTexto);

        // --- PAINEL CENTRAL: Histórico / Descrição da Fatura ---
        JPanel painelHistorico = new JPanel(new BorderLayout());
        painelHistorico.setBackground(Color.WHITE);
        painelHistorico.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));

        JLabel lblTituloHistorico = new JLabel("Histórico da Fatura");
        lblTituloHistorico.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTituloHistorico.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        painelHistorico.add(lblTituloHistorico, BorderLayout.NORTH);

        // Tabela de compras
        String[] colunas = {"Data", "Estabelecimento / Descrição", "Parcelas", "Valor"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable tabelaFatura = new JTable(modeloTabela);
        tabelaFatura.setRowHeight(30);
        tabelaFatura.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaFatura.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaFatura.setFillsViewportHeight(true);

        // Carregar dados dinamicamente usando o controller
        try {
            FaturaCartao fatura = controller.buscarFaturaAberta(cartao.getIdCartao());
            if (fatura != null) {
                List<CompraFatura> compras = controller.listarComprasDaFatura(fatura.getIdFatura());
                for (CompraFatura compra : compras) {
                    modeloTabela.addRow(new Object[]{
                        compra.getDataCompra(),
                        compra.getDescricao(),
                        compra.getParcelas() != null ? compra.getParcelas() + "x" : "À vista",
                        "R$ " + compra.getValor()
                    });
                }
            } else {
                modeloTabela.addRow(new Object[]{"-", "Nenhuma fatura aberta encontrada", "-", "-"});
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar compras: " + ex.getMessage());
        }

        painelHistorico.add(new JScrollPane(tabelaFatura), BorderLayout.CENTER);

        // Unindo tudo na janela flutuante
        dialog.add(painelResumo, BorderLayout.NORTH);
        dialog.add(painelHistorico, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private JPanel criarPainelCartoes() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Título da aba
        JLabel lblTitulo = new JLabel("Meus Cartões de Crédito");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        mainPanel.add(lblTitulo, BorderLayout.NORTH);

        // Painel onde os cartões em formato de quadrado vão ficar lado a lado
        JPanel painelGridCartoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        painelGridCartoes.setBackground(Color.WHITE);

        try {
            // Busca a conta logada na sessão para listar os cartões vinculados a ela
            if (!Sessao.getInstance().getContasCliente().isEmpty()) {
                int idConta = Sessao.getInstance().getContasCliente().get(0).getIdConta();

                List<CartaoCredito> listaCartoes = controller.listarCartoesPorConta(idConta);

                if (listaCartoes != null && !listaCartoes.isEmpty()) {
                    for (CartaoCredito cartao : listaCartoes) {
                        // Adiciona o widget customizado para cada cartão encontrado
                        painelGridCartoes.add(criarWidgetCartao(cartao));
                    }
                } else {
                    JLabel lblAviso = new JLabel("Você não possui cartões ativos nesta conta.");
                    lblAviso.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    painelGridCartoes.add(lblAviso);
                }
            }
        } catch (Exception ex) {
            JLabel lblErro = new JLabel("Erro ao carregar cartões: " + ex.getMessage());
            lblErro.setForeground(Color.RED);
            painelGridCartoes.add(lblErro);
        }

        mainPanel.add(new JScrollPane(painelGridCartoes), BorderLayout.CENTER);
        return mainPanel;
    }

    private void carregarTabelaCartoes() {
        if (modelCartoes == null) return;
        try {
            modelCartoes.setRowCount(0);
            List<CartaoCredito> cartoes = controller.listarCartoesCliente(clienteLogado.getIdCliente());
            for (CartaoCredito c : cartoes) {
                modelCartoes.addRow(new Object[]{
                        c.getIdCartao(),
                        c.getNumeroCartao(),
                        c.getBandeira(),
                        FormatadorUtil.formatarMoeda(c.getLimiteTotal()),
                        FormatadorUtil.formatarMoeda(c.getLimiteUtilizado()),
                        c.getStatus(),
                        c.getTipo()
                });
            }
        } catch (Exception ex) {
            // ignora
        }
    }

    private void mostrarDetalhesFatura(int idFatura) throws Exception {
        FaturaCartao fatura = controller.buscarFaturaPorId(idFatura);
        if (fatura == null) throw new Exception("Fatura não encontrada");

        List<CompraFatura> compras = new CompraFaturaDAO().listarPorFatura(idFatura);

        JDialog dialog = new JDialog(this, "Detalhes da Fatura #" + idFatura, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridLayout(3, 2, 5, 5));
        top.add(new JLabel("Mês Referência:"));
        top.add(new JLabel(FormatadorUtil.formatarData(fatura.getMesReferencia())));
        top.add(new JLabel("Vencimento:"));
        top.add(new JLabel(FormatadorUtil.formatarData(fatura.getDataVencimento())));
        top.add(new JLabel("Total:"));
        top.add(new JLabel(FormatadorUtil.formatarMoeda(fatura.getValorTotal())));
        panel.add(top, BorderLayout.NORTH);

        DefaultTableModel modelCompras = new DefaultTableModel(
                new Object[]{"Data", "Descrição", "Valor", "Parcelas"}, 0);
        JTable tableCompras = new JTable(modelCompras);
        for (CompraFatura c : compras) {
            modelCompras.addRow(new Object[]{
                    FormatadorUtil.formatarData(c.getDataCompra()),
                    c.getDescricao(),
                    FormatadorUtil.formatarMoeda(c.getValor()),
                    c.getParcelas() != null ? c.getParcelas() + "x" : "À vista"
            });
        }
        JScrollPane scroll = new JScrollPane(tableCompras);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(btnFechar);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void carregarTabelaFaturas() {
        if (modelFaturas == null) return;
        try {
            modelFaturas.setRowCount(0);
            List<FaturaCartao> faturas = controller.listarFaturasCliente(clienteLogado.getIdCliente());
            for (FaturaCartao f : faturas) {
                modelFaturas.addRow(new Object[]{
                        f.getIdFatura(),
                        f.getIdCartao(),
                        FormatadorUtil.formatarData(f.getMesReferencia()),
                        FormatadorUtil.formatarMoeda(f.getValorTotal()),
                        FormatadorUtil.formatarMoeda(f.getValorPago()),
                        FormatadorUtil.formatarData(f.getDataVencimento()),
                        f.getStatus()
                });
            }
        } catch (Exception ex) {
            // ignora
        }
    }

    private void solicitarCartao() {
        try {
            if (contas.isEmpty()) throw new Exception("Você não possui contas.");
            Conta conta = contas.get(0);
            String limiteStr = JOptionPane.showInputDialog(this, "Limite desejado:");
            BigDecimal limite = new BigDecimal(limiteStr.replace(",", "."));
            String bandeira = (String) JOptionPane.showInputDialog(this, "Bandeira:", "Visa",
                    JOptionPane.QUESTION_MESSAGE, null, new String[]{"Visa", "Mastercard", "Elo"}, "Visa");
            controller.solicitarCartaoPessoal(conta.getIdConta(), limite, bandeira);
            JOptionPane.showMessageDialog(this, "Cartão solicitado com sucesso!");
            carregarTabelaCartoes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void solicitarCartaoCorporativo() {
        try {
            if (contas.isEmpty()) throw new Exception("Você não possui contas.");
            Conta conta = contas.get(0);
            String limiteStr = JOptionPane.showInputDialog(this, "Limite desejado:");
            BigDecimal limite = new BigDecimal(limiteStr.replace(",", "."));
            String bandeira = (String) JOptionPane.showInputDialog(this, "Bandeira:", "Visa",
                    JOptionPane.QUESTION_MESSAGE, null, new String[]{"Visa", "Mastercard", "Elo"}, "Visa");
            String idPortadorStr = JOptionPane.showInputDialog(this, "ID do portador (cliente):");
            int idPortador = Integer.parseInt(idPortadorStr);
            controller.solicitarCartaoCorporativo(conta.getIdConta(), limite, bandeira, idPortador);
            JOptionPane.showMessageDialog(this, "Cartão corporativo solicitado com sucesso!");
            carregarTabelaCartoes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bloquearDesbloquearCartao() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "ID do cartão:");
            int id = Integer.parseInt(idStr);
            CartaoCredito cartao = null;
            for (CartaoCredito c : controller.listarCartoesCliente(clienteLogado.getIdCliente())) {
                if (c.getIdCartao().equals(id)) {
                    cartao = c;
                    break;
                }
            }
            if (cartao == null) throw new Exception("Cartão não encontrado.");
            if ("ATIVO".equals(cartao.getStatus())) {
                controller.bloquearCartao(id);
                JOptionPane.showMessageDialog(this, "Cartão bloqueado.");
            } else {
                controller.desbloquearCartao(id);
                JOptionPane.showMessageDialog(this, "Cartão desbloqueado.");
            }
            carregarTabelaCartoes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== AGENDAMENTOS ==================
    private JPanel criarPainelAgendamentos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelAgendamentos = new DefaultTableModel(new Object[]{"ID", "Conta Origem", "Tipo", "Valor", "Data Agendada", "Status"}, 0);
        tableAgendamentos = new JTable(modelAgendamentos);
        JScrollPane scroll = new JScrollPane(tableAgendamentos);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnCarregar = new JButton("Carregar Agendamentos");
        btnCarregar.addActionListener(e -> carregarTabelaAgendamentos());
        panel.add(btnCarregar, BorderLayout.SOUTH);

        carregarTabelaAgendamentos();
        return panel;
    }

    private void carregarTabelaAgendamentos() {
        if (modelAgendamentos == null) return;
        try {
            modelAgendamentos.setRowCount(0);
            List<AgendamentoTransferencia> lista = controller.listarAgendamentosCliente(clienteLogado.getIdCliente());
            for (AgendamentoTransferencia a : lista) {
                modelAgendamentos.addRow(new Object[]{
                        a.getIdAgendamento(),
                        a.getIdContaOrigem(),
                        a.getTipoTransferencia(),
                        FormatadorUtil.formatarMoeda(a.getValor()),
                        FormatadorUtil.formatarData(a.getDataAgendada()),
                        a.getStatus()
                });
            }
        } catch (Exception ex) {
            // ignora
        }
    }

    // ================== BOLETOS (PJ) ==================
    private JPanel criarPainelBoletos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        top.add(new JLabel("Emissão de Boleto"), gbc);
        y++;

        gbc.gridwidth = 1;
        gbc.gridy = y; gbc.gridx = 0;
        top.add(new JLabel("Valor:"), gbc);
        JTextField txtValorBoleto = new JTextField(10);
        gbc.gridx = 1;
        top.add(txtValorBoleto, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        top.add(new JLabel("Vencimento (dd/mm/aaaa):"), gbc);
        JTextField txtVencBoleto = new JTextField(10);
        gbc.gridx = 1;
        top.add(txtVencBoleto, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        top.add(new JLabel("Juros (% dia):"), gbc);
        JTextField txtJurosBoleto = new JTextField(5);
        gbc.gridx = 1;
        top.add(txtJurosBoleto, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        top.add(new JLabel("Multa (%):"), gbc);
        JTextField txtMultaBoleto = new JTextField(5);
        gbc.gridx = 1;
        top.add(txtMultaBoleto, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        top.add(new JLabel("Instruções:"), gbc);
        JTextField txtInstBoleto = new JTextField(20);
        gbc.gridx = 1;
        top.add(txtInstBoleto, gbc);
        y++;

        JButton btnEmitir = new JButton("Emitir Boleto");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnEmitir.addActionListener(e -> {
            try {
                BigDecimal valor = new BigDecimal(txtValorBoleto.getText().replace(",", "."));
                LocalDate venc = FormatadorUtil.parseData(txtVencBoleto.getText());
                BigDecimal juros = new BigDecimal(txtJurosBoleto.getText().replace(",", "."));
                BigDecimal multa = new BigDecimal(txtMultaBoleto.getText().replace(",", "."));
                String instrucoes = txtInstBoleto.getText().trim();
                BoletoEmitido boleto = controller.emitirBoleto(clienteLogado.getIdCliente(), valor, venc, juros, multa, instrucoes);
                JOptionPane.showMessageDialog(this, "Boleto emitido!\nNosso número: " + boleto.getNossoNumero() +
                        "\nCódigo de barras: " + boleto.getCodigoBarras());
                carregarTabelaBoletos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        top.add(btnEmitir, gbc);

        JButton btnCancelar = new JButton("Cancelar Boleto");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnCancelar.addActionListener(e -> {
            try {
                String idStr = JOptionPane.showInputDialog(this, "ID do boleto para cancelar:");
                int id = Integer.parseInt(idStr);
                controller.cancelarBoleto(id);
                JOptionPane.showMessageDialog(this, "Boleto cancelado.");
                carregarTabelaBoletos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        top.add(btnCancelar, gbc);

        panel.add(top, BorderLayout.NORTH);

        modelBoletos = new DefaultTableModel(new Object[]{"ID", "Nosso Número", "Valor", "Vencimento", "Status", "Código Barras"}, 0);
        tableBoletos = new JTable(modelBoletos);
        JScrollPane scroll = new JScrollPane(tableBoletos);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnCarregar = new JButton("Carregar Boletos");
        btnCarregar.addActionListener(e -> carregarTabelaBoletos());
        panel.add(btnCarregar, BorderLayout.SOUTH);

        carregarTabelaBoletos();
        return panel;
    }

    private void carregarTabelaBoletos() {
        if (modelBoletos == null) return;
        try {
            modelBoletos.setRowCount(0);
            List<BoletoEmitido> lista = controller.listarBoletosCliente(clienteLogado.getIdCliente());
            for (BoletoEmitido b : lista) {
                modelBoletos.addRow(new Object[]{
                        b.getIdBoleto(),
                        b.getNossoNumero(),
                        FormatadorUtil.formatarMoeda(b.getValor()),
                        FormatadorUtil.formatarData(b.getDataVencimento()),
                        b.getStatus(),
                        b.getCodigoBarras()
                });
            }
        } catch (Exception ex) {
            // ignora
        }
    }

    // ================== PAGAMENTOS EM LOTE (PJ) ==================
    private JPanel criarPainelLote() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        top.add(new JLabel("Pagamentos em Lote"), gbc);
        y++;

        gbc.gridwidth = 1;
        gbc.gridy = y; gbc.gridx = 0;
        top.add(new JLabel("Tipo:"), gbc);
        JComboBox<String> cmbTipoLote = new JComboBox<>(new String[]{"FORNECEDORES", "FOLHA"});
        gbc.gridx = 1;
        top.add(cmbTipoLote, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        top.add(new JLabel("Data Execução (dd/mm/aaaa):"), gbc);
        JTextField txtDataLote = new JTextField(10);
        gbc.gridx = 1;
        top.add(txtDataLote, gbc);
        y++;

        JButton btnCriarLote = new JButton("Criar Lote");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnCriarLote.addActionListener(e -> {
            try {
                String tipo = (String) cmbTipoLote.getSelectedItem();
                LocalDate data = FormatadorUtil.parseData(txtDataLote.getText());
                int idLote = controller.criarLote(clienteLogado.getIdCliente(), tipo, data);
                JOptionPane.showMessageDialog(this, "Lote criado! ID: " + idLote);
                carregarTabelaLotes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        top.add(btnCriarLote, gbc);
        y++;

        JPanel botoesLote = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnAdicionarItem = new JButton("Adicionar Item");
        btnAdicionarItem.addActionListener(e -> adicionarItemLote());
        botoesLote.add(btnAdicionarItem);

        JButton btnExecutarLote = new JButton("Executar Lote");
        btnExecutarLote.addActionListener(e -> executarLote());
        botoesLote.add(btnExecutarLote);

        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        top.add(botoesLote, gbc);
        y++;

        panel.add(top, BorderLayout.NORTH);

        modelLotes = new DefaultTableModel(new Object[]{"ID", "Tipo", "Valor Total", "Data Execução", "Status"}, 0);
        tableLotes = new JTable(modelLotes);
        JScrollPane scroll = new JScrollPane(tableLotes);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnCarregar = new JButton("Carregar Lotes");
        btnCarregar.addActionListener(e -> carregarTabelaLotes());
        panel.add(btnCarregar, BorderLayout.SOUTH);

        carregarTabelaLotes();
        return panel;
    }

    private void adicionarItemLote() {
        try {
            String idLoteStr = JOptionPane.showInputDialog(this, "ID do Lote:");
            int idLote = Integer.parseInt(idLoteStr);
            String beneficiario = JOptionPane.showInputDialog(this, "Beneficiário:");
            String chavePix = JOptionPane.showInputDialog(this, "Chave PIX (ou deixe vazio):");
            String dadosTedJson = JOptionPane.showInputDialog(this, "Dados TED (JSON) (ou deixe vazio):");
            String valorStr = JOptionPane.showInputDialog(this, "Valor:");
            BigDecimal valor = new BigDecimal(valorStr.replace(",", "."));
            controller.adicionarItemLote(idLote, beneficiario, chavePix.isEmpty() ? null : chavePix,
                    dadosTedJson.isEmpty() ? null : dadosTedJson, valor);
            JOptionPane.showMessageDialog(this, "Item adicionado!");
            carregarTabelaLotes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executarLote() {
        try {
            String idLoteStr = JOptionPane.showInputDialog(this, "ID do Lote para executar:");
            int idLote = Integer.parseInt(idLoteStr);
            controller.executarLote(idLote);
            JOptionPane.showMessageDialog(this, "Lote executado com sucesso!");
            carregarTabelaLotes();
            carregarSaldo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTabelaLotes() {
        if (modelLotes == null) return;
        try {
            modelLotes.setRowCount(0);
            List<PagamentoLote> lista = controller.listarLotesCliente(clienteLogado.getIdCliente());
            for (PagamentoLote l : lista) {
                modelLotes.addRow(new Object[]{
                        l.getIdLote(),
                        l.getTipo(),
                        FormatadorUtil.formatarMoeda(l.getValorTotal()),
                        l.getDataExecucao() != null ? FormatadorUtil.formatarData(l.getDataExecucao()) : "",
                        l.getStatus()
                });
            }
        } catch (Exception ex) {
            // ignora
        }
    }

    // ================== CONFIGURAÇÕES ==================
    private JPanel criarPainelConfiguracoes() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Preferências de Alertas"), gbc);
        y++;

        gbc.gridwidth = 1;
        JCheckBox chkSaldoBaixo = new JCheckBox("Alertar saldo baixo");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(chkSaldoBaixo, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Limite saldo:"), gbc);
        JTextField txtLimiteSaldo = new JTextField(10);
        gbc.gridx = 1;
        panel.add(txtLimiteSaldo, gbc);
        y++;

        JCheckBox chkVencimento = new JCheckBox("Alertar vencimento de contas");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(chkVencimento, gbc);
        y++;

        JCheckBox chkExtrato = new JCheckBox("Alertar extrato disponível");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(chkExtrato, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Canais de notificação:"), gbc);
        y++;
        JCheckBox chkEmail = new JCheckBox("E-mail");
        JCheckBox chkSms = new JCheckBox("SMS");
        JCheckBox chkPush = new JCheckBox("Push");
        JPanel canais = new JPanel(new FlowLayout(FlowLayout.LEFT));
        canais.add(chkEmail);
        canais.add(chkSms);
        canais.add(chkPush);
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(canais, gbc);
        y++;

        JButton btnSalvarConfig = new JButton("Salvar Preferências");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnSalvarConfig.addActionListener(e -> {
            try {
                PreferenciaAlertas pref = new PreferenciaAlertas(clienteLogado.getIdCliente());
                pref.setAlertaSaldoBaixo(chkSaldoBaixo.isSelected());
                if (chkSaldoBaixo.isSelected() && !txtLimiteSaldo.getText().isEmpty()) {
                    pref.setValorLimiteSaldo(new BigDecimal(txtLimiteSaldo.getText().replace(",", ".")));
                }
                pref.setAlertaVencimentoConta(chkVencimento.isSelected());
                pref.setAlertaExtratoDisponivel(chkExtrato.isSelected());
                pref.setCanalEmail(chkEmail.isSelected());
                pref.setCanalSms(chkSms.isSelected());
                pref.setCanalPush(chkPush.isSelected());
                controller.configurarAlertas(pref);
                JOptionPane.showMessageDialog(this, "Preferências salvas!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnSalvarConfig, gbc);

        try {
            PreferenciaAlertas pref = controller.buscarAlertas(clienteLogado.getIdCliente());
            if (pref != null) {
                chkSaldoBaixo.setSelected(pref.getAlertaSaldoBaixo());
                if (pref.getValorLimiteSaldo() != null)
                    txtLimiteSaldo.setText(pref.getValorLimiteSaldo().toString());
                chkVencimento.setSelected(pref.getAlertaVencimentoConta());
                chkExtrato.setSelected(pref.getAlertaExtratoDisponivel());
                chkEmail.setSelected(pref.getCanalEmail());
                chkSms.setSelected(pref.getCanalSms());
                chkPush.setSelected(pref.getCanalPush());
            }
        } catch (Exception ex) {
            // ignora
        }

        return panel;
    }

    private void logout() {
        Sessao.getInstance().limpar();
        new TelaLogin().setVisible(true);
        dispose();
    }
}