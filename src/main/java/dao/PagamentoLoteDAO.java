/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.PagamentoLote;
import com.mycompany.yashin.model.enums.StatusLote;


import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class PagamentoLoteDAO {

    public void inserir(PagamentoLote lote) throws SQLException {
        String sql = "INSERT INTO pagamento_lote (id_cliente_pj, tipo, data_execucao, status, valor_total) VALUES (?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lote.getIdClientePj());
            ps.setString(2, lote.getTipo());
            ps.setDate(3, lote.getDataExecucao() != null ? Date.valueOf(lote.getDataExecucao()) : null);
            ps.setString(4, lote.getStatus().name());
            ps.setBigDecimal(5, lote.getValorTotal());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    lote.setIdLote(rs.getInt(1));
                }
            }
        }
    }

    public PagamentoLote buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM pagamento_lote WHERE id_lote = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearLote(rs);
                }
            }
        }
        return null;
    }

    public List<PagamentoLote> listarPorCliente(int idClientePj) throws SQLException {
        List<PagamentoLote> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagamento_lote WHERE id_cliente_pj = ? ORDER BY data_criacao DESC";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idClientePj);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearLote(rs));
                }
            }
        }
        return lista;
    }

    public List<PagamentoLote> listarAgendados(LocalDate dataExecucao) throws SQLException {
        List<PagamentoLote> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagamento_lote WHERE data_execucao = ? AND status = 'AGENDADO'";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(dataExecucao));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearLote(rs));
                }
            }
        }
        return lista;
    }

    public void atualizar(PagamentoLote lote) throws SQLException {
        String sql = "UPDATE pagamento_lote SET id_cliente_pj=?, tipo=?, data_execucao=?, status=?, valor_total=? WHERE id_lote=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lote.getIdClientePj());
            ps.setString(2, lote.getTipo());
            ps.setDate(3, lote.getDataExecucao() != null ? Date.valueOf(lote.getDataExecucao()) : null);
            ps.setString(4, lote.getStatus().name());
            ps.setBigDecimal(5, lote.getValorTotal());
            ps.setInt(6, lote.getIdLote());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM pagamento_lote WHERE id_lote = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private PagamentoLote mapearLote(ResultSet rs) throws SQLException {
        PagamentoLote l = new PagamentoLote();
        l.setIdLote(rs.getInt("id_lote"));
        l.setIdClientePj(rs.getInt("id_cliente_pj"));
        Timestamp ts = rs.getTimestamp("data_criacao");
        l.setDataCriacao(ts != null ? ts.toLocalDateTime() : null);
        l.setTipo(rs.getString("tipo"));
        Date de = rs.getDate("data_execucao");
        l.setDataExecucao(de != null ? de.toLocalDate() : null);
        l.setStatus(StatusLote.valueOf(rs.getString("status")));
        l.setValorTotal(rs.getBigDecimal("valor_total"));
        return l;
    }
}