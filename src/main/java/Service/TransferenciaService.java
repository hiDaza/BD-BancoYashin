/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.AgendamentoTransferencia;
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.StatusAgendamento;
import com.mycompany.yashin.model.enums.StatusConta;
import com.mycompany.yashin.model.enums.StatusTransacao;
import com.mycompany.yashin.model.enums.TipoTransacao;
import dao.AgendamentoDAO;
import dao.ContaDAO;
import dao.TransacaoDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import util.LogUtil;

public class TransferenciaService {
    private ContaDAO contaDAO = new ContaDAO();
    private TransacaoDAO transacaoDAO = new TransacaoDAO();
    private AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

    // Simulação de integração externa (será substituída pela camada external)
    private String simularSpix(String chavePix, BigDecimal valor) {
        return "E2E" + System.currentTimeMillis();
    }

    private String simularStr(String banco, String agencia, String conta, BigDecimal valor) {
        return "CTRL" + System.currentTimeMillis();
    }

    public void transferenciaInterna(int idContaOrigem, int idContaDestino, BigDecimal valor) throws Exception {
        Conta origem = contaDAO.buscarPorId(idContaOrigem);
        Conta destino = contaDAO.buscarPorId(idContaDestino);

        validarContasAtivas(origem, destino);
        validarSaldoLimites(origem, valor, "INTERNA");

        // Atualiza saldos
        origem.setSaldo(origem.getSaldo().subtract(valor));
        destino.setSaldo(destino.getSaldo().add(valor));
        contaDAO.atualizarSaldo(origem);
        new NotificacaoService().verificarEAlertarSaldoBaixo(
        origem.getIdCliente(), 
        origem.getIdConta(), 
        origem.getSaldo()
    );
        contaDAO.atualizarSaldo(destino);

        // Registra transação
        Transacao t = new Transacao();
        t.setIdContaOrigem(idContaOrigem);
        t.setIdContaDestino(idContaDestino);
        t.setTipoTransacao(TipoTransacao.TRANSFERENCIA_INTERNA);
        t.setValor(valor);
        t.setStatus(StatusTransacao.PROCESSADA);
        t.setDescricao("Transferência interna para conta " + destino.getNumeroConta());
        transacaoDAO.inserir(t);

        LogUtil.registrarLog(origem.getIdCliente(), "TRANSFERENCIA_INTERNA", 
                "Valor: " + valor + " para conta: " + destino.getNumeroConta());
    }

    public void transferenciaTED(int idContaOrigem, String bancoDestino, String agenciaDestino,
                                 String contaDestino, String cpfCnpjBeneficiario,
                                 String nomeBeneficiario, BigDecimal valor) throws Exception {
        Conta origem = contaDAO.buscarPorId(idContaOrigem);
        validarContaAtiva(origem);
        validarSaldoLimites(origem, valor, "TED");

        // Simula envio ao STR
        String numControle = simularStr(bancoDestino, agenciaDestino, contaDestino, valor);

        // Atualiza saldo
        origem.setSaldo(origem.getSaldo().subtract(valor));
        contaDAO.atualizarSaldo(origem);
        new NotificacaoService().verificarEAlertarSaldoBaixo(
        origem.getIdCliente(), 
        origem.getIdConta(), 
        origem.getSaldo()
    );

        // Registra transação
        Transacao t = new Transacao();
        t.setIdContaOrigem(idContaOrigem);
        t.setTipoTransacao(TipoTransacao.TED);
        t.setValor(valor);
        t.setStatus(StatusTransacao.PROCESSADA);
        t.setIdentificadorExterno(numControle);
        t.setDescricao("TED para " + nomeBeneficiario);
        t.setJsonDadosExtras("{\"banco\":\"" + bancoDestino + "\", \"agencia\":\"" + agenciaDestino + 
                             "\", \"conta\":\"" + contaDestino + "\", \"cpf_cnpj\":\"" + cpfCnpjBeneficiario + "\"}");
        transacaoDAO.inserir(t);

        LogUtil.registrarLog(origem.getIdCliente(), "TED_ENVIADO", 
                "Valor: " + valor + " para " + nomeBeneficiario);
    }

