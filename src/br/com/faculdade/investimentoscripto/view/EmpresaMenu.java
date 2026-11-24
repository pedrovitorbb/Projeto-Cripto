package br.com.faculdade.investimentoscripto.view;

import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.EmpresaDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Empresa;

import java.util.List;

public final class EmpresaMenu {

    private static final EmpresaDAO EMPRESA_DAO = new EmpresaDAO();
    private static final CarteiraDAO CARTEIRA_DAO = new CarteiraDAO();

    /** Tamanho da coluna nm_razao_social de T_SIP_EMPRESA. */
    private static final int TAMANHO_MAXIMO_RAZAO_SOCIAL = 150;

    private EmpresaMenu() {
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
                case 4 -> exibirTodas();
                case 5 -> exibirUma();
                case 6 -> buscarPorNome();
                case 0 -> System.out.println("\nVoltando ao menu principal.");
                default -> System.out.println("\nOpção inválida.");
            }
        } while (opcao != 0);
    }

    private static void exibirMenu() {
        System.out.println("\n================================");
        System.out.println("       GERENCIAR EMPRESAS");
        System.out.println("================================");
        System.out.println("1 - Incluir empresa");
        System.out.println("2 - Alterar empresa");
        System.out.println("3 - Excluir empresa");
        System.out.println("4 - Exibir todas as empresas");
        System.out.println("5 - Exibir uma empresa");
        System.out.println("6 - Buscar empresa por razão social");
        System.out.println("0 - Voltar");
        System.out.println();
    }

    private static void incluir() {
        System.out.println("\n--- INCLUIR EMPRESA ---");
        String razaoSocial = lerRazaoSocialObrigatoria("Razão social: ");
        String cnpj = lerCNPJObrigatorio("CNPJ: ");
        long idUsuario = lerIdPositivo("ID do usuário responsável: ");

        Empresa empresa = new Empresa(null, razaoSocial, cnpj, idUsuario);

        try {
            EMPRESA_DAO.inserir(empresa);
            System.out.println("Empresa incluída com sucesso. ID: " + empresa.getId());
        } catch (PersistenciaException | IllegalStateException | IllegalArgumentException e) {
            System.out.println("Não foi possível incluir a empresa: " + e.getMessage());
        }
    }

    private static void alterar() {
        System.out.println("\n--- ALTERAR EMPRESA ---");
        long id = lerIdPositivo("Digite o ID da empresa: ");

        try {
            Empresa empresa = EMPRESA_DAO.buscarPorId(id);
            if (empresa == null) {
                System.out.println("Empresa não encontrada.");
                return;
            }

            exibirDados(empresa);
            System.out.println("Deixe o campo em branco para manter o valor atual.");

            empresa.setRazaoSocial(lerRazaoSocialOpcional(empresa.getRazaoSocial()));
            empresa.setCnpj(lerCNPJOpcional(empresa.getCnpj()));
            empresa.setIdUsuario(lerIdUsuarioOpcional(empresa.getIdUsuario()));

            if (EMPRESA_DAO.atualizar(empresa)) {
                System.out.println("Empresa alterada com sucesso.");
            } else {
                System.out.println("Empresa não encontrada.");
            }
        } catch (PersistenciaException | IllegalStateException | IllegalArgumentException e) {
            System.out.println("Não foi possível alterar a empresa: " + e.getMessage());
        }
    }

    private static void excluir() {
        System.out.println("\n--- EXCLUIR EMPRESA ---");
        long id = lerIdPositivo("Digite o ID da empresa: ");

        try {
            Empresa empresa = EMPRESA_DAO.buscarPorId(id);
            if (empresa == null) {
                System.out.println("Empresa não encontrada.");
                return;
            }

            exibirDados(empresa);

            List<Carteira> carteiras = CARTEIRA_DAO.listarPorEmpresa(id);
            if (!carteiras.isEmpty()) {
                System.out.println("Não foi possível excluir a empresa porque ela possui "
                        + carteiras.size() + " carteira(s) vinculada(s).");
                return;
            }

            if (!Console.confirmar("Deseja realmente excluir esta empresa? (S/N): ")) {
                System.out.println("Exclusão cancelada.");
                return;
            }

            if (EMPRESA_DAO.excluir(id)) {
                System.out.println("Empresa excluída com sucesso.");
            } else {
                System.out.println("Empresa não encontrada.");
            }
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível excluir a empresa: " + e.getMessage());
        }
    }

    private static void exibirTodas() {
        System.out.println("\n--- EMPRESAS CADASTRADAS ---");

        try {
            exibirLista(EMPRESA_DAO.listarTodas(), "Nenhuma empresa cadastrada.");
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível listar as empresas: " + e.getMessage());
        }
    }

    private static void exibirUma() {
        System.out.println("\n--- EXIBIR EMPRESA ---");
        long id = lerIdPositivo("Digite o ID da empresa: ");

        try {
            Empresa empresa = EMPRESA_DAO.buscarPorId(id);
            if (empresa == null) {
                System.out.println("Empresa não encontrada.");
                return;
            }
            exibirDados(empresa);
        } catch (PersistenciaException | IllegalStateException e) {
            System.out.println("Não foi possível buscar a empresa: " + e.getMessage());
        }
    }

    private static void buscarPorNome() {
        System.out.println("\n--- BUSCAR EMPRESA POR RAZÃO SOCIAL ---");
        String nome = Console.lerTextoObrigatorio("Parte da razão social: ");

        try {
            exibirLista(EMPRESA_DAO.buscarPorNome(nome), "Nenhuma empresa encontrada.");
        } catch (PersistenciaException | IllegalStateException | IllegalArgumentException e) {
            System.out.println("Não foi possível buscar as empresas: " + e.getMessage());
        }
    }

    private static String lerRazaoSocialObrigatoria(String mensagem) {
        while (true) {
            String razaoSocial = Console.lerTextoObrigatorio(mensagem);
            if (razaoSocial.length() <= TAMANHO_MAXIMO_RAZAO_SOCIAL) {
                return razaoSocial;
            }
            System.out.println("A razão social deve ter no máximo "
                    + TAMANHO_MAXIMO_RAZAO_SOCIAL + " caracteres.");
        }
    }

    private static String lerRazaoSocialOpcional(String razaoSocialAtual) {
        while (true) {
            String razaoSocial = Console.lerTexto("Nova razão social: ");
            if (razaoSocial.isEmpty()) {
                return razaoSocialAtual;
            }
            if (razaoSocial.length() <= TAMANHO_MAXIMO_RAZAO_SOCIAL) {
                return razaoSocial;
            }
            System.out.println("A razão social deve ter no máximo "
                    + TAMANHO_MAXIMO_RAZAO_SOCIAL + " caracteres.");
        }
    }

    private static String lerCNPJObrigatorio(String mensagem) {
        while (true) {
            String cnpj = Console.lerTextoObrigatorio(mensagem);
            if (Validador.validarCNPJ(cnpj)) {
                return Validador.removerFormatacaoNumerica(cnpj);
            }
            System.out.println("CNPJ inválido.");
        }
    }

    private static String lerCNPJOpcional(String cnpjAtual) {
        while (true) {
            String cnpj = Console.lerTexto("Novo CNPJ: ");
            if (cnpj.isEmpty()) {
                return cnpjAtual;
            }
            if (Validador.validarCNPJ(cnpj)) {
                return Validador.removerFormatacaoNumerica(cnpj);
            }
            System.out.println("CNPJ inválido.");
        }
    }

    private static long lerIdPositivo(String mensagem) {
        while (true) {
            int id = Console.lerInt(mensagem);
            if (id > 0) {
                return id;
            }
            System.out.println("O ID deve ser maior que zero.");
        }
    }

    private static Long lerIdUsuarioOpcional(Long idUsuarioAtual) {
        while (true) {
            String entrada = Console.lerTexto("Novo ID do usuário responsável: ");
            if (entrada.isEmpty()) {
                return idUsuarioAtual;
            }
            try {
                long id = Long.parseLong(entrada);
                if (id > 0) {
                    return id;
                }
                System.out.println("O ID deve ser maior que zero.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    private static void exibirLista(List<Empresa> empresas, String mensagemVazia) {
        if (empresas.isEmpty()) {
            System.out.println(mensagemVazia);
            return;
        }

        for (Empresa empresa : empresas) {
            exibirDados(empresa);
        }
    }

    private static void exibirDados(Empresa empresa) {
        System.out.println("------------------------------");
        System.out.println("ID: " + empresa.getId());
        System.out.println("Razão social: " + empresa.getRazaoSocial());
        System.out.println("CNPJ: " + formatarCNPJ(empresa.getCnpj()));
        System.out.println("ID do usuário responsável: " + empresa.getIdUsuario());
    }

    private static String formatarCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8)
                + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }
}
