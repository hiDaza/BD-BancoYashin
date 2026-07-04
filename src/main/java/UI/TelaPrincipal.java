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
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.EmprestimoSolicitacao;
import com.mycompany.yashin.model.FaturaCartao;
import com.mycompany.yashin.model.PagamentoLote;
import com.mycompany.yashin.model.PreferenciaAlertas;
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.TipoPessoa;
import controller.BancoController;
import controller.Sessao;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import util.CatalogoBancos;
import util.FormatadorUtil;

public class TelaPrincipal extends JFrame {
    private BancoController controller = new BancoController();
    private Cliente clienteLogado;
    private List<Conta> contas;

    private JTabbedPane tabbedPane;

    // Componentes principais (serão inicializados nos métodos)
    private JComboBox<Conta> cmbContaSaldo, cmbContaOrigem, cmbContaDestino, cmbContaPagamento;
    private JLabel lblSaldo;
    private JTextArea txtExtrato, txtResultadoEmprestimo;
    private JTable tableCartoes, tableFaturas, tableBoletos, tableLotes, tableAgendamentos;
    private DefaultTableModel modelCartoes, modelFaturas, modelBoletos, modelLotes, modelAgendamentos;
    private JComboBox<String> cmbAgenciaTED;

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
        DefaultComboBoxModel<Conta> model = new DefaultComboBoxModel<>();
        for (Conta c : contas) {
            model.addElement(c);
        }
        if (cmbContaSaldo != null) cmbContaSaldo.setModel(model);
        if (cmbContaOrigem != null) cmbContaOrigem.setModel(model);
        if (cmbContaDestino != null) cmbContaDestino.setModel(model);
        if (cmbContaPagamento != null) cmbContaPagamento.setModel(model);
    }

    // ================== SALDO/EXTRATO ==================
    private JPanel criarPainelSaldoExtrato() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Conta:"));
        cmbContaSaldo = new JComboBox<>();
        cmbContaSaldo.addActionListener(e -> carregarSaldo());
        top.add(cmbContaSaldo);
        JButton btnAtualizar = new JButton("Atualizar Saldo");
        btnAtualizar.addActionListener(e -> carregarSaldo());
        top.add(btnAtualizar);
        lblSaldo = new JLabel("R$ 0,00");
        lblSaldo.setFont(new Font("Arial", Font.BOLD, 20));
        top.add(lblSaldo);
        panel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(new JLabel("Extrato (últimos 30 dias):"), BorderLayout.NORTH);
        txtExtrato = new JTextArea(20, 60);
        txtExtrato.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtExtrato);
        center.add(scroll, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JButton btnExtrato = new JButton("Carregar Extrato");
        btnExtrato.addActionListener(e -> carregarExtrato());
        JButton btnExtratoPDF = new JButton("Gerar Extrato PDF");
        btnExtratoPDF.addActionListener(e -> gerarExtratoPDF());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnExtrato);
        botoes.add(btnExtratoPDF);
        panel.add(botoes, BorderLayout.SOUTH);

        return panel;
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

        // Conteúdo interno com GridBagLayout
        JPanel conteudo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        int linha = 0;

        // Título
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel titulo = new JLabel("Transferências");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        conteudo.add(titulo, gbc);
        linha++;

        // -- Transferência Interna --
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

        // -- Separador --
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // -- TED --
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

        // Agora use a variável de instância
        cmbAgenciaTED = new JComboBox<>();  // inicializa a variável da classe

        // Listener para autocompletar nome do banco e popular agências
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
        // Já temos cmbAgenciaTED como variável de instância, usamos ela
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


        // -- Separador --
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // -- PIX --
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

        // -- Separador --
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // -- Agendamento --
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

        // Conteúdo interno com GridBagLayout
        JPanel conteudo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        
        /*
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        */
        int linha = 0;

        // Título
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel titulo = new JLabel("Pagamentos");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        conteudo.add(titulo, gbc);
        linha++;

        // -- Pagar Conta --
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

        // -- Separador --
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // -- Pagar Fatura Cartão --
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

        // -- Separador --
        gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
        conteudo.add(new JSeparator(), gbc);
        linha++;

        // -- PIX QR Code --
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

            // Histórico de solicitações
            gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
            panel.add(new JLabel("Histórico de Solicitações:"), gbc);
            linha++;

            // Tabela de histórico com colunas (ID, Valor, Prazo, Status, Data)
            DefaultTableModel modelHistorico = new DefaultTableModel(
                    new Object[]{"ID", "Valor", "Prazo", "Status", "Data"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable tableHistorico = new JTable(modelHistorico);
            tableHistorico.setRowHeight(25);
            // Adicionar listener de duplo clique
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

            // Botão para carregar histórico e botão "Ver Detalhes" (opcional, mas temos duplo clique)
            JPanel bottomHistorico = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnCarregarHistorico = new JButton("Carregar Histórico");
            btnCarregarHistorico.addActionListener(e -> {
                try {
                    modelHistorico.setRowCount(0);
                    List<EmprestimoSolicitacao> lista = controller.listarEmprestimosCliente(clienteLogado.getIdCliente());
                    for (EmprestimoSolicitacao s : lista) {
                        modelHistorico.addRow(new Object[]{
                                s.getIdSolicitacao(),
                                FormatadorUtil.formatarMoeda(s.getValorSolicitado()),
                                s.getPrazoMeses(),
                                s.getStatus(),
                                FormatadorUtil.formatarDataHora(s.getDataSolicitacao())
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });
            bottomHistorico.add(btnCarregarHistorico);

            JButton btnVerDetalhes = new JButton("Ver Detalhes (duplo clique)");
            btnVerDetalhes.setEnabled(false);
            bottomHistorico.add(btnVerDetalhes);

            gbc.gridy = linha; gbc.gridx = 0; gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
            panel.add(bottomHistorico, gbc);

            // Ação do botão Simular
            btnSimular.addActionListener(e -> {
                try {
                    BigDecimal valor = new BigDecimal(txtValorEmprestimo.getText().replace(",", "."));
                    int prazo = Integer.parseInt(txtPrazoEmprestimo.getText());
                    EmprestimoSolicitacao sim = controller.simularEmprestimo(clienteLogado.getIdCliente(), valor, prazo);
                    // Exibe apenas simulação
                    exibirDetalhesEmprestimo(sim, "SIMULAÇÃO");
                    // Exibe resumo rápido
                    txtResultadoEmprestimo.setText("Simulação: Parcela = " + FormatadorUtil.formatarMoeda(sim.getValorParcela()) +
                            " | Total = " + FormatadorUtil.formatarMoeda(sim.getValorParcela().multiply(BigDecimal.valueOf(sim.getNumeroParcelas()))));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Ação do botão Solicitar
            btnSolicitar.addActionListener(e -> {
                try {
                    BigDecimal valor = new BigDecimal(txtValorEmprestimo.getText().replace(",", "."));
                    int prazo = Integer.parseInt(txtPrazoEmprestimo.getText());
                    String finalidade = txtFinalidade.getText().trim();
                    EmprestimoSolicitacao solicitacao = controller.solicitarEmprestimo(clienteLogado.getIdCliente(), valor, prazo, finalidade);
                    exibirDetalhesEmprestimo(solicitacao, "SOLICITAÇÃO");
                    if (solicitacao.getStatus().toString().equals("APROVADO")) {
                        carregarSaldo();
                    }
                    btnCarregarHistorico.doClick();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Carregar histórico automaticamente ao abrir
            btnCarregarHistorico.doClick();

            return panel;
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

        // Dados gerais
        addLinha(panel, "Valor Solicitado:", FormatadorUtil.formatarMoeda(obj.getValorSolicitado()), normalFont);
        addLinha(panel, "Prazo (meses):", String.valueOf(obj.getPrazoMeses()), normalFont);
        addLinha(panel, "Taxa de Juros:", (obj.getTaxaJuros() != null ? obj.getTaxaJuros() : "N/A") + "% ao mês", normalFont);

        // Cálculos financeiros - com verificação de nulo
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

            if (obj.getStatus().toString().equals("ANALISE_MANUAL")) {
                panel.add(Box.createVerticalStrut(10));
                JLabel lblManual = new JLabel("Solicitação em Análise Manual");
                lblManual.setFont(tituloFont);
                lblManual.setForeground(Color.ORANGE);
                lblManual.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(lblManual);
                panel.add(Box.createVerticalStrut(5));
                JLabel lblAguarde = new JLabel("Aguarde o contato de um de nossos funcionários.");
                lblAguarde.setFont(normalFont);
                lblAguarde.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(lblAguarde);
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
    private JPanel criarPainelCartoes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSolicitarCartao = new JButton("Solicitar Cartão Pessoal");
        btnSolicitarCartao.addActionListener(e -> solicitarCartao());
        top.add(btnSolicitarCartao);

        JButton btnSolicitarCorporativo = new JButton("Solicitar Cartão Corporativo");
        btnSolicitarCorporativo.addActionListener(e -> solicitarCartaoCorporativo());
        top.add(btnSolicitarCorporativo);

        JButton btnBloquear = new JButton("Bloquear/Desbloquear");
        btnBloquear.addActionListener(e -> bloquearDesbloquearCartao());
        top.add(btnBloquear);

        panel.add(top, BorderLayout.NORTH);

        // Tabela de cartões
        modelCartoes = new DefaultTableModel(new Object[]{"ID", "Número", "Bandeira", "Limite", "Utilizado", "Status", "Tipo"}, 0);
        tableCartoes = new JTable(modelCartoes);
        JScrollPane scrollCartoes = new JScrollPane(tableCartoes);
        panel.add(scrollCartoes, BorderLayout.CENTER);

        // Tabela de faturas
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("Faturas"));
        modelFaturas = new DefaultTableModel(new Object[]{"ID Fatura", "Cartão", "Mês", "Valor Total", "Valor Pago", "Vencimento", "Status"}, 0);
        tableFaturas = new JTable(modelFaturas);
        JScrollPane scrollFaturas = new JScrollPane(tableFaturas);
        bottom.add(scrollFaturas, BorderLayout.CENTER);
        JButton btnCarregarFaturas = new JButton("Carregar Faturas");
        btnCarregarFaturas.addActionListener(e -> carregarTabelaFaturas());
        bottom.add(btnCarregarFaturas, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        // Carregar dados
        carregarTabelaCartoes();
        carregarTabelaFaturas();

        return panel;
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
            // Buscar cartão
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

        // Tabela de boletos
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

        // Tabela de lotes
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

        // Carregar preferências atuais
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