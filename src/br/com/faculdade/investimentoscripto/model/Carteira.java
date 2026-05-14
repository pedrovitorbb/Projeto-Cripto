package br.com.faculdade.investimentocripto.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Carteira {
    private Long id;
    private BigDecimal saldoTotalGeral;
    private List<Transacao> listaTransacoes;
    private List<AtivoCripto> listaAtivos;


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

        if (t.getValorTotal() != null) {
            this.saldoTotalGeral = this.saldoTotalGeral.add(t.getValorTotal());
        }
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
}