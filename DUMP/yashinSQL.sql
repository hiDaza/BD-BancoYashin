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
-- Dumping data for table `agendamento_transferencia`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `agendamento_transferencia` WRITE;
/*!40000 ALTER TABLE `agendamento_transferencia` DISABLE KEYS */;
/*!40000 ALTER TABLE `agendamento_transferencia` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `boleto_emitido`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `boleto_emitido` WRITE;
/*!40000 ALTER TABLE `boleto_emitido` DISABLE KEYS */;
INSERT INTO `boleto_emitido` VALUES
(1,5,'001783279693759',150.00,'2026-08-20','2026-07-05 19:28:13',2.00,50.00,'pagar logo essa porra','00000000000000000000000000000000000039300434','ATIVO',NULL);
/*!40000 ALTER TABLE `boleto_emitido` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `cartao_credito`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `cartao_credito` WRITE;
/*!40000 ALTER TABLE `cartao_credito` DISABLE KEYS */;
INSERT INTO `cartao_credito` VALUES
(1,2,'2582 9780 6769 2143','Visa',100.00,0.00,'2029-07-04','219','ATIVO','PESSOAL',NULL),
(2,4,'6463 9058 0674 4385','Visa',20000.00,8422.00,'2029-07-05','208','ATIVO','PESSOAL',NULL),
(3,4,'7787 3280 8169 8361','Visa',1500.00,1500.00,'2029-07-05','074','ATIVO','PESSOAL',NULL);
/*!40000 ALTER TABLE `cartao_credito` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `cliente`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES
(1,'PF','38978645612','alex','alex@gmail.com','18997654412','joao limpagato','2026-07-04 19:18:50','265f36d18f324200b8f1e5306b15928075e0c693e267a0978602c1cf84d6b5e3',1,'984237283','2001-04-28',NULL,NULL,NULL),
(2,'PF','46194101873','gerson','gerson@gmail.com','18997374110','joao limpagato','2026-07-04 19:20:09','265f36d18f324200b8f1e5306b15928075e0c693e267a0978602c1cf84d6b5e3',1,'569468127','2001-04-28',NULL,NULL,NULL),
(3,'PF','12345678900','davi','davi@gmail.com','18996534120','das neves','2026-07-04 20:12:01','265f36d18f324200b8f1e5306b15928075e0c693e267a0978602c1cf84d6b5e3',1,'879674123','2001-04-28',NULL,NULL,NULL),
(4,'PF','46194101872','gerson LTDA','gerson@gmail.com','18997374110','joao limpagato','2026-07-04 20:38:30','265f36d18f324200b8f1e5306b15928075e0c693e267a0978602c1cf84d6b5e3',1,'569468127','2001-04-28',NULL,NULL,NULL),
(5,'PJ','00394460005887','alexandre LTDA','alexandre@gmail.com','11997342211','rua matador','2026-07-05 19:27:40','265f36d18f324200b8f1e5306b15928075e0c693e267a0978602c1cf84d6b5e3',1,NULL,NULL,'2131231','alexnadre.com','alexandre');
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `compra_fatura`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `compra_fatura` WRITE;
/*!40000 ALTER TABLE `compra_fatura` DISABLE KEYS */;
INSERT INTO `compra_fatura` VALUES
(1,1,'pizza',50.00,'2026-07-05',5),
(2,1,'carro',5000.00,'2026-07-05',12),
(3,1,'Compra no cartão',150.00,'2026-07-05',NULL),
(4,1,'carro',2222.00,'2026-07-05',2),
(5,2,'agiota',1500.00,'2026-07-05',12);
/*!40000 ALTER TABLE `compra_fatura` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `conta`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `conta` WRITE;
/*!40000 ALTER TABLE `conta` DISABLE KEYS */;
INSERT INTO `conta` VALUES
(1,1,'CORRENTE','22','92436768',0.00,'2026-07-04 19:18:50','ATIVA',5000.00,5000.00),
(2,2,'CORRENTE','13','05389018',988.00,'2026-07-04 19:20:09','ATIVA',5000.00,5000.00),
(3,3,'CORRENTE','22','88627934',1021.51,'2026-07-04 20:12:01','ATIVA',5000.00,5000.00),
(4,4,'CORRENTE','0001','93501986',2.98,'2026-07-04 20:38:30','ATIVA',5000.00,5000.00),
(5,4,'POUPANCA','0002','97569947',245370.95,'2026-07-05 16:08:16','ATIVA',5000.00,5000.00),
(6,5,'CORRENTE','0003','96023156',1321.25,'2026-07-05 19:27:40','ATIVA',5000.00,5000.00),
(7,4,'CORRENTE','0001','12457753',6305.53,'2026-07-06 16:20:09','ATIVA',5000.00,5000.00),
(8,4,'CORRENTE','0001','81303028',6728.70,'2026-07-06 16:20:22','ATIVA',5000.00,5000.00),
(9,4,'CORRENTE','0001','68504850',2867.88,'2026-07-06 16:23:00','ATIVA',5000.00,5000.00),
(10,4,'POUPANCA','0001','33419643',6402.94,'2026-07-06 16:23:10','ATIVA',5000.00,5000.00),
(11,2,'POUPANCA','13','52818904',7070.84,'2026-07-06 16:23:41','ATIVA',5000.00,5000.00),
(12,2,'POUPANCA','0001','69104920',4307.18,'2026-07-06 16:23:57','ATIVA',5000.00,5000.00),
(13,2,'CORRENTE','0001','38581091',2949.97,'2026-07-06 16:24:12','ATIVA',5000.00,5000.00),
(14,4,'CORRENTE','0001','02904296',944.91,'2026-07-06 16:25:42','ATIVA',5000.00,5000.00),
(15,4,'CORRENTE','0001','86112837',9272.39,'2026-07-06 16:26:30','ATIVA',5000.00,5000.00),
(16,4,'CORRENTE','0001','38358133',7550.92,'2026-07-06 16:27:35','ATIVA',5000.00,5000.00),
(17,4,'CORRENTE','0001','63966105',6680.47,'2026-07-06 16:27:57','ATIVA',5000.00,5000.00);
/*!40000 ALTER TABLE `conta` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `emprestimo_solicitacao`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `emprestimo_solicitacao` WRITE;
/*!40000 ALTER TABLE `emprestimo_solicitacao` DISABLE KEYS */;
INSERT INTO `emprestimo_solicitacao` VALUES
(1,4,1000.00,12,'Comprar Carro','2026-07-04 20:46:08','ANALISE_MANUAL',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(2,4,1000.00,12,'comprar carro','2026-07-04 21:02:57','ANALISE_MANUAL',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(3,4,53000.00,24,'carro','2026-07-04 21:13:08','ANALISE_MANUAL',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(4,4,53000.00,24,'carro','2026-07-04 21:13:39','ANALISE_MANUAL',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(5,4,54212.00,22,'comprar casa','2026-07-04 21:19:49','ANALISE_MANUAL',1.99,NULL,22,3801.37,NULL,NULL,NULL),
(6,4,12345.00,12,'comprar carro','2026-07-05 17:43:26','NEGADO',1.99,NULL,12,1303.17,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(7,4,11324.00,13,'','2026-07-05 17:44:34','NEGADO',1.99,NULL,13,1125.40,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(8,4,1500.00,12,'','2026-07-05 17:45:43','NEGADO',1.99,NULL,12,158.34,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(9,4,100.00,12,'','2026-07-05 17:45:54','NEGADO',1.99,NULL,12,10.56,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(10,4,100.00,60,'','2026-07-05 17:46:30','NEGADO',1.99,NULL,60,5.44,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(11,4,12345.00,22,'comprar carro','2026-07-05 18:05:16','NEGADO',1.99,NULL,22,865.64,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(12,4,12345.00,22,'comprar carro','2026-07-05 18:05:23','NEGADO',1.99,NULL,22,865.64,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(13,4,11222.00,39,'comprar um carro','2026-07-05 18:26:35','APROVADO',1.99,11222.00,39,620.51,'2026-07-05 21:26:35',37,NULL),
(14,4,11222.00,39,'comprar um carro','2026-07-05 18:26:50','NEGADO',1.99,NULL,39,620.51,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(15,4,11222.00,39,'comprar um carro','2026-07-05 18:27:01','NEGADO',1.99,NULL,39,620.51,NULL,NULL,'Perfil de crédito não atende aos critérios mínimos'),
(16,4,100.00,39,'comprar um carro','2026-07-05 18:27:11','APROVADO',1.99,100.00,39,5.53,'2026-07-05 21:27:11',38,NULL),
(17,4,15000.00,12,'pagar o agiota','2026-07-05 19:25:31','APROVADO',1.99,15000.00,12,1583.44,'2026-07-05 22:25:31',43,NULL),
(18,4,500.00,12,'','2026-07-06 16:55:26','APROVADO',1.49,500.00,12,49.76,'2026-07-06 19:55:26',46,NULL);
/*!40000 ALTER TABLE `emprestimo_solicitacao` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `fatura_cartao`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fatura_cartao` WRITE;
/*!40000 ALTER TABLE `fatura_cartao` DISABLE KEYS */;
INSERT INTO `fatura_cartao` VALUES
(1,2,'2026-07-01',8572.00,0.00,'2026-07-31',NULL,'ABERTA'),
(2,3,'2026-07-01',1500.00,0.00,'2026-07-31',NULL,'ABERTA');
/*!40000 ALTER TABLE `fatura_cartao` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `logs_auditoria`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `logs_auditoria` WRITE;
/*!40000 ALTER TABLE `logs_auditoria` DISABLE KEYS */;
INSERT INTO `logs_auditoria` VALUES
(1,1,'ABERTURA_CONTA','Conta criada: 92436768','127.0.0.1','2026-07-04 19:18:50'),
(2,2,'ABERTURA_CONTA','Conta criada: 05389018','127.0.0.1','2026-07-04 19:20:09'),
(3,2,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 19:20:20'),
(4,2,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 19:53:16'),
(5,2,'CARTAO_SOLICITADO','Cartão pessoal: 2582 9780 6769 2143','127.0.0.1','2026-07-04 19:53:45'),
(6,2,'PAGAMENTO_REALIZADO','Tipo: PAGAMENTO_CONTA valor: 12','127.0.0.1','2026-07-04 19:54:29'),
(7,3,'ABERTURA_CONTA','Conta criada: 88627934','127.0.0.1','2026-07-04 20:12:01'),
(8,3,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 20:12:10'),
(9,3,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 20:26:55'),
(10,4,'ABERTURA_CONTA','Conta criada: 93501986','127.0.0.1','2026-07-04 20:38:30'),
(11,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 20:38:38'),
(12,4,'PAGAMENTO_REALIZADO','Tipo: PAGAMENTO_CONTA valor: 50','127.0.0.1','2026-07-04 20:39:20'),
(13,4,'PAGAMENTO_REALIZADO','Tipo: PAGAMENTO_IMPOSTO valor: 11','127.0.0.1','2026-07-04 20:39:35'),
(14,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 20:45:44'),
(15,4,'SOLICITACAO_EMPRESTIMO','Valor: 1000 Status: ANALISE_MANUAL','127.0.0.1','2026-07-04 20:46:09'),
(16,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 21:02:27'),
(17,4,'SOLICITACAO_EMPRESTIMO','Valor: 1000 Status: ANALISE_MANUAL','127.0.0.1','2026-07-04 21:02:57'),
(18,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 21:12:22'),
(19,4,'ALERTAS_CONFIGURADOS','Preferências salvas','127.0.0.1','2026-07-04 21:12:58'),
(20,4,'SOLICITACAO_EMPRESTIMO','Valor: 53000 Status: ANALISE_MANUAL','127.0.0.1','2026-07-04 21:13:08'),
(21,4,'SOLICITACAO_EMPRESTIMO','Valor: 53000 Status: ANALISE_MANUAL','127.0.0.1','2026-07-04 21:13:39'),
(22,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-04 21:19:30'),
(23,4,'SOLICITACAO_EMPRESTIMO','Valor: 54212 Status: ANALISE_MANUAL','127.0.0.1','2026-07-04 21:19:49'),
(24,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 15:23:14'),
(25,4,'TED_ENVIADO','Valor: 100 para Guilherme','127.0.0.1','2026-07-05 15:24:03'),
(26,4,'PIX_ENVIADO','Valor: 22 chave: 1423123123','127.0.0.1','2026-07-05 15:24:13'),
(27,4,'AGENDAMENTO_CRIADO','Tipo: PIX valor: 10000 data: 2026-08-05','127.0.0.1','2026-07-05 15:24:37'),
(28,4,'CARTAO_SOLICITADO','Cartão pessoal: 6463 9058 0674 4385','127.0.0.1','2026-07-05 15:24:54'),
(29,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 16:08:02'),
(30,4,'ABERTURA_NOVA_CONTA','Conta criada: 97569947','127.0.0.1','2026-07-05 16:08:16'),
(31,NULL,'COMPRA_CARTAO','Cartão: 6463 9058 0674 4385 Valor: 1000 Parcelas: 12','127.0.0.1','2026-07-05 16:10:18'),
(32,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 16:12:33'),
(33,4,'TRANSFERENCIA_INTERNA','Valor: 220000 para conta: 97569947','127.0.0.1','2026-07-05 16:12:47'),
(34,4,'ALERTAS_CONFIGURADOS','Preferências salvas','127.0.0.1','2026-07-05 16:15:58'),
(35,4,'TRANSFERENCIA_INTERNA','Valor: 226 para conta: 97569947','127.0.0.1','2026-07-05 16:16:16'),
(36,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 16:39:50'),
(37,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 16:41:56'),
(38,NULL,'COMPRA_CARTAO','Cartão: 6463 9058 0674 4385 Valor: 50 Parcelas: 5','127.0.0.1','2026-07-05 16:42:08'),
(39,NULL,'COMPRA_CARTAO','Cartão: 6463 9058 0674 4385 Valor: 5000 Parcelas: 12','127.0.0.1','2026-07-05 16:43:01'),
(40,NULL,'COMPRA_CARTAO','Cartão: 6463 9058 0674 4385 Valor: 150 Parcelas: 1','127.0.0.1','2026-07-05 16:43:25'),
(41,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 17:11:58'),
(42,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 17:18:56'),
(43,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 17:19:57'),
(44,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 17:27:28'),
(45,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 17:28:11'),
(46,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 17:43:15'),
(47,4,'SOLICITACAO_EMPRESTIMO','Valor: 12345 Status: NEGADO','127.0.0.1','2026-07-05 17:43:26'),
(48,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 17:44:22'),
(49,4,'SOLICITACAO_EMPRESTIMO','Valor: 11324 Status: NEGADO','127.0.0.1','2026-07-05 17:44:34'),
(50,4,'SOLICITACAO_EMPRESTIMO','Valor: 1500 Status: NEGADO','127.0.0.1','2026-07-05 17:45:43'),
(51,4,'SOLICITACAO_EMPRESTIMO','Valor: 100 Status: NEGADO','127.0.0.1','2026-07-05 17:45:54'),
(52,4,'SOLICITACAO_EMPRESTIMO','Valor: 100 Status: NEGADO','127.0.0.1','2026-07-05 17:46:31'),
(53,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 18:04:50'),
(54,4,'SOLICITACAO_EMPRESTIMO','Valor: 12345 Status: NEGADO','127.0.0.1','2026-07-05 18:05:16'),
(55,4,'SOLICITACAO_EMPRESTIMO','Valor: 12345 Status: NEGADO','127.0.0.1','2026-07-05 18:05:23'),
(56,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 18:06:48'),
(57,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 18:26:17'),
(58,4,'SOLICITACAO_EMPRESTIMO','Valor: 11222 Status: APROVADO','127.0.0.1','2026-07-05 18:26:35'),
(59,4,'SOLICITACAO_EMPRESTIMO','Valor: 11222 Status: NEGADO','127.0.0.1','2026-07-05 18:26:50'),
(60,4,'SOLICITACAO_EMPRESTIMO','Valor: 11222 Status: NEGADO','127.0.0.1','2026-07-05 18:27:02'),
(61,4,'SOLICITACAO_EMPRESTIMO','Valor: 100 Status: APROVADO','127.0.0.1','2026-07-05 18:27:11'),
(62,4,'CARTAO_SOLICITADO','Cartão pessoal: 7787 3280 8169 8361','127.0.0.1','2026-07-05 18:27:58'),
(63,NULL,'COMPRA_CARTAO','Cartão: 6463 9058 0674 4385 Valor: 2222 Parcelas: 2','127.0.0.1','2026-07-05 18:28:38'),
(64,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 18:31:15'),
(65,NULL,'COMPRA_CARTAO','Cartão: 7787 3280 8169 8361 Valor: 1500 Parcelas: 12','127.0.0.1','2026-07-05 18:31:38'),
(66,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 18:34:29'),
(67,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 18:49:31'),
(68,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 18:50:52'),
(69,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:05:52'),
(70,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:05:56'),
(71,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:06:17'),
(72,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:09:41'),
(73,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:09:46'),
(74,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:15:50'),
(75,4,'TRANSFERENCIA_INTERNA','Valor: 2000 para conta: 97569947','127.0.0.1','2026-07-05 19:16:47'),
(76,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:20:08'),
(77,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:21:40'),
(78,4,'TED_ENVIADO','Valor: 10 para nicole','127.0.0.1','2026-07-05 19:23:13'),
(79,4,'PIX_ENVIADO','Valor: 5000 chave: 12124123','127.0.0.1','2026-07-05 19:23:52'),
(80,4,'PAGAMENTO_REALIZADO','Tipo: PAGAMENTO_CONTA valor: 200000','127.0.0.1','2026-07-05 19:24:24'),
(81,4,'SOLICITACAO_EMPRESTIMO','Valor: 15000 Status: APROVADO','127.0.0.1','2026-07-05 19:25:32'),
(82,5,'ABERTURA_CONTA','Conta criada: 96023156','127.0.0.1','2026-07-05 19:27:40'),
(83,5,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-05 19:27:47'),
(84,5,'BOLETO_EMITIDO','Boleto: 001783279693759 valor: 150','127.0.0.1','2026-07-05 19:28:13'),
(85,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 15:27:02'),
(86,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 15:41:58'),
(87,4,'PIX_ENVIADO','Valor: 20 chave: 231231','127.0.0.1','2026-07-06 15:42:17'),
(88,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 15:49:05'),
(89,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:04:17'),
(90,4,'TED_ENVIADO','Valor: 100 para 38726734821','127.0.0.1','2026-07-06 16:04:53'),
(91,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:15:11'),
(92,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:19:56'),
(93,4,'ABERTURA_NOVA_CONTA','Conta criada: 12457753','127.0.0.1','2026-07-06 16:20:09'),
(94,4,'ABERTURA_NOVA_CONTA','Conta criada: 81303028','127.0.0.1','2026-07-06 16:20:22'),
(95,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:22:57'),
(96,4,'ABERTURA_NOVA_CONTA','Conta criada: 68504850','127.0.0.1','2026-07-06 16:23:01'),
(97,4,'ABERTURA_NOVA_CONTA','Conta criada: 33419643','127.0.0.1','2026-07-06 16:23:10'),
(98,2,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:23:34'),
(99,2,'ABERTURA_NOVA_CONTA','Conta criada: 52818904','127.0.0.1','2026-07-06 16:23:41'),
(100,2,'ABERTURA_NOVA_CONTA','Conta criada: 69104920','127.0.0.1','2026-07-06 16:23:57'),
(101,2,'ABERTURA_NOVA_CONTA','Conta criada: 38581091','127.0.0.1','2026-07-06 16:24:12'),
(102,2,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:24:21'),
(103,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:24:41'),
(104,4,'ABERTURA_NOVA_CONTA','Conta criada: 02904296','127.0.0.1','2026-07-06 16:25:42'),
(105,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:26:27'),
(106,4,'ABERTURA_NOVA_CONTA','Conta criada: 86112837','127.0.0.1','2026-07-06 16:26:30'),
(107,4,'ABERTURA_NOVA_CONTA','Conta criada: 38358133','127.0.0.1','2026-07-06 16:27:35'),
(108,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:27:55'),
(109,4,'ABERTURA_NOVA_CONTA','Conta criada: 63966105','127.0.0.1','2026-07-06 16:27:57'),
(110,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:29:05'),
(111,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:42:26'),
(112,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:42:33'),
(113,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:42:43'),
(114,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:43:29'),
(115,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 16:54:35'),
(116,4,'SOLICITACAO_EMPRESTIMO','Valor: 500 Status: APROVADO','127.0.0.1','2026-07-06 16:55:26'),
(117,4,'TED_ENVIADO','Valor: 200 para 14123123','127.0.0.1','2026-07-06 16:56:20'),
(118,4,'PIX_ENVIADO','Valor: 2424 chave: 2312312312','127.0.0.1','2026-07-06 16:56:26'),
(119,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 17:39:24'),
(120,4,'ATUALIZACAO_CADASTRO','Dados atualizados: gerson@gmail.com','127.0.0.1','2026-07-06 17:40:05'),
(121,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 18:13:25'),
(122,4,'TRANSFERENCIA_INTERNA','Valor: 10 para conta: 97569947','127.0.0.1','2026-07-06 18:13:46'),
(123,4,'ATUALIZACAO_CADASTRO','Dados atualizados: gerson@gmail.com','127.0.0.1','2026-07-06 18:14:03'),
(124,4,'TRANSFERENCIA_INTERNA','Valor: 26830 para conta: 97569947','127.0.0.1','2026-07-06 18:14:10'),
(125,5,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 18:16:17'),
(126,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 18:27:01'),
(127,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 18:29:28'),
(128,4,'LOGIN_SUCESSO','Login realizado de 127.0.0.1','127.0.0.1','2026-07-06 18:32:02');
/*!40000 ALTER TABLE `logs_auditoria` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `pagamento_lote`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `pagamento_lote` WRITE;
/*!40000 ALTER TABLE `pagamento_lote` DISABLE KEYS */;
INSERT INTO `pagamento_lote` VALUES
(1,5,'2026-07-05 19:28:39','FOLHA','2027-04-28','AGENDADO',0.00),
(2,5,'2026-07-06 18:16:46','FOLHA','2026-07-06','AGENDADO',0.00);
/*!40000 ALTER TABLE `pagamento_lote` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `pagamento_lote_item`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `pagamento_lote_item` WRITE;
/*!40000 ALTER TABLE `pagamento_lote_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `pagamento_lote_item` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `preferencias_alertas`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `preferencias_alertas` WRITE;
/*!40000 ALTER TABLE `preferencias_alertas` DISABLE KEYS */;
INSERT INTO `preferencias_alertas` VALUES
(1,4,1,10.00,0,0,1,1,1);
/*!40000 ALTER TABLE `preferencias_alertas` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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

--
-- Dumping data for table `transacao`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `transacao` WRITE;
/*!40000 ALTER TABLE `transacao` DISABLE KEYS */;
INSERT INTO `transacao` VALUES
(1,NULL,2,'EMPRESTIMO',1000.00,'2026-07-04 19:54:13','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(2,2,NULL,'PAGAMENTO_CONTA',12.00,'2026-07-04 19:54:29','PROCESSADA','Pagamento de conta: 1231231223...',NULL,NULL),
(3,4,NULL,'PAGAMENTO_CONTA',50.00,'2026-07-04 20:39:20','PROCESSADA','Pagamento de conta: 1231231241...',NULL,NULL),
(4,4,NULL,'PAGAMENTO_IMPOSTO',11.00,'2026-07-04 20:39:35','PROCESSADA','Pagamento PIX QR Code: 2123124124...',NULL,NULL),
(5,NULL,4,'EMPRESTIMO',50000.00,'2026-07-04 20:49:20','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(6,NULL,4,'EMPRESTIMO',5000.00,'2026-07-04 20:49:30','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(7,NULL,4,'EMPRESTIMO',5000.00,'2026-07-04 20:49:33','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(8,NULL,4,'EMPRESTIMO',1000.00,'2026-07-04 21:03:01','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(9,NULL,4,'EMPRESTIMO',222.00,'2026-07-04 21:04:29','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(10,NULL,4,'EMPRESTIMO',53000.00,'2026-07-04 21:12:42','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(11,NULL,4,'EMPRESTIMO',53000.00,'2026-07-04 21:12:50','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(12,NULL,4,'EMPRESTIMO',53000.00,'2026-07-04 21:13:14','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(13,4,NULL,'TED',100.00,'2026-07-05 15:24:03','PROCESSADA','TED para Guilherme','CTRL1783265043413','{\"banco\":\"237\", \"agencia\":\"0002\", \"conta\":\"22\", \"cpf_cnpj\":\"38746728312\"}'),
(14,4,NULL,'PIX',22.00,'2026-07-05 15:24:13','PROCESSADA','PIX para chave 1423123123','E2E1783265053555',NULL),
(15,4,5,'TRANSFERENCIA_INTERNA',220000.00,'2026-07-05 16:12:47','PROCESSADA','Transferência interna para conta 97569947',NULL,NULL),
(16,4,5,'TRANSFERENCIA_INTERNA',226.00,'2026-07-05 16:16:16','PROCESSADA','Transferência interna para conta 97569947',NULL,NULL),
(17,NULL,4,'EMPRESTIMO',15000.00,'2026-07-05 17:12:19','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(18,NULL,4,'EMPRESTIMO',15000.00,'2026-07-05 17:19:15','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(19,NULL,4,'EMPRESTIMO',15000.00,'2026-07-05 17:20:36','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(20,NULL,4,'EMPRESTIMO',12345.00,'2026-07-05 17:27:49','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(21,NULL,4,'EMPRESTIMO',12345.00,'2026-07-05 17:28:28','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(22,NULL,4,'EMPRESTIMO',123.00,'2026-07-05 17:43:41','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(23,NULL,4,'EMPRESTIMO',123.00,'2026-07-05 17:43:50','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(24,NULL,4,'EMPRESTIMO',12311.00,'2026-07-05 17:43:57','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(25,NULL,4,'EMPRESTIMO',12311.00,'2026-07-05 17:44:14','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(26,NULL,4,'EMPRESTIMO',1500.00,'2026-07-05 17:44:53','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(27,NULL,4,'EMPRESTIMO',100.00,'2026-07-05 17:46:25','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(28,NULL,4,'EMPRESTIMO',12345.00,'2026-07-05 18:05:03','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(29,NULL,4,'EMPRESTIMO',12345.00,'2026-07-05 18:05:09','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(30,NULL,4,'EMPRESTIMO',12342.00,'2026-07-05 18:05:30','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(31,NULL,4,'EMPRESTIMO',12342.00,'2026-07-05 18:05:36','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(32,NULL,4,'EMPRESTIMO',12342.00,'2026-07-05 18:05:42','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(33,NULL,4,'EMPRESTIMO',11123.00,'2026-07-05 18:05:50','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(34,NULL,4,'EMPRESTIMO',11123.00,'2026-07-05 18:05:56','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(35,NULL,4,'EMPRESTIMO',12322.00,'2026-07-05 18:07:01','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(36,NULL,4,'EMPRESTIMO',12322.00,'2026-07-05 18:07:07','PROCESSADA','Crédito de empréstimo #null',NULL,NULL),
(37,NULL,4,'EMPRESTIMO',11222.00,'2026-07-05 18:26:35','PROCESSADA','Crédito de empréstimo #13',NULL,NULL),
(38,NULL,4,'EMPRESTIMO',100.00,'2026-07-05 18:27:11','PROCESSADA','Crédito de empréstimo #16',NULL,NULL),
(39,4,5,'TRANSFERENCIA_INTERNA',2000.00,'2026-07-05 19:16:47','PROCESSADA','Transferência interna para conta 97569947',NULL,NULL),
(40,5,NULL,'TED',10.00,'2026-07-05 19:23:13','PROCESSADA','TED para nicole','CTRL1783279393623','{\"banco\":\"237\", \"agencia\":\"0008\", \"conta\":\"2123\", \"cpf_cnpj\":\"38726312321\"}'),
(41,5,NULL,'PIX',5000.00,'2026-07-05 19:23:52','PROCESSADA','PIX para chave 12124123','E2E1783279432714',NULL),
(42,4,NULL,'PAGAMENTO_CONTA',200000.00,'2026-07-05 19:24:24','PROCESSADA','Pagamento de conta: 1231231223...',NULL,NULL),
(43,NULL,4,'EMPRESTIMO',15000.00,'2026-07-05 19:25:31','PROCESSADA','Crédito de empréstimo #17',NULL,NULL),
(44,4,NULL,'PIX',20.00,'2026-07-06 15:42:17','PROCESSADA','PIX para chave 231231','E2E1783352537197',NULL),
(45,4,NULL,'TED',100.00,'2026-07-06 16:04:53','PROCESSADA','TED para 38726734821','CTRL1783353893173','{\"banco\":\"237\", \"agencia\":\"001\", \"conta\":\"22\", \"cpf_cnpj\":\"3\"}'),
(46,NULL,4,'EMPRESTIMO',500.00,'2026-07-06 16:55:26','PROCESSADA','Crédito de empréstimo #18',NULL,NULL),
(47,4,NULL,'TED',200.00,'2026-07-06 16:56:20','PROCESSADA','TED para 14123123','CTRL1783356980185','{\"banco\":\"237\", \"agencia\":\"0001\", \"conta\":\"123\", \"cpf_cnpj\":\"2\"}'),
(48,4,NULL,'PIX',2424.00,'2026-07-06 16:56:26','PROCESSADA','PIX para chave 2312312312','E2E1783356986030',NULL),
(49,4,5,'TRANSFERENCIA_INTERNA',10.00,'2026-07-06 18:13:45','PROCESSADA','Transferência interna para conta 97569947',NULL,NULL),
(50,4,5,'TRANSFERENCIA_INTERNA',26830.00,'2026-07-06 18:14:10','PROCESSADA','Transferência interna para conta 97569947',NULL,NULL);
/*!40000 ALTER TABLE `transacao` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-07-06 15:58:51
