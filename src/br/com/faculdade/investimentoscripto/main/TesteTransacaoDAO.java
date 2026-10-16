package br.com.faculdade.investimentoscripto.main;

import br.com.faculdade.investimentoscripto.conexao.ConnectionFactory;
import br.com.faculdade.investimentoscripto.dao.CarteiraDAO;
import br.com.faculdade.investimentoscripto.dao.TransacaoDAO;
import br.com.faculdade.investimentoscripto.exception.PersistenciaException;
import br.com.faculdade.investimentoscripto.model.Carteira;
import br.com.faculdade.investimentoscripto.model.Transacao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Teste executavel do TransacaoDAO, rodando no schema configurado no db.properties.
 *
 * <p>Depende do DML: usa a primeira carteira ja cadastrada para satisfazer a FK.
 * Cria uma unica transacao temporaria, exercita todos os metodos do DAO, testa
 * dois erros do banco e remove a transacao no final, mesmo se algum passo falhar.</p>
 */
public class TesteTransacaoDAO {

    private static final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private static final CarteiraDAO carteiraDAO = new CarteiraDAO();

    private static final long ID_CARTEIRA_INEXISTENTE = 999999L;

    private static final String ERRO_VALOR_UNITARIO = "O valor unitário deve ser maior que zero";
    private static final String ERRO_CARTEIRA_INEXISTENTE = "A carteira informada não existe";

    /** Codigo Oracle de tabela ou view inexistente (ORA-00942). */
    private static final int TABELA_INEXISTENTE = 942;

    public static void main(String[] args) {
        titulo("TESTE DE CONEXAO");
        try {
            if (!ConnectionFactory.testarConexao()) {
                System.out.println("O banco nao respondeu ao teste de conexao.");
                return;
            }
            System.out.println("Conexao realizada com sucesso.");
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Nao foi possivel conectar ao Oracle.");
            System.out.println("Erro: " + e.getMessage());
            return;
        }

        Transacao transacao = null;

        try {
            titulo("PREPARACAO (carteira existente)");
            List<Carteira> carteiras = carteiraDAO.listarTodos();
            if (carteiras.isEmpty()) {
                System.out.println("Nenhuma carteira cadastrada. Rode o DDL e o DML antes deste teste.");
                return;
            }
            long idCarteira = carteiras.get(0).getId();
            System.out.println("Usando a carteira id=" + idCarteira);

            titulo("INSERT");
            String sufixo = String.valueOf(System.currentTimeMillis());
            LocalDateTime dataHora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            BigDecimal quantidade = new BigDecimal("0.25000000");
            BigDecimal valorUnitario = new BigDecimal("350000.00");
            String hash = "hash_teste_" + sufixo;

            transacao = new Transacao(null, idCarteira, Transacao.Tipo.COMPRA,
                    quantidade, valorUnitario, hash, dataHora);
            transacaoDAO.inserir(transacao);
            exigir(transacao.getId() != null, "O banco nao devolveu o id da transacao.");
            System.out.println("Inserida: id=" + transacao.getId() + " " + transacao);

            titulo("SELECT por id");
            Transacao lida = transacaoDAO.buscarPorId(transacao.getId());
            exigir(lida != null, "Transacao inserida nao foi encontrada.");
            exigir(lida.getIdCarteira().equals(idCarteira), "Carteira lida difere da gravada.");
            exigir(lida.getTipo() == Transacao.Tipo.COMPRA, "Tipo lido difere do gravado.");
            exigir(lida.getQuantidade().compareTo(quantidade) == 0, "Quantidade lida difere da gravada.");
            exigir(lida.getValorUnitario().compareTo(valorUnitario) == 0,
                    "Valor unitario lido difere do gravado.");
            exigir(hash.equals(lida.getHashBlockchain()), "Hash lido difere do gravado.");
            exigir(dataHora.equals(lida.getDataHora()),
                    "Data e hora lidas (" + lida.getDataHora() + ") diferem das gravadas (" + dataHora + ").");
            System.out.println("Lida: id=" + lida.getId() + " " + lida + " | dataHora=" + lida.getDataHora()
                    + " | idCarteira=" + lida.getIdCarteira());
            System.out.println("Todos os campos conferem com o que foi gravado.");

            titulo("SELECT listarTodos e listarPorCarteira");
            List<Transacao> todas = transacaoDAO.listarTodos();
            List<Transacao> daCarteira = transacaoDAO.listarPorCarteira(idCarteira);
            System.out.println("listarTodos: " + todas.size() + " transacao(oes)");
            System.out.println("listarPorCarteira(" + idCarteira + "): " + daCarteira.size() + " transacao(oes)");
            exigir(contem(todas, transacao.getId()), "A transacao nao apareceu em listarTodos.");
            exigir(contem(daCarteira, transacao.getId()), "A transacao nao apareceu em listarPorCarteira.");
            exigir(daCarteira.stream().allMatch(t -> t.getIdCarteira().equals(idCarteira)),
                    "listarPorCarteira trouxe transacao de outra carteira.");
            System.out.println("A transacao aparece nas duas listas.");

            titulo("SELECT listarPorTipo(COMPRA)");
            List<Transacao> compras = transacaoDAO.listarPorTipo(Transacao.Tipo.COMPRA);
            System.out.println("listarPorTipo(COMPRA): " + compras.size() + " transacao(oes)");
            exigir(contem(compras, transacao.getId()), "A transacao nao apareceu em listarPorTipo(COMPRA).");
            exigir(compras.stream().allMatch(t -> t.getTipo() == Transacao.Tipo.COMPRA),
                    "listarPorTipo(COMPRA) trouxe transacao de outro tipo.");
            System.out.println("A transacao aparece na lista de compras.");

            titulo("somarValorPorCarteira");
            BigDecimal total = transacaoDAO.somarValorPorCarteira(idCarteira);
            exigir(total != null, "A soma voltou null.");
            System.out.println("Valor da transacao de teste: " + transacao.getValorTotal().toPlainString());
            System.out.println("Total da carteira " + idCarteira + ": " + total.toPlainString());

            titulo("UPDATE");
            BigDecimal novoValor = new BigDecimal("362500.75");
            transacao.setTipo(Transacao.Tipo.VENDA);
            transacao.setValorUnitario(novoValor);
            exigir(transacaoDAO.atualizar(transacao), "Nenhuma transacao foi atualizada.");
            Transacao atualizada = transacaoDAO.buscarPorId(transacao.getId());
            exigir(atualizada != null, "A transacao atualizada nao foi encontrada.");
            exigir(atualizada.getTipo() == Transacao.Tipo.VENDA, "Tipo nao foi atualizado.");
            exigir(atualizada.getValorUnitario().compareTo(novoValor) == 0,
                    "Valor unitario nao foi atualizado.");
            System.out.println("Atualizada: id=" + atualizada.getId() + " " + atualizada);

            titulo("ERRO 1: valor unitario zero");
            esperarErro(new Transacao(null, idCarteira, Transacao.Tipo.COMPRA,
                    new BigDecimal("1.00000000"), BigDecimal.ZERO, null, null), ERRO_VALOR_UNITARIO);

            titulo("ERRO 2: carteira inexistente");
            exigir(carteiraDAO.buscarPorId(ID_CARTEIRA_INEXISTENTE) == null,
                    "A carteira " + ID_CARTEIRA_INEXISTENTE + " existe; escolha outro id para o teste.");
            esperarErro(new Transacao(null, ID_CARTEIRA_INEXISTENTE, Transacao.Tipo.COMPRA,
                    new BigDecimal("1.00000000"), new BigDecimal("100.00"), null, null), ERRO_CARTEIRA_INEXISTENTE);

            titulo("DELETE");
            exigir(transacaoDAO.deletar(transacao.getId()), "Nenhuma transacao foi excluida.");
            exigir(transacaoDAO.buscarPorId(transacao.getId()) == null, "A transacao ainda existe.");
            System.out.println("Exclusao confirmada.");
            transacao = null;

            System.out.println("\nTodos os testes do TransacaoDAO foram concluidos com sucesso.");
        } catch (Exception e) {
            System.out.println("\nOs testes foram interrompidos.");
            if (codigoOracle(e) == TABELA_INEXISTENTE) {
                System.out.println("ORA-00942: o DDL nao foi executado neste schema.");
            }
            System.out.println("Erro: " + e.getMessage());
        } finally {
            limpar(transacao);
        }
    }

