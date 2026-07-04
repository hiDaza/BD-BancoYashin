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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;


public class STRInterface {


    public String enviarTED(String bancoDestino, String agenciaDestino, String contaDestino,
                            String cpfCnpjBeneficiario, String nomeBeneficiario, BigDecimal valor) throws Exception {
        LocalDateTime agora = LocalDateTime.now();
        LocalTime horario = agora.toLocalTime();
        boolean diaUtil = agora.getDayOfWeek().getValue() <= 5; // Seg-Sex
        boolean horarioValido = horario.isBefore(LocalTime.of(17, 0));

        if (!diaUtil || !horarioValido) {
            System.out.println("[STR] TED solicitado fora do horário/dia útil. Será processado no próximo dia útil.");
        }

        simularLatencia();

        String numeroControle = "CTRL" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
        System.out.println("[STR] TED processado: " + numeroControle + " | Destino: " + bancoDestino + 
                           " Ag: " + agenciaDestino + " Conta: " + contaDestino + " | Valor: " + valor);

        return numeroControle;
    }


    private void simularLatencia() throws Exception {
        if (Math.random() < 0.05) {
            throw new Exception("Falha de comunicação com STR (timeout)");
        }
        try {
            Thread.sleep((long) (100 + Math.random() * 300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
