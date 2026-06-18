package br.com.faculdade.investimentoscripto.main;

import java.util.Scanner;
import java.util.HashMap;
import br.com.faculdade.investimentoscripto.model.Usuario;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Usuario usuario = new Usuario();

        System.out.println("Sistema de Gestao de Investimentos em Criptomoedas");
        System.out.print("Digite seu nome: ");
        usuario.nome = sc.nextLine();

        System.out.print("Digite sua idade: ");
        usuario.idade = Integer.parseInt(sc.nextLine());

        System.out.print("Digite seu email: ");
        usuario.email = sc.nextLine();

        System.out.print("Digite seu telefone: ");
        usuario.telefone = sc.nextLine();

        System.out.print("Digite sua senha: ");
        usuario.senha = sc.nextLine();

        System.out.println();
        System.out.println("Cadastro realizado com sucesso");
        System.out.println("Usuario cadastrado: " + usuario.nome);
        System.out.println("Idade: " + usuario.idade);
        System.out.println("Email: " + usuario.email);
        System.out.println("Telefone: " + usuario.telefone);

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