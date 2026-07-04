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


public class SCRInterface {


    private Map<String, Map<String, Object>> baseSCR = new HashMap<>();

    public SCRInterface() {
        // Popula alguns exemplos
        Map<String, Object> dados1 = new HashMap<>();
        dados1.put("renda_comprometida", new BigDecimal("0.25"));
        dados1.put("total_dividas", new BigDecimal("5000.00"));
        dados1.put("quantidade_operacoes", 2);
        baseSCR.put("12345678901", dados1);

        Map<String, Object> dados2 = new HashMap<>();
        dados2.put("renda_comprometida", new BigDecimal("0.45"));
        dados2.put("total_dividas", new BigDecimal("15000.00"));
        dados2.put("quantidade_operacoes", 5);
        baseSCR.put("98765432100", dados2);

        Map<String, Object> dados3 = new HashMap<>();
        dados3.put("renda_comprometida", new BigDecimal("0.10"));
        dados3.put("total_dividas", new BigDecimal("1000.00"));
        dados3.put("quantidade_operacoes", 1);
        baseSCR.put("11122233344", dados3);
    }


    public String consultarCredito(String cpfCnpj, String autorizacao) throws Exception {
        if (autorizacao == null || autorizacao.isEmpty()) {
            throw new Exception("Autorização do cliente é obrigatória para consulta ao SCR");
        }

        simularLatencia();

        Map<String, Object> dados = baseSCR.get(cpfCnpj);
        if (dados == null) {
            // Gera dados aleatórios para CPF/CNPJ não cadastrado
            BigDecimal renda = new BigDecimal(String.format("%.2f", 0.10 + Math.random() * 0.50));
            return "{\"renda_comprometida\": " + renda + ", " +
                   "\"total_dividas\": " + (500 + (int)(Math.random() * 20000)) + ", " +
                   "\"quantidade_operacoes\": " + (1 + (int)(Math.random() * 8)) + "}";
        }

        return "{\"renda_comprometida\": " + dados.get("renda_comprometida") + ", " +
               "\"total_dividas\": " + dados.get("total_dividas") + ", " +
               "\"quantidade_operacoes\": " + dados.get("quantidade_operacoes") + "}";
    }


    public BigDecimal obterRendaComprometida(String cpfCnpj, String autorizacao) throws Exception {
        String json = consultarCredito(cpfCnpj, autorizacao);
        // Extração simples (em produção usaria JSON parser)
        int idx = json.indexOf("renda_comprometida");
        if (idx > 0) {
            String sub = json.substring(idx + 20);
            int fim = sub.indexOf(",");
            if (fim < 0) fim = sub.indexOf("}");
            String valor = sub.substring(0, fim).trim();
            return new BigDecimal(valor);
        }
        return new BigDecimal("0.30"); // fallback
    }

    private void simularLatencia() throws Exception {
        if (Math.random() < 0.03) {
            throw new Exception("Falha de comunicação com SCR");
        }
        try {
            Thread.sleep((long) (50 + Math.random() * 150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
