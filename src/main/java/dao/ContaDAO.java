/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.enums.StatusConta;
import com.mycompany.yashin.model.enums.TipoConta;


import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class ContaDAO {

    public void inserir(Conta conta) throws SQLException {
        String sql = "INSERT INTO conta (id_cliente, tipo_conta, agencia, numero_conta, saldo, status, limite_diario_ted, limite_diario_pix) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, conta.getIdCliente());
            ps.setString(2, conta.getTipoConta().name());
            ps.setString(3, conta.getAgencia());
            ps.setString(4, conta.getNumeroConta());
            ps.setBigDecimal(5, conta.getSaldo());
            ps.setString(6, conta.getStatus().name());
            ps.setBigDecimal(7, conta.getLimiteDiarioTed());
            ps.setBigDecimal(8, conta.getLimiteDiarioPix());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    conta.setIdConta(rs.getInt(1));
                }
            }
        }
    }

    public Conta buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM conta WHERE id_conta = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearConta(rs);
                }
            }
        }
        return null;
    }

    public Conta buscarPorNumeroConta(String numeroConta) throws SQLException {
        String sql = "SELECT * FROM conta WHERE numero_conta = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroConta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearConta(rs);
                }
            }
        }
        return null;
    }

    public List<Conta> listarPorCliente(int idCliente) throws SQLException {
        List<Conta> contas = new ArrayList<>();
        String sql = "SELECT * FROM conta WHERE id_cliente = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contas.add(mapearConta(rs));
                }
            }
        }
        return contas;
    }

    public void atualizar(Conta conta) throws SQLException {
        String sql = "UPDATE conta SET id_cliente=?, tipo_conta=?, agencia=?, numero_conta=?, saldo=?, status=?, limite_diario_ted=?, limite_diario_pix=? WHERE id_conta=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conta.getIdCliente());
            ps.setString(2, conta.getTipoConta().name());
            ps.setString(3, conta.getAgencia());
            ps.setString(4, conta.getNumeroConta());
            ps.setBigDecimal(5, conta.getSaldo());
            ps.setString(6, conta.getStatus().name());
            ps.setBigDecimal(7, conta.getLimiteDiarioTed());
            ps.setBigDecimal(8, conta.getLimiteDiarioPix());
            ps.setInt(9, conta.getIdConta());
            ps.executeUpdate();
        }
    }

    public void atualizarSaldo(Conta conta) throws SQLException {
        String sql = "UPDATE conta SET saldo = ? WHERE id_conta = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, conta.getSaldo());
            ps.setInt(2, conta.getIdConta());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM conta WHERE id_conta = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Conta mapearConta(ResultSet rs) throws SQLException {
        Conta c = new Conta();
        c.setIdConta(rs.getInt("id_conta"));
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setTipoConta(TipoConta.valueOf(rs.getString("tipo_conta")));
        c.setAgencia(rs.getString("agencia"));
        c.setNumeroConta(rs.getString("numero_conta"));
        c.setSaldo(rs.getBigDecimal("saldo"));
        Timestamp ts = rs.getTimestamp("data_abertura");
        c.setDataAbertura(ts != null ? ts.toLocalDateTime() : null);
        c.setStatus(StatusConta.valueOf(rs.getString("status")));
        c.setLimiteDiarioTed(rs.getBigDecimal("limite_diario_ted"));
        c.setLimiteDiarioPix(rs.getBigDecimal("limite_diario_pix"));
        return c;
    }
}
