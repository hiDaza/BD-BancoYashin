/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

/**
 *
 * @author daza
 */
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.EmprestimoSolicitacao;
import com.mycompany.yashin.model.Transacao;
import com.mycompany.yashin.model.enums.StatusEmprestimo;
import com.mycompany.yashin.model.enums.StatusTransacao;
import com.mycompany.yashin.model.enums.TipoTransacao;

import dao.ContaDAO;
import dao.EmprestimoDAO;
import dao.TransacaoDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import util.LogUtil;

public class EmprestimoService {
    private EmprestimoDAO emprestimoDAO = new EmprestimoDAO();
    private ContaDAO contaDAO = new ContaDAO();
    private TransacaoDAO transacaoDAO = new TransacaoDAO();

    // Simulação de consultas externas
    private int consultarScoreBureau(String cpfCnpj) {
        return 500 + (int)(Math.random() * 500); // 500 a 1000
    }

    private BigDecimal consultarRendaComprometidaSCR(String cpfCnpj) {
        return new BigDecimal("0.20"); // 20%
    }

    public EmprestimoSolicitacao simularEmprestimo(int idCliente, BigDecimal valor, int prazoMeses) throws Exception {
        if (valor.compareTo(BigDecimal.ZERO) <= 0 || prazoMeses <= 0) {
            throw new Exception("Valor e prazo devem ser positivos");
        }

        // Taxa simulada baseada em perfil (mock)
        BigDecimal taxaBase = new BigDecimal("1.99");
        BigDecimal juros = taxaBase.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal fator = BigDecimal.ONE.add(juros);
        BigDecimal parcela = valor.multiply(fator.pow(prazoMeses))
                .divide(new BigDecimal(prazoMeses), 2, RoundingMode.HALF_UP);

        EmprestimoSolicitacao sim = new EmprestimoSolicitacao();
        sim.setIdCliente(idCliente);
        sim.setValorSolicitado(valor);
        sim.setPrazoMeses(prazoMeses);
        sim.setStatus(StatusEmprestimo.ANALISE); // não importa muito para simulação
        sim.setTaxaJuros(taxaBase);
        sim.setValorParcela(parcela);
        sim.setNumeroParcelas(prazoMeses);
        return sim;
    }

