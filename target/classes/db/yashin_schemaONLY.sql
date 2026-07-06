/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-12.3.2-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: yashin_bank
-- ------------------------------------------------------
-- Server version	12.3.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `agendamento_transferencia`
--

CREATE DATABASE IF NOT EXISTS yashin_bank

use yashin_bank;

DROP TABLE IF EXISTS `agendamento_transferencia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `agendamento_transferencia` (
  `id_agendamento` int(11) NOT NULL AUTO_INCREMENT,
  `id_conta_origem` int(11) NOT NULL,
  `tipo_transferencia` enum('TED','PIX') NOT NULL,
  `dados_destino` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`dados_destino`)),
  `valor` decimal(15,2) NOT NULL,
  `data_agendada` date NOT NULL,
  `data_criacao` timestamp NULL DEFAULT current_timestamp(),
  `status` enum('AGENDADO','EXECUTADO','CANCELADO','FALHA') DEFAULT 'AGENDADO',
  `id_transacao_executada` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_agendamento`),
  KEY `id_conta_origem` (`id_conta_origem`),
  KEY `id_transacao_executada` (`id_transacao_executada`),
  KEY `idx_agendamento_data` (`data_agendada`),
  CONSTRAINT `1` FOREIGN KEY (`id_conta_origem`) REFERENCES `conta` (`id_conta`),
  CONSTRAINT `2` FOREIGN KEY (`id_transacao_executada`) REFERENCES `transacao` (`id_transacao`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `boleto_emitido`
--

DROP TABLE IF EXISTS `boleto_emitido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `boleto_emitido` (
  `id_boleto` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente_pj` int(11) NOT NULL,
  `nosso_numero` varchar(20) NOT NULL,
  `valor` decimal(15,2) NOT NULL,
  `data_vencimento` date NOT NULL,
  `data_emissao` timestamp NULL DEFAULT current_timestamp(),
  `juros_dia` decimal(5,2) DEFAULT NULL,
  `multa` decimal(5,2) DEFAULT NULL,
  `instrucoes` text DEFAULT NULL,
  `codigo_barras` varchar(50) NOT NULL,
  `status` enum('ATIVO','PAGO','CANCELADO','VENCIDO') DEFAULT 'ATIVO',
  `id_transacao_pagamento` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_boleto`),
  UNIQUE KEY `nosso_numero` (`nosso_numero`),
  UNIQUE KEY `codigo_barras` (`codigo_barras`),
  KEY `id_transacao_pagamento` (`id_transacao_pagamento`),
  KEY `idx_boleto_cliente` (`id_cliente_pj`),
  CONSTRAINT `1` FOREIGN KEY (`id_cliente_pj`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `2` FOREIGN KEY (`id_transacao_pagamento`) REFERENCES `transacao` (`id_transacao`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cartao_credito`
--

DROP TABLE IF EXISTS `cartao_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cartao_credito` (
  `id_cartao` int(11) NOT NULL AUTO_INCREMENT,
  `id_conta` int(11) NOT NULL,
  `numero_cartao` varchar(20) NOT NULL,
  `bandeira` varchar(20) DEFAULT NULL,
  `limite_total` decimal(15,2) NOT NULL,
  `limite_utilizado` decimal(15,2) DEFAULT 0.00,
  `data_validade` date NOT NULL,
  `codigo_seguranca` varchar(4) DEFAULT NULL,
  `status` enum('ATIVO','BLOQUEADO','CANCELADO') DEFAULT 'ATIVO',
  `tipo` enum('PESSOAL','CORPORATIVO') DEFAULT 'PESSOAL',
  `id_cliente_portador` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_cartao`),
  UNIQUE KEY `numero_cartao` (`numero_cartao`),
  KEY `id_conta` (`id_conta`),
  KEY `id_cliente_portador` (`id_cliente_portador`),
  CONSTRAINT `1` FOREIGN KEY (`id_conta`) REFERENCES `conta` (`id_conta`),
  CONSTRAINT `2` FOREIGN KEY (`id_cliente_portador`) REFERENCES `cliente` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id_cliente` int(11) NOT NULL AUTO_INCREMENT,
  `tipo_pessoa` enum('PF','PJ') NOT NULL,
  `cpf_cnpj` varchar(18) NOT NULL,
  `nome_razao` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  `endereco` varchar(200) DEFAULT NULL,
  `data_cadastro` timestamp NULL DEFAULT current_timestamp(),
  `senha_hash` varchar(255) NOT NULL,
  `ativo` tinyint(1) DEFAULT 1,
  `rg` varchar(20) DEFAULT NULL,
  `data_nascimento` date DEFAULT NULL,
  `inscricao_estadual` varchar(20) DEFAULT NULL,
  `contrato_social_url` varchar(255) DEFAULT NULL,
  `representante_legal` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_cliente`),
  UNIQUE KEY `cpf_cnpj` (`cpf_cnpj`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `compra_fatura`
--

DROP TABLE IF EXISTS `compra_fatura`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `compra_fatura` (
  `id_compra` int(11) NOT NULL AUTO_INCREMENT,
  `id_fatura` int(11) NOT NULL,
  `descricao` varchar(200) DEFAULT NULL,
  `valor` decimal(15,2) NOT NULL,
  `data_compra` date NOT NULL,
  `parcelas` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_compra`),
  KEY `id_fatura` (`id_fatura`),
  CONSTRAINT `1` FOREIGN KEY (`id_fatura`) REFERENCES `fatura_cartao` (`id_fatura`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conta`
--

DROP TABLE IF EXISTS `conta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `conta` (
  `id_conta` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente` int(11) NOT NULL,
  `tipo_conta` enum('CORRENTE','POUPANCA') NOT NULL,
  `agencia` varchar(10) NOT NULL,
  `numero_conta` varchar(20) NOT NULL,
  `saldo` decimal(15,2) DEFAULT 0.00,
  `data_abertura` timestamp NULL DEFAULT current_timestamp(),
  `status` enum('ATIVA','ENCERRADA') DEFAULT 'ATIVA',
  `limite_diario_ted` decimal(15,2) DEFAULT 5000.00,
  `limite_diario_pix` decimal(15,2) DEFAULT 5000.00,
  PRIMARY KEY (`id_conta`),
  UNIQUE KEY `numero_conta` (`numero_conta`),
  KEY `idx_conta_cliente` (`id_cliente`),
  CONSTRAINT `1` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `emprestimo_solicitacao`
--

DROP TABLE IF EXISTS `emprestimo_solicitacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `emprestimo_solicitacao` (
  `id_solicitacao` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente` int(11) NOT NULL,
  `valor_solicitado` decimal(15,2) NOT NULL,
  `prazo_meses` int(11) NOT NULL,
  `finalidade` varchar(200) DEFAULT NULL,
  `data_solicitacao` timestamp NULL DEFAULT current_timestamp(),
  `status` enum('ANALISE','APROVADO','NEGADO','ANALISE_MANUAL') DEFAULT 'ANALISE',
  `taxa_juros` decimal(5,2) DEFAULT NULL,
  `valor_aprovado` decimal(15,2) DEFAULT NULL,
  `numero_parcelas` int(11) DEFAULT NULL,
  `valor_parcela` decimal(15,2) DEFAULT NULL,
  `data_aprovacao` timestamp NULL DEFAULT NULL,
  `id_transacao_credito` int(11) DEFAULT NULL,
  `motivo_negacao` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_solicitacao`),
  KEY `id_transacao_credito` (`id_transacao_credito`),
  KEY `idx_emprestimo_cliente` (`id_cliente`),
  CONSTRAINT `1` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `2` FOREIGN KEY (`id_transacao_credito`) REFERENCES `transacao` (`id_transacao`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fatura_cartao`
--

DROP TABLE IF EXISTS `fatura_cartao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fatura_cartao` (
  `id_fatura` int(11) NOT NULL AUTO_INCREMENT,
  `id_cartao` int(11) NOT NULL,
  `mes_referencia` date NOT NULL,
  `valor_total` decimal(15,2) NOT NULL,
  `valor_pago` decimal(15,2) DEFAULT 0.00,
  `data_vencimento` date NOT NULL,
  `data_pagamento` date DEFAULT NULL,
  `status` enum('ABERTA','PAGA','VENCIDA') DEFAULT 'ABERTA',
  PRIMARY KEY (`id_fatura`),
  KEY `idx_fatura_cartao` (`id_cartao`,`mes_referencia`),
  CONSTRAINT `1` FOREIGN KEY (`id_cartao`) REFERENCES `cartao_credito` (`id_cartao`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logs_auditoria`
--

DROP TABLE IF EXISTS `logs_auditoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `logs_auditoria` (
  `id_log` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente` int(11) DEFAULT NULL,
  `acao` varchar(50) NOT NULL,
  `descricao` text DEFAULT NULL,
  `ip_origem` varchar(45) DEFAULT NULL,
  `data_hora` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_log`),
  KEY `idx_log_cliente` (`id_cliente`),
  KEY `idx_log_data` (`data_hora`),
  CONSTRAINT `1` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pagamento_lote`
--

DROP TABLE IF EXISTS `pagamento_lote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pagamento_lote` (
  `id_lote` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente_pj` int(11) NOT NULL,
  `data_criacao` timestamp NULL DEFAULT current_timestamp(),
  `tipo` enum('FORNECEDORES','FOLHA') NOT NULL,
  `data_execucao` date DEFAULT NULL,
  `status` enum('AGENDADO','EXECUTADO','CANCELADO') DEFAULT 'AGENDADO',
  `valor_total` decimal(15,2) NOT NULL,
  PRIMARY KEY (`id_lote`),
  KEY `id_cliente_pj` (`id_cliente_pj`),
  CONSTRAINT `1` FOREIGN KEY (`id_cliente_pj`) REFERENCES `cliente` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pagamento_lote_item`
--

DROP TABLE IF EXISTS `pagamento_lote_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pagamento_lote_item` (
  `id_item` int(11) NOT NULL AUTO_INCREMENT,
  `id_lote` int(11) NOT NULL,
  `beneficiario_nome` varchar(100) NOT NULL,
  `chave_pix` varchar(100) DEFAULT NULL,
  `dados_ted` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`dados_ted`)),
  `valor` decimal(15,2) NOT NULL,
  `status` enum('PENDENTE','PROCESSADO','FALHA') DEFAULT 'PENDENTE',
  `id_transacao` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_item`),
  KEY `id_lote` (`id_lote`),
  KEY `id_transacao` (`id_transacao`),
  CONSTRAINT `1` FOREIGN KEY (`id_lote`) REFERENCES `pagamento_lote` (`id_lote`),
  CONSTRAINT `2` FOREIGN KEY (`id_transacao`) REFERENCES `transacao` (`id_transacao`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `preferencias_alertas`
--

DROP TABLE IF EXISTS `preferencias_alertas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `preferencias_alertas` (
  `id_preferencia` int(11) NOT NULL AUTO_INCREMENT,
  `id_cliente` int(11) NOT NULL,
  `alerta_saldo_baixo` tinyint(1) DEFAULT 0,
  `valor_limite_saldo` decimal(15,2) DEFAULT NULL,
  `alerta_vencimento_conta` tinyint(1) DEFAULT 0,
  `alerta_extrato_disponivel` tinyint(1) DEFAULT 0,
  `canal_email` tinyint(1) DEFAULT 1,
  `canal_sms` tinyint(1) DEFAULT 0,
  `canal_push` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id_preferencia`),
  KEY `id_cliente` (`id_cliente`),
  CONSTRAINT `1` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transacao`
--

DROP TABLE IF EXISTS `transacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `transacao` (
  `id_transacao` int(11) NOT NULL AUTO_INCREMENT,
  `id_conta_origem` int(11) DEFAULT NULL,
  `id_conta_destino` int(11) DEFAULT NULL,
  `tipo_transacao` enum('TRANSFERENCIA_INTERNA','TED','PIX','PAGAMENTO_CONTA','PAGAMENTO_CARTAO','PAGAMENTO_IMPOSTO','EMPRESTIMO','OUTROS') NOT NULL,
  `valor` decimal(15,2) NOT NULL,
  `data_hora` timestamp NULL DEFAULT current_timestamp(),
  `status` enum('PENDENTE','PROCESSADA','FALHA','CANCELADA') DEFAULT 'PROCESSADA',
  `descricao` varchar(255) DEFAULT NULL,
  `identificador_externo` varchar(100) DEFAULT NULL,
  `json_dados_extras` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`json_dados_extras`)),
  PRIMARY KEY (`id_transacao`),
  KEY `idx_transacao_origem` (`id_conta_origem`),
  KEY `idx_transacao_destino` (`id_conta_destino`),
  KEY `idx_transacao_data` (`data_hora`),
  CONSTRAINT `1` FOREIGN KEY (`id_conta_origem`) REFERENCES `conta` (`id_conta`),
  CONSTRAINT `2` FOREIGN KEY (`id_conta_destino`) REFERENCES `conta` (`id_conta`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-07-06 16:01:57
