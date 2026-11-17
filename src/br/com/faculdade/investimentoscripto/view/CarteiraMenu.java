package br.com.faculdade.investimentoscripto.view;

import br.com.faculdade.investimentoscripto.dao.AtivoCriptoDAO;
import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.EmpresaDAO;
import br.com.faculdade.investimentoscripto.dao.InvestidorDAO;
import br.com.faculdade.investimentoscripto.dao.TransacaoDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Empresa;
import br.com.faculdade.investimentoscripto.model.Investidor;
import br.com.faculdade.investimentoscripto.model.Transacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Menu do modulo Carteira (T_SIP_CARTEIRA).
 *
 * <p>Inclusao, alteracao, exclusao, exibicao de todas e de uma unica carteira,
 * mais consultas por investidor e por empresa e um resumo. A carteira depende
 * de um investidor e de uma empresa ja cadastrados (as duas chaves
 * estrangeiras), entao a inclusao mostra as listas para a pessoa escolher.</p>
 *
 * <p>Esta classe so faz entrada e saida no console; o acesso ao banco fica nos DAOs.
 * Erros do banco chegam como {@link PersistenciaException} com mensagem amigavel.</p>
 */
public class CarteiraMenu {

    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();
    private static final InvestidorDAO investidorDAO = new InvestidorDAO();
    private static final EmpresaDAO empresaDAO = new EmpresaDAO();
    private static final AtivoCriptoDAO ativoDAO = new AtivoCriptoDAO();
    private static final TransacaoDAO transacaoDAO = new TransacaoDAO();

    /** vl_saldo_total_geral e NUMBER(15,2): ate 13 digitos inteiros e 2 decimais. */
    private static final int MAXIMO_DIGITOS_INTEIROS = 13;
    private static final int MAXIMO_CASAS_DECIMAIS = 2;

    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    /** Classe utilitaria: nao deve ser instanciada. */
    private CarteiraMenu() {
    }