    /**
     * Tenta inserir uma transacao invalida e exige a PersistenciaException com a
     * mensagem esperada. Se o banco aceitar, remove o registro e falha o teste.
     */
    private static void esperarErro(Transacao invalida, String mensagemEsperada) throws PersistenciaException {
        try {
            transacaoDAO.inserir(invalida);
        } catch (PersistenciaException e) {
            System.out.println("PersistenciaException recebida: " + e.getMessage());
            exigir(mensagemEsperada.equals(e.getMessage()),
                    "Mensagem inesperada. Esperado: \"" + mensagemEsperada + "\".");
            System.out.println("Erro tratado como esperado.");
            return;
        }

        transacaoDAO.deletar(invalida.getId());
        throw new IllegalStateException("O banco aceitou a transacao invalida (id=" + invalida.getId()
                + ", ja removida).");
    }

    /** Remove a transacao temporaria se o teste parou antes do DELETE. */
    private static void limpar(Transacao transacao) {
        if (transacao == null || transacao.getId() == null) {
            return;
        }
        try {
            transacaoDAO.deletar(transacao.getId());
            System.out.println("Limpeza: transacao temporaria id=" + transacao.getId() + " removida.");
        } catch (PersistenciaException e) {
            System.out.println("Aviso: nao foi possivel remover a transacao temporaria id="
                    + transacao.getId() + ": " + e.getMessage());
        }
    }

    private static boolean contem(List<Transacao> transacoes, Long id) {
        return transacoes.stream().anyMatch(t -> t.getId().equals(id));
    }

    /** Devolve o codigo Oracle da SQLException causadora, ou -1 se nao houver. */
    private static int codigoOracle(Exception e) {
        return e.getCause() instanceof SQLException sql ? sql.getErrorCode() : -1;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalStateException(mensagem);
        }
    }

    private static void titulo(String titulo) {
        System.out.println("\n===============================");
        System.out.println(titulo);
        System.out.println("===============================");
    }
}
