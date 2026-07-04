/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Cliente;
import com.mycompany.yashin.model.enums.TipoPessoa;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import util.ConexaoBD;

public class ClienteDAO {

    public void inserir(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (tipo_pessoa, cpf_cnpj, nome_razao, email, telefone, endereco, senha_hash, rg, data_nascimento, inscricao_estadual, contrato_social_url, representante_legal) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getTipoPessoa().name());
            ps.setString(2, cliente.getCpfCnpj());
            ps.setString(3, cliente.getNomeRazao());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getTelefone());
            ps.setString(6, cliente.getEndereco());
            ps.setString(7, cliente.getSenhaHash());
            ps.setString(8, cliente.getRg());
            ps.setDate(9, cliente.getDataNascimento() != null ? Date.valueOf(cliente.getDataNascimento()) : null);
            ps.setString(10, cliente.getInscricaoEstadual());
            ps.setString(11, cliente.getContratoSocialUrl());
            ps.setString(12, cliente.getRepresentanteLegal());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdCliente(rs.getInt(1));
                }
            }
        }
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        }
        return null;
    }

    public Cliente buscarPorCpfCnpj(String cpfCnpj) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE cpf_cnpj = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpfCnpj);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        }
        return null;
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente";
        try (Connection conn = ConexaoBD.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
        }
        return clientes;
    }

    public void atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET tipo_pessoa=?, cpf_cnpj=?, nome_razao=?, email=?, telefone=?, endereco=?, senha_hash=?, rg=?, data_nascimento=?, inscricao_estadual=?, contrato_social_url=?, representante_legal=?, ativo=? WHERE id_cliente=?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getTipoPessoa().name());
            ps.setString(2, cliente.getCpfCnpj());
            ps.setString(3, cliente.getNomeRazao());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getTelefone());
            ps.setString(6, cliente.getEndereco());
            ps.setString(7, cliente.getSenhaHash());
            ps.setString(8, cliente.getRg());
            ps.setDate(9, cliente.getDataNascimento() != null ? Date.valueOf(cliente.getDataNascimento()) : null);
            ps.setString(10, cliente.getInscricaoEstadual());
            ps.setString(11, cliente.getContratoSocialUrl());
            ps.setString(12, cliente.getRepresentanteLegal());
            ps.setBoolean(13, cliente.isAtivo());
            ps.setInt(14, cliente.getIdCliente());
            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        try (Connection conn = ConexaoBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setTipoPessoa(TipoPessoa.valueOf(rs.getString("tipo_pessoa")));
        c.setCpfCnpj(rs.getString("cpf_cnpj"));
        c.setNomeRazao(rs.getString("nome_razao"));
        c.setEmail(rs.getString("email"));
        c.setTelefone(rs.getString("telefone"));
        c.setEndereco(rs.getString("endereco"));
        Timestamp ts = rs.getTimestamp("data_cadastro");
        c.setDataCadastro(ts != null ? ts.toLocalDateTime() : null);
        c.setSenhaHash(rs.getString("senha_hash"));
        c.setAtivo(rs.getBoolean("ativo"));
        c.setRg(rs.getString("rg"));
        Date d = rs.getDate("data_nascimento");
        c.setDataNascimento(d != null ? d.toLocalDate() : null);
        c.setInscricaoEstadual(rs.getString("inscricao_estadual"));
        c.setContratoSocialUrl(rs.getString("contrato_social_url"));
        c.setRepresentanteLegal(rs.getString("representante_legal"));
        return c;
    }
}
