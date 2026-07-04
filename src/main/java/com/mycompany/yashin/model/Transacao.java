/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.enums.StatusTransacao;
import com.mycompany.yashin.model.enums.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transacao {
    private Integer idTransacao;
    private Integer idContaOrigem;
    private Integer idContaDestino;
    private TipoTransacao tipoTransacao;
    private BigDecimal valor;
    private LocalDateTime dataHora;
    private StatusTransacao status;
    private String descricao;
    private String identificadorExterno;
    private String jsonDadosExtras;

    // Construtores
    public Transacao() {
        this.dataHora = LocalDateTime.now();
        this.status = StatusTransacao.PENDENTE;
    }

    public Transacao(Integer idContaOrigem, Integer idContaDestino, TipoTransacao tipoTransacao,
                     BigDecimal valor, String descricao) {
        this();
        this.idContaOrigem = idContaOrigem;
        this.idContaDestino = idContaDestino;
        this.tipoTransacao = tipoTransacao;
        this.valor = valor;
        this.descricao = descricao;
    }

    // Getters e Setters
    public Integer getIdTransacao() { return idTransacao; }
    public void setIdTransacao(Integer idTransacao) { this.idTransacao = idTransacao; }

    public Integer getIdContaOrigem() { return idContaOrigem; }
    public void setIdContaOrigem(Integer idContaOrigem) { this.idContaOrigem = idContaOrigem; }

    public Integer getIdContaDestino() { return idContaDestino; }
    public void setIdContaDestino(Integer idContaDestino) { this.idContaDestino = idContaDestino; }

    public TipoTransacao getTipoTransacao() { return tipoTransacao; }
    public void setTipoTransacao(TipoTransacao tipoTransacao) { this.tipoTransacao = tipoTransacao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public StatusTransacao getStatus() { return status; }
    public void setStatus(StatusTransacao status) { this.status = status; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getIdentificadorExterno() { return identificadorExterno; }
    public void setIdentificadorExterno(String identificadorExterno) { this.identificadorExterno = identificadorExterno; }

    public String getJsonDadosExtras() { return jsonDadosExtras; }
    public void setJsonDadosExtras(String jsonDadosExtras) { this.jsonDadosExtras = jsonDadosExtras; }
}
