package com.example.produtoapi.service;

import java.math.BigDecimal;
// import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.produtoapi.dto.CategoriaResponseDTO;
import com.example.produtoapi.dto.ProdutoRequestDTO;
import com.example.produtoapi.dto.ProdutoResponseDTO;
import com.example.produtoapi.dto.ValorEstoqueCategoriaDTO;
import com.example.produtoapi.dto.ValorEstoqueDTO;
import com.example.produtoapi.exception.CategoriaNaoEncontradaException;
import com.example.produtoapi.exception.ProdutoNaoEncontradoException;
import com.example.produtoapi.model.Categoria;
import com.example.produtoapi.model.Produto;
import com.example.produtoapi.repository.CategoriaRepository;
import com.example.produtoapi.repository.ProdutoRepository;

@Service
@Transactional
public class ProdutoService {
    
    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
            .orElseThrow(() -> new CategoriaNaoEncontradaException("Categoria não encontrada."));

        Produto produto = new Produto();

        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidade(dto.quantidade());
        produto.setCategoria(categoria);

        Produto salvo = repository.save(produto);

        return converterParaDTO(salvo);
    }

    public List<ProdutoResponseDTO> listarTodos() {
        
        return repository.findAll()
                    .stream()
                    .map(this::converterParaDTO)
                    .toList();
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        
        Produto produto = repository.findById(id)
                            .orElseThrow(() ->
                                new ProdutoNaoEncontradoException("Produto não encontrado"));
        
        return converterParaDTO(produto);
    }

    public ProdutoResponseDTO atualizar(
        Long id,
        ProdutoRequestDTO dto
    ) {
        Produto produto = repository.findById(id)
            .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
            .orElseThrow(() -> new CategoriaNaoEncontradaException("Categoria não encontrada"));
        
        
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidade(dto.quantidade());
        produto.setCategoria(categoria);

        Produto atualizado = repository.save(produto);

        return converterParaDTO(atualizado);
    }

    public void excluir(Long id) {
        
        if (!repository.existsById(id)) {
            throw new ProdutoNaoEncontradoException("Produto não encontrado");
        }

        repository.deleteById(id);
    }

    private ProdutoResponseDTO converterParaDTO(Produto produto) {

        // Produtos cadastrados antes da criação de categorias podem não ter uma
        CategoriaResponseDTO categoriaDTO = produto.getCategoria() == null
            ? null
            : new CategoriaResponseDTO(
                produto.getCategoria().getId(),
                produto.getCategoria().getNome()
            );

        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidade(),
                categoriaDTO
        );
    }

    public Page<ProdutoResponseDTO> listarPaginado(Pageable pageable) {
        /*
        repository,findAll(pageable): solicita ao banco apenas os registros da página desejada.

        Page<Produto> é o retorno do método findAll(pageable), que contém os produtos da página solicitada.

        .map(this::converterParaDTO): converte cada Produto em ProdutoResponseDTO, mantendo a estrutura de página.

        O resultado final é um Page<ProdutoResponseDTO> que contém os produtos da página solicitada, já convertidos para o formato de resposta.
        */
        return repository.findAll(pageable)
                .map(this::converterParaDTO);
    }

    public List<ProdutoResponseDTO> buscarPorNome(String nome) {

        return repository.findByNomeContainingIgnoreCase(nome)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    public List<ProdutoResponseDTO> buscarPorCategoria(Long categoriaId) {

        return repository.findByCategoriaId(categoriaId)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    public List<ProdutoResponseDTO> buscarPorFaixaDePreco(
        BigDecimal minimo,
        BigDecimal maximo
    ) {

        return repository.findByPrecoBetween(minimo, maximo)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    public List<ProdutoResponseDTO> buscarPorQuantidade(Integer quantidade) {

        return repository.findByQuantidadeGreaterThan(quantidade)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    // ===== Consultas com @Query (JPQL) =====

    public List<ProdutoResponseDTO> buscarProdutosMaisCarosQue(BigDecimal preco) {

        return repository.buscarProdutosMaisCarosQue(preco)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    public List<ProdutoResponseDTO> buscarPorCategoriaComQuery(Long categoriaId) {

        return repository.buscarPorCategoriaComQuery(categoriaId)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    public ValorEstoqueDTO calcularValorTotalEstoque() {

        // SUM retorna null quando não há produtos cadastrados
        BigDecimal total = repository.calcularValorTotalEstoque();

        return new ValorEstoqueDTO(total == null ? BigDecimal.ZERO : total);
    }

    // ===== Consultas nativas (SQL) =====

    public List<ProdutoResponseDTO> buscarProdutosComEstoqueBaixo(Integer quantidade) {

        return repository.buscarProdutosComEstoqueBaixo(quantidade)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    public List<ProdutoResponseDTO> buscarProdutosPorNomeCategoria(String nome) {

        return repository.buscarProdutosPorNomeCategoria(nome)
            .stream()
            .map(this::converterParaDTO)
            .toList();
    }

    public List<ValorEstoqueCategoriaDTO> calcularValorEstoquePorCategoria() {

        /*
        Consultas nativas com várias colunas retornam List<Object[]>.
        Cada Object[] é uma linha: [0] = nome da categoria, [1] = soma.
        O tipo numérico pode variar conforme o driver, por isso
        convertemos a partir de Number.
        */
        return repository.calcularValorEstoquePorCategoria()
            .stream()
            .map(linha -> new ValorEstoqueCategoriaDTO(
                (String) linha[0],
                linha[1] == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(linha[1].toString())
            ))
            .toList();
    }
}
