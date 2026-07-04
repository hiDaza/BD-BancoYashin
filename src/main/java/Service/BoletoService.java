/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.BoletoEmitido;
import com.mycompany.yashin.model.enums.StatusBoleto;

import dao.BoletoDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import util.LogUtil;

public class BoletoService {
    private BoletoDAO boletoDAO = new BoletoDAO();

    public BoletoEmitido emitirBoleto(int idClientePj, BigDecimal valor, LocalDate vencimento,
                                      BigDecimal jurosDia, BigDecimal multa, String instrucoes) throws Exception {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Valor deve ser positivo");
        if (vencimento.isBefore(LocalDate.now())) throw new Exception("Vencimento deve ser futuro");

        BoletoEmitido boleto = new BoletoEmitido();
        boleto.setIdClientePj(idClientePj);
        boleto.setNossoNumero(gerarNossoNumero());
        boleto.setValor(valor);
        boleto.setDataVencimento(vencimento);
        boleto.setDataEmissao(LocalDateTime.now());
        boleto.setJurosDia(jurosDia != null ? jurosDia : BigDecimal.ZERO);
        boleto.setMulta(multa != null ? multa : BigDecimal.ZERO);
        boleto.setInstrucoes(instrucoes);
        boleto.setCodigoBarras(gerarCodigoBarras());
        boleto.setStatus(StatusBoleto.ATIVO);

        boletoDAO.inserir(boleto);
        LogUtil.registrarLog(idClientePj, "BOLETO_EMITIDO", 
                "Boleto: " + boleto.getNossoNumero() + " valor: " + valor);
        return boleto;
    }

    private String gerarNossoNumero() {
        return String.format("%015d", System.currentTimeMillis() % 1000000000000000L);
    }

    private String gerarCodigoBarras() {
        Random r = new Random();
        return String.format("%044d", Math.abs(r.nextLong()) % 100000000L);
    }

    public void baixarBoleto(String nossoNumero, int idTransacaoPagamento) throws Exception {
        BoletoEmitido boleto = boletoDAO.buscarPorNossoNumero(nossoNumero);
        if (boleto == null) throw new Exception("Boleto não encontrado");
        if (boleto.getStatus() != StatusBoleto.ATIVO) {
            throw new Exception("Boleto já foi baixado ou cancelado");
        }
        boleto.setStatus(StatusBoleto.PAGO);
        boleto.setIdTransacaoPagamento(idTransacaoPagamento);
        boletoDAO.atualizar(boleto);
    }

    public void cancelarBoleto(int idBoleto) throws Exception {
        BoletoEmitido boleto = boletoDAO.buscarPorId(idBoleto);
        if (boleto == null) throw new Exception("Boleto não encontrado");
        if (boleto.getStatus() == StatusBoleto.PAGO) {
            throw new Exception("Não é possível cancelar um boleto já pago");
        }
        boleto.setStatus(StatusBoleto.CANCELADO);
        boletoDAO.atualizar(boleto);
    }

    public BoletoEmitido buscarPorId(int id) throws SQLException {
        return boletoDAO.buscarPorId(id);
    }

    public List<BoletoEmitido> listarPorCliente(int idClientePj) throws SQLException {
        return boletoDAO.listarPorCliente(idClientePj);
    }

    public List<BoletoEmitido> listarAtivos(int idClientePj) throws SQLException {
        return boletoDAO.listarPorStatus(idClientePj, StatusBoleto.ATIVO);
    }
}
