package com.example.produtoapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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

/*
Testes unitários do ProdutoService.

Os repositórios são substituídos por mocks (Mockito), então nenhum banco
de dados é usado: testamos apenas a lógica do service.
*/
@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProdutoService service;

    private Categoria categoria;
    private Produto produto;

    @BeforeEach
    void setUp() {
        categoria = novaCategoria(1L, "Informatica");
        produto = novoProduto(10L, "Notebook", new BigDecimal("4500.00"), 10, categoria);
    }

    // ===== CRUD =====

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("deve salvar o produto com a categoria informada")
        void deveCriarProduto() {
            ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Notebook", "16GB", new BigDecimal("4500.00"), 10, 1L);

            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
            when(repository.save(any(Produto.class))).thenReturn(produto);

            ProdutoResponseDTO resposta = service.criar(dto);

            ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
            verify(repository).save(captor.capture());
            Produto salvo = captor.getValue();

            assertThat(salvo.getNome()).isEqualTo("Notebook");
            assertThat(salvo.getDescricao()).isEqualTo("16GB");
            assertThat(salvo.getPreco()).isEqualByComparingTo("4500.00");
            assertThat(salvo.getQuantidade()).isEqualTo(10);
            assertThat(salvo.getCategoria()).isSameAs(categoria);

            assertThat(resposta.id()).isEqualTo(10L);
            assertThat(resposta.categoria().id()).isEqualTo(1L);
            assertThat(resposta.categoria().nome()).isEqualTo("Informatica");
        }

        @Test
        @DisplayName("deve lançar exceção quando a categoria não existe")
        void deveFalharSemCategoria() {
            ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Notebook", null, BigDecimal.TEN, 1, 99L);

            when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(CategoriaNaoEncontradaException.class);

            verify(repository, never()).save(any());
        }
    }

    @Test
    @DisplayName("listarTodos deve converter todos os produtos")
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(produto));

        List<ProdutoResponseDTO> resposta = service.listarTodos();

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).nome()).isEqualTo("Notebook");
    }

    @Test
    @DisplayName("listarPaginado deve manter a estrutura da página")
    void deveListarPaginado() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(produto), pageable, 1));

        Page<ProdutoResponseDTO> pagina = service.listarPaginado(pageable);

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent().get(0).id()).isEqualTo(10L);
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar o produto quando existe")
        void deveRetornarProduto() {
            when(repository.findById(10L)).thenReturn(Optional.of(produto));

            assertThat(service.buscarPorId(10L).nome()).isEqualTo("Notebook");
        }

        @Test
        @DisplayName("deve lançar exceção quando não existe")
        void deveFalharQuandoNaoExiste() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        }
    }

    @Test
    @DisplayName("deve converter produto sem categoria com categoria nula")
    void deveConverterProdutoSemCategoria() {
        Produto semCategoria = novoProduto(20L, "Antigo", BigDecimal.ONE, 1, null);
        when(repository.findById(20L)).thenReturn(Optional.of(semCategoria));

        assertThat(service.buscarPorId(20L).categoria()).isNull();
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar os campos e a categoria")
        void deveAtualizar() {
            Categoria nova = novaCategoria(2L, "Perifericos");
            ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Notebook Pro", "32GB", new BigDecimal("6000.00"), 5, 2L);

            when(repository.findById(10L)).thenReturn(Optional.of(produto));
            when(categoriaRepository.findById(2L)).thenReturn(Optional.of(nova));
            when(repository.save(produto)).thenReturn(produto);

            ProdutoResponseDTO resposta = service.atualizar(10L, dto);

            assertThat(resposta.nome()).isEqualTo("Notebook Pro");
            assertThat(resposta.descricao()).isEqualTo("32GB");
            assertThat(resposta.preco()).isEqualByComparingTo("6000.00");
            assertThat(resposta.quantidade()).isEqualTo(5);
            assertThat(resposta.categoria().id()).isEqualTo(2L);
        }

        @Test
        @DisplayName("deve lançar exceção quando o produto não existe")
        void deveFalharSemProduto() {
            ProdutoRequestDTO dto = new ProdutoRequestDTO("X", null, BigDecimal.ONE, 1, 1L);
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(99L, dto))
                .isInstanceOf(ProdutoNaoEncontradoException.class);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando a categoria não existe")
        void deveFalharSemCategoria() {
            ProdutoRequestDTO dto = new ProdutoRequestDTO("X", null, BigDecimal.ONE, 1, 99L);
            when(repository.findById(10L)).thenReturn(Optional.of(produto));
            when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(10L, dto))
                .isInstanceOf(CategoriaNaoEncontradaException.class);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("excluir")
    class Excluir {

        @Test
        @DisplayName("deve excluir quando o produto existe")
        void deveExcluir() {
            when(repository.existsById(10L)).thenReturn(true);

            service.excluir(10L);

            verify(repository).deleteById(10L);
        }

        @Test
        @DisplayName("deve lançar exceção quando o produto não existe")
        void deveFalharQuandoNaoExiste() {
            when(repository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(ProdutoNaoEncontradoException.class);

            verify(repository, never()).deleteById(any());
        }
    }

    // ===== Consultas derivadas =====

    @Test
    @DisplayName("buscarPorNome deve delegar para findByNomeContainingIgnoreCase")
    void deveBuscarPorNome() {
        when(repository.findByNomeContainingIgnoreCase("note")).thenReturn(List.of(produto));

        assertThat(service.buscarPorNome("note")).extracting(ProdutoResponseDTO::nome)
            .containsExactly("Notebook");
    }

    @Test
    @DisplayName("buscarPorCategoria deve delegar para findByCategoriaId")
    void deveBuscarPorCategoria() {
        when(repository.findByCategoriaId(1L)).thenReturn(List.of(produto));

        assertThat(service.buscarPorCategoria(1L)).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorFaixaDePreco deve delegar para findByPrecoBetween")
    void deveBuscarPorFaixaDePreco() {
        BigDecimal min = new BigDecimal("100");
        BigDecimal max = new BigDecimal("5000");
        when(repository.findByPrecoBetween(min, max)).thenReturn(List.of(produto));

        assertThat(service.buscarPorFaixaDePreco(min, max)).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorQuantidade deve delegar para findByQuantidadeGreaterThan")
    void deveBuscarPorQuantidade() {
        when(repository.findByQuantidadeGreaterThan(5)).thenReturn(List.of(produto));

        assertThat(service.buscarPorQuantidade(5)).hasSize(1);
    }

    @Test
    @DisplayName("consultas devem retornar lista vazia quando não há resultados")
    void deveRetornarListaVazia() {
        when(repository.findByNomeContainingIgnoreCase("xyz")).thenReturn(List.of());

        assertThat(service.buscarPorNome("xyz")).isEmpty();
    }

    // ===== Consultas @Query (JPQL) =====

    @Test
    @DisplayName("buscarProdutosMaisCarosQue deve converter o resultado")
    void deveBuscarMaisCaros() {
        BigDecimal preco = new BigDecimal("1000");
        when(repository.buscarProdutosMaisCarosQue(preco)).thenReturn(List.of(produto));

        assertThat(service.buscarProdutosMaisCarosQue(preco))
            .extracting(ProdutoResponseDTO::id)
            .containsExactly(10L);
    }

    @Test
    @DisplayName("buscarPorCategoriaComQuery deve converter o resultado")
    void deveBuscarPorCategoriaComQuery() {
        when(repository.buscarPorCategoriaComQuery(1L)).thenReturn(List.of(produto));

        assertThat(service.buscarPorCategoriaComQuery(1L)).hasSize(1);
    }

    @Nested
    @DisplayName("calcularValorTotalEstoque")
    class ValorTotalEstoque {

        @Test
        @DisplayName("deve retornar a soma calculada pelo banco")
        void deveRetornarSoma() {
            when(repository.calcularValorTotalEstoque()).thenReturn(new BigDecimal("45240.00"));

            ValorEstoqueDTO resposta = service.calcularValorTotalEstoque();

            assertThat(resposta.valorTotal()).isEqualByComparingTo("45240.00");
        }

        @Test
        @DisplayName("deve retornar zero quando não há produtos (SUM = null)")
        void deveRetornarZeroQuandoNulo() {
            when(repository.calcularValorTotalEstoque()).thenReturn(null);

            assertThat(service.calcularValorTotalEstoque().valorTotal())
                .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ===== Consultas nativas =====

    @Test
    @DisplayName("buscarProdutosComEstoqueBaixo deve converter o resultado")
    void deveBuscarEstoqueBaixo() {
        Produto mouse = novoProduto(11L, "Mouse", new BigDecimal("80.00"), 3, categoria);
        when(repository.buscarProdutosComEstoqueBaixo(5)).thenReturn(List.of(mouse));

        assertThat(service.buscarProdutosComEstoqueBaixo(5))
            .extracting(ProdutoResponseDTO::nome)
            .containsExactly("Mouse");
    }

    @Test
    @DisplayName("buscarProdutosPorNomeCategoria deve converter o resultado")
    void deveBuscarPorNomeCategoria() {
        when(repository.buscarProdutosPorNomeCategoria("Informatica")).thenReturn(List.of(produto));

        assertThat(service.buscarProdutosPorNomeCategoria("Informatica")).hasSize(1);
    }

    @Nested
    @DisplayName("calcularValorEstoquePorCategoria")
    class ValorEstoquePorCategoria {

        @Test
        @DisplayName("deve converter cada linha Object[] em DTO, aceitando tipos numéricos diferentes")
        void deveConverterLinhas() {
            List<Object[]> linhas = List.of(
                new Object[] { "Informatica", new BigDecimal("45240.00") },
                new Object[] { "Perifericos", 150.5d }
            );
            when(repository.calcularValorEstoquePorCategoria()).thenReturn(linhas);

            List<ValorEstoqueCategoriaDTO> resposta = service.calcularValorEstoquePorCategoria();

            assertThat(resposta).hasSize(2);
            assertThat(resposta.get(0).categoria()).isEqualTo("Informatica");
            assertThat(resposta.get(0).valorTotal()).isEqualByComparingTo("45240.00");
            assertThat(resposta.get(1).categoria()).isEqualTo("Perifericos");
            assertThat(resposta.get(1).valorTotal()).isEqualByComparingTo("150.5");
        }

        @Test
        @DisplayName("deve usar zero quando a soma vier nula")
        void deveUsarZeroQuandoNulo() {
            List<Object[]> linhas = List.<Object[]>of(new Object[] { "Vazia", null });
            when(repository.calcularValorEstoquePorCategoria()).thenReturn(linhas);

            assertThat(service.calcularValorEstoquePorCategoria().get(0).valorTotal())
                .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ===== Helpers =====

    private static Categoria novaCategoria(Long id, String nome) {
        Categoria c = new Categoria();
        // Categoria não tem setId: o id é gerado pelo banco
        ReflectionTestUtils.setField(c, "id", id);
        c.setNome(nome);
        return c;
    }

    private static Produto novoProduto(Long id, String nome, BigDecimal preco,
                                       Integer quantidade, Categoria categoria) {
        Produto p = new Produto();
        p.setId(id);
        p.setNome(nome);
        p.setPreco(preco);
        p.setQuantidade(quantidade);
        p.setCategoria(categoria);
        return p;
    }
}
