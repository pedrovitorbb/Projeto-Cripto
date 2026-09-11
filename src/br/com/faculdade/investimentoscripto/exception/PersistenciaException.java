package br.com.faculdade.investimentoscripto.exception;

/**
 * Erro ao acessar o banco de dados. Traduz a SQLException do driver Oracle
 * em uma mensagem entendivel pelo usuario, mantendo a causa original.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
