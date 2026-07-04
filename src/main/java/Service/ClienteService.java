/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Cliente;
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.enums.StatusConta;
import com.mycompany.yashin.model.enums.TipoConta;
import com.mycompany.yashin.model.enums.TipoPessoa;

import dao.ClienteDAO;
import dao.ContaDAO;
import java.math.BigDecimal;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import util.CriptografiaUtil;
import util.LogUtil;

public class ClienteService {
    private ClienteDAO clienteDAO = new ClienteDAO();
    private ContaDAO contaDAO = new ContaDAO();
    private ContaService contaService = new ContaService();

    public Cliente abrirContaPF(String cpf, String nome, String email, String telefone,
                                String endereco, String senha, String rg, LocalDate dataNascimento,
                                String tipoConta, String agencia) throws Exception {
        validarCpfCnpj(cpf);

        Cliente cliente = new Cliente();
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setCpfCnpj(cpf);
        cliente.setNomeRazao(nome);
        cliente.setEmail(email);
        cliente.setTelefone(telefone);
        cliente.setEndereco(endereco);
        cliente.setSenhaHash(CriptografiaUtil.gerarHash(senha)); 
        cliente.setRg(rg);
        cliente.setDataNascimento(dataNascimento);
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());

        return salvarClienteEConta(cliente, tipoConta, agencia);
    }

    public Cliente abrirContaPJ(String cnpj, String razaoSocial, String email, String telefone,
                                String endereco, String senha, String inscricaoEstadual,
                                String contratoSocialUrl, String representanteLegal,
                                String tipoConta, String agencia) throws Exception {
        validarCpfCnpj(cnpj);

        Cliente cliente = new Cliente();
        cliente.setTipoPessoa(TipoPessoa.PJ);
        cliente.setCpfCnpj(cnpj);
        cliente.setNomeRazao(razaoSocial);
        cliente.setEmail(email);
        cliente.setTelefone(telefone);
        cliente.setEndereco(endereco);
        cliente.setSenhaHash(CriptografiaUtil.gerarHash(senha));
        cliente.setInscricaoEstadual(inscricaoEstadual);
        cliente.setContratoSocialUrl(contratoSocialUrl);
        cliente.setRepresentanteLegal(representanteLegal);
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());
        

        return salvarClienteEConta(cliente, tipoConta, agencia);
    }

    private Cliente salvarClienteEConta(Cliente cliente, String tipoConta, String agencia) throws Exception {
        // Verifica se já existe
        if (clienteDAO.buscarPorCpfCnpj(cliente.getCpfCnpj()) != null) {
            throw new Exception("CPF/CNPJ já cadastrado");
            
        }

        // Insere cliente
        clienteDAO.inserir(cliente);

        // Cria conta padrão
        Conta conta = new Conta();
        conta.setIdCliente(cliente.getIdCliente());
        conta.setTipoConta(TipoConta.valueOf(tipoConta.toUpperCase()));
        conta.setAgencia(agencia);
        conta.setNumeroConta(contaService.gerarNumeroConta());
        conta.setStatus(StatusConta.ATIVA);
        conta.setSaldo(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(100, 10000)));
        contaDAO.inserir(conta);

        LogUtil.registrarLog(cliente.getIdCliente(), "ABERTURA_CONTA", 
                "Conta criada: " + conta.getNumeroConta());

        return cliente;
    }

    public Cliente buscarPorCpfCnpj(String cpfCnpj) throws SQLException {
        return clienteDAO.buscarPorCpfCnpj(cpfCnpj);
    }

    public Cliente buscarPorId(int id) throws SQLException {
        return clienteDAO.buscarPorId(id);
    }

    public void atualizarDados(Cliente cliente) throws Exception {
        if (cliente.getIdCliente() == null) {
            throw new Exception("Cliente não identificado");
        }
        clienteDAO.atualizar(cliente);
        LogUtil.registrarLog(cliente.getIdCliente(), "ATUALIZACAO_CADASTRO", 
                "Dados atualizados: " + cliente.getEmail());
    }

    public void desativarCliente(int idCliente) throws Exception {
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) throw new Exception("Cliente não encontrado");
        cliente.setAtivo(false);
        clienteDAO.atualizar(cliente);
    }

    private void validarCpfCnpj(String documento) throws Exception {
        if (documento == null || documento.trim().isEmpty()) {
            throw new Exception("Documento não informado");
        }
        // Validação simplificada - apenas verifica tamanho
        String numeros = documento.replaceAll("\\D", "");
        if (!(numeros.length() == 11 || numeros.length() == 14)) {
            throw new Exception("CPF/CNPJ inválido");
        }
    }
}