    public EmprestimoSolicitacao solicitarEmprestimo(int idCliente, BigDecimal valor, int prazo, String finalidade) throws Exception {
            // Validação
            if (valor.compareTo(new BigDecimal("100.00")) < 0 || valor.compareTo(new BigDecimal("100000.00")) > 0) {
                throw new Exception("Valor deve estar entre R$ 100,00 e R$ 100.000,00");
            }
            if (prazo < 6 || prazo > 60) {
                throw new Exception("Prazo deve estar entre 6 e 60 meses");
            }

            // Consulta externa (mock)
            int score = consultarScoreBureau("cpf");
            BigDecimal comprometimento = consultarRendaComprometidaSCR("cpf");

            // Primeiro, calcula os dados da simulação (mesmo que depois seja negado, vamos guardar)
            BigDecimal taxaBase = new BigDecimal("1.99");
            BigDecimal juros = taxaBase.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal fator = BigDecimal.ONE.add(juros);
            BigDecimal parcela = valor.multiply(fator.pow(prazo))
                    .divide(new BigDecimal(prazo), 2, RoundingMode.HALF_UP);

            EmprestimoSolicitacao solicitacao = new EmprestimoSolicitacao(idCliente, valor, prazo, finalidade);
            solicitacao.setDataSolicitacao(LocalDateTime.now());
            // Preenche sempre os dados calculados
            solicitacao.setTaxaJuros(taxaBase);
            solicitacao.setValorParcela(parcela);
            solicitacao.setNumeroParcelas(prazo);

            // Regras de negócio - APENAS define o status e valores aqui, NÃO executa o crédito ainda
            if (score >= 700 && comprometimento.compareTo(new BigDecimal("0.30")) < 0) {
                // APROVADO
                BigDecimal taxa = calcularTaxa(score);
                BigDecimal parcelaAprovada = calcularParcela(valor, prazo, taxa);
                solicitacao.setStatus(StatusEmprestimo.APROVADO);
                solicitacao.setTaxaJuros(taxa);
                solicitacao.setValorAprovado(valor);
                solicitacao.setNumeroParcelas(prazo);
                solicitacao.setValorParcela(parcelaAprovada);
                solicitacao.setDataAprovacao(LocalDateTime.now());

                // A LINHA creditarEmprestimo FOI REMOVIDA DAQUI

            } else if (score < 400 || comprometimento.compareTo(new BigDecimal("0.50")) > 0) {
                solicitacao.setStatus(StatusEmprestimo.NEGADO);
                solicitacao.setMotivoNegacao("Score insuficiente ou comprometimento alto");
            } else {
                solicitacao.setStatus(StatusEmprestimo.NEGADO);
                solicitacao.setMotivoNegacao("Perfil de crédito não atende aos critérios mínimos");
            }

           // 1. Primeiro salvamos no banco para GERAR O ID
           emprestimoDAO.inserir(solicitacao);
           System.out.println("DEBUG: Solicitação ID após inserção = " + solicitacao.getIdSolicitacao());

           // Fallback caso não tenha retornado a chave
           if (solicitacao.getIdSolicitacao() == null) {
               solicitacao.setIdSolicitacao(emprestimoDAO.buscarUltimoIdPorCliente(idCliente));
           }

           // 2. Agora, com a solicitação salva e com um ID gerado, creditamos o valor se for aprovado
           if (solicitacao.getStatus() == StatusEmprestimo.APROVADO) {
               creditarEmprestimo(idCliente, valor, solicitacao);
           }

           LogUtil.registrarLog(idCliente, "SOLICITACAO_EMPRESTIMO", 
                    "Valor: " + valor + " Status: " + solicitacao.getStatus());
           return solicitacao;
    }
    
    private BigDecimal calcularTaxa(int score) {
        if (score >= 900) return new BigDecimal("1.49");
        if (score >= 700) return new BigDecimal("1.99");
        if (score >= 500) return new BigDecimal("2.99");
        return new BigDecimal("4.99");
    }

    private BigDecimal calcularParcela(BigDecimal valor, int prazo, BigDecimal taxa) {
        // Juros compostos simplificados: M = C * (1 + i)^n / n
        BigDecimal juros = taxa.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal fator = BigDecimal.ONE.add(juros);
        BigDecimal montante = valor.multiply(fator.pow(prazo));
        return montante.divide(new BigDecimal(prazo), 2, RoundingMode.HALF_UP);
    }

    private void creditarEmprestimo(int idCliente, BigDecimal valor, EmprestimoSolicitacao solicitacao) throws Exception {
        // Buscar primeira conta ativa do cliente
        List<Conta> contas = new ContaService().buscarPorCliente(idCliente);
        if (contas == null || contas.isEmpty()) {
            throw new Exception("Cliente não possui conta ativa");
        }
        Conta conta = contas.get(0);

        // Atualiza saldo
        conta.setSaldo(conta.getSaldo().add(valor));
        contaDAO.atualizarSaldo(conta);

        // Registra transação
        Transacao t = new Transacao();
        t.setIdContaDestino(conta.getIdConta());
        t.setTipoTransacao(TipoTransacao.EMPRESTIMO);
        t.setValor(valor);
        t.setStatus(StatusTransacao.PROCESSADA);
        t.setDescricao("Crédito de empréstimo #" + solicitacao.getIdSolicitacao());
        transacaoDAO.inserir(t);

        solicitacao.setIdTransacaoCredito(t.getIdTransacao());
        emprestimoDAO.atualizar(solicitacao);
    }

    public EmprestimoSolicitacao buscarSolicitacaoPorId(int id) throws SQLException {
        return emprestimoDAO.buscarPorId(id);
    }

    public List<EmprestimoSolicitacao> listarPorCliente(int idCliente) throws SQLException {
        return emprestimoDAO.listarPorCliente(idCliente);
    }
    
}