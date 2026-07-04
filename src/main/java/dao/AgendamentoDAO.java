/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.AgendamentoTransferencia;
import com.mycompany.yashin.model.enums.StatusAgendamento;


import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class AgendamentoDAO {

    public void inserir(AgendamentoTransferencia agendamento) throws SQLException {
        String sql = "INSERT INTO agendamento_transferencia (id_conta_origem, tipo_transferencia, dados_destino, valor, data_agendada, status) VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, agendamento.getIdContaOrigem());
            ps.setString(2, agendamento.getTipoTransferencia());
            ps.setString(3, agendamento.getDadosDestino());
            ps.setBigDecimal(4, agendamento.getValor());
            ps.setDate(5, Date.valueOf(agendamento.getDataAgendada()));
            ps.setString(6, agendamento.getStatus().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    agendamento.setIdAgendamento(rs.getInt(1));
                }
            }
        }
    }

    public AgendamentoTransferencia buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM agendamento_transferencia WHERE id_agendamento = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAgendamento(rs);
                }
            }
        }
        return null;
    }

    public List<AgendamentoTransferencia> listarPorConta(int idConta) throws SQLException {
        List<AgendamentoTransferencia> lista = new ArrayList<>();
        String sql = "SELECT * FROM agendamento_transferencia WHERE id_conta_origem = ? ORDER BY data_agendada";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idConta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgendamento(rs));
                }
            }
        }
        return lista;
    }

    public List<AgendamentoTransferencia> listarPendentesPorData(LocalDate data) throws SQLException {
        List<AgendamentoTransferencia> lista = new ArrayList<>();
        String sql = "SELECT * FROM agendamento_transferencia WHERE data_agendada = ? AND status = 'AGENDADO'";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgendamento(rs));
                }
            }
        }
        return lista;
    }
    
        public List<AgendamentoTransferencia> listarPorCliente(int idCliente) throws SQLException {
        List<AgendamentoTransferencia> lista = new ArrayList<>();
        String sql = "SELECT a.* FROM agendamento_transferencia a " +
                     "JOIN conta c ON a.id_conta_origem = c.id_conta " +
                     "WHERE c.id_cliente = ? ORDER BY a.data_agendada";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAgendamento(rs));
                }
            }
        }
        return lista;
    }

    public void atualizar(AgendamentoTransferencia agendamento) throws SQLException {
        String sql = "UPDATE agendamento_transferencia SET id_conta_origem=?, tipo_transferencia=?, dados_destino=?, valor=?, data_agendada=?, status=?, id_transacao_executada=? WHERE id_agendamento=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agendamento.getIdContaOrigem());
            ps.setString(2, agendamento.getTipoTransferencia());
            ps.setString(3, agendamento.getDadosDestino());
            ps.setBigDecimal(4, agendamento.getValor());
            ps.setDate(5, Date.valueOf(agendamento.getDataAgendada()));
            ps.setString(6, agendamento.getStatus().name());
            ps.setObject(7, agendamento.getIdTransacaoExecutada());
            ps.setInt(8, agendamento.getIdAgendamento());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM agendamento_transferencia WHERE id_agendamento = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private AgendamentoTransferencia mapearAgendamento(ResultSet rs) throws SQLException {
        AgendamentoTransferencia a = new AgendamentoTransferencia();
        a.setIdAgendamento(rs.getInt("id_agendamento"));
        a.setIdContaOrigem(rs.getInt("id_conta_origem"));
        a.setTipoTransferencia(rs.getString("tipo_transferencia"));
        a.setDadosDestino(rs.getString("dados_destino"));
        a.setValor(rs.getBigDecimal("valor"));
        a.setDataAgendada(rs.getDate("data_agendada").toLocalDate());
        Timestamp ts = rs.getTimestamp("data_criacao");
        a.setDataCriacao(ts != null ? ts.toLocalDateTime() : null);
        a.setStatus(StatusAgendamento.valueOf(rs.getString("status")));
        a.setIdTransacaoExecutada(rs.getInt("id_transacao_executada") != 0 ? rs.getInt("id_transacao_executada") : null);
        return a;
    }
}
