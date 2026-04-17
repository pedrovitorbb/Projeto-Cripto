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

    public BigDecimal getSaldoTotalGeral() {
        return saldoTotalGeral;
    }
}