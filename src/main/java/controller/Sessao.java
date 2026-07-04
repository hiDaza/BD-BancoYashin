/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Cliente;
import com.mycompany.yashin.model.Conta;
import java.util.ArrayList;
import java.util.List;

public class Sessao {
    private static Sessao instance;
    private Cliente clienteLogado;
    private List<Conta> contasCliente;

    private Sessao() {
        contasCliente = new ArrayList<>();
    }

    public static Sessao getInstance() {
        if (instance == null) {
            instance = new Sessao();
        }
        return instance;
    }

    public Cliente getClienteLogado() {
        return clienteLogado;
    }

    public void setClienteLogado(Cliente cliente) {
        this.clienteLogado = cliente;
    }

    public List<Conta> getContasCliente() {
        return contasCliente;
    }

    public void setContasCliente(List<Conta> contas) {
        this.contasCliente = contas;
    }

    public void limpar() {
        clienteLogado = null;
        contasCliente.clear();
    }
}
