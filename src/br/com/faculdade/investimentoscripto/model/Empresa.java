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


    public Empresa() {
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



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public List<Carteira> getCarteiras() {
        return carteiras;
    }

    public void setCarteiras(List<Carteira> carteiras) {
        this.carteiras = carteiras;
    }
}