/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.enums.StatusConta;
import com.mycompany.yashin.model.enums.TipoConta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Conta {
    private Integer idConta;
    private Integer idCliente;
    private TipoConta tipoConta;
    private String agencia;
    private String numeroConta;
    private BigDecimal saldo;
    private LocalDateTime dataAbertura;
    private StatusConta status;
    private BigDecimal limiteDiarioTed;
    private BigDecimal limiteDiarioPix;

    // Construtores
    public Conta() {
        this.saldo = BigDecimal.ZERO;
        this.dataAbertura = LocalDateTime.now();
        this.status = StatusConta.ATIVA;
        this.limiteDiarioTed = new BigDecimal("5000.00");
        this.limiteDiarioPix = new BigDecimal("5000.00");
    }

    public Conta(Integer idCliente, TipoConta tipoConta, String agencia, String numeroConta) {
        this();
        this.idCliente = idCliente;
        this.tipoConta = tipoConta;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
    }

    // Getters e Setters
    public Integer getIdConta() { return idConta; }
    public void setIdConta(Integer idConta) { this.idConta = idConta; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public TipoConta getTipoConta() { return tipoConta; }
    public void setTipoConta(TipoConta tipoConta) { this.tipoConta = tipoConta; }

    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }

    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public StatusConta getStatus() { return status; }
    public void setStatus(StatusConta status) { this.status = status; }

    public BigDecimal getLimiteDiarioTed() { return limiteDiarioTed; }
    public void setLimiteDiarioTed(BigDecimal limiteDiarioTed) { this.limiteDiarioTed = limiteDiarioTed; }

    public BigDecimal getLimiteDiarioPix() { return limiteDiarioPix; }
    public void setLimiteDiarioPix(BigDecimal limiteDiarioPix) { this.limiteDiarioPix = limiteDiarioPix; }
    
    @Override
    public String toString() {
        return numeroConta != null ? numeroConta : "";
    }


}

