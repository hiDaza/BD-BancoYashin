/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author daza
 */
import Service.AutenticacaoService;
import Service.BoletoService;
import Service.CartaoService;
import Service.ClienteService;
import Service.ContaService;
import Service.EmprestimoService;
import Service.NotificacaoService;
import Service.PagamentoLoteService;
import Service.PagamentoService;
import Service.RelatorioService;
import Service.TransferenciaService;
import com.mycompany.yashin.model.AgendamentoTransferencia;
import com.mycompany.yashin.model.BoletoEmitido;
import com.mycompany.yashin.model.CartaoCredito;
import com.mycompany.yashin.model.Cliente;
import com.mycompany.yashin.model.CompraFatura;
import com.mycompany.yashin.model.Conta;
import com.mycompany.yashin.model.EmprestimoSolicitacao;
import com.mycompany.yashin.model.FaturaCartao;
import com.mycompany.yashin.model.PagamentoLote;
import com.mycompany.yashin.model.PreferenciaAlertas;
import com.mycompany.yashin.model.Transacao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BancoController {
    private AutenticacaoService authService = new AutenticacaoService();
    private ClienteService clienteService = new ClienteService();
    private ContaService contaService = new ContaService();
    private TransferenciaService transferenciaService = new TransferenciaService();
    private PagamentoService pagamentoService = new PagamentoService();
    private EmprestimoService emprestimoService = new EmprestimoService();
    private CartaoService cartaoService = new CartaoService();
    private BoletoService boletoService = new BoletoService();
    private PagamentoLoteService pagamentoLoteService = new PagamentoLoteService();
    private NotificacaoService notificacaoService = new NotificacaoService();
    private RelatorioService relatorioService = new RelatorioService();

    // === Autenticação ===
    public Cliente login(String cpfCnpj, String senha) throws Exception {
        return authService.login(cpfCnpj, senha, "127.0.0.1");
    }

    // === Cliente ===
    public Cliente abrirContaPF(String cpf, String nome, String email, String telefone,
                                String endereco, String senha, String rg, LocalDate dataNascimento,
                                String tipoConta, String agencia) throws Exception {
        return clienteService.abrirContaPF(cpf, nome, email, telefone, endereco, senha,
                rg, dataNascimento, tipoConta, agencia);
    }

    public Cliente abrirContaPJ(String cnpj, String razaoSocial, String email, String telefone,
                                String endereco, String senha, String inscricaoEstadual,
                                String contratoSocialUrl, String representanteLegal,
                                String tipoConta, String agencia) throws Exception {
        return clienteService.abrirContaPJ(cnpj, razaoSocial, email, telefone, endereco, senha,
                inscricaoEstadual, contratoSocialUrl, representanteLegal, tipoConta, agencia);
    }

    public Cliente buscarClientePorId(int id) throws SQLException {
        return clienteService.buscarPorId(id);
    }

    public void atualizarCliente(Cliente cliente) throws Exception {
        clienteService.atualizarDados(cliente);
    }

    // === Conta ===
    
    public Conta abrirNovaConta(int idCliente, String tipoConta, String agencia) throws Exception {
        return contaService.abrirNovaConta(idCliente, tipoConta, agencia);
    }
    
    
    public Conta buscarContaPorId(int id) throws SQLException {
        return contaService.buscarPorId(id);
    }

    public List<Conta> buscarContasPorCliente(int idCliente) throws SQLException {
        return contaService.buscarPorCliente(idCliente);
    }

    public BigDecimal consultarSaldo(int idConta) throws Exception {
        return contaService.consultarSaldo(idConta);
    }

    public List<Transacao> visualizarExtrato(int idConta, LocalDateTime inicio, LocalDateTime fim) throws Exception {
        return contaService.visualizarExtrato(idConta, inicio, fim);
    }

    public void encerrarConta(int idConta, int idContaDestino) throws Exception {
        contaService.encerrarConta(idConta, idContaDestino);
    }

    // === Transferências ===
    public void transferenciaInterna(int idOrigem, int idDestino, BigDecimal valor) throws Exception {
        transferenciaService.transferenciaInterna(idOrigem, idDestino, valor);
    }

    public void transferenciaTED(int idOrigem, String banco, String agencia, String conta,
                                 String cpfCnpj, String nomeBeneficiario, BigDecimal valor) throws Exception {
        transferenciaService.transferenciaTED(idOrigem, banco, agencia, conta, cpfCnpj, nomeBeneficiario, valor);
    }

    public void transferenciaPIX(int idOrigem, String chave, BigDecimal valor) throws Exception {
        transferenciaService.transferenciaPIX(idOrigem, chave, valor);
    }

    public void agendarTransferencia(int idOrigem, String tipo, String dadosDestino,
                                     BigDecimal valor, LocalDate dataAgendada) throws Exception {
        transferenciaService.agendarTransferencia(idOrigem, tipo, dadosDestino, valor, dataAgendada);
    }

    public List<AgendamentoTransferencia> listarAgendamentosCliente(int idCliente) throws SQLException {
        return transferenciaService.listarAgendamentosPorCliente(idCliente);
    }

    // === Pagamentos ===
    public void pagarConta(int idConta, String linhaDigitavel, BigDecimal valor) throws Exception {
        pagamentoService.pagarConta(idConta, linhaDigitavel, valor);
    }

    public void pagarFaturaCartao(int idConta, int idFatura) throws Exception {
        pagamentoService.pagarFaturaCartao(idConta, idFatura);
    }

    public void pagarPIXQRCode(int idConta, String qrCode, BigDecimal valor) throws Exception {
        pagamentoService.pagarPIXQRCode(idConta, qrCode, valor);
    }

    // === Empréstimos ===
    public EmprestimoSolicitacao simularEmprestimo(int idCliente, BigDecimal valor, int prazo) throws Exception {
        return emprestimoService.simularEmprestimo(idCliente, valor, prazo);
    }

    public EmprestimoSolicitacao solicitarEmprestimo(int idCliente, BigDecimal valor, int prazo, String finalidade) throws Exception {
        return emprestimoService.solicitarEmprestimo(idCliente, valor, prazo, finalidade);
    }

    public List<EmprestimoSolicitacao> listarEmprestimosCliente(int idCliente) throws SQLException {
        return emprestimoService.listarPorCliente(idCliente);
    }

    // === Cartões ===
    public CartaoCredito solicitarCartaoPessoal(int idConta, BigDecimal limite, String bandeira) throws Exception {
        return cartaoService.solicitarCartaoPessoal(idConta, limite, bandeira);
    }

    public CartaoCredito solicitarCartaoCorporativo(int idConta, BigDecimal limite, String bandeira, int idPortador) throws Exception {
        return cartaoService.solicitarCartaoCorporativo(idConta, limite, bandeira, idPortador);
    }

    public List<CartaoCredito> listarCartoesConta(int idConta) throws SQLException {
        return cartaoService.listarCartoesPorConta(idConta);
    }

    public List<CartaoCredito> listarCartoesCliente(int idCliente) throws SQLException {
        return cartaoService.listarCartoesPorCliente(idCliente);
    }

    public void bloquearCartao(int idCartao) throws Exception {
        cartaoService.bloquearCartao(idCartao);
    }

    public void desbloquearCartao(int idCartao) throws Exception {
        cartaoService.desbloquearCartao(idCartao);
    }

    public List<FaturaCartao> listarFaturasCartao(int idCartao) throws SQLException {
        return cartaoService.listarFaturas(idCartao);
    }

    public List<FaturaCartao> listarFaturasCliente(int idCliente) throws SQLException {
        return cartaoService.listarFaturasPorCliente(idCliente);
    }

    public FaturaCartao buscarFaturaPorId(int idFatura) throws SQLException {
        return cartaoService.buscarFaturaPorId(idFatura);
    }

    // === Boletos (PJ) ===
    public BoletoEmitido emitirBoleto(int idClientePj, BigDecimal valor, LocalDate vencimento,
                                      BigDecimal juros, BigDecimal multa, String instrucoes) throws Exception {
        return boletoService.emitirBoleto(idClientePj, valor, vencimento, juros, multa, instrucoes);
    }

    public void cancelarBoleto(int idBoleto) throws Exception {
        boletoService.cancelarBoleto(idBoleto);
    }

    public List<BoletoEmitido> listarBoletosCliente(int idClientePj) throws SQLException {
        return boletoService.listarPorCliente(idClientePj);
    }

    // === Pagamentos em Lote (PJ) ===
    public int criarLote(int idClientePj, String tipo, LocalDate dataExecucao) throws Exception {
        return pagamentoLoteService.criarLote(idClientePj, tipo, dataExecucao);
    }

    public void adicionarItemLote(int idLote, String beneficiario, String chavePix, String dadosTedJson, BigDecimal valor) throws Exception {
        pagamentoLoteService.adicionarItemLote(idLote, beneficiario, chavePix, dadosTedJson, valor);
    }

    public void executarLote(int idLote) throws Exception {
        pagamentoLoteService.executarLote(idLote);
    }

    public List<PagamentoLote> listarLotesCliente(int idClientePj) throws SQLException {
        return pagamentoLoteService.listarLotesPorCliente(idClientePj);
    }

    public List<?> listarItensLote(int idLote) {
        return pagamentoLoteService.listarItensLote(idLote);
    }

    // === Relatórios ===
    public String gerarComprovante(int idTransacao) throws Exception {
        return relatorioService.gerarComprovante(idTransacao);
    }

    public String gerarExtratoPDF(int idConta, LocalDateTime inicio, LocalDateTime fim) throws Exception {
        return relatorioService.gerarExtratoPDF(idConta, inicio, fim);
    }

    // === Notificações ===
    public void configurarAlertas(PreferenciaAlertas pref) throws Exception {
        notificacaoService.configurarPreferencias(pref);
    }
    
   

    public PreferenciaAlertas buscarAlertas(int idCliente) throws SQLException {
        return notificacaoService.buscarPreferencias(idCliente);
    }
    
    public EmprestimoSolicitacao buscarSolicitacaoPorId(int id) throws SQLException {
        return emprestimoService.buscarSolicitacaoPorId(id);
    }
    
    
    public void pagarComCartao(int idCartao, BigDecimal valor, String descricao, int parcelas) throws Exception {
        cartaoService.registrarCompra(idCartao, valor, descricao, parcelas);
    }
    
    public List<CartaoCredito> listarCartoesPorConta(int idConta) throws Exception {
    return cartaoService.listarCartoesPorConta(idConta);
}

    public FaturaCartao buscarFaturaAberta(int idCartao) throws Exception {
        java.time.LocalDate mesReferencia = java.time.LocalDate.now().withDayOfMonth(1);
        return new dao.FaturaDAO().buscarFaturaAberta(idCartao, mesReferencia);
    }

    public List<CompraFatura> listarComprasDaFatura(int idFatura) throws Exception {
        return new dao.CompraFaturaDAO().listarPorFatura(idFatura);
    }
    
    public void atualizarDadosCliente(Cliente cliente) throws Exception {
    clienteService.atualizarDados(cliente);
}

    public void excluirCliente(int idCliente) throws Exception {
        clienteService.excluirCliente(idCliente);
    }

    public boolean temEmprestimoAtivo(int idCliente) throws SQLException {
        return emprestimoService.temEmprestimoAtivo(idCliente);
    }

    public boolean temFaturaEmAberto(int idCliente) throws SQLException {
        return cartaoService.temFaturaEmAberto(idCliente);
    }

    
}