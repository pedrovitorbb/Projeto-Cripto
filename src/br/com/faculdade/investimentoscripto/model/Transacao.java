package br.com.faculdade.investimentoscripto.model;

public class Transacao {

    private int id;
    private String tipo;
    private double valor;
    private String dataHora;
    private String descricao;
    private String simboloAtivo;

    public Transacao() {
    }

    public Transacao(int id, String tipo, double valor, String dataHora, String descricao, String simboloAtivo) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
        this.descricao = descricao;
        this.simboloAtivo = simboloAtivo;
    }

    public void registrar() {
        System.out.println("Transacao registrada com sucesso!");
        System.out.println("ID: " + id);
        System.out.println("Tipo: " + tipo);
        System.out.println("Ativo: " + simboloAtivo);
        System.out.println("Valor: R$ " + String.format("%.2f", valor));
        System.out.println("Data/Hora: " + dataHora);
        System.out.println("Descricao: " + descricao);
    }

    public String getResumo() {
        return "[" + dataHora + "] " + tipo + " | " + simboloAtivo + " | R$ " + String.format("%.2f", valor);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSimboloAtivo() {
        return simboloAtivo;
    }

    public void setSimboloAtivo(String simboloAtivo) {
        this.simboloAtivo = simboloAtivo;
    }

    @Override
    public String toString() {
        return getResumo();
    }
}
