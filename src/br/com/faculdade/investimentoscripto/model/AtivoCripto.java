package br.com.faculdade.investimentoscripto.model;

public class AtivoCripto {

    private String simbolo;
    private String nome;
    private double cotacaoAtual;
    private double variacao24h;

    public AtivoCripto() {
    }

    public AtivoCripto(String simbolo, String nome, double cotacaoAtual, double variacao24h) {
        this.simbolo = simbolo;
        this.nome = nome;
        this.cotacaoAtual = cotacaoAtual;
        this.variacao24h = variacao24h;
    }

    public void atualizarCotacao(double novaCotacao) {
        this.variacao24h = ((novaCotacao - this.cotacaoAtual) / this.cotacaoAtual) * 100;
        this.cotacaoAtual = novaCotacao;
    }

    public void exibirDetalhes() {
        System.out.println("=== Ativo Cripto ===");
        System.out.println("Simbolo: " + simbolo);
        System.out.println("Nome: " + nome);
        System.out.println("Cotacao Atual: R$ " + String.format("%.2f", cotacaoAtual));
        System.out.println("Variacao 24h: " + String.format("%.2f", variacao24h) + "%");
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getCotacaoAtual() {
        return cotacaoAtual;
    }

    public void setCotacaoAtual(double cotacaoAtual) {
        this.cotacaoAtual = cotacaoAtual;
    }

    public double getVariacao24h() {
        return variacao24h;
    }

    public void setVariacao24h(double variacao24h) {
        this.variacao24h = variacao24h;
    }

    @Override
    public String toString() {
        return simbolo + " - " + nome + " | R$ " + String.format("%.2f", cotacaoAtual);
    }
}
