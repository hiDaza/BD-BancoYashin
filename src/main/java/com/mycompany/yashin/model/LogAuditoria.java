/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import java.time.LocalDateTime;

public class LogAuditoria {
    private Integer idLog;
    private Integer idCliente;
    private String acao;
    private String descricao;
    private String ipOrigem;
    private LocalDateTime dataHora;

    // Construtores
    public LogAuditoria() {
        this.dataHora = LocalDateTime.now();
    }

    public LogAuditoria(Integer idCliente, String acao, String descricao, String ipOrigem) {
        this();
        this.idCliente = idCliente;
        this.acao = acao;
        this.descricao = descricao;
        this.ipOrigem = ipOrigem;
    }

    // Getters e Setters
    public Integer getIdLog() { return idLog; }
    public void setIdLog(Integer idLog) { this.idLog = idLog; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getIpOrigem() { return ipOrigem; }
    public void setIpOrigem(String ipOrigem) { this.ipOrigem = ipOrigem; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}