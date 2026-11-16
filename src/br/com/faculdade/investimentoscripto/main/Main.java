package br.com.faculdade.investimentoscripto.main;

import br.com.faculdade.investimentoscripto.view.Console;

public class Main {

    public static void main(String[] args) {
        int opcao;

        do {
            exibirMenu();
            opcao = Console.lerInt("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> System.out.println("\nEste módulo ainda não está disponível.");
                case 2, 3, 4, 5 -> System.out.println("\nEste módulo ainda não está disponível.");
                case 0 -> System.out.println("\nSistema encerrado.");
                default -> System.out.println("\nOpção inválida.");
            }
        } while (opcao != 0);
    }

    private static void exibirMenu() {
        System.out.println("\n================================");
        System.out.println("       PROJETO CRIPTO");
        System.out.println("================================");
        System.out.println("1 - Gerenciar investidores");
        System.out.println("2 - Gerenciar empresas");
        System.out.println("3 - Gerenciar carteiras");
        System.out.println("4 - Gerenciar ativos cripto");
        System.out.println("5 - Gerenciar transações");
        System.out.println("0 - Sair");
        System.out.println();
    }
}
