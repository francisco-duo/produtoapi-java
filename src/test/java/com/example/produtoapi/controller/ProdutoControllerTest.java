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

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.produtoapi.dto.CategoriaResponseDTO;
import com.example.produtoapi.dto.ProdutoRequestDTO;
import com.example.produtoapi.dto.ProdutoResponseDTO;
import com.example.produtoapi.dto.ValorEstoqueCategoriaDTO;
import com.example.produtoapi.dto.ValorEstoqueDTO;
import com.example.produtoapi.exception.CategoriaNaoEncontradaException;
import com.example.produtoapi.exception.ProdutoNaoEncontradoException;
import com.example.produtoapi.service.ProdutoService;

/*
@WebMvcTest sobe apenas a camada web (controller + GlobalExceptionHandler),
sem banco de dados. O service é substituído por um mock com @MockitoBean.
*/
@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    private static final String URL = "/api/produtos";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService service;

    private final ProdutoResponseDTO notebook = new ProdutoResponseDTO(
        10L, "Notebook", "16GB", new BigDecimal("4500.00"), 10,
        new CategoriaResponseDTO(1L, "Informatica"));

    private static final String JSON_VALIDO = """
        {
          "nome": "Notebook",
          "descricao": "16GB",
          "preco": 4500.00,
          "quantidade": 10,
          "categoriaId": 1
        }
        """;

    // ===== CRUD =====

    @Test
    @DisplayName("POST deve criar e retornar 201")
    void deveCriar() throws Exception {
        when(service.criar(any(ProdutoRequestDTO.class))).thenReturn(notebook);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.nome").value("Notebook"))
            .andExpect(jsonPath("$.categoria.nome").value("Informatica"));
    }

    @Test
    @DisplayName("POST com dados inválidos deve retornar 400 sem chamar o service")
    void deveRetornar400ComDadosInvalidos() throws Exception {
        String invalido = """
            { "nome": "", "preco": 0, "quantidade": -1 }
            """;

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));

        verify(service, never()).criar(any());
    }

    @Test
    @DisplayName("POST com categoria inexistente deve retornar 404")
    void deveRetornar404SemCategoria() throws Exception {
        when(service.criar(any(ProdutoRequestDTO.class)))
            .thenThrow(new CategoriaNaoEncontradaException("Categoria não encontrada."));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensagem").value("Categoria não encontrada."));
    }

    @Test
    @DisplayName("GET deve listar todos")
    void deveListarTodos() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].nome").value("Notebook"));
    }

    @Test
    @DisplayName("GET /paginado deve repassar page e size ao service")
    void deveListarPaginado() throws Exception {
        Pageable pageable = PageRequest.of(1, 2);
        when(service.listarPaginado(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(notebook), pageable, 3));

        mockMvc.perform(get(URL + "/paginado").param("page", "1").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(10));

        verify(service).listarPaginado(PageRequest.of(1, 2));
    }

    @Test
    @DisplayName("GET /{id} deve retornar o produto")
    void deveBuscarPorId() throws Exception {
        when(service.buscarPorId(10L)).thenReturn(notebook);

        mockMvc.perform(get(URL + "/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET /{id} inexistente deve retornar 404")
    void deveRetornar404QuandoNaoExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new ProdutoNaoEncontradoException("Produto não encontrado"));

        mockMvc.perform(get(URL + "/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensagem").value("Produto não encontrado"));
    }

    @Test
    @DisplayName("GET /{id} com id não numérico deve retornar 400")
    void deveRetornar400ComIdInvalido() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /{id} deve atualizar")
    void deveAtualizar() throws Exception {
        when(service.atualizar(eq(10L), any(ProdutoRequestDTO.class))).thenReturn(notebook);

        mockMvc.perform(put(URL + "/10").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Notebook"));
    }

    @Test
    @DisplayName("DELETE /{id} deve retornar 204")
    void deveExcluir() throws Exception {
        mockMvc.perform(delete(URL + "/10"))
            .andExpect(status().isNoContent());

        verify(service).excluir(10L);
    }

    @Test
    @DisplayName("DELETE /{id} inexistente deve retornar 404")
    void deveRetornar404AoExcluirInexistente() throws Exception {
        doThrow(new ProdutoNaoEncontradoException("Produto não encontrado")).when(service).excluir(99L);

        mockMvc.perform(delete(URL + "/99"))
            .andExpect(status().isNotFound());
    }

    // ===== Consultas derivadas =====

    @Test
    @DisplayName("GET /buscar?nome= deve buscar por nome")
    void deveBuscarPorNome() throws Exception {
        when(service.buscarPorNome("note")).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/buscar").param("nome", "note"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /categoria/{id} deve buscar por categoria")
    void deveBuscarPorCategoria() throws Exception {
        when(service.buscarPorCategoria(1L)).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/categoria/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].categoria.id").value(1));
    }

    @Test
    @DisplayName("GET /preco deve buscar por faixa de preço")
    void deveBuscarPorFaixaDePreco() throws Exception {
        when(service.buscarPorFaixaDePreco(new BigDecimal("100"), new BigDecimal("5000")))
            .thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/preco").param("minimo", "100").param("maximo", "5000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /quantidade deve buscar por quantidade maior que")
    void deveBuscarPorQuantidade() throws Exception {
        when(service.buscarPorQuantidade(5)).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/quantidade").param("quantidade", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    // ===== Consultas @Query (JPQL) =====

    @Test
    @DisplayName("GET /mais-caros?preco= deve buscar produtos mais caros")
    void deveBuscarMaisCaros() throws Exception {
        when(service.buscarProdutosMaisCarosQue(new BigDecimal("1000"))).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/mais-caros").param("preco", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].preco").value(4500.00));
    }

    @Test
    @DisplayName("GET /mais-caros com preço inválido deve retornar 400")
    void deveRetornar400ComPrecoInvalido() throws Exception {
        mockMvc.perform(get(URL + "/mais-caros").param("preco", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensagem").value("O parâmetro 'preco' recebeu um valor inválido."));
    }

    @Test
    @DisplayName("GET /categoria/{id}/query deve buscar por categoria com JOIN")
    void deveBuscarPorCategoriaComQuery() throws Exception {
        when(service.buscarPorCategoriaComQuery(1L)).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/categoria/1/query"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /estoque/valor-total deve retornar o valor total")
    void deveCalcularValorTotal() throws Exception {
        when(service.calcularValorTotalEstoque()).thenReturn(new ValorEstoqueDTO(new BigDecimal("45240.00")));

        mockMvc.perform(get(URL + "/estoque/valor-total"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valorTotal").value(45240.00));
    }

    // ===== Consultas nativas =====

    @Test
    @DisplayName("GET /estoque/baixo?quantidade= deve buscar estoque baixo")
    void deveBuscarEstoqueBaixo() throws Exception {
        when(service.buscarProdutosComEstoqueBaixo(5)).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/estoque/baixo").param("quantidade", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /categoria/nome?nome= deve buscar pelo nome da categoria")
    void deveBuscarPorNomeCategoria() throws Exception {
        when(service.buscarProdutosPorNomeCategoria("Informatica")).thenReturn(List.of(notebook));

        mockMvc.perform(get(URL + "/categoria/nome").param("nome", "Informatica"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].categoria.nome").value("Informatica"));
    }

    @Test
    @DisplayName("GET /estoque/valor-por-categoria deve retornar o valor por categoria")
    void deveCalcularValorPorCategoria() throws Exception {
        when(service.calcularValorEstoquePorCategoria()).thenReturn(List.of(
            new ValorEstoqueCategoriaDTO("Informatica", new BigDecimal("45240.00"))));

        mockMvc.perform(get(URL + "/estoque/valor-por-categoria"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].categoria").value("Informatica"))
            .andExpect(jsonPath("$[0].valorTotal").value(45240.00));
    }
}
