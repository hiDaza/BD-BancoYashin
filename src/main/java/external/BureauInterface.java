/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package external;

/**
 *
 * @author daza
 */
import java.util.*;
import java.util.stream.Collectors;


public class BureauInterface {

    // Simulação de base de scores e restrições
    private Map<String, BureauData> baseBureau = new HashMap<>();

    private static class BureauData {
        int score;
        List<String> restricoes;

        BureauData(int score, List<String> restricoes) {
            this.score = score;
            this.restricoes = restricoes != null ? restricoes : new ArrayList<>();
        }
    }

    public BureauInterface() {
        // Popula exemplos
        baseBureau.put("12345678901", new BureauData(750, Arrays.asList("Restrição SPC 2023-05", "Serasa: dívida R$ 500")));
        baseBureau.put("98765432100", new BureauData(420, Arrays.asList("Restrição Serasa 2024-01", "SPC: protesto")));
        baseBureau.put("11122233344", new BureauData(880, new ArrayList<>()));
        baseBureau.put("55566677788", new BureauData(620, Arrays.asList("Boa Vista: pendência")));
    }


    public int consultarScore(String cpfCnpj) throws Exception {
        simularLatencia();

        BureauData data = baseBureau.get(cpfCnpj);
        if (data != null) {
            return data.score;
        }

        // Para CPF/CNPJ não cadastrado, gera score aleatório realista
        Random r = new Random();
        int score = 300 + r.nextInt(700); // 300 a 1000
        System.out.println("[Bureau] Score gerado para " + cpfCnpj + ": " + score);
        return score;
    }


    public List<String> consultarRestricoes(String cpfCnpj) throws Exception {
        simularLatencia();

        BureauData data = baseBureau.get(cpfCnpj);
        if (data != null) {
            return new ArrayList<>(data.restricoes);
        }
        return new ArrayList<>();
    }


    public boolean possuiRestricao(String cpfCnpj) throws Exception {
        List<String> restricoes = consultarRestricoes(cpfCnpj);
        return !restricoes.isEmpty();
    }

 
    public String obterRelatorioCompleto(String cpfCnpj) throws Exception {
        int score = consultarScore(cpfCnpj);
        List<String> restricoes = consultarRestricoes(cpfCnpj);
        String restStr = restricoes.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));

        return "{\"score\": " + score + ", " +
               "\"restricoes\": [" + restStr + "], " +
               "\"data_consulta\": \"" + java.time.LocalDate.now() + "\"}";
    }

    private void simularLatencia() throws Exception {
        if (Math.random() < 0.03) {
            throw new Exception("Falha de comunicação com Bureau de Crédito");
        }
        try {
            Thread.sleep((long) (50 + Math.random() * 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
