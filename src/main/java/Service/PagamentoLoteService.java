/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.PagamentoLote;
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.StatusLote;
import com.mycompany.yashin.model.enums.StatusTransacao;
import com.mycompany.yashin.model.enums.TipoTransacao;

import dao.ContaDAO;
import dao.PagamentoLoteDAO;
import dao.TransacaoDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import util.LogUtil;

public class PagamentoLoteService {
    private PagamentoLoteDAO loteDAO = new PagamentoLoteDAO();
    private ContaDAO contaDAO = new ContaDAO();
    private TransacaoDAO transacaoDAO = new TransacaoDAO();

    private Map<Integer, List<ItemLote>> itensPorLote = new HashMap<>();

    private static class ItemLote {
        String beneficiario;
        String chavePix;
        String dadosTedJson;
        BigDecimal valor;
        String status; // PENDENTE, PROCESSADO, FALHA
        Integer idTransacao;
    }

    public int criarLote(int idClientePj, String tipo, LocalDate dataExecucao) throws Exception {
        if (!"FORNECEDORES".equals(tipo) && !"FOLHA".equals(tipo)) {
            throw new Exception("Tipo de lote inválido");
        }
        PagamentoLote lote = new PagamentoLote(idClientePj, tipo, dataExecucao);
        lote.setStatus(StatusLote.AGENDADO);
        loteDAO.inserir(lote);
        itensPorLote.put(lote.getIdLote(), new ArrayList<>());
        return lote.getIdLote();
    }

    public void adicionarItemLote(int idLote, String beneficiario, String chavePix, String dadosTedJson, BigDecimal valor) throws Exception {
        PagamentoLote lote = loteDAO.buscarPorId(idLote);
        if (lote == null) throw new Exception("Lote não encontrado");
        if (lote.getStatus() != StatusLote.AGENDADO) {
            throw new Exception("Lote não está em status AGENDADO");
        }
        if (chavePix == null && dadosTedJson == null) {
            throw new Exception("Informe chave PIX ou dados TED");
        }

        ItemLote item = new ItemLote();
        item.beneficiario = beneficiario;
        item.chavePix = chavePix;
        item.dadosTedJson = dadosTedJson;
        item.valor = valor;
        item.status = "PENDENTE";
        itensPorLote.get(idLote).add(item);

        // Atualiza valor total do lote
        lote.setValorTotal(lote.getValorTotal().add(valor));
        loteDAO.atualizar(lote);
    }

    public void executarLote(int idLote) throws Exception {
        PagamentoLote lote = loteDAO.buscarPorId(idLote);
        if (lote == null) throw new Exception("Lote não encontrado");
        if (lote.getStatus() != StatusLote.AGENDADO) {
            throw new Exception("Lote já executado ou cancelado");
        }

        // Busca conta do cliente PJ
        List<Conta> contas = new ContaService().buscarPorCliente(lote.getIdClientePj());
        if (contas == null || contas.isEmpty()) {
            throw new Exception("Cliente não possui conta ativa");
        }
        Conta contaOrigem = contas.get(0);

        BigDecimal total = lote.getValorTotal();
        if (contaOrigem.getSaldo().compareTo(total) < 0) {
            throw new Exception("Saldo insuficiente para executar o lote");
        }

        // Processa cada item
        List<ItemLote> itens = itensPorLote.get(idLote);
        if (itens == null) throw new Exception("Lote sem itens");

        for (ItemLote item : itens) {
            try {
                contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(item.valor));
                contaDAO.atualizarSaldo(contaOrigem);

                Transacao t = new Transacao();
                t.setIdContaOrigem(contaOrigem.getIdConta());
                t.setTipoTransacao(TipoTransacao.OUTROS);
                t.setValor(item.valor);
                t.setStatus(StatusTransacao.PROCESSADA);
                t.setDescricao("Lote #" + idLote + " - " + item.beneficiario);
                transacaoDAO.inserir(t);

                item.status = "PROCESSADO";
                item.idTransacao = t.getIdTransacao();
            } catch (Exception e) {
                item.status = "FALHA";
            }
        }

        lote.setStatus(StatusLote.EXECUTADO);
        loteDAO.atualizar(lote);

        LogUtil.registrarLog(lote.getIdClientePj(), "LOTE_EXECUTADO", 
                "Lote #" + idLote + " total: " + total);
    }

    public List<PagamentoLote> listarLotesPorCliente(int idClientePj) throws SQLException {
        return loteDAO.listarPorCliente(idClientePj);
    }

    public PagamentoLote buscarLotePorId(int id) throws SQLException {
        return loteDAO.buscarPorId(id);
    }
    
    public List<ItemLote> listarItensLote(int idLote) {
        return itensPorLote.getOrDefault(idLote, new ArrayList<>());
    }
}