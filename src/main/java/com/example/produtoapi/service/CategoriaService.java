package com.example.produtoapi.service;

import com.example.produtoapi.dto.CategoriaRequestDTO;
import com.example.produtoapi.dto.CategoriaResponseDTO;
import com.example.produtoapi.exception.CategoriaNaoEncontradaException;
import com.example.produtoapi.exception.ConflitoException;
import com.example.produtoapi.model.Categoria;
import com.example.produtoapi.repository.CategoriaRepository;
import com.example.produtoapi.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository repository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(CategoriaRepository repository, ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {

        if (repository.existsByNome(dto.nome())) {
            throw new ConflitoException("Já existe uma categoria com esse nome.");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome());

        Categoria salva = repository.save(categoria);

        return converterParaDTO(salva);
    }

    public List<CategoriaResponseDTO> listarTodas() {

        return repository.findAll()
                    .stream()
                    .map(this::converterParaDTO)
                    .toList();
    }

    public CategoriaResponseDTO buscarPorId(Long id) {

        return converterParaDTO(buscarEntidade(id));
    }

    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {

        Categoria categoria = buscarEntidade(id);

        // Só verifica duplicidade se o nome realmente mudou
        if (!categoria.getNome().equals(dto.nome()) && repository.existsByNome(dto.nome())) {
            throw new ConflitoException("Já existe uma categoria com esse nome.");
        }

        categoria.setNome(dto.nome());

        Categoria atualizada = repository.save(categoria);

        return converterParaDTO(atualizada);
    }

    public void excluir(Long id) {

        if (!repository.existsById(id)) {
            throw new CategoriaNaoEncontradaException("Categoria não encontrada.");
        }

        if (produtoRepository.existsByCategoriaId(id)) {
            throw new ConflitoException("Não é possível excluir uma categoria que possui produtos.");
        }

        repository.deleteById(id);
    }

    private Categoria buscarEntidade(Long id) {

        return repository.findById(id)
            .orElseThrow(() -> new CategoriaNaoEncontradaException("Categoria não encontrada."));
    }

    private CategoriaResponseDTO converterParaDTO(Categoria categoria) {

        return new CategoriaResponseDTO(
            categoria.getId(),
            categoria.getNome()
        );
    }
}
