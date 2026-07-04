/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.enums.StatusLote;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PagamentoLote {
    private Integer idLote;
    private Integer idClientePj;
    private LocalDateTime dataCriacao;
    private String tipo; // FORNECEDORES, FOLHA
    private LocalDate dataExecucao;
    private StatusLote status;
    private BigDecimal valorTotal;

    // Construtores
    public PagamentoLote() {
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusLote.AGENDADO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public PagamentoLote(Integer idClientePj, String tipo, LocalDate dataExecucao) {
        this();
        this.idClientePj = idClientePj;
        this.tipo = tipo;
        this.dataExecucao = dataExecucao;
    }

    // Getters e Setters
    public Integer getIdLote() { return idLote; }
    public void setIdLote(Integer idLote) { this.idLote = idLote; }

    public Integer getIdClientePj() { return idClientePj; }
    public void setIdClientePj(Integer idClientePj) { this.idClientePj = idClientePj; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDate getDataExecucao() { return dataExecucao; }
    public void setDataExecucao(LocalDate dataExecucao) { this.dataExecucao = dataExecucao; }

    public StatusLote getStatus() { return status; }
    public void setStatus(StatusLote status) { this.status = status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}

