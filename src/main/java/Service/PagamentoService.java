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
import com.mycompany.yashin.model.FaturaCartao;
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.StatusConta;
import com.mycompany.yashin.model.enums.StatusTransacao;
import com.mycompany.yashin.model.enums.TipoTransacao;
import dao.ContaDAO;
import dao.FaturaDAO;
import dao.TransacaoDAO;

import java.math.BigDecimal;
import java.time.LocalDate;
import util.LogUtil;

public class PagamentoService {
    private ContaDAO contaDAO = new ContaDAO();
    private TransacaoDAO transacaoDAO = new TransacaoDAO();
    private FaturaDAO faturaDAO = new FaturaDAO();

    public void pagarConta(int idContaOrigem, String linhaDigitavel, BigDecimal valor) throws Exception {
        Conta origem = contaDAO.buscarPorId(idContaOrigem);
        validarContaAtiva(origem);
        validarSaldo(origem, valor);

        // Simula interpretação da linha digitável
        if (linhaDigitavel == null || linhaDigitavel.length() < 10) {
            throw new Exception("Linha digitável inválida");
        }

        debitarEProcessar(origem, valor, TipoTransacao.PAGAMENTO_CONTA, 
                         "Pagamento de conta: " + linhaDigitavel.substring(0, 10) + "...");
    }

    public void pagarFaturaCartao(int idContaOrigem, int idFatura) throws Exception {
        Conta origem = contaDAO.buscarPorId(idContaOrigem);
        validarContaAtiva(origem);

        FaturaCartao fatura = faturaDAO.buscarPorId(idFatura);
        if (fatura == null) throw new Exception("Fatura não encontrada");
        if (!"ABERTA".equals(fatura.getStatus())) {
            throw new Exception("Fatura já foi paga ou está vencida");
        }

        BigDecimal valor = fatura.getValorTotal().subtract(fatura.getValorPago());
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Fatura já quitada");
        }

        validarSaldo(origem, valor);

        // Atualiza fatura
        fatura.setValorPago(fatura.getValorTotal());
        fatura.setDataPagamento(LocalDate.now());
        fatura.setStatus("PAGA");
        faturaDAO.atualizar(fatura);

        // Débito e registro
        debitarEProcessar(origem, valor, TipoTransacao.PAGAMENTO_CARTAO, 
                         "Pagamento fatura cartão #" + idFatura);
    }

    public void pagarPIXQRCode(int idContaOrigem, String qrCode, BigDecimal valor) throws Exception {
        Conta origem = contaDAO.buscarPorId(idContaOrigem);
        validarContaAtiva(origem);
        validarSaldo(origem, valor);

        // Simula leitura do QR Code e envio via SPI
        String payload = "QRCODE_" + System.currentTimeMillis();

        debitarEProcessar(origem, valor, TipoTransacao.PAGAMENTO_IMPOSTO, 
                         "Pagamento PIX QR Code: " + qrCode.substring(0, 10) + "...");
    }

    private void debitarEProcessar(Conta conta, BigDecimal valor, TipoTransacao tipo, String descricao) throws Exception {
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaDAO.atualizarSaldo(conta);

        Transacao t = new Transacao();
        t.setIdContaOrigem(conta.getIdConta());
        t.setTipoTransacao(tipo);
        t.setValor(valor);
        t.setStatus(StatusTransacao.PROCESSADA);
        t.setDescricao(descricao);
        transacaoDAO.inserir(t);

        LogUtil.registrarLog(conta.getIdCliente(), "PAGAMENTO_REALIZADO", 
                "Tipo: " + tipo + " valor: " + valor);
    }

    private void validarContaAtiva(Conta conta) throws Exception {
        if (conta == null) throw new Exception("Conta não encontrada");
        if (conta.getStatus() != StatusConta.ATIVA) {
            throw new Exception("Conta inativa");
        }
    }

    private void validarSaldo(Conta conta, BigDecimal valor) throws Exception {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new Exception("Saldo insuficiente");
        }
    }
}