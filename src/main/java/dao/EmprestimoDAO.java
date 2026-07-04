/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.EmprestimoSolicitacao;
import com.mycompany.yashin.model.enums.StatusEmprestimo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class EmprestimoDAO {

    public void inserir(EmprestimoSolicitacao solicitacao) throws SQLException {
        String sql = "INSERT INTO emprestimo_solicitacao (id_cliente, valor_solicitado, prazo_meses, finalidade, status, taxa_juros, valor_aprovado, numero_parcelas, valor_parcela, data_aprovacao, id_transacao_credito, motivo_negacao) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, solicitacao.getIdCliente());
            ps.setBigDecimal(2, solicitacao.getValorSolicitado());
            ps.setInt(3, solicitacao.getPrazoMeses());
            ps.setString(4, solicitacao.getFinalidade());
            ps.setString(5, solicitacao.getStatus().name());
            ps.setBigDecimal(6, solicitacao.getTaxaJuros());
            ps.setBigDecimal(7, solicitacao.getValorAprovado());
            ps.setObject(8, solicitacao.getNumeroParcelas());
            ps.setBigDecimal(9, solicitacao.getValorParcela());
            ps.setTimestamp(10, solicitacao.getDataAprovacao() != null ? Timestamp.valueOf(solicitacao.getDataAprovacao()) : null);
            ps.setObject(11, solicitacao.getIdTransacaoCredito());
            ps.setString(12, solicitacao.getMotivoNegacao());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    solicitacao.setIdSolicitacao(rs.getInt(1));
                }
            }
        }
    }

    public EmprestimoSolicitacao buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM emprestimo_solicitacao WHERE id_solicitacao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearSolicitacao(rs);
                }
            }
        }
        return null;
    }

    public List<EmprestimoSolicitacao> listarPorCliente(int idCliente) throws SQLException {
        List<EmprestimoSolicitacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM emprestimo_solicitacao WHERE id_cliente = ? ORDER BY data_solicitacao DESC";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitacao(rs));
                }
            }
        }
        return lista;
    }

    public List<EmprestimoSolicitacao> listarPorStatus(StatusEmprestimo status) throws SQLException {
        List<EmprestimoSolicitacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM emprestimo_solicitacao WHERE status = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSolicitacao(rs));
                }
            }
        }
        return lista;
    }

    public void atualizar(EmprestimoSolicitacao solicitacao) throws SQLException {
        String sql = "UPDATE emprestimo_solicitacao SET id_cliente=?, valor_solicitado=?, prazo_meses=?, finalidade=?, status=?, taxa_juros=?, valor_aprovado=?, numero_parcelas=?, valor_parcela=?, data_aprovacao=?, id_transacao_credito=?, motivo_negacao=? WHERE id_solicitacao=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, solicitacao.getIdCliente());
            ps.setBigDecimal(2, solicitacao.getValorSolicitado());
            ps.setInt(3, solicitacao.getPrazoMeses());
            ps.setString(4, solicitacao.getFinalidade());
            ps.setString(5, solicitacao.getStatus().name());
            ps.setBigDecimal(6, solicitacao.getTaxaJuros());
            ps.setBigDecimal(7, solicitacao.getValorAprovado());
            ps.setObject(8, solicitacao.getNumeroParcelas());
            ps.setBigDecimal(9, solicitacao.getValorParcela());
            ps.setTimestamp(10, solicitacao.getDataAprovacao() != null ? Timestamp.valueOf(solicitacao.getDataAprovacao()) : null);
            ps.setObject(11, solicitacao.getIdTransacaoCredito());
            ps.setString(12, solicitacao.getMotivoNegacao());
            ps.setInt(13, solicitacao.getIdSolicitacao());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM emprestimo_solicitacao WHERE id_solicitacao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private EmprestimoSolicitacao mapearSolicitacao(ResultSet rs) throws SQLException {
        EmprestimoSolicitacao e = new EmprestimoSolicitacao();
        e.setIdSolicitacao(rs.getInt("id_solicitacao"));
        e.setIdCliente(rs.getInt("id_cliente"));
        e.setValorSolicitado(rs.getBigDecimal("valor_solicitado"));
        e.setPrazoMeses(rs.getInt("prazo_meses"));
        e.setFinalidade(rs.getString("finalidade"));
        Timestamp ts = rs.getTimestamp("data_solicitacao");
        e.setDataSolicitacao(ts != null ? ts.toLocalDateTime() : null);
        e.setStatus(StatusEmprestimo.valueOf(rs.getString("status")));
        e.setTaxaJuros(rs.getBigDecimal("taxa_juros"));
        e.setValorAprovado(rs.getBigDecimal("valor_aprovado"));
        e.setNumeroParcelas(rs.getInt("numero_parcelas") != 0 ? rs.getInt("numero_parcelas") : null);
        e.setValorParcela(rs.getBigDecimal("valor_parcela"));
        Timestamp ta = rs.getTimestamp("data_aprovacao");
        e.setDataAprovacao(ta != null ? ta.toLocalDateTime() : null);
        e.setIdTransacaoCredito(rs.getInt("id_transacao_credito") != 0 ? rs.getInt("id_transacao_credito") : null);
        e.setMotivoNegacao(rs.getString("motivo_negacao"));
        return e;
    }
}
