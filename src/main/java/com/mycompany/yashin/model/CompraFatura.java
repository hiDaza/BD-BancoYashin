/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author daza
 */

public class CompraFatura {
    private Integer idCompra;
    private Integer idFatura;
    private String descricao;
    private BigDecimal valor;
    private LocalDate dataCompra;
    private Integer parcelas;

    // Construtores
    public CompraFatura() {}

    public CompraFatura(Integer idFatura, String descricao, BigDecimal valor, LocalDate dataCompra, Integer parcelas) {
        this.idFatura = idFatura;
        this.descricao = descricao;
        this.valor = valor;
        this.dataCompra = dataCompra;
        this.parcelas = parcelas;
    }

    // Getters e Setters
    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer idCompra) { this.idCompra = idCompra; }

    public Integer getIdFatura() { return idFatura; }
    public void setIdFatura(Integer idFatura) { this.idFatura = idFatura; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataCompra() { return dataCompra; }
    public void setDataCompra(LocalDate dataCompra) { this.dataCompra = dataCompra; }

    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
}