    public void transferenciaPIX(int idContaOrigem, String chavePix, BigDecimal valor) throws Exception {
        Conta origem = contaDAO.buscarPorId(idContaOrigem);
        validarContaAtiva(origem);
        validarSaldoLimites(origem, valor, "PIX");

        // Simula envio ao SPI
        String endToEndId = simularSpix(chavePix, valor);

        // Atualiza saldo
        origem.setSaldo(origem.getSaldo().subtract(valor));
        contaDAO.atualizarSaldo(origem);
        new NotificacaoService().verificarEAlertarSaldoBaixo(
        origem.getIdCliente(), 
        origem.getIdConta(), 
        origem.getSaldo()
    );


        // Registra transação
        Transacao t = new Transacao();
        t.setIdContaOrigem(idContaOrigem);
        t.setTipoTransacao(TipoTransacao.PIX);
        t.setValor(valor);
        t.setStatus(StatusTransacao.PROCESSADA);
        t.setIdentificadorExterno(endToEndId);
        t.setDescricao("PIX para chave " + chavePix);
        transacaoDAO.inserir(t);

        LogUtil.registrarLog(origem.getIdCliente(), "PIX_ENVIADO", 
                "Valor: " + valor + " chave: " + chavePix);
    }

    public void agendarTransferencia(int idContaOrigem, String tipo, String dadosDestino,
                                     BigDecimal valor, LocalDate dataAgendada) throws Exception {
        if (dataAgendada.isBefore(LocalDate.now())) {
            throw new Exception("Data de agendamento deve ser futura");
        }

        Conta origem = contaDAO.buscarPorId(idContaOrigem);
        validarContaAtiva(origem);

        if (!(tipo.equals("TED") || tipo.equals("PIX"))) {
            throw new Exception("Tipo de transferência inválido. Use TED ou PIX");
        }

        AgendamentoTransferencia agendamento = new AgendamentoTransferencia();
        agendamento.setIdContaOrigem(idContaOrigem);
        agendamento.setTipoTransferencia(tipo);
        agendamento.setDadosDestino(dadosDestino);
        agendamento.setValor(valor);
        agendamento.setDataAgendada(dataAgendada);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamentoDAO.inserir(agendamento);

        LogUtil.registrarLog(origem.getIdCliente(), "AGENDAMENTO_CRIADO", 
                "Tipo: " + tipo + " valor: " + valor + " data: " + dataAgendada);
    }

    // Método para processar agendamentos
    public void processarAgendamentos(LocalDate data) throws Exception {
        List<AgendamentoTransferencia> agendamentos = agendamentoDAO.listarPendentesPorData(data);
        for (AgendamentoTransferencia ag : agendamentos) {
            try {
                Conta origem = contaDAO.buscarPorId(ag.getIdContaOrigem());
                if (origem.getSaldo().compareTo(ag.getValor()) < 0) {
                    ag.setStatus(StatusAgendamento.FALHA);
                    agendamentoDAO.atualizar(ag);
                    continue;
                }

                if ("TED".equals(ag.getTipoTransferencia())) {
                    
                    transferenciaTED(ag.getIdContaOrigem(), "001", "0001", "12345", "12345678901", 
                                     "Beneficiario", ag.getValor());
                } else {
                    // PIX
                    transferenciaPIX(ag.getIdContaOrigem(), "chave@email.com", ag.getValor());
                }

                ag.setStatus(StatusAgendamento.EXECUTADO);
                agendamentoDAO.atualizar(ag);
            } catch (Exception e) {
                ag.setStatus(StatusAgendamento.FALHA);
                agendamentoDAO.atualizar(ag);
            }
        }
    }

    private void validarContaAtiva(Conta conta) throws Exception {
        if (conta == null) throw new Exception("Conta não encontrada");
        if (conta.getStatus() != StatusConta.ATIVA) {
            throw new Exception("Conta inativa");
        }
    }

    private void validarContasAtivas(Conta origem, Conta destino) throws Exception {
        validarContaAtiva(origem);
        validarContaAtiva(destino);
    }
    
    public List<AgendamentoTransferencia> listarAgendamentosPorCliente(int idCliente) throws SQLException {
        return agendamentoDAO.listarPorCliente(idCliente);
    }

    private void validarSaldoLimites(Conta conta, BigDecimal valor, String tipo) throws Exception {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new Exception("Saldo insuficiente");
        }

        BigDecimal limite = "TED".equals(tipo) ? conta.getLimiteDiarioTed() : 
                           ("PIX".equals(tipo) ? conta.getLimiteDiarioPix() : null);
        if (limite != null && valor.compareTo(limite) > 0) {
            throw new Exception("Valor excede o limite diário para " + tipo);
        }
    }
}
