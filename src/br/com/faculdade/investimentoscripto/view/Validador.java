package br.com.faculdade.investimentoscripto.view;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public final class Validador {

    private static final Pattern PADRAO_EMAIL = Pattern.compile(
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@"
                    + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");

    private Validador() {
    }

    public static boolean validarCPF(String cpf) {
        String numeros = removerFormatacaoNumerica(cpf);
        if (numeros.length() != 11 || todosDigitosIguais(numeros)) {
            return false;
        }

        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(numeros.charAt(i)) * (10 - i);
        }
        int primeiroDigito = calcularDigitoCPF(soma);
        if (primeiroDigito != Character.getNumericValue(numeros.charAt(9))) {
            return false;
        }

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(numeros.charAt(i)) * (11 - i);
        }
        int segundoDigito = calcularDigitoCPF(soma);
        return segundoDigito == Character.getNumericValue(numeros.charAt(10));
    }

    public static boolean validarCNPJ(String cnpj) {
        String numeros = removerFormatacaoNumerica(cnpj);
        if (numeros.length() != 14 || todosDigitosIguais(numeros)) {
            return false;
        }

        int[] pesosPrimeiro = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundo = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int primeiroDigito = calcularDigitoCNPJ(numeros, pesosPrimeiro);
        if (primeiroDigito != Character.getNumericValue(numeros.charAt(12))) {
            return false;
        }

        int segundoDigito = calcularDigitoCNPJ(numeros, pesosSegundo);
        return segundoDigito == Character.getNumericValue(numeros.charAt(13));
    }

    public static boolean validarEmail(String email) {
        if (email == null || email.isBlank() || !email.equals(email.trim())) {
            return false;
        }
        return PADRAO_EMAIL.matcher(email).matches();
    }

    public static boolean validarTextoObrigatorio(String texto) {
        return texto != null && !texto.isBlank();
    }

    public static boolean validarValorPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean validarValorNaoNegativo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean validarDuasCasasDecimais(BigDecimal valor) {
        return valor != null && valor.stripTrailingZeros().scale() <= 2;
    }

    public static String removerFormatacaoNumerica(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }

    public static String gerarHashSenha(String senha) {
        if (senha == null) {
            throw new IllegalArgumentException("A senha não pode ser nula.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexadecimal = new StringBuilder();
            for (byte parte : hash) {
                hexadecimal.append(String.format("%02x", parte));
            }
            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não está disponível.", e);
        }
    }

    private static int calcularDigitoCPF(int soma) {
        int resultado = 11 - (soma % 11);
        return resultado >= 10 ? 0 : resultado;
    }

    private static int calcularDigitoCNPJ(String numeros, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(numeros.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosDigitosIguais(String numeros) {
        char primeiro = numeros.charAt(0);
        for (int i = 1; i < numeros.length(); i++) {
            if (numeros.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }
}
