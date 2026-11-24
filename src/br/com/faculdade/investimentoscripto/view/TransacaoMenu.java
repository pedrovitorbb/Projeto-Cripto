package br.com.faculdade.investimentoscripto.view;

import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.TransacaoDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Transacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Menu do módulo Transação.
 *
 * Regras:
 *  - tipo só pode ser COMPRA ou VENDA (CHECK no banco);
 *  - a tabela T_SIP_TRANSACAO não tem coluna de ticker, por isso ele não é pedido;
 *  - quantidade e valor unitário > 0;
 *  - hash é opcional, mas se informado tem que ser único;
 *  - a carteira informada precisa existir.
 */
public class TransacaoMenu {

    /** ds_hash_blockchain e VARCHAR2(64); qt_transacao e NUMBER(16,8); vl_unitario e NUMBER(15,2). */
    private static final int TAMANHO_MAX_HASH = 64;
    private static final int CASAS_QUANTIDADE = 8;
    private static final int CASAS_VALOR = 2;
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TransacaoMenu() {
    }

    public static void executar() {
        int opcao;
        do {
            System.out.println();
            System.out.println("========== TRANSAÇÕES ==========");
            System.out.println("1 - Incluir transação");
            System.out.println("2 - Alterar transação");
            System.out.println("3 - Excluir transação");
            System.out.println("4 - Exibir todas");
            System.out.println("5 - Exibir um registro");
            System.out.println("6 - Listar por carteira");
            System.out.println("7 - Listar por tipo (COMPRA/VENDA)");
            System.out.println("8 - Somar valor movimentado por carteira");
            System.out.println("0 - Voltar");
            opcao = Console.lerInt("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> incluir();
                    case 2 -> alterar();
                    case 3 -> excluir();
                    case 4 -> exibirTodas();
                    case 5 -> exibirUma();
                    case 6 -> listarPorCarteira();
                    case 7 -> listarPorTipo();
                    case 8 -> somarPorCarteira();
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
        System.out.println("\n--- Incluir transação ---");

        int idCarteira = Console.lerInt("ID da carteira: ");
        if (!carteiraExiste(idCarteira)) {
            System.out.println("Carteira " + idCarteira + " não encontrada. Cadastre a carteira antes.");
            return;
        }

        Transacao.Tipo tipo = lerTipo();
        BigDecimal quantidade = lerPositivo("Quantidade: ", CASAS_QUANTIDADE);
        BigDecimal valorUnitario = lerPositivo("Valor unitário (R$): ", CASAS_VALOR);

        TransacaoDAO dao = new TransacaoDAO();
        String hash = lerHash(dao, null);

        // A coluna DATE nao guarda fracao de segundo.
        Transacao t = new Transacao(null, (long) idCarteira, tipo, quantidade, valorUnitario, hash,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        dao.inserir(t);
        System.out.println("Transação cadastrada com sucesso!");
        imprimirDetalhe(t);
    }

    // ---------------------------------------------------------------- ALTERAR

    private static void alterar() throws PersistenciaException {
        System.out.println("\n--- Alterar transação ---");
        TransacaoDAO dao = new TransacaoDAO();

        Transacao t = buscarPeloId(dao);
        if (t == null) {
            return;
        }

        imprimirDetalhe(t);
        System.out.println("(Deixe em branco para manter o valor atual)");

        Transacao.Tipo tipo = lerTipoOpcional(t.getTipo());
        BigDecimal quantidade = lerPositivoOpcional("Nova quantidade", t.getQuantidade(), CASAS_QUANTIDADE);
        BigDecimal valorUnitario = lerPositivoOpcional("Novo valor unitário (R$)", t.getValorUnitario(), CASAS_VALOR);

        System.out.println("Hash atual: " + (t.getHashBlockchain() == null ? "(nenhum)" : t.getHashBlockchain()));
        String hash = t.getHashBlockchain();
        if (Console.confirmar("Deseja alterar o hash? (S/N): ")) {
            hash = lerHash(dao, t.getId());
        }

        t.setTipo(tipo);
        t.setQuantidade(quantidade);
        t.setValorUnitario(valorUnitario);
        t.setHashBlockchain(hash);

        if (dao.atualizar(t)) {
            System.out.println("Transação atualizada com sucesso!");
            imprimirDetalhe(t);
        } else {
            System.out.println("A transação não foi atualizada (ela pode ter sido excluída).");
        }
    }

    // ---------------------------------------------------------------- EXCLUIR

    private static void excluir() throws PersistenciaException {
        System.out.println("\n--- Excluir transação ---");
        TransacaoDAO dao = new TransacaoDAO();

        Transacao t = buscarPeloId(dao);
        if (t == null) {
            return;
        }

        imprimirDetalhe(t);
        if (!Console.confirmar("Deseja realmente excluir esta transação? (S/N): ")) {
            System.out.println("Exclusão cancelada.");
            return;
        }

        if (dao.deletar(t.getId())) {
            System.out.println("Transação excluída com sucesso!");
        } else {
            System.out.println("A transação não foi excluída (ela pode já ter sido removida).");
        }
    }

    // ---------------------------------------------------------------- EXIBIR

    private static void exibirTodas() throws PersistenciaException {
        System.out.println("\n--- Todas as transações ---");
        imprimirTabela(new TransacaoDAO().listarTodos());
    }

    private static void exibirUma() throws PersistenciaException {
        System.out.println("\n--- Exibir transação ---");
        Transacao t = buscarPeloId(new TransacaoDAO());
        if (t != null) {
            imprimirDetalhe(t);
        }
    }

    // ---------------------------------------------------------------- EXTRAS

    private static void listarPorCarteira() throws PersistenciaException {
        System.out.println("\n--- Transações por carteira ---");
        int idCarteira = Console.lerInt("ID da carteira: ");
        if (!carteiraExiste(idCarteira)) {
            System.out.println("Carteira " + idCarteira + " não encontrada.");
            return;
        }
        imprimirTabela(new TransacaoDAO().listarPorCarteira(idCarteira));
    }

    private static void listarPorTipo() throws PersistenciaException {
        System.out.println("\n--- Transações por tipo ---");
        Transacao.Tipo tipo = lerTipo();
        imprimirTabela(new TransacaoDAO().listarPorTipo(tipo));
    }

    private static void somarPorCarteira() throws PersistenciaException {
        System.out.println("\n--- Valor movimentado por carteira ---");
        int idCarteira = Console.lerInt("ID da carteira: ");
        if (!carteiraExiste(idCarteira)) {
            System.out.println("Carteira " + idCarteira + " não encontrada.");
            return;
        }
        BigDecimal soma = new TransacaoDAO().somarValorPorCarteira(idCarteira);
        if (soma == null) {
            soma = BigDecimal.ZERO; // SUM de carteira sem transações volta NULL no Oracle
        }
        System.out.println("Total movimentado pela carteira " + idCarteira + ": R$ "
                + soma.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    // ---------------------------------------------------------------- AUXILIARES

    private static boolean carteiraExiste(int idCarteira) throws PersistenciaException {
        return new CarteiraDAO().buscarPorId(idCarteira) != null;
    }

    private static Transacao buscarPeloId(TransacaoDAO dao) throws PersistenciaException {
        int id = Console.lerInt("ID da transação: ");
        Transacao t = dao.buscarPorId(id);
        if (t == null) {
            System.out.println("Transação " + id + " não encontrada.");
        }
        return t;
    }

    /** Menu numérico evita erro de digitação e garante o CHECK (COMPRA/VENDA). */
    private static Transacao.Tipo lerTipo() {
        while (true) {
            int op = Console.lerInt("Tipo (1 - COMPRA | 2 - VENDA): ");
            if (op == 1) {
                return Transacao.Tipo.COMPRA;
            }
            if (op == 2) {
                return Transacao.Tipo.VENDA;
            }
            System.out.println("Opção inválida. Digite 1 ou 2.");
        }
    }

    private static Transacao.Tipo lerTipoOpcional(Transacao.Tipo atual) {
        while (true) {
            String texto = Console.lerTexto("Tipo (1 - COMPRA | 2 - VENDA) [" + atual + "]: ").trim();
            if (texto.isEmpty()) {
                return atual;
            }
            if (texto.equals("1") || texto.equalsIgnoreCase("COMPRA")) {
                return Transacao.Tipo.COMPRA;
            }
            if (texto.equals("2") || texto.equalsIgnoreCase("VENDA")) {
                return Transacao.Tipo.VENDA;
            }
            System.out.println("Opção inválida. Digite 1, 2 ou deixe em branco.");
        }
    }

    /**
     * Hash opcional: Enter vazio = sem hash (null no banco).
     * Se informado, confere se outra transação já usa o mesmo hash.
     * idAtual = id da transação sendo alterada (null na inclusão), para não acusar duplicidade nela mesma.
     */
    private static String lerHash(TransacaoDAO dao, Long idAtual) throws PersistenciaException {
        while (true) {
            String hash = Console.lerTexto("Hash da transação (opcional, Enter para pular): ").trim();
            if (hash.isEmpty()) {
                return null;
            }
            if (hash.length() > TAMANHO_MAX_HASH) {
                System.out.println("O hash deve ter no máximo " + TAMANHO_MAX_HASH + " caracteres.");
                continue;
            }
            if (!hashEmUso(dao, hash, idAtual)) {
                return hash;
            }
            System.out.println("Esse hash já está registrado em outra transação. Informe outro.");
        }
    }

    private static boolean hashEmUso(TransacaoDAO dao, String hash, Long idAtual)
            throws PersistenciaException {
        for (Transacao t : dao.listarTodos()) {
            boolean mesmaTransacao = idAtual != null && idAtual.equals(t.getId());
            if (!mesmaTransacao && hash.equals(t.getHashBlockchain())) {
                return true;
            }
        }
        return false;
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

    private static BigDecimal valorTotal(Transacao t) {
        if (t.getQuantidade() == null || t.getValorUnitario() == null) {
            return null;
        }
        return t.getValorTotal().setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatar(BigDecimal valor) {
        return valor == null ? "-" : valor.stripTrailingZeros().toPlainString();
    }

    private static String formatar(LocalDateTime data) {
        return data == null ? "-" : data.format(FORMATO_DATA);
    }

    private static void imprimirDetalhe(Transacao t) {
        System.out.println("----------------------------------");
        System.out.println("ID ..............: " + t.getId());
        System.out.println("Carteira ........: " + t.getIdCarteira());
        System.out.println("Tipo ............: " + t.getTipo());
        System.out.println("Quantidade ......: " + formatar(t.getQuantidade()));
        System.out.println("Valor unit. (R$) : " + formatar(t.getValorUnitario()));
        System.out.println("Valor total (R$) : " + formatar(valorTotal(t)));
        System.out.println("Data ............: " + formatar(t.getDataHora()));
        System.out.println("Hash ............: " + (t.getHashBlockchain() == null ? "(nenhum)" : t.getHashBlockchain()));
        System.out.println("----------------------------------");
    }

    private static void imprimirTabela(List<Transacao> transacoes) {
        if (transacoes == null || transacoes.isEmpty()) {
            System.out.println("Nenhuma transação encontrada.");
            return;
        }
        System.out.printf("%-5s %-9s %-7s %14s %14s %14s %-17s%n",
                "ID", "CARTEIRA", "TIPO", "QUANTIDADE", "VL UNIT.", "VL TOTAL", "DATA");
        for (Transacao t : transacoes) {
            System.out.printf("%-5d %-9d %-7s %14s %14s %14s %-17s%n",
                    t.getId(), t.getIdCarteira(), t.getTipo(),
                    formatar(t.getQuantidade()), formatar(t.getValorUnitario()),
                    formatar(valorTotal(t)), formatar(t.getDataHora()));
        }
        System.out.println("Total de registros: " + transacoes.size());
    }
}
