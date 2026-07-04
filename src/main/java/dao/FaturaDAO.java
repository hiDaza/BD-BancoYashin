/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.FaturaCartao;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class FaturaDAO {

    public void inserir(FaturaCartao fatura) throws SQLException {
        String sql = "INSERT INTO fatura_cartao (id_cartao, mes_referencia, valor_total, valor_pago, data_vencimento, data_pagamento, status) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fatura.getIdCartao());
            ps.setDate(2, Date.valueOf(fatura.getMesReferencia()));
            ps.setBigDecimal(3, fatura.getValorTotal());
            ps.setBigDecimal(4, fatura.getValorPago());
            ps.setDate(5, Date.valueOf(fatura.getDataVencimento()));
            ps.setDate(6, fatura.getDataPagamento() != null ? Date.valueOf(fatura.getDataPagamento()) : null);
            ps.setString(7, fatura.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    fatura.setIdFatura(rs.getInt(1));
                }
            }
        }
    }

    public FaturaCartao buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM fatura_cartao WHERE id_fatura = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFatura(rs);
                }
            }
        }
        return null;
    }

    public List<FaturaCartao> listarPorCartao(int idCartao) throws SQLException {
        List<FaturaCartao> lista = new ArrayList<>();
        String sql = "SELECT * FROM fatura_cartao WHERE id_cartao = ? ORDER BY mes_referencia DESC";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCartao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFatura(rs));
                }
            }
        }
        return lista;
    }

    public FaturaCartao buscarFaturaAberta(int idCartao, LocalDate mesReferencia) throws SQLException {
        String sql = "SELECT * FROM fatura_cartao WHERE id_cartao = ? AND mes_referencia = ? AND status IN ('ABERTA', 'VENCIDA')";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCartao);
            ps.setDate(2, Date.valueOf(mesReferencia));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFatura(rs);
                }
            }
        }
        return null;
    }

    public void atualizar(FaturaCartao fatura) throws SQLException {
        String sql = "UPDATE fatura_cartao SET id_cartao=?, mes_referencia=?, valor_total=?, valor_pago=?, data_vencimento=?, data_pagamento=?, status=? WHERE id_fatura=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fatura.getIdCartao());
            ps.setDate(2, Date.valueOf(fatura.getMesReferencia()));
            ps.setBigDecimal(3, fatura.getValorTotal());
            ps.setBigDecimal(4, fatura.getValorPago());
            ps.setDate(5, Date.valueOf(fatura.getDataVencimento()));
            ps.setDate(6, fatura.getDataPagamento() != null ? Date.valueOf(fatura.getDataPagamento()) : null);
            ps.setString(7, fatura.getStatus());
            ps.setInt(8, fatura.getIdFatura());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM fatura_cartao WHERE id_fatura = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
    
        public List<FaturaCartao> listarPorCliente(int idCliente) throws SQLException {
        List<FaturaCartao> lista = new ArrayList<>();
        String sql = "SELECT f.* FROM fatura_cartao f " +
                     "JOIN cartao_credito c ON f.id_cartao = c.id_cartao " +
                     "JOIN conta co ON c.id_conta = co.id_conta " +
                     "WHERE co.id_cliente = ? ORDER BY f.data_vencimento";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFatura(rs));
                }
            }
        }
        return lista;
    }

    private FaturaCartao mapearFatura(ResultSet rs) throws SQLException {
        FaturaCartao f = new FaturaCartao();
        f.setIdFatura(rs.getInt("id_fatura"));
        f.setIdCartao(rs.getInt("id_cartao"));
        f.setMesReferencia(rs.getDate("mes_referencia").toLocalDate());
        f.setValorTotal(rs.getBigDecimal("valor_total"));
        f.setValorPago(rs.getBigDecimal("valor_pago"));
        f.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());
        Date dp = rs.getDate("data_pagamento");
        f.setDataPagamento(dp != null ? dp.toLocalDate() : null);
        f.setStatus(rs.getString("status"));
        return f;
    }
}
