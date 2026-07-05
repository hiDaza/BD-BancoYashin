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
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.StatusConta;
import com.mycompany.yashin.model.enums.TipoConta;

import dao.ContaDAO;
import dao.TransacaoDAO;
import dao.ClienteDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import util.LogUtil;

public class ContaService {
    private ContaDAO contaDAO = new ContaDAO();
    private TransacaoDAO transacaoDAO = new TransacaoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();

    public String gerarNumeroConta() {
        Random random = new Random();
        return String.format("%08d", random.nextInt(100000000));
    }

    public BigDecimal consultarSaldo(int idConta) throws Exception {
        Conta conta = contaDAO.buscarPorId(idConta);
        if (conta == null) throw new Exception("Conta não encontrada");
        return conta.getSaldo();
    }

    public List<Transacao> visualizarExtrato(int idConta, LocalDateTime inicio, LocalDateTime fim) throws Exception {
        Conta conta = contaDAO.buscarPorId(idConta);
        if (conta == null) throw new Exception("Conta não encontrada");
        return transacaoDAO.listarPorConta(idConta, inicio, fim);
    }

    public void encerrarConta(int idConta, int idContaDestinoTransferencia) throws Exception {
        Conta conta = contaDAO.buscarPorId(idConta);
        if (conta == null) throw new Exception("Conta não encontrada");

        // Verifica pendências (ex: empréstimos ativos, faturas em aberto - simplificado)
        if (conta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            if (idContaDestinoTransferencia <= 0) {
                throw new Exception("É necessário informar uma conta destino para transferir o saldo remanescente");
            }
            // Transferir saldo para outra conta
            Conta destino = contaDAO.buscarPorId(idContaDestinoTransferencia);
            if (destino == null) throw new Exception("Conta destino não encontrada");
            destino.setSaldo(destino.getSaldo().add(conta.getSaldo()));
            contaDAO.atualizarSaldo(destino);
            conta.setSaldo(BigDecimal.ZERO);
            contaDAO.atualizarSaldo(conta);
        }

        conta.setStatus(StatusConta.ENCERRADA);
        contaDAO.atualizar(conta);

        LogUtil.registrarLog(conta.getIdCliente(), "ENCERRAR_CONTA", 
                "Conta encerrada: " + conta.getNumeroConta());
    }

    public void atualizarLimites(int idConta, BigDecimal limiteTed, BigDecimal limitePix) throws Exception {
        Conta conta = contaDAO.buscarPorId(idConta);
        if (conta == null) throw new Exception("Conta não encontrada");
        conta.setLimiteDiarioTed(limiteTed);
        conta.setLimiteDiarioPix(limitePix);
        contaDAO.atualizar(conta);
    }
    
    
        public Conta abrirNovaConta(int idCliente, String tipoConta, String agencia) throws Exception {
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) throw new Exception("Cliente não encontrado");
        if (!cliente.isAtivo()) throw new Exception("Cliente inativo");

        Conta conta = new Conta();
        conta.setIdCliente(idCliente);
        conta.setTipoConta(TipoConta.valueOf(tipoConta.toUpperCase()));
        conta.setAgencia(agencia);
        conta.setNumeroConta(gerarNumeroConta());
        conta.setStatus(StatusConta.ATIVA);
        // Saldo inicial aleatório (R$ 100 a R$ 10.000)
        conta.setSaldo(BigDecimal.valueOf(100 + Math.random() * 9900));
        contaDAO.inserir(conta);

        LogUtil.registrarLog(idCliente, "ABERTURA_NOVA_CONTA", "Conta criada: " + conta.getNumeroConta());
        return conta;
    }   
    
    

    public Conta buscarPorId(int id) throws SQLException {
        return contaDAO.buscarPorId(id);
    }
    
    public List<Conta> buscarPorCliente(int idCliente) throws SQLException {
        return contaDAO.listarPorCliente(idCliente);
    }
    
    
}
