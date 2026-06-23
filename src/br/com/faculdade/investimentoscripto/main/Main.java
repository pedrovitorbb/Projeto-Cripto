package br.com.faculdade.investimentoscripto.main;

import java.util.Scanner;
import java.util.HashMap;
import br.com.faculdade.investimentoscripto.model.Usuario;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

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

        ArrayList<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Investidor(1L, "Alice Silva", "alice@email.com", "hash123"));
        usuarios.add(new Investidor(2L, "Bruno Costa", "bruno@email.com", "hash456"));

        for (Usuario u : usuarios) {
            u.exibirPerfil();
            System.out.println();
        }

        // =====================================================
        // Bloco 5 - Arquivos de texto
        // =====================================================
        System.out.println("=== Bloco 5: Arquivos de texto ===");

        HashMap<String, AtivoCripto> mapaAtivos = new HashMap<>();
        mapaAtivos.put(bitcoin.getTicker(), bitcoin);

        salvarDadosEmArquivo(usuarios, mapaAtivos, "dados.txt");

        usuarios.add(new Investidor(3L, "Carla Lima", "carla@email.com", "hash789"));
        bitcoin.setCotacaoAtual(360000.00);

        AtivoCripto ethereum = new AtivoCripto("ETH", "Ethereum", 18000.00);
        mapaAtivos.put(ethereum.getTicker(), ethereum);

        salvarDadosEmArquivo(usuarios, mapaAtivos, "dados.txt");
    }

    private static void salvarDadosEmArquivo(ArrayList<Usuario> usuarios,
                                             HashMap<String, AtivoCripto> ativos,
                                             String nomeArquivo) {
        File arquivo = new File(nomeArquivo);

        try (FileWriter fw = new FileWriter(arquivo);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println("Usuarios");
            pw.println("--------");

            for (Usuario usuario : usuarios) {
                pw.println("Id: " + usuario.getId());
                pw.println("Nome: " + usuario.getNome());
                pw.println("Email: " + usuario.getEmail());
                pw.println("Senha hash: " + usuario.getSenhaHash());
                pw.println();
            }

            pw.println("Ativos");
            pw.println("------");

            for (AtivoCripto ativo : ativos.values()) {
                pw.println("Ticker: " + ativo.getTicker());
                pw.println("Nome: " + ativo.getNome());
                pw.println("Cotacao atual: " + ativo.getCotacaoAtual());
                pw.println();
            }

        System.out.println();

        HashMap<String, Usuario> mapUser = new HashMap<>();
        mapUser.put(usuario.email, usuario);

        HashMap<String, AtivoCripto> mapCripto = new HashMap<>();
        AtivoCripto moeda = new AtivoCripto();

        moeda.sigla = "BTC";
        moeda.nome = "Bitcoin";
        mapCripto.put(moeda.sigla, moeda);

        String emailBusca = usuario.email;
        Usuario uFnd = mapUser.get(emailBusca);

        if (uFnd != null) {
            System.out.println("Resultado mapa usuario: " + uFnd.nome);
        }

        String siglaBusca = "BTC";
        AtivoCripto cFnd = mapCripto.get(siglaBusca);

        if (cFnd != null) {
            System.out.println("Resultado mapa ativo: " + cFnd.nome);
        }

        sc.close();
    }
}