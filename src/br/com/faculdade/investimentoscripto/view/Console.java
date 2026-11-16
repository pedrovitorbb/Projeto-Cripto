package br.com.faculdade.investimentoscripto.view;

import java.math.BigDecimal;
import java.util.Scanner;

public final class Console {

    private static final Scanner SCANNER = new Scanner(System.in);

    private Console() {
    }

    public static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return SCANNER.nextLine().trim();
    }

    public static String lerTextoObrigatorio(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este campo é obrigatório.");
        }
    }

    public static int lerInt(String mensagem) {
        while (true) {
            String entrada = lerTexto(mensagem);
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    public static BigDecimal lerDecimal(String mensagem) {
        while (true) {
            String entrada = lerTexto(mensagem).replace(',', '.');
            try {
                return new BigDecimal(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um valor decimal.");
            }
        }
    }

    public static boolean confirmar(String mensagem) {
        while (true) {
            String resposta = lerTexto(mensagem);
            if (resposta.equalsIgnoreCase("S")) {
                return true;
            }
            if (resposta.equalsIgnoreCase("N")) {
                return false;
            }
            System.out.println("Resposta inválida. Digite S ou N.");
        }
    }
}
