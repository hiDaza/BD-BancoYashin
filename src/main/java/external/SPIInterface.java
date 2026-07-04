/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package external;

/**
 *
 * @author daza
 */
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class SPIInterface {


    private Map<String, Map<String, String>> baseChaves = new HashMap<>();

    public SPIInterface() {

        adicionarChave("12345678901", "cpf", "João Silva");
        adicionarChave("98765432100", "cpf", "Maria Oliveira");
        adicionarChave("empresa@email.com", "email", "Empresa XYZ");
        adicionarChave("11999999999", "telefone", "Carlos Souza");
        adicionarChave("chave-aleatoria-001", "aleatoria", "Ana Costa");
    }

    private void adicionarChave(String chave, String tipo, String nome) {
        Map<String, String> dados = new HashMap<>();
        dados.put("tipo", tipo);
        dados.put("nome", nome);
        dados.put("chave", chave);
        baseChaves.put(chave, dados);
    }


    public String validarChavePix(String chavePix) throws Exception {
        // Simula validação com timeout e tentativas
        simularLatencia();

        if (chavePix == null || chavePix.trim().isEmpty()) {
            return null;
        }

        Map<String, String> dados = baseChaves.get(chavePix);
        if (dados == null) {
            return null;
        }

        // Retorna em formato JSON (simulado)
        return "{\"nome\":\"" + dados.get("nome") + "\", " +
               "\"tipo\":\"" + dados.get("tipo") + "\", " +
               "\"chave\":\"" + dados.get("chave") + "\", " +
               "\"instituicao\":\"BANCO YASHIN\"}";
    }


    public String enviarPix(String chaveDestino, BigDecimal valor, String idContaOrigem) throws Exception {
        // Valida a chave primeiro
        String dados = validarChavePix(chaveDestino);
        if (dados == null) {
            throw new Exception("Chave PIX não encontrada no SPI");
        }

        // Simula processamento
        simularLatencia();

        // Gera EndToEndId único
        String endToEndId = "E2E" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
        System.out.println("[SPI] PIX processado: " + endToEndId + " | Origem: " + idContaOrigem + 
                           " | Destino: " + chaveDestino + " | Valor: " + valor);

        return endToEndId;
    }


    private void simularLatencia() throws Exception {

        if (Math.random() < 0.05) {
            throw new Exception("Falha de comunicação com SPI (timeout)");
        }
        try {
            Thread.sleep((long) (100 + Math.random() * 200)); // 100-300ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
