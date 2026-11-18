
package com.example.produtoapi.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    /*
    Informa ao JPA que uma categoria pode estar relacionada a vários produtos.

    mappedBy = "categoria": informa que o relacionamento é controlado
    pelo atributo **categoria** que existirá dentro da entidade **Produto**

    FetchType.Lazy: os produtos não serão carregador automaticamente
    sempre que buscarmos uma categoria. Eles serão carregados quando
    forem acessados, dentro de um contexto de persistência.

    */
    @OneToMany(
        mappedBy = "categoria",
        fetch = FetchType.LAZY
    )
    private List<Produto> produtos = new ArrayList<>();

    public Categoria() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }
}
