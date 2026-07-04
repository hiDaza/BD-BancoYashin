/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.StatusTransacao;
import com.mycompany.yashin.model.enums.TipoTransacao;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class TransacaoDAO {

    public void inserir(Transacao transacao) throws SQLException {
        String sql = "INSERT INTO transacao (id_conta_origem, id_conta_destino, tipo_transacao, valor, status, descricao, identificador_externo, json_dados_extras) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, transacao.getIdContaOrigem());
            ps.setObject(2, transacao.getIdContaDestino());
            ps.setString(3, transacao.getTipoTransacao().name());
            ps.setBigDecimal(4, transacao.getValor());
            ps.setString(5, transacao.getStatus().name());
            ps.setString(6, transacao.getDescricao());
            ps.setString(7, transacao.getIdentificadorExterno());
            ps.setString(8, transacao.getJsonDadosExtras());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    transacao.setIdTransacao(rs.getInt(1));
                }
            }
        }
    }

    public Transacao buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM transacao WHERE id_transacao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearTransacao(rs);
                }
            }
        }
        return null;
    }

    public List<Transacao> listarPorConta(int idConta, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM transacao WHERE id_conta_origem = ? OR id_conta_destino = ?");
        if (inicio != null && fim != null) {
            sql.append(" AND data_hora BETWEEN ? AND ?");
        }
        sql.append(" ORDER BY data_hora DESC");
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, idConta);
            ps.setInt(2, idConta);
            int paramIndex = 3;
            if (inicio != null && fim != null) {
                ps.setTimestamp(paramIndex++, Timestamp.valueOf(inicio));
                ps.setTimestamp(paramIndex, Timestamp.valueOf(fim));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transacoes.add(mapearTransacao(rs));
                }
            }
        }
        return transacoes;
    }

    public List<Transacao> listarPorCliente(int idCliente, LocalDateTime inicio, LocalDateTime fim) throws SQLException {
        // Busca transações onde a conta de origem ou destino pertence ao cliente (via contas do cliente)
        String sql = "SELECT t.* FROM transacao t " +
                     "JOIN conta c ON (t.id_conta_origem = c.id_conta OR t.id_conta_destino = c.id_conta) " +
                     "WHERE c.id_cliente = ? ";
        if (inicio != null && fim != null) {
            sql += " AND t.data_hora BETWEEN ? AND ? ";
        }
        sql += " ORDER BY t.data_hora DESC";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            int idx = 2;
            if (inicio != null && fim != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(inicio));
                ps.setTimestamp(idx, Timestamp.valueOf(fim));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Transacao> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapearTransacao(rs));
                }
                return lista;
            }
        }
    }

    public void atualizarStatus(int idTransacao, StatusTransacao novoStatus, String identificadorExterno) throws SQLException {
        String sql = "UPDATE transacao SET status = ?, identificador_externo = ? WHERE id_transacao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoStatus.name());
            ps.setString(2, identificadorExterno);
            ps.setInt(3, idTransacao);
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM transacao WHERE id_transacao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Transacao mapearTransacao(ResultSet rs) throws SQLException {
        Transacao t = new Transacao();
        t.setIdTransacao(rs.getInt("id_transacao"));
        t.setIdContaOrigem(rs.getInt("id_conta_origem"));
        t.setIdContaDestino(rs.getInt("id_conta_destino"));
        t.setTipoTransacao(TipoTransacao.valueOf(rs.getString("tipo_transacao")));
        t.setValor(rs.getBigDecimal("valor"));
        Timestamp ts = rs.getTimestamp("data_hora");
        t.setDataHora(ts != null ? ts.toLocalDateTime() : null);
        t.setStatus(StatusTransacao.valueOf(rs.getString("status")));
        t.setDescricao(rs.getString("descricao"));
        t.setIdentificadorExterno(rs.getString("identificador_externo"));
        t.setJsonDadosExtras(rs.getString("json_dados_extras"));
        return t;
    }
}
