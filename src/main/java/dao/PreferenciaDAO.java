/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.PreferenciaAlertas;

import java.sql.*;
import util.ConexaoBD;

public class PreferenciaDAO {

    public void inserir(PreferenciaAlertas pref) throws SQLException {
        String sql = "INSERT INTO preferencias_alertas (id_cliente, alerta_saldo_baixo, valor_limite_saldo, alerta_vencimento_conta, alerta_extrato_disponivel, canal_email, canal_sms, canal_push) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pref.getIdCliente());
            ps.setBoolean(2, pref.getAlertaSaldoBaixo());
            ps.setBigDecimal(3, pref.getValorLimiteSaldo());
            ps.setBoolean(4, pref.getAlertaVencimentoConta());
            ps.setBoolean(5, pref.getAlertaExtratoDisponivel());
            ps.setBoolean(6, pref.getCanalEmail());
            ps.setBoolean(7, pref.getCanalSms());
            ps.setBoolean(8, pref.getCanalPush());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pref.setIdPreferencia(rs.getInt(1));
                }
            }
        }
    }

    public PreferenciaAlertas buscarPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT * FROM preferencias_alertas WHERE id_cliente = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPreferencia(rs);
                }
            }
        }
        return null;
    }

    public void atualizar(PreferenciaAlertas pref) throws SQLException {
        String sql = "UPDATE preferencias_alertas SET alerta_saldo_baixo=?, valor_limite_saldo=?, alerta_vencimento_conta=?, alerta_extrato_disponivel=?, canal_email=?, canal_sms=?, canal_push=? WHERE id_preferencia=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, pref.getAlertaSaldoBaixo());
            ps.setBigDecimal(2, pref.getValorLimiteSaldo());
            ps.setBoolean(3, pref.getAlertaVencimentoConta());
            ps.setBoolean(4, pref.getAlertaExtratoDisponivel());
            ps.setBoolean(5, pref.getCanalEmail());
            ps.setBoolean(6, pref.getCanalSms());
            ps.setBoolean(7, pref.getCanalPush());
            ps.setInt(8, pref.getIdPreferencia());
            ps.executeUpdate();
        }
    }

    public void deletar(int idPreferencia) throws SQLException {
        String sql = "DELETE FROM preferencias_alertas WHERE id_preferencia = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPreferencia);
            ps.executeUpdate();
        }
    }

    private PreferenciaAlertas mapearPreferencia(ResultSet rs) throws SQLException {
        PreferenciaAlertas p = new PreferenciaAlertas();
        p.setIdPreferencia(rs.getInt("id_preferencia"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setAlertaSaldoBaixo(rs.getBoolean("alerta_saldo_baixo"));
        p.setValorLimiteSaldo(rs.getBigDecimal("valor_limite_saldo"));
        p.setAlertaVencimentoConta(rs.getBoolean("alerta_vencimento_conta"));
        p.setAlertaExtratoDisponivel(rs.getBoolean("alerta_extrato_disponivel"));
        p.setCanalEmail(rs.getBoolean("canal_email"));
        p.setCanalSms(rs.getBoolean("canal_sms"));
        p.setCanalPush(rs.getBoolean("canal_push"));
        return p;
    }
}
