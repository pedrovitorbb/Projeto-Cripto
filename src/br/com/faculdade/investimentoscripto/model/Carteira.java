package br.com.faculdade.investimentoscripto.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Carteira {
    private Long id;
    private BigDecimal saldoTotalGeral;
    private List<Transacao> listaTransacoes;
    private List<AtivoCripto> listaAtivos;
    private Investidor investidor;
    private Empresa empresa;

    /** Construtor completo: usado pelo CarteiraDAO ao ler o banco (FKs de T_SIP_CARTEIRA). */
    public Carteira(Long id, BigDecimal saldoTotalGeral, Investidor investidor, Empresa empresa) {
        this.id = id;
        this.saldoTotalGeral = saldoTotalGeral;
        this.investidor = investidor;
        this.empresa = empresa;
        this.listaTransacoes = new ArrayList<>();
        this.listaAtivos = new ArrayList<>();
    }

    public Carteira(Long id) {
        this.id = id;
        this.saldoTotalGeral = BigDecimal.ZERO;
        this.listaTransacoes = new ArrayList<>();
        this.listaAtivos = new ArrayList<>();
    }


    public Carteira() {
        this.saldoTotalGeral = BigDecimal.ZERO;
        this.listaTransacoes = new ArrayList<>();
        this.listaAtivos = new ArrayList<>();
    }

    public void registrarTransacao(Transacao t) {
        listaTransacoes.add(t);
        this.saldoTotalGeral = this.saldoTotalGeral.add(BigDecimal.valueOf(t.getValorTotal()));
    }

    public AtivoCripto buscarAtivo(String ticker) {
        for (AtivoCripto a : listaAtivos) {
            if (a.getTicker().equalsIgnoreCase(ticker)) {
                return a;
            }
        }
        return null;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getSaldoTotalGeral() {
        return saldoTotalGeral;
    }

    public void setSaldoTotalGeral(BigDecimal saldoTotalGeral) {
        this.saldoTotalGeral = saldoTotalGeral;
    }

    public List<Transacao> getListaTransacoes() {
        return listaTransacoes;
    }

    public void setListaTransacoes(List<Transacao> listaTransacoes) {
        this.listaTransacoes = listaTransacoes;
    }

    public List<AtivoCripto> getListaAtivos() {
        return listaAtivos;
    }

    public void setListaAtivos(List<AtivoCripto> listaAtivos) {
        this.listaAtivos = listaAtivos;
    }

    public Investidor getInvestidor() {
        return investidor;
    }

    public void setInvestidor(Investidor investidor) {
        this.investidor = investidor;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    /** Nao imprime listas nem os objetos completos, para evitar ciclos e dados sensiveis. */
    @Override
    public String toString() {
        return "Carteira{id=" + id
                + ", saldoTotalGeral=" + saldoTotalGeral
                + ", idInvestidor=" + (investidor == null ? null : investidor.getId())
                + ", idEmpresa=" + (empresa == null ? null : empresa.getId())
                + "}";
    }
}
