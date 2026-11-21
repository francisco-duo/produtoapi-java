package com.example.produtoapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.produtoapi.dto.CategoriaRequestDTO;
import com.example.produtoapi.dto.CategoriaResponseDTO;
import com.example.produtoapi.exception.CategoriaNaoEncontradaException;
import com.example.produtoapi.exception.ConflitoException;
import com.example.produtoapi.model.Categoria;
import com.example.produtoapi.repository.CategoriaRepository;
import com.example.produtoapi.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private CategoriaService service;

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("deve salvar quando o nome é novo")
        void deveCriar() {
            when(repository.existsByNome("Informatica")).thenReturn(false);
            when(repository.save(any(Categoria.class))).thenReturn(novaCategoria(1L, "Informatica"));

            CategoriaResponseDTO resposta = service.criar(new CategoriaRequestDTO("Informatica"));

            assertThat(resposta.id()).isEqualTo(1L);
            assertThat(resposta.nome()).isEqualTo("Informatica");
        }

        @Test
        @DisplayName("deve lançar conflito quando o nome já existe")
        void deveFalharComNomeDuplicado() {
            when(repository.existsByNome("Informatica")).thenReturn(true);

            assertThatThrownBy(() -> service.criar(new CategoriaRequestDTO("Informatica")))
                .isInstanceOf(ConflitoException.class);

            verify(repository, never()).save(any());
        }
    }

    @Test
    @DisplayName("listarTodas deve converter todas as categorias")
    void deveListarTodas() {
        when(repository.findAll()).thenReturn(List.of(
            novaCategoria(1L, "Informatica"),
            novaCategoria(2L, "Perifericos")
        ));

        assertThat(service.listarTodas())
            .extracting(CategoriaResponseDTO::nome)
            .containsExactly("Informatica", "Perifericos");
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar a categoria quando existe")
        void deveRetornar() {
            when(repository.findById(1L)).thenReturn(Optional.of(novaCategoria(1L, "Informatica")));

            assertThat(service.buscarPorId(1L).nome()).isEqualTo("Informatica");
        }

        @Test
        @DisplayName("deve lançar exceção quando não existe")
        void deveFalhar() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(CategoriaNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar quando o novo nome está livre")
        void deveAtualizar() {
            Categoria categoria = novaCategoria(1L, "Informatica");
            when(repository.findById(1L)).thenReturn(Optional.of(categoria));
            when(repository.existsByNome("Tecnologia")).thenReturn(false);
            when(repository.save(categoria)).thenReturn(categoria);

            CategoriaResponseDTO resposta = service.atualizar(1L, new CategoriaRequestDTO("Tecnologia"));

            assertThat(resposta.nome()).isEqualTo("Tecnologia");
        }

        @Test
        @DisplayName("não deve checar duplicidade quando o nome não mudou")
        void naoDeveChecarDuplicidadeComMesmoNome() {
            Categoria categoria = novaCategoria(1L, "Informatica");
            when(repository.findById(1L)).thenReturn(Optional.of(categoria));
            when(repository.save(categoria)).thenReturn(categoria);

            service.atualizar(1L, new CategoriaRequestDTO("Informatica"));

            verify(repository, never()).existsByNome(anyString());
        }

        @Test
        @DisplayName("deve lançar conflito quando o novo nome já pertence a outra categoria")
        void deveFalharComNomeDuplicado() {
            when(repository.findById(1L)).thenReturn(Optional.of(novaCategoria(1L, "Informatica")));
            when(repository.existsByNome("Perifericos")).thenReturn(true);

            assertThatThrownBy(() -> service.atualizar(1L, new CategoriaRequestDTO("Perifericos")))
                .isInstanceOf(ConflitoException.class);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando a categoria não existe")
        void deveFalharSemCategoria() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(99L, new CategoriaRequestDTO("X")))
                .isInstanceOf(CategoriaNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("excluir")
    class Excluir {

        @Test
        @DisplayName("deve excluir quando existe e não tem produtos")
        void deveExcluir() {
            when(repository.existsById(1L)).thenReturn(true);
            when(produtoRepository.existsByCategoriaId(1L)).thenReturn(false);

            service.excluir(1L);

            verify(repository).deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar exceção quando não existe")
        void deveFalharQuandoNaoExiste() {
            when(repository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.excluir(99L))
                .isInstanceOf(CategoriaNaoEncontradaException.class);

            verify(repository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deve lançar conflito quando a categoria possui produtos")
        void deveFalharComProdutos() {
            when(repository.existsById(1L)).thenReturn(true);
            when(produtoRepository.existsByCategoriaId(1L)).thenReturn(true);

            assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(ConflitoException.class);

            verify(repository, never()).deleteById(any());
        }
    }

    private static Categoria novaCategoria(Long id, String nome) {
        Categoria c = new Categoria();
        ReflectionTestUtils.setField(c, "id", id);
        c.setNome(nome);
        return c;
    }
}
