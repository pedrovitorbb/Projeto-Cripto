package br.com.faculdade.investimentoscripto.exception;

public class SaldoInsuficienteException extends Exception {

    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
