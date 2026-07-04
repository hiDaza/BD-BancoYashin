/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.TipoTransacao;
import dao.TransacaoDAO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import util.LogUtil;

public class RelatorioService {
    private TransacaoDAO transacaoDAO = new TransacaoDAO();

    public String gerarComprovante(int idTransacao) throws Exception {
        Transacao t = transacaoDAO.buscarPorId(idTransacao);
        if (t == null) throw new Exception("Transação não encontrada");

        StringBuilder sb = new StringBuilder();
        sb.append("=== COMPROVANTE DE TRANSAÇÃO ===\n");
        sb.append("ID: ").append(t.getIdTransacao()).append("\n");
        sb.append("Tipo: ").append(t.getTipoTransacao()).append("\n");
        sb.append("Valor: R$ ").append(t.getValor()).append("\n");
        sb.append("Data: ").append(t.getDataHora()).append("\n");
        sb.append("Status: ").append(t.getStatus()).append("\n");
        if (t.getIdContaOrigem() != null) sb.append("Conta Origem: ").append(t.getIdContaOrigem()).append("\n");
        if (t.getIdContaDestino() != null) sb.append("Conta Destino: ").append(t.getIdContaDestino()).append("\n");
        if (t.getIdentificadorExterno() != null) sb.append("Identificador: ").append(t.getIdentificadorExterno()).append("\n");
        sb.append("Descrição: ").append(t.getDescricao()).append("\n");
        sb.append("==================================\n");
        return sb.toString();
    }

    public String gerarExtratoPDF(int idConta, LocalDateTime inicio, LocalDateTime fim) throws Exception {
        List<Transacao> transacoes = transacaoDAO.listarPorConta(idConta, inicio, fim);

        StringBuilder sb = new StringBuilder();
        sb.append("=== EXTRATO BANCÁRIO ===\n");
        sb.append("Conta: ").append(idConta).append("\n");
        sb.append("Período: ").append(inicio).append(" a ").append(fim).append("\n");
        sb.append("------------------------------\n");

        BigDecimal saldoTotal = BigDecimal.ZERO;
        for (Transacao t : transacoes) {
            sb.append(t.getDataHora()).append(" | ")
              .append(String.format("%-20s", t.getTipoTransacao())).append(" | ")
              .append("R$ ").append(t.getValor()).append(" | ")
              .append(t.getDescricao()).append("\n");
            // Créditos aumentam, débitos diminuem
            if (t.getTipoTransacao() == TipoTransacao.EMPRESTIMO ||
                (t.getIdContaDestino() != null && t.getIdContaDestino().equals(idConta) && t.getIdContaOrigem() != null)) {
                saldoTotal = saldoTotal.add(t.getValor());
            } else if (t.getIdContaOrigem() != null && t.getIdContaOrigem().equals(idConta)) {
                saldoTotal = saldoTotal.subtract(t.getValor());
            }
        }
        sb.append("------------------------------\n");
        sb.append("SALDO FINAL: R$ ").append(saldoTotal).append("\n");
        sb.append("================================\n");
        return sb.toString();
    }

    public void imprimirComprovante(int idTransacao) throws Exception {
        String comprovante = gerarComprovante(idTransacao);
        System.out.println(comprovante);
        // Em produção, salvaria em arquivo ou enviaria por e-mail
        LogUtil.registrarLog(null, "COMPROVANTE_EMITIDO", "Transação: " + idTransacao);
    }
}
