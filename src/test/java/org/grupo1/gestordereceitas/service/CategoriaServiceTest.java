package org.grupo1.gestordereceitas.service;

import org.grupo1.gestordereceitas.exception.ResourceNotFoundException;
import org.grupo1.gestordereceitas.model.Categoria;
import org.grupo1.gestordereceitas.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @InjectMocks
    private CategoriaService service;

    @Mock
    private CategoriaRepository categoriaRepository;

    // --- Teste para listarTodas() ---
    @Test
    public void deveRetornarListaDeCategoriasCorretamente() {
        // Cenário
        Categoria cat1 = criarCategoria(1L, "Massas");
        Categoria cat2 = criarCategoria(2L, "Sobremesas");
        Categoria cat3 = criarCategoria(3L, "Carnes");
        List<Categoria> categoriasEsperadas = Arrays.asList(cat1, cat2, cat3);

        when(categoriaRepository.findAll()).thenReturn(categoriasEsperadas);

        // Ação
        List<Categoria> categoriasRetornadas = service.listarTodas();

        // Validação
        assertNotNull(categoriasRetornadas);
        assertEquals(categoriasEsperadas.size(), categoriasRetornadas.size());
        assertEquals(categoriasEsperadas.getFirst().getNome(), categoriasRetornadas.getFirst().getNome());

        verify(categoriaRepository, times(1)).findAll();
    }

    // --- Testes para buscarPorId() ---
    @Test
    public void deveRetornarCategoriaQuandoBuscaPorIdExistente() {
        // Cenário
        Long idExistente = 1L;
        Categoria categoriaEsperada = criarCategoria(idExistente, "Sopas");

        when(categoriaRepository.findById(idExistente)).thenReturn(Optional.of(categoriaEsperada));

        // Ação
        Categoria categoriaRetornada = service.buscarPorId(idExistente);

        // Validação
        assertNotNull(categoriaRetornada);
        assertEquals(categoriaEsperada.getId(), categoriaRetornada.getId());
        assertEquals(categoriaEsperada.getNome(), categoriaRetornada.getNome());

        verify(categoriaRepository, times(1)).findById(idExistente);
    }

    @Test
    public void deveLancarExcecaoQuandoBuscarCategoriaPorIdInexistente() {
        // Cenário
        Long idInexistente = 11L;

        when(categoriaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        ResourceNotFoundException exceptionEsperada =
                assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(idInexistente));

        assertEquals("Categoria com ID " + idInexistente + " não encontrada", exceptionEsperada.getMessage());

        verify(categoriaRepository, times(1)).findById(idInexistente);
    }

    // --- Teste para salvar() ---
    @Test
    void deveSalvarCategoriaCorretamente() {
        // Cenário
        Categoria categoriaASalvar = criarCategoria(null, "Saladas");
        Categoria categoriaSalva = criarCategoria(1L, "Saladas");

        when(categoriaRepository.save(categoriaASalvar)).thenReturn(categoriaSalva);

        // Ação
        Categoria categoriaRetornada = service.salvar(categoriaASalvar);

        // Validação
        assertNotNull(categoriaRetornada);
        assertNotNull(categoriaRetornada.getId());
        assertEquals("Saladas", categoriaRetornada.getNome());

        verify(categoriaRepository, times(1)).save(categoriaASalvar);
    }

    // --- Testes para atualizar() ---
    @Test
    void deveAtualizarCategoriaComIdExistente() {
        // Cenário
        Long idExistente = 1L;
        Categoria categoriaExistente = criarCategoria(idExistente, "Sobremesas");
        Categoria categoriaASerAtualizada = criarCategoria(idExistente, "Doces");

        when(categoriaRepository.findById(idExistente)).thenReturn(Optional.of(categoriaExistente));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ação
        Categoria categoriaAtualizada = service.atualizar(idExistente, categoriaASerAtualizada);

        // Validação
        assertNotNull(categoriaAtualizada);
        assertEquals(idExistente, categoriaAtualizada.getId());
        assertEquals("Doces", categoriaAtualizada.getNome());

        verify(categoriaRepository, times(1)).findById(idExistente);
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void deveLancarExcecaoAoTentarAtualizarCategoriaComIdInexistente() {
        // Cenário
        Long idInexistente = 11L;
        Categoria categoriaASerAtualizada = criarCategoria(idInexistente, "Doces");

        when(categoriaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        assertThrows(ResourceNotFoundException.class, () -> service.atualizar(idInexistente, categoriaASerAtualizada));

        verify(categoriaRepository, times(1)).findById(idInexistente);
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    // --- Testes para deletar() ---
    @Test
    void deveDeletarCategoriaComIdExistenteComSucesso() {
        // Cenário
        Long idExistente = 1L;

        when(categoriaRepository.existsById(idExistente)).thenReturn(true);
        doNothing().when(categoriaRepository).deleteById(idExistente);

        // Ação && Validação
        assertDoesNotThrow(() -> service.deletar(idExistente));

        verify(categoriaRepository, times(1)).existsById(idExistente);
        verify(categoriaRepository, times(1)).deleteById(idExistente);
    }

    @Test
    void deveLancarExcecaoAoTentarDeletarCategoriaComIdInexistente() {
        // Cenário
        Long idInexistente = 11L;

        when(categoriaRepository.existsById(idInexistente)).thenReturn(false);

        // Ação && Validação
        ResourceNotFoundException exceptionEsperada = assertThrows(ResourceNotFoundException.class, () ->
                service.deletar(idInexistente));

        assertEquals("Categoria com ID " + idInexistente + " não encontrada para exclusão",
                exceptionEsperada.getMessage()
        );

        verify(categoriaRepository, times(1)).existsById(idInexistente);
        verify(categoriaRepository, never()).deleteById(idInexistente);
    }

    private Categoria criarCategoria(Long id, String nome) {
        return new Categoria(id, nome);
    }
}
