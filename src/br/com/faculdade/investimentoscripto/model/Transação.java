package br.com.faculdade.investimentocripto.model;

import java.time.LocalDateTime;

/**
 * Entidade Associativa que conecta uma Carteira a um AtivoCripto.
 * Cada transação representa uma compra/venda registrada na blockchain.
 */
public class Transacao {

    public enum Tipo { COMPRA, VENDA }

    private String ticker;             // p/ consulta rápida (ex.: "BTC")
    private double quantidade;
    private double precoMedioCompra;   // preço médio pago por unidade
    private double valorUnitario;      // preço unitário no momento da transação
    private String hashBlockchain;     // hash da transação on-chain
    private Tipo tipo;
    private LocalDateTime dataHora;

    // Referências da associação (Carteira - AtivoCripto)
    private Carteira carteira;
    private AtivoCripto ativo;

    public Transacao(Carteira carteira, AtivoCripto ativo, Tipo tipo,
                     double quantidade, double precoMedioCompra,
                     double valorUnitario, String hashBlockchain) {
        this.carteira = carteira;
        this.ativo = ativo;
        this.ticker = ativo.getTicker();
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.precoMedioCompra = precoMedioCompra;
        this.valorUnitario = valorUnitario;
        this.hashBlockchain = hashBlockchain;
        this.dataHora = LocalDateTime.now();

        // Mantém a associação consistente
        ativo.adicionarTransacao(this);
    }

    public double getValorTotal() {
        return quantidade * valorUnitario;
    }

    // Getters & Setters
    public String getTicker() { return ticker; }
    public double getQuantidade() { return quantidade; }
    public double getPrecoMedioCompra() { return precoMedioCompra; }
    public double getValorUnitario() { return valorUnitario; }
    public String getHashBlockchain() { return hashBlockchain; }
    public Tipo getTipo() { return tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public Carteira getCarteira() { return carteira; }
    public AtivoCripto getAtivo() { return ativo; }

    @Override
    public String toString() {
        return String.format("[%s] %s %.8f %s @ %.2f (hash=%s)",
                tipo, ticker, quantidade, ticker, valorUnitario,
                hashBlockchain.substring(0, Math.min(10, hashBlockchain.length())) + "...");
    }
}
