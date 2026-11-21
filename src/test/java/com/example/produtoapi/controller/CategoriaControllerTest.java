package com.example.produtoapi.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.produtoapi.dto.CategoriaRequestDTO;
import com.example.produtoapi.dto.CategoriaResponseDTO;
import com.example.produtoapi.exception.CategoriaNaoEncontradaException;
import com.example.produtoapi.exception.ConflitoException;
import com.example.produtoapi.service.CategoriaService;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    private static final String URL = "/api/categorias";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService service;

    private final CategoriaResponseDTO informatica = new CategoriaResponseDTO(1L, "Informatica");

    @Test
    @DisplayName("POST deve criar e retornar 201")
    void deveCriar() throws Exception {
        when(service.criar(any(CategoriaRequestDTO.class))).thenReturn(informatica);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Informatica\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Informatica"));
    }

    @Test
    @DisplayName("POST com nome vazio deve retornar 400")
    void deveRetornar400ComNomeVazio() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensagem").value("nome: O nome é obrigatório"));

        verify(service, never()).criar(any());
    }

    @Test
    @DisplayName("POST com nome duplicado deve retornar 409")
    void deveRetornar409ComNomeDuplicado() throws Exception {
        when(service.criar(any(CategoriaRequestDTO.class)))
            .thenThrow(new ConflitoException("Já existe uma categoria com esse nome."));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Informatica\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET deve listar todas")
    void deveListarTodas() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(informatica));

        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /{id} deve retornar a categoria")
    void deveBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(informatica);

        mockMvc.perform(get(URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Informatica"));
    }

    @Test
    @DisplayName("GET /{id} inexistente deve retornar 404")
    void deveRetornar404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new CategoriaNaoEncontradaException("Categoria não encontrada."));

        mockMvc.perform(get(URL + "/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{id} deve atualizar")
    void deveAtualizar() throws Exception {
        when(service.atualizar(eq(1L), any(CategoriaRequestDTO.class)))
            .thenReturn(new CategoriaResponseDTO(1L, "Tecnologia"));

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Tecnologia\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Tecnologia"));
    }

    @Test
    @DisplayName("DELETE /{id} deve retornar 204")
    void deveExcluir() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
            .andExpect(status().isNoContent());

        verify(service).excluir(1L);
    }

    @Test
    @DisplayName("DELETE /{id} de categoria com produtos deve retornar 409")
    void deveRetornar409AoExcluirComProdutos() throws Exception {
        doThrow(new ConflitoException("Não é possível excluir uma categoria que possui produtos."))
            .when(service).excluir(1L);

        mockMvc.perform(delete(URL + "/1"))
            .andExpect(status().isConflict());
    }
}
