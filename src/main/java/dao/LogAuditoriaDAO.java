/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.LogAuditoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class LogAuditoriaDAO {

    public void inserir(LogAuditoria log) throws SQLException {
        String sql = "INSERT INTO logs_auditoria (id_cliente, acao, descricao, ip_origem) VALUES (?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, log.getIdCliente());
            ps.setString(2, log.getAcao());
            ps.setString(3, log.getDescricao());
            ps.setString(4, log.getIpOrigem());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setIdLog(rs.getInt(1));
                }
            }
        }
    }

    public List<LogAuditoria> listarPorCliente(int idCliente) throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM logs_auditoria WHERE id_cliente = ? ORDER BY data_hora DESC";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLog(rs));
                }
            }
        }
        return logs;
    }

    public List<LogAuditoria> listarPorPeriodo(Timestamp inicio, Timestamp fim) throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM logs_auditoria WHERE data_hora BETWEEN ? AND ? ORDER BY data_hora DESC";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, inicio);
            ps.setTimestamp(2, fim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLog(rs));
                }
            }
        }
        return logs;
    }

    private LogAuditoria mapearLog(ResultSet rs) throws SQLException {
        LogAuditoria log = new LogAuditoria();
        log.setIdLog(rs.getInt("id_log"));
        log.setIdCliente(rs.getInt("id_cliente") != 0 ? rs.getInt("id_cliente") : null);
        log.setAcao(rs.getString("acao"));
        log.setDescricao(rs.getString("descricao"));
        log.setIpOrigem(rs.getString("ip_origem"));
        Timestamp ts = rs.getTimestamp("data_hora");
        log.setDataHora(ts != null ? ts.toLocalDateTime() : null);
        return log;
    }
}
