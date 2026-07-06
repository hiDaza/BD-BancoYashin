/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author daza
 */
import java.util.regex.Pattern;

public class ValidadorUtil {

    public static boolean validarCPF(String cpf) {
        String numeros = cpf.replaceAll("\\D", "");
        if (numeros.length() != 11) return false;
        if (numeros.matches("(\\d)\\1{10}")) return false;

        // Cálculo dos dígitos verificadores (simplificado)
        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (numeros.charAt(i) - '0') * (10 - i);
            }
            int resto = 11 - (soma % 11);
            int digito1 = (resto >= 10) ? 0 : resto;
            if (digito1 != (numeros.charAt(9) - '0')) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (numeros.charAt(i) - '0') * (11 - i);
            }
            resto = 11 - (soma % 11);
            int digito2 = (resto >= 10) ? 0 : resto;
            return digito2 == (numeros.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validarCNPJ(String cnpj) {
        String numeros = cnpj.replaceAll("\\D", "");
        if (numeros.length() != 14) return false;
        if (numeros.matches("(\\d)\\1{13}")) return false;

        // Cálculo simplificado dos dígitos
        try {
            int[] pesos1 = {5,4,3,2,9,8,7,6,5,4,3,2};
            int[] pesos2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += (numeros.charAt(i) - '0') * pesos1[i];
            }
            int resto = soma % 11;
            int digito1 = (resto < 2) ? 0 : 11 - resto;
            if (digito1 != (numeros.charAt(12) - '0')) return false;

            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += (numeros.charAt(i) - '0') * pesos2[i];
            }
            resto = soma % 11;
            int digito2 = (resto < 2) ? 0 : 11 - resto;
            return digito2 == (numeros.charAt(13) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validarEmail(String email) {
        if (email == null) return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(regex, email);
    }

    public static boolean validarTelefone(String telefone) {
        String numeros = telefone.replaceAll("\\D", "");
        return numeros.length() >= 10 && numeros.length() <= 11;
    }
}
