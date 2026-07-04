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

public class CartaoCredito {
    private Integer idCartao;
    private Integer idConta;
    private String numeroCartao;
    private String bandeira;
    private BigDecimal limiteTotal;
    private BigDecimal limiteUtilizado;
    private LocalDate dataValidade;
    private String codigoSeguranca;
    private String status; // ATIVO, BLOQUEADO, CANCELADO
    private String tipo;   // PESSOAL, CORPORATIVO
    private Integer idClientePortador; // para cartões corporativos

    // Construtores
    public CartaoCredito() {
        this.limiteUtilizado = BigDecimal.ZERO;
        this.status = "ATIVO";
        this.tipo = "PESSOAL";
    }

    public CartaoCredito(Integer idConta, String numeroCartao, String bandeira,
                         BigDecimal limiteTotal, LocalDate dataValidade, String codigoSeguranca) {
        this();
        this.idConta = idConta;
        this.numeroCartao = numeroCartao;
        this.bandeira = bandeira;
        this.limiteTotal = limiteTotal;
        this.dataValidade = dataValidade;
        this.codigoSeguranca = codigoSeguranca;
    }

    // Getters e Setters
    public Integer getIdCartao() { return idCartao; }
    public void setIdCartao(Integer idCartao) { this.idCartao = idCartao; }

    public Integer getIdConta() { return idConta; }
    public void setIdConta(Integer idConta) { this.idConta = idConta; }

    public String getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(String numeroCartao) { this.numeroCartao = numeroCartao; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public BigDecimal getLimiteTotal() { return limiteTotal; }
    public void setLimiteTotal(BigDecimal limiteTotal) { this.limiteTotal = limiteTotal; }

    public BigDecimal getLimiteUtilizado() { return limiteUtilizado; }
    public void setLimiteUtilizado(BigDecimal limiteUtilizado) { this.limiteUtilizado = limiteUtilizado; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public String getCodigoSeguranca() { return codigoSeguranca; }
    public void setCodigoSeguranca(String codigoSeguranca) { this.codigoSeguranca = codigoSeguranca; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getIdClientePortador() { return idClientePortador; }
    public void setIdClientePortador(Integer idClientePortador) { this.idClientePortador = idClientePortador; }
}
