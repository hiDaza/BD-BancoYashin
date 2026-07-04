/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

/**
 *
 * @author daza
 */
import controller.BancoController;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import util.CatalogoBancos;
import util.FormatadorUtil;

public class TelaCadastro extends JFrame {
    private BancoController controller = new BancoController();

    private JComboBox<String> cmbTipoPessoa;
    private JTextField txtDocumento, txtNome, txtEmail, txtTelefone, txtEndereco;
    private JPasswordField txtSenha;
    private JComboBox<String> cmbTipoConta;
    private JComboBox<String> cmbAgencia;  // <--- declarado como atributo

    // Campos PF
    private JTextField txtRg, txtDataNascimento;
    // Campos PJ
    private JTextField txtInscricaoEstadual, txtRepresentante, txtContratoSocial;

    private JButton btnCadastrar, btnVoltar;

    public TelaCadastro() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Cadastro - Banco Yashin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 580);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        JLabel titulo = new JLabel("Cadastro de Cliente");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        panel.add(titulo, gbc);
        y++;

        // Tipo de pessoa
        gbc.gridwidth = 1;
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Tipo de Pessoa:"), gbc);
        cmbTipoPessoa = new JComboBox<>(new String[]{"PF", "PJ"});
        cmbTipoPessoa.addActionListener(e -> atualizarCamposPessoa());
        gbc.gridx = 1;
        panel.add(cmbTipoPessoa, gbc);
        y++;

        // Documento
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("CPF/CNPJ:"), gbc);
        txtDocumento = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtDocumento, gbc);
        y++;

        // Nome
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Nome/Razão Social:"), gbc);
        txtNome = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtNome, gbc);
        y++;

        // Email
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("E-mail:"), gbc);
        txtEmail = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);
        y++;

        // Telefone
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Telefone:"), gbc);
        txtTelefone = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtTelefone, gbc);
        y++;

        // Endereço
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Endereço:"), gbc);
        txtEndereco = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtEndereco, gbc);
        y++;

        // Senha
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Senha:"), gbc);
        txtSenha = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(txtSenha, gbc);
        y++;

        // Campos PF
        JLabel lblRg = new JLabel("RG:");
        txtRg = new JTextField(20);
        JLabel lblDataNasc = new JLabel("Data Nasc. (dd/mm/aaaa):");
        txtDataNascimento = new JTextField(20);
        // Campos PJ
        JLabel lblInscEst = new JLabel("Inscrição Estadual:");
        txtInscricaoEstadual = new JTextField(20);
        JLabel lblRepresentante = new JLabel("Representante Legal:");
        txtRepresentante = new JTextField(20);
        JLabel lblContratoSocial = new JLabel("Contrato Social (URL):");
        txtContratoSocial = new JTextField(20);

        // Adicionar PF
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(lblRg, gbc);
        gbc.gridx = 1;
        panel.add(txtRg, gbc);
        y++;
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(lblDataNasc, gbc);
        gbc.gridx = 1;
        panel.add(txtDataNascimento, gbc);
        y++;

        // Adicionar PJ
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(lblInscEst, gbc);
        gbc.gridx = 1;
        panel.add(txtInscricaoEstadual, gbc);
        y++;
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(lblRepresentante, gbc);
        gbc.gridx = 1;
        panel.add(txtRepresentante, gbc);
        y++;
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(lblContratoSocial, gbc);
        gbc.gridx = 1;
        panel.add(txtContratoSocial, gbc);
        y++;

        // Tipo de conta
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Tipo de Conta:"), gbc);
        cmbTipoConta = new JComboBox<>(new String[]{"CORRENTE", "POUPANCA"});
        gbc.gridx = 1;
        panel.add(cmbTipoConta, gbc);
        y++;

        // Agência - usando JComboBox
        gbc.gridy = y; gbc.gridx = 0;
        panel.add(new JLabel("Agência:"), gbc);
        cmbAgencia = new JComboBox<>();   // inicializado aqui
        // Popular com as agências do catálogo
        for (String ag : CatalogoBancos.getAgenciasYashin()) {
            cmbAgencia.addItem(ag);
        }
        // Se a lista estiver vazia, adicionar um item padrão
        if (cmbAgencia.getItemCount() == 0) {
            cmbAgencia.addItem("0001");
        }
        gbc.gridx = 1;
        panel.add(cmbAgencia, gbc);
        y++;

        // Botões
        btnCadastrar = new JButton("Cadastrar");
        btnVoltar = new JButton("Voltar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoes.add(btnCadastrar);
        botoes.add(btnVoltar);
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(botoes, gbc);

        add(new JScrollPane(panel));
        atualizarCamposPessoa();
        pack();

        btnCadastrar.addActionListener(e -> cadastrar());
        btnVoltar.addActionListener(e -> {
            new TelaLogin().setVisible(true);
            dispose();
        });
    }

    private void atualizarCamposPessoa() {
        boolean isPJ = "PJ".equals(cmbTipoPessoa.getSelectedItem());
        txtRg.setVisible(!isPJ);
        txtDataNascimento.setVisible(!isPJ);
        txtInscricaoEstadual.setVisible(isPJ);
        txtRepresentante.setVisible(isPJ);
        txtContratoSocial.setVisible(isPJ);
        revalidate();
        repaint();
    }

    private void cadastrar() {
        try {
            String tipoPessoa = (String) cmbTipoPessoa.getSelectedItem();
            String documento = txtDocumento.getText().trim();
            String nome = txtNome.getText().trim();
            String email = txtEmail.getText().trim();
            String telefone = txtTelefone.getText().trim();
            String endereco = txtEndereco.getText().trim();
            String senha = new String(txtSenha.getPassword());
            String tipoConta = (String) cmbTipoConta.getSelectedItem();
            String agencia = (String) cmbAgencia.getSelectedItem();

            if (documento.isEmpty() || nome.isEmpty() || email.isEmpty() || senha.isEmpty() || agencia == null || agencia.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            com.mycompany.yashin.model.Cliente cliente;
            if ("PF".equals(tipoPessoa)) {
                String rg = txtRg.getText().trim();
                String dataNascStr = txtDataNascimento.getText().trim();
                if (rg.isEmpty() || dataNascStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Preencha RG e Data de Nascimento.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LocalDate dataNasc = FormatadorUtil.parseData(dataNascStr);
                cliente = controller.abrirContaPF(documento, nome, email, telefone, endereco, senha, rg, dataNasc, tipoConta, agencia);
            } else {
                String inscEst = txtInscricaoEstadual.getText().trim();
                String repLegal = txtRepresentante.getText().trim();
                String contrato = txtContratoSocial.getText().trim();
                if (inscEst.isEmpty() || repLegal.isEmpty() || contrato.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Preencha todos os campos PJ.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                cliente = controller.abrirContaPJ(documento, nome, email, telefone, endereco, senha,
                        inscEst, contrato, repLegal, tipoConta, agencia);
            }

            JOptionPane.showMessageDialog(this, "Cadastro realizado com sucesso! Seu ID: " + cliente.getIdCliente());
            new TelaLogin().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro no cadastro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
