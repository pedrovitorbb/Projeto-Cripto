package br.com.faculdade.investimentocripto.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Empresa {
    private Long id;
    private String razaoSocial;
    private String cnpj;
    private List<Carteira> carteiras;

    public Empresa(Long id, String razaoSocial, String cnpj) {
        this.id = id;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.carteiras = new ArrayList<>();
    }

    public BigDecimal calcularPatrimonioTotal() {
        BigDecimal soma = BigDecimal.ZERO;

        for (int i = 0; i < carteiras.size(); i++) {
            soma = soma.add(carteiras.get(i).getSaldoTotalGeral());
        }
        return soma;
    }

    public void adicionarCarteira(Carteira c) {
        this.carteiras.add(c);
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }
}