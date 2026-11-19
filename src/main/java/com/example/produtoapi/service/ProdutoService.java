package com.example.produtoapi.service;

import com.example.produtoapi.dto.CategoriaResponseDTO;
import com.example.produtoapi.dto.ProdutoRequestDTO;
import com.example.produtoapi.dto.ProdutoResponseDTO;
import com.example.produtoapi.exception.CategoriaNaoEncontradaException;
import com.example.produtoapi.exception.ProdutoNaoEncontradoException;
import com.example.produtoapi.model.Categoria;
import com.example.produtoapi.model.Produto;
import com.example.produtoapi.repository.CategoriaRepository;
import com.example.produtoapi.repository.ProdutoRepository;

// import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import org.springframework.web.server.ResponseStatusException;
import java.util.List;

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
}
