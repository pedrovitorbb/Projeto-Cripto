package br.com.faculdade.investimentoscripto.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Ativo cripto. Alem dos campos de dominio, guarda os dados da posicao do ativo
 * em uma carteira (T_SIP_ATIVO_CRIPTO, chave composta idCarteira + ticker).
 *
 * <p>Os campos {@code nome}, {@code cotacaoAtual} e {@code transacoes} existem apenas
 * no dominio: nao ha coluna para eles na tabela e o AtivoCriptoDAO nao os persiste.</p>
 */
public class AtivoCripto {

    private Long idCarteira;
    private BigDecimal quantidade;
    private BigDecimal precoMedioCompra;
    private BigDecimal valorTotalInvestido;

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

    /**
     * Construtor de persistencia: usado pelo AtivoCriptoDAO ao ler o banco ou ao
     * montar um ativo para gravar. Nome e cotacao ficam vazios (nao ha coluna).
     */
    public AtivoCripto(Long idCarteira, String ticker, BigDecimal quantidade,
                       BigDecimal precoMedioCompra, BigDecimal valorTotalInvestido) {
        this.idCarteira = idCarteira;
        this.ticker = ticker;
        this.quantidade = quantidade;
        this.precoMedioCompra = precoMedioCompra;
        this.valorTotalInvestido = valorTotalInvestido;
        this.transacoes = new ArrayList<>();
    }

    // =========================================================
    //  OVERLOAD
    // =========================================================

    /** 1) Lucro/Prejuízo de UMA transação específica usando a cotação atual do ativo. */
    public double calcularLucroPrejuizo(Transacao t) {
        return (this.cotacaoAtual - t.getPrecoMedioCompra()) * t.getQuantidade().doubleValue();
    }

    /** 2) Lucro/Prejuízo informando manualmente o preço de venda. */
    public double calcularLucroPrejuizo(Transacao t, double precoVenda) {
        return (precoVenda - t.getPrecoMedioCompra()) * t.getQuantidade().doubleValue();
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
        return transacoes.stream().mapToDouble(t -> t.getQuantidade().doubleValue()).sum();
    }

    // Getters & Setters
    public Long getIdCarteira() { return idCarteira; }
    public void setIdCarteira(Long idCarteira) { this.idCarteira = idCarteira; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoMedioCompra() { return precoMedioCompra; }
    public void setPrecoMedioCompra(BigDecimal precoMedioCompra) { this.precoMedioCompra = precoMedioCompra; }

    public BigDecimal getValorTotalInvestido() { return valorTotalInvestido; }
    public void setValorTotalInvestido(BigDecimal valorTotalInvestido) { this.valorTotalInvestido = valorTotalInvestido; }

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
