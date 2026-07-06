/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.CartaoCredito;
import com.mycompany.yashin.model.CompraFatura;
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.FaturaCartao;

import dao.CartaoDAO;
import dao.CompraFaturaDAO;
import dao.ContaDAO;
import dao.FaturaDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import util.LogUtil;

public class CartaoService {
    private CartaoDAO cartaoDAO = new CartaoDAO();
    private FaturaDAO faturaDAO = new FaturaDAO();
    private ContaDAO contaDAO = new ContaDAO();

    public CartaoCredito solicitarCartaoPessoal(int idConta, BigDecimal limite, String bandeira) throws Exception {
        Conta conta = contaDAO.buscarPorId(idConta);
        if (conta == null) throw new Exception("Conta não encontrada");
        if (limite.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Limite deve ser positivo");

        CartaoCredito cartao = criarCartao(conta, limite, bandeira, "PESSOAL", null);
        cartaoDAO.inserir(cartao);
        LogUtil.registrarLog(conta.getIdCliente(), "CARTAO_SOLICITADO", 
                "Cartão pessoal: " + cartao.getNumeroCartao());
        return cartao;
    }

    public CartaoCredito solicitarCartaoCorporativo(int idConta, BigDecimal limite, String bandeira,
                                                    int idClientePortador) throws Exception {
        Conta conta = contaDAO.buscarPorId(idConta);
        if (conta == null) throw new Exception("Conta não encontrada");
        if (limite.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("Limite deve ser positivo");

        CartaoCredito cartao = criarCartao(conta, limite, bandeira, "CORPORATIVO", idClientePortador);
        cartaoDAO.inserir(cartao);
        LogUtil.registrarLog(conta.getIdCliente(), "CARTAO_CORPORATIVO_SOLICITADO", 
                "Cartão corporativo: " + cartao.getNumeroCartao());
        return cartao;
    }

    private CartaoCredito criarCartao(Conta conta, BigDecimal limite, String bandeira, String tipo, Integer idPortador) {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setIdConta(conta.getIdConta());
        cartao.setNumeroCartao(gerarNumeroCartao());
        cartao.setBandeira(bandeira);
        cartao.setLimiteTotal(limite);
        cartao.setLimiteUtilizado(BigDecimal.ZERO);
        cartao.setDataValidade(LocalDate.now().plusYears(3));
        cartao.setCodigoSeguranca(String.format("%03d", new Random().nextInt(1000)));
        cartao.setStatus("ATIVO");
        cartao.setTipo(tipo);
        cartao.setIdClientePortador(idPortador);
        return cartao;
    }

    private String gerarNumeroCartao() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) sb.append(" ");
            sb.append(String.format("%04d", r.nextInt(10000)));
        }
        return sb.toString();
    }

    public List<CartaoCredito> listarCartoesPorConta(int idConta) throws SQLException {
        return cartaoDAO.listarPorConta(idConta);
    }

    public List<FaturaCartao> listarFaturas(int idCartao) throws SQLException {
        return faturaDAO.listarPorCartao(idCartao);
    }
    
    public List<CartaoCredito> listarCartoesPorCliente(int idCliente) throws SQLException {
        // Buscar todas as contas do cliente e depois os cartões
        List<CartaoCredito> todos = new ArrayList<>();
        for (Conta c : new ContaService().buscarPorCliente(idCliente)) {
            todos.addAll(cartaoDAO.listarPorConta(c.getIdConta()));
        }
        return todos;
    }

    public List<FaturaCartao> listarFaturasPorCliente(int idCliente) throws SQLException {
        return faturaDAO.listarPorCliente(idCliente);
    }

    public FaturaCartao buscarFaturaAberta(int idCartao, LocalDate mes) throws SQLException {
        return faturaDAO.buscarFaturaAberta(idCartao, mes);
    }
    
    public FaturaCartao buscarFaturaPorId(int idFatura) throws SQLException {
        return faturaDAO.buscarPorId(idFatura);
    }

    public void bloquearCartao(int idCartao) throws Exception {
        CartaoCredito cartao = cartaoDAO.buscarPorId(idCartao);
        if (cartao == null) throw new Exception("Cartão não encontrado");
        cartao.setStatus("BLOQUEADO");
        cartaoDAO.atualizar(cartao);
        LogUtil.registrarLog(null, "CARTAO_BLOQUEADO", "Cartão: " + cartao.getNumeroCartao());
    }

    public void desbloquearCartao(int idCartao) throws Exception {
        CartaoCredito cartao = cartaoDAO.buscarPorId(idCartao);
        if (cartao == null) throw new Exception("Cartão não encontrado");
        cartao.setStatus("ATIVO");
        cartaoDAO.atualizar(cartao);
        LogUtil.registrarLog(null, "CARTAO_DESBLOQUEADO", "Cartão: " + cartao.getNumeroCartao());
    }
    
    
    public void registrarCompra(int idCartao, BigDecimal valor, String descricao, int parcelas) throws Exception {
        CartaoCredito cartao = cartaoDAO.buscarPorId(idCartao);
        if (cartao == null) throw new Exception("Cartão não encontrado");
        if (!"ATIVO".equals(cartao.getStatus())) throw new Exception("Cartão inativo");

        BigDecimal novoLimiteUtilizado = cartao.getLimiteUtilizado().add(valor);
        if (novoLimiteUtilizado.compareTo(cartao.getLimiteTotal()) > 0) {
            throw new Exception("Limite do cartão excedido");
        }

        LocalDate mesReferencia = LocalDate.now().withDayOfMonth(1);
        FaturaCartao fatura = faturaDAO.buscarFaturaAberta(idCartao, mesReferencia);
        if (fatura == null) {
            fatura = new FaturaCartao();
            fatura.setIdCartao(idCartao);
            fatura.setMesReferencia(mesReferencia);
            fatura.setValorTotal(BigDecimal.ZERO);
            fatura.setValorPago(BigDecimal.ZERO);
            fatura.setDataVencimento(mesReferencia.plusMonths(1).minusDays(1));
            fatura.setStatus("ABERTA");
            faturaDAO.inserir(fatura);
        }

        // Atualizar fatura (total)
        fatura.setValorTotal(fatura.getValorTotal().add(valor));
        faturaDAO.atualizar(fatura);

        // Inserir compra na fatura
        CompraFatura compra = new CompraFatura();
        compra.setIdFatura(fatura.getIdFatura());
        compra.setDescricao(descricao);
        compra.setValor(valor);
        compra.setDataCompra(LocalDate.now());
        compra.setParcelas(parcelas > 1 ? parcelas : null); // se 1, não registra parcelamento
        new CompraFaturaDAO().inserir(compra);

        // Atualizar limite utilizado
        cartao.setLimiteUtilizado(novoLimiteUtilizado);
        cartaoDAO.atualizar(cartao);

        LogUtil.registrarLog(null, "COMPRA_CARTAO", "Cartão: " + cartao.getNumeroCartao() + 
                             " Valor: " + valor + " Parcelas: " + parcelas);
    }
    
    
    public boolean temFaturaEmAberto(int idCliente) throws SQLException {
        List<FaturaCartao> faturas = faturaDAO.listarPorCliente(idCliente);
        for (FaturaCartao f : faturas) {
            if ("ABERTA".equals(f.getStatus()) || "VENCIDA".equals(f.getStatus())) {
                return true;
            }
        }
        return false;
    }    
    
    
}
