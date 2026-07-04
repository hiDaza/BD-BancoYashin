/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.enums.StatusEmprestimo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmprestimoSolicitacao {
    private Integer idSolicitacao;
    private Integer idCliente;
    private BigDecimal valorSolicitado;
    private Integer prazoMeses;
    private String finalidade;
    private LocalDateTime dataSolicitacao;
    private StatusEmprestimo status;
    private BigDecimal taxaJuros;
    private BigDecimal valorAprovado;
    private Integer numeroParcelas;
    private BigDecimal valorParcela;
    private LocalDateTime dataAprovacao;
    private Integer idTransacaoCredito;
    private String motivoNegacao;

    // Construtores
    public EmprestimoSolicitacao() {
        this.dataSolicitacao = LocalDateTime.now();
        this.status = StatusEmprestimo.ANALISE;
    }

    public EmprestimoSolicitacao(Integer idCliente, BigDecimal valorSolicitado,
                                 Integer prazoMeses, String finalidade) {
        this();
        this.idCliente = idCliente;
        this.valorSolicitado = valorSolicitado;
        this.prazoMeses = prazoMeses;
        this.finalidade = finalidade;
    }
    
    

    // Getters e Setters
    public Integer getIdSolicitacao() { return idSolicitacao; }
    public void setIdSolicitacao(Integer idSolicitacao) { this.idSolicitacao = idSolicitacao; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public BigDecimal getValorSolicitado() { return valorSolicitado; }
    public void setValorSolicitado(BigDecimal valorSolicitado) { this.valorSolicitado = valorSolicitado; }

    public Integer getPrazoMeses() { return prazoMeses; }
    public void setPrazoMeses(Integer prazoMeses) { this.prazoMeses = prazoMeses; }

    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public StatusEmprestimo getStatus() { return status; }
    public void setStatus(StatusEmprestimo status) { this.status = status; }

    public BigDecimal getTaxaJuros() { return taxaJuros; }
    public void setTaxaJuros(BigDecimal taxaJuros) { this.taxaJuros = taxaJuros; }

    public BigDecimal getValorAprovado() { return valorAprovado; }
    public void setValorAprovado(BigDecimal valorAprovado) { this.valorAprovado = valorAprovado; }

    public Integer getNumeroParcelas() { return numeroParcelas; }
    public void setNumeroParcelas(Integer numeroParcelas) { this.numeroParcelas = numeroParcelas; }

    public BigDecimal getValorParcela() { return valorParcela; }
    public void setValorParcela(BigDecimal valorParcela) { this.valorParcela = valorParcela; }

    public LocalDateTime getDataAprovacao() { return dataAprovacao; }
    public void setDataAprovacao(LocalDateTime dataAprovacao) { this.dataAprovacao = dataAprovacao; }

    public Integer getIdTransacaoCredito() { return idTransacaoCredito; }
    public void setIdTransacaoCredito(Integer idTransacaoCredito) { this.idTransacaoCredito = idTransacaoCredito; }

    
    
    public String getMotivoNegacao() { return motivoNegacao; }
    public void setMotivoNegacao(String motivoNegacao) { this.motivoNegacao = motivoNegacao; }
}

