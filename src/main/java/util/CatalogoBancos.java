/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author daza
 */
public class CatalogoBancos {
    private static final Map<String, BancoInfo> BANCOS = new LinkedHashMap<>();

    static {
        // Banco Central (referência)
        BANCOS.put("000", new BancoInfo("Banco Central do Brasil", List.of()));
        BANCOS.put("001", new BancoInfo("Banco do Brasil", Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011", "0012")));
        BANCOS.put("033", new BancoInfo("Santander", Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010")));
        BANCOS.put("104", new BancoInfo("Caixa Econômica Federal", Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011", "0012")));
        BANCOS.put("237", new BancoInfo("Bradesco", Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010")));
        BANCOS.put("341", new BancoInfo("Itaú Unibanco", Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011")));
        BANCOS.put("422", new BancoInfo("Banco Safra", Arrays.asList("0001", "0002", "0003", "0004", "0005")));
        BANCOS.put("748", new BancoInfo("Banco Cooperativo Sicredi", Arrays.asList("0001", "0002", "0003")));
        BANCOS.put("756", new BancoInfo("Banco Cooperativo do Brasil - Sicoob", Arrays.asList("0001", "0002", "0003")));
        BANCOS.put("260", new BancoInfo("Nu Pagamentos S.A. - Nubank", Arrays.asList("0001", "0002", "0003")));
        BANCOS.put("323", new BancoInfo("Mercado Pago", Arrays.asList("0001", "0002")));
        BANCOS.put("077", new BancoInfo("Banco Inter", Arrays.asList("0001", "0002", "0003", "0004")));
        // Adicione mais bancos conforme necessário
    }

    private static final List<String> AGENCIAS_YASHIN = Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011", "0012");

    public static List<String> getAgenciasYashin() {
        return Collections.unmodifiableList(AGENCIAS_YASHIN);
    }

    public static boolean isBancoValido(String codigo) {
        return BANCOS.containsKey(codigo);
    }

    public static String getNomeBanco(String codigo) {
        BancoInfo info = BANCOS.get(codigo);
        return info != null ? info.nome : null;
    }

    public static List<String> getAgencias(String codigoBanco) {
        BancoInfo info = BANCOS.get(codigoBanco);
        return info != null ? info.agencias : Collections.emptyList();
    }

    public static List<String> getListaCodigosBancos() {
        return new ArrayList<>(BANCOS.keySet());
    }

    public static String getDescricaoBanco(String codigo) {
        String nome = getNomeBanco(codigo);
        return nome != null ? codigo + " - " + nome : codigo;
    }

    private static class BancoInfo {
        String nome;
        List<String> agencias;

        BancoInfo(String nome, List<String> agencias) {
            this.nome = nome;
            this.agencias = agencias;
        }
    }
}