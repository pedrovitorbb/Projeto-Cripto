package br.com.faculdade.investimentoscripto.view;

import br.com.faculdade.investimentoscripto.dao.AtivoCriptoDAO;
import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.AtivoCripto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Menu do módulo Ativo Cripto.
 *
 * A tabela T_SIP_ATIVO_CRIPTO usa chave primária composta (id_carteira + sg_ticker),
 * então toda busca, alteração e exclusão pede os DOIS campos.
 *
 * Nome e cotação do ativo existem só no objeto Java (não vão para o banco),
 * por isso não são pedidos aqui.
 */
public class AtivoCriptoMenu {

    private static final int TAMANHO_MAX_TICKER = 10;
    /** qt_ativo e NUMBER(18,8); vl_preco_medio_compra e vl_total_investido sao NUMBER(15,2). */
    private static final int CASAS_QUANTIDADE = 8;
    private static final int CASAS_VALOR = 2;

    private AtivoCriptoMenu() {
    }

    public static void executar() {
        int opcao;
        do {
            System.out.println();
            System.out.println("========== ATIVOS CRIPTO ==========");
            System.out.println("1 - Incluir ativo");
            System.out.println("2 - Alterar ativo");
            System.out.println("3 - Excluir ativo");
            System.out.println("4 - Exibir todos");
            System.out.println("5 - Exibir um registro");
            System.out.println("6 - Listar ativos de uma carteira");
            System.out.println("0 - Voltar");
            opcao = Console.lerInt("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> incluir();
                    case 2 -> alterar();
                    case 3 -> excluir();
                    case 4 -> exibirTodos();
                    case 5 -> exibirUm();
                    case 6 -> listarPorCarteira();
                    case 0 -> System.out.println("Voltando ao menu principal...");
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (PersistenciaException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Erro inesperado: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    // ---------------------------------------------------------------- INCLUIR

    private static void incluir() throws PersistenciaException {
        System.out.println("\n--- Incluir ativo ---");

        int idCarteira = Console.lerInt("ID da carteira: ");
        if (new CarteiraDAO().buscarPorId(idCarteira) == null) {
            System.out.println("Carteira " + idCarteira + " não encontrada. Cadastre a carteira antes.");
            return;
        }

        String ticker = lerTicker();
        if (ticker == null) {
            return;
        }

        AtivoCriptoDAO dao = new AtivoCriptoDAO();
        if (dao.buscarPorChave(idCarteira, ticker) != null) {
            System.out.println("A carteira " + idCarteira + " já possui o ativo " + ticker
                    + ". Use a opção Alterar.");
            return;
        }

        BigDecimal quantidade = lerPositivo("Quantidade: ", CASAS_QUANTIDADE);
        BigDecimal precoMedio = lerPositivo("Preço médio de compra (R$): ", CASAS_VALOR);

        AtivoCripto ativo = new AtivoCripto((long) idCarteira, ticker, quantidade, precoMedio,
                calcularTotal(quantidade, precoMedio));

        dao.inserir(ativo);
        System.out.println("Ativo cadastrado com sucesso!");
        imprimirDetalhe(ativo);
    }

    // ---------------------------------------------------------------- ALTERAR

    private static void alterar() throws PersistenciaException {
        System.out.println("\n--- Alterar ativo ---");
        AtivoCriptoDAO dao = new AtivoCriptoDAO();

        AtivoCripto ativo = buscarPelaChave(dao);
        if (ativo == null) {
            return;
        }

        imprimirDetalhe(ativo);
        System.out.println("(Deixe em branco para manter o valor atual)");

        BigDecimal quantidade = lerPositivoOpcional("Nova quantidade", ativo.getQuantidade(), CASAS_QUANTIDADE);
        BigDecimal precoMedio = lerPositivoOpcional("Novo preço médio (R$)", ativo.getPrecoMedioCompra(), CASAS_VALOR);

        ativo.setQuantidade(quantidade);
        ativo.setPrecoMedioCompra(precoMedio);
        ativo.setValorTotalInvestido(calcularTotal(quantidade, precoMedio));

        if (dao.atualizar(ativo)) {
            System.out.println("Ativo atualizado com sucesso!");
            imprimirDetalhe(ativo);
        } else {
            System.out.println("O ativo não foi atualizado (ele pode ter sido excluído).");
        }
    }

    // ---------------------------------------------------------------- EXCLUIR

    private static void excluir() throws PersistenciaException {
        System.out.println("\n--- Excluir ativo ---");
        AtivoCriptoDAO dao = new AtivoCriptoDAO();

        AtivoCripto ativo = buscarPelaChave(dao);
        if (ativo == null) {
            return;
        }

        imprimirDetalhe(ativo);
        if (!Console.confirmar("Deseja realmente excluir este ativo? (S/N): ")) {
            System.out.println("Exclusão cancelada.");
            return;
        }

        if (dao.deletar(ativo.getIdCarteira(), ativo.getTicker())) {
            System.out.println("Ativo excluído com sucesso!");
        } else {
            System.out.println("O ativo não foi excluído (ele pode já ter sido removido).");
        }
    }

    // ---------------------------------------------------------------- EXIBIR

    private static void exibirTodos() throws PersistenciaException {
        System.out.println("\n--- Todos os ativos ---");
        imprimirTabela(new AtivoCriptoDAO().listarTodos());
    }

    private static void exibirUm() throws PersistenciaException {
        System.out.println("\n--- Exibir ativo ---");
        AtivoCripto ativo = buscarPelaChave(new AtivoCriptoDAO());
        if (ativo != null) {
            imprimirDetalhe(ativo);
        }
    }

    private static void listarPorCarteira() throws PersistenciaException {
        System.out.println("\n--- Ativos por carteira ---");
        int idCarteira = Console.lerInt("ID da carteira: ");
        if (new CarteiraDAO().buscarPorId(idCarteira) == null) {
            System.out.println("Carteira " + idCarteira + " não encontrada.");
            return;
        }
        imprimirTabela(new AtivoCriptoDAO().listarPorCarteira(idCarteira));
    }

    // ---------------------------------------------------------------- AUXILIARES

    /** Pede id_carteira + ticker e devolve o ativo, ou null com mensagem amigável. */
    private static AtivoCripto buscarPelaChave(AtivoCriptoDAO dao) throws PersistenciaException {
        int idCarteira = Console.lerInt("ID da carteira: ");
        String ticker = lerTicker();
        if (ticker == null) {
            return null;
        }

        AtivoCripto ativo = dao.buscarPorChave(idCarteira, ticker);
        if (ativo == null) {
            System.out.println("Ativo " + ticker + " não encontrado na carteira " + idCarteira + ".");
        }
        return ativo;
    }

    /** Lê o ticker, normaliza para maiúsculas e valida. Retorna null se inválido. */
    private static String lerTicker() {
        String ticker = Console.lerTexto("Ticker (ex.: BTC, ETH): ").trim().toUpperCase();

        if (ticker.isEmpty()) {
            System.out.println("O ticker é obrigatório.");
            return null;
        }
        if (ticker.length() > TAMANHO_MAX_TICKER) {
            System.out.println("O ticker deve ter no máximo " + TAMANHO_MAX_TICKER + " caracteres.");
            return null;
        }
        if (!ticker.matches("[A-Z0-9]+")) {
            System.out.println("O ticker deve conter apenas letras e números.");
            return null;
        }
        return ticker;
    }

    /** Repete a leitura ate o valor ser maior que zero e caber nas casas decimais da coluna. */
    private static BigDecimal lerPositivo(String mensagem, int casas) {
        while (true) {
            BigDecimal valor = Console.lerDecimal(mensagem);
            String erro = validarPositivo(valor, casas);
            if (erro == null) {
                return valor;
            }
            System.out.println(erro);
        }
    }

    /** Igual ao lerPositivo, mas Enter vazio mantem o valor atual. */
    private static BigDecimal lerPositivoOpcional(String rotulo, BigDecimal atual, int casas) {
        while (true) {
            String texto = Console.lerTexto(rotulo + " [" + formatar(atual) + "]: ").trim();
            if (texto.isEmpty()) {
                return atual;
            }
            try {
                BigDecimal valor = new BigDecimal(texto.replace(",", "."));
                String erro = validarPositivo(valor, casas);
                if (erro == null) {
                    return valor;
                }
                System.out.println(erro);
            } catch (NumberFormatException e) {
                System.out.println("Número inválido. Use apenas dígitos e vírgula/ponto.");
            }
        }
    }

    /** @return a mensagem de erro, ou null se o valor for valido */
    private static String validarPositivo(BigDecimal valor, int casas) {
        if (!Validador.validarValorPositivo(valor)) {
            return "O valor deve ser maior que zero.";
        }
        if (valor.stripTrailingZeros().scale() > casas) {
            return "O valor aceita no máximo " + casas + " casas decimais.";
        }
        return null;
    }

    private static BigDecimal calcularTotal(BigDecimal quantidade, BigDecimal precoMedio) {
        return quantidade.multiply(precoMedio).setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatar(BigDecimal valor) {
        return valor == null ? "-" : valor.stripTrailingZeros().toPlainString();
    }

    private static void imprimirDetalhe(AtivoCripto a) {
        System.out.println("----------------------------------");
        System.out.println("Carteira ........: " + a.getIdCarteira());
        System.out.println("Ticker ..........: " + a.getTicker());
        System.out.println("Quantidade ......: " + formatar(a.getQuantidade()));
        System.out.println("Preço médio (R$) : " + formatar(a.getPrecoMedioCompra()));
        System.out.println("Total invest.(R$): " + formatar(a.getValorTotalInvestido()));
        System.out.println("----------------------------------");
    }

    private static void imprimirTabela(List<AtivoCripto> ativos) {
        if (ativos == null || ativos.isEmpty()) {
            System.out.println("Nenhum ativo encontrado.");
            return;
        }
        System.out.printf("%-9s %-10s %18s %16s %16s%n",
                "CARTEIRA", "TICKER", "QUANTIDADE", "PREÇO MÉDIO", "TOTAL INVEST.");
        for (AtivoCripto a : ativos) {
            System.out.printf("%-9d %-10s %18s %16s %16s%n",
                    a.getIdCarteira(), a.getTicker(),
                    formatar(a.getQuantidade()),
                    formatar(a.getPrecoMedioCompra()),
                    formatar(a.getValorTotalInvestido()));
        }
        System.out.println("Total de registros: " + ativos.size());
    }
}
