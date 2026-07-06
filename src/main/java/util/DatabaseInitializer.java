package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    private static final String URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root"; // ALTERE AQUI

    public static void inicializar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 1. Conecta sem banco específico
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                // 2. Cria o banco se não existir
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS yashin_bank");
                System.out.println("Banco 'yashin_bank' verificado/criado.");

                // 3. Conecta ao banco yashin_bank
                try (Connection connDB = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/yashin_bank?useSSL=false&serverTimezone=UTC",
                        USER, PASSWORD);
                     Statement stmtDB = connDB.createStatement()) {

                    // 4. Verifica se a tabela 'cliente' existe
                    boolean tabelaExiste = false;
                    try {
                        stmtDB.executeQuery("SELECT 1 FROM cliente LIMIT 1");
                        tabelaExiste = true;
                    } catch (SQLException e) {
                        // tabela não existe
                    }

                    if (!tabelaExiste) {
                        System.out.println("Criando tabelas do banco Yashin...");
                        String[] comandos = obterComandosSQL();
                        for (String cmd : comandos) {
                            String comandoLimpo = cmd.trim();
                            if (!comandoLimpo.isEmpty() && !comandoLimpo.startsWith("--")) {
                                try {
                                    stmtDB.execute(comandoLimpo);
                                } catch (SQLException e) {
                                    System.err.println("Erro ao executar comando: " + comandoLimpo);
                                    e.printStackTrace();
                                }
                            }
                        }
                        System.out.println("Banco de dados inicializado com sucesso!");
                    } else {
                        System.out.println("Banco de dados já existe. Nenhuma ação necessária.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao inicializar banco de dados.", e);
        }
    }

    private static String[] obterComandosSQL() {
        return new String[]{
            // ============================
            // TABELA: cliente
            // ============================
            """
            CREATE TABLE IF NOT EXISTS cliente (
                id_cliente INT AUTO_INCREMENT PRIMARY KEY,
                tipo_pessoa ENUM('PF','PJ') NOT NULL,
                cpf_cnpj VARCHAR(18) NOT NULL UNIQUE,
                nome_razao VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL,
                telefone VARCHAR(20),
                endereco VARCHAR(200),
                data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                senha_hash VARCHAR(255) NOT NULL,
                ativo BOOLEAN DEFAULT TRUE,
                rg VARCHAR(20),
                data_nascimento DATE,
                inscricao_estadual VARCHAR(20),
                contrato_social_url VARCHAR(255),
                representante_legal VARCHAR(100)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: conta
            // ============================
            """
            CREATE TABLE IF NOT EXISTS conta (
                id_conta INT AUTO_INCREMENT PRIMARY KEY,
                id_cliente INT NOT NULL,
                tipo_conta ENUM('CORRENTE','POUPANCA') NOT NULL,
                agencia VARCHAR(10) NOT NULL,
                numero_conta VARCHAR(20) NOT NULL UNIQUE,
                saldo DECIMAL(15,2) DEFAULT 0.00,
                data_abertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status ENUM('ATIVA','ENCERRADA') DEFAULT 'ATIVA',
                limite_diario_ted DECIMAL(15,2) DEFAULT 5000.00,
                limite_diario_pix DECIMAL(15,2) DEFAULT 5000.00,
                FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: transacao
            // ============================
            """
            CREATE TABLE IF NOT EXISTS transacao (
                id_transacao INT AUTO_INCREMENT PRIMARY KEY,
                id_conta_origem INT,
                id_conta_destino INT,
                tipo_transacao ENUM('TRANSFERENCIA_INTERNA','TED','PIX','PAGAMENTO_CONTA','PAGAMENTO_CARTAO','PAGAMENTO_IMPOSTO','EMPRESTIMO','OUTROS') NOT NULL,
                valor DECIMAL(15,2) NOT NULL,
                data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status ENUM('PENDENTE','PROCESSADA','FALHA','CANCELADA') DEFAULT 'PROCESSADA',
                descricao VARCHAR(255),
                identificador_externo VARCHAR(100),
                json_dados_extras JSON,
                FOREIGN KEY (id_conta_origem) REFERENCES conta(id_conta),
                FOREIGN KEY (id_conta_destino) REFERENCES conta(id_conta)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: cartao_credito
            // ============================
            """
            CREATE TABLE IF NOT EXISTS cartao_credito (
                id_cartao INT AUTO_INCREMENT PRIMARY KEY,
                id_conta INT NOT NULL,
                numero_cartao VARCHAR(20) NOT NULL UNIQUE,
                bandeira VARCHAR(20),
                limite_total DECIMAL(15,2) NOT NULL,
                limite_utilizado DECIMAL(15,2) DEFAULT 0.00,
                data_validade DATE NOT NULL,
                codigo_seguranca VARCHAR(4),
                status ENUM('ATIVO','BLOQUEADO','CANCELADO') DEFAULT 'ATIVO',
                tipo ENUM('PESSOAL','CORPORATIVO') DEFAULT 'PESSOAL',
                id_cliente_portador INT,
                FOREIGN KEY (id_conta) REFERENCES conta(id_conta),
                FOREIGN KEY (id_cliente_portador) REFERENCES cliente(id_cliente)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: fatura_cartao
            // ============================
            """
            CREATE TABLE IF NOT EXISTS fatura_cartao (
                id_fatura INT AUTO_INCREMENT PRIMARY KEY,
                id_cartao INT NOT NULL,
                mes_referencia DATE NOT NULL,
                valor_total DECIMAL(15,2) NOT NULL,
                valor_pago DECIMAL(15,2) DEFAULT 0.00,
                data_vencimento DATE NOT NULL,
                data_pagamento DATE,
                status ENUM('ABERTA','PAGA','VENCIDA') DEFAULT 'ABERTA',
                FOREIGN KEY (id_cartao) REFERENCES cartao_credito(id_cartao)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: compra_fatura
            // ============================
            """
            CREATE TABLE IF NOT EXISTS compra_fatura (
                id_compra INT AUTO_INCREMENT PRIMARY KEY,
                id_fatura INT NOT NULL,
                descricao VARCHAR(200),
                valor DECIMAL(15,2) NOT NULL,
                data_compra DATE NOT NULL,
                parcelas INT,
                FOREIGN KEY (id_fatura) REFERENCES fatura_cartao(id_fatura)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: emprestimo_solicitacao
            // ============================
            """
            CREATE TABLE IF NOT EXISTS emprestimo_solicitacao (
                id_solicitacao INT AUTO_INCREMENT PRIMARY KEY,
                id_cliente INT NOT NULL,
                valor_solicitado DECIMAL(15,2) NOT NULL,
                prazo_meses INT NOT NULL,
                finalidade VARCHAR(200),
                data_solicitacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status ENUM('ANALISE','APROVADO','NEGADO','ANALISE_MANUAL') DEFAULT 'ANALISE',
                taxa_juros DECIMAL(5,2),
                valor_aprovado DECIMAL(15,2),
                numero_parcelas INT,
                valor_parcela DECIMAL(15,2),
                data_aprovacao TIMESTAMP,
                id_transacao_credito INT,
                motivo_negacao VARCHAR(255),
                FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
                FOREIGN KEY (id_transacao_credito) REFERENCES transacao(id_transacao)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: boleto_emitido
            // ============================
            """
            CREATE TABLE IF NOT EXISTS boleto_emitido (
                id_boleto INT AUTO_INCREMENT PRIMARY KEY,
                id_cliente_pj INT NOT NULL,
                nosso_numero VARCHAR(20) NOT NULL UNIQUE,
                valor DECIMAL(15,2) NOT NULL,
                data_vencimento DATE NOT NULL,
                data_emissao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                juros_dia DECIMAL(5,2),
                multa DECIMAL(5,2),
                instrucoes TEXT,
                codigo_barras VARCHAR(50) NOT NULL UNIQUE,
                status ENUM('ATIVO','PAGO','CANCELADO','VENCIDO') DEFAULT 'ATIVO',
                id_transacao_pagamento INT,
                FOREIGN KEY (id_cliente_pj) REFERENCES cliente(id_cliente),
                FOREIGN KEY (id_transacao_pagamento) REFERENCES transacao(id_transacao)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: agendamento_transferencia
            // ============================
            """
            CREATE TABLE IF NOT EXISTS agendamento_transferencia (
                id_agendamento INT AUTO_INCREMENT PRIMARY KEY,
                id_conta_origem INT NOT NULL,
                tipo_transferencia ENUM('TED','PIX') NOT NULL,
                dados_destino JSON NOT NULL,
                valor DECIMAL(15,2) NOT NULL,
                data_agendada DATE NOT NULL,
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status ENUM('AGENDADO','EXECUTADO','CANCELADO','FALHA') DEFAULT 'AGENDADO',
                id_transacao_executada INT,
                FOREIGN KEY (id_conta_origem) REFERENCES conta(id_conta),
                FOREIGN KEY (id_transacao_executada) REFERENCES transacao(id_transacao)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: pagamento_lote
            // ============================
            """
            CREATE TABLE IF NOT EXISTS pagamento_lote (
                id_lote INT AUTO_INCREMENT PRIMARY KEY,
                id_cliente_pj INT NOT NULL,
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                tipo ENUM('FORNECEDORES','FOLHA') NOT NULL,
                data_execucao DATE,
                status ENUM('AGENDADO','EXECUTADO','CANCELADO') DEFAULT 'AGENDADO',
                valor_total DECIMAL(15,2) NOT NULL,
                FOREIGN KEY (id_cliente_pj) REFERENCES cliente(id_cliente)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: pagamento_lote_item
            // ============================
            """
            CREATE TABLE IF NOT EXISTS pagamento_lote_item (
                id_item INT AUTO_INCREMENT PRIMARY KEY,
                id_lote INT NOT NULL,
                beneficiario_nome VARCHAR(100) NOT NULL,
                chave_pix VARCHAR(100),
                dados_ted JSON,
                valor DECIMAL(15,2) NOT NULL,
                status ENUM('PENDENTE','PROCESSADO','FALHA') DEFAULT 'PENDENTE',
                id_transacao INT,
                FOREIGN KEY (id_lote) REFERENCES pagamento_lote(id_lote),
                FOREIGN KEY (id_transacao) REFERENCES transacao(id_transacao)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: logs_auditoria
            // ============================
            """
            CREATE TABLE IF NOT EXISTS logs_auditoria (
                id_log INT AUTO_INCREMENT PRIMARY KEY,
                id_cliente INT,
                acao VARCHAR(50) NOT NULL,
                descricao TEXT,
                ip_origem VARCHAR(45),
                data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """,

            // ============================
            // TABELA: preferencias_alertas
            // ============================
            """
            CREATE TABLE IF NOT EXISTS preferencias_alertas (
                id_preferencia INT AUTO_INCREMENT PRIMARY KEY,
                id_cliente INT NOT NULL,
                alerta_saldo_baixo BOOLEAN DEFAULT FALSE,
                valor_limite_saldo DECIMAL(15,2),
                alerta_vencimento_conta BOOLEAN DEFAULT FALSE,
                alerta_extrato_disponivel BOOLEAN DEFAULT FALSE,
                canal_email BOOLEAN DEFAULT TRUE,
                canal_sms BOOLEAN DEFAULT FALSE,
                canal_push BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """
        };
    }
}