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
import com.mycompany.yashin.model.PreferenciaAlertas;
import com.mycompany.yashin.model.Transacao;
import dao.ContaDAO;
import dao.PreferenciaDAO;
import dao.TransacaoDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import util.LogUtil;

public class NotificacaoService {
    private PreferenciaDAO preferenciaDAO = new PreferenciaDAO();
    private ContaDAO contaDAO = new ContaDAO();
    private TransacaoDAO transacaoDAO = new TransacaoDAO();

    public void configurarPreferencias(PreferenciaAlertas preferencias) throws Exception {
        if (preferencias.getIdCliente() == null) throw new Exception("Cliente não identificado");

        PreferenciaAlertas existente = preferenciaDAO.buscarPorCliente(preferencias.getIdCliente());
        if (existente != null) {
            preferencias.setIdPreferencia(existente.getIdPreferencia());
            preferenciaDAO.atualizar(preferencias);
        } else {
            preferenciaDAO.inserir(preferencias);
        }
        LogUtil.registrarLog(preferencias.getIdCliente(), "ALERTAS_CONFIGURADOS", 
                "Preferências salvas");
    }

    public PreferenciaAlertas buscarPreferencias(int idCliente) throws SQLException {
        return preferenciaDAO.buscarPorCliente(idCliente);
    }

    public void verificarAlertasSaldoBaixo(int idCliente, int idConta) throws Exception {
        PreferenciaAlertas pref = preferenciaDAO.buscarPorCliente(idCliente);
        if (pref == null || !pref.getAlertaSaldoBaixo()) return;

        Conta conta = contaDAO.buscarPorId(idConta);
        if (conta == null) return;

        BigDecimal limite = pref.getValorLimiteSaldo();
        if (limite != null && conta.getSaldo().compareTo(limite) < 0) {
            String mensagem = "ALERTA: Seu saldo (R$ " + conta.getSaldo() + ") está abaixo do limite (R$ " + limite + ")";
            enviarNotificacao(pref, mensagem);
        }
    }

    public void verificarVencimentoContas(int idCliente, LocalDateTime dataLimite) throws Exception {
        PreferenciaAlertas pref = preferenciaDAO.buscarPorCliente(idCliente);
        if (pref == null || !pref.getAlertaVencimentoConta()) return;

        // Busca transações pendentes (exemplo: contas a vencer nos próximos 3 dias)
        List<Transacao> transacoes = transacaoDAO.listarPorCliente(idCliente, null, null);
        // Lógica simplificada - na vida real verificar faturas e boletos
        String mensagem = "ALERTA: Você possui contas a vencer nos próximos dias.";
        enviarNotificacao(pref, mensagem);
    }

    public void enviarExtratoDisponivel(int idCliente) throws Exception {
        PreferenciaAlertas pref = preferenciaDAO.buscarPorCliente(idCliente);
        if (pref == null || !pref.getAlertaExtratoDisponivel()) return;
        String mensagem = "Seu extrato mensal está disponível para download.";
        enviarNotificacao(pref, mensagem);
    }

    private void enviarNotificacao(PreferenciaAlertas pref, String mensagem) {
        // Simulação de envio pelos canais configurados
        if (pref.getCanalEmail()) {
            System.out.println("[EMAIL] Para cliente " + pref.getIdCliente() + ": " + mensagem);
        }
        if (pref.getCanalSms()) {
            System.out.println("[SMS] Para cliente " + pref.getIdCliente() + ": " + mensagem);
        }
        if (pref.getCanalPush()) {
            System.out.println("[PUSH] Para cliente " + pref.getIdCliente() + ": " + mensagem);
        }

        try {
            LogUtil.registrarLog(pref.getIdCliente(), "NOTIFICACAO_ENVIADA", mensagem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}