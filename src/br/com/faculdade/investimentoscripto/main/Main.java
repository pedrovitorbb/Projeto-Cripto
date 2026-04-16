package br.com.faculdade.investimentoscripto.main;

import java.util.Scanner;

import br.com.faculdade.investimentoscripto.model.Usuario;

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

        sc.close();
    }
}
