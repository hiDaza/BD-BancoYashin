/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.CartaoCredito;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class CartaoDAO {

    public void inserir(CartaoCredito cartao) throws SQLException {
        String sql = "INSERT INTO cartao_credito (id_conta, numero_cartao, bandeira, limite_total, limite_utilizado, data_validade, codigo_seguranca, status, tipo, id_cliente_portador) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cartao.getIdConta());
            ps.setString(2, cartao.getNumeroCartao());
            ps.setString(3, cartao.getBandeira());
            ps.setBigDecimal(4, cartao.getLimiteTotal());
            ps.setBigDecimal(5, cartao.getLimiteUtilizado());
            ps.setDate(6, Date.valueOf(cartao.getDataValidade()));
            ps.setString(7, cartao.getCodigoSeguranca());
            ps.setString(8, cartao.getStatus());
            ps.setString(9, cartao.getTipo());
            ps.setObject(10, cartao.getIdClientePortador());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cartao.setIdCartao(rs.getInt(1));
                }
            }
        }
    }

    public CartaoCredito buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM cartao_credito WHERE id_cartao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCartao(rs);
                }
            }
        }
        return null;
    }

    public CartaoCredito buscarPorNumero(String numero) throws SQLException {
        String sql = "SELECT * FROM cartao_credito WHERE numero_cartao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCartao(rs);
                }
            }
        }
        return null;
    }

    public List<CartaoCredito> listarPorConta(int idConta) throws SQLException {
        List<CartaoCredito> lista = new ArrayList<>();
        String sql = "SELECT * FROM cartao_credito WHERE id_conta = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idConta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCartao(rs));
                }
            }
        }
        return lista;
    }

    public List<CartaoCredito> listarPorClientePortador(int idCliente) throws SQLException {
        List<CartaoCredito> lista = new ArrayList<>();
        String sql = "SELECT * FROM cartao_credito WHERE id_cliente_portador = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCartao(rs));
                }
            }
        }
        return lista;
    }

    public void atualizar(CartaoCredito cartao) throws SQLException {
        String sql = "UPDATE cartao_credito SET id_conta=?, numero_cartao=?, bandeira=?, limite_total=?, limite_utilizado=?, data_validade=?, codigo_seguranca=?, status=?, tipo=?, id_cliente_portador=? WHERE id_cartao=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartao.getIdConta());
            ps.setString(2, cartao.getNumeroCartao());
            ps.setString(3, cartao.getBandeira());
            ps.setBigDecimal(4, cartao.getLimiteTotal());
            ps.setBigDecimal(5, cartao.getLimiteUtilizado());
            ps.setDate(6, Date.valueOf(cartao.getDataValidade()));
            ps.setString(7, cartao.getCodigoSeguranca());
            ps.setString(8, cartao.getStatus());
            ps.setString(9, cartao.getTipo());
            ps.setObject(10, cartao.getIdClientePortador());
            ps.setInt(11, cartao.getIdCartao());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM cartao_credito WHERE id_cartao = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private CartaoCredito mapearCartao(ResultSet rs) throws SQLException {
        CartaoCredito c = new CartaoCredito();
        c.setIdCartao(rs.getInt("id_cartao"));
        c.setIdConta(rs.getInt("id_conta"));
        c.setNumeroCartao(rs.getString("numero_cartao"));
        c.setBandeira(rs.getString("bandeira"));
        c.setLimiteTotal(rs.getBigDecimal("limite_total"));
        c.setLimiteUtilizado(rs.getBigDecimal("limite_utilizado"));
        c.setDataValidade(rs.getDate("data_validade").toLocalDate());
        c.setCodigoSeguranca(rs.getString("codigo_seguranca"));
        c.setStatus(rs.getString("status"));
        c.setTipo(rs.getString("tipo"));
        int portador = rs.getInt("id_cliente_portador");
        c.setIdClientePortador(portador != 0 ? portador : null);
        return c;
    }
}
