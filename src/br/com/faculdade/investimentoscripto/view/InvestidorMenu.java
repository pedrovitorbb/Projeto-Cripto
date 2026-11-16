package br.com.faculdade.investimentoscripto.view;

import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.InvestidorDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Investidor;

import java.util.List;

public final class InvestidorMenu {

    private static final InvestidorDAO INVESTIDOR_DAO = new InvestidorDAO();
    private static final CarteiraDAO CARTEIRA_DAO = new CarteiraDAO();

    private InvestidorMenu() {
    }

    public static void executar() {
        int opcao;

        do {
            exibirMenu();
            opcao = Console.lerInt("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> incluir();
                case 2 -> alterar();
                case 3 -> excluir();
                case 4 -> exibirTodos();
                case 5 -> exibirUm();
                case 0 -> System.out.println("\nVoltando ao menu principal.");
                default -> System.out.println("\nOpção inválida.");
            }
        } while (opcao != 0);
    }

    private static void exibirMenu() {
        System.out.println("\n================================");
        System.out.println("     GERENCIAR INVESTIDORES");
        System.out.println("================================");
        System.out.println("1 - Incluir investidor");
        System.out.println("2 - Alterar investidor");
        System.out.println("3 - Excluir investidor");
        System.out.println("4 - Exibir todos os investidores");
        System.out.println("5 - Exibir um investidor");
        System.out.println("0 - Voltar");
        System.out.println();
    }

    private static void incluir() {
        System.out.println("\n--- INCLUIR INVESTIDOR ---");
        String nome = Console.lerTextoObrigatorio("Nome: ");
        String email = lerEmailObrigatorio("E-mail: ");
        String cpf = lerCPFObrigatorio("CPF: ");
        String senha = Console.lerTextoObrigatorio("Senha: ");
        String senhaHash = Validador.gerarHashSenha(senha);

        Investidor investidor = new Investidor(null, nome, email, senhaHash, cpf);

        try {
            INVESTIDOR_DAO.inserir(investidor);
            System.out.println("Investidor incluído com sucesso. ID: " + investidor.getId());
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível incluir o investidor: " + e.getMessage());
        }
    }

    private static void alterar() {
        System.out.println("\n--- ALTERAR INVESTIDOR ---");
        long id = Console.lerInt("Digite o ID do investidor: ");

        try {
            Investidor investidor = INVESTIDOR_DAO.buscarPorId(id);
            if (investidor == null) {
                System.out.println("Investidor não encontrado.");
                return;
            }

            exibirDados(investidor);
            System.out.println("Deixe o campo em branco para manter o valor atual.");

            String nome = Console.lerTexto("Novo nome: ");
            if (!nome.isEmpty()) {
                investidor.setNome(nome);
            }

            String email = lerEmailOpcional(investidor.getEmail());
            investidor.setEmail(email);

            String cpf = lerCPFOpcional(investidor.getCpf());
            investidor.setCpf(cpf);

            String senha = Console.lerTexto("Nova senha: ");
            if (!senha.isEmpty()) {
                investidor.setSenhaHash(Validador.gerarHashSenha(senha));
            }

            if (INVESTIDOR_DAO.alterar(investidor)) {
                System.out.println("Investidor alterado com sucesso.");
            } else {
                System.out.println("Investidor não encontrado.");
            }
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível alterar o investidor: " + e.getMessage());
        }
    }

    private static void excluir() {
        System.out.println("\n--- EXCLUIR INVESTIDOR ---");
        long id = Console.lerInt("Digite o ID do investidor: ");

        try {
            Investidor investidor = INVESTIDOR_DAO.buscarPorId(id);
            if (investidor == null) {
                System.out.println("Investidor não encontrado.");
                return;
            }

            System.out.println("ID: " + investidor.getId());
            System.out.println("Nome: " + investidor.getNome());

            List<Carteira> carteiras = CARTEIRA_DAO.listarPorInvestidor(id);
            if (!carteiras.isEmpty()) {
                System.out.println("Não foi possível excluir o investidor porque ele possui carteiras vinculadas.");
                return;
            }

            if (!Console.confirmar("Deseja realmente excluir este investidor? (S/N): ")) {
                System.out.println("Exclusão cancelada.");
                return;
            }

            if (INVESTIDOR_DAO.excluir(id)) {
                System.out.println("Investidor excluído com sucesso.");
            } else {
                System.out.println("Investidor não encontrado.");
            }
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível excluir o investidor: " + e.getMessage());
        }
    }

    private static void exibirTodos() {
        System.out.println("\n--- INVESTIDORES CADASTRADOS ---");

        try {
            List<Investidor> investidores = INVESTIDOR_DAO.listar();
            if (investidores.isEmpty()) {
                System.out.println("Nenhum investidor cadastrado.");
                return;
            }

            for (Investidor investidor : investidores) {
                exibirDados(investidor);
            }
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível listar os investidores: " + e.getMessage());
        }
    }

    private static void exibirUm() {
        System.out.println("\n--- EXIBIR INVESTIDOR ---");
        long id = Console.lerInt("Digite o ID do investidor: ");

        try {
            Investidor investidor = INVESTIDOR_DAO.buscarPorId(id);
            if (investidor == null) {
                System.out.println("Investidor não encontrado.");
                return;
            }
            exibirDados(investidor);
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível buscar o investidor: " + e.getMessage());
        }
    }

    private static String lerEmailObrigatorio(String mensagem) {
        while (true) {
            String email = Console.lerTextoObrigatorio(mensagem);
            if (Validador.validarEmail(email)) {
                return email;
            }
            System.out.println("E-mail inválido.");
        }
    }

    private static String lerCPFObrigatorio(String mensagem) {
        while (true) {
            String cpf = Console.lerTextoObrigatorio(mensagem);
            if (Validador.validarCPF(cpf)) {
                return Validador.removerFormatacaoNumerica(cpf);
            }
            System.out.println("CPF inválido.");
        }
    }

    private static String lerEmailOpcional(String emailAtual) {
        while (true) {
            String email = Console.lerTexto("Novo e-mail: ");
            if (email.isEmpty()) {
                return emailAtual;
            }
            if (Validador.validarEmail(email)) {
                return email;
            }
            System.out.println("E-mail inválido.");
        }
    }

    private static String lerCPFOpcional(String cpfAtual) {
        while (true) {
            String cpf = Console.lerTexto("Novo CPF: ");
            if (cpf.isEmpty()) {
                return cpfAtual;
            }
            if (Validador.validarCPF(cpf)) {
                return Validador.removerFormatacaoNumerica(cpf);
            }
            System.out.println("CPF inválido.");
        }
    }

    private static void exibirDados(Investidor investidor) {
        System.out.println("------------------------------");
        System.out.println("ID: " + investidor.getId());
        System.out.println("Nome: " + investidor.getNome());
        System.out.println("E-mail: " + investidor.getEmail());
        System.out.println("CPF: " + investidor.getCpf());
    }
}
