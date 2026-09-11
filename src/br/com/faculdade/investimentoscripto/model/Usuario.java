package br.com.faculdade.investimentoscripto.model;

public abstract class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String senhaHash;
    private String cpf;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String email, String senhaHash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    /** Construtor completo, incluindo o CPF (coluna cpf_usuario de T_SIP_USUARIO). */
    public Usuario(Long id, String nome, String email, String senhaHash, String cpf) {
        this(id, nome, email, senhaHash);
        this.cpf = cpf;
    }

    public abstract void exibirPerfil();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    /** @return o CPF com 11 digitos, sem pontuacao */
    public String getCpf() {
        return cpf;
    }

    /** @param cpf CPF com 11 digitos, sem pontuacao */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
