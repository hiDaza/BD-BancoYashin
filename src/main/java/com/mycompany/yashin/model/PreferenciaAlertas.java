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

public class PreferenciaAlertas {
    private Integer idPreferencia;
    private Integer idCliente;
    private Boolean alertaSaldoBaixo;
    private BigDecimal valorLimiteSaldo;
    private Boolean alertaVencimentoConta;
    private Boolean alertaExtratoDisponivel;
    private Boolean canalEmail;
    private Boolean canalSms;
    private Boolean canalPush;

    // Construtores
    public PreferenciaAlertas() {
        this.alertaSaldoBaixo = false;
        this.alertaVencimentoConta = false;
        this.alertaExtratoDisponivel = false;
        this.canalEmail = true;
        this.canalSms = false;
        this.canalPush = false;
    }

    public PreferenciaAlertas(Integer idCliente) {
        this();
        this.idCliente = idCliente;
    }

    // Getters e Setters
    public Integer getIdPreferencia() { return idPreferencia; }
    public void setIdPreferencia(Integer idPreferencia) { this.idPreferencia = idPreferencia; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public Boolean getAlertaSaldoBaixo() { return alertaSaldoBaixo; }
    public void setAlertaSaldoBaixo(Boolean alertaSaldoBaixo) { this.alertaSaldoBaixo = alertaSaldoBaixo; }

    public BigDecimal getValorLimiteSaldo() { return valorLimiteSaldo; }
    public void setValorLimiteSaldo(BigDecimal valorLimiteSaldo) { this.valorLimiteSaldo = valorLimiteSaldo; }

    public Boolean getAlertaVencimentoConta() { return alertaVencimentoConta; }
    public void setAlertaVencimentoConta(Boolean alertaVencimentoConta) { this.alertaVencimentoConta = alertaVencimentoConta; }

    public Boolean getAlertaExtratoDisponivel() { return alertaExtratoDisponivel; }
    public void setAlertaExtratoDisponivel(Boolean alertaExtratoDisponivel) { this.alertaExtratoDisponivel = alertaExtratoDisponivel; }

    public Boolean getCanalEmail() { return canalEmail; }
    public void setCanalEmail(Boolean canalEmail) { this.canalEmail = canalEmail; }

    public Boolean getCanalSms() { return canalSms; }
    public void setCanalSms(Boolean canalSms) { this.canalSms = canalSms; }

    public Boolean getCanalPush() { return canalPush; }
    public void setCanalPush(Boolean canalPush) { this.canalPush = canalPush; }
}
