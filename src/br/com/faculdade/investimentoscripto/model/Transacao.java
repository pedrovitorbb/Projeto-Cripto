package br.com.faculdade.investimentoscripto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Transacao de compra ou venda de um ativo cripto (T_SIP_TRANSACAO).
 *
 * <p>Os campos {@code ticker}, {@code precoMedioCompra} e {@code ativo} existem
 * apenas no dominio: nao ha coluna para eles na tabela e o TransacaoDAO nao os
 * persiste. A ligacao com a carteira no banco e feita por {@code idCarteira}.</p>
 */
public class Transacao {

    public enum Tipo { COMPRA, VENDA }

    private Long id;
    private Long idCarteira;
    private String ticker;
    private BigDecimal quantidade;
    private double precoMedioCompra;
    private BigDecimal valorUnitario;
    private String hashBlockchain;
    private Tipo tipo;
    private LocalDateTime dataHora;

    private Carteira carteira;
    private AtivoCripto ativo;

    /**
     * Construtor de dominio: registra a transacao no ativo e usa a data e hora atuais.
     * Preenche {@code idCarteira} a partir da carteira, quando ela nao for null.
     */
    public Transacao(Carteira carteira, AtivoCripto ativo, Tipo tipo,
                     BigDecimal quantidade, double precoMedioCompra,
                     BigDecimal valorUnitario, String hashBlockchain) {
        this.carteira = carteira;
        this.idCarteira = carteira == null ? null : carteira.getId();
        this.ativo = ativo;
        this.ticker = ativo.getTicker();
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.precoMedioCompra = precoMedioCompra;
        this.valorUnitario = valorUnitario;
        this.hashBlockchain = hashBlockchain;
        this.dataHora = LocalDateTime.now();

        ativo.adicionarTransacao(this);
    }

    /**
     * Construtor de persistencia: usado pelo TransacaoDAO ao ler o banco ou ao
     * montar uma transacao para gravar. Nao exige AtivoCripto, nao registra a
     * transacao em nenhum ativo e nao altera a data e hora informadas.
     */
    public Transacao(Long id, Long idCarteira, Tipo tipo, BigDecimal quantidade,
                     BigDecimal valorUnitario, String hashBlockchain, LocalDateTime dataHora) {
        this.id = id;
        this.idCarteira = idCarteira;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.hashBlockchain = hashBlockchain;
        this.dataHora = dataHora;
    }

    /** Valor total da transacao: quantidade multiplicada pelo valor unitario. */
    public BigDecimal getValorTotal() {
        return quantidade.multiply(valorUnitario);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdCarteira() { return idCarteira; }
    public void setIdCarteira(Long idCarteira) { this.idCarteira = idCarteira; }

    public String getTicker() { return ticker; }
    public BigDecimal getQuantidade() { return quantidade; }
    public double getPrecoMedioCompra() { return precoMedioCompra; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public String getHashBlockchain() { return hashBlockchain; }
    public Tipo getTipo() { return tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public Carteira getCarteira() { return carteira; }
    public AtivoCripto getAtivo() { return ativo; }

    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    public void setHashBlockchain(String hashBlockchain) { this.hashBlockchain = hashBlockchain; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    /**
     * Usa Locale.US para os numeros sairem com ponto decimal. O ticker nao vem
     * do banco, entao aparece como "-"; o hash e opcional e aparece como "sem hash".
     */
    @Override
    public String toString() {
        String hash = hashBlockchain == null
                ? "sem hash"
                : hashBlockchain.substring(0, Math.min(10, hashBlockchain.length())) + "...";
        return String.format(Locale.US, "[%s] %s %.8f @ %.2f (hash=%s)",
                tipo, ticker == null ? "-" : ticker, quantidade, valorUnitario, hash);
    }
}
