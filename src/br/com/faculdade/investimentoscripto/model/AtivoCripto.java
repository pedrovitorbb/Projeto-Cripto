package br.com.faculdade.investimentoscripto.model;

import java.util.ArrayList;
import java.util.List;

public class AtivoCripto {

    private String ticker;            // Ex.: BTC, ETH, SOL
    private String nome;              // Ex.: Bitcoin
    private double cotacaoAtual;      // Preço de mercado atual (R$ ou USD)
    private List<Transacao> transacoes;

    public AtivoCripto(String ticker, String nome, double cotacaoAtual) {
        this.ticker = ticker;
        this.nome = nome;
        this.cotacaoAtual = cotacaoAtual;
        this.transacoes = new ArrayList<>();
    }

    // =========================================================
    //  OVERLOAD
    // =========================================================

    /** 1) Lucro/Prejuízo de UMA transação específica usando a cotação atual do ativo. */
    public double calcularLucroPrejuizo(Transacao t) {
        return (this.cotacaoAtual - t.getPrecoMedioCompra()) * t.getQuantidade();
    }

    /** 2) Lucro/Prejuízo informando manualmente o preço de venda. */
    public double calcularLucroPrejuizo(Transacao t, double precoVenda) {
        return (precoVenda - t.getPrecoMedioCompra()) * t.getQuantidade();
    }

    /** 3) Lucro/Prejuízo de uma quantidade arbitrária dado um preço médio de compra. */
    public double calcularLucroPrejuizo(double quantidade, double precoMedioCompra) {
        return (this.cotacaoAtual - precoMedioCompra) * quantidade;
    }

    /** 4) Lucro/Prejuízo CONSOLIDADO de todas as transações do ativo. */
    public double calcularLucroPrejuizo() {
        double total = 0.0;
        for (Transacao t : transacoes) {
            total += calcularLucroPrejuizo(t);
        }
        return total;
    }

    // =========================================================
    //  Auxiliares
    // =========================================================

    public void adicionarTransacao(Transacao t) {
        this.transacoes.add(t);
    }

    public double getQuantidadeTotal() {
        return transacoes.stream().mapToDouble(Transacao::getQuantidade).sum();
    }

    // Getters & Setters
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getCotacaoAtual() { return cotacaoAtual; }
    public void setCotacaoAtual(double cotacaoAtual) { this.cotacaoAtual = cotacaoAtual; }

    public List<Transacao> getTransacoes() { return transacoes; }

    @Override
    public String toString() {
        return String.format("%s (%s) - Cotação: %.2f | Qtd total: %.8f",
                nome, ticker, cotacaoAtual, getQuantidadeTotal());
    }
}
