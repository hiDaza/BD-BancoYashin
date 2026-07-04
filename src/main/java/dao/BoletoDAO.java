/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.BoletoEmitido;
import com.mycompany.yashin.model.enums.StatusBoleto;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class BoletoDAO {

    public void inserir(BoletoEmitido boleto) throws SQLException {
        String sql = "INSERT INTO boleto_emitido (id_cliente_pj, nosso_numero, valor, data_vencimento, juros_dia, multa, instrucoes, codigo_barras, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, boleto.getIdClientePj());
            ps.setString(2, boleto.getNossoNumero());
            ps.setBigDecimal(3, boleto.getValor());
            ps.setDate(4, Date.valueOf(boleto.getDataVencimento()));
            ps.setBigDecimal(5, boleto.getJurosDia());
            ps.setBigDecimal(6, boleto.getMulta());
            ps.setString(7, boleto.getInstrucoes());
            ps.setString(8, boleto.getCodigoBarras());
            ps.setString(9, boleto.getStatus().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    boleto.setIdBoleto(rs.getInt(1));
                }
            }
        }
    }

    public BoletoEmitido buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM boleto_emitido WHERE id_boleto = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearBoleto(rs);
                }
            }
        }
        return null;
    }

    public BoletoEmitido buscarPorNossoNumero(String nossoNumero) throws SQLException {
        String sql = "SELECT * FROM boleto_emitido WHERE nosso_numero = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nossoNumero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearBoleto(rs);
                }
            }
        }
        return null;
    }

    public List<BoletoEmitido> listarPorCliente(int idClientePj) throws SQLException {
        List<BoletoEmitido> lista = new ArrayList<>();
        String sql = "SELECT * FROM boleto_emitido WHERE id_cliente_pj = ? ORDER BY data_vencimento";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idClientePj);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBoleto(rs));
                }
            }
        }
        return lista;
    }

    public List<BoletoEmitido> listarPorStatus(int idClientePj, StatusBoleto status) throws SQLException {
        List<BoletoEmitido> lista = new ArrayList<>();
        String sql = "SELECT * FROM boleto_emitido WHERE id_cliente_pj = ? AND status = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idClientePj);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBoleto(rs));
                }
            }
        }
        return lista;
    }

    public void atualizar(BoletoEmitido boleto) throws SQLException {
        String sql = "UPDATE boleto_emitido SET id_cliente_pj=?, nosso_numero=?, valor=?, data_vencimento=?, juros_dia=?, multa=?, instrucoes=?, codigo_barras=?, status=?, id_transacao_pagamento=? WHERE id_boleto=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, boleto.getIdClientePj());
            ps.setString(2, boleto.getNossoNumero());
            ps.setBigDecimal(3, boleto.getValor());
            ps.setDate(4, Date.valueOf(boleto.getDataVencimento()));
            ps.setBigDecimal(5, boleto.getJurosDia());
            ps.setBigDecimal(6, boleto.getMulta());
            ps.setString(7, boleto.getInstrucoes());
            ps.setString(8, boleto.getCodigoBarras());
            ps.setString(9, boleto.getStatus().name());
            ps.setObject(10, boleto.getIdTransacaoPagamento());
            ps.setInt(11, boleto.getIdBoleto());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM boleto_emitido WHERE id_boleto = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private BoletoEmitido mapearBoleto(ResultSet rs) throws SQLException {
        BoletoEmitido b = new BoletoEmitido();
        b.setIdBoleto(rs.getInt("id_boleto"));
        b.setIdClientePj(rs.getInt("id_cliente_pj"));
        b.setNossoNumero(rs.getString("nosso_numero"));
        b.setValor(rs.getBigDecimal("valor"));
        b.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());
        Timestamp ts = rs.getTimestamp("data_emissao");
        b.setDataEmissao(ts != null ? ts.toLocalDateTime() : null);
        b.setJurosDia(rs.getBigDecimal("juros_dia"));
        b.setMulta(rs.getBigDecimal("multa"));
        b.setInstrucoes(rs.getString("instrucoes"));
        b.setCodigoBarras(rs.getString("codigo_barras"));
        b.setStatus(StatusBoleto.valueOf(rs.getString("status")));
        b.setIdTransacaoPagamento(rs.getInt("id_transacao_pagamento") != 0 ? rs.getInt("id_transacao_pagamento") : null);
        return b;
    }
}
