package br.com.faculdade.investimentoscripto.model;

import java.time.LocalDateTime;

public class Transacao {

    public enum Tipo { COMPRA, VENDA }

    private String ticker;
    private double quantidade;
    private double precoMedioCompra;
    private double valorUnitario;
    private String hashBlockchain;
    private Tipo tipo;
    private LocalDateTime dataHora;

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

        ativo.adicionarTransacao(this);
    }

    public double getValorTotal() {
        return quantidade * valorUnitario;
    }

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
        return String.format("[%s] %s %.8f @ %.2f (hash=%s)",
                tipo, ticker, quantidade, valorUnitario,
                hashBlockchain.substring(0, Math.min(10, hashBlockchain.length())) + "...");
    }
}
