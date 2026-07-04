package com.mycompany.yashin.model;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.enums.TipoPessoa;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Cliente {
    private Integer idCliente;
    private TipoPessoa tipoPessoa;
    private String cpfCnpj;
    private String nomeRazao;
    private String email;
    private String telefone;
    private String endereco;
    private LocalDateTime dataCadastro;
    private String senhaHash;
    private boolean ativo;
    private String rg;
    private LocalDate dataNascimento;
    private String inscricaoEstadual;
    private String contratoSocialUrl;
    private String representanteLegal;

    // Construtores
    public Cliente() {}

    public Cliente(TipoPessoa tipoPessoa, String cpfCnpj, String nomeRazao, String email,
                   String telefone, String endereco, String senhaHash) {
        this.tipoPessoa = tipoPessoa;
        this.cpfCnpj = cpfCnpj;
        this.nomeRazao = nomeRazao;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.senhaHash = senhaHash;
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters e Setters
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public TipoPessoa getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(TipoPessoa tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getNomeRazao() { return nomeRazao; }
    public void setNomeRazao(String nomeRazao) { this.nomeRazao = nomeRazao; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }

    public String getContratoSocialUrl() { return contratoSocialUrl; }
    public void setContratoSocialUrl(String contratoSocialUrl) { this.contratoSocialUrl = contratoSocialUrl; }

    public String getRepresentanteLegal() { return representanteLegal; }
    public void setRepresentanteLegal(String representanteLegal) { this.representanteLegal = representanteLegal; }
}
