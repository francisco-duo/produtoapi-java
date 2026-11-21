package com.example.produtoapi.repository;

import com.example.produtoapi.model.Produto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Não é necessário implementar os métodos de CRUD.
    // O Spring Data JPA já disponibiliza
    /*
    Método	            Funcionamento
    ____________________________________________
    save()	            Salva ou atualiza
    findAll()	        Busca todos
    findById()	        Busca por ID
    deleteById()	    Exclui por ID
    existsById()	    Verifica existência
    */

    // Verifica se algum produto pertence à categoria informada
    boolean existsByCategoriaId(Long categoriaId);

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByCategoriaId(Long categoriaId);

    List<Produto> findByPrecoBetween(BigDecimal minimo, BigDecimal maximo);

    List<Produto> findByQuantidadeGreaterThan(Integer quantidade);

    @Query("SELECT p FROM Produto p WHERE p.preco > :preco")
    List<Produto> buscarProdutosMaisCarosQue(@Param("preco") BigDecimal preco);

    @Query("""
        SELECT p
        FROM Produto p
        JOIN p.categoria c
        WHERE c.id = :categoriaId
        """)
    List<Produto> buscarPorCategoriaComQuery(@Param("categoriaId") Long categoriaId);

    @Query("SELECT SUM(p.preco * p.quantidade) FROM Produto p")
    BigDecimal calcularValorTotalEstoque();

    @Query(
        value = "SELECT * FROM produtos WHERE quantidade < :quantidade",
        nativeQuery = true
    )
    List<Produto> buscarProdutosComEstoqueBaixo(
        @Param("quantidade") Integer quantidade
    );

    @Query(
        value = """
            SELECT p.*
            FROM produtos p
            INNER JOIN categorias c ON c.id = p.categoria_id
            WHERE c.nome = :nome
        """,
        nativeQuery = true
    )
    List<Produto> buscarProdutosPorNomeCategoria(
        @Param("nome") String nome
    );

    @Query(
        value = """
            SELECT c.nome, SUM(p.preco * p.quantidade)
            FROM produtos p
            INNER JOIN categorias c ON c.id = p.categoria_id
            GROUP BY c.nome
        """,
        nativeQuery = true
    )
    List<Object[]> calcularValorEstoquePorCategoria();
}
