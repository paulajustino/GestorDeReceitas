package org.grupo1.gestordereceitas.service;

import org.grupo1.gestordereceitas.exception.ResourceNotFoundException;
import org.grupo1.gestordereceitas.model.Ingrediente;
import org.grupo1.gestordereceitas.repository.IngredienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class IngredienteServiceTest {

    @InjectMocks
    private IngredienteService service;

    @Mock
    private IngredienteRepository ingredienteRepository;

    // --- Teste para listarTodas() ---
    @Test
    public void deveRetornarListaDeIngredientesCorretamente() {
        // Cenário
        Ingrediente ing1 = criarIngrediente(1L, "Arroz");
        Ingrediente ing2 = criarIngrediente(2L, "Limao");
        Ingrediente ing3 = criarIngrediente(3L, "Chocolate");
        List<Ingrediente> ingredientesEsperados = Arrays.asList(ing1, ing2, ing3);

        when(ingredienteRepository.findAll()).thenReturn(ingredientesEsperados);

        // Ação
        List<Ingrediente> ingredientesRetornados = service.listarTodos();

        // Validação
        assertNotNull(ingredientesRetornados);
        assertEquals(ingredientesEsperados.size(), ingredientesRetornados.size());
        assertEquals(ingredientesEsperados.getFirst().getNome(), ingredientesRetornados.getFirst().getNome());

        verify(ingredienteRepository, times(1)).findAll();
    }

    // --- Testes para buscarPorId() ---
    @Test
    public void deveRetornarIngredienteQuandoBuscaPorIdExistente() {
        // Cenário
        Long idExistente = 5L;
        Ingrediente ingredienteEsperado = criarIngrediente(idExistente, "Óleo");

        when(ingredienteRepository.findById(idExistente)).thenReturn(Optional.of(ingredienteEsperado));

        // Ação
        Ingrediente ingredienteRetornado = service.buscarPorId(idExistente);

        // Validação
        assertNotNull(ingredienteRetornado);
        assertEquals(ingredienteEsperado.getId(), ingredienteRetornado.getId());
        assertEquals(ingredienteEsperado.getNome(), ingredienteRetornado.getNome());

        verify(ingredienteRepository, times(1)).findById(idExistente);
    }

    @Test
    public void deveLancarExcecaoQuandoBuscarIngredientePorIdInexistente() {
        // Cenário
        Long idInexistente = 11L;

        when(ingredienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        ResourceNotFoundException exceptionEsperada =
                assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(idInexistente));

        assertEquals("Ingrediente com ID " + idInexistente + " não encontrado", exceptionEsperada.getMessage());

        verify(ingredienteRepository, times(1)).findById(idInexistente);
    }

    // --- Teste para salvar() ---
    @Test
    void deveSalvarIngredienteCorretamente() {
        // Cenário
        Ingrediente ingredienteASalvar = criarIngrediente(null, "Pimentão");
        Ingrediente ingredienteSalvo = criarIngrediente(6L, "Pimentão");

        when(ingredienteRepository.save(ingredienteASalvar)).thenReturn(ingredienteSalvo);

        // Ação
        Ingrediente ingredienteRetornado = service.salvar(ingredienteASalvar);

        // Validação
        assertNotNull(ingredienteRetornado);
        assertNotNull(ingredienteRetornado.getId());
        assertEquals("Pimentão", ingredienteRetornado.getNome());

        verify(ingredienteRepository, times(1)).save(ingredienteASalvar);
    }

    // --- Testes para atualizar() ---
    @Test
    void deveAtualizarIngredienteComIdExistente() {
        // Cenário
        Long idExistente = 2L;
        Ingrediente ingredienteExistente = criarIngrediente(idExistente, "Limao");
        Ingrediente ingredienteASerAtualizado = criarIngrediente(idExistente, "Limão");

        when(ingredienteRepository.findById(idExistente)).thenReturn(Optional.of(ingredienteExistente));
        when(ingredienteRepository.save(any(Ingrediente.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ação
        Ingrediente ingredienteAtualizado = service.atualizar(idExistente, ingredienteASerAtualizado);

        // Validação
        assertNotNull(ingredienteAtualizado);
        assertEquals(idExistente, ingredienteAtualizado.getId());
        assertEquals("Limão", ingredienteAtualizado.getNome());

        verify(ingredienteRepository, times(1)).findById(idExistente);
        verify(ingredienteRepository, times(1)).save(any(Ingrediente.class));
    }

    @Test
    void deveLancarExcecaoAoTentarAtualizarIngredienteComIdInexistente() {
        // Cenário
        Long idInexistente = 11L;
        Ingrediente ingredienteASerAtualizado = criarIngrediente(idInexistente, "Limão");

        when(ingredienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Ação && Validação
        assertThrows(ResourceNotFoundException.class, () -> service.atualizar(idInexistente, ingredienteASerAtualizado));

        verify(ingredienteRepository, times(1)).findById(idInexistente);
        verify(ingredienteRepository, never()).save(any(Ingrediente.class));
    }

    // --- Testes para deletar() ---
    @Test
    void deveDeletarIngredienteComIdExistenteComSucesso() {
        // Cenário
        Long idExistente = 1L;

        when(ingredienteRepository.existsById(idExistente)).thenReturn(true);
        doNothing().when(ingredienteRepository).deleteById(idExistente);

        // Ação && Validação
        assertDoesNotThrow(() -> service.deletar(idExistente));

        verify(ingredienteRepository, times(1)).existsById(idExistente);
        verify(ingredienteRepository, times(1)).deleteById(idExistente);
    }

    @Test
    void deveLancarExcecaoAoTentarDeletarIngredienteComIdInexistente() {
        // Cenário
        Long idInexistente = 11L;

        when(ingredienteRepository.existsById(idInexistente)).thenReturn(false);

        // Ação && Validação
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.deletar(idInexistente));

        assertEquals("Ingrediente com ID " + idInexistente + " não encontrado para exclusão",
                exception.getMessage()
        );

        verify(ingredienteRepository, times(1)).existsById(idInexistente);
        verify(ingredienteRepository, never()).deleteById(idInexistente);
    }

    private Ingrediente criarIngrediente(Long id, String nome) {
        return new Ingrediente(id, nome);
    }
}
