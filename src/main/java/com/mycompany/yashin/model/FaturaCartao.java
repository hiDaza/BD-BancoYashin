/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import java.math.BigDecimal;
import java.time.LocalDate;

public class FaturaCartao {
    private Integer idFatura;
    private Integer idCartao;
    private LocalDate mesReferencia;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private String status; // ABERTA, PAGA, VENCIDA

    // Construtores
    public FaturaCartao() {
        this.valorPago = BigDecimal.ZERO;
        this.status = "ABERTA";
    }

    public FaturaCartao(Integer idCartao, LocalDate mesReferencia, BigDecimal valorTotal,
                        LocalDate dataVencimento) {
        this();
        this.idCartao = idCartao;
        this.mesReferencia = mesReferencia;
        this.valorTotal = valorTotal;
        this.dataVencimento = dataVencimento;
    }

    // Getters e Setters
    public Integer getIdFatura() { return idFatura; }
    public void setIdFatura(Integer idFatura) { this.idFatura = idFatura; }

    public Integer getIdCartao() { return idCartao; }
    public void setIdCartao(Integer idCartao) { this.idCartao = idCartao; }

    public LocalDate getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(LocalDate mesReferencia) { this.mesReferencia = mesReferencia; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}