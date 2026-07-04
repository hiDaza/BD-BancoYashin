/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Cliente;
import dao.ClienteDAO;
import util.CriptografiaUtil;
import util.LogUtil;

public class AutenticacaoService {
    private ClienteDAO clienteDAO = new ClienteDAO();
    private int tentativasConsecutivas = 0;

    public Cliente login(String cpfCnpj, String senha, String ipOrigem) throws Exception {
        Cliente cliente = clienteDAO.buscarPorCpfCnpj(cpfCnpj);
        if (cliente == null) {
            throw new Exception("Cliente não encontrado");
        }

        if (!cliente.isAtivo()) {
            throw new Exception("Conta bloqueada. Entre em contato com o suporte.");
        }

        String senhaHash = CriptografiaUtil.gerarHash(senha);
        if (!cliente.getSenhaHash().equals(senhaHash)) {
            tentativasConsecutivas++;
            if (tentativasConsecutivas >= 3) {
                cliente.setAtivo(false);
                clienteDAO.atualizar(cliente);
                LogUtil.registrarLog(cliente.getIdCliente(), "BLOQUEIO_CONTA", 
                        "Bloqueada por 3 tentativas falhas");
                throw new Exception("Conta bloqueada por excesso de tentativas");
            }
            throw new Exception("Senha incorreta. Tentativa " + tentativasConsecutivas + " de 3");
        }

        tentativasConsecutivas = 0;
        LogUtil.registrarLog(cliente.getIdCliente(), "LOGIN_SUCESSO", 
                "Login realizado de " + ipOrigem);
        return cliente;
    }

    public void logout(int idCliente) {
        try {
            LogUtil.registrarLog(idCliente, "LOGOUT", "Sessão encerrada");
        } catch (Exception e) {
            // Log silencioso
        }
    }
}