package com.example.produtoapi.service;

import com.example.produtoapi.dto.ProdutoRequestDTO;
import com.example.produtoapi.dto.ProdutoResponseDTO;
import com.example.produtoapi.model.Produto;
import com.example.produtoapi.repository.ProdutoRepository;

// import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
// import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ProdutoService {
    
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Produto produto = new Produto();

        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidade(dto.quantidade());

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
                                new RuntimeException("Produto não encontrado"));
        
        return converterParaDTO(produto);
    }

    public ProdutoResponseDTO atualizar(
        Long id,
        ProdutoRequestDTO dto
    ) {
        Produto produto = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidade(dto.quantidade());

        Produto atualizado = repository.save(produto);

        return converterParaDTO(atualizado);
    }

    public void excluir(Long id) {
        
        if (!repository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado");
        }
    }

    private ProdutoResponseDTO converterParaDTO(Produto produto) {

        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidade()
        );
    }
}
