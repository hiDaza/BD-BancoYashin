/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author daza
 */
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatadorUtil {
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String formatarMoeda(double valor) {
        return MOEDA.format(valor);
    }

    public static String formatarMoeda(BigDecimal valor) {
        return MOEDA.format(valor.doubleValue());
    }

    public static String formatarData(LocalDate data) {
        if (data == null) return "";
        return data.format(DATA);
    }

    public static String formatarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) return "";
        return dataHora.format(DATA_HORA);
    }

    public static LocalDate parseData(String dataStr) throws Exception {
        if (dataStr == null || dataStr.trim().isEmpty()) return null;
        return LocalDate.parse(dataStr, DATA);
    }
}
