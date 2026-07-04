/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Cliente;
import com.mycompany.yashin.model.Conta;
import controller.BancoController;
import controller.Sessao;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TelaLogin extends JFrame {
    private JTextField txtCpf;
    private JPasswordField txtSenha;
    private JButton btnLogin, btnCadastrar;

    private BancoController controller = new BancoController();

    public TelaLogin() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema Bancário Yashin - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Título
        JLabel lblTitulo = new JLabel("Bem-vindo ao Banco Yashin");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // CPF/CNPJ
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("CPF/CNPJ:"), gbc);
        txtCpf = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtCpf, gbc);

        // Senha
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Senha:"), gbc);
        txtSenha = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(txtSenha, gbc);

        // Botões
        btnLogin = new JButton("Entrar");
        btnCadastrar = new JButton("Cadastrar-se");

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoes.add(btnLogin);
        botoes.add(btnCadastrar);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(botoes, gbc);

        add(panel);
        pack();

        // Eventos
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        btnCadastrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaCadastro().setVisible(true);
                dispose();
            }
        });

        // Enter para login
        getRootPane().setDefaultButton(btnLogin);
    }

    private void login() {
        String cpf = txtCpf.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (cpf.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Cliente cliente = controller.login(cpf, senha);
            Sessao.getInstance().setClienteLogado(cliente);

            // Carregar contas do cliente
            List<Conta> contas = controller.buscarContasPorCliente(cliente.getIdCliente());
            Sessao.getInstance().setContasCliente(contas);

            // Abrir tela principal
            new TelaPrincipal().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro no login: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaLogin().setVisible(true);
            }
        });
    }
}
