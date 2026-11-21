package com.example.produtoapi.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.produtoapi.dto.ProdutoRequestDTO;
import com.example.produtoapi.dto.ProdutoResponseDTO;
import com.example.produtoapi.dto.ValorEstoqueCategoriaDTO;
import com.example.produtoapi.dto.ValorEstoqueDTO;
import com.example.produtoapi.service.ProdutoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    
    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponseDTO criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        return service.criar(dto);
    }

    @GetMapping
    public List<ProdutoResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/paginado")
    public Page<ProdutoResponseDTO> listarPaginado(@PageableDefault (size = 10) Pageable pageable) {
        // http://127.0.0.1:8080/api/produtos/paginado?page=1&size=2
        return service.listarPaginado(pageable);
    }

    @GetMapping("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ProdutoResponseDTO atualizar(
        @PathVariable Long id,
        @Valid @RequestBody ProdutoRequestDTO dto
    ) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/buscar")
    public List<ProdutoResponseDTO> buscarPorNome(
        @RequestParam String nome
    ) {
        return service.buscarPorNome(nome);
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<ProdutoResponseDTO> buscarPorCategoria(
        @PathVariable Long categoriaId
    ) {
        return service.buscarPorCategoria(categoriaId);
    }

    @GetMapping("/preco")
    public List<ProdutoResponseDTO> buscarPorFaixaDePreco(
        @RequestParam BigDecimal minimo,
        @RequestParam BigDecimal maximo
    ) {
        return service.buscarPorFaixaDePreco(minimo, maximo);
    }

    @GetMapping("/quantidade")
    public List<ProdutoResponseDTO> buscarPorQuantidade(
        @RequestParam Integer quantidade
    ) {
        return service.buscarPorQuantidade(quantidade);
    }

    // ===== Consultas com @Query (JPQL) =====

    @GetMapping("/mais-caros")
    public List<ProdutoResponseDTO> buscarProdutosMaisCarosQue(
        @RequestParam BigDecimal preco
    ) {
        // http://127.0.0.1:8080/api/produtos/mais-caros?preco=100
        return service.buscarProdutosMaisCarosQue(preco);
    }

    @GetMapping("/categoria/{categoriaId}/query")
    public List<ProdutoResponseDTO> buscarPorCategoriaComQuery(
        @PathVariable Long categoriaId
    ) {
        // http://127.0.0.1:8080/api/produtos/categoria/1/query
        return service.buscarPorCategoriaComQuery(categoriaId);
    }

    @GetMapping("/estoque/valor-total")
    public ValorEstoqueDTO calcularValorTotalEstoque() {
        // http://127.0.0.1:8080/api/produtos/estoque/valor-total
        return service.calcularValorTotalEstoque();
    }

    // ===== Consultas nativas (SQL) =====

    @GetMapping("/estoque/baixo")
    public List<ProdutoResponseDTO> buscarProdutosComEstoqueBaixo(
        @RequestParam Integer quantidade
    ) {
        // http://127.0.0.1:8080/api/produtos/estoque/baixo?quantidade=5
        return service.buscarProdutosComEstoqueBaixo(quantidade);
    }

    @GetMapping("/categoria/nome")
    public List<ProdutoResponseDTO> buscarProdutosPorNomeCategoria(
        @RequestParam String nome
    ) {
        // http://127.0.0.1:8080/api/produtos/categoria/nome?nome=Informatica
        return service.buscarProdutosPorNomeCategoria(nome);
    }

    @GetMapping("/estoque/valor-por-categoria")
    public List<ValorEstoqueCategoriaDTO> calcularValorEstoquePorCategoria() {
        // http://127.0.0.1:8080/api/produtos/estoque/valor-por-categoria
        return service.calcularValorEstoquePorCategoria();
    }
}
