package br.com.faculdade.investimentoscripto.main;

import java.util.Scanner;

import br.com.faculdade.investimentoscripto.model.Investidor;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Investidor investidor = new Investidor();

        System.out.println("Sistema de Gestao de Investimentos em Criptomoedas");
        System.out.print("Digite seu id: ");
        investidor.setId(Long.parseLong(sc.nextLine()));

        System.out.print("Digite seu nome: ");
        investidor.setNome(sc.nextLine());

        System.out.print("Digite seu email: ");
        investidor.setEmail(sc.nextLine());

        System.out.print("Digite sua senha: ");
        investidor.setSenhaHash(sc.nextLine());

        System.out.println();
        System.out.println("Cadastro realizado com sucesso");
        investidor.exibirPerfil();

        sc.close();
    }
}
