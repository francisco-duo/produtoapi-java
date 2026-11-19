package com.example.produtoapi.controller;

import com.example.produtoapi.dto.CategoriaRequestDTO;
import com.example.produtoapi.dto.CategoriaResponseDTO;
import com.example.produtoapi.service.CategoriaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponseDTO criar(@Valid @RequestBody CategoriaRequestDTO dto) {
        return service.criar(dto);
    }

    @GetMapping
    public List<CategoriaResponseDTO> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public CategoriaResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDTO atualizar(
        @PathVariable Long id,
        @Valid @RequestBody CategoriaRequestDTO dto
    ) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
