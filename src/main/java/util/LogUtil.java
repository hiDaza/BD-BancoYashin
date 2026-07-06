/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.LogAuditoria;

import dao.LogAuditoriaDAO;

public class LogUtil {
    private static LogAuditoriaDAO logDAO = new LogAuditoriaDAO();

    public static void registrarLog(Integer idCliente, String acao, String descricao) {
        try {
            LogAuditoria log = new LogAuditoria();
            log.setIdCliente(idCliente);
            log.setAcao(acao);
            log.setDescricao(descricao);
            log.setIpOrigem("127.0.0.1");
            logDAO.inserir(log);
        } catch (Exception e) {
            System.err.println("Erro ao registrar log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
