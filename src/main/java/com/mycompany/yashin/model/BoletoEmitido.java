/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.enums.StatusBoleto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BoletoEmitido {
    private Integer idBoleto;
    private Integer idClientePj;
    private String nossoNumero;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDateTime dataEmissao;
    private BigDecimal jurosDia;
    private BigDecimal multa;
    private String instrucoes;
    private String codigoBarras;
    private StatusBoleto status;
    private Integer idTransacaoPagamento;

    // Construtores
    public BoletoEmitido() {
        this.dataEmissao = LocalDateTime.now();
        this.status = StatusBoleto.ATIVO;
    }

    public BoletoEmitido(Integer idClientePj, String nossoNumero, BigDecimal valor,
                         LocalDate dataVencimento, String codigoBarras) {
        this();
        this.idClientePj = idClientePj;
        this.nossoNumero = nossoNumero;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.codigoBarras = codigoBarras;
    }

    // Getters e Setters
    public Integer getIdBoleto() { return idBoleto; }
    public void setIdBoleto(Integer idBoleto) { this.idBoleto = idBoleto; }

    public Integer getIdClientePj() { return idClientePj; }
    public void setIdClientePj(Integer idClientePj) { this.idClientePj = idClientePj; }

    public String getNossoNumero() { return nossoNumero; }
    public void setNossoNumero(String nossoNumero) { this.nossoNumero = nossoNumero; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }

    public BigDecimal getJurosDia() { return jurosDia; }
    public void setJurosDia(BigDecimal jurosDia) { this.jurosDia = jurosDia; }

    public BigDecimal getMulta() { return multa; }
    public void setMulta(BigDecimal multa) { this.multa = multa; }

    public String getInstrucoes() { return instrucoes; }
    public void setInstrucoes(String instrucoes) { this.instrucoes = instrucoes; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public StatusBoleto getStatus() { return status; }
    public void setStatus(StatusBoleto status) { this.status = status; }

    public Integer getIdTransacaoPagamento() { return idTransacaoPagamento; }
    public void setIdTransacaoPagamento(Integer idTransacaoPagamento) { this.idTransacaoPagamento = idTransacaoPagamento; }
}