package br.com.faculdade.investimentoscripto.main;

import br.com.faculdade.investimentoscripto.exception.SaldoInsuficienteException;
import br.com.faculdade.investimentoscripto.exception.UsuarioInvalidoException;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Empresa;
import br.com.faculdade.investimentoscripto.model.Investidor;
import br.com.faculdade.investimentoscripto.model.Transacao;
import br.com.faculdade.investimentoscripto.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // =====================================================
        // Bloco 1 — Herança + Override + UsuarioInvalidoException
        // =====================================================
        System.out.println("=== Bloco 1: Herança + Override ===");

        try {
            String nome = "";
            if (nome == null || nome.isEmpty()) {
                throw new UsuarioInvalidoException("Nome não pode ser vazio.");
            }
        } catch (UsuarioInvalidoException e) {
            System.out.println("UsuarioInvalidoException: " + e.getMessage());
        }

        try {
            String email = "";
            if (email == null || email.isEmpty()) {
                throw new UsuarioInvalidoException("Email não pode ser vazio.");
            }
        } catch (UsuarioInvalidoException e) {
            System.out.println("UsuarioInvalidoException: " + e.getMessage());
        }

        Investidor investidor1 = new Investidor(1L, "Alice Silva", "alice@email.com", "hash123");
        investidor1.exibirPerfil();

        // =====================================================
        // Bloco 2 — Overload de calcularLucroPrejuizo
        // =====================================================
        System.out.println("\n=== Bloco 2: Overload (Polimorfismo Estático) ===");

        AtivoCripto bitcoin = new AtivoCripto("BTC", "Bitcoin", 350000.00);
        Carteira carteira = new Carteira(1L);

        Transacao compra = new Transacao(carteira, bitcoin, Transacao.Tipo.COMPRA,
                0.5, 300000.00, 300000.00, "abc123def456xyz");
        carteira.registrarTransacao(compra);

        double r1 = bitcoin.calcularLucroPrejuizo(compra);
        System.out.printf("1) Lucro/Prejuizo (cotacao atual):       R$ %.2f%n", r1);

        double r2 = bitcoin.calcularLucroPrejuizo(compra, 400000.00);
        System.out.printf("2) Lucro/Prejuizo (preco venda manual):  R$ %.2f%n", r2);

        double r3 = bitcoin.calcularLucroPrejuizo(0.5, 300000.00);
        System.out.printf("3) Lucro/Prejuizo (qtd + preco medio):   R$ %.2f%n", r3);

        double r4 = bitcoin.calcularLucroPrejuizo();
        System.out.printf("4) Lucro/Prejuizo (consolidado):         R$ %.2f%n", r4);

        try {
            double saldoDisponivel = 1000.00;
            double valorCompra = 350000.00;
            if (valorCompra > saldoDisponivel) {
                throw new SaldoInsuficienteException(
                        "Saldo insuficiente: disponivel R$ " + saldoDisponivel
                        + ", necessario R$ " + valorCompra);
            }
        } catch (SaldoInsuficienteException e) {
            System.out.println("SaldoInsuficienteException: " + e.getMessage());
        }

        // =====================================================
        // Bloco 3 — Empresa e patrimônio
        // =====================================================
        System.out.println("\n=== Bloco 3: Empresa e Patrimônio ===");

        try {
            Empresa empresa = new Empresa(1L, "Cripto Investimentos LTDA", "12.345.678/0001-90");
            empresa.adicionarCarteira(carteira);
            System.out.println("Empresa: " + empresa.getRazaoSocial());
            System.out.printf("Patrimônio total: R$ %.2f%n",
                    empresa.calcularPatrimonioTotal().doubleValue());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }

        // =====================================================
        // Bloco 4 — Polimorfismo dinâmico com List<Usuario>
        // =====================================================
        System.out.println("\n=== Bloco 4: Polimorfismo Dinâmico ===");

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Investidor(1L, "Alice Silva", "alice@email.com", "hash123"));
        usuarios.add(new Investidor(2L, "Bruno Costa", "bruno@email.com", "hash456"));

        for (Usuario u : usuarios) {
            u.exibirPerfil();
            System.out.println();
        }
    }
}
