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
import com.mycompany.yashin.model.enums.StatusConta;
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
import util.FormatadorUtil;
import javax.swing.SwingWorker;
import util.CriptografiaUtil;

public class TelaPrincipal extends JFrame {
    private BancoController controller = new BancoController();
    private Cliente clienteLogado;
    private List<Conta> contas;

    private JTabbedPane tabbedPane;

    // Componentes principais
    private JComboBox<Conta> cmbContaSaldo = new JComboBox<>(), cmbContaOrigem, cmbContaDestino, cmbContaPagamento = new JComboBox<>();
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
        tabbedPane.addTab("Nova Conta", criarPainelNovaConta());
        

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


    // ================== SALDO/EXTRATO ==================
    private JPanel criarPainelSaldoExtrato() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ==========================================
        // SELETOR GLOBAL DE CONTA (Topo)
        // ==========================================
        JPanel painelTopoConta = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        painelTopoConta.setBackground(new Color(248, 249, 250));
        painelTopoConta.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 233, 240)));

        JLabel lblSelecionarConta = new JLabel("Visualizar Conta:");
        lblSelecionarConta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        painelTopoConta.add(lblSelecionarConta);

        JComboBox<com.mycompany.yashin.model.Conta> cmbContaVisualizar = new JComboBox<>();
        cmbContaVisualizar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbContaVisualizar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof com.mycompany.yashin.model.Conta) {
                    com.mycompany.yashin.model.Conta c = (com.mycompany.yashin.model.Conta) value;
                    setText("Agência: " + c.getAgencia() + " | Conta: " + c.getNumeroConta() + " (" + c.getTipoConta() + ")");
                }
                return this;
            }
        });

        for (com.mycompany.yashin.model.Conta c : Sessao.getInstance().getContasCliente()) {
            cmbContaVisualizar.addItem(c);
        }
        painelTopoConta.add(cmbContaVisualizar);
        mainPanel.add(painelTopoConta, BorderLayout.NORTH);

        // ==========================================
        // CORPO DA TELA (Painel de Exibição de Dados)
        // ==========================================
        JPanel painelConteudo = new JPanel(new BorderLayout(0, 15));
        painelConteudo.setBackground(Color.WHITE);
        painelConteudo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Bloco do Saldo
        JPanel cardSaldo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        cardSaldo.setBackground(new Color(108, 92, 231));
        JLabel lblTextoSaldo = new JLabel("Saldo Disponível: ");
        lblTextoSaldo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTextoSaldo.setForeground(Color.WHITE);
        JLabel lblValorSaldo = new JLabel("R$ 0,00");
        lblValorSaldo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValorSaldo.setForeground(Color.WHITE);
        cardSaldo.add(lblTextoSaldo);
        cardSaldo.add(lblValorSaldo);
        painelConteudo.add(cardSaldo, BorderLayout.NORTH);

        // Tabela de Extrato
        String[] colunasExtrato = {"Data/Hora", "Tipo", "Descrição", "Valor", "Status"};
        DefaultTableModel modeloExtrato = new DefaultTableModel(colunasExtrato, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabelaExtrato = new JTable(modeloExtrato);
        tabelaExtrato.setRowHeight(28);
        tabelaExtrato.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaExtrato.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollExtrato = new JScrollPane(tabelaExtrato);
        scrollExtrato.getViewport().setBackground(Color.WHITE);
        painelConteudo.add(scrollExtrato, BorderLayout.CENTER);

        mainPanel.add(painelConteudo, BorderLayout.CENTER);

        // ==========================================
        // LÓGICA DE ATUALIZAÇÃO DINÂMICA
        // ==========================================
        Runnable atualizarDadosTela = () -> {
            modeloExtrato.setRowCount(0);
            com.mycompany.yashin.model.Conta contaSelecionada = (com.mycompany.yashin.model.Conta) cmbContaVisualizar.getSelectedItem();

            if (contaSelecionada != null) {
                java.text.NumberFormat fmtMoeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
                lblValorSaldo.setText(fmtMoeda.format(contaSelecionada.getSaldo()));

                try {
                    // CORRIGIDO: Passando os 3 parâmetros exigidos pela sua BancoController (Últimos 30 dias)
                    java.time.LocalDateTime dataFim = java.time.LocalDateTime.now();
                    java.time.LocalDateTime dataInicio = dataFim.minusDays(30);

                    java.util.List<com.mycompany.yashin.model.Transacao> transacoes = this.controller.visualizarExtrato(contaSelecionada.getIdConta(), dataInicio, dataFim);

                    if (transacoes != null && !transacoes.isEmpty()) {
                        java.time.format.DateTimeFormatter fmtData = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        for (com.mycompany.yashin.model.Transacao t : transacoes) {
                            modeloExtrato.addRow(new Object[]{
                                t.getDataHora() != null ? t.getDataHora().format(fmtData) : "-",
                                t.getTipoTransacao(),
                                t.getDescricao(),
                                fmtMoeda.format(t.getValor()),
                                t.getStatus()
                            });
                        }
                    } else {
                        modeloExtrato.addRow(new Object[]{"-", "Nenhuma transação encontrada nos últimos 30 dias.", "-", "-", "-"});
                    }
                } catch (Exception ex) {
                    modeloExtrato.addRow(new Object[]{"Erro", "Falha ao carregar extrato", ex.getMessage(), "-", "-"});
                }
            } else {
                lblValorSaldo.setText("R$ 0,00");
                modeloExtrato.addRow(new Object[]{"-", "Nenhuma conta selecionada", "-", "-", "-"});
            }
        };

        cmbContaVisualizar.addActionListener(e -> atualizarDadosTela.run());
        atualizarDadosTela.run();

        return mainPanel;
    }

    private JPanel criarPainelNovaConta() {
            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblTitulo = new JLabel("Abrir Nova Conta Bancária");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            mainPanel.add(lblTitulo, gbc);

            JLabel lblTipo = new JLabel("Tipo de Conta:");
            lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
            mainPanel.add(lblTipo, gbc);

            // Usa o Enum do seu próprio modelo do projeto
            JComboBox<com.mycompany.yashin.model.enums.TipoConta> cmbTipoConta = new JComboBox<>(com.mycompany.yashin.model.enums.TipoConta.values());
            cmbTipoConta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            gbc.gridx = 1;
            mainPanel.add(cmbTipoConta, gbc);

            JLabel lblAgencia = new JLabel("Agência:");
            lblAgencia.setFont(new Font("Segoe UI", Font.BOLD, 13));
            gbc.gridx = 0; gbc.gridy = 2;
            mainPanel.add(lblAgencia, gbc);

            // Usa os dados existentes nas suas agências ou uma agência padrão do seu projeto
            JTextField txtAgencia = new JTextField("0001");
            txtAgencia.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            gbc.gridx = 1;
            mainPanel.add(txtAgencia, gbc);

            // Autofill baseado nas suas contas da sessão
            if (!contas.isEmpty()) {
                txtAgencia.setText(contas.get(0).getAgencia());
            }

            JButton btnCriarConta = new JButton("Solicitar e Ativar Conta ➔");
            btnCriarConta.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnCriarConta.setBackground(new Color(46, 204, 113)); // Verde moderno
            btnCriarConta.setForeground(Color.WHITE);
            btnCriarConta.setFocusPainted(false);
            btnCriarConta.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCriarConta.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            mainPanel.add(btnCriarConta, gbc);

            btnCriarConta.addActionListener(e -> {
                try {
                    String tipoSelecionadoStr = cmbTipoConta.getSelectedItem().toString();
                    String agenciaDigitada = txtAgencia.getText().trim();

                    if (agenciaDigitada.isEmpty()) {
                        JOptionPane.showMessageDialog(mainPanel, "Informe o número da agência.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Chama rigorosamente o seu método da controller
                    com.mycompany.yashin.model.Conta novaConta = this.controller.abrirNovaConta(clienteLogado.getIdCliente(), tipoSelecionadoStr, agenciaDigitada);

                    if (novaConta != null) {
                        JOptionPane.showMessageDialog(mainPanel, "Nova conta criada com sucesso!\nNúmero: " + novaConta.getNumeroConta() + "\n" + "Reinicie Seu Aplicativo Para Ter Acesso a Nova Conta", "Sucesso!!", JOptionPane.INFORMATION_MESSAGE);

                        // RECARGA: Busca os dados atualizados do banco usando sua lógica original para sincronizar os componentes
                        contas = controller.buscarContasPorCliente(clienteLogado.getIdCliente());
                        Sessao.getInstance().setContasCliente(contas);
                        atualizarCombosContas();

                        // Atualiza também a própria tela de Saldo atual trocando de aba para atualizar os dados visuais
                        tabbedPane.setSelectedIndex(0);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Erro ao abrir nova conta: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

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
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ==========================================
        // SELETOR GLOBAL DE CONTA ORIGEM
        // ==========================================
        JPanel painelTopoConta = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        painelTopoConta.setBackground(new Color(248, 249, 250));
        painelTopoConta.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 233, 240)));

        JLabel lblSelecionarConta = new JLabel("Selecione a conta de origem:");
        lblSelecionarConta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        painelTopoConta.add(lblSelecionarConta);

        JComboBox<Conta> cmbContaOrigem = new JComboBox<>();
        cmbContaOrigem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbContaOrigem.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Conta) {
                    Conta c = (Conta) value;
                    setText("Agência: " + c.getAgencia() + " | Conta: " + c.getNumeroConta());
                }
                return this;
            }
        });

        for (Conta c : Sessao.getInstance().getContasCliente()) {
            cmbContaOrigem.addItem(c);
        }
        painelTopoConta.add(cmbContaOrigem);
        mainPanel.add(painelTopoConta, BorderLayout.NORTH);

        // Abas Internas
        JTabbedPane abasInternas = new JTabbedPane();
        abasInternas.setFont(new Font("Segoe UI", Font.BOLD, 13));
        abasInternas.setBackground(Color.WHITE);

        // ==========================================
        // ABA 1: FORMULÁRIO DE ENVIO / AGENDAMENTO
        // ==========================================
        JPanel painelEnviar = new JPanel(new GridBagLayout());
        painelEnviar.setBackground(Color.WHITE);
        painelEnviar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Tipo de Transferência
        JLabel lblTipo = new JLabel("Modalidade:");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0;
        painelEnviar.add(lblTipo, gbc);

        String[] tiposTransf = {"PIX (Instantâneo)", "Transferência Interna (Mesmo Banco)", "TED / DOC (Tradicional)"};
        JComboBox<String> cmbTipoTransf = new JComboBox<>(tiposTransf);
        cmbTipoTransf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        painelEnviar.add(cmbTipoTransf, gbc);

        // 2. Painel Dinâmico de Destinatário
        JPanel painelDadosDestino = new JPanel(new CardLayout());
        painelDadosDestino.setBackground(Color.WHITE);

        // PIX
        JPanel pnlPix = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlPix.setBackground(Color.WHITE);
        JLabel lblPix = new JLabel("Chave PIX (CPF/Email/Celular):");
        lblPix.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTextField txtPixDestino = new JTextField();
        txtPixDestino.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlPix.add(lblPix);
        pnlPix.add(txtPixDestino);

        // INTERNA - usando JComboBox com as contas disponíveis
        JPanel pnlInterna = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlInterna.setBackground(Color.WHITE);
        JLabel lblInterna = new JLabel("Conta Destino:");
        lblInterna.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComboBox<Conta> cmbContaDestinoInterna = new JComboBox<>();
        cmbContaDestinoInterna.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbContaDestinoInterna.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Conta) {
                    Conta c = (Conta) value;
                    setText("Ag: " + c.getAgencia() + " | Conta: " + c.getNumeroConta());
                }
                return this;
            }
        });
        // Preenche com as contas do usuário (exceto a origem – atualizado depois)
        pnlInterna.add(lblInterna);
        pnlInterna.add(cmbContaDestinoInterna);

        // TED
        JPanel pnlTed = new JPanel(new GridLayout(5, 2, 10, 6));
        pnlTed.setBackground(Color.WHITE);
        JLabel lblTedBanco = new JLabel("Banco (Código):");
        lblTedBanco.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTextField txtTedBanco = new JTextField();
        JLabel lblTedAgencia = new JLabel("Agência:");
        lblTedAgencia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTextField txtTedAgencia = new JTextField();
        JLabel lblTedConta = new JLabel("Número da Conta:");
        lblTedConta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTextField txtTedConta = new JTextField();
        JLabel lblTedDigito = new JLabel("Dígito:");
        lblTedDigito.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTextField txtTedDigito = new JTextField();
        JLabel lblTedCpf = new JLabel("CPF/CNPJ do Favorecido:");
        lblTedCpf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTextField txtTedCpf = new JTextField();

        pnlTed.add(lblTedBanco); pnlTed.add(txtTedBanco);
        pnlTed.add(lblTedAgencia); pnlTed.add(txtTedAgencia);
        pnlTed.add(lblTedConta); pnlTed.add(txtTedConta);
        pnlTed.add(lblTedDigito); pnlTed.add(txtTedDigito);
        pnlTed.add(lblTedCpf); pnlTed.add(txtTedCpf);

        painelDadosDestino.add(pnlPix, "PIX");
        painelDadosDestino.add(pnlInterna, "INTERNA");
        painelDadosDestino.add(pnlTed, "TED");

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        painelEnviar.add(painelDadosDestino, gbc);

        // Atualiza combos de destino quando a origem muda
        cmbContaOrigem.addActionListener(e -> {
            Conta origem = (Conta) cmbContaOrigem.getSelectedItem();
            if (origem != null) {
                cmbContaDestinoInterna.removeAllItems();
                for (Conta c : Sessao.getInstance().getContasCliente()) {
                    if (c.getIdConta() != origem.getIdConta()) {
                        cmbContaDestinoInterna.addItem(c);
                    }
                }
                if (cmbContaDestinoInterna.getItemCount() == 0) {
                    cmbContaDestinoInterna.addItem(null); // placeholder
                }
            }
        });
        // Força a primeira atualização
        cmbContaOrigem.getActionListeners()[0].actionPerformed(null);

        // Alternar painéis
        cmbTipoTransf.addActionListener(e -> {
            CardLayout cl = (CardLayout) painelDadosDestino.getLayout();
            int sel = cmbTipoTransf.getSelectedIndex();
            if (sel == 0) cl.show(painelDadosDestino, "PIX");
            else if (sel == 1) cl.show(painelDadosDestino, "INTERNA");
            else cl.show(painelDadosDestino, "TED");
        });

        // 3. Valor
        JLabel lblValor = new JLabel("Valor da Operação:");
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        painelEnviar.add(lblValor, gbc);

        JTextField txtValor = new JTextField();
        txtValor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        painelEnviar.add(txtValor, gbc);

        // 4. Agendamento
        JCheckBox chkAgendar = new JCheckBox("Agendar esta transferência para uma data futura");
        chkAgendar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkAgendar.setBackground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        painelEnviar.add(chkAgendar, gbc);

        JLabel lblData = new JLabel("Data do Agendamento (DD/MM/AAAA):");
        lblData.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblData.setEnabled(false);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        painelEnviar.add(lblData, gbc);

        JTextField txtData = new JTextField();
        txtData.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtData.setEnabled(false);
        gbc.gridx = 1;
        painelEnviar.add(txtData, gbc);

        chkAgendar.addActionListener(e -> {
            boolean ativo = chkAgendar.isSelected();
            lblData.setEnabled(ativo);
            txtData.setEnabled(ativo);
            if (ativo) {
                txtData.setText(java.time.LocalDate.now().plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                txtData.setText("");
            }
        });

        // 5. Botão Confirmar
        JButton btnConfirmar = new JButton("Confirmar Operação Bancária ➔");
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirmar.setBackground(new Color(108, 92, 231));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        painelEnviar.add(btnConfirmar, gbc);

        // ==========================================
        // LÓGICA DE ENVIO
        // ==========================================
        btnConfirmar.addActionListener(e -> {
            try {
                Conta contaOrigem = (Conta) cmbContaOrigem.getSelectedItem();
                if (contaOrigem == null) {
                    JOptionPane.showMessageDialog(mainPanel, "Nenhuma conta de origem selecionada.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int idContaOrigem = contaOrigem.getIdConta();

                String valorStr = txtValor.getText().trim();
                if (valorStr.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "Informe o valor da transferência.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                BigDecimal valor = new BigDecimal(valorStr.replace(",", "."));
                int modalidade = cmbTipoTransf.getSelectedIndex();

                String destinoReal = "";
                if (modalidade == 0) {
                    destinoReal = txtPixDestino.getText().trim();
                    if (destinoReal.isEmpty()) {
                        JOptionPane.showMessageDialog(mainPanel, "Preencha a chave PIX.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else if (modalidade == 1) {
                    Conta destinoConta = (Conta) cmbContaDestinoInterna.getSelectedItem();
                    if (destinoConta == null) {
                        JOptionPane.showMessageDialog(mainPanel, "Selecione uma conta destino.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    destinoReal = String.valueOf(destinoConta.getIdConta());
                } else {
                    // TED
                    String banco = txtTedBanco.getText().trim();
                    String agencia = txtTedAgencia.getText().trim();
                    String conta = txtTedConta.getText().trim();
                    String digito = txtTedDigito.getText().trim();
                    String cpfCnpj = txtTedCpf.getText().trim();
                    if (banco.isEmpty() || agencia.isEmpty() || conta.isEmpty() || digito.isEmpty() || cpfCnpj.isEmpty()) {
                        JOptionPane.showMessageDialog(mainPanel, "Preencha TODOS os campos do formulário de TED.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    destinoReal = conta;
                }

                String tipoTexto = modalidade == 0 ? "PIX" : (modalidade == 1 ? "INTERNA" : "TED");

                // Agendamento
                if (chkAgendar.isSelected()) {
                    String dataStr = txtData.getText().trim();
                    java.time.format.DateTimeFormatter fmtInput = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    java.time.LocalDate dataAgendada = java.time.LocalDate.parse(dataStr, fmtInput);
                    if (!dataAgendada.isAfter(java.time.LocalDate.now())) {
                        JOptionPane.showMessageDialog(mainPanel, "A data deve ser futura.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    controller.agendarTransferencia(idContaOrigem, tipoTexto, destinoReal, valor, dataAgendada);
                    JOptionPane.showMessageDialog(mainPanel, "Agendamento realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Envio imediato
                    if (modalidade == 0) {
                        controller.transferenciaPIX(idContaOrigem, destinoReal, valor);
                        JOptionPane.showMessageDialog(mainPanel, "PIX efetuado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    } else if (modalidade == 1) {
                        int idDestino = Integer.parseInt(destinoReal);
                        controller.transferenciaInterna(idContaOrigem, idDestino, valor);
                        JOptionPane.showMessageDialog(mainPanel, "Transferência interna concluída!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // TED com todos os campos
                        String banco = txtTedBanco.getText().trim();
                        String agencia = txtTedAgencia.getText().trim();
                        String conta = txtTedConta.getText().trim();
                        String digito = txtTedDigito.getText().trim();
                        String cpfCnpj = txtTedCpf.getText().trim();
                        controller.transferenciaTED(idContaOrigem, banco, agencia, conta, digito, cpfCnpj, valor);
                        JOptionPane.showMessageDialog(mainPanel, "TED enviada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                // Limpeza
                txtPixDestino.setText("");
                txtTedBanco.setText("");
                txtTedAgencia.setText("");
                txtTedConta.setText("");
                txtTedDigito.setText("");
                txtTedCpf.setText("");
                txtValor.setText("");
                txtData.setText("");
                chkAgendar.setSelected(false);
                lblData.setEnabled(false);
                txtData.setEnabled(false);
                cmbContaDestinoInterna.setSelectedIndex(0);

            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Data inválida! Use DD/MM/AAAA.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Valor ou número de conta inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Erro na operação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ==========================================
        // ABA 2: HISTÓRICO DE AGENDAMENTOS
        // ==========================================
        JPanel painelAgendamentos = new JPanel(new BorderLayout(0, 15));
        painelAgendamentos.setBackground(Color.WHITE);
        painelAgendamentos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTituloAgendados = new JLabel("Transferências agendadas desta conta");
        lblTituloAgendados.setFont(new Font("Segoe UI", Font.BOLD, 16));
        painelAgendamentos.add(lblTituloAgendados, BorderLayout.NORTH);

        String[] colunasAgendamento = {"ID", "Data Programada", "Modalidade - Destino", "Valor", "Status"};
        DefaultTableModel modeloAgendamento = new DefaultTableModel(colunasAgendamento, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable tabelaAgendados = new JTable(modeloAgendamento);
        tabelaAgendados.setRowHeight(32);
        tabelaAgendados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaAgendados.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabelaAgendados.getTableHeader().setBackground(new Color(242, 244, 248));

        Runnable recarregarTabelaAgendamentos = () -> {
            modeloAgendamento.setRowCount(0);
            Conta contaAtual = (Conta) cmbContaOrigem.getSelectedItem();
            if (contaAtual != null) {
                try {
                    List<AgendamentoTransferencia> agendados = controller.listarAgendamentosCliente(contaAtual.getIdConta());
                    if (agendados != null && !agendados.isEmpty()) {
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        java.text.NumberFormat fmtMoeda = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));
                        for (AgendamentoTransferencia a : agendados) {
                            modeloAgendamento.addRow(new Object[]{
                                a.getIdAgendamento(),
                                a.getDataAgendada() != null ? a.getDataAgendada().format(fmt) : "-",
                                a.getTipoTransferencia() + " - " + a.getDadosDestino(),
                                fmtMoeda.format(a.getValor()),
                                a.getStatus() != null ? a.getStatus().toString() : "AGENDADO"
                            });
                        }
                    } else {
                        modeloAgendamento.addRow(new Object[]{"-", "Nenhum agendamento para esta conta", "-", "-", "-"});
                    }
                } catch (Exception ex) {
                    modeloAgendamento.addRow(new Object[]{"Erro", "Falha ao carregar", ex.getMessage(), "-", "-"});
                }
            }
        };

        cmbContaOrigem.addActionListener(e -> recarregarTabelaAgendamentos.run());
        recarregarTabelaAgendamentos.run();

        JScrollPane scrollAgendados = new JScrollPane(tabelaAgendados);
        scrollAgendados.getViewport().setBackground(Color.WHITE);
        painelAgendamentos.add(scrollAgendados, BorderLayout.CENTER);

        abasInternas.addTab("Enviar / Agendar Dinheiro", painelEnviar);
        abasInternas.addTab("Histórico de Agendamentos", painelAgendamentos);

        mainPanel.add(abasInternas, BorderLayout.CENTER);
        return mainPanel;
    }

    // ================== PAGAMENTOS ==================
    private JPanel criarPainelPagamentos() {
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.WHITE);

            JTabbedPane abasPagamento = new JTabbedPane();
            abasPagamento.setFont(new Font("Segoe UI", Font.BOLD, 13));

            // -------------------------------------------------------------------------
            // ABA 1: Pagar Conta com Linha Digitável
            // Campos: Conta Débito, Linha Digitável, Valor
            // -------------------------------------------------------------------------
            JPanel pnlLinhaDigitavel = new JPanel(new GridBagLayout());
            pnlLinhaDigitavel.setBackground(Color.WHITE);
            GridBagConstraints gbc1 = new GridBagConstraints();
            gbc1.insets = new Insets(10, 10, 10, 10); gbc1.fill = GridBagConstraints.HORIZONTAL;

            gbc1.gridx = 0; gbc1.gridy = 0;
            pnlLinhaDigitavel.add(new JLabel("Conta Débito:"), gbc1);
            JComboBox<Conta> cmbContaDebitoLinha = new JComboBox<>();
            for (Conta c : contas) cmbContaDebitoLinha.addItem(c);
            gbc1.gridx = 1;
            pnlLinhaDigitavel.add(cmbContaDebitoLinha, gbc1);

            gbc1.gridx = 0; gbc1.gridy = 1;
            pnlLinhaDigitavel.add(new JLabel("Linha Digitável:"), gbc1);
            JTextField txtLinhaDig = new JTextField(20);
            gbc1.gridx = 1;
            pnlLinhaDigitavel.add(txtLinhaDig, gbc1);

            gbc1.gridx = 0; gbc1.gridy = 2;
            pnlLinhaDigitavel.add(new JLabel("Valor (R$):"), gbc1);
            JTextField txtValorLinha = new JTextField(10);
            gbc1.gridx = 1;
            pnlLinhaDigitavel.add(txtValorLinha, gbc1);

            JButton btnPagarLinha = new JButton("Pagar Conta");
            btnPagarLinha.setBackground(new Color(46, 204, 113));
            btnPagarLinha.setForeground(Color.WHITE);
            gbc1.gridx = 0; gbc1.gridy = 3; gbc1.gridwidth = 2;
            pnlLinhaDigitavel.add(btnPagarLinha, gbc1);

            btnPagarLinha.addActionListener(e -> {
                try {
                    Conta c = (Conta) cmbContaDebitoLinha.getSelectedItem();
                    String linha = txtLinhaDig.getText().trim();
                    BigDecimal valor = new BigDecimal(txtValorLinha.getText().trim().replace(",", "."));

                    controller.pagarConta(c.getIdConta(), linha, valor);
                    JOptionPane.showMessageDialog(mainPanel, "Conta paga com sucesso!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            // -------------------------------------------------------------------------
            // ABA 2: Pagar Fatura de Cartão de Crédito
            // Campos: Conta Débito, Fatura (Exibindo valor em aberto)
            // -------------------------------------------------------------------------
            JPanel pnlFatura = new JPanel(new GridBagLayout());
            pnlFatura.setBackground(Color.WHITE);
            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc2.insets = new Insets(10, 10, 10, 10); gbc2.fill = GridBagConstraints.HORIZONTAL;

            gbc2.gridx = 0; gbc2.gridy = 0;
            pnlFatura.add(new JLabel("Conta Débito:"), gbc2);
            JComboBox<Conta> cmbContaDebitoFat = new JComboBox<>();
            for (Conta c : contas) cmbContaDebitoFat.addItem(c);
            gbc2.gridx = 1;
            pnlFatura.add(cmbContaDebitoFat, gbc2);

            gbc2.gridx = 0; gbc2.gridy = 1;
            pnlFatura.add(new JLabel("Fatura em Aberto:"), gbc2);
            JComboBox<FaturaCartao> cmbFaturas = new JComboBox<>();
            cmbFaturas.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof FaturaCartao) {
                        FaturaCartao f = (FaturaCartao) value;
                        setText("Fatura " + f.getIdFatura() + " - Valor: R$ " + f.getValorTotal());
                    }
                    return this;
                }
            });
            gbc2.gridx = 1;
            pnlFatura.add(cmbFaturas, gbc2);

            JButton btnPagarFat = new JButton("Pagar Fatura");
            btnPagarFat.setBackground(new Color(108, 92, 231));
            btnPagarFat.setForeground(Color.WHITE);
            gbc2.gridx = 0; gbc2.gridy = 2; gbc2.gridwidth = 2;
            pnlFatura.add(btnPagarFat, gbc2);

            btnPagarFat.addActionListener(e -> {
                try {
                    Conta c = (Conta) cmbContaDebitoFat.getSelectedItem();
                    FaturaCartao f = (FaturaCartao) cmbFaturas.getSelectedItem();

                    controller.pagarFaturaCartao(c.getIdConta(), f.getIdFatura());
                    JOptionPane.showMessageDialog(mainPanel, "Fatura paga com sucesso!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            // -------------------------------------------------------------------------
            // ABA 3: Pagamento PIX QR Code
            // Campos: Conta Débito, QR Code, Valor
            // -------------------------------------------------------------------------
            JPanel pnlPix = new JPanel(new GridBagLayout());
            pnlPix.setBackground(Color.WHITE);
            GridBagConstraints gbc3 = new GridBagConstraints();
            gbc3.insets = new Insets(10, 10, 10, 10); gbc3.fill = GridBagConstraints.HORIZONTAL;

            gbc3.gridx = 0; gbc3.gridy = 0;
            pnlPix.add(new JLabel("Conta Débito:"), gbc3);
            JComboBox<Conta> cmbContaPix = new JComboBox<>();
            for (Conta c : contas) cmbContaPix.addItem(c);
            gbc3.gridx = 1;
            pnlPix.add(cmbContaPix, gbc3);

            gbc3.gridx = 0; gbc3.gridy = 1;
            pnlPix.add(new JLabel("QR Code (Copia e Cola):"), gbc3);
            JTextField txtQRCode = new JTextField(20);
            gbc3.gridx = 1;
            pnlPix.add(txtQRCode, gbc3);

            gbc3.gridx = 0; gbc3.gridy = 2;
            pnlPix.add(new JLabel("Valor (R$):"), gbc3);
            JTextField txtValorPix = new JTextField(10);
            gbc3.gridx = 1;
            pnlPix.add(txtValorPix, gbc3);

            JButton btnPagarPix = new JButton("Pagar via PIX");
            btnPagarPix.setBackground(new Color(108, 92, 231));
            btnPagarPix.setForeground(Color.WHITE);
            gbc3.gridx = 0; gbc3.gridy = 3; gbc3.gridwidth = 2;
            pnlPix.add(btnPagarPix, gbc3);

            btnPagarPix.addActionListener(e -> {
                try {
                    Conta c = (Conta) cmbContaPix.getSelectedItem();
                    String qr = txtQRCode.getText().trim();
                    BigDecimal valor = new BigDecimal(txtValorPix.getText().trim().replace(",", "."));

                    controller.pagarPIXQRCode(c.getIdConta(), qr, valor);
                    JOptionPane.showMessageDialog(mainPanel, "PIX realizado com sucesso!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            // -------------------------------------------------------------------------
            // ABA 4: Pagamento com Cartão de Crédito
            // Campos: Cartão (exibindo limite), Descrição, Valor, Parcelas
            // -------------------------------------------------------------------------
            JPanel pnlCartao = new JPanel(new GridBagLayout());
            pnlCartao.setBackground(Color.WHITE);
            GridBagConstraints gbc4 = new GridBagConstraints();
            gbc4.insets = new Insets(10, 10, 10, 10); gbc4.fill = GridBagConstraints.HORIZONTAL;

            gbc4.gridx = 0; gbc4.gridy = 0;
            pnlCartao.add(new JLabel("Selecionar Cartão:"), gbc4);
            JComboBox<CartaoCredito> cmbCartoes = new JComboBox<>();
            cmbCartoes.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof CartaoCredito) {
                        CartaoCredito c = (CartaoCredito) value;
                        setText("Cartão " + c.getIdCartao() + " - Limite Utilizado: R$ " + c.getLimiteUtilizado());
                    }
                    return this;
                }
            });
            gbc4.gridx = 1;
            pnlCartao.add(cmbCartoes, gbc4);

            gbc4.gridx = 0; gbc4.gridy = 1;
            pnlCartao.add(new JLabel("Descrição da Compra:"), gbc4);
            JTextField txtDesc = new JTextField(20);
            gbc4.gridx = 1;
            pnlCartao.add(txtDesc, gbc4);

            gbc4.gridx = 0; gbc4.gridy = 2;
            pnlCartao.add(new JLabel("Valor (R$):"), gbc4);
            JTextField txtValorCartao = new JTextField(10);
            gbc4.gridx = 1;
            pnlCartao.add(txtValorCartao, gbc4);

            gbc4.gridx = 0; gbc4.gridy = 3;
            pnlCartao.add(new JLabel("Parcelas:"), gbc4);
            JSpinner spnParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
            gbc4.gridx = 1;
            pnlCartao.add(spnParcelas, gbc4);

            JButton btnComprarCartao = new JButton("Realizar Pagamento");
            btnComprarCartao.setBackground(new Color(108, 92, 231));
            btnComprarCartao.setForeground(Color.WHITE);
            gbc4.gridx = 0; gbc4.gridy = 4; gbc4.gridwidth = 2;
            pnlCartao.add(btnComprarCartao, gbc4);

            btnComprarCartao.addActionListener(e -> {
                try {
                    CartaoCredito c = (CartaoCredito) cmbCartoes.getSelectedItem();
                    String desc = txtDesc.getText().trim();
                    BigDecimal valor = new BigDecimal(txtValorCartao.getText().trim().replace(",", "."));
                    int parcelas = (Integer) spnParcelas.getValue();

                    controller.pagarComCartao(c.getIdCartao(), valor, desc, parcelas);
                    JOptionPane.showMessageDialog(mainPanel, "Compra no cartão aprovada!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            // -------------------------------------------------------------------------
            // CARREGAMENTO DOS DADOS PARA AS COMBOS
            // -------------------------------------------------------------------------
            try {
                List<FaturaCartao> faturas = controller.listarFaturasCliente(clienteLogado.getIdCliente());
                if (faturas != null) for (FaturaCartao f : faturas) cmbFaturas.addItem(f);

                List<CartaoCredito> cartoes = controller.listarCartoesCliente(clienteLogado.getIdCliente());
                if (cartoes != null) for (CartaoCredito c : cartoes) cmbCartoes.addItem(c);
            } catch (Exception ex) {
                System.err.println("Erro ao carregar faturas/cartões: " + ex.getMessage());
            }

            abasPagamento.addTab("Linha Digitável", pnlLinhaDigitavel);
            abasPagamento.addTab("Fatura do Cartão", pnlFatura);
            abasPagamento.addTab("PIX QR Code", pnlPix);
            abasPagamento.addTab("Pagamento com Cartão", pnlCartao);

            mainPanel.add(abasPagamento, BorderLayout.CENTER);
            return mainPanel;
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
    

        private void atualizarTopo() {
        // Atualiza a barra superior com o novo nome, se necessário
        Component[] comps = getContentPane().getComponents();
        for (Component c : comps) {
            if (c instanceof JPanel && ((JPanel) c).getLayout() instanceof FlowLayout) {
                JPanel top = (JPanel) c;
                // Encontrar o JLabel com o nome
                for (Component sub : top.getComponents()) {
                    if (sub instanceof JLabel) {
                        JLabel lbl = (JLabel) sub;
                        if (lbl.getText().contains("Cliente:")) {
                            lbl.setText("Cliente: " + clienteLogado.getNomeRazao() + " | " +
                                    (clienteLogado.getTipoPessoa() == TipoPessoa.PF ? "PF" : "PJ"));
                            break;
                        }
                    }
                }
            }
        }
    }
        
        
    private JPanel criarPainelConfiguracoes() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        int y = 0;

        // ========== SEÇÃO ALERTAS ==========
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel lblAlertas = new JLabel("Alertas");
        lblAlertas.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblAlertas, gbc);
        y++;

        gbc.gridwidth = 1;
        JCheckBox chkSaldoBaixo = new JCheckBox("Alertar saldo baixo (pop-up)");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(chkSaldoBaixo, gbc);
        y++;

        gbc.gridwidth = 1;
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Limite (R$):"), gbc);
        JTextField txtLimiteSaldo = new JTextField(10);
        gbc.gridx = 1;
        panel.add(txtLimiteSaldo, gbc);
        y++;

        // ========== SEÇÃO DADOS CADASTRAIS ==========
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel lblDados = new JLabel("Dados Cadastrais");
        lblDados.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblDados, gbc);
        y++;

        gbc.gridwidth = 1;
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Nome/Razão Social:"), gbc);
        JTextField txtNome = new JTextField(clienteLogado.getNomeRazao(), 20);
        gbc.gridx = 1;
        panel.add(txtNome, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("E-mail:"), gbc);
        JTextField txtEmail = new JTextField(clienteLogado.getEmail(), 20);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Telefone:"), gbc);
        JTextField txtTelefone = new JTextField(clienteLogado.getTelefone(), 20);
        gbc.gridx = 1;
        panel.add(txtTelefone, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Endereço:"), gbc);
        JTextField txtEndereco = new JTextField(clienteLogado.getEndereco(), 20);
        gbc.gridx = 1;
        panel.add(txtEndereco, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Nova Senha:"), gbc);
        JPasswordField txtNovaSenha = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(txtNovaSenha, gbc);
        y++;

        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Confirmar Senha:"), gbc);
        JPasswordField txtConfirmarSenha = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(txtConfirmarSenha, gbc);
        y++;

        JButton btnSalvarDados = new JButton("Salvar Alterações");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnSalvarDados.addActionListener(e -> {
            try {
                String nome = txtNome.getText().trim();
                String email = txtEmail.getText().trim();
                String telefone = txtTelefone.getText().trim();
                String endereco = txtEndereco.getText().trim();
                String novaSenha = new String(txtNovaSenha.getPassword());
                String confirmar = new String(txtConfirmarSenha.getPassword());

                if (nome.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nome e e-mail são obrigatórios.");
                    return;
                }

                clienteLogado.setNomeRazao(nome);
                clienteLogado.setEmail(email);
                clienteLogado.setTelefone(telefone);
                clienteLogado.setEndereco(endereco);

                if (!novaSenha.isEmpty()) {
                    if (!novaSenha.equals(confirmar)) {
                        JOptionPane.showMessageDialog(this, "As senhas não conferem.");
                        return;
                    }
                    // Atualizar senha (hash)
                    clienteLogado.setSenhaHash(CriptografiaUtil.gerarHash(novaSenha));
                }

                controller.atualizarCliente(clienteLogado);
                JOptionPane.showMessageDialog(this, "Dados atualizados com sucesso!");
                // Atualizar a barra superior
                atualizarTopo();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnSalvarDados, gbc);
        y++;

        // ========== SEÇÃO GERENCIAMENTO DE CONTAS ==========
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel lblContas = new JLabel("Gerenciamento de Contas");
        lblContas.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblContas, gbc);
        y++;

        gbc.gridwidth = 1;
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Selecione a conta:"), gbc);
        JComboBox<Conta> cmbContaEncerrar = new JComboBox<>();
        for (Conta c : contas) {
            if (c.getStatus() == StatusConta.ATIVA) {
                cmbContaEncerrar.addItem(c);
            }
        }
        gbc.gridx = 1;
        panel.add(cmbContaEncerrar, gbc);
        y++;

        
        JButton btnEncerrarConta = new JButton("Encerrar Conta Selecionada");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnEncerrarConta.addActionListener(e -> {
            Conta conta = (Conta) cmbContaEncerrar.getSelectedItem();
            if (conta == null) {
                JOptionPane.showMessageDialog(TelaPrincipal.this, "Nenhuma conta ativa para encerrar.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(TelaPrincipal.this,
                    "Tem certeza que deseja encerrar a conta " + conta.getNumeroConta() + "?",
                    "Confirmar Encerramento", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                Conta destino = null;
                if (conta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
                    JComboBox<Conta> cmbDestino = new JComboBox<>();
                    for (Conta c : contas) {
                        if (c.getIdConta() != conta.getIdConta() && c.getStatus() == StatusConta.ATIVA) {
                            cmbDestino.addItem(c);
                        }
                    }
                    if (cmbDestino.getItemCount() == 0) {
                        JOptionPane.showMessageDialog(TelaPrincipal.this, "Não há outra conta ativa para transferir o saldo.");
                        return;
                    }

                    Object[] options = {"OK", "Cancelar"};
                    // Usar um JPanel com nome diferente para não conflitar com o panel da tela
                    JPanel painelDestino = new JPanel(new BorderLayout(5, 5));
                    painelDestino.add(new JLabel("Saldo remanescente: " + FormatadorUtil.formatarMoeda(conta.getSaldo())), BorderLayout.NORTH);
                    painelDestino.add(cmbDestino, BorderLayout.CENTER);

                    int result = JOptionPane.showOptionDialog(TelaPrincipal.this, painelDestino, "Transferir Saldo",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            options, options[0]);
                    if (result == JOptionPane.OK_OPTION) {
                        destino = (Conta) cmbDestino.getSelectedItem();
                    } else {
                        return;
                    }
                }

                controller.encerrarConta(conta.getIdConta(), destino != null ? destino.getIdConta() : 0);
                JOptionPane.showMessageDialog(TelaPrincipal.this, "Conta encerrada com sucesso!");

                // Atualizar lista de contas
                contas = controller.buscarContasPorCliente(clienteLogado.getIdCliente());
                Sessao.getInstance().setContasCliente(contas);
                atualizarCombosContas();
                // Atualizar combobox de encerramento
                cmbContaEncerrar.removeAllItems();
                for (Conta c : contas) {
                    if (c.getStatus() == StatusConta.ATIVA) {
                        cmbContaEncerrar.addItem(c);
                    }
                }
                carregarSaldo();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(TelaPrincipal.this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnEncerrarConta, gbc);
        y++;

        // ========== SEÇÃO EXCLUIR USUÁRIO ==========
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel lblExcluir = new JLabel("Excluir Usuário");
        lblExcluir.setFont(new Font("Arial", Font.BOLD, 14));
        lblExcluir.setForeground(Color.RED);
        panel.add(lblExcluir, gbc);
        y++;

        JButton btnExcluirUsuario = new JButton("Excluir Minha Conta (Usuário)");
        btnExcluirUsuario.setForeground(Color.RED);
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnExcluirUsuario.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "ATENÇÃO: Isso excluirá permanentemente sua conta de usuário e todas as suas contas bancárias. Tem certeza?",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    controller.excluirCliente(clienteLogado.getIdCliente());
                    JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso.");
                    logout();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(btnExcluirUsuario, gbc);
        y++;

        // ========== BOTÃO SALVAR CONFIGURAÇÕES DE ALERTA ==========
        JButton btnSalvarAlertas = new JButton("Salvar Preferências de Alertas");
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        btnSalvarAlertas.addActionListener(e -> {
            try {
                PreferenciaAlertas pref = new PreferenciaAlertas(clienteLogado.getIdCliente());
                pref.setAlertaSaldoBaixo(chkSaldoBaixo.isSelected());
                if (chkSaldoBaixo.isSelected() && !txtLimiteSaldo.getText().isEmpty()) {
                    pref.setValorLimiteSaldo(new BigDecimal(txtLimiteSaldo.getText().replace(",", ".")));
                } else {
                    pref.setValorLimiteSaldo(null);
                }
                // Removemos os outros canais e alertas
                controller.configurarAlertas(pref);
                JOptionPane.showMessageDialog(this, "Preferências de alertas salvas!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnSalvarAlertas, gbc);
        y++;

        // Carregar preferências atuais
        try {
            PreferenciaAlertas pref = controller.buscarAlertas(clienteLogado.getIdCliente());
            if (pref != null) {
                chkSaldoBaixo.setSelected(pref.getAlertaSaldoBaixo());
                if (pref.getValorLimiteSaldo() != null)
                    txtLimiteSaldo.setText(pref.getValorLimiteSaldo().toString());
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