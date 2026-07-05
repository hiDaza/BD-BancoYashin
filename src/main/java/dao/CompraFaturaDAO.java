/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.CompraFatura;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class CompraFaturaDAO {

    public void inserir(CompraFatura compra) throws SQLException {
        String sql = "INSERT INTO compra_fatura (id_fatura, descricao, valor, data_compra, parcelas) VALUES (?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, compra.getIdFatura());
            ps.setString(2, compra.getDescricao());
            ps.setBigDecimal(3, compra.getValor());
            ps.setDate(4, Date.valueOf(compra.getDataCompra()));
            ps.setObject(5, compra.getParcelas());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    compra.setIdCompra(rs.getInt(1));
                }
            }
        }
    }

    public List<CompraFatura> listarPorFatura(int idFatura) throws SQLException {
        List<CompraFatura> lista = new ArrayList<>();
        String sql = "SELECT * FROM compra_fatura WHERE id_fatura = ? ORDER BY data_compra DESC";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFatura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCompra(rs));
                }
            }
        }
        return lista;
    }

    private CompraFatura mapearCompra(ResultSet rs) throws SQLException {
        CompraFatura c = new CompraFatura();
        c.setIdCompra(rs.getInt("id_compra"));
        c.setIdFatura(rs.getInt("id_fatura"));
        c.setDescricao(rs.getString("descricao"));
        c.setValor(rs.getBigDecimal("valor"));
        c.setDataCompra(rs.getDate("data_compra").toLocalDate());
        c.setParcelas(rs.getInt("parcelas") != 0 ? rs.getInt("parcelas") : null);
        return c;
    }
}