    /** Abre o submenu de Carteira e fica nele ate a pessoa escolher voltar. */
    public static void executar() {
        int opcao;
        do {
            exibirOpcoes();
            opcao = Console.lerInt("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> incluir();
                    case 2 -> alterar();
                    case 3 -> excluir();
                    case 4 -> exibirTodas();
                    case 5 -> exibirUma();
                    case 6 -> listarPorInvestidor();
                    case 7 -> listarPorEmpresa();
                    case 8 -> exibirResumo();
                    case 0 -> System.out.println("\nVoltando ao menu principal.");
                    default -> System.out.println("\nOpção inválida.");
                }
            } catch (PersistenciaException | IllegalStateException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void exibirOpcoes() {
        System.out.println("\n================================");
        System.out.println("      GERENCIAR CARTEIRAS");
        System.out.println("================================");
        System.out.println("1 - Incluir carteira");
        System.out.println("2 - Alterar carteira");
        System.out.println("3 - Excluir carteira");
        System.out.println("4 - Exibir todas as carteiras");
        System.out.println("5 - Exibir uma carteira");
        System.out.println("6 - Listar carteiras de um investidor");
        System.out.println("7 - Listar carteiras de uma empresa");
        System.out.println("8 - Resumo (total de carteiras e saldo)");
        System.out.println("0 - Voltar");
        System.out.println();
    }

    // =====================================================================
    //  Inclusao
    // =====================================================================

    private static void incluir() throws PersistenciaException {
        titulo("INCLUIR CARTEIRA");

        List<Investidor> investidores = investidorDAO.listar();
        if (investidores.isEmpty()) {
            System.out.println("Nenhum investidor cadastrado. Cadastre um investidor antes.");
            return;
        }
        List<Empresa> empresas = empresaDAO.listarTodas();
        if (empresas.isEmpty()) {
            System.out.println("Nenhuma empresa cadastrada. Cadastre uma empresa antes.");
            return;
        }

        System.out.println("Investidores disponíveis:");
        for (Investidor i : investidores) {
            System.out.println("  " + i.getId() + " - " + i.getNome() + " (" + i.getEmail() + ")");
        }
        Investidor investidor = escolherInvestidor(investidores);
        if (investidor == null) {
            System.out.println("Inclusão cancelada.");
            return;
        }

        System.out.println("Empresas disponíveis:");
        for (Empresa e : empresas) {
            System.out.println("  " + e.getId() + " - " + e.getRazaoSocial() + " (CNPJ " + e.getCnpj() + ")");
        }
        Empresa empresa = escolherEmpresa(empresas);
        if (empresa == null) {
            System.out.println("Inclusão cancelada.");
            return;
        }

        BigDecimal saldo = lerSaldo("Saldo inicial: ");

        Carteira carteira = new Carteira(null, saldo, investidor, empresa);
        carteiraDAO.inserir(carteira);
        System.out.println("Carteira incluída com sucesso. ID gerado: " + carteira.getId());
    }

    /** Pede o id ate ser de um investidor da lista; 0 cancela e devolve null. */
    private static Investidor escolherInvestidor(List<Investidor> investidores) {
        while (true) {
            long id = lerId("ID do investidor (0 para cancelar): ");
            if (id == 0) {
                return null;
            }
            for (Investidor i : investidores) {
                if (i.getId() == id) {
                    return i;
                }
            }
            System.out.println("Investidor não encontrado na lista.");
        }
    }

    /** Pede o id ate ser de uma empresa da lista; 0 cancela e devolve null. */
    private static Empresa escolherEmpresa(List<Empresa> empresas) {
        while (true) {
            long id = lerId("ID da empresa (0 para cancelar): ");
            if (id == 0) {
                return null;
            }
            for (Empresa e : empresas) {
                if (e.getId() == id) {
                    return e;
                }
            }
            System.out.println("Empresa não encontrada na lista.");
        }
    }

    // =====================================================================
    //  Alteracao
    // =====================================================================

    private static void alterar() throws PersistenciaException {
        titulo("ALTERAR CARTEIRA");

        Carteira carteira = buscarCarteira("ID da carteira (0 para cancelar): ");
        if (carteira == null) {
            return;
        }
        exibirResumoDaCarteira(carteira);
        System.out.println("Pressione ENTER para manter o valor atual.");

        Long idInvestidor = lerIdOpcional("ID do investidor", carteira.getInvestidor().getId());
        if (!idInvestidor.equals(carteira.getInvestidor().getId())) {
            Investidor novo = investidorDAO.buscarPorId(idInvestidor);
            if (novo == null) {
                System.out.println("Investidor não encontrado. Alteração cancelada.");
                return;
            }
            carteira.setInvestidor(novo);
        }

        Long idEmpresa = lerIdOpcional("ID da empresa", carteira.getEmpresa().getId());
        if (!idEmpresa.equals(carteira.getEmpresa().getId())) {
            Empresa nova = empresaDAO.buscarPorId(idEmpresa);
            if (nova == null) {
                System.out.println("Empresa não encontrada. Alteração cancelada.");
                return;
            }
            carteira.setEmpresa(nova);
        }

        carteira.setSaldoTotalGeral(lerSaldoOpcional("Saldo", carteira.getSaldoTotalGeral()));

        if (!Console.confirmar("Salvar as alterações? (S/N): ")) {
            System.out.println("Alteração cancelada.");
            return;
        }
        if (carteiraDAO.atualizar(carteira)) {
            System.out.println("Carteira alterada com sucesso.");
        } else {
            System.out.println("A carteira não foi alterada (ela pode ter sido excluída).");
        }
    }

    /** Le um id; ENTER mantem o valor atual. Repete ate a entrada ser valida. */
    private static Long lerIdOpcional(String nome, Long atual) {
        while (true) {
            String texto = Console.lerTexto(nome + " [" + atual + "]: ");
            if (texto.isEmpty()) {
                return atual;
            }
            try {
                long id = Long.parseLong(texto);
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException e) {
                // cai na mensagem abaixo
            }
            System.out.println("Digite um ID inteiro maior que zero.");
        }
    }

    /** Le o saldo; ENTER mantem o valor atual. Repete ate a entrada ser valida. */
    private static BigDecimal lerSaldoOpcional(String nome, BigDecimal atual) {
        while (true) {
            String texto = Console.lerTexto(nome + " [" + formatarValor(atual) + "]: ");
            if (texto.isEmpty()) {
                return atual;
            }
            try {
                BigDecimal valor = new BigDecimal(texto.replace(',', '.'));
                String erro = validarSaldo(valor);
                if (erro == null) {
                    return valor;
                }
                System.out.println(erro);
            } catch (NumberFormatException e) {
                System.out.println("Digite um valor numérico válido.");
            }
        }
    }

    // =====================================================================
    //  Exclusao
    // =====================================================================

    private static void excluir() throws PersistenciaException {
        titulo("EXCLUIR CARTEIRA");

        Carteira carteira = buscarCarteira("ID da carteira (0 para cancelar): ");
        if (carteira == null) {
            return;
        }
        exibirResumoDaCarteira(carteira);

        if (!Console.confirmar("Deseja realmente excluir esta carteira? (S/N): ")) {
            System.out.println("Exclusão cancelada.");
            return;
        }
        if (carteiraDAO.deletar(carteira.getId())) {
            System.out.println("Carteira excluída com sucesso.");
        } else {
            System.out.println("A carteira não foi excluída (ela pode já ter sido removida).");
        }
    }

    // =====================================================================
    //  Consultas
    // =====================================================================

    private static void exibirTodas() throws PersistenciaException {
        titulo("TODAS AS CARTEIRAS");
        List<Carteira> carteiras = carteiraDAO.listarTodos();
        exibirTabela(carteiras);
    }

    private static void exibirUma() throws PersistenciaException {
        titulo("EXIBIR UMA CARTEIRA");

        Carteira carteira = buscarCarteira("ID da carteira (0 para cancelar): ");
        if (carteira == null) {
            return;
        }
        exibirResumoDaCarteira(carteira);

        List<AtivoCripto> ativos = ativoDAO.listarPorCarteira(carteira.getId());
        System.out.println("\nAtivos (" + ativos.size() + "):");
        if (ativos.isEmpty()) {
            System.out.println("  Nenhum ativo nesta carteira.");
        }
        for (AtivoCripto a : ativos) {
            System.out.println("  " + a.getTicker()
                    + " | quantidade " + a.getQuantidade().toPlainString()
                    + " | preço médio " + formatarValor(a.getPrecoMedioCompra())
                    + " | total investido " + formatarValor(a.getValorTotalInvestido()));
        }

        List<Transacao> transacoes = transacaoDAO.listarPorCarteira(carteira.getId());
        System.out.println("\nTransações (" + transacoes.size() + "):");
        if (transacoes.isEmpty()) {
            System.out.println("  Nenhuma transação nesta carteira.");
        }
        for (Transacao t : transacoes) {
            System.out.println("  #" + t.getId() + " " + t);
        }
        if (!transacoes.isEmpty()) {
            System.out.println("  Valor total movimentado: "
                    + formatarValor(transacaoDAO.somarValorPorCarteira(carteira.getId())));
        }
    }

    private static void listarPorInvestidor() throws PersistenciaException {
        titulo("CARTEIRAS DE UM INVESTIDOR");

        long id = lerId("ID do investidor (0 para cancelar): ");
        if (id == 0) {
            return;
        }
        Investidor investidor = investidorDAO.buscarPorId(id);
        if (investidor == null) {
            System.out.println("Investidor não encontrado.");
            return;
        }
        System.out.println("Investidor: " + investidor.getNome());
        exibirTabela(carteiraDAO.listarPorInvestidor(id));
    }

    private static void listarPorEmpresa() throws PersistenciaException {
        titulo("CARTEIRAS DE UMA EMPRESA");

        long id = lerId("ID da empresa (0 para cancelar): ");
        if (id == 0) {
            return;
        }
        Empresa empresa = empresaDAO.buscarPorId(id);
        if (empresa == null) {
            System.out.println("Empresa não encontrada.");
            return;
        }
        System.out.println("Empresa: " + empresa.getRazaoSocial());
        exibirTabela(carteiraDAO.listarPorEmpresa(id));
    }

    private static void exibirResumo() throws PersistenciaException {
        titulo("RESUMO DAS CARTEIRAS");
        System.out.println("Total de carteiras: " + carteiraDAO.contarTotalCarteiras());
        System.out.println("Saldo somado: " + formatarValor(carteiraDAO.somarSaldoTotal()));
    }

    // =====================================================================
    //  Apoio
    // =====================================================================

    /** Cabecalho de secao, no mesmo estilo do InvestidorMenu. */
    private static void titulo(String texto) {
        System.out.println("\n--- " + texto + " ---");
    }

    /** Le um id (o Console so le int, como no InvestidorMenu); 0 e usado para cancelar. */
    private static long lerId(String prompt) {
        while (true) {
            int id = Console.lerInt(prompt);
            if (id >= 0) {
                return id;
            }
            System.out.println("O ID não pode ser negativo.");
        }
    }

    /** Pede o id e busca a carteira; 0 cancela. Devolve null se cancelou ou nao achou. */
    private static Carteira buscarCarteira(String prompt) throws PersistenciaException {
        long id = lerId(prompt);
        if (id == 0) {
            return null;
        }
        Carteira carteira = carteiraDAO.buscarPorId(id);
        if (carteira == null) {
            System.out.println("Carteira não encontrada.");
        }
        return carteira;
    }

    /** Le o saldo ate ser valido (positivo ou zero, no maximo 13 inteiros e 2 decimais). */
    private static BigDecimal lerSaldo(String prompt) {
        while (true) {
            BigDecimal valor = Console.lerDecimal(prompt);
            String erro = validarSaldo(valor);
            if (erro == null) {
                return valor;
            }
            System.out.println(erro);
        }
    }

    /** @return a mensagem de erro, ou null se o saldo for valido */
    private static String validarSaldo(BigDecimal valor) {
        if (!Validador.validarValorNaoNegativo(valor)) {
            return "O saldo não pode ser negativo.";
        }
        if (!Validador.validarDuasCasasDecimais(valor)) {
            return "O saldo aceita no máximo " + MAXIMO_CASAS_DECIMAIS + " casas decimais.";
        }
        if (valor.toBigInteger().toString().length() > MAXIMO_DIGITOS_INTEIROS) {
            return "O saldo aceita no máximo " + MAXIMO_DIGITOS_INTEIROS + " dígitos inteiros.";
        }
        return null;
    }

    private static void exibirTabela(List<Carteira> carteiras) {
        if (carteiras.isEmpty()) {
            System.out.println("Nenhuma carteira encontrada.");
            return;
        }
        System.out.printf("%-6s %-28s %-32s %18s%n", "ID", "INVESTIDOR", "EMPRESA", "SALDO");
        for (Carteira c : carteiras) {
            System.out.printf("%-6d %-28s %-32s %18s%n",
                    c.getId(),
                    cortar(c.getInvestidor().getNome(), 28),
                    cortar(c.getEmpresa().getRazaoSocial(), 32),
                    formatarValor(c.getSaldoTotalGeral()));
        }
        System.out.println(carteiras.size() + " carteira(s).");
    }

    /** Mostra os dados da carteira, sem a senha do investidor. */
    private static void exibirResumoDaCarteira(Carteira c) {
        System.out.println("ID: " + c.getId());
        System.out.println("Saldo: " + formatarValor(c.getSaldoTotalGeral()));
        System.out.println("Investidor: " + c.getInvestidor().getId() + " - "
                + c.getInvestidor().getNome() + " (" + c.getInvestidor().getEmail() + ")");
        System.out.println("Empresa: " + c.getEmpresa().getId() + " - "
                + c.getEmpresa().getRazaoSocial() + " (CNPJ " + c.getEmpresa().getCnpj() + ")");
    }

    private static String formatarValor(BigDecimal valor) {
        return String.format(BRASIL, "R$ %,.2f", valor);
    }

    private static String cortar(String texto, int limite) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= limite ? texto : texto.substring(0, limite - 3) + "...";
    }
}
