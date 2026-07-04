/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.yashin.model;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.enums.StatusAgendamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AgendamentoTransferencia {
    private Integer idAgendamento;
    private Integer idContaOrigem;
    private String tipoTransferencia; // "TED" ou "PIX"
    private String dadosDestino;      // JSON com chave PIX ou dados TED
    private BigDecimal valor;
    private LocalDate dataAgendada;
    private LocalDateTime dataCriacao;
    private StatusAgendamento status;
    private Integer idTransacaoExecutada;

    // Construtores
    public AgendamentoTransferencia() {
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusAgendamento.AGENDADO;
    }

    public AgendamentoTransferencia(Integer idContaOrigem, String tipoTransferencia,
                                    String dadosDestino, BigDecimal valor, LocalDate dataAgendada) {
        this();
        this.idContaOrigem = idContaOrigem;
        this.tipoTransferencia = tipoTransferencia;
        this.dadosDestino = dadosDestino;
        this.valor = valor;
        this.dataAgendada = dataAgendada;
    }

    // Getters e Setters
    public Integer getIdAgendamento() { return idAgendamento; }
    public void setIdAgendamento(Integer idAgendamento) { this.idAgendamento = idAgendamento; }

    public Integer getIdContaOrigem() { return idContaOrigem; }
    public void setIdContaOrigem(Integer idContaOrigem) { this.idContaOrigem = idContaOrigem; }

    public String getTipoTransferencia() { return tipoTransferencia; }
    public void setTipoTransferencia(String tipoTransferencia) { this.tipoTransferencia = tipoTransferencia; }

    public String getDadosDestino() { return dadosDestino; }
    public void setDadosDestino(String dadosDestino) { this.dadosDestino = dadosDestino; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataAgendada() { return dataAgendada; }
    public void setDataAgendada(LocalDate dataAgendada) { this.dataAgendada = dataAgendada; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public Integer getIdTransacaoExecutada() { return idTransacaoExecutada; }
    public void setIdTransacaoExecutada(Integer idTransacaoExecutada) { this.idTransacaoExecutada = idTransacaoExecutada; }
}
